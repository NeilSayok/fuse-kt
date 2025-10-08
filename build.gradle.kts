plugins {
    kotlin("multiplatform") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    `maven-publish`
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
        groupId = "io.github.neilsayok"
        version = project.version.toString()
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/NeilSayok/fuse-kt")
            credentials {
                username = providers.gradleProperty("gpr.user").orElse(providers.environmentVariable("USERNAME")).orNull
                password = providers.gradleProperty("gpr.key").orElse(providers.environmentVariable("TOKEN")).orNull
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    enabled = false
}