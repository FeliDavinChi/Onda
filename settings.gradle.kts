pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "SocialMusic"
include(":app", ":core:common", ":core:model", ":core:database", ":core:network", ":core:designsystem", ":domain:music", ":data:music")
