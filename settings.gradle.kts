pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") {
            content {
                includeGroup("com.github.TeamNewPipe")
                includeGroup("com.github.teamnewpipe")
            }
        }
    }
}
rootProject.name = "Onda"
include(":app", ":core:common", ":core:model", ":core:database", ":core:network", ":core:designsystem", ":domain:music", ":data:music")
include(":core:playback")
