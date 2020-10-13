plugins {
  id("com.android.application")
  id("default-android-config")
  kotlin("android")
}

android {
  defaultConfig {
    applicationId = "com.zachklipp.compose.backstack.sample"
  }
  lintOptions {
    // Workaround lint bug.
    disable += "InvalidFragmentVersionForActivityResult"
  }
}

dependencies {
  implementation(project(":compose-backstack-viewer"))
  implementation(Dependencies.AndroidX.appcompat)
  implementation(Dependencies.Compose.foundation)
  implementation(Dependencies.Compose.util)

  androidTestImplementation(Dependencies.AndroidX.junitExt)
  androidTestImplementation(Dependencies.Compose.test)
}
