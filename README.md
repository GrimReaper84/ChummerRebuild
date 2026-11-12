# Chummer SR4 Reborn

Eine Charakterverwaltung für **Shadowrun, 4. Edition** auf Basis des deutschen Grundregelwerks. Sie führt durch die
Charaktererschaffung mit Baupunkten, rechnet Kosten und Grenzen nach und begleitet den Charakter danach im
Karrieremodus mit Karma, Nuyen und Protokoll.

Chummer SR4 Reborn ist ein Neuaufbau des Open-Source-Programms Chummer (C#, Windows) in Java/JavaFX und läuft unter
Windows, Linux und macOS.

> Inoffizielles Fanprojekt, siehe [Rechtliches](#rechtliches). Noch in Entwicklung, nicht alle Bereiche sind vollständig.

## Funktionen

- **Charaktererschaffung** mit 400 Baupunkten: Metatyp, Attribute und Sonderattribute (Edge, Magie,
  Resonanz), aktive Fertigkeiten, Fertigkeitsgruppen und Spezialisierungen, Wissensfertigkeiten, Gaben und Handicaps,
  Kontakte. Kosten und Grenzen des Grundregelwerks werden nachgerechnet.
- **Ausrüstung** aus den Ressourcen-BP: Cyberware und Bioware (mit Essenz, Kapazität und Wirkungen), Rüstung,
  Lebensstil, Waffen, Ausrüstung, Fahrzeuge und Drohnen.
- **Magier, Adepten und Technomancer**: Tradition und Zauber, Adeptenkräfte, Komplexe Formen.
- **Würfelpools, Initiative und Zustandsmonitor** werden aus den aktuellen Werten berechnet.
- **Karrieremodus**: Steigerungen mit Karma, Käufe mit Nuyen, Protokoll aller Änderungen.
- **Biografie** mit Beschreibung, Hintergrund und Notizen.
- **Übersichtliche Oberfläche**: Seitenleiste mit allen Bereichen, Status-Übersicht als ausklappbares Panel oder fester
  Bereich, Fußzeile mit BP bzw. Karma, Nuyen und Essenz.

Charaktere werden als JSON-Dateien gespeichert, wo du möchtest. Den genauen Stand je Bereich zeigt
[`docs/feature_checklist.md`](docs/feature_checklist.md).

## Roadmap

Die nächsten großen Schritte, ungefähr in dieser Reihenfolge:

1. ✅ **Cyberware und Bioware vollständig**: alle Implantate des Grundregelwerks, mit ihren Wirkungen auf Attribute,
   Initiative und Würfelpools.
2. ✅ **Rüstung überarbeiten**: Darstellung als Baum mit Detailbereich wie bei Waffen, dazu Zubehör und
   Panzerungsmodifikationen.
3. ✅ **Cybergliedmaßen**: Arme, Beine und Torso mit eigenen Attributen, Verbesserungen, Zubehör und Cyberwaffen.
4. **Magie und Resonanz ausbauen**: Geister und Sprites, Initiation mit Metamagie, Wandlung mit Echos.
5. ✅ **Neue Oberfläche**: grundlegende Überarbeitung von Layout, Navigation und Darstellung der Werte.

Das erste Release erscheint, wenn alle Inhalte des Grundregelwerks umgesetzt sind. Die ausführliche Planung steht in
[`docs/feature_checklist.md`](docs/feature_checklist.md).

## Download und Installation

Die aktuelle Version gibt es auf der Seite [Releases](https://github.com/GrimReaper84/ChummerRebuild/releases). Jedes
Paket bringt sein eigenes Java mit, sonst muss nichts installiert werden.

| System | Installer | Portabel |
|---|---|---|
| Windows | `.msi` | `…-win.zip` |
| Linux (Debian, Ubuntu, Mint, …) | `.deb` | `…-linux.zip` |
| macOS (Apple Silicon) | `.dmg` | `…-mac-aarch64.zip` |

Die Pakete sind nicht signiert, daher warnt dein System beim ersten Start:

- **Windows:** SmartScreen meldet „Der Computer wurde durch Windows geschützt“ → *Weitere Informationen* →
  *Trotzdem ausführen*. Der Installer braucht keine Administratorrechte.
- **macOS:** Die App wird beim ersten Start blockiert → Rechtsklick auf die App → *Öffnen*, oder unter
  *Systemeinstellungen → Datenschutz & Sicherheit* erlauben.
- **Linux:** Installation mit `sudo apt install ./chummer-sr4-reborn_<version>_amd64.deb`; die App erscheint im Menü
  unter *Spiele*.

Portables ZIP: an beliebiger Stelle entpacken und `bin/ChummerSR4Reborn` starten (unter Windows die `.bat`).

## Selbst bauen

Voraussetzung ist eine **JDK 25 mit JavaFX** (z.B. Liberica „Full JDK“). Für Kompilieren, Tests und `run` genügt jede
JDK 25; Installer und portables ZIP übernehmen JavaFX aus der JDK in ihr Laufzeit-Image.

```bash
./gradlew build        # kompilieren und testen
./gradlew run          # Anwendung starten
./gradlew jpackage     # Installer für das aktuelle Betriebssystem (build/jpackage)
./gradlew runtimeZip   # portables ZIP für das aktuelle Betriebssystem (build/portable)
./gradlew generateIcons  # Icons neu zeichnen (nur nach Änderungen an GenerateIcons)
```

## Mitmachen

Das Projekt entsteht mit [Claude Code](https://claude.com/claude-code). Alle arbeiten nach denselben Regeln, die im
Repository liegen:

- [`CLAUDE.md`](CLAUDE.md): Projektüberblick, wird automatisch geladen
- [`.claude/skills/_gemeinsam/projektregeln.md`](.claude/skills/_gemeinsam/projektregeln.md): Arbeitsablauf,
  Code-Konventionen, Regelwerksprüfung
- [`.claude/skills/`](.claude/skills): ein Skill je Arbeitsschritt (Analyse, Plan, Umsetzung, Review) und für Kataloge
- [`DECISIONS.md`](DECISIONS.md): Designentscheidungen mit Begründung

Lokale Einrichtung:

1. JDK 25 installieren und prüfen, dass `./gradlew build` durchläuft.
2. Das Grundregelwerk ist die Wahrheitsquelle und bleibt aus urheberrechtlichen Gründen außerhalb des Repositorys.
   Lege dein eigenes PDF als `docs/rulebooks/grundregelwerk.pdf` ab und erzeuge die Textdateien (Fließtext und
   Tabellen) wie in den Projektregeln, Abschnitt 4, beschrieben.

Niemals Regelwerkstext in versionierte Dateien kopieren: nur Werte, Seitenangaben und kurze Beschreibungen in eigenen
Worten.

## Release erstellen

1. Version in `gradle.properties` setzen (z.B. `version=1.1.0`) und committen. Nur Ziffern (`MAJOR.MINOR.PATCH`, ohne
   `-SNAPSHOT`): MSI- und macOS-Installer verlangen eine numerische Version, der macOS-Installer zusätzlich eine
   erste Stelle größer 0; ein Test prüft das Format.
2. Tag `v1.1.0` anlegen und pushen (`git tag v1.1.0`, dann `git push origin v1.1.0`).
3. GitHub Actions baut die Pakete unter Windows, Linux und macOS und legt einen **Entwurf** an. Prüfen und
   veröffentlichen.

Der Release-Workflow lässt sich auch von Hand starten (*Actions → Release → Run workflow*); dann entstehen die Pakete
nur als Workflow-Artefakte.

## Wie das Projekt entsteht

Die Anwendung wird mit umfangreicher KI-Unterstützung geschrieben (Claude Opus 5.5 über Claude Code), angeleitet,
geprüft und getestet vom Autor.

## Rechtliches

Dies ist ein inoffizielles Fanprojekt. Es steht in keiner Verbindung zu den Rechteinhabern oder Verlagen von
Shadowrun und wird von ihnen weder unterstützt noch genehmigt. Shadowrun ist eine Marke von The Topps Company, Inc.

Das Programm enthält keinen Regelwerkstext, nur Spielwerte, Seitenangaben und kurze Zusammenfassungen in eigenen
Worten. Zum Spielen brauchst du das Grundregelwerk.

## Danksagung

Vorbild ist das Open-Source-Programm Chummer, dessen Verhalten für diesen Neuaufbau analysiert wurde.

Das Anwendungs-Icon ist das Symbol „dragon“ aus [Font Awesome Free](https://fontawesome.com) von Fonticons, Inc.,
lizenziert unter [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/); es wurde umgefärbt und auf einen
petrolfarbenen Hintergrund gesetzt. Die Schaltflächen des Programms nutzen die Icon-Schrift von Font Awesome Free (Schriftdateien
unter [SIL OFL 1.1](https://openfontlicense.org/), Icons unter CC BY 4.0) über [Ikonli](https://kordamp.org/ikonli/).

Die Oberfläche nutzt die Schriften [Inter](https://rsms.me/inter/) (The Inter Project Authors) und
[Chakra Petch](https://fonts.google.com/specimen/Chakra+Petch) (The Chakra Petch Project Authors), beide unter
[SIL OFL 1.1](https://openfontlicense.org/); die Lizenztexte liegen neben den Schriftdateien unter
`src/main/resources/de/shalorian/chummer/fonts/`.

## Lizenz

[GNU General Public License v3.0](LICENSE) © 2026 GrimReaper84
