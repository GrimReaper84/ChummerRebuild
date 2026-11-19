# CLAUDE.md

Diese Datei gibt Claude Code (claude.ai/code) Hinweise für die Arbeit an diesem Repository.

## Build & Start

```bash
./gradlew run            # Anwendung starten
./gradlew build          # Kompilieren + Tests (die Verifikation)
./gradlew test           # Nur Tests
./gradlew jpackage       # Installer für das aktuelle Betriebssystem (build/jpackage)
./gradlew runtimeZip     # Portables ZIP mit eingebautem Java (build/portable)
./gradlew generateIcons  # Fenster- und Installer-Icons neu zeichnen (von Hand, Ergebnis wird eingecheckt)
./gradlew generateSamples  # Testfälle des Beta-Menüs neu erzeugen (von Hand, Ergebnis wird eingecheckt)
```

**Beta-Menü:** Nur `./gradlew run` setzt `chummer.beta`; dann öffnet das Menü „Beta“ Archetypen, Schergengruppen und
die Testfälle aus `src/test/resources/samples` als ungespeicherte Kopie (ADR-063, `docs/beta_checkliste.md`).

**Java 25** ist Pflicht (per Toolchain erzwungen; Gradle findet auch die JDKs aus `~/.jdks`). Hauptklasse: `de.shalorian.chummer.App`.
Für `jpackage`/`runtimeZip` muss die JDK JavaFX enthalten (Liberica Full, ADR-043).

**Version und Release:** Die Version steht nur in `gradle.properties` (`AppInfo.version()` liest sie zur Laufzeit). Ein
Tag `v<version>` startet `.github/workflows/release.yml` (Installer und ZIPs für Windows, Linux, macOS als
Release-Entwurf); `ci.yml` baut bei Push auf `master` und bei Pull Requests. Ablauf: `README.md`, „Release erstellen“.

## Architektur

**MVC mit FXML.** Views liegen als `.fxml` unter `src/main/resources/de/shalorian/chummer/`, die zugehörigen
Controller unter `src/main/java/de/shalorian/chummer/view/`.

**Package-Struktur:** `model.character` und `view` sind nach Fachbereichen in Unterpakete aufgeteilt, damit jedes
Package überschaubar bleibt:

- `model.character` (Wurzel): `Character` (Aggregatwurzel), `CharacterBiography`, `Contact`, `LogEntry`,
  `CatalogEntry`, `DataPaths`
- `model.character.attribute`, `.skill`, `.quality`, `.magic`, `.technomancer`, `.initiation`, `.spirit`, `.cyberware`, `.metatype`, `.armor`,
  `.weapon`, `.gear`, `.vehicle`, `.lifestyle`, `.equipment`, `.situation` (situative Würfelpools): Modellklassen des jeweiligen Bereichs (z.B.
  `Skill`/`SkillDataLoader` in `.skill`, `SpellData`/`Tradition` in `.magic`); alle Kaufkategorien liegen in
  `equipment.Equipment` (`character.getEquipment()`)
- `view` (Wurzel): `MainWindowController`, `CharacterViewController`, `ViewPreferences` (Einstellungen der
  Oberfläche in den Java-Preferences)
- `view.shell`: Rahmen des Charakterbogens: Seitenleiste (`CharacterSection`, `SectionGroup`, `SectionNavigator`),
  Status-Panel (`SlidePanel`, `FloatingButtonPosition`), Fußzeile (`FooterText`)
- `view.tab`: ein Builder je Bereich des Charakterbogens (Rüstung: `ArmorTabBuilder`), dazu
  `StatusOverviewBuilder`/`StatusHints` (`AttributeTabBuilder`, `SkillTabBuilder` mit
  `KnowledgeSkillSection`, `QualityTabBuilder`, `ContactTabBuilder`, `BiographyTabBuilder`, `MagicTabBuilder`,
  `TechnomancerTabBuilder`, `CyberwareTabBuilder`, `StreetGearTabBuilder`, `WeaponTabBuilder`, `GearTabBuilder`,
  `VehicleTabBuilder`, `LogTabBuilder`), ihre Anzeigewerte (`WeaponDetails`, `GearDetails`, `VehicleDetails`) und die
  gemeinsamen Bausteine `CharacterTabHost`, `TabServices`, `IncrementWiring`
- `view.dialog`: alle modalen Dialog-Controller und ihre Ergebnis-/Handle-Typen
- `view.converter`: JavaFX-`StringConverter`
- `view.grid`: gemeinsame Zeilen-Bausteine (`RowModel`, `RowControls`, `AttributeRowLabel`, `SkillSortMode`)

`model.npc` enthält die Schergengruppen, `model.critter` die Critter (Kataloge, Merkmale), `model.template` die Vorlagen für den Dialog „Neu“ (Archetypen,
gemeinsame Bauanleitung `CharacterSpec`). `persistence` und `utils` bleiben flach. Neue Klassen kommen in das Unterpaket ihres Fachbereichs, nicht in die
Package-Wurzel.

**Controller-Hierarchie:**

- `MainWindowController`: Wurzel; Menüs Datei und Einstellungen, öffnet den NewCharacterDialog, lädt/speichert über
  `CharacterStore`
- `CharacterViewController`: vom Hauptfenster eingebunden; hält die FXML-Knoten (Seiten der Bereiche in
  `fx:define`), Kopf- und Fußzeile, Seitenleiste und Status-Panel, erzeugt die Tab-Builder und implementiert
  `CharacterTabHost`: Die Bereiche melden fachliche Änderungen (z.B. `onQualitiesChanged`), der Controller entscheidet
  zentral, was neu gebaut wird
