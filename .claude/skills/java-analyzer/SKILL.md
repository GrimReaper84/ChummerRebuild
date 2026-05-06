# Java-Analyse-Skill

## Kurzbeschreibung

Dieser Skill analysiert vorhandenen Java-Code im Rahmen einer schrittweisen Feature-Migration von C# nach Java. Er dient als Gegenstück zum C#-Explorer-Skill: Während der C#-Explorer das fachliche und technische Verhalten des C#-Altsystems rekonstruiert, ermittelt dieser Skill den aktuellen Stand des Java-Zielsystems und bereitet die Ergebnisse so auf, dass ein nachgelagerter Java-Planungs-Skill daraus konkrete Planungsentscheidungen ableiten kann.

Der Skill erzeugt keine Implementierung. Er liefert eine belegbare, strukturierte Analysebasis.

## Einsatzbereich

Nutze diesen Skill, wenn vorhandener Java-Code analysiert werden soll, um:

- den aktuellen Java-Stand eines Features oder Use Cases zu bestimmen,
- Java-Strukturen mit Erkenntnissen aus dem C#-Explorer abzugleichen,
- vorhandene Java-Implementierungen, Tests und Integrationspunkte zu kartieren,
- fachliche, technische und testbezogene Lücken sichtbar zu machen,
- die Grundlage für eine spätere Java-Planung zu schaffen.

Der Skill ist besonders relevant für Migrationen, bei denen nicht das komplette System in einem Schritt ersetzt wird, sondern einzelne Features nacheinander übertragen, ergänzt oder an bestehende Java-Strukturen angeschlossen werden.

## Grundannahmen

- Der Java-Code ist insgesamt konsistenter und wartbarer als der C#-Altcode.
- Bestehende Java-Strukturen sollen bevorzugt verstanden und genutzt werden.
- Abweichungen zwischen C# und Java sind nicht automatisch Fehler.
- C#-Verhalten darf nicht blind übernommen werden; es muss fachlich eingeordnet werden.
- Die Analyse muss so dokumentiert werden, dass der Java-Planungs-Skill ohne erneute Grundlagenanalyse weiterarbeiten kann.

## Abgrenzung zu anderen Skills

| Skill | Aufgabe | Ergebnis |
|---|---|---|
| C#-Explorer-Skill | Rekonstruiert Verhalten, Datenflüsse und Sonderfälle im C#-Altsystem. | C#-Explorer-Bericht mit fachlichen Erkenntnissen. |
| Java-Analyse-Skill | Prüft, was davon im Java-Zielsystem bereits existiert, anders umgesetzt wurde oder fehlt. | Java-Analysebericht mit Abgleich, Lücken und Planungsgrundlage. |
| Java-Planungs-Skill | Leitet aus C#-Explorer und Java-Analyse konkrete Umsetzungsschritte ab. | Implementierungs-, Integrations- und Testplan. |
| Umsetzungsskill | Setzt explizit freigegebene Änderungen um. | Codeänderungen, Tests, Dokumentation. |

## Eingaben

Der Skill kann mit folgenden Eingaben arbeiten:

- Feature- oder Use-Case-Beschreibung,
- C#-Explorer-Bericht,
- Pfade zu Java-Modulen, Packages oder Klassen,
- Hinweise auf relevante UI-Aktionen, APIs, Jobs oder Services,
- Tests, Fixtures oder Beispieldaten,
- Architektur- oder Projektkonventionen,
- Fehlermeldungen, Tickets oder fachliche Notizen,
- konkrete Abgleichsfragen.

Beispielhafte Fragen:

```text
Gibt es dieses C#-Verhalten bereits im Java-Projekt?
```

```text
Welche Java-Komponenten sind für dieses Feature relevant?
```

```text
Welche Lücken bleiben nach dem Abgleich mit dem C#-Explorer offen?
```

## Ausgaben

Die Ausgabe muss planungsfähig sein. Sie soll typischerweise enthalten:

- Kurzfazit zum Java-Stand,
- untersuchte Java-Bereiche,
- relevante Klassen, Interfaces, Services, Repositories, Controller, UI-Komponenten und Tests,
- fachliche Verantwortung der gefundenen Komponenten,
- rekonstruierter Java-Ablauf,
- Daten- und Objektmodell,
- Abgleich mit dem C#-Explorer-Bericht,
- Status je C#-Erkenntnis,
- Abweichungen, Lücken, Risiken und offene Fragen,
- Übergabeblock für den Java-Planungs-Skill.

## Arbeitsprinzipien

### 1. Erst verstehen, dann bewerten

Der Skill darf Java-Code nicht vorschnell anhand von Namen bewerten. Er muss zuerst nachvollziehen:

- Package- und Modulstruktur,
- Schichten und Verantwortlichkeiten,
- öffentliche Einstiegspunkte,
- Aufrufketten,
- Datenflüsse,
- Persistenzzugriffe,
- Mapping- und Transformationslogik,
- Fehlerbehandlung,
- Tests und Testdaten.

Erst danach werden Abdeckung, Abweichungen und Lücken bewertet.

### 2. Fachliche Bedeutung priorisieren

Die zentrale Frage ist nicht, ob eine Java-Klasse ähnlich heißt wie eine C#-Klasse, sondern ob sie dieselbe fachliche Verantwortung erfüllt.

Zu prüfen ist insbesondere:

- Welche fachliche Entscheidung wird getroffen?
- Welche Daten werden gelesen, verändert, persistiert oder verworfen?
- Welche Seiteneffekte entstehen?
- Welche externen Systeme werden angesprochen?
- Welche Sonderfälle werden behandelt?
- Welche Fehlerfälle werden bewusst oder unbewusst ignoriert?
- Welche Annahmen über Datenzustände sind im Code verborgen?

### 3. Bestehende Java-Konventionen respektieren

Da der Java-Code als vergleichsweise konsistent gilt, soll der Skill vorhandene Strukturen nicht unnötig infrage stellen. Empfehlungen zur Änderung bestehender Strukturen sind nur sinnvoll, wenn sie:

- eine konkrete Migrationshürde beseitigen,
- eine fachliche Inkonsistenz reduzieren,
- die spätere Integration deutlich vereinfachen,
- Testbarkeit für das konkrete Feature verbessern,
- relevante Duplikation entfernen,
- oder ein konkretes technisches Risiko entschärfen.

### 4. Keine Implementierung erzeugen

Der Skill schlägt keine Codeänderungen als fertigen Code vor, außer der Auftrag verlangt ausdrücklich danach. Standardmäßig werden Erkenntnisse, Lücken und Planungshinweise beschrieben.

Erlaubt sind:

- Verweise auf vorhandene Klassen und Methoden,
- konzeptionelle Änderungsvorschläge,
- Testfall-Vorschläge,
- Schnittstellen- und Integrationshinweise,
- Pseudostrukturen auf hohem Niveau, wenn sie für die Planung nötig sind.

Nicht erlaubt ohne expliziten Auftrag:

- vollständige Java-Klassen,
- produktionsreife Methodenimplementierungen,
- automatische Refactorings,
- eigenständige Änderungen am Projekt.

### 5. Unsicherheit sichtbar machen

Jede relevante Aussage soll, wenn nötig, mit einer Sicherheitsstufe versehen werden:

| Sicherheitsstufe | Bedeutung |
|---|---|
| Gesichert | Direkt durch Code, Tests, Konfiguration oder Dokumentation belegt. |
| Wahrscheinlich | Aus konsistenter Aufrufstruktur oder Projektmuster plausibel ableitbar. |
| Unklar | Hinweise vorhanden, aber keine ausreichende Beleglage. |
| Nicht gefunden | Gezielt gesucht, aber keine belastbare Entsprechung gefunden. |

Fehlende Informationen dürfen nicht durch Spekulation ersetzt werden.

## Vorgehensweise

### Phase 1: Analysefokus bestimmen

Der Skill bestimmt zuerst:

- welches Feature oder welcher Use Case untersucht wird,
- welche C#-Explorer-Erkenntnisse relevant sind,
- welche Java-Bereiche wahrscheinlich betroffen sind,
- welche Tests oder Tickets Hinweise liefern,
- welche Annahmen für die Analyse getroffen werden.

Wenn der Fokus unvollständig ist, arbeitet der Skill mit einer explizit genannten Arbeitshypothese weiter.

### Phase 2: Einstiegspunkte suchen

Der Skill sucht sowohl top-down als auch bottom-up.

Top-down:

- Controller,
- UI-Actions,
- REST-/API-Endpunkte,
- Services,
- Handler,
- Use-Case-Klassen,
- Scheduler,
- Event Listener,
- Jobs.

Bottom-up:

- Entities,
- Tabellen,
- Repositories oder DAOs,
- Queries,
- DTOs,
- Mapper,
- fachliche Schlüsselbegriffe,
- Tests und Fixtures.

### Phase 3: Relevante Komponenten kartieren

Für jede relevante Komponente werden dokumentiert:

- Name,
- Pfad,
- Typ,
- fachliche Verantwortung,
- wichtigste Methoden,
- zentrale Abhängigkeiten,
- gelesene oder geschriebene Daten,
- externe Schnittstellen,
- vorhandene Tests,
- bekannte Risiken oder Einschränkungen,
- Sicherheitsstufe der Einschätzung.

### Phase 4: Java-Verhalten rekonstruieren

Der Skill beschreibt den tatsächlichen Ablauf im Java-Code:

- Auslöser,
- Eingabedaten,
- Validierung,
- Kontrollfluss,
- Datenfluss,
- Mapping,
- Persistenz,
- Transaktionsgrenzen,
- Fehlerbehandlung,
- Nebenläufigkeit,
- Logging,
- Rückgabe oder sichtbarer Effekt.

Bei komplexen Abläufen soll eine nummerierte Ablaufbeschreibung verwendet werden.

### Phase 5: Daten- und Objektmodell prüfen

Zu untersuchen sind:

- Domänenobjekte,
- Entities,
- DTOs,
- Wrapper,
- Mapper,
- Tabellen,
- relevante Felder,
- Defaultwerte,
- Statuswerte,
- IDs und Referenzen,
- Konvertierungen zwischen technischen und fachlichen Darstellungen.

Besonders wichtig ist die Frage, ob das Java-Modell alle Informationen abbilden kann, die aus dem C#-Explorer als fachlich relevant hervorgegangen sind.

### Phase 6: Tests bewerten

Der Skill prüft nicht nur, ob Tests existieren, sondern welche fachlichen Aussagen sie absichern.

Zu bewerten sind:

- Happy Path,
- Fehlerfälle,
- Grenzfälle,
- Sonderfälle aus dem C#-Explorer,
- Regressionen,
- Persistenzverhalten,
- Mapping-Verhalten,
- Integration mit UI, API, Job oder externem System,
- Mocking- und Fixture-Struktur.

Testlücken sind als Planungsinput zu formulieren.

### Phase 7: Abgleich mit dem C#-Explorer

Für jede relevante C#-Erkenntnis wird geprüft:

- Existiert eine fachliche Entsprechung in Java?
- Ist sie vollständig, teilweise oder abweichend umgesetzt?
- Ist die Abweichung fachlich relevant?
- Ist die Java-Lösung bewusst moderner oder nur zufällig anders?
- Muss das C#-Verhalten übernommen, angepasst, verworfen oder fachlich geklärt werden?

Empfohlene Statuswerte:

| Status | Bedeutung |
|---|---|
| Vorhanden | Java deckt die C#-Erkenntnis fachlich ab. |
| Teilweise vorhanden | Java enthält Ansätze, aber nicht den vollständigen Umfang. |
| Abweichend vorhanden | Java enthält eine Umsetzung, die fachlich oder technisch relevant anders funktioniert. |
| Nicht vorhanden | Keine passende Java-Umsetzung gefunden. |
| Unklar | Hinweise vorhanden, aber keine belastbare Bewertung möglich. |
| Nicht relevant | C#-Verhalten muss voraussichtlich nicht übernommen werden. |

Zusätzlich ist eine Übernahmekategorie zu vergeben:

| Übernahmekategorie | Bedeutung |
|---|---|
| Muss | Fachliches Verhalten muss im Java-Zielsystem vorhanden sein. |
| Kann anders | Verhalten ist fachlich relevant, die technische Umsetzung darf abweichen. |
| Nicht übernehmen | C#-Verhalten ist Altlast, Workaround oder im Java-Zielsystem unerwünscht. |
| Klären | Fachliche oder technische Entscheidung fehlt. |

### Phase 8: Lücken und Risiken ableiten

Der Skill klassifiziert erkannte Lücken:

| Lückentyp | Bedeutung |
|---|---|
| Fachliche Lücke | Verhalten aus C# existiert in Java noch nicht. |
| Semantische Abweichung | Java tut etwas Ähnliches, aber nicht Gleichwertiges. |
| Technische Lücke | Infrastruktur, Schnittstelle oder Persistenzzugriff fehlt. |
| Testlücke | Verhalten existiert, ist aber nicht ausreichend abgesichert. |
| Integrationslücke | Komponente existiert, ist aber nicht in den relevanten Ablauf eingebunden. |
| Datenmodell-Lücke | Java-Struktur kann benötigte Informationen nicht oder nur unklar abbilden. |

