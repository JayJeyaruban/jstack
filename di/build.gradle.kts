plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(project(":core"))

    testImplementation(kotlin("test"))
}
