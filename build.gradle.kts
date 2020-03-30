import org.jetbrains.dokka.gradle.DokkaTask
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
        classpath(Dependencies.Kotlin.dokka)
        classpath(Dependencies.Kotlin.gradlePlugin)
        classpath(Dependencies.ktlint)
    }
}

subprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

// Dokka config for grouped docs.

repositories {
    mavenCentral()
}

plugins {
    id("org.jetbrains.dokka")
}

tasks.named<DokkaTask>("dokka") {
    outputFormat = "html"
    subProjects = listOf("compose-backstack", "compose-backstack-viewer")
}
