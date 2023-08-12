plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "1.56"
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
}

taboolib {
    description {
        contributors {
            name("坏黑")
            name("枫溪")
            name("ItsFlicker")
        }
        dependencies {
            name("Adyeshach")
        }
    }
    install("common", "common-5")
    install(
        "module-chat",
        "module-configuration",
        "module-effect",
        "module-navigation",
        "module-nms",
        "module-nms-util",
        "module-ui"
    )
    install("expansion-command-helper")
    install("platform-bukkit")
    classifier = null
    version = "6.0.10-117"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("ink.ptms.core:v11900:11900:mapped")
    compileOnly("ink.ptms.core:v11900:11900:universal")

    compileOnly("ink.ptms.adyeshach:all:2.0.0-snapshot-10")

    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}