import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
  }

  dependencies {
    classpath(Dependencies.androidGradlePlugin)
    classpath(Dependencies.Kotlin.binaryCompatibilityValidatorPlugin)
    classpath(Dependencies.Kotlin.dokka)
    classpath(Dependencies.Kotlin.gradlePlugin)
    classpath(Dependencies.ktlint)
  }
}

apply(plugin = "binary-compatibility-validator")
extensions.configure<kotlinx.validation.ApiValidationExtension> {
  ignoredProjects.add("sample")
}

// See https://stackoverflow.com/questions/25324880/detect-ide-environment-with-gradle
val isRunningFromIde get() = project.properties["android.injected.invoked.from.ide"] == "true"

subprojects {
  repositories {
    google()
    mavenCentral()
    jcenter()
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"

      // Allow warnings when running from IDE, makes it easier to experiment.
      if (!isRunningFromIde) {
        allWarningsAsErrors = true
      }

      // Required while Compose is built on a compiler that is somewhere in between Kotlin
      // 1.3 and 1.4. Otherwise you'll see errors like "Runtime JAR file has version 1.3 which
      // is older than required for API version 1.4"
      apiVersion = "1.3"

      freeCompilerArgs = listOf(
          "-Xopt-in=kotlin.RequiresOptIn"
      )
    }
  }
}

// Dokka config for grouped docs.

repositories {
  jcenter()
}

plugins {
  id("org.jetbrains.dokka")
}
