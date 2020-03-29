plugins {
    id("com.android.application")
    id("default-android-config")
    kotlin("android")
}

android {
    defaultConfig {
        applicationId = "com.zachklipp.compose.backstack.sample"
    }
}

dependencies {
    implementation(project(":backstack-viewer"))
    implementation(Dependencies.AndroidX.appcompat)
    implementation(Dependencies.Compose.foundation)
    implementation(Dependencies.Kotlin.stdlib)

    androidTestImplementation(Dependencies.AndroidX.junitExt)
    androidTestImplementation(Dependencies.Compose.test)
}
