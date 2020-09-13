import org.gradle.api.plugins.JavaBasePlugin.DOCUMENTATION_GROUP
import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

val releaseVersion = loadPropertyFromResources("versions.properties", "releaseVersion")
val composeDevVersion = loadPropertyFromResources("versions.properties", "composeDevVersion")
val isRelease = properties["isRelease"] == "true"

group = "com.zachklipp"
version = "$releaseVersion+$composeDevVersion"
    .let { if (isRelease) it else "$it-SNAPSHOT" }

val projectUrl = "https://github.com/zach-klippenstein/compose-backstack"
val sonatypeUrl = if (isRelease) {
    "https://oss.sonatype.org/service/local/staging/deploy/maven2"
} else {
    "https://oss.sonatype.org/content/repositories/snapshots"
}
val sonatypeUsername get() = (findProperty("SONATYPE_NEXUS_USERNAME") as? String).orEmpty()
val sonatypePassword get() = (findProperty("SONATYPE_NEXUS_PASSWORD") as? String).orEmpty()

val dokkaJar by tasks.creating(Jar::class) {
    group = DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier.set("javadoc")
    from(tasks["dokkaHtml"])
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifact(dokkaJar)

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
                        developerConnection.set("scm:git:git@github.com:zach-klippenstein/compose-backstack.git")
                        url.set(projectUrl)
                    }
                }
            }
        }

        repositories {
            maven {
                url = uri(sonatypeUrl)
                credentials {
                    username = sonatypeUsername
                    password = sonatypePassword
                }
            }
        }
    }

    signing {
        sign(publishing.publications["release"])
    }
}
