apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'

android rootProject.ext.defaultAndroidConfig

android {
  defaultConfig {
    applicationId "com.zachklipp.compose.backstack"
  }
}

dependencies {
  implementation project(':backstack')
  implementation deps.androidx.appcompat
  implementation deps.compose.icons
  implementation deps.compose.foundation
  implementation deps.compose.material
  implementation deps.compose.tooling
  implementation deps.kotlin.stdlib
}
