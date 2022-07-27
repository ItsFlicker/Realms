plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "1.40"
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
}

taboolib {
    description {
        contributors {
            name("坏黑")
            name("枫溪")
        }
        dependencies {
            name("Blockdb")
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
    install("platform-bukkit")
    relocate("ink.ptms.blockdb", "ink.ptms.realms.library.blockdb")
    classifier = null
    version = "6.0.9-39"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("ink.ptms:Blockdb:1.1.0")

    compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("ink.ptms.core:v11900:11900:mapped")
    compileOnly("ink.ptms.core:v11900:11900:universal")

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