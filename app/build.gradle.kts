repositories.mavenCentral()

plugins {
    kotlin("jvm")
}

tasks.register<JavaExec>("run") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "test.kotlin.https.AppKt"
}
