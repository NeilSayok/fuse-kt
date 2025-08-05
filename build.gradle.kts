plugins {
    kotlin("multiplatform") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

group = "io.github.neilsayok"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)

    // JVM target
    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    // JavaScript target
    js(IR) {
        browser()
        nodejs()
    }
    
    // WebAssembly target
    wasmJs {
        browser()
    }

    // Native targets
    // iOS
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // macOS
    macosX64()
    macosArm64()

    // Linux
    linuxX64()

    // Windows
    mingwX64()
    
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            }
        }
        
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        jvmMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
            }
        }
        
        jvmTest {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:5.11.0")
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("Fuse-kt")
            description.set("A lightweight fuzzy-search library for Kotlin multiplatform, ported from Fuse.js")
            url.set("https://github.com/neilsayok/fuse-kt")
            
            licenses {
                license {
                    name.set("Apache License 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }
            }
            
            developers {
                developer {
                    id.set("neilsayok")
                    name.set("Sayok Dey Majumder")
                    email.set("sayokdeymajumder1998@gmail.com")
                }
            }
            
            scm {
                connection.set("scm:git:git://github.com/neilsayok/fuse-kt.git")
                developerConnection.set("scm:git:ssh://github.com:neilsayok/fuse-kt.git")
                url.set("https://github.com/neilsayok/fuse-kt")
            }
        }
    }
    
    repositories {
        maven {
            name = "sonatype"
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            
            credentials {
                username = findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey = findProperty("signingKey") as String? ?: System.getenv("SIGNING_KEY")
    val signingPassword = findProperty("signingPassword") as String? ?: System.getenv("SIGNING_PASSWORD")
    
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            
            username.set(findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME"))
            password.set(findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD"))
        }
    }
}