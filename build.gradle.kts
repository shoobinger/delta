val kotlinVersion: String = "1.4.20-release-327"
val tinylogVersion: String = "2.2.1"
val jacksonVersion: String = "2.12.0"

plugins {
    kotlin("jvm").version("1.4.20-release-327")
    idea
    application
}

group = "suive.delta"
version = "0.1.1"

repositories {
    mavenCentral()
    maven("https://cache-redirector.jetbrains.com/kotlin.bintray.com/kotlin-plugin")
    mavenLocal()
}

dependencies {
    // Kotlin.
    implementation(kotlin("stdlib"))

    // Logging.
    implementation("org.tinylog:tinylog-api:$tinylogVersion")
    implementation("org.tinylog:tinylog-impl:$tinylogVersion")
    implementation("org.tinylog:tinylog-api-kotlin:$tinylogVersion")
    implementation("org.tinylog:slf4j-tinylog:$tinylogVersion")

    // Serialization.
    implementation(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Kotlin Compiler.
    implementation("org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion")
    implementation("org.jetbrains.kotlin:ide-common-ij201:$kotlinVersion")

    // Classpath scanning.
    implementation("io.github.classgraph:classgraph:4.8.98")

    // JUnit.
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.22.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xuse-experimental=kotlin.contracts.ExperimentalContracts",
        "-Xopt-in=kotlin.RequiresOptIn",
        "-Xopt-in=kotlin.time.ExperimentalTime"
    )
    kotlinOptions.jvmTarget = "1.8"
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

application {
    mainClass.set("suive.delta.DeltaKt")
}

val startScripts = tasks.withType<CreateStartScripts>().getByName("startScripts")

val debugArgs = "-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n,quiet=y"

tasks.withType<CreateStartScripts>().configureEach {
    applicationName = startScripts.applicationName
    mainClassName = startScripts.mainClassName
    outputDir = buildDir.resolve("bin")
    classpath = startScripts.classpath
    defaultJvmOpts = mutableListOf(debugArgs)
}
