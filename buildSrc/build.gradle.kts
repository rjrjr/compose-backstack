plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
  google()
}

dependencies {
  implementation("com.android.tools.build:gradle:7.0.0-alpha14")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
  implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.0")
}
