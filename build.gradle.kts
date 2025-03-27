plugins {
    kotlin("jvm") version "2.1.10"
}

group = "sample"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("jstack:core")
    implementation("jstack:di")
    implementation("jstack:rpc")
    implementation("jstack:rpc-jdk")
    implementation("jstack:log")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(23)
}