Risiken sollen konkret formuliert werden, nicht abstrakt.

Schlecht:

```text
Es könnte Probleme geben.
```

Besser:

```text
Die Java-Implementierung persistiert den Statuswechsel, bildet aber den im C#-Explorer beschriebenen Sonderfall für bereits stornierte Datensätze nicht ab. Dadurch kann bei der Migration eine doppelte Verarbeitung entstehen.
```

### Phase 9: Übergabe an den Java-Planungs-Skill erstellen

Am Ende jeder Analyse steht ein kompakter Übergabeblock. Dieser Block ist der wichtigste Output für den nächsten Skill.

Er muss enthalten:

- gesicherte Ausgangslage,
- bestehende Java-Komponenten, die genutzt werden sollten,
- fehlende oder anzupassende Funktionalität,
- fachliche Entscheidungen,
- technische Entscheidungen,
- empfohlene Tests,
- Risiken,
- offene Fragen.

## Ergebnisformat

Standardformat:

```markdown
# Java-Analyse: <Feature / Use Case>

## 1. Kurzfazit

<knappe Bewertung des aktuellen Java-Stands und der wichtigsten Konsequenzen>

## 2. Analysefokus

- Feature / Use Case:
- C#-Explorer-Grundlage:
- Untersuchte Java-Bereiche:
- Arbeitshypothesen:
- Nicht untersucht:

## 3. Relevante Java-Komponenten

| Komponente | Typ | Pfad | Verantwortung | Relevante Methoden | Sicherheit |
|---|---|---|---|---|---|

## 4. Rekonstruierter Java-Ablauf

1. ...
2. ...
3. ...

## 5. Daten- und Objektmodell

| Objekt / Tabelle / DTO | Rolle im Ablauf | Relevante Felder | Abgleich zu C# | Hinweise |
|---|---|---|---|---|

## 6. Abgleich mit C#-Explorer

| C#-Erkenntnis | Java-Entsprechung | Status | Übernahmekategorie | Bewertung | Planungsimplikation |
|---|---|---|---|---|---|

## 7. Tests und Absicherung

| Test | Abgesichertes Verhalten | Fehlende Szenarien | Bewertung | Planungsimplikation |
|---|---|---|---|---|

## 8. Lücken und Risiken

| Typ | Beschreibung | Auswirkung | Sicherheit | Empfehlung |
|---|---|---|---|---|

## 9. Offene Fragen

| Frage | Warum relevant? | Benötigt für |
|---|---|---|

## 10. Übergabe an Java-Planungs-Skill

### Gesicherte Ausgangslage

- ...

### Bestehende Komponenten, die genutzt werden sollten

- ...

### Zu implementieren oder anzupassen

- ...

### Fachlich zu entscheiden

- ...

### Technisch zu entscheiden

- ...

### Empfohlene Tests

- ...

### Risiken

- ...
```

## Arbeitsmodi

### Kompaktmodus

Für kleine oder gezielte Fragen:

```markdown
## Kurzfazit

## Relevante Java-Stellen

## Abgleich mit C#-Erkenntnissen

## Lücken / Risiken

## Übergabe an Java-Planungs-Skill
```

### Standardmodus

Für normale Feature-Analysen wird das vollständige Ergebnisformat verwendet.

### Tiefenanalyse-Modus

Für kritische, komplexe oder besonders unsichere Features ergänzt der Skill:

- textuelles Ablaufdiagramm,
- detaillierte Datenflussbeschreibung,
- Testfall-Matrix,
- Liste möglicher Regressionen,
- Liste fachlicher Entscheidungsfragen,
- Liste technischer Entscheidungsfragen,
- empfohlene Reihenfolge weiterer Analyse- oder Planungsschritte.

## Spezielle Prüfpunkte für Java-Projekte

### Architektur und Schichten

- Liegt Fachlogik in Services, Domänenobjekten, Handlern oder Utilities?
- Sind Persistenz, Fachlogik und Schnittstellen sauber getrennt?
- Gibt es Umgehungen etablierter Projektmuster?
- Gibt es zentrale Abstraktionen, die für neue Migrationsteile genutzt werden sollten?
- Gibt es Utility-Klassen mit fachlich relevanter Logik?