- `NewCharacterDialogController`, `LogEntryDialogController` und die Auswahldialoge in `view.dialog`: modale Dialoge

**Optik:** Alle Farben stehen als Variablen (`-cr-…`) am Anfang von `style.css`; Schriften Inter und Chakra Petch
liegen unter `resources/.../fonts/` (ADR-049). Regeln des Designsystems: Projektregeln Abschnitt 8, „Designsystem“.

**UI-Refresh:** Die Tab-Builder führen `List<Runnable>`-Refresh-Listen für ihre +/−-Zeilen (`refresh()`). Ändert sich
ein Wert, der andere Zeilen beeinflusst, meldet der Tab `refreshRows()`, und der Controller ruft `refresh()` aller Tabs.

**Daten laden:** Die `XDataLoader` (z.B. `MetatypeDataLoader`, `SkillDataLoader`) lesen ihr XML beim ersten Zugriff
und cachen es; für jeden neuen Charakter gibt es frische Kopien. Die XML-Dateien liegen unter
`src/main/resources/de/shalorian/chummer/data/`.

**Persistenz:** `CharacterStore` wandelt `Character` ↔ `CharacterData` (DTO) und schreibt eingerücktes JSON über
Jackson. Dateiendung `.json`.

## Spielregeln (Shadowrun 4. Edition)

Das Modell setzt die SR4-Regeln durch:

- **Charaktererschaffung:** 400 BP gesamt; 10 BP je Attributpunkt über dem Rassenminimum
- **Fertigkeiten:** 4 BP je Stufe (einzeln) bzw. 10 BP je Stufe (Gruppe); Maximalstufe 6
- **Ressourcen:** Den Ressourcen zugewiesene BP (max. 50) kaufen Ausrüstung zu 5.000 ¥ je BP, also max. 250.000 ¥
  (SR4-Grundregelwerk S. 110, `Character.NUYEN_PER_BP`)
- **Karrieremodus:** Nach dem Abschluss werden die Ressourcen-BP zu Start-Nuyen; Attribute kosten (neue Stufe × 5)
  Karma, Fertigkeiten (neue Stufe × 2) Karma
- **Metatypen:** die 5 Metatypen des GRW (S. 105) mit eigenen Attributbereichen; Metavarianten (Runner-Kompendium)
  sind entfernt, der Loader kennt `<variant>` aber weiterhin

## Sprache

**Alles ist Deutsch:** UI-Texte, XML-Daten, Kommentare, Javadoc, Skills, Projektregeln, `DECISIONS.md`, Pläne,
Analysen, Berichte und Rückfragen an den Nutzer sowie Commit-Vorschläge. Bezeichner (Klassen, Methoden, Felder)
folgen dem bestehenden Code mit überwiegend **englischen** Namen (z.B. `Character.getEssence()`, `Cyberware`);
deutsche Fachbegriffe stehen in Texten und Daten, nicht in Bezeichnern. Spielbegriffe wie im deutschen GRW.

## Regelwerk und Dokumentation

| Was | Pfad |
|---|---|
| Designentscheidungen mit Begründung | `DECISIONS.md` |
| Feature-Stand und Roadmap | `docs/feature_checklist.md` |
| Pläne (fertige in `archiv/`) | `docs/plaene/` |
| Analysen (fertige in `archiv/`) | `docs/analysen/` |
| SR4-Grundregelwerk | `docs/rulebooks/grundregelwerk.pdf` (Scan), `grundregelwerk.txt` (Fließtext), `grundregelwerk_layout.txt` (Tabellen) |

Befehle und Seitenberechnung: `.claude/skills/_gemeinsam/projektregeln.md`, Abschnitt 4.

## Workflows

Ein Feature läuft im **verschlankten Modus** (Standard seit 2026-11-11, Details in den Projektregeln Abschnitt 2):
ein gemeinsamer Plan `docs/plaene/<feature>_plan.md` mit C#-Verhalten, Java-Ist, GRW-Prüfung, Entscheidungen und
Schritten; eine Freigabe; alle Schritte am Stück mit Build nach jedem Schritt; ein Abschluss-Review am Ende.

Beteiligte Skills:

- **csharp-legacy-analyzer**: Verhalten des alten C#-Codes (Abschnitt 4 des Plans)
- **java-analyzer**: Ist-Stand des Java-Codes (Abschnitt 5 des Plans)
- **javafx-planner**: Plan mit Zielarchitektur, JavaFX-UI, Tests und Schritten
- **java-developer**: Umsetzung des freigegebenen Plans
- **catalog-extractor**: Katalogdaten aus dem GRW in die XML-Kataloge übertragen bzw. prüfen (Nebenzweig, eigener
  Schritt oder Schritt im Plan)
- **java-code-quality-reviewer** (Diff-Modus) als letzter Schritt: java-developer startet nach der Umsetzung einen
  eigenen Agenten mit frischem Kontext, der den Diff erst gegen den Plan abgleicht (Plantreue) und dann auf unnötige
  Komplexität, Überabstraktion und Duplikate prüft;
  er berichtet nur, der Nutzer entscheidet, welche Befunde umgesetzt werden

Für Code-Qualitäts-Reviews an bestehendem Code:

- **java-code-quality-reviewer**: prüft den Code und schlägt Verbesserungen vor

## REGELN FÜR CLAUDE

Die folgenden Regeln gelten zwingend für jede Instanz und jede Aktion des Agenten.

1. Nie Code implementieren, ohne ausdrücklich dazu aufgefordert worden zu sein.
2. Git ist schreibgeschützt: keine schreibenden Git-Aktionen, stattdessen Commit-Vorschläge machen.
