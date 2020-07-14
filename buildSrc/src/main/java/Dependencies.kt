import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources

object Versions {
    private const val versionsFile = "versions.properties"

    const val targetSdk = 29
    val agp = loadPropertyFromResources(versionsFile, "androidGradlePluginVersion")
    const val compose = "0.1.0-dev13"
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
        const val foundation = "androidx.ui:ui-foundation:${Versions.compose}"
        const val icons = "androidx.ui:ui-material-icons-extended:${Versions.compose}"
        const val layout = "androidx.ui:ui-layout:${Versions.compose}"
        const val material = "androidx.ui:ui-material:${Versions.compose}"
        const val savedstate = "androidx.ui:ui-saved-instance-state:${Versions.compose}"
        const val test = "androidx.ui:ui-test:${Versions.compose}"
        const val tooling = "androidx.ui:ui-tooling:${Versions.compose}"
    }

    object Kotlin {
        const val binaryCompatibilityValidatorPlugin =
            "org.jetbrains.kotlinx:binary-compatibility-validator:0.2.1"
        val dokka = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}"
        val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
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
