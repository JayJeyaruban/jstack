plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":di"))
    api(project(":log"))
}
