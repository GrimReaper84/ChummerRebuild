# Gemeinsame Projektregeln – Chummer-Rebuild

Gilt für alle Workflow-Skills (`csharp-legacy-analyzer`, `java-analyzer`, `javafx-planner`,
`java-developer`, `java-code-quality-reviewer`). Jeder Skill liest diese Datei, bevor er arbeitet.
Hier stehen die Dinge, die sich im Projekt als wichtig herausgestellt haben — meist, weil sie einmal
schiefgegangen sind.

## Inhalt

1. Projekt in einem Absatz
2. Workflow und Freigaben
3. Artefakte: wo was liegt
4. Regelwerk (GRW) als Wahrheitsquelle
5. Scope-Politik
6. Code-Konventionen
7. Bewährte Bausteine (nicht neu erfinden)
8. UI-Konventionen
9. Verifikation
10. Nacharbeiten nach einer Umsetzung

---

## 1. Projekt in einem Absatz

Neuaufbau des C#-WinForms-Tools **Chummer** (Charaktergenerator für **Shadowrun 4. Edition**, nicht
SR5) als plattformunabhängige **JavaFX**-Anwendung (Java 21, Gradle, Error Prone + NullAway, JUnit 5,
Jackson, ControlsFX, MigLayout, Ikonli/FontAwesome 6). UI-Texte, XML-Daten, Kommentare und Javadoc
sind **Deutsch** (Details in Abschnitt 6). Architektur und Package-Struktur: siehe `CLAUDE.md` im
Projektroot.

## 2. Workflow und Freigaben

Ein Feature durchläuft fünf Schritte, jeweils mit eigenem Skill:

```
csharp-legacy-analyzer → java-analyzer → javafx-planner → java-developer → java-code-quality-reviewer
  (Ist-Verhalten C#)      (Ist-Stand Java)  (Plan)           (Umsetzung)     (Abschluss-Review, Diff)
```

Der fünfte Schritt läuft **automatisch am Ende jeder Umsetzung** (Nutzervorgabe 2026-09-30):
`java-developer` startet nach grünem Build einen eigenen Agenten mit frischem Kontext, der den Diff im
Diff-Modus des Reviewers prüft, mit Schwerpunkt auf unnötiger Komplexität, Überabstraktion und
Duplikaten. Frischer Kontext, weil der Autor des Codes die eigenen Entscheidungen kaum infrage stellt.
Der Review-Agent ändert nichts; seine Befunde landen im Abschlussbericht, und der Nutzer gibt frei,
welche umgesetzt werden. Das Abschluss-Review entfällt, wenn die Umsetzung selbst aus Review-Befunden
bestand.

Unabhängig davon kann `java-code-quality-reviewer` jederzeit für bestehenden Code aufgerufen werden.

**Nach jedem Schritt anhalten und berichten.** Der Nutzer gibt jeden Schritt einzeln frei. Ein fertiger
Plan ist *keine* Freigabe zur Umsetzung — Code wird nur geschrieben, wenn der Nutzer das ausdrücklich
sagt ("leg los", "setz das um"). Auch nicht als "kleine Reparatur" nebenbei. Grund: Der Nutzer will
die Kontrolle darüber behalten, wann Code entsteht, und Zwischenergebnisse prüfen.

**Entscheidungen, die fachlich oder im Scope offen sind, gehören dem Nutzer.** Dafür gezielt
nachfragen (AskUserQuestion), mit einer Empfehlung als erster Option. Kleine technische Details, die
sich aus Code oder Konventionen klar ergeben, selbst entscheiden und im Ergebnis erwähnen.

**Abkürzungen, die der Nutzer bereits bestätigt hat:**
- Reiner UI-Struktur-Abgleich (Layout eines Tabs gegen die alten `*.Designer.cs`) braucht nicht den
  vollen Workflow — direkt Designer.cs extrahieren → mit Java-Ist abgleichen → `javafx-planner`-Kurzplan.
- Ist der Java-Ist-Stand aus der laufenden Sitzung schon bekannt, darf die Java-Analyse knapp ausfallen.

## 3. Artefakte: wo was liegt

