plugins {
    kotlin("jvm") version "1.8.20"
    id("xyz.jpenilla.run-paper") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("io.prometheus:simpleclient:0.16.0")
    implementation("io.prometheus:simpleclient_httpserver:0.16.0")
}

tasks.runServer {
    minecraftVersion("1.19.4")
}

kotlin {
    jvmToolchain(17)
}
