repositories.mavenCentral()

plugins {
    kotlin("jvm")
}

tasks.register<JavaExec>("run") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "test.kotlin.client.AppKt"
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
}
