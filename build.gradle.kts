import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java")
    id("application")
    alias(libs.plugins.javafx)
    alias(libs.plugins.errorprone)
}

group = "de.shalorian"
version = "1.1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass.set("de.shalorian.chummer.App")
    applicationName = "Chummer"
}

javafx {
    version = libs.versions.javafx.runtime.get()
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation(libs.jspecify)
    implementation(libs.jackson.databind)
    implementation(libs.controlsfx)
    implementation(libs.miglayout.javafx)
    implementation(libs.ikonli.javafx)
    implementation(libs.ikonli.fontawesome6.pack)

    // JavaFX native JARs für alle Plattformen – damit distZip auf Windows, Mac und Linux läuft
    val javafxVersion = libs.versions.javafx.runtime.get()
    listOf("win", "linux", "mac", "mac-aarch64").forEach { platform ->
        listOf("base", "controls", "fxml", "graphics").forEach { module ->
            runtimeOnly("org.openjfx:javafx-$module:$javafxVersion:$platform")
        }
    }

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        option("NullAway:AnnotatedPackages", "de.shalorian.chummer")
        option("NullAway:ExcludedFieldAnnotations", "javafx.fxml.FXML")
        // FXML-Handler ruft der FXMLLoader per Reflection auf.
        option("UnusedMethod:ExemptingMethodAnnotations", "javafx.fxml.FXML")
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
