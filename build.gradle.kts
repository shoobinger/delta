plugins {
    groovy
    kotlin("jvm") version "1.4.10"
}

group = "suive"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.tinylog:tinylog-api:2.2.1")
    implementation("org.tinylog:tinylog-impl:2.2.1")
    implementation("org.tinylog:tinylog-api-kotlin:2.2.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")

    testImplementation("org.codehaus.groovy:groovy-all:3.0.7")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<GroovyCompile>().configureEach {
    options.isIncremental = true
}
