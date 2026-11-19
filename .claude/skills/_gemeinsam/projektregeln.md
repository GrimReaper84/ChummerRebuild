# Gemeinsame Projektregeln – Chummer-Rebuild

Gilt für alle Workflow-Skills (`csharp-legacy-analyzer`, `java-analyzer`, `javafx-planner`,
`java-developer`, `java-code-quality-reviewer`, `catalog-extractor`). Jeder Skill liest diese Datei, bevor er arbeitet.
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
9. Verifikation und Fehlersuche
10. Nacharbeiten nach einer Umsetzung

---

## 1. Projekt in einem Absatz

Neuaufbau des C#-WinForms-Tools **Chummer** (Charaktergenerator für **Shadowrun 4. Edition**, nicht
SR5) als plattformunabhängige **JavaFX**-Anwendung (Java 25, Gradle, Error Prone + NullAway, JUnit 5,
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
Duplikaten. Vorher gleicht er den Diff gegen den Plan ab (Plantreue: Schritte, Akzeptanzkriterien,
Nutzerentscheidungen; seit 2026-11-18). Frischer Kontext, weil der Autor des Codes die eigenen
Entscheidungen kaum infrage stellt.
Der Review-Agent ändert nichts; seine Befunde landen im Abschlussbericht, und der Nutzer gibt frei,
welche umgesetzt werden. Das Abschluss-Review entfällt, wenn die Umsetzung selbst aus Review-Befunden
bestand.

Unabhängig davon kann `java-code-quality-reviewer` jederzeit für bestehenden Code aufgerufen werden.

`catalog-extractor` ist ein Nebenzweig: Er legt Katalogdaten aus dem GRW an und prüft sie. Ein
Feature-Plan nennt die Kataloge, die er braucht; ihre Übertragung ist ein eigener freigegebener Schritt
(im verschlankten Modus innerhalb der Umsetzung, wenn der Plan ihn aufführt).

**Verschlankter Modus (Nutzerentscheidung 2026-11-11, Standard bis auf Widerruf, aus dem
Fallout2d20-Projekt übernommen):** Um Tokens zu sparen, läuft ein Feature so:
1. **Ein gemeinsames Dokument** `docs/plaene/<feature>_plan.md` statt drei: C#-Verhalten, Java-Ist,
   GRW-Prüfung, Scope-Entscheidungen und Umsetzungsschritte. Die Analyse-Skills liefern ihre Ergebnisse
   als Abschnitte dieses Plans (Plan-Vorlage Abschnitt 4 „Gesichertes Altverhalten“ und 5 „Bestehende
   Java-Struktur“); eigene `_analyse.md`/
   `_java_analyse.md` entfallen. Fachliche Entscheidungen werden weiterhin vor bzw. im Plan per
   AskUserQuestion eingeholt.
2. **Eine Freigabe** für den ganzen Plan.
3. **Alle Umsetzungsschritte am Stück**, `./gradlew build` nach jedem Schritt, kein Halt dazwischen.
4. **Ein** Abschluss-Review im frischen Kontext am Ende des Features statt eines pro Teilschritt; der
   Nutzer wählt die Befunde aus.

Grund: getrennte Analysedokumente lesen dieselben Dateien mehrfach, Reviews pro Teilschritt kosten je
~60–80k Tokens. Separate Analysedokumente nur, wenn der Nutzer sie ausdrücklich verlangt oder ein
Feature so groß ist, dass der Plan sonst unlesbar wird (dann vorher fragen).

**Nach jedem Schritt anhalten und berichten** (außerhalb des verschlankten Modus; dort nach dem Plan
und nach dem Abschluss-Review). Der Nutzer gibt jeden Schritt einzeln frei. Ein fertiger
Plan ist *keine* Freigabe zur Umsetzung — Code wird nur geschrieben, wenn der Nutzer das ausdrücklich
sagt ("leg los", "setz das um"). Auch nicht als "kleine Reparatur" nebenbei. Grund: Der Nutzer will
die Kontrolle darüber behalten, wann Code entsteht, und Zwischenergebnisse prüfen.

**Entscheidungen, die fachlich oder im Scope offen sind, gehören dem Nutzer.** Dafür gezielt
nachfragen (AskUserQuestion), mit einer Empfehlung als erster Option. Kleine technische Details, die
sich aus Code oder Konventionen klar ergeben, selbst entscheiden und im Ergebnis erwähnen.

**Git ist schreibgeschützt.** Nie committen, stagen, stashen, pushen oder Branches wechseln; schreibende
Git-Aktionen macht nur der Nutzer. Wenn ein Commit sinnvoll ist, eine Commit-Nachricht vorschlagen.

**Abkürzungen, die der Nutzer bereits bestätigt hat:**
- Reiner UI-Struktur-Abgleich (Layout eines Tabs gegen die alten `*.Designer.cs`) braucht nicht den
  vollen Workflow — direkt Designer.cs extrahieren → mit Java-Ist abgleichen → `javafx-planner`-Kurzplan.
- Ist der Java-Ist-Stand aus der laufenden Sitzung schon bekannt, darf die Java-Analyse knapp ausfallen.

## 3. Artefakte: wo was liegt

| Was | Pfad |
|---|---|
| Designentscheidungen mit Begründung | `DECISIONS.md` (Projektroot) |
| Plan (verschlankt: inkl. Analysen) | `docs/plaene/<feature>_plan.md`; fertige nach `docs/plaene/archiv/` |
| C#-/Java-Analyse (nur auf Wunsch) | `docs/analysen/<feature>_analyse.md`, `..._java_analyse.md`; fertige nach `docs/analysen/archiv/` |
| Katalogbericht | `docs/analysen/<katalog>_katalog.md` |
| Feature-Stand + Roadmap | `docs/feature_checklist.md` (Roadmap: Abschnitt „Prioritisierte Reihenfolge“) |
| GRW (PDF, 411 S.) | `docs/rulebooks/grundregelwerk.pdf` (Original-Scan) |
| GRW (Fließtext) | `docs/rulebooks/grundregelwerk.txt` (Lesereihenfolge, Seiten durch `\f` getrennt) |
| GRW (Tabellen) | `docs/rulebooks/grundregelwerk_layout.txt` (spaltentreu, gleiche Seitenumbrüche) |
| C#-Original | `/home/luca/Chummer4_OriginalCode/chummer/Chummer/` (`cls*.cs`, `frm*.cs`) |
| C#-Daten-XML | `/home/luca/Chummer4_OriginalCode/chummer/Chummer/bin/Release/data/*.xml` |
| Alte UI-Struktur | `/home/luca/Chummer4_OriginalCode/chummer/Chummer/*.Designer.cs` (v.a. `frmCreate`/`frmCareer`) |
| Java-Daten-XML | `src/main/resources/de/shalorian/chummer/data/` |

`<feature>` ist ein kurzer deutscher Slug in snake_case (z.B. `cyberware`, `wissensfertigkeiten`).
Jeder Skill liest die Artefakte seiner Vorgänger aus diesen Pfaden und schreibt sein eigenes dorthin —
so kann der nächste Schritt auch in einer neuen Sitzung ohne Kontextverlust weitermachen. Ein Dokument
eines älteren Features kann im Archiv liegen; fehlt es ganz, ist das kein Fehler, dann zählt der Code.

## 4. Regelwerk (GRW) als Wahrheitsquelle

Das deutsche SR4-Grundregelwerk ist die maßgebliche Quelle, **nicht** der C#-Code. Das C#-Original
enthält Bugs, Hausregeln und viel Inhalt aus Zusatzbänden. Deshalb wird jede Fachregel und jeder
Katalogwert gegen das GRW geprüft, bevor er als gesichert gilt.

**Dateien:** Das PDF ist ein Scan von 2009 mit schwacher OCR-Schicht. Beide Textdateien stammen aus
einer eigenen OCR (`ocrmypdf --force-ocr -l deu`, dann `pdftotext -enc UTF-8` bzw. `pdftotext -enc
UTF-8 -layout`); die OCR-PDF selbst wird nicht aufbewahrt. Neu erzeugen nur bei Bedarf, z.B. nach einem
besseren Scan.

**Suchen:** `grep -n "<Begriff>" docs/rulebooks/grundregelwerk.txt`.
Englische C#-Begriffe haben oft eine eigene deutsche Übersetzung, z.B. *Submersion* → **Wandlung**,
*Sensitive System* → **Immunabstoßung**, *Street Samurai* → Straßensamurai. Findet die englische Suche
nichts, gezielt nach dem deutschen Begriff suchen, bevor "kein GRW-Beleg" gefolgert wird. Genau dieser
Fehler hat schon einmal ein Kern-Feature fälschlich als Zusatzband eingestuft.

**Seite bestimmen:** PDF-Seite = Anzahl `\f` bis zur Zeile + 1:
```bash
echo $(( $(head -n <ZEILE> docs/rulebooks/grundregelwerk.txt | tr -cd '\f' | wc -c) + 1 ))
```
Die gedruckte Buchseite liegt in der Regel **eine** Seite darunter (PDF 121 = Buch S. 120). In
Belegen die gedruckte Seite angeben und im Zweifel am Bild nachsehen.

**Tabellen:** In `grundregelwerk.txt` sind Tabellenspalten auseinandergerissen. Tabellen zuerst in
`grundregelwerk_layout.txt` lesen (gleiche Seite, Spalten bleiben in ihrer Zeile).

**Bild-Verifikation:** Auch die Layout-Datei ist OCR. Bei **Zahlentabellen und Namen** bleibt sie
fehleranfällig (Ziffern verlesen, Umlaute zerstört). Tabellenwerte, Kosten und Eigennamen deshalb am
Seitenbild gegenlesen, bevor sie in Code oder Daten landen:
```bash
gs -dQUIET -dSAFER -dBATCH -dNOPAUSE -sDEVICE=pdfwrite -dFirstPage=<N> -dLastPage=<M> \
   -sOutputFile=<scratchpad>/seiten.pdf docs/rulebooks/grundregelwerk.pdf 2>/dev/null
```
Danach die kleine PDF mit dem Read-Tool öffnen (max. ~20 Seiten pro Aufruf). Die volle PDF ist zu
groß zum direkten Lesen. Eindeutiger Fließtext braucht keine Bildprüfung.

**Belegen:** Fachregeln mit gedruckter Seite und einer Zusammenfassung **in eigenen Worten** belegen, z.B.
`SR4-Grundregelwerk S. 359: bei der Erschaffung nur Standard- und Alphaware.` So kann man später nachvollziehen,
woher eine Regel kommt. Wörtliche Kurzzitate sind nur in Analysen und Plänen erlaubt (`docs/plaene`,
`docs/analysen`; gitignored, bleiben lokal).

**Kein Regelwerkstext 1:1 in versionierten Dateien (Nutzervorgabe 2026-11-11).** Alles, was ins Repository kommt
(Java-Code samt Javadoc und Kommentaren, XML-Kataloge, FXML, UI-Texte, versionierte Doku wie
`docs/feature_checklist.md`), enthält nur Werte, Namen, Seitenangaben und Beschreibungen **in eigenen Worten**, nie
wörtlich übernommene Sätze aus dem GRW. Grund: Das Projekt ist öffentlich (GPL-3.0), der Regelwerkstext ist
urheberrechtlich geschützt. Wer aus einem Plan Belege in Javadoc übernimmt, formuliert sie dabei um.

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
- **Quelle ist Pflicht (ADR-061):** Jeder neue Katalogeintrag bekommt `<source>` mit einem Kürzel aus `books.xml`
  (sonst blendet der Regelwerk-Filter ihn aus; ein Test prüft das). Inhalte eines Zusatzbands kommen erst mit dessen
  Eintrag in `books.xml`. Neue Katalog-Records mit Quelle implementieren `Sourced`.

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
- **Javadoc** für öffentliche und fachlich relevante APIs: Zweck, Regelbezug mit GRW-Seite und eigener
  Zusammenfassung (kein Wortlaut, Abschnitt 4),
  Vorbedingungen, bewusste Abweichungen vom C#-Original. Nicht den Methodennamen wiederholen.
- **Sprache: Deutsch** (Nutzervorgabe, bestätigt 2026-11-11). Deutsch sind UI-Texte, XML-Daten,
  Kommentare, Javadoc, Testnamen-Kommentare, Skills, Projektregeln, `DECISIONS.md`, Pläne, Analysen,
  Berichte und Rückfragen an den Nutzer sowie Commit-Vorschläge. Bezeichner folgen dem bestehenden
  Code (überwiegend englische Klassen-/Methodennamen wie `getEssence()`, deutsche Fachbegriffe nur in
  Texten und Daten). Spielbegriffe so, wie sie im deutschen GRW stehen.
- **Moderne Java-Idiome (Nutzervorgabe 2026-11-11, Java 25):** Code ist generell auf das Sprachlevel der
  Projekt-Java-Version ausgelegt (derzeit Java 25; bei einem Versionswechsel diese Regel und den
  Reviewer-Skill anpassen). Die aktuelle Form statt älterer Entsprechungen: `getFirst()`/`getLast()`/`reversed()` statt `get(0)`/`get(size() - 1)`, `Stream.toList()`,
  `List.of`/`Set.of`/`Map.of`, Records, Switch-Ausdrücke, Pattern Matching (`instanceof Foo foo`, Record- und
  Typ-Muster im `switch`, auch bei DOM-Knoten: `node instanceof Element el` statt `getNodeType()` + Cast), Textblöcke für mehrzeilige feste Texte, unbenannte Variablen `_` für ungenutzte
  Lambda- und Catch-Parameter (z.B. `(_, _, val) -> …`, `catch (IOException _)`), flexible Konstruktor-Rümpfe,
  wo vor `this(…)`/`super(…)` geprüft werden muss. Achtung: `List.of`/`Set.of` lehnen `null` ab, auch in
  `contains(null)`.
- **Dezimalregeln** (z.B. Essenz) mit `BigDecimal` rechnen und an genau einer fachlichen Stelle runden.
  `double`-Summen erzeugen Artefakte an Ganzzahlgrenzen.
- **Fachlogik in `Character` bzw. den Modellklassen**, nicht im Controller. Eine zentrale Methode pro
  Regel, keine Duplikate (das C#-Original rechnet dieselbe Summe teils viermal).
- Bekannte Error-Prone-Warnung: `AvoidCommonTypeNames` bei `Character`. Neue Warnungen vermeiden.
  `@FXML`-Handler sind von `UnusedMethod` ausgenommen (`build.gradle.kts`). Gewollte Identitätsvergleiche
  (`==` auf dieselbe Instanz, z.B. Auswahl im Baum) gezielt an der Methode mit
  `@SuppressWarnings("ReferenceEquality") // Identität gewollt: …` kennzeichnen, nicht global abschalten.

## 7. Bewährte Bausteine (nicht neu erfinden)

| Aufgabe | Vorbild |
|---|---|
| Katalogeintrag | `record XData(...) implements CatalogEntry` (z.B. `ComplexFormData`, `CyberwareData`) |
| XML-Loader | `XDataLoader`: statischer `synchronized` Cache, `disallow-doctype-decl`, `childText()`, Pfad als Konstante in `DataPaths` |
| Charakter-Instanz | Klasse mit Referenz auf `XData` + veränderlichem Zustand (Rating, Grade …), z.B. `ComplexForm`, `Cyberware` |
| Quality-Effekt | neuer Wert in `QualityBonusType` + Tag in `QualityDataLoader.BONUS_TAGS` + Auswertung in `Character` |
| Persistenz | DTO-Klasse `XEntry` in `persistence` (public Felder, Default-Konstruktor), Feld in `CharacterData`, Mapping in `CharacterStore` (`toData`/`applyX`) |
| Auswahl-Dialog | Subklasse von `CatalogSelectDialogController<T>` + eigene FXML (Vorbild `ComplexFormSelectDialog`) |
| Tab-Inhalt | eigener `XTabBuilder` in `view.tab` (Vorbild `ContactTabBuilder`): Konstruktor mit FXML-Container(n), `CharacterTabHost` und ggf. `IncrementWiring`; `setCharacter`, `build()` baut komplett neu auf, `refresh()` für Zeilen-Refresher. Wirkt eine Änderung auf andere Tabs, eine fachliche Rückmeldung in `CharacterTabHost` ergänzen (z.B. `onImplantsChanged`) und im `CharacterViewController` entscheiden, was neu gebaut wird; Tabs rufen sich nie gegenseitig auf |
| Dialoge, Hinweise, Abbuchung | `host.services()` (`TabServices`): `openDialog`, `warn`, `confirmYesNo`, `karmaChargeFailed` (optional mit Zusatzhinweis)/`nuyenChargeFailed`, `bookKarma`/`bookNuyen` |
| +/−-Zeile | `IncrementWiring.wire(...)` bzw. `wireFree(...)` mit einer `RowModel`-Implementierung; Knöpfe und Anzeigen nur über `RowControls.create()` bzw. `RowControls.minusButton()`/`plusButton()` |
| Zahlen-Spinner (Schergen- und Critter-Bogen) | `IntegerSpinner.create(min, max, wert)`: feste Breite, schrumpft nicht, damit die Zahl sichtbar bleibt |
| Zustandsmonitor-Kästchen | `view.grid.MonitorBoxRow.build(...)` (Status-Panel, Schergen-Bogen) |
| NSC-Werteblock | `model.npc.NpcStatblock`: ein `Character` im Spielleiter-Modus plus Ausrüstungstext (ADR-062); Anzeige über `view.npc.StatblockBuilder` |
| Critter | `model.critter.CritterTemplate.statblock(...)`: Metatyp aus dem Katalog (`CritterMetatype`, ±3), Merkmale in `CritterTraits` am `Character` (ADR-064) |
| Vorlage im Dialog „Neu“ | `model.template.DocumentTemplate` + Eintrag in `Templates.available(...)` (ADR-063) |

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
- **In `build()` eines Tabs kein `setDisable()` aus Live-Zustand**, wenn der Tab bei der relevanten Änderung
  nicht neu gebaut wird. Prüfung stattdessen im Klick-Handler mit Warn-Notification. Das war schon
  einmal ein Bug (Button blieb nach Quality-Erwerb gesperrt).
- **Hinweise/Limits:** `Notifications.create()...showWarning()` (ControlsFX), weiche Warnung statt harter
  Sperre, sofern die Regel nicht eindeutig ein Verbot ist.
- `Alert`/`TextInputDialog` über `applyDialogStyle()` ans Dark-Theme hängen. Neue Control-Typen auf
  Styling in `style.css` prüfen (fehlende Regeln fallen auf das helle Modena-Theme zurück).
- **Designsystem (ADR-049):** siehe unten; neue UI nutzt die vorhandenen Variablen und Klassen, statt
  eigene Farben, Schriften oder Abstände einzuführen.
- **Karrieremodus:** Käufe kosten Karma mit Bestätigung (`karmaChargeFailed()`, im Spielleiter-Modus kostenlos, ADR-060),
  Erstellungsmodus rechnet in BP. Beide Modi bei jedem Feature mitdenken.

### Designsystem

Quelle ist `src/main/resources/de/shalorian/chummer/style.css`; diese Zusammenfassung beschreibt die Regeln,
die Werte stehen nur dort. Ändert sich das Designsystem, beides nachziehen.

**Grundsatz:** dunkle Oberfläche in Grau-Blau, **ein** Akzent (Türkis), Orange nur für Hinweise, Rot nur für
Fehler/Überzug. Farbe trägt Bedeutung, nicht Dekoration.

**Farben:** ausschließlich die Variablen `-cr-…` aus `.root` am Dateianfang, nie Hex-Werte in Regeln, FXML
oder Java (`setStyle`).

| Rolle | Variablen | Einsatz |
|---|---|---|
| Flächen | `-cr-bg` < `-cr-surface-alt` < `-cr-surface` < `-cr-raised` < `-cr-hover` | Fenstergrund, Karten, erhöhte Controls, Hover; Tiefe durch Helligkeit, nicht durch Schatten |
| Linien | `-cr-border`, `-cr-border-strong` | Kartenrahmen, Trennlinien; „strong“ für Controls (Buttons, Eingaben) |
| Text | `-cr-text` > `-cr-text-soft` > `-cr-muted` > `-cr-faint` | Werte/Titel, normaler Text, Beschriftungen, Nebensächliches |
| Akzent | `-cr-accent`, `-cr-accent-bg`, `-cr-accent-bg-hover`, `-cr-accent-border` | verstärkte Werte, aktiver Eintrag, Gruppenüberschriften der Seitenleiste; Hintergrund für Primärbutton, Auswahl, gewählter Toggle |
| Hinweis | `-cr-warn`, `-cr-warn-bg`, `-cr-warn-border`, `-cr-warn-text` | weiche Warnungen, Hinweiskarten |
| Fehler | `-cr-danger` | BP/Karma überzogen, körperlicher Schaden |
| Geistig | `-cr-stun` | geistiger Schaden im Zustandsmonitor |

JavaFX-Standards sind daran gebunden (`-fx-accent`, `-fx-focus-color`); neue Control-Typen brauchen eigene
Regeln, sonst erscheint das helle Modena-Theme.

**Schriften** (liegen unter `resources/.../fonts/`, SIL OFL):
- **Inter** (Regular/Medium/SemiBold) für allen Fließtext, Grundgröße 13px.
- **Chakra Petch** (Medium/SemiBold) nur für Überschriften und große Zahlen: Seiten- und Dialogtitel,
  Charaktername, Gruppen der Seitenleiste, Abschnittstitel, Kennzahlen, Attribut- und Poolwerte.
- Größenstufen: 24 Seitentitel · 22 Kennzahl · 15–18 Abschnitt/Werte · 13–14 Text · 11–12 Beschriftungen und
  Kartentitel. Keine neuen Zwischengrößen ohne Grund.
- Zahlen in Anzeigetexten nur über `utils.Numbers` (`nuyen`, `integer`, `decimal`, `essence`), nie per Verkettung
  wie `+ " ¥"`; das Format ist eine Anzeige-Einstellung (Standard ohne Tausenderpunkt, Dezimalkomma, ADR-061).
- **Zeichen und Schreibweisen (Nutzervorgaben 2026-11-19):**
  - Minus immer „−“ (U+2212): negative Zahlen über `Numbers.integer`, Vorzeichen über `Numbers.signed` („+2“/„−2“),
    auch in Katalogtexten (PB, Entzug). Der Halbgeviertstrich „–“ ist nur Bis-Strich („20–100“) und Platzhalter für
    fehlende Werte (`Numbers.NO_VALUE`), nie Minus; „-“ (Bindestrich) nie als Minus in der Anzeige.
  - Seitenangaben in sichtbaren Texten als „GRW S. n“. Die Quellenspalte der Katalogdetails bleibt „SR4 S. n“.
  - Satzzeichen: Ist eine Textzeile als Satz formuliert (Hinweis, Warnung, Dialogtext, Problemtext), endet sie mit
    Punkt. Tooltips sind standardmäßig Stichpunkte ohne Punkt, ebenso kurze Zustandszeilen wie „Kein Einbau möglich“.
- **Dialoge:** Wurzel `dialog-pane` mit `spacing="14"` und Innenabstand 20/24/20/24; einen Titel im Inhalt
  (`dialog-title`) nur bei Dialogen mit mehreren Bereichen (Neu, Einstellungen).

**Bausteine (CSS-Klassen):**
- Flächen: `.card` (+ `.card-title`, klein, fett, `muted`), `.hint-card` (+ `.hint-card-title`, `.hint-text`)
  für Hinweise; Radius 8, Innenabstand 14/16.
- Texte: `.page-title`, `.section-title`, `.key-figure` (+ `.key-figure-accent`, `.key-caption`), `.warn-text`.
- Buttons: `.btn-primary` (eine Hauptaktion pro Bereich, Petrol mit Akzentrand) und `.btn-secondary`
  (alles andere); Radius 6, Innenabstand 6/14; deaktiviert mit Deckkraft 0,45. Toggle-Buttons im selben Stil,
  gewählt = Akzent-Hintergrund. Icon-Buttons über `GridSupport.useIcon()`, das Icon übernimmt die Textfarbe.
- Grids: `MigPane` mit `GridSupport` (`addRowCells`, `preventShrink`, `withRightFiller`), Spalten nach Inhalt.

**Rahmen (ADR-049):** gruppierte Seitenleiste, schmale Kopfzeile, Fußzeile `BP | Nuyen | Essenz` bzw.
`Karma | Nuyen | Essenz`, Status als Slide-Panel oder statisch; Listen mit Kategorien als Master-Detail
(Baum links, Details rechts, ADR-035).

**Dialoge:** `Alert`/`TextInputDialog` über `applyDialogStyle()` (Font-Awesome-Icon in Akzentfarbe statt
Java-Symbol), Hinweise als ControlsFX-Notification.

**Später (Nutzerentscheidung 2026-11-18):** Ein Design-Durchgang, z.B. mit dem Plugin Impeccable
(`critique`, `typeset`, `layout`, `harden` an Screenshots), kommt erst, wenn die Funktionalität nicht mehr
im Vordergrund steht.

## 9. Verifikation und Fehlersuche

- **`./gradlew build`** (Compile + Tests + Checks) ist die Verifikation. Anzahl Tests und
  Fehlschläge berichten.
- **Die App nicht starten** (`./gradlew run`). Das Display ist der echte Desktop des Nutzers, und es gibt
  keine Klick-Automatisierung. UI-Verhalten prüft der Nutzer selbst, vor dem Release über das Beta-Menü
  (nur mit `./gradlew run`) nach `docs/beta_checkliste.md`. Neue Bereiche dort mit Prüfpunkt und passendem
  Archetyp bzw. Testfall nachtragen; fehlt ein passender Zustand, einen Testfall in `GenerateSamples` ergänzen.
- Keine TestFX/UI-Test-Infrastruktur ohne Auftrag einführen. Fachlogik so bauen, dass sie ohne UI
  testbar ist.
- **GRW-Rechenbeispiele als Unit-Tests nachrechnen** (z.B. "1,5 Cyberware + 2,5 Bioware → 2,75"), dazu
  Grenzfälle.
- Tests nie löschen oder abschwächen, um grün zu werden.
- **Erst belegen, dann melden:** „Fertig“ oder „grün“ nur nach einem Build-Lauf nach der letzten
  Änderung, dessen Ausgabe gelesen wurde (Details: `java-developer`, Schritt 6).

**Fehlersuche (Bugfix ohne Plan, aus Superpowers „systematic-debugging“ übernommen 2026-11-18):**
1. **Ursache belegen, bevor etwas geändert wird:** Fehler nachvollziehen, möglichst mit einem
   fehlschlagenden Unit-Test, sonst mit der konkreten Codestelle und dem Datenfluss dorthin. Ist die
   Ursache eine Fachregel, zuerst im GRW prüfen, ob der Code oder die Erwartung falsch ist.
2. **Eine Hypothese, eine Änderung:** Nicht mehrere Vermutungen gleichzeitig „mitfixen“.
3. **Am Ursprung beheben**, nicht am Symptom (z.B. die falsche Summe in `Character` statt einer Korrektur
   im Tab). Dieselbe Ursache an anderen Stellen suchen.
4. **Regressionstest** behält den Fall dauerhaft; dann `./gradlew build`.
5. **Nach zwei gescheiterten Ansätzen anhalten** und dem Nutzer Befund, Versuche und Vermutung
   berichten, statt weiter zu probieren.

## 10. Nacharbeiten nach einer Umsetzung

- `docs/feature_checklist.md`: Status und Anmerkungen des Bereichs aktualisieren, Zurückgestelltes
  festhalten, Roadmap nachziehen.
- `DECISIONS.md`: die dauerhaften Entscheidungen des Features mit Begründung als neue Einträge
  ergänzen (Regeln am Dateianfang); den fertigen Plan nach `docs/plaene/archiv/` verschieben, ggf.
  zugehörige Analysen nach `docs/analysen/archiv/`.
- Im Abschlussbericht Abweichungen vom Plan, neue GRW-Funde und offene Punkte klar benennen.
- Nicht committen, außer der Nutzer bittet darum.
