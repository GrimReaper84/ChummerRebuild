# Designentscheidungen

Kurze Entscheidungsprotokolle (ADRs): was entschieden wurde und warum, wo der Code allein es nicht zeigt. Details
eines Features stehen im Code, seinem Javadoc und den Tests.

**Regeln für diese Datei**
- Ein Eintrag kommt hinzu, wenn eine Entscheidung am Code schwer erkennbar ist, eine echte Wahl war oder eine Lücke
  im Grundregelwerk (GRW) füllt. Routine-Details der UI nicht aufnehmen.
- Alte Entscheidungen nie umschreiben: als **überholt** markieren und einen neuen Eintrag anlegen.
- Regeln mit GRW-Seite und kurzem Wortlaut belegen (Projektregeln Abschnitt 4).

Die Einträge bis 2026-11-11 wurden aus Abschnitt 13 der archivierten Pläne (`docs/plaene/archiv/`) und den
Projekt-Notizen zusammengetragen. **Warum (r)** kennzeichnet eine nachträglich aus dem Zusammenhang rekonstruierte
Begründung; bitte korrigieren, wenn die ursprüngliche anders war.

## Projekt und Vorgehen

### ADR-001: Das GRW entscheidet, nicht das C#-Original (2026-08-04)
Jede Fachregel und jeder Katalogwert wird gegen das deutsche SR4-Grundregelwerk geprüft, bevor er gilt.
**Warum:** Das Original enthält Bugs, Hausregeln und viel Zusatzband-Inhalt.

### ADR-002: GRW-Kern zuerst, Zusatzbände später per Regelwerk-Schalter (2026-08-04, 2026-09-27)
Inhalte ohne GRW-Beleg werden zurückgestellt und dem Nutzer vorgelegt. Regelwerke sollen später einzeln zuschaltbar
werden; bis dahin `source`/`page` je Katalogeintrag sauber pflegen.
**Warum:** Nur Geprüftes soll als „fertig“ gelten; der Schalter braucht saubere Quellenangaben.

### ADR-003: Alles Deutsch, Bezeichner englisch (2026-08-04, bestätigt 2026-11-11)
UI, Daten, Kommentare, Javadoc, Skills und Projektdokumente sind deutsch; Klassen- und Methodennamen folgen dem
bestehenden englischen Stil.
**Warum (r):** Zielgruppe und Regelwerk sind deutsch; englische Bezeichner passen zu Java-Konventionen und Bibliotheken.

### ADR-004: Kataloge klein und verifiziert starten (2026-08-08)
Neue Kataloge beginnen mit einer am GRW geprüften Teilmenge (z.B. 2+ Einträge je Kategorie); die Vervollständigung
ist ein eigener Auftrag (Zauber, Qualities, Komplexe Formen, Cyberware).
**Warum:** Fehlerfreie Daten sind wichtiger als Vollständigkeit; das Übertragen ist der größte Aufwand.

### ADR-005: Keine Halbfunktionen (2026-09-27)
Lieber eine Kategorie ganz weglassen, als sie ohne Kernmechanik anzubieten (z.B. keine Cybergliedmaßen ohne
Attributersatz).
**Warum:** Halbe Funktionen erzeugen falsche Charakterwerte.

### ADR-006: Code nur auf ausdrücklichen Auftrag, Git nur lesend (dauerhaft)
Pläne sind keine Umsetzungsfreigabe; schreibende Git-Aktionen macht nur der Nutzer.
**Warum:** Der Nutzer will steuern, wann Code entsteht, und Zwischenstände prüfen.

### ADR-007: Abschluss-Review im frischen Kontext (2026-09-30)
Nach jeder Umsetzung prüft ein eigener Agent den Diff auf Komplexität, Überabstraktion und Duplikate und berichtet nur.
**Warum:** Wer den Code geschrieben hat, stellt die eigenen Entscheidungen kaum infrage.

### ADR-008: Verschlankter Feature-Ablauf (2026-11-11)
Ein gemeinsamer Plan mit Analysen, eine Freigabe, alle Schritte am Stück, ein Review pro Feature (aus dem
Fallout2d20-Projekt übernommen).
**Warum:** Getrennte Analysedokumente und Reviews pro Teilschritt waren die größten Tokenkosten.

### ADR-009: Keine Live-GUI-Prüfung durch den Agenten (2026-08)
Verifikation ist `./gradlew build`; die App wird nicht gestartet.
**Warum:** Das Display ist der echte Desktop des Nutzers, es gibt keine Klick-Automatisierung.

