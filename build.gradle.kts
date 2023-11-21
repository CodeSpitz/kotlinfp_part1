plugins {
    kotlin("multiplatform") version "1.9.20"
}

repositories {
    mavenCentral()
}

kotlin {
    jvm{
        withJava()
        compilations.all {
            kotlinOptions {
                jvmTarget = "21"
                freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
            }
        }
        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
    }
}