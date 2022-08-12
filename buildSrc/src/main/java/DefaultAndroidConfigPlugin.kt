import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/** Configures default values for the android plugins (both apps and libraries). */
@Suppress("UnstableApiUsage")
class DefaultAndroidConfigPlugin : Plugin<Project> {

  override fun apply(target: Project) {
    target.apply(plugin = "com.android.base")
    target.apply(plugin = "kotlin-android")

    target.configure<BaseExtension> {
      compileSdkVersion(Versions.compileSdk)

      compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
      }

      defaultConfig {
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      }

      buildFeatures.apply {
        buildConfig = false
        compose = true
      }

      composeOptions {
        kotlinCompilerExtensionVersion = Versions.compose
      }

      packagingOptions {
        excludes += "META-INF/AL2.0"
        excludes += "META-INF/LGPL2.1"
      }
    }
  }
}
