val kotlinVersion: String = "1.4.20-release-327"

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
    implementation("org.tinylog:tinylog-api:2.2.1")
    implementation("org.tinylog:tinylog-impl:2.2.1")
    implementation("org.tinylog:tinylog-api-kotlin:2.2.1")
    implementation("org.tinylog:slf4j-tinylog:2.2.1")

    // Serialization.
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.12.0"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Kotlin Compiler.
    implementation("org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion")
    implementation("org.jetbrains.kotlin:ide-common-ij201:$kotlinVersion")
//    implementation("org.jetbrains.kotlin:kotlin-plugin-ij201:$kotlinVersion") {
//        this.isTransitive = false
//    }

    // Maven.
    implementation("org.apache.maven:maven-core:3.0.4")
    implementation("com.jcabi:jcabi-aether:0.10.1") {
        exclude("org.kuali.maven.wagons")
        exclude("org.hibernate")
    }

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
