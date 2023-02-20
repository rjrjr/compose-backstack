plugins {
  id("com.android.library")
  id("default-android-config")
  id("release-config")
}

dependencies {
  val composeBom = platform(Dependencies.Compose.bom)
  implementation(composeBom)

  compileOnly(Dependencies.Compose.tooling)

  implementation(Dependencies.Compose.foundation)

  testImplementation(Dependencies.Test.junit)
  testImplementation(Dependencies.Test.truth)

  androidTestImplementation(Dependencies.AndroidX.junitExt)
  androidTestImplementation(composeBom)
  androidTestImplementation(Dependencies.Compose.test)
  androidTestImplementation(Dependencies.Test.truth)
}
