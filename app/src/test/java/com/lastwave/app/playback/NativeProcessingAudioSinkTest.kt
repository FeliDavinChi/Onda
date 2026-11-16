@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.lastwave.app.playback

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.exoplayer.audio.DefaultAudioSink
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.nio.ByteBuffer
import org.junit.Test

class NativeProcessingAudioSinkTest {
    private val fallback = mockk<DefaultAudioSink>(relaxed = true)
    private val processor = mockk<NativePcmAudioProcessor>(relaxed = true)
    private val usb = mockk<ExclusiveUsbOutput>(relaxed = true)
    private val sink = NativeProcessingAudioSink(mockk(relaxed = true), fallback, processor, exclusiveUsb = usb)
    private val source = Format.Builder()
        .setSampleMimeType(MimeTypes.AUDIO_RAW)
        .setSampleRate(44_100)
        .setChannelCount(2)
        .setPcmEncoding(C.ENCODING_PCM_16BIT)
        .build()

    @Test
    fun deadConvertedUsbStreamHandsUnconsumedPcmToAndroid() {
        configureConvertedUsb()
        every { usb.isStreamAlive() } returns false
        val input = ByteBuffer.allocate(8)
        every { fallback.handleBuffer(input, 0, 1) } answers {
            assertThat(input.position()).isEqualTo(0)
            true
        }

        assertThat(sink.handleBuffer(input, 0, 1)).isTrue()
        assertThat(sink.isExclusiveUsbActive()).isFalse()
        verify(exactly = 1) { usb.reset() }
    }

    @Test
    fun busyConvertedUsbStreamWaitsForQueueSpace() {
        configureConvertedUsb()
        every { usb.isStreamAlive() } returns true
        every { processor.getOutput() } returns ByteBuffer.allocate(8)
        every { usb.write(any(), 0, true) } returns false

        assertThat(sink.handleBuffer(ByteBuffer.allocate(8), 0, 1)).isFalse()
        assertThat(sink.isExclusiveUsbActive()).isTrue()
        verify(exactly = 0) { fallback.handleBuffer(any(), any(), any()) }
    }

    @Test
    fun convertedUsbQueueFailureKeepsTheOriginalBufferForAndroidFallback() {
        configureConvertedUsb()
        every { usb.isStreamAlive() } returns true
        val converted = ByteBuffer.allocate(8)
        every { processor.getOutput() } returns converted
        every { usb.write(converted, 0, true) } returns false
        val input = ByteBuffer.allocate(12).apply { position(4) }

        assertThat(sink.handleBuffer(input, 0, 1)).isFalse()
        assertThat(input.position()).isEqualTo(4)

        every { usb.isStreamAlive() } returns false
        every { fallback.handleBuffer(input, 0, 1) } answers {
            assertThat(input.position()).isEqualTo(4)
            true
        }

        assertThat(sink.handleBuffer(input, 0, 1)).isTrue()
        verify(exactly = 1) { processor.queueInput(any()) }
        verify(exactly = 1) { fallback.handleBuffer(input, 0, 1) }
    }

    @Test
    fun retryConsumesOriginalInputAfterConvertedOutputIsAccepted() {
        configureConvertedUsb()
        every { usb.isStreamAlive() } returns true
        val converted = ByteBuffer.allocate(8)
        every { processor.getOutput() } returnsMany listOf(converted, AudioProcessor.EMPTY_BUFFER)
        every { usb.write(converted, 0, true) } returnsMany listOf(false, true)
        val input = ByteBuffer.allocate(8)

        assertThat(sink.handleBuffer(input, 0, 1)).isFalse()
        assertThat(input.position()).isEqualTo(0)
        assertThat(sink.handleBuffer(input, 0, 1)).isTrue()
        assertThat(input.hasRemaining()).isFalse()
        verify(exactly = 1) { processor.queueInput(any()) }
        verify(exactly = 0) { fallback.handleBuffer(any(), any(), any()) }
    }

    @Test
    fun conversionFailureKeepsOriginalInputForAndroid() {
        configureConvertedUsb()
        every { usb.isStreamAlive() } returns true
        every { processor.queueInput(any()) } throws IllegalStateException("converter stopped")
        val input = ByteBuffer.allocate(8)
        every { fallback.handleBuffer(input, 0, 1) } answers {
            assertThat(input.position()).isEqualTo(0)
            true
        }

        assertThat(sink.handleBuffer(input, 0, 1)).isTrue()
        assertThat(sink.isExclusiveUsbActive()).isFalse()
        verify(exactly = 1) { fallback.handleBuffer(input, 0, 1) }
    }

    private fun configureConvertedUsb() {
        every { usb.configure(source, null, null) } returns true
        every { usb.currentRateHz() } returns 48_000
        every { processor.isAvailable } returns true
        every { processor.configure(any()) } returns
            AudioProcessor.AudioFormat(48_000, 2, C.ENCODING_PCM_FLOAT)
        every { processor.nativeOutputSampleRate } returns 48_000
        sink.syncExclusiveUsb(true)
        sink.configure(source, 0, null)
    }
}