| Was | Pfad |
|---|---|
| C#-Analyse | `docs/analysen/<feature>_analyse.md` |
| Java-Analyse | `docs/analysen/<feature>_java_analyse.md` |
| Plan | `docs/plaene/<feature>_plan.md` |
| Feature-Stand | `docs/feature_checklist.md` |
| GRW (PDF, 411 S.) | `docs/rulebooks/Grundregelwerk_SR4.pdf` |
| GRW (OCR-Text) | `docs/rulebooks/Grundregelwerk_SR4_ocr.txt` (Seiten durch `\f` getrennt) |
| C#-Original | `/home/luca/Chummer4_OriginalCode/chummer/Chummer/` (`cls*.cs`, `frm*.cs`) |
| C#-Daten-XML | `/home/luca/Chummer4_OriginalCode/chummer/Chummer/bin/Release/data/*.xml` |
| Alte UI-Struktur | `/home/luca/Chummer4_OriginalCode/chummer/Chummer/*.Designer.cs` (v.a. `frmCreate`/`frmCareer`) |
| Java-Daten-XML | `src/main/resources/de/shalorian/chummer/data/` |

`<feature>` ist ein kurzer deutscher Slug in snake_case (z.B. `cyberware`, `wissensfertigkeiten`).
Jeder Skill liest die Artefakte seiner Vorgänger aus diesen Pfaden und schreibt sein eigenes dorthin —
so kann der nächste Schritt auch in einer neuen Sitzung ohne Kontextverlust weitermachen.

## 4. Regelwerk (GRW) als Wahrheitsquelle

Das deutsche SR4-Grundregelwerk ist die maßgebliche Quelle, **nicht** der C#-Code. Das C#-Original
enthält Bugs, Hausregeln und viel Inhalt aus Zusatzbänden. Deshalb wird jede Fachregel und jeder
Katalogwert gegen das GRW geprüft, bevor er als gesichert gilt.

**Suchen:** `grep -n "<Begriff>" docs/rulebooks/Grundregelwerk_SR4_ocr.txt`.
Englische C#-Begriffe haben oft eine eigene deutsche Übersetzung, z.B. *Submersion* → **Wandlung**,
*Sensitive System* → **Immunabstoßung**, *Street Samurai* → Straßensamurai. Findet die englische Suche
nichts, gezielt nach dem deutschen Begriff suchen, bevor "kein GRW-Beleg" gefolgert wird. Genau dieser
Fehler hat schon einmal ein Kern-Feature fälschlich als Zusatzband eingestuft.

**Seite bestimmen:** PDF-Seite = Anzahl `\f` bis zur Zeile + 1:
```bash
echo $(( $(head -n <ZEILE> docs/rulebooks/Grundregelwerk_SR4_ocr.txt | tr -cd '\f' | wc -c) + 1 ))
```
Die gedruckte Buchseite liegt in der Regel **eine** Seite darunter (PDF 121 = Buch S. 120). In
Zitaten die gedruckte Seite angeben und im Zweifel am Bild nachsehen.

**Bild-Verifikation:** Der OCR-Text taugt für Fließtext, bei **Zahlentabellen und Namen** ist er
unzuverlässig (Spalten vertauscht, Umlaute zerstört). Tabellenwerte, Kosten und Eigennamen deshalb am
Seitenbild gegenlesen, bevor sie in Code oder Daten landen:
```bash
gs -dQUIET -dSAFER -dBATCH -dNOPAUSE -sDEVICE=pdfwrite -dFirstPage=<N> -dLastPage=<M> \
   -sOutputFile=<scratchpad>/seiten.pdf docs/rulebooks/Grundregelwerk_SR4.pdf 2>/dev/null
```
Danach die kleine PDF mit dem Read-Tool öffnen (max. ~20 Seiten pro Aufruf). Die volle PDF ist zu
groß zum direkten Lesen. Eindeutiger Fließtext braucht keine Bildprüfung.

**Zitieren:** Fachregeln in Analysen, Plänen und Javadoc mit Seite und kurzem Wortlaut belegen, z.B.
`SR4-Grundregelwerk S. 359: "Nur Standard- und Alphaware können bei der Charaktererschaffung gekauft
werden."` So kann man später nachvollziehen, woher eine Regel kommt.

