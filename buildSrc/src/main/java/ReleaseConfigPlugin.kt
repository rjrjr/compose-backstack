import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources

/** Configures projects to be published to Maven. */
class ReleaseConfigPlugin : Plugin<Project> {

  override fun apply(target: Project) {
    val releaseVersion = loadPropertyFromResources("versions.properties", "releaseVersion")
    val isRelease = target.properties["isRelease"] == "true"

    target.apply(plugin = "maven-publish")
    target.apply(plugin = "signing")
    target.apply(plugin = "org.jetbrains.dokka")

    target.group = "com.zachklipp"

    val dokkaJar = target.tasks.register("dokkaJar", Jar::class) {
      group = JavaBasePlugin.DOCUMENTATION_GROUP
      description = "Assembles Kotlin docs with Dokka"
      archiveClassifier.set("javadoc")
      from(target.tasks["dokkaHtml"])
    }

    val sourcesJar = target.tasks.register("sourcesJar", Jar::class) {
      archiveClassifier.set("sources")
      val androidExtension = project.extensions.getByType<LibraryExtension>()
      from(androidExtension.sourceSets.getByName("main").java.srcDirs)
    }

    target.afterEvaluate {
      target.version = "$releaseVersion+${Versions.composeBom}"
        .let { if (isRelease) it else "$it-SNAPSHOT" }

      target.configure<PublishingExtension> {
        repositories {
          maven {
            url = uri(getSonatypeUrl(isRelease))
            credentials {
              val (username, password) = loadSonatypeCredentials()
              this.username = username
              this.password = password
            }
          }
        }

        publications.create("release", MavenPublication::class) {
          from(components["release"])
          artifact(dokkaJar)
          artifact(sourcesJar)

          pom {
            name.set("Compose Backstack")
            description.set("Composable for rendering transitions between backstacks.")
            url.set(projectUrl)
            licenses {
              license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
              }
            }
            developers {
              developer {
                id.set("zach-klippenstein")
                name.set("Zach Klippenstein")
                email.set("zach.klippenstein@gmail.com")
              }
            }
            scm {
              connection.set("scm:git:git://github.com/zach-klippenstein/compose-backstack.git")
              developerConnection.set(
                "scm:git:git@github.com:zach-klippenstein/compose-backstack.git"
              )
              url.set(projectUrl)
            }
          }
        }

        target.configure<SigningExtension> {
          sign(publications["release"])
        }
      }
    }
  }

  private companion object {
    const val projectUrl = "https://github.com/zach-klippenstein/compose-backstack"

    fun getSonatypeUrl(isRelease: Boolean) = if (isRelease) {
      "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    } else {
      "https://oss.sonatype.org/content/repositories/snapshots"
    }

    fun Project.loadSonatypeCredentials() = Pair(
      (findProperty("SONATYPE_NEXUS_USERNAME") as? String).orEmpty(),
      (findProperty("SONATYPE_NEXUS_PASSWORD") as? String).orEmpty()
    )
  }
}
