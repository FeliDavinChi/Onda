package dev.socialmusic.core.playback

import dev.socialmusic.model.*
import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Exercise the private snapshot file and Android AtomicFile on a pre-33 SDK. */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class SnapshotStorageTest {
    @get:Rule val temporaryFolder = TemporaryFolder()
    private val firstTrack = Track(MusicId("youtube", "abcdefghijk"), "First song", emptyList())
    private val secondTrack = Track(MusicId("youtube", "lmnopqrstuv"), "Second song", emptyList())

    @Test fun serviceRebindReadsFinalOldSnapshotBeforeNewTimelineReplacesIt() = runBlocking {
        withTimeout(10_000) {
            val firstServiceFile = File(temporaryFolder.root, "playback.json")
            SnapshotStorage.write(firstServiceFile, PlaybackSnapshotCodec.encode(PlaybackState(
                queue = listOf(QueueEntry("old-occurrence", firstTrack)), currentIndex = 0,
                status = PlaybackStatus.PLAYING, positionMs = 5678)))

            // Recreating a service gives it a new File object referring to the same private file.
            val reboundServiceFile = File(temporaryFolder.root, "playback.json")
            val resumed = SnapshotStorage.read(reboundServiceFile)
            assertEquals("old-occurrence", resumed.queue[resumed.currentIndex].occurrenceId)
            assertEquals("First song", resumed.currentTrack!!.title)
            assertEquals(5678L, resumed.positionMs)
            assertEquals(PlaybackStatus.PAUSED, resumed.status)

            SnapshotStorage.write(reboundServiceFile, PlaybackSnapshotCodec.encode(PlaybackState(
                queue = listOf(QueueEntry("new-occurrence", secondTrack)), currentIndex = 0,
                status = PlaybackStatus.PLAYING, positionMs = 9012, repeat = RepeatMode.ALL)))
            val newest = SnapshotStorage.read(File(temporaryFolder.root, "playback.json"))
            assertEquals("new-occurrence", newest.queue[newest.currentIndex].occurrenceId)
            assertEquals("Second song", newest.currentTrack!!.title)
            assertEquals(9012L, newest.positionMs)
            assertEquals(RepeatMode.ALL, newest.repeat)
            assertEquals(PlaybackStatus.PAUSED, newest.status)

            // Confirm the newest queue was durably written, independently of the store's cache.
            val onDisk = PlaybackSnapshotCodec.decode(firstServiceFile.readText(Charsets.UTF_8))
            assertEquals("new-occurrence", onDisk.queue[0].occurrenceId)
            assertEquals(9012L, onDisk.positionMs)
        }
    }

    @Test fun pendingFinalSnapshotIsVisibleToTheNextReadWithoutWaitingForWriter() = runBlocking {
        withTimeout(10_000) {
            val file = File(temporaryFolder.root, "playback.json")
            for (position in 0L..49L) {
                SnapshotStorage.write(file, PlaybackSnapshotCodec.encode(PlaybackState(
                    queue = listOf(QueueEntry("active", firstTrack)), currentIndex = 0,
                    status = PlaybackStatus.PLAYING, positionMs = position)))
            }
            val restored = SnapshotStorage.read(File(file.absolutePath))
            assertEquals(49L, restored.positionMs)
            assertEquals("active", restored.queue[0].occurrenceId)
            assertEquals(49L, PlaybackSnapshotCodec.decode(file.readText(Charsets.UTF_8)).positionMs)
        }
    }

    @Test fun missingCorruptAndOversizedFilesRestoreEmptyOnPre33Sdk() = runBlocking {
        withTimeout(10_000) {
            val file = File(temporaryFolder.root, "playback.json")
            assertEquals(PlaybackState(), SnapshotStorage.read(file))
            file.writeText("{invalid json", Charsets.UTF_8)
            assertEquals(PlaybackState(), SnapshotStorage.read(file))
            file.writeBytes(ByteArray(1_000_001) { 120 })
            assertEquals(PlaybackState(), SnapshotStorage.read(file))
        }
    }

    @Test fun oversizedWritePreservesLastValidSnapshotIncludingByteLimit() = runBlocking {
        withTimeout(10_000) {
            val file = File(temporaryFolder.root, "playback.json")
            SnapshotStorage.write(file, PlaybackSnapshotCodec.encode(PlaybackState(
                queue = listOf(QueueEntry("keep", secondTrack)), currentIndex = 0, positionMs = 4321)))
            assertEquals(4321L, SnapshotStorage.read(file).positionMs)
            // UTF-8 size exceeds the bound even though Kotlin's character count does not.
            SnapshotStorage.write(File(file.absolutePath), "\u20ac".repeat(400_000))
            val restored = SnapshotStorage.read(file)
            assertEquals("keep", restored.queue[0].occurrenceId)
            assertEquals(4321L, restored.positionMs)
            assertEquals(4321L, PlaybackSnapshotCodec.decode(file.readText(Charsets.UTF_8)).positionMs)
        }
    }
}
