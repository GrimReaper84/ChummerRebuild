import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java")
    id("application")
    alias(libs.plugins.javafx)
    alias(libs.plugins.errorprone)
    alias(libs.plugins.runtime)
}

group = "de.shalorian"
// version: gradle.properties (einzige Quelle; der Release-Workflow prüft den Tag dagegen)

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
    // Ohne Leerzeichen: Name des Starters, des App-Images und der Archive.
    applicationName = "ChummerSR4Reborn"
}

// Beta-Menü mit Vorlagen und Testfällen (ADR-063): nur für Entwicklungsläufe. Bewusst nicht in
// applicationDefaultJvmArgs, das käme in Startskripte und Installer.
tasks.named<JavaExec>("run") {
    systemProperty("chummer.beta", layout.projectDirectory.dir("src/test/resources").asFile.absolutePath)
}

// Testfälle für das Beta-Menü neu erzeugen (von Hand, nicht im Build; Ergebnis wird eingecheckt).
tasks.register<JavaExec>("generateSamples") {
    group = "application"
    description = "Schreibt die Testfälle des Beta-Menüs nach src/test/resources/samples."
    mainClass.set("de.shalorian.chummer.samples.GenerateSamples")
    classpath = sourceSets.test.get().runtimeClasspath
    args(layout.projectDirectory.dir("src/test/resources/samples").asFile.absolutePath)
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
    implementation(libs.pdfbox)

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

// Version für Fenstertitel und künftigen Über-Dialog, gelesen von AppInfo.
val generateVersionResource = tasks.register("generateVersionResource") {
    group = "build"
    description = "Schreibt die App-Version in eine Ressource für AppInfo."
    val appVersion = project.version.toString()
    val output = layout.buildDirectory.dir("generated/version")
    inputs.property("version", appVersion)
    outputs.dir(output)
    doLast {
        val file = output.get().file("de/shalorian/chummer/version.properties").asFile
        file.parentFile.mkdirs()
        file.writeText("version=$appVersion\n")
    }
}
sourceSets.main {
    resources.srcDir(generateVersionResource)
}

// Zeichnet das Anwendungs-Icon (Fenster-PNGs und Installer-Icons) aus dem Font-Awesome-Drachen; nur von Hand starten.
tasks.register<JavaExec>("generateIcons") {
    group = "development"
    description = "Schreibt die Fenster- und Installer-Icons."
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("de.shalorian.chummer.icons.GenerateIcons")
    systemProperty("java.awt.headless", "true")
    args(
        layout.projectDirectory.dir("src/main/resources/de/shalorian/chummer").asFile.absolutePath,
        layout.projectDirectory.dir("packaging").asFile.absolutePath)
}

val osName: String = System.getProperty("os.name").lowercase()
val isWindows = osName.contains("win")
val isMac = osName.contains("mac")
val appName: String = application.applicationName

// Das .deb nimmt sein Icon aus einem jpackage-Ressourcenordner (siehe jpackage-Block unten).
val linuxIconDir = layout.buildDirectory.dir("jpackage-resources")
val prepareLinuxIcon = tasks.register<Copy>("prepareLinuxIcon") {
    group = "distribution"
    description = "Kopiert das App-Icon in den jpackage-Ressourcenordner für das .deb."
    from(layout.projectDirectory.file("packaging/icon.png"))
    into(linuxIconDir)
    rename { "$appName.png" }
}

// JavaFX kommt als Module aus der JDK ins Laufzeit-Image (Liberica Full bzw. jdk+fx im CI, ADR-043). Die JavaFX-JARs
// aus Maven würden im Classpath daneben liegen und dort ignoriert; sie fallen deshalb aus dem Image heraus.
tasks.named<Sync>("installDist") {
    exclude("javafx-*.jar")
}

runtime {
    options.set(listOf(
        "--strip-debug", "--compress", "zip-6", "--no-header-files", "--no-man-pages",
        // Deutsche Zahlenformate und Sortierung (z.B. EssenceStringConverter, Katalogdialog).
        "--include-locales=de,en"))
    // Feste Liste statt Vorschlag: Fehlt JavaFX in der JDK, bricht jlink ab, statt ein Image zu bauen, das nicht startet.
    modules.set(listOf(
        "java.base", "java.desktop", "java.logging", "java.prefs", "java.scripting", "java.sql",
        "java.transaction.xa", "java.xml", "jdk.unsupported", "jdk.localedata",
        "javafx.base", "javafx.graphics", "javafx.controls", "javafx.fxml"))
    // Wurzelordner im portablen ZIP trägt den App-Namen.
    imageDir.set(layout.buildDirectory.dir(appName))
    // Portables ZIP je Betriebssystem (unter macOS auch je Architektur), z.B. ChummerSR4Reborn-0.1.0-linux.zip.
    imageZip.set(layout.buildDirectory.file(
        "portable/$appName-${project.version}-${javafx.platform.classifier}.zip"))
    // JavaFX lädt eigene native Bibliotheken; ohne Freigabe warnt Java 25 (und blockiert es künftig).
    val nativeAccess = listOf("--enable-native-access=javafx.graphics")
    launcher {
        noConsole = true
        jvmArgs = nativeAccess
    }
    // Installer für das Betriebssystem des Builds (jpackage baut nicht für fremde Systeme): msi, deb oder dmg.
    jpackage {
        imageName = appName
        jvmArgs = nativeAccess
        appVersion = project.version.toString()
        val common = listOf(
            "--vendor", "GrimReaper84",
            "--description", "Charakterverwaltung für Shadowrun 4 (inoffizielles Fanprojekt)",
            "--copyright", "Copyright (c) 2026 GrimReaper84, GPL-3.0")
        // Anwendungs-Icon (generateIcons): ico für Windows, icns für macOS, png für Linux.
        val iconFile = when {
            isWindows -> "icon.ico"
            isMac -> "icon.icns"
            else -> "icon.png"
        }
        imageOptions = listOf("--icon", layout.projectDirectory.file("packaging/$iconFile").asFile.absolutePath)
        when {
            isWindows -> {
                installerType = "msi"
                installerOptions = common + listOf(
                    "--win-menu", "--win-menu-group", "Shadowrun",
                    "--win-shortcut", "--win-dir-chooser", "--win-per-user-install",
                    // Fest, damit ein neueres MSI eine installierte ältere Version ersetzt.
                    "--win-upgrade-uuid", "8e0d6f3a-5b2c-4c71-9f4e-2a7d1c9b6e58")
            }
            isMac -> {
                installerType = "dmg"
                installerOptions = common + listOf("--mac-package-identifier", "de.shalorian.chummer")
            }
            else -> {
                installerType = "deb"
                // jpackage ersetzt beim .deb das Icon des App-Images durch sein Standard-Icon, außer der
                // Ressourcenordner enthält <Name>.png (gefüllt vom Task prepareLinuxIcon).
                installerOptions = common + listOf(
                    "--resource-dir", linuxIconDir.get().asFile.absolutePath,
                    "--linux-package-name", "chummer-sr4-reborn",
                    "--linux-shortcut", "--linux-menu-group", "Game")
            }
        }
    }
}

// Das Icon ist eine Option, kein deklarierter Input: Images bei Änderungen neu bauen.
tasks.named("jpackageImage") {
    inputs.dir(layout.projectDirectory.dir("packaging"))
    // runtime-Plugin 2.0.1 deklariert nicht, dass es build/jre aus dem Task runtime liest (Gradle 9 bricht sonst ab).
    dependsOn(tasks.named("runtime"))
}
tasks.named("jpackage") {
    inputs.dir(layout.projectDirectory.dir("packaging"))
    if (!isWindows && !isMac) {
        dependsOn(prepareLinuxIcon)
    }
}
