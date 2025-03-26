plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":di"))
    implementation(project(":rpc"))
    implementation(project(":log"))

    testImplementation(kotlin("test"))
}
