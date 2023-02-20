plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
  google()
}

dependencies {
  implementation("com.android.tools.build:gradle:7.4.1")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
  implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")
}