### ADR-047: Kataloge: GRW zuerst, Zusatzbände draußen (2026-11-12)
Kataloge werden einer nach dem anderen gegen das GRW vervollständigt; das GRW ist die erste Quelle, das C#-Original
die zweite (Vollständigkeitskontrolle, Struktur). Inhalte aus Zusatzbänden bleiben draußen, auch wenn das C# sie als
SR4 führt oder sie schon im Katalog standen: Die fünf Metavarianten und ihre fünf Qualities aus dem Runner-Kompendium
sind entfernt (überholt den Teil von ADR-044, der sie behielt); der Loader kennt `<variant>` weiterhin. Zauber aus
Straßenmagie kommen nicht hinein. Zauber mit Platzhalter im Namen ([Attribut], [Critter], [Lebensform], [Objekt])
stehen vorerst als je ein Eintrag. Kleidung (S. 373) ist eine eigene Rüstungskategorie: Der Spieler wählt den
Kleidungspreis aus 20–100.000 ¥, Feedback-Kleidung und Lederjacke kosten diesen Preis plus ihren Aufschlag.
**Warum:** Nutzerentscheidungen 2026-11-12 (Metavarianten raus, Kleidung wie im GRW mit Preisspanne, GRW vor C#).

### ADR-048: Implantate vollständig nach GRW (2026-11-12)
Cyber- und Bioware-Katalog mit allen GRW-Einträgen ohne Cybergliedmaßen (51 + 25, S. 386–389, 394). Cyberaugen und
Cyberohren sind Basissysteme mit Kapazität (Stufe × 4); Zubehör kostet darin Kapazität statt Essenz und hat die Grade
des Basissystems (wie im C#), einzeln im natürlichen Organ kostet es Essenz; beides zugleich ist gesperrt (S. 386/387).
Dauerhafte Wirkungen rechnen (`<effect>`, `ImplantEffectType`): Attributboni bis zum augmentierten Maximum zusammen mit
Adeptenkräften, Initiativedurchgänge, Panzerung (zählt wie Mystischer Panzer nicht zur Behinderung, die nur getragene
Panzerung rechnet, S. 190), Poolboni (Athletik, sozial, körperlich verbundene Fertigkeiten, Wissen, Reflexrekorder) und
Schadenskompensator (addiert sich mit Schmerzresistenz, Annahme); situative Wirkungen stehen nur als Hinweis.
Unverträglichkeiten (`<excludes>`, `<excludescategory>`, Gesteigerte Reflexe ↔ technische Initiativesteigerung, S. 230)
sperren in beide Richtungen, ebenso Hohe Schmerztoleranz gegen Schmerzeditor und Schadenskompensator (S. 116);
eine Kategorie sperrt nur Organmodifikationen (Basissystem, Zubehör), Schutzgläser bleiben neben Katzenaugen erlaubt.
Je Fertigkeit nur ein Reflexrekorder, auch nicht einer für die Fertigkeit und einer für ihre Gruppe; er gibt einen
Zusatzwürfel, keine Stufe. Die Hyperschilddrüse gilt nicht als technische Initiativesteigerung (Annahme). Gekauft wird
über `Implants#add`, das die Regeln prüft; der Ladepfad bleibt nachsichtig. Ohne `<multiple/>` ist ein Implantat nur
einmal kaufbar. Kosmetische
Modifikation mit Preiseingabe (200–10.000 ¥); in die Kommlink-Headware wird ein Kommlink-Modell aus dem
Ausrüstungskatalog eingebaut, sein Preis kommt ohne Grade-Aufschlag dazu. Wertlisten je Stufe in `RatingFormula`
(`"2; 3; 5"`, Semikolon, weil ein Komma als deutsches Dezimalzeichen weiter ein Fehler bleiben soll). Kaufregeln und
Wirkungssummen liegen in `Implants` (Unterobjekt von `Character`), die Implantatliste bleibt in `Equipment`.
**Warum:** Nutzerentscheidungen 2026-11-12 (Kapazität und Einzelkauf, dauerhafte Wirkungen, sperren statt warnen,
Preise wie im C#, Kommlink mit Einbau); `docs/plaene/archiv/implantate_plan.md`.

### ADR-049: UI-Rework: Seitenleiste, Status-Panel, Kartenstil (2026-11-12)
Der Charakterbogen hat statt Tabs eine gruppierte Seitenleiste (Charakter, Magie & Resonanz, Ausrüstung, Netzwerk &
Protokoll; die Ebene Straßenausrüstung entfällt), eine schmale Kopfzeile (Name, Metatyp, Ressourcen-BP, Abschließen)
und eine Fußzeile `BP | Nuyen | Essenz` bei der Erschaffung bzw. `Karma | Nuyen | Essenz` in der Karriere. Die
Status-Übersicht liegt wie im Fallout2d20-Projekt in einem Slide-Panel mit verschiebbarem Knopf; Einstellungen ›
„Status statisch anzeigen“ macht sie zum obersten Bereich unter „Charakter“ (Java-Preferences, gilt für alle
Charaktere). Magie, Adeptenkräfte und Technomancer bleiben ohne Gabe sichtbar und deaktiviert (ADR-033). Optik dunkel
mit Grau-Blau-Tönen, Türkis als Akzent, dunklem Petrol als Akzent-Hintergrund, alle Farben als CSS-Variablen; Schriften
Inter und Chakra Petch (SIL OFL) im Projekt. Das App-Icon nutzt dieselben Farben. Zahlen ohne Tausenderpunkt wie bisher.
Die Kästchen des Monitors sind im Status wie im Bereich „Zustand“ nur im Karrieremodus klickbar. Eingabe-, Rückfrage-
und Hinweisdialoge zeigen statt der Java-Symbole Font-Awesome-Icons in der Akzentfarbe (`TabServices.applyDialogStyle`).
Fußzeile und Status werden an einer Stelle aktualisiert (`CharacterViewController#updateSummary`); ob die Erschaffung
überzogen ist, entscheidet `Character#isCreationOverspent`.
Überholt den Teil von ADR-034, der den Aufbau nach den Designer.cs vorgab; Inhalte und Bedienung innerhalb der Bereiche
bleiben.
**Warum:** Nutzerentscheidungen 2026-11-12 nach klickbarem Entwurf; `docs/plaene/archiv/ui_rework_plan.md`.

### ADR-050: Rüstung mit Zubehör, Modifikationen und Schild-Regeln (2026-11-12)
Zubehör der Ganzkörperpanzerung und des Urban Explorer Overalls sowie die sechs Panzerungsmodifikationen (S. 373) stehen
in `armor_mods.xml` und werden in ein Teil eingebaut (`Armor#install`): Zubehör nur an seinem Teil, Modifikationen an
Panzerung und Lederjacke, an Schilden nur Chemische und Elektrische Isolierung sowie Feuerresistenz (S. 372), jede
einmal, keine Kapazität (das GRW kennt keine). Zubehör-Boni erhöhen den Wert ihres Teils und zählen zur Behinderung.
Sonderschutz gegen Elektrizität, Feuer und Kälte = normaler Schutz + höchste Modifikationsstufe dieser Art unter den
angelegten Teilen (Annahme zu „nur eine zählt“); Chemische Isolierung als Würfelbonus auf den Toxinwiderstand (Kontakt,
an der Ganzkörperpanzerung auch Inhalation; Tabelle „Schutz vor Chemikalien“) und gegen Säure zur Panzerung (S. 194),
Wärmedämpfung als Würfelbonus. Die Anzeigezeilen liefert das Modell (`Equipment#getSpecialProtections`). Ein angelegter Schild gibt −1 auf Fertigkeiten mit körperlichem Attribut
und körperliche Attributproben. Schutz- und Taserschild sowie Schockstreifen stehen als Waffen in `weapons.xml`
(`<fromarmor/>`, seit ADR-051 `<builtin/>`, nicht kaufbar) und erscheinen im Waffenbereich, solange ihr Teil angelegt ist. Die Rüstung ist ein
Master-Detail-Bereich wie die Waffen (`ArmorTabBuilder`). Rüstungskapazität und eingebaute Waffen (Arsenal) bleiben
draußen.
**Warum:** Nutzerentscheidungen 2026-11-12; `docs/plaene/archiv/ruestung_plan.md`.

### ADR-051: Cybergliedmaßen (2026-11-12)
Die 14 Glieder aus S. 391 sind Cyberware mit Kapazität, Körperstelle (`BodyLocation`) und Zustand angeschlossen/
abgenommen wie Rüstung, damit Ersatzglieder möglich sind; eine harte Stellenbegrenzung gibt es nicht, weil Surge später
zusätzliche Glieder erlaubt, nur eine Warnung bei mehrfach belegten Stellen. Ein neues Glied an einer belegten Stelle
kommt abgenommen dazu. Essenz zählt je Stelle nur das teuerste Glied, egal ob angeschlossen. Grundwerte 3, beim Kauf
maßgefertigt bis zum natürlichen Maximum (je Punkt Verfügbarkeit +1 und 1.500 ¥ als Endkosten ohne Grade-Aufschlag),
darüber Verbesserungen bis zum augmentierten Maximum, über Stufe 3 nur mit angeschlossenem Cybertorso (S. 390).
Werte je Glied; Waffen in einem Glied rechnen mit seiner STR. Der Durchschnitt über die fünf Körperteile (zwei Arme,
zwei Beine, Torso; das Rechenbeispiel S. 390 geht nur so auf) wird angezeigt, Pools und Monitor rechnen weiter mit
den Charakterattributen. Gliedpanzerung zählt als Summe aller angeschlossenen Glieder wie im C# (das GRW regelt
mehrere gepanzerte Glieder nicht; später per Einstellung umschaltbar auf den Durchschnitt über die fünf Teile) und
nicht zur Behinderung. Jedes angeschlossene Glied gibt ein Kästchen im Körperlichen Monitor (S. 390, Archetyp S. 135).
Verbesserungen, Zubehör, Cyber-Nahkampfwaffen und Head-/Bodyware mit eckigem Kapazitätswert (S. 385, 388) werden
gegen Kapazität eingebaut; Head- und Bodyware in Gliedern zählt bei Doppelkauf und Unverträglichkeiten mit; die Sprunghydraulik geht mit einem Kauf (Stufe × 2.000 ¥) in beide Cyberbeine, ohne
zweites Bein ist sie gesperrt. Cyber-Nahkampfwaffen gibt es auch in der natürlichen Hand gegen Essenz; Klingen über
Klingenwaffen (S. 148), die Schockhand über Waffenlosen Kampf (S. 149). Cyberguns bleiben draußen, weil das GRW keine
Essenz, Kapazität und Preise nennt. Eingebaute Waffen aus Rüstung und Implantaten tragen `<builtin/>`.
**Warum:** Nutzerentscheidungen 2026-11-12; `docs/plaene/archiv/cybergliedmassen_plan.md`.

### ADR-052: Initiation und Wandlung (2026-11-12)
Ein Bereich in der Seitenleiste heißt bei Magie „Initiation“, bei Resonanz „Wandlung“ (`model.character.initiation`,
Teilobjekt `Initiation` von `Character`). Nur im Karrieremodus (S. 112); jeder Grad kostet 10 + neuer Grad × 3 Karma
und darf den wirksamen Magie- bzw. Resonanzwert (nach Essenzverlust) nicht übersteigen; das natürliche Maximum steigt
auf 6 + Grad (über `SpecialAttribute#setMaxBonus`). Je Grad eine Technik, nachholbar (Anzahl ≤ Grad), kein
Rückgängigmachen. Sechs Metamagien (S. 232/233; reine Adepten nur vier, Adeptenzentrierung nur mit Adeptenkräften) und
fünf Echos (S. 284; drei davon bis zu dreimal) in `metamagic.xml`; ihre Wirkung steht als Hinweis mit Grad bzw.
Anzahl, gerechnet wird nichts (keine Lebende Persona, keine Entzugspools). Sinkt der wirksame Wert unter den Grad,
gibt es nur einen Hinweis im Bereich und im Status, Grade und Techniken bleiben. Gruppeninitiation und Prüfungen
(Straßenmagie, nur im C#) bleiben draußen.
**Warum:** Nutzerentscheidungen 2026-11-12; `docs/plaene/archiv/initiation_wandlung_plan.md`.

### ADR-053: Geister und Sprites (2026-11-12)
Geister stehen als Abschnitt im Bereich Magie (nur mit Zauberei, also Zauberer und Magieradepten), Sprites im Bereich
Technomancer (`model.character.spirit`, Teilobjekt `Spirits`). Ein Eintrag hat Art, Kraft bzw. Stufe, Dienste und ist
gebunden/registriert oder nur herbeigerufen/kompiliert; ungebundene sind Merkzettel ohne BP und zählen nicht gegen
Charisma. Erschaffung (S. 112/113): 1 BP je Dienst, Kraft bzw. Stufe = wirksame Magie bzw. Resonanz, 1 bis Herbeirufen-
bzw. Kompilieren-Stufe Dienste, gebundene bzw. registrierte ≤ Charisma. Im Spiel wird nur eingetragen (kein Material,
kein Karma), Dienste lassen sich verbrauchen und erhöhen. Geisterarten nur die fünf der eigenen Tradition (S. 213).
`spirits.xml` enthält sechs Geister (S. 347/348) und fünf Sprites (S. 282) mit Werteblock als Formeln, Fertigkeiten,
Kräften und optionalen Kräften bzw. Komplexen Formen (je 3 volle Punkte eine); die Oberfläche zeigt sie, gerechnet
wird damit sonst nichts. Watcher, Fessellose und Freie Geister bleiben draußen. Passen eingetragene Einträge nach
späteren Änderungen (Charisma, Magie/Resonanz, Fertigkeit) nicht mehr, gibt es nur einen Hinweis im Abschnitt und im
Status (wie bei der Initiation), nichts wird automatisch geändert.
**Warum:** Nutzerentscheidungen 2026-11-12; `docs/plaene/archiv/geister_sprites_plan.md`.

### ADR-054: Matrix und Kommlinks (2026-11-13)
Ein Kommlink nimmt Betriebssystem, Hardware-Module (Prozessor, Signal), Softwarepakete (Firewall, System), Programme und
ein Sim-Modul auf (`GearData.accepts` ist eine Menge). Ein Modul ersetzt den Ausgangswert durch seine Stufe, Hardware
höchstens +2, je Attribut eins (S. 260/261); das System bleibt höchstens beim aufgerüsteten Prozessor. Programme gibt es
einzeln nach GRW (S. 271–273), sie laufen höchstens mit dem System; laufende Programme senken den Prozessor je volles
Prozessorlimit um 1 (S. 261, Schalter „läuft“). Die Rechnung steht nur in `MatrixStats`/`MatrixUpgrades` und gilt auch
für Fahrzeuge (Gerätestufe als Ausgangswert). Ein aktives Kommlink je Charakter (ohne Wahl das einzige; gewählt im
Status-Panel, das alle Kommlinks samt Headware kennt; gespeichert wird nur die ausdrückliche Wahl) liefert die
Matrixinitiative: AR körperlich, VR nur mit Sim-Modul und Direktem Neuralinterface, kalt Prozessor + INT / 2, heiß + 1 /
3 (S. 258, 265, 276); Technomancer über die Lebende Persona (S. 279). Mit dem Betriebssystem gehen die Softwarepakete.
Agenten zählen als gewöhnliche Programme (kein eigenes Merkmal). Smartlink-Bonus und Programmpakete bleiben draußen.
**Warum:** Nutzerentscheidungen 2026-11-13; `docs/plaene/archiv/matrix_kommlinks_plan.md`.

### ADR-055: Foki, Schutzpatrone, situative Würfelpools (2026-11-13)
Foki werden im Detail des Gegenstands gebunden (Erschaffung 1 BP je Kraft, Karriere Kraft × Faktor der Tabelle S. 234
Karma; Lösen gibt bei der Erschaffung die BP frei, erstattet im Karrieremodus kein Karma) und aktiviert; höchstens Magie gebundene Foki mit zusammen Kraft 5 × Magie, höchstens
Logik aktive (S. 233/234), Überschreitungen nach späteren Änderungen nur als Hinweis. Zauberkategorie, Geisterart und
Nahkampffertigkeit (statt einer bestimmten Waffe) sind Auswahl statt Freitext. Die Gabe Schutzpatron wählt aus 19
GRW-Patronen; „nach Wahl“-Vorteile stehen als „Katze (Infiltration)“ in der Auswahl. Situative Boni (aktive Foki,
Patron, sieben Adeptenkräfte) bilden eine eigene Liste „Situative Würfelpools“ (`model.character.situation`); die festen
Pools bleiben unverändert. Je Probe zählt nur der stärkste Fokus; Patron-Zauberboni gelten für Spruch- und
Ritualzauberei, Geisterboni für Herbeirufen, Binden und Verbannen; der Kraftfokus für alle Magiefertigkeiten außer
Antimagie. Grundpools ohne Fertigkeit nach GRW (Verteidigung, Schadens-, Toxin-, Zauberwiderstand, Heilung). Der
Anlass einer situativen Adeptenkraft steht an ihrer Wirkung (`AdeptPowerEffect.getSituation()`). Jeder Charakter hat im
Waffen-Tab die feste, nicht kaufbare Zeile „Waffenloser Angriff“ ((STR÷2)G, S. 188), an der Kritischer Schlag sichtbar wird.
**Warum:** Nutzerentscheidungen 2026-11-13 und 2026-11-15 (Review-Befund 5, Option a); `docs/plaene/archiv/magie_abrunden_plan.md`.

### ADR-056: Kampfwerte: Waffenpool, Munition, Rigger-/Drohnenpools, Fahrzeugschaden (2026-11-15)
Jede Waffe zeigt den Pool ihrer Fertigkeit; Fernkampf mit Smartgunsystem + Smartlink +2, sonst Lasermarkierer +1, nicht
kumulativ (S. 180). Ein Smartlink zählt als Cyberware immer, im Gerät nur mit Bildverbindung im selben Gerät (S. 380).
Je Waffe ist eine Munitionssorte aus der Ausrüstung geladen (gleiche Waffenklasse; Leuchtspur nur Vollautomatik);
Schaden, PB und Panzerung folgen der Tabelle S. 369 (`AmmoEffect`), Munition für feste Waffen ist nicht ladbar.
Fahrzeugpools nach der Tabelle S. 287 in drei Spalten (`VehiclePools`): hineingesprungen nur mit Riggeradapter, mit
Riggerkontrolle +2 auch auf Geschütze; autonom nur Drohnen, Angriff nur mit Zielerfassung, sonst Pilot allein ohne
passende Autosoft; Fernsteuerung mit dem laufenden Befehl des aktiven Kommlinks. Der Verletzungsmodifikator des Riggers
gilt hineingesprungen und ferngesteuert; Implantat- und Adeptenwürfel und der Schild-Malus des Riggers zählen dort
nicht, die Sperren der Fertigkeit (Inkompetenz, ahnungslos) schon. Die Fahrzeugfertigkeit steht je Fahrzeug im Katalog. Fahrzeugschaden nur im
Karrieremodus, ohne Verletzungsmodifikator.
**Warum:** Nutzerentscheidungen 2026-11-15; `docs/plaene/archiv/kampfwerte_plan.md`.

## Architektur und Code

### ADR-010: Ein Top-Level-Typ pro Datei (2026-08-06)
Keine verschachtelten Klassen, Records, Enums oder Interfaces, auch nicht `private`.
**Warum:** Dauerhafte Nutzervorgabe für Übersicht.

### ADR-011: XML-Kataloge mit statischem Loader-Cache (2026-08)
Je Katalog eine XML-Datei und ein `XDataLoader` mit einmaligem Laden; Instanzklassen referenzieren ihr `XData`.
**Warum (r):** Nähe zu den C#-Daten-XMLs erleichtert den Abgleich; Katalogdaten sind unveränderlich.

### ADR-012: Kaufbereiche in `Equipment`, Zugriff über `character.getEquipment()` (2026-10-04)
Alle Kaufkategorien (inkl. Cyber-/Bioware) liegen in `model.character.equipment.Equipment` mit Rückreferenz auf
`Character`; keine dauerhaften Delegatoren. Essenz-Kombinationsregel und Immunabstoßung bleiben in `Character`.
**Warum:** `Character` war zu groß (Sonar S6539); Equipment braucht Modus, Attribute und Fertigkeiten.

### ADR-013: Abwärtskompatibilität der Speicherdateien ist (noch) nicht nötig (2026-08)
Unbekannte Namen beim Laden überspringen statt abzustürzen; Formatänderungen im Ergebnis nennen.
**Warum:** Das Tool ist noch nicht im Einsatz.

### ADR-014: `Skill.linkedAttribute` als Text statt Summentyp (2026-08-08)
**Warum:** Das Feld dient nur der Anzeige; ein Summentyp aus `AttributeType`/`SpecialAttributeType` wäre unnötig.

### ADR-015: Gemeinsame Basis für Auswahldialoge (2026-08-08)
`CatalogSelectDialogController<T>` als Basis aller Katalog-Auswahldialoge.
**Warum:** Jeder neue Katalog brauchte sonst einen fast gleichen Dialog.

### ADR-038: Java 25 LTS, Versionen wie im Fallout2d20-Projekt (2026-11-11)
Toolchain 25, JavaFX 25.0.4, Error Prone 2.50.0, NullAway 0.14.2, Jackson 2.22.4, ControlsFX 11.2.6, MigLayout
11.5.1; gewollte Identitätsvergleiche per `@SuppressWarnings("ReferenceEquality")` an der Methode statt globaler
Abschaltung.
**Warum:** IDE stand schon auf Java 25; Error Prone 2.26 lief unter JDK 25 nicht, die Fallout-Kombination ist
erprobt. Die Prüfung bleibt für künftige echte Verwechslungen von `==` und `equals` aktiv.

### ADR-039: Auslieferung wie Fallout2d20, aber ohne Modularisierung (2026-11-11)
Releases über GitHub (Installer und portables ZIP mit eingebautem Java je Betriebssystem); Chummer bekommt dafür
**kein** `module-info`, sondern nutzt das Plugin `org.beryx.runtime` für nicht-modulare Anwendungen. `distZip` mit den
nativen JavaFX-JARs aller Plattformen entfällt dann.
**Warum:** Nutzer müssen kein Java installieren; eine nachträgliche Modularisierung lohnt den Aufwand nicht.

### ADR-040: Moderne Java-Idiome als Regel (2026-11-11)
Neuer und bestehender Code nutzt die Formen bis Java 25 (Projektregeln Abschnitt 6); umgestellt wurden u.a. 101 ×
`get(0)` → `getFirst()`, ungenutzte Lambda-/Catch-Parameter → `_` und die DOM-Knotenprüfung der XML-Loader
auf `instanceof Element el`. Der Reviewer-Skill prüft auf das Sprachlevel Java 25 und wird bei einem
Versionswechsel mit angepasst.
**Warum:** Nutzervorgabe aus dem Fallout2d20-Projekt; kürzer und zeigt die Absicht (z.B. „Parameter bewusst
ungenutzt“).

### ADR-041: Ein Builder je Tab, zentrale Neuaufbau-Entscheidung (2026-11-11)
Der `CharacterViewController` (vorher 3067 Zeilen) hält nur noch FXML-Knoten, Kopfbereich und Tab-Sichtbarkeit; jeder
Tab ist ein Builder in `view.tab`. Tabs melden fachliche Änderungen über `CharacterTabHost`, der Controller entscheidet
an einer Stelle, was neu gebaut wird. Dienste (`TabServices`) und +/−-Verdrahtung (`IncrementWiring`) sind eigene
Klassen. Das FXML bleibt unverändert (kein `fx:include`).
**Warum:** Der Controller war unübersichtlich; Querabhängigkeiten zwischen Tabs stehen jetzt an einer Stelle statt
verstreut in den Tabs. Nutzerentscheidungen per Rückfrage (Package `view.tab`, fachliche Rückmeldungen, ein Host).

### ADR-042: Kein Regelwerkstext 1:1 in Katalogen und Anzeige (2026-11-11)
Kataloge enthalten nur Werte, Namen, Seitenangaben und eigene Kurzbeschreibungen. Seit 2026-11-11 gilt das für alle
versionierten Dateien, auch Javadoc, Kommentare, Tests und UI-Texte: Regelbelege dort als Seite plus eigene
Zusammenfassung statt Wortlaut. Wörtliche Kurzzitate nur noch in den lokalen Plänen und Analysen (`docs/`, gitignored).
Bestand geprüft und umformuliert am 2026-11-11 (Kataloge 22 Stellen, Java-Code rund 100 Zitate).
**Warum:** Das Projekt wird öffentlich (GPL-3.0), der GRW-Text ist urheberrechtlich geschützt.

### ADR-043: Release-Pakete mit JavaFX aus der JDK, Name und Icon (2026-11-11)
Auslieferung als „Chummer SR4 Reborn“ (Paketname `chummer-sr4-reborn`, Starter `ChummerSR4Reborn`) unter GPL-3.0,
Start mit Version 1.0.0 (statt wie zuerst geplant 0.x: der macOS-Installer lehnt eine führende 0 ab;
Nutzerentscheidung 2026-11-11), einzige Versionsquelle `gradle.properties`. Das Icon ist der
Font-Awesome-Drache in Neon-Cyan `#00E5FF` auf `#0D0F12`, gezeichnet von `GenerateIcons` (Test-Quellen, Task
`generateIcons`) und eingecheckt. Das Laufzeit-Image (`org.beryx.runtime`) linkt JavaFX als **Module aus der JDK**:
lokal Liberica Full, im CI `setup-java` mit `liberica`/`jdk+fx`. Die feste Modulliste lässt jlink abbrechen, wenn die
JDK kein JavaFX hat; die JavaFX-JARs aus Maven fallen aus dem Image heraus.
**Warum:** `App` erbt direkt von `Application`; ohne JPMS startet JavaFX vom Classpath so nicht (ADR-039). JavaFX als
Module im Laufzeit-Image ist die unterstützte Konfiguration, braucht keine Startklasse als Umweg und entspricht der JDK,
mit der ohnehin entwickelt wird. Name, Version, Icon und Lizenz sind Nutzerentscheidungen per Rückfrage
(`docs/plaene/archiv/release_plan.md`).

### ADR-044: Gaben und Handicaps vollständig nach GRW (2026-11-12) — teilweise überholt durch ADR-047
Der Katalog folgt der Tabelle S. 106 und den Beschreibungen S. 114–122: GRW-Namen (Zauberer, Magieradept,
Technomancer, Glück, Unglück, Körperliche Schwäche, SIN-Mensch …), Stufen und Varianten als eigene Einträge mit
`<group>` (nur einer je Gruppe), Auswahl per `<select>`, Regeln per `<requires>`/`<excludes>`/`<metatypes>`. Nicht-GRW-
Gaben sind entfernt; Qualities der Metavarianten bleiben als nicht wählbare Metatyp-Qualities. Alte Namen in
Speicherdateien werden beim Laden übersetzt. Das 35-GP-Limit gilt ohne Ausnahmen (anders als C#). Der Magieradept
schaltet Zaubern frei (S. 116). Situative Würfelpool-, Zustandsmonitor- und Matrix-Wirkungen stehen nur als
Kurzwirkung im Dialog und Tooltip.
**Warum:** GRW als Wahrheitsquelle (ADR-002); Nutzerentscheidungen 2026-11-11 (übersetzen, entfernen, Metavarianten
unverändert). Die Prüfung mit Begründung an einer Stelle (`getQualityBlockReason`) dient Dialog und Modell zugleich.

### ADR-045: Würfelpools, Zustandsmonitor, Initiative (2026-11-12) — teilweise überholt durch ADR-046
Pools aus Fertigkeitswert und wirksamem Attribut (S. 146), ohne Wert Attribut − 1 nur bei den nicht kursiven
Fertigkeiten der Tabelle S. 146 und nicht bei Ahnungslosigkeit durch Handicaps (S. 120/122). Der Verletzungsmodifikator
(S. 193) ist in allen angezeigten Pools und Initiativen schon eingerechnet. Schaden wird nur im Karrieremodus
eingetragen und gespeichert; geistiger Überlauf (S. 192) wird nicht automatisch in die körperliche Spalte gebucht.
Spaltengröße aus den natürlichen Werten von KON und WIL (überholt: seit ADR-046 verstärkte Werte). Astrale Initiative nur mit Astralzugang (Zauberer; S. 116
schließt Magieradepten ohne Adeptenkraft aus). Wissensfertigkeiten ohne Wert erlauben Attribut − 1, außer Sprachen
(S. 146 kursiv) und Akademisch/Beruf mit Ungebildet (S. 121). Stufen von
Qualities stehen als `<level>` im Katalog. Matrix-Initiative ist zurückgestellt.
**Warum:** Nutzerentscheidungen 2026-11-12 (Schaden nur Karriere, eigener Tab, Matrix zurückgestellt); Annahmen dort,
wo das GRW schweigt, damit der Bogen ohne Improvement-System schon richtige Grundwerte zeigt.

### ADR-046: Adeptenkräfte (2026-11-12)
Katalog `adept_powers.xml` mit den 19 Kräften S. 229–232; sieben rechnen (Gesteigerte Reflexe, Gesteigertes
körperliches Attribut, Verbesserte Fertigkeit, Verbesserte Wahrnehmung inkl. Askennen, Schmerzresistenz, Mystischer
Panzer, Astrale Wahrnehmung), die übrigen tragen ihre Wirkung als Hinweis. Kraftpunkte = wirksame Magie (nach
Essenzverlust); der Magieradept legt im Tab Adeptenkräfte fest, wie viel Magie in Kräfte geht (S. 229), der Rest gilt
für alle MAG-Fertigkeitspools, die Höchststufe richtet sich nach der vollen Magie. Essenzverlust nimmt zuerst der
Zauberei Punkte (Annahme). Kauf und Steigerung nur mit genug Kraftpunkten (harte Sperre); sinkt die Magie später,
bleiben die Kräfte und der Tab warnt. Gesteigertes körperliches Attribut: Stufen über dem natürlichen Maximum kosten
doppelt, Grenze ist das augmentierte Maximum, Karma-Steigerung nach Gesamtwert (S. 230). Zustandsmonitor und
Panzerungsbehinderung nutzen ab jetzt verstärkte KON/WIL (überholt ADR-045); Mystischer Panzer zählt nicht zur
Behinderung (Annahme). Askennen und Astralkampf nur mit astraler Wahrnehmung (S. 108, 149; auch für den
Magieradepten). Verbesserte Fertigkeit nur für Fertigkeiten, für die S. 232 Kosten nennt (keine Magie/Resonanz).
Schmerzresistenz und Hohe Schmerztoleranz schließen sich gegenseitig aus (S. 116). Gesteigerte Reflexe und
Gesteigerte Reaktion teilen sich den Raum bis zum augmentierten Maximum der Reaktion. Stufen über der Höchststufe (z.B.
nach gesenktem Fertigkeitswert) bleiben mit Warnung stehen. Die Magie-Aufteilung des Magieradepten lässt sich bei der
Erschaffung nicht unter die ausgegebenen Kraftpunkte senken und im Karrieremodus nur noch steigern (S. 116, 229:
Entscheidung je Magiepunkt, kein Umverteilen beschrieben). Kauf, Stufen, Kosten und Kraftpunkte liegen im Teilobjekt
`AdeptPowers` (`character.getAdeptPowers()`, wie `Equipment`); `Character` behält die Wirkungen und das Entfernen
einer Kraft, weil es Fertigkeiten sperren kann.
**Warum:** Nutzerentscheidungen 2026-11-12 (feste Wirkungen rechnen, Aufteilung im Kräfte-Tab, kein Umverteilen im
Karrieremodus); verstärkte Werte, weil
SR4 sie allgemein für abgeleitete Werte nutzt; GRW-Wortlaut „aufwenden“ für die harte Sperre.

## Fachregeln: Entscheidungen, wo das GRW schweigt oder unklar ist

### ADR-016: Weiche Warnungen statt harter Sperren (2026-08, 2026-09-27)
Regelverstöße als Notification; hart blockiert wird nur, wo das GRW eindeutig „muss“ sagt (Restgeld < 0 blockiert den
Abschluss, GRW S. 111). Essenz ≤ 0 bei Erschaffung bleibt bewusst eine weiche Warnung, obwohl GRW S. 111 sie ausschließt.
**Warum:** Nutzerentscheidung; Korrekturen sollen möglich bleiben.

### ADR-017: Magier-Erkennung über `QualityBonusType.GRANTS_SPELLCASTING` (2026-08-08)
Nicht über den Namen der Gabe.
**Warum:** Typsicher und erweiterbar (z.B. Mystischer Adept).

### ADR-018: Tradition als Enum mit Standardwert, kein Pflicht-Gate (2026-08-08)
Hermetisch/Schamanistisch, Default Hermetisch; Zauber sind ohne vorherige Wahl lernbar, jeder Zauber nur einmal.
**Warum:** Das GRW-Kern kennt genau diese zwei Traditionen; ein Zustand „keine Tradition“ bringt nur Sonderfälle.

### ADR-019: Adepten und Wandlung zurückgestellt (2026-08-08)
**Warum:** Eigene Folgefeatures ohne vorhandenes Initiations-/Kräfte-Fundament.

### ADR-020: Technomancer ohne Stream-Auswahl (2026-08-08)
Feste Schwund-Formel „Willenskraft + Resonanz“, keine Stream-Validierung.
**Warum:** Das GRW-Kern kennt den Begriff „Stream“ nicht.

### ADR-021: Komplexe Formen: Cap immer, kein Nachkappen, Start bei Stufe 1 (2026-08-08)
Das Resonanz-Cap gilt in Erschaffung und Karriere; sinkt RES nachträglich, bleiben höhere Stufen bestehen.
**Warum:** GRW formuliert das Cap als durchgehende Regel; Nachkappen würde Karma vernichten (r).

### ADR-022: Cyberware-Erstschnitt: 4 Grades, Essenz auf 2 Nachkommastellen (2026-09-27) — teilweise überholt durch ADR-048
Ohne Cybergliedmaßen, Essenzloch und Kapazität; Essenz mit `BigDecimal`, an einer Stelle gerundet. Immunabstoßung nach
GRW-Funktion (nur Cyberware verdoppelt).
**Warum:** Kleiner, geprüfter Schnitt; `double`-Summen erzeugen Rundungsartefakte.

### ADR-023: Nuyen, Rüstung und Lebensstil (2026-09-27)
Lebensstil nur die 6 GRW-Stufen; Startkapital würfelt die App, Wurf editierbar; Verfügbarkeit ≤ 12 bei Erschaffung,
darüber sichtbar-deaktiviert; Helme/Schilde zählen zur Behinderung (GRW S. 372); durch Behinderung reduzierte GES/REA
fällt vorläufig nicht unter 1 (GRW nennt keine Untergrenze).
**Warum:** Nutzerentscheidungen beim Plan; die Untergrenze wird entfernt, falls weitere Regelwerke sie nicht bestätigen.

### ADR-024: Waffen in drei Ausbaustufen, Bogen als Stufenmodell (2026-09-28)
A Grundkatalog, B berechnete Werte, C Zubehör; Bogen als ein Eintrag mit Mindeststärke 1–8; Unterlauf- und
Flechette-Varianten als Hinweistext; HK-XM30-Umbausätze zurückgestellt.
**Warum:** Kleine Schritte; Bogenvarianten unterscheiden sich nur in der Stufe.

### ADR-025: Kein Würfelpool vor dem Würfelpool-Feature (2026-09-28) — teilweise überholt durch ADR-045
Waffen und Fahrzeuge zeigen Fertigkeit und Wert, keinen Pool. (Fertigkeiten haben seit ADR-045 einen Pool; Waffen-
und Fahrzeugtabellen weiterhin nicht.)
**Warum:** Würfelpools kommen geschlossen mit Roadmap Stufe 3.

### ADR-026: Waffenzubehör nach den Zubehörtexten (2026-09-30, 2026-10-04)
Eingebautes Zubehör als `<included>`-Liste (kostenlos, belegt keine Halterung, RK schon im Tabellenwert); Zweibein/
Dreibein-RK in den Klammerwert (GRW S. 357); Taser nur aufmontiert; Einschränkungen laut S. 367/368 (Airburst,
Schnelllader, Ersatzladestreifen, Gasventil); Gyrostabilisator ohne Schrotflinten. Holster, Armhalterung und
Smart-Feuerplattform gehören zur Ausrüstung.
**Warum:** Wortlaut der Zubehörtexte; das C# übernimmt die Einschränkungen nicht vollständig.

### ADR-027: Ausrüstung: Stufengrenze 6, Preisspannen, Zusammenführen (2026-10-04)
Stufengrenze 6 bei Erschaffung allgemein (GRW S. 111); bei Preisspannen gibt der Spieler den Preis ein (Untergrenze =
GRW-Mindestpreis); gleiche Gegenstände werden zusammengeführt; Menge nachträglich änderbar (Verbrauch ohne
Erstattung), Stufe nur beim Kauf; Lagerorte zurückgestellt.
**Warum:** Nutzerentscheidungen beim Gear-Plan.

### ADR-028: Einbauten datengetrieben, Betriebssystem als Kind des Kommlinks (2026-10-04, 2026-10-08)
Kapazität und erlaubte Einbauten stehen in `gear.xml`; Kamera/Mikrofon sind Gerät und Sensor-Funktion zugleich;
Sensorpakete ohne GRW-Preis zu 0 ¥ mit Hinweis.
**Warum:** Das GRW listet beide Verwendungen; Sensorpakete haben in Tabelle S. 381 keinen Preis.

### ADR-029: Munition, Pfeile und Bolzen (2026-10-04, 2026-10-08)
Pfeile im Waffen-Tab am Bogen (Stufe 1–8, Schaden nach min(Pfeil, Bogen), fester Preis); Bolzen als normale Ausrüstung.
**Warum:** Pfeile hängen am Bogen; die Bolzentabelle S. 361 hat keine Stufe.

### ADR-030: Fahrzeuge (2026-10-09)
Eigener Haupttab; Waffenhalterung für Feuerwaffen bis LMG plus Granatwerfer; mitgelieferte Halterungen zählen gegen
Rumpf ÷ 3; alle Ausrüstung darf ins Fahrzeug; Fahrzeugwaffen zeigen nur Tabellenwerte und bekommen kein Zubehör;
Autosoft-Typ als Auswahl der fünf GRW-Typen; Matrix-Upgrades und Eigenname zurückgestellt; „Sensorpaket (Hand)“
heißt „Sensorpaket (Hand/Minidrohne)“ (GRW S. 381).
**Warum:** Nutzerentscheidungen beim Fahrzeug-Plan; GRW S. 395 „ein LMG oder eine kleinere Waffe“.

### ADR-031: Fertigkeitszugang über die Gaben (2026-10-09)
`Character.canRaiseSkill/canRaiseGroup` regeln den Zugang; Arkana auch für Mundane; beim Entfernen von Magier/
Technomant Ja/Nein-Abfrage, gesperrte Fertigkeiten fallen auf 0.
**Warum:** GRW S. 146–154; stilles Weiterführen gesperrter Werte würde BP verfälschen (r).

### ADR-032: Wissensfertigkeiten: Vorschlagsliste zurückholen (2026-08-06)
Die Namensvorschläge (Sprachen, Lingos) sollen wiederkommen, aus C#-Liste und GRW S. 156, ohne Zusatzband-Begriffe.
Noch offen.
**Warum:** Nicht alle Spieler haben SR-spezifische Begriffe im Kopf.

## Benutzeroberfläche

### ADR-033: Deaktivieren statt Ausblenden (2026-08-08)
Nicht nutzbare Listeneinträge bleiben sichtbar-deaktiviert; nur ganze Tabs dürfen bis zu ihrer Voraussetzung fehlen.
**Warum:** Ausgeblendete Einträge führten zu einem Refresh-Bug; der Nutzer soll sehen, was es gibt.

### ADR-034: Tabs gegen die alten Designer.cs aufbauen (2026-09-25) — teilweise überholt durch ADR-049
Controls, Gruppierung und Reihenfolge aus `frmCreate`/`frmCareer.Designer.cs`, keine Pixelpositionen; bewusst
verbesserte Muster bleiben.
**Warum:** Vertraute Bedienung für Chummer-Nutzer.

### ADR-035: Master-Detail mit Kategorienbaum für Waffen, Gear, Fahrzeuge (2026-09-30)
Links Baum nach Kategorie, rechts Detailbereich; überholt die frühere Entscheidung „Baum mit Spalten“ (2026-09-28).
Rüstung folgt später.
**Warum:** Der Nutzer fand die Detailansicht deutlich übersichtlicher.

### ADR-036: Kategorie-Filter alphabetisch (2026-10-08)
In `CatalogSelectDialogController` mit deutschem Collator, „Alle“ oben; überholt die GRW-Reihenfolge.
**Warum:** Schneller beim Suchen.

### ADR-037: Fertigkeits-Sortierung als nicht gespeicherter UI-Zustand (2026-08)
ToggleButton-Paar statt ComboBox; der Modus wird nicht gespeichert.
**Warum (r):** Reine Ansichtssache, kein Charakterzustand.
