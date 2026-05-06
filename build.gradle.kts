import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("net.ltgt.errorprone") version "4.0.1"
}

group = "de.shalorian"
version = "1.1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("de.shalorian.chummer.App")
    applicationName = "Chummer"
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.jspecify:jspecify:1.0.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("org.controlsfx:controlsfx:11.2.1")

    // JavaFX native JARs für alle Plattformen – damit distZip auf Windows, Mac und Linux läuft
    listOf("win", "linux", "mac", "mac-aarch64").forEach { platform ->
        listOf("base", "controls", "fxml", "graphics").forEach { module ->
            runtimeOnly("org.openjfx:javafx-$module:21:$platform")
        }
    }

    errorprone("com.google.errorprone:error_prone_core:2.26.1")
    errorprone("com.uber.nullaway:nullaway:0.10.22")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        option("NullAway:AnnotatedPackages", "de.shalorian.chummer")
        option("NullAway:ExcludedFieldAnnotations", "javafx.fxml.FXML")
        error("NullAway")
    }
}

tasks.named<JavaCompile>("compileTestJava") {
    options.errorprone {
        disable("NullAway")
    }
}

tasks.test {
    useJUnitPlatform()
}
