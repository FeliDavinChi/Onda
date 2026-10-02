plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}
android {
    namespace = "dev.socialmusic.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "dev.socialmusic.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.3.0"
    }
    buildTypes {
        debug { applicationIdSuffix = ".debug" }
        create("staging") { initWith(getByName("debug")); applicationIdSuffix = ".staging"; matchingFallbacks += listOf("debug") }
        release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures { compose = true }
    testOptions { unitTests.isIncludeAndroidResources = true }
}
kotlin { jvmToolchain(17) }
val prepareLicenseAssets by tasks.registering(Copy::class) {
    from(rootProject.file("LICENSE"), rootProject.file("THIRD_PARTY_NOTICES.md"))
    from(rootProject.file("third_party/licenses"))
    into(layout.buildDirectory.dir("generated/licenseAssets/licenses"))
}
android.sourceSets.getByName("main").assets.srcDir(layout.buildDirectory.dir("generated/licenseAssets"))
tasks.named("preBuild") { dependsOn(prepareLicenseAssets) }
dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:playback"))
    implementation(project(":domain:music"))
    implementation(project(":data:music"))
    implementation(platform(libs.compose.bom))
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.navigation.compose)
    implementation(libs.compose.material)
    implementation(libs.compose.icons)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation)
    implementation(libs.datastore)
    implementation(libs.room.runtime)
    implementation(libs.serialization.json)
    implementation(libs.coil)
    implementation(libs.coil.network)
    coreLibraryDesugaring(libs.desugar.nio)
    implementation(libs.coroutines.android)
    ksp(libs.hilt.compiler)
    debugImplementation(libs.compose.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.compose.ui.test)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.coil.test)
    debugImplementation(libs.compose.test.manifest)
}
