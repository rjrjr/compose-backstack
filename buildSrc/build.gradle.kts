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

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

val versions = loadProperties("$projectDir/src/main/resources/versions.properties")
val agpVersion = versions["androidGradlePluginVersion"]
val kotlinVersion = versions["kotlinVersion"]
val dokkaVersion = versions["dokkaVersion"]
dependencies {
    implementation("com.android.tools.build:gradle:$agpVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
}
