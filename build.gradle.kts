plugins {
    `java-library`
    id("io.izzel.taboolib") version "1.56"
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
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
        "module-database",
        "module-effect",
        "module-navigation",
        "module-nms",
        "module-nms-util",
        "module-ui"
    )
    install(
        "expansion-command-helper",
        "expansion-persistent-container-object"
    )
    install("platform-bukkit")
    relocate("com.alibaba.fastjson2","ink.ptms.realms.lib.fastjson2")
    classifier = null
    version = "6.0.12-local"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("ink.ptms.core:v12000:12000:mapped")
    compileOnly("ink.ptms.core:v12000:12000:universal")

    compileOnly("ink.ptms.adyeshach:all:2.0.0-snapshot-25")
    taboo("com.alibaba.fastjson2:fastjson2-kotlin:2.0.39-SNAPSHOT")
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