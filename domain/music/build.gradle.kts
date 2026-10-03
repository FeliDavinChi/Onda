plugins { alias(libs.plugins.kotlin.jvm) }
kotlin { jvmToolchain(17) }
dependencies {
    api(project(":core:model"))
    api(libs.coroutines.core)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
}