### Dependency Injection

- Welche Beans oder Komponenten sind beteiligt?
- Sind Abhängigkeiten explizit und testbar?
- Gibt es zyklische oder schwer nachvollziehbare Abhängigkeiten?
- Werden statische Helfer genutzt, die Tests oder Erweiterungen erschweren?

### Persistenz

- Welche Entities, Repositories, DAOs oder Queries sind relevant?
- Welche Tabellen oder Datenstrukturen werden gelesen oder geschrieben?
- Gibt es native Queries?
- Gibt es implizite Annahmen über vorhandene Daten?
- Werden Transaktionen explizit oder implizit gesteuert?
- Gibt es Unterschiede zum Datenmodell des C#-Systems?

### Mapping und Transformation

- Wie werden externe, alte oder technische Datenmodelle in Java-Objekte übersetzt?
- Gibt es Mapper, Wrapper oder Adapter?
- Sind Transformationen fachlich verlustfrei?
- Gibt es relevante Defaultwerte?
- Gibt es stille Normalisierungen, Formatierungen oder Filter?

### Fehlerbehandlung

- Welche Exceptions werden gefangen?
- Welche Exceptions werden weitergereicht?
- Gibt es Retry-Logik?
- Gibt es bewusst ignorierte Fehler?
- Gibt es fachliche Fallbacks?
- Wird Fehlerverhalten getestet?

### Nebenläufigkeit und Jobs

- Läuft Logik synchron oder asynchron?
- Gibt es Scheduler, Hintergrundthreads oder Queues?
- Gibt es Locks oder andere Schutzmechanismen?
- Können Race Conditions entstehen?
- Wie werden Teilfehler behandelt?

### API- und UI-Anbindung

- Wo wird das Feature ausgelöst?
- Welche Eingaben kommen aus UI, API, Job oder externem System?
- Welche Rückgaben oder sichtbaren Effekte entstehen?
- Gibt es Berechtigungs-, Sichtbarkeits- oder Validierungslogik?

## Qualitätskriterien

Eine gute Analyse erfüllt folgende Kriterien:

- Relevante Java-Komponenten wurden nachvollziehbar identifiziert.
- Der fachliche Ablauf wurde aus Code, Tests und Aufrufstruktur rekonstruiert.
- Der Abgleich mit dem C#-Explorer enthält konkrete Statuswerte.
- Abweichungen werden fachlich bewertet, nicht nur technisch beschrieben.
- Lücken sind klassifiziert und mit Planungsimplikationen versehen.
- Tests werden nach abgesichertem Verhalten bewertet.
- Unsicherheiten sind klar markiert.
- Die Übergabe an den Java-Planungs-Skill ist direkt weiterverwendbar.

## Nicht-Ziele

Der Skill soll nicht:

- eigenständig Code ändern,
- ohne expliziten Auftrag Java-Code generieren,
- größere Refactorings planen,
- C#-Verhalten unkritisch reproduzieren,
- Architekturentscheidungen endgültig treffen,
- fehlende Informationen durch Spekulation ersetzen,
- Java-Code allein anhand von Klassen- oder Methodennamen bewerten,
- Shell-Befehle oder sonstige Werkzeuge ohne passende Erlaubnis ausführen.

## Beispiel-Prompt

```text
Analysiere den vorhandenen Java-Code für das Feature <Feature-Name>.

Nutze den folgenden C#-Explorer-Bericht als Vergleichsgrundlage:

<C#-Explorer-Bericht einfügen>

Untersuche insbesondere:
- welche Java-Komponenten fachlich relevant sind,
- welche C#-Erkenntnisse bereits abgedeckt sind,
- welche Abweichungen bestehen,
- welche Tests vorhanden sind,
- welche Lücken für die Feature-für-Feature-Migration relevant sind.

Erzeuge keinen Code. Bereite das Ergebnis so auf, dass ein Java-Planungs-Skill daraus konkrete Implementierungs-, Integrations- und Testaufgaben ableiten kann.
```

## Minimaler Übergabeblock

Wenn die Analyse sehr klein ist, muss trotzdem mindestens dieser Block erzeugt werden:

```markdown
## Übergabe an Java-Planungs-Skill

### Gesichert

- ...

### Teilweise / abweichend

- ...

### Fehlend

- ...

### Zu klären

- ...

### Empfohlene nächste Planungsschritte

- ...
```
