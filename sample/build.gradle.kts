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
    implementation(project(":backstack"))
    implementation(Dependencies.AndroidX.appcompat)
    implementation(Dependencies.Compose.icons)
    implementation(Dependencies.Compose.foundation)
    implementation(Dependencies.Compose.material)
    implementation(Dependencies.Compose.tooling)
    implementation(Dependencies.Kotlin.stdlib)

    androidTestImplementation(Dependencies.AndroidX.junitExt)
    androidTestImplementation(Dependencies.Compose.test)
}
