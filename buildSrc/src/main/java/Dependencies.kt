object Versions {
  const val compileSdk = 31
  const val minSdk = 21
  const val targetSdk = 30
  const val composeCompiler = "1.4.2"
  const val composeBom = "2023.01.00"
}

object Dependencies {
  object AndroidX {
    const val appcompat = "androidx.appcompat:appcompat:1.6.1"

    // Note that we're not using the actual androidx material dep yet, it's still alpha.
    const val material = "com.google.android.material:material:1.1.0"

    const val junitExt = "androidx.test.ext:junit:1.1.1"
  }

  object Compose {
    const val activity = "androidx.activity:activity-compose:1.6.1"
    const val bom = "androidx.compose:compose-bom:${Versions.composeBom}"
    const val foundation = "androidx.compose.foundation:foundation"
    const val icons = "androidx.compose.material:material-icons-extended"
    const val material = "androidx.compose.material:material"
    const val test = "androidx.compose.ui:ui-test-junit4"
    const val tooling = "androidx.compose.ui:ui-tooling"
    const val util = "androidx.compose.ui:ui-util"
  }

  object Kotlin {
    const val binaryCompatibilityValidatorPlugin =
      "org.jetbrains.kotlinx:binary-compatibility-validator:0.7.1"

    object Test {
      const val common = "org.jetbrains.kotlin:kotlin-test-common"
    }
  }

  object Test {
    const val junit = "junit:junit:4.13"
    const val truth = "com.google.truth:truth:1.0.1"
  }

  const val ktlint = "org.jlleitschuh.gradle:ktlint-gradle:9.2.0"
}
