plugins {
    kotlin("jvm") version "2.0.21"
}

group = "hasab.lang"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    explicitApi()
}

sourceSets {
    main {
        kotlin.srcDir("src/main/kotlin")
    }
    test {
        kotlin.srcDir("src/test/kotlin")
        dependencies {
            implementation(kotlin("test"))
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
