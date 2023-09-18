import java.io.ByteArrayOutputStream
import java.net.URL

plugins {
    idea
    java
    `maven-publish`
    id("xyz.wagyourtail.unimined") version "1.0.5"
    id("org.cadixdev.licenser") version "0.6.1"
    id("org.jetbrains.dokka") version "1.8.10"
}

group = "org.notenoughupdates.moulconfig"

fun cmd(vararg args: String): String? {
    val output = ByteArrayOutputStream()
    val r = exec {
        this.commandLine(args.toList())
        this.isIgnoreExitValue = true
        this.standardOutput = output
        this.errorOutput = ByteArrayOutputStream()
    }
    return if (r.exitValue == 0) output.toByteArray().decodeToString().trim()
    else null
}

val tag = cmd("git", "describe", "--tags", "HEAD")
val hash = cmd("git", "rev-parse", "--short", "HEAD")!!
val isSnapshot = tag == null
version = tag ?: hash

unimined.minecraft {
    version("1.8.9")
    mappings {
        searge()
        mcp("stable", "22-1.8.9")
    }
    minecraftForge {
        loader("11.15.1.2318-1.8.9")
    }
    runs {
        config("client") {
            this.jvmArgs.add("-Dmoulconfig.testmod=true")
            this.args.add(0, "--tweakClass")
            this.args.add(1, "net.minecraftforge.fml.common.launcher.FMLTweaker")
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.nea.moe/releases")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.26")
    compileOnly("org.projectlombok:lombok:1.18.26")
    compileOnly("org.jetbrains:annotations:24.0.1")
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}

sourceSets.main {
    output.setResourcesDir(java.classesDirectory)
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.dokkaHtml {
    dokkaSourceSets {
        create("main") {
            moduleName.set("MoulConfig")
            sourceRoots.from(sourceSets.main.get().allSource)
            classpath.from(tasks.compileJava.get().classpath)

            includes.from(fileTree("docs") { include("*.md") })

            sourceLink {
                localDirectory.set(file("src/main/"))
                remoteUrl.set(URL("https://github.com/NotEnoughUpdates/MoulConfig/blob/$hash/src/main/"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

project.afterEvaluate {
    tasks.named("runClient", JavaExec::class) {
        this.javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(8))
        })
    }
}

license {
    header(project.file("HEADER.txt"))
    properties {
        set("year", 2023)
    }
    skipExistingHeaders(true)
}
val remapJar: Zip by tasks

val noTestJar by tasks.creating(Jar::class) {
    from(zipTree(remapJar.archiveFile))
    archiveClassifier.set("notest")
    exclude("io/github/moulberry/moulconfig/test/*")
    exclude("mcmod.info")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(noTestJar) {
                classifier = ""
            }
            artifact(remapJar) {
                classifier = "test"
            }
            artifact(tasks.jar) {
                classifier = "named"
            }
            pom {
                licenses {
                    license {
                        name.set("LGPL-3.0 or later")
                        url.set("https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/HEAD/COPYING.LESSER")
                    }
                }
                developers {
                    developer {
                        name.set("NotEnoughUpdates contributors")
                    }
                    developer {
                        name.set("Linnea Gräf")
                    }
                }
                scm {
                    url.set("https://github.com/NotEnoughUpdates/MoulConfig")
                }
            }
        }
    }
    repositories {
        if (project.hasProperty("moulconfigPassword")) {
            maven {
                url = uri("https://maven.notenoughupdates.org/releases")
                name = "moulconfig"
                credentials(PasswordCredentials::class)
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}
