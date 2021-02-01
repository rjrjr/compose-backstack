import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources

object Versions {
  private const val versionsFile = "versions.properties"

  const val targetSdk = 29
  val agp = loadPropertyFromResources(versionsFile, "androidGradlePluginVersion")
  private val composeDev = loadPropertyFromResources(versionsFile, "composeDevVersion")
  val compose = "1.0.0-$composeDev"
  val kotlin = loadPropertyFromResources(versionsFile, "kotlinVersion")
  val dokka = loadPropertyFromResources(versionsFile, "dokkaVersion")
}

object Dependencies {
  val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.agp}"

  object AndroidX {
    const val activity = "androidx.activity:activity:1.1.0"
    const val annotations = "androidx.annotation:annotation:1.1.0"
    const val appcompat = "androidx.appcompat:appcompat:1.1.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
    const val fragment = "androidx.fragment:fragment:1.2.2"

    // Note that we're not using the actual androidx material dep yet, it's still alpha.
    const val material = "com.google.android.material:material:1.1.0"
    const val recyclerview = "androidx.recyclerview:recyclerview:1.1.0"

    // Note that we are *not* using lifecycle-viewmodel-savedstate, which at this
    // writing is still in beta and still fixing bad bugs. Probably we'll never bother to,
    // it doesn't really add value for us.
    const val savedstate = "androidx.savedstate:savedstate:1.0.0"
    const val transition = "androidx.transition:transition:1.3.1"
    const val viewbinding = "androidx.databinding:viewbinding:3.6.1"

    const val junitExt = "androidx.test.ext:junit:1.1.1"
  }

  object Compose {
    val foundation = "androidx.compose.foundation:foundation:${Versions.compose}"
    val icons = "androidx.compose.material:material-icons-extended:${Versions.compose}"
    val material = "androidx.compose.material:material:${Versions.compose}"
    val savedstate = "androidx.compose.runtime:runtime-saved-instance-state:${Versions.compose}"
    val test = "androidx.compose.ui:ui-test-junit4:${Versions.compose}"
    val tooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
    val util = "androidx.compose.ui:ui-util:${Versions.compose}"
  }

  object Kotlin {
    const val binaryCompatibilityValidatorPlugin =
      "org.jetbrains.kotlinx:binary-compatibility-validator:0.2.1"
    val dokka = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}"
    val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"

    object Test {
      const val common = "org.jetbrains.kotlin:kotlin-test-common"
      const val annotations = "org.jetbrains.kotlin:kotlin-test-annotations-common"
      const val jdk = "org.jetbrains.kotlin:kotlin-test-junit"
      const val mockito = "com.nhaarman:mockito-kotlin-kt1.1:1.6.0"
    }
  }

  object Test {
    const val junit = "junit:junit:4.13"
    const val truth = "com.google.truth:truth:1.0.1"
  }

  const val ktlint = "org.jlleitschuh.gradle:ktlint-gradle:9.2.0"
}
