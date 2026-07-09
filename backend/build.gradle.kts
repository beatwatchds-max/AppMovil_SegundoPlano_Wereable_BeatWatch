plugins {
    kotlin("jvm") version "2.0.21"
    id("io.ktor.plugin") version "3.0.0"
    kotlin("plugin.serialization") version "2.0.21"
}

application {
    mainClass.set("com.bitwatch.backend.ApplicationKt")
}

ktor {
    docker {
        localImageName.set("bitwatch-backend")
    }
}

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core:3.0.0")
    implementation("io.ktor:ktor-server-netty:3.0.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")

    // Health check for Render
    implementation("io.ktor:ktor-server-status-pages:3.0.0")

    // MongoDB
    implementation("org.litote.kmongo:kmongo-core:9.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.16")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host:3.0.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.21")
}