**Nichts erfinden.** Katalogdaten (Zauber, Qualities, Implantate …) nur aus einer tatsächlich
gelesenen Quelle übernehmen, nicht aus dem Gedächtnis.

## 5. Scope-Politik

- **GRW-Kern zuerst.** Was nur im C#-Code oder in Zusatzbänden steht (erkennbar u.a. an `<source>` ≠
  `SR4` in den C#-XMLs), wird nicht stillschweigend übernommen. Solche Punkte als "zurückgestellt,
  kein GRW-Beleg" markieren und dem Nutzer zur Entscheidung vorlegen.
- **Kataloge klein und verifiziert starten** (z.B. 2+ Einträge pro Kategorie). Die Vervollständigung
  ist ein eigener Folgeauftrag.
- **Keine Halbfunktionen.** Lieber eine Kategorie ganz weglassen, als sie ohne ihre Kernmechanik
  anzubieten (Beispiel: Cybergliedmaßen ohne Attributersatz).
- **Abwärtskompatibilität von Speicherdateien ist nicht nötig** (das Tool ist noch nicht im Einsatz),
  aber unbekannte Namen beim Laden überspringen statt abzustürzen. Formatänderungen im Ergebnis
  erwähnen.
- Offene Folgeaufträge und Zurückgestelltes landen in `docs/feature_checklist.md`.

## 6. Code-Konventionen

- **Ein Top-Level-Typ pro Datei.** Keine nested/inner Klassen, Records, Enums oder Interfaces — auch
  nicht `private`. Hilfstypen bekommen eine eigene Datei, package-private wenn nur lokal gebraucht.
  Das ist eine ausdrückliche, dauerhafte Nutzervorgabe.
- **Package nach Fachbereich:** `model.character.<bereich>`, `view.dialog`, `view.converter`,
  `view.grid`. `persistence` und `utils` bleiben flach. Neue Klassen nie in die Package-Wurzel, wenn es
  einen passenden Unterbereich gibt.
- **Null-Handling:** jspecify `@NonNull`/`@Nullable`, NullAway ist als Fehler aktiv. Nullable Felder
  **direkt** prüfen (`if (field == null) return;`), keine lokale Hilfsvariable nur für NullAway. Nur wenn
  ein Lambda das Feld einfängt und NullAway wirklich einen Build-Fehler wirft, strukturell lösen.
- **Javadoc** für öffentliche und fachlich relevante APIs: Zweck, Regelbezug mit GRW-Zitat und Seite,
  Vorbedingungen, bewusste Abweichungen vom C#-Original. Nicht den Methodennamen wiederholen.
- **Sprache:** UI-Texte, XML-Daten, Kommentare und Javadoc auf Deutsch. Bezeichner folgen dem
  bestehenden Code (überwiegend englische Klassen-/Methodennamen wie `getEssence()`, deutsche
  Fachbegriffe nur in Texten und Daten).
- **Dezimalregeln** (z.B. Essenz) mit `BigDecimal` rechnen und an genau einer fachlichen Stelle runden.
  `double`-Summen erzeugen Artefakte an Ganzzahlgrenzen.
