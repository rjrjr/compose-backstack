import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

val versions = loadProperties("$projectDir/src/main/resources/versions.properties")
val agpVersion = versions["androidGradlePluginVersion"]
val kotlinVersion = versions["kotlinVersion"]
dependencies {
    implementation("com.android.tools.build:gradle:$agpVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}
