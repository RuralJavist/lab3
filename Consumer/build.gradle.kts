plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
}

group = "com.itmo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "com.itmo.Main"
        )
    }
}

dependencies {
    implementation("org.apache.activemq:activemq-broker:6.1.1")
    testImplementation(kotlin("test"))
    implementation("com.rabbitmq:amqp-client:5.23.0")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    implementation("ch.qos.logback:logback-classic:1.5.12")
    implementation("org.slf4j:slf4j-simple:2.1.0-alpha1")
    implementation("ch.qos.logback:logback-core:1.5.12")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.1")

}

tasks.test {
    useJUnitPlatform()
}
