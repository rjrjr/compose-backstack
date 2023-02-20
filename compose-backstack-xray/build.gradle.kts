plugins {
  id("com.android.library")
  id("default-android-config")
  id("release-config")
}

android {
  namespace = "com.zachklipp.compose.backstack.xray"
  testNamespace = "com.zachklipp.compose.backstack.xray.test"

  lintOptions {
    // Workaround for lint bug.
    disable += "InvalidFragmentVersionForActivityResult"
  }
}

dependencies {
  val composeBom = platform(Dependencies.Compose.bom)
  implementation(composeBom)

  api(project(":compose-backstack"))

  implementation(Dependencies.Compose.foundation)
  implementation(Dependencies.Compose.tooling)

  testImplementation(Dependencies.Test.junit)
  testImplementation(Dependencies.Test.truth)

  androidTestImplementation(Dependencies.AndroidX.junitExt)
  androidTestImplementation(composeBom)
  androidTestImplementation(Dependencies.Compose.test)
}
