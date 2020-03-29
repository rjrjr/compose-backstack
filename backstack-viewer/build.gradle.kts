plugins {
    id("com.android.library")
    id("default-android-config")
    kotlin("android")
    id("org.jetbrains.dokka")
}

dependencies {
    compileOnly(Dependencies.Compose.tooling)

    api(project(":backstack"))

    implementation(Dependencies.AndroidX.appcompat)
    implementation(Dependencies.Compose.icons)
    implementation(Dependencies.Compose.foundation)
    implementation(Dependencies.Compose.material)
    implementation(Dependencies.Compose.tooling)
    implementation(Dependencies.Kotlin.stdlib)

    testImplementation(Dependencies.Test.junit)
    testImplementation(Dependencies.Test.truth)

    androidTestImplementation(Dependencies.AndroidX.junitExt)
    androidTestImplementation(Dependencies.Compose.test)
}
