plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "hasab.lang"
version = "1.0.0"

application {
    mainClass.set("hasab.cli.HasabCli")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    explicitApi()
}

dependencies {
    implementation("org.ow2.asm:asm:9.7.1")
    implementation("org.ow2.asm:asm-util:9.7.1")
    implementation("org.ow2.asm:asm-tree:9.7.1")
    implementation(kotlin("reflect"))
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.21.0")
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
