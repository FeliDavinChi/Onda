plugins { alias(libs.plugins.kotlin.jvm) }
kotlin { jvmToolchain(17) }
dependencies { api(project(":domain:music")); implementation(libs.coroutines.core); testImplementation(libs.coroutines.test); testImplementation(libs.junit) }
