plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(project(":di"))
    implementation(project(":rpc"))

    testImplementation(kotlin("test"))
}
