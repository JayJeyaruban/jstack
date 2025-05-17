plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    api(project(":log"))
    implementation("org.slf4j:slf4j-api:2.0.17")
}
