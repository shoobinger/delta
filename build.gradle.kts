plugins {
    groovy
    kotlin("jvm") version "1.4.10"
    idea
}

group = "suive.kotlinls.suive"
version = "0.0.1"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Kotlin.
    implementation(kotlin("stdlib"))

    // Logging.
    implementation("org.tinylog:tinylog-api:2.2.1")
    implementation("org.tinylog:tinylog-impl:2.2.1")
    implementation("org.tinylog:tinylog-api-kotlin:2.2.1")

    // Serialization.
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")

    // Kotlin Compiler.
    implementation("org.jetbrains.kotlin:kotlin-compiler:1.4.21")

    // Maven.
    implementation("org.apache.maven:maven-core:3.0.4")
    implementation("com.jcabi:jcabi-aether:0.10.1")

    // Groovy.
    testImplementation("org.codehaus.groovy:groovy-all:3.0.7")

    // JUnit.
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs = listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
}

tasks.withType<GroovyCompile>().configureEach {
    options.isIncremental = true
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
