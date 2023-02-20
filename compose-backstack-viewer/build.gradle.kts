plugins {
  id("com.android.library")
  id("default-android-config")
  id("release-config")
}

android {
  lintOptions {
    // Workaround for lint bug.
    disable += "InvalidFragmentVersionForActivityResult"
  }
}

dependencies {
  val composeBom = platform(Dependencies.Compose.bom)
  implementation(composeBom)

  compileOnly(Dependencies.Compose.tooling)

  api(project(":compose-backstack"))
  api(project(":compose-backstack-xray"))

  implementation(Dependencies.AndroidX.appcompat)
  implementation(Dependencies.Compose.icons)
  implementation(Dependencies.Compose.foundation)
  implementation(Dependencies.Compose.material)
  implementation(Dependencies.Compose.tooling)

  testImplementation(Dependencies.Test.junit)
  testImplementation(Dependencies.Test.truth)

  androidTestImplementation(composeBom)
  androidTestImplementation(Dependencies.AndroidX.junitExt)
  androidTestImplementation(Dependencies.Compose.test)
}
