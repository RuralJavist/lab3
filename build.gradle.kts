plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "org.itmo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.activemq:activemq-broker:6.1.1")
    testImplementation(kotlin("test"))

}

tasks.test {
    useJUnitPlatform()
}