- **Fachlogik in `Character` bzw. den Modellklassen**, nicht im Controller. Eine zentrale Methode pro
  Regel, keine Duplikate (das C#-Original rechnet dieselbe Summe teils viermal).
- Bestehende Error-Prone-Warnungen (`UnusedMethod` an `@FXML`-Handlern, `JavaLangClash` bei
  `Character`) sind bekannt. Neue Warnungen anderer Art vermeiden.

## 7. Bewährte Bausteine (nicht neu erfinden)

| Aufgabe | Vorbild |
|---|---|
| Katalogeintrag | `record XData(...) implements CatalogEntry` (z.B. `ComplexFormData`, `CyberwareData`) |
| XML-Loader | `XDataLoader`: statischer `synchronized` Cache, `disallow-doctype-decl`, `childText()`, Pfad als Konstante in `DataPaths` |
| Charakter-Instanz | Klasse mit Referenz auf `XData` + veränderlichem Zustand (Rating, Grade …), z.B. `ComplexForm`, `Cyberware` |
| Quality-Effekt | neuer Wert in `QualityBonusType` + Tag in `QualityDataLoader.BONUS_TAGS` + Auswertung in `Character` |
| Persistenz | DTO-Klasse `XEntry` in `persistence` (public Felder, Default-Konstruktor), Feld in `CharacterData`, Mapping in `CharacterStore` (`toData`/`applyX`) |
| Auswahl-Dialog | Subklasse von `CatalogSelectDialogController<T>` + eigene FXML (Vorbild `ComplexFormSelectDialog`) |
| Rating-Zeile mit +/− | `RowModel`-Implementierung + `wireIncrementable()` (BP/Karma-Logik, Bestätigung) |
| Tab-Inhalt | `buildXTab()` baut komplett neu auf; nach Änderungen Tab + abhängige Anzeigen neu bauen |

Vorhandene Muster bevorzugen und nur erweitern, wenn sie nicht passen. Erweiterungen der Basisklassen
klein halten und im Ergebnis erwähnen.

## 8. UI-Konventionen

- **Neue Tabs zuerst gegen das alte `*.Designer.cs`** (TabPage in `frmCreate.Designer.cs`/
  `frmCareer.Designer.cs`) abgleichen: Controls, Gruppierung, Reihenfolge übernehmen, Pixelpositionen
  nicht. Bewusst verbesserte Muster (getrennte Zeilentypen statt Mehrzweck-Controls, +/−-Stepper mit
  `canIncrement()`, BP/Karma inline) nicht zurückbauen.
- **Grids:** `MigPane` mit inhaltsbasierten Spalten, `addRowCells()`, `preventShrink()`,
  `withRightFiller()`. Buttons über `useIcon()` mit FontAwesome-Icon, Text wird Tooltip.
- **Nicht nutzbare Elemente in Listen sichtbar, aber deaktiviert** zeigen, nicht ausblenden. Der Nutzer
  sieht dann, dass es sie gibt und warum sie gesperrt sind. Ganze Tabs dürfen weiterhin verschwinden,
  bis ihre Voraussetzung erfüllt ist (Magie/Technomancer).
- **In `build*Tab()` kein `setDisable()` aus Live-Zustand**, wenn der Tab bei der relevanten Änderung
  nicht neu gebaut wird. Prüfung stattdessen im Klick-Handler mit Warn-Notification. Das war schon
  einmal ein Bug (Button blieb nach Quality-Erwerb gesperrt).
- **Hinweise/Limits:** `Notifications.create()...showWarning()` (ControlsFX), weiche Warnung statt harter
  Sperre, sofern die Regel nicht eindeutig ein Verbot ist.
- `Alert`/`TextInputDialog` über `applyDialogStyle()` ans Dark-Theme hängen. Neue Control-Typen auf
  Styling in `style.css` prüfen (fehlende Regeln fallen auf das helle Modena-Theme zurück).
- **Karrieremodus:** Käufe kosten Karma mit Bestätigung (`karmaChargeFailed()`/`confirmKarmaSpend()`),
  Erstellungsmodus rechnet in BP. Beide Modi bei jedem Feature mitdenken.

## 9. Verifikation

- **`./gradlew build`** (Compile + Tests + Checks) ist die Verifikation. Anzahl Tests und
  Fehlschläge berichten.
- **Die App nicht starten** (`./gradlew run`). Das Display ist der echte Desktop des Nutzers, und es gibt
  keine Klick-Automatisierung. UI-Verhalten prüft der Nutzer selbst.
- Keine TestFX/UI-Test-Infrastruktur ohne Auftrag einführen. Fachlogik so bauen, dass sie ohne UI
  testbar ist.
- **GRW-Rechenbeispiele als Unit-Tests nachrechnen** (z.B. "1,5 Cyberware + 2,5 Bioware → 2,75"), dazu
  Grenzfälle.
- Tests nie löschen oder abschwächen, um grün zu werden.

## 10. Nacharbeiten nach einer Umsetzung

- `docs/feature_checklist.md`: Status und Anmerkungen des Bereichs aktualisieren, Zurückgestelltes
  festhalten.
- Im Abschlussbericht Abweichungen vom Plan, neue GRW-Funde und offene Punkte klar benennen.
- Nicht committen, außer der Nutzer bittet darum.
