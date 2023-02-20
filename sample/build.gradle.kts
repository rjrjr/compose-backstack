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
  val composeBom = platform(Dependencies.Compose.bom)
  implementation(composeBom)
  implementation(project(":compose-backstack-viewer"))
  implementation(Dependencies.AndroidX.appcompat)
  implementation(Dependencies.Compose.activity)
  implementation(Dependencies.Compose.foundation)
  implementation(Dependencies.Compose.util)

  androidTestImplementation(Dependencies.AndroidX.junitExt)
  androidTestImplementation(composeBom)
  androidTestImplementation(Dependencies.Compose.test)
}
