plugins { alias(libs.plugins.kotlin.jvm) }
kotlin { jvmToolchain(17) }
dependencies {
    api(project(":domain:music"))
    implementation(libs.coroutines.core)
    implementation(libs.newpipe)
    implementation(libs.okhttp)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
}
tasks.withType<Test>().configureEach {
    inputs.property("liveProviderTest", providers.environmentVariable("ONDA_LIVE_PROVIDER_TEST").orElse("false"))
}
