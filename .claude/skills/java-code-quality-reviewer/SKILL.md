---
name: java-codequality-review
description: Analysiert bestehenden Java-Code auf Codequalität, Wartbarkeit, Verständlichkeit, Testbarkeit und technische Risiken. Nutze diesen Skill, wenn Java-Code bewertet werden soll, ohne automatisch Änderungen vorzunehmen, oder wenn Review-Erkenntnisse als präzise Umsetzungsaufträge für einen nachgelagerten Coding-Skill vorbereitet werden sollen.
---

# Skill: Java-Codequality-Review

## Zweck

Dieser Skill analysiert bestehenden Java-Code gezielt im Hinblick auf Codequalität, Wartbarkeit, Verständlichkeit, Testbarkeit, Robustheit und technische Risiken.

Der Skill ergänzt den Java-Analyse-Skill und den Java-Planungs-Skill:

- Der **Java-Analyse-Skill** rekonstruiert primär Funktionalität und aktuellen Projektstand.
- Der **Java-Codequality-Review-Skill** bewertet bestehenden Java-Code und leitet konkrete Verbesserungsmöglichkeiten ab.
- Der **Java-Planungs-Skill** plant neue oder migrierte Java-Features.
- Der **Coding-Skill** setzt Änderungen um, wenn dies ausdrücklich gewünscht ist.

Der Codequality-Review-Skill nimmt selbst keine Codeänderungen vor. Er erstellt entweder eine menschenlesbare Review-Ausgabe oder strukturierte Umsetzungsaufträge für einen nachgelagerten Coding-Skill.

## Wann dieser Skill verwendet wird

Verwende diesen Skill, wenn der:die Nutzer:in eine der folgenden Aufgaben formuliert:

- bestehenden Java-Code auf Qualität prüfen
- Refactoring-Möglichkeiten erkennen
- technische Schulden sichtbar machen
- Lesbarkeit, Wartbarkeit oder Testbarkeit bewerten
- Komplexität, Architektur oder Verantwortlichkeiten analysieren
- Verbesserungsvorschläge erhalten, ohne dass Code automatisch geändert wird
- Review-Erkenntnisse für eine spätere Umsetzung durch einen Coding-Skill vorbereiten
- Java-Code nach einer Migration aus C# qualitativ bewerten
- JavaFX-Code auf Struktur, Threading, Bindings oder Plattformrobustheit prüfen

Typische Nutzer:innenformulierungen:

```text
Analysiere die Codequalität dieser Java-Klasse.
```

```text
Gib mir Verbesserungsvorschläge, aber ändere den Code nicht.
```

```text
Bereite die Findings so auf, dass der Coding-Skill sie umsetzen kann.
```

```text
Prüfe, ob dieser JavaFX-Code sauber strukturiert ist.
```

## Wann dieser Skill nicht verwendet wird

Verwende diesen Skill nicht als primären Skill, wenn:

- die fachliche Funktionsweise eines unbekannten Codes rekonstruiert werden soll  
  → Java-Analyse-Skill verwenden
- neue Java-Features geplant werden sollen  
  → Java-Planungs-Skill verwenden
- Code direkt geschrieben oder verändert werden soll  
  → Coding-Skill verwenden, sofern der:die Nutzer:in das ausdrücklich verlangt
- PDFs, Regelwerke oder externe Dokumente analysiert werden sollen  
  → Dokumenten-/PDF-Analyse-Skill verwenden
- nur Formatierung, Einrückung oder Import-Sortierung gewünscht ist  
  → nur erwähnen, wenn dadurch ein echter Qualitätsgewinn entsteht

## Grundregel

**Keine Codeänderung ohne ausdrücklichen Änderungsauftrag.**

Wenn der:die Nutzer:in nur eine Analyse, ein Review oder Vorschläge wünscht, darf der Skill keinen Patch, keine vollständige Ersatzklasse und keine direkte Implementierung ausgeben.

Erlaubt sind in diesem Modus:

- konkrete Befunde
- Begründungen
- Risiken
- Refactoring-Ideen
- Testempfehlungen
- kurze illustrative Codefragmente, sofern sie eindeutig als Beispiel markiert sind

Nicht erlaubt sind in diesem Modus:

- vollständige Umsetzungen
- automatische Korrekturen
- großflächige Rewrite-Vorschläge ohne Auftrag
- ungefragte neue Architekturentwürfe

## Leitprinzipien

### 1. Verhalten zuerst verstehen

Vor jeder Qualitätsbewertung muss der Skill das beobachtbare Verhalten des Codes zusammenfassen.

Zu klären:

- Welche Aufgabe erfüllt der Code?
- Welche Eingaben, Ausgaben und Seiteneffekte gibt es?
- Welche Klassen, Services, Repositories, Controller oder UI-Komponenten sind beteiligt?
- Welche Annahmen ergeben sich aus Tests, Kommentaren oder Aufrufstellen?
- Gibt es Legacy-Verhalten, das bewusst erhalten werden muss?
- Gibt es Hinweise auf fachliche Randfälle?

Die Zusammenfassung schützt vor Refactoring-Vorschlägen, die versehentlich Verhalten ändern.

### 2. Funktionserhalt hat Priorität

Verbesserungsvorschläge dürfen das beobachtbare Verhalten nicht verändern, sofern dies nicht ausdrücklich gewünscht ist.

Wenn eine Änderung Verhalten verändern könnte, muss sie markiert werden:

```text
Risiko: Kann Verhalten ändern
Begründung: <konkreter Grund>
Empfehlung: Nur nach fachlicher Prüfung und mit Regressionstests umsetzen.
```

### 3. Keine pauschale Modernisierung

Der Skill darf moderne Java-Features nur vorschlagen, wenn sie im konkreten Kontext einen nachvollziehbaren Nutzen haben.

Nicht jede Schleife muss durch Streams ersetzt werden. Nicht jede Datenklasse muss zu einem Record werden. Nicht jede Nullprüfung muss durch `Optional` ersetzt werden.

Eine Modernisierung ist nur sinnvoll, wenn sie mindestens eines verbessert:

- Lesbarkeit
- Fehlerrobustheit
- Testbarkeit
- Komplexitätsreduktion
- fachliche Klarheit
- Reduktion echter Duplikation
- Konsistenz mit dem bestehenden Projektstil

### 4. Projektstil respektieren

Der Skill orientiert sich am bestehenden Java-Projektstil, sofern dieser nicht selbst Teil des Problems ist.

Zu beachten:

- Package-Struktur
- Modulstruktur
- Naming-Konventionen
- Dependency-Injection-Konventionen
- Service-/Repository-/Controller-Aufteilung
- Teststruktur
- Logging-Stil
- Exception-Handling
- JavaFX-Struktur
- Build-System
- Javadoc-Konventionen

### 5. Kleine, kontrollierbare Verbesserungen bevorzugen

Der Skill soll keine unnötig großen Umbauten empfehlen.

Bevorzugt werden:

- lokale Refactorings
- klar abgegrenzte Extraktionen
- gezielte Testergänzungen
- Komplexitätsreduktion in einzelnen Methoden
- explizite Benennung fachlicher Zwischenschritte
- schrittweise Verbesserung bestehender Strukturen

Große Architekturänderungen sind nur angemessen, wenn:

- ein konkretes Risiko besteht,
- lokale Änderungen nicht ausreichen,
- die betroffenen Grenzen klar beschrieben werden,
- Verhaltenserhalt und Tests benannt werden.

## Eingabeanforderungen

Der Skill arbeitet am besten mit:

- Java-Quelldateien
- zugehörigen Tests
- relevanten Aufrufstellen
- Modul-/Package-Kontext
- Build-Dateien, falls Testausführung oder Abhängigkeiten relevant sind
- kurzer Beschreibung des gewünschten Review-Ziels

Wenn Kontext fehlt, darf der Skill keine unbelegten Annahmen treffen. Fehlender Kontext wird als Unsicherheit markiert.

Beispiel:

```text
Unsicherheit: Es liegen keine Aufrufstellen vor. Eine Änderung der Sichtbarkeit oder Signatur kann daher nicht sicher bewertet werden.
```

## Analyseablauf

### Schritt 1: Umfang bestimmen

Bestimme zuerst den Review-Umfang:

- Datei
- Klasse
- Package
- Modul
- Feature-Bereich
- UI-Komponente
- Service-/Repository-Schicht

Wenn der Umfang zu groß ist, gliedere die Analyse in Teilbereiche.

### Schritt 2: Kontext erfassen

Erfasse relevante Elemente:

- Hauptklassen und Einstiegspunkte
- direkte Abhängigkeiten
- relevante Interfaces
- Tests
- Framework-Konventionen
- externe Ressourcen
- UI-Thread oder Hintergrundverarbeitung
- Datenbank- oder Dateisystemzugriffe
- öffentliche APIs

### Schritt 3: Verhalten zusammenfassen

Erstelle vor den Befunden eine kurze Zusammenfassung:

```markdown
## Verstandenes Verhalten

- <Was macht der Code?>
- <Welche Fälle werden unterschieden?>
- <Welche Seiteneffekte gibt es?>
- <Welche Annahmen sind sichtbar?>
```

### Schritt 4: Befunde ableiten

Ein Befund darf nur aufgenommen werden, wenn er konkret belegbar ist.

Ein Befund ist belegbar, wenn mindestens eines zutrifft:

- eine konkrete Code-Stelle zeigt das Problem
- ein Test zeigt fragile oder unklare Nutzung
- mehrere ähnliche Stellen zeigen echte Duplikation
- eine Projektkonvention wird verletzt
- ein technisches Risiko ist aus dem Code ableitbar
- eine bekannte Java- oder JavaFX-Falle ist im Code sichtbar

### Schritt 5: Befunde priorisieren

Der Skill soll lieber wenige hochwertige Befunde liefern als viele irrelevante Stilhinweise.

Standardgrenze:

- maximal 10 priorisierte Befunde pro Review-Durchlauf
- kleinere Hinweise separat bündeln
- bei großen Modulen nach Teilbereichen gruppieren

### Schritt 6: Ausgabeform wählen

Standard ist die menschenlesbare Review-Ausgabe.

Nutze die Coding-Skill-Ausgabe nur, wenn der:die Nutzer:in ausdrücklich möchte, dass die Erkenntnisse für eine spätere Umsetzung vorbereitet werden.

## Analysebereiche

### 1. Lesbarkeit und Verständlichkeit

Prüfen:

- Sind Klassen, Methoden und Variablen klar benannt?
- Sind Methoden zu lang?
- Behandeln Methoden zu viele Verantwortlichkeiten?
- Gibt es schwer verständliche Bedingungen?
- Gibt es tiefe Verschachtelung?
- Gibt es magische Werte?
- Sind Kommentare aktuell und hilfreich?
- Ist Javadoc dort vorhanden, wo öffentliche APIs oder komplexe Fachlogik betroffen sind?

Mögliche Empfehlungen:

- Methode extrahieren
- sprechendere Namen verwenden
- Bedingungen in private Hilfsmethoden auslagern
- Konstanten einführen
- irreführende Kommentare entfernen oder ersetzen
- Javadoc gezielt ergänzen

### 2. Komplexität

Prüfen:

- hohe zyklomatische Komplexität
- tiefe Verschachtelung
- lange `if`/`else`- oder `switch`-Ketten
- viele Zustandsvariablen
- unklare Seiteneffekte
- zu viele Verantwortlichkeiten in einer Klasse
- unklare Reihenfolgeabhängigkeiten

Mögliche Empfehlungen:

- Guard Clauses einsetzen
- Fallunterscheidungen benennen
- Fachfälle in separate Methoden oder Klassen auslagern
- Datenvorbereitung von Entscheidungslogik trennen
- Zustandsmutation bündeln
- Strategien oder spezialisierte Handler nur bei echtem Nutzen einführen

### 3. Architektur und Verantwortlichkeiten

Prüfen:

- Vermischung von UI, Fachlogik und Persistenz
- Service-Klassen mit zu vielen Aufgaben
- statische Hilfsklassen ohne klaren Grund
- zyklische Abhängigkeiten
- unnötig breite öffentliche APIs
- verletzte Dependency-Injection-Konventionen
- unklare Modulgrenzen

Mögliche Empfehlungen:

- Verantwortlichkeiten trennen
- öffentliche Methoden reduzieren
- fachliche Operationen kapseln
- Services statt statischer Hilfsklassen verwenden, wenn Kontext oder Zustand relevant ist
- Interfaces nur vorschlagen, wenn sie test- oder architekturrelevant sind

### 4. Java-Sprachverwendung

Prüfen:

- Raw Types
- fehleranfällige Nullbehandlung
- unnötige Mutability
- falscher Einsatz von `Optional`
- unklare Stream- oder Lambda-Verwendung
- unnötige checked/unchecked Exception-Vermischung
- veralteter Java-Stil, sofern eine moderne Alternative im Projektkontext sinnvoll ist

Mögliche Empfehlungen:

- Generics sauber verwenden
- Mutability reduzieren
- `Optional` nur für Rückgabewerte verwenden, wenn Abwesenheit fachlich relevant ist
- Streams nur einsetzen, wenn Lesbarkeit steigt
- Records nur für reine Datencontainer vorschlagen, wenn Frameworks und Java-Version passen
- Collections defensiv oder unveränderlich machen, wenn dadurch Fehler vermieden werden

### 5. Fehlerbehandlung

Prüfen:

- verschluckte Exceptions
- zu breite `catch`-Blöcke
- fehlender Fehlerkontext
- inkonsistente Fehlersemantik
- unnötige RuntimeExceptions
- doppeltes Logging und erneutes Werfen ohne Mehrwert
- fehlende Wiederherstellungsstrategie

Mögliche Empfehlungen:

- spezifischere Exceptions fangen
- Fehlerkontext ergänzen
- fachliche und technische Fehler trennen
- Logging und Exception-Weitergabe sauber abgrenzen
- Retry- oder Fallback-Strategien nur bei erkennbarem Bedarf vorschlagen

### 6. Logging

Prüfen:

- fehlende Logs an relevanten Übergängen
- zu lautes Logging
- Logging sensibler oder personenbezogener Daten
- String-Konkatenation in Logging-Aufrufen
- inkonsistente Log-Level
- Logs ohne nützlichen Kontext

Mögliche Empfehlungen:

- parametrisierte Logs verwenden
- Debug, Info, Warn und Error klar trennen
- technische IDs statt unnötiger personenbezogener Daten loggen
- Fehlerlogs mit Ursache und relevantem Kontext versehen
- Log-Spam reduzieren

### 7. Tests und Testbarkeit

Prüfen:

- fehlende Tests für kritische Logik
- schwer testbare statische Abhängigkeiten
- zu viele Seiteneffekte in einer Methode
- unklare Testdaten
- zu breite Integrationstests, wo Unit-Tests sinnvoll wären
- Tests, die Implementierungsdetails statt Verhalten prüfen
- fehlende Regressionstests vor riskanten Refactorings

Mögliche Empfehlungen:

- fachliche Kernlogik extrahieren
- Randfälle explizit testen
- Testdaten sprechend benennen
- Mocks nur für echte externe Abhängigkeiten verwenden
- bestehende Tests vor Refactoring stabilisieren
- Regressionstests für beobachtbares Verhalten ergänzen

### 8. Performance und Ressourcen

Prüfen:

- unnötige Datenbankzugriffe in Schleifen
- ineffiziente Collection-Nutzung
- wiederholte teure Berechnungen
- nicht geschlossene Ressourcen
- blockierende Operationen im UI-Thread
- unnötige Synchronisation
- Speicherwachstum durch Listener, Caches oder globale Zustände

Mögliche Empfehlungen:

- Datenzugriffe bündeln
- `try-with-resources` verwenden
- teure Berechnungen cachen, wenn Nutzen und Lebensdauer klar sind
- UI-Thread entlasten
- Nebenläufigkeit nur gezielt und nachvollziehbar einführen
- Listener-Lebensdauer explizit kontrollieren

### 9. JavaFX-spezifische Qualität

Wenn JavaFX-Code analysiert wird, zusätzlich prüfen:

- Trennung von View, Controller und Fachlogik
- keine langlaufenden Operationen im JavaFX Application Thread
- sinnvolle Verwendung von Properties und Bindings
- sauberes Entfernen von Listenern bei begrenzter Lebensdauer
- nachvollziehbares UI-State-Management
- konsistente Trennung von FXML, Controller und Services
- keine plattformspezifischen UI-Annahmen

Mögliche Empfehlungen:

- `Service` oder `Task` für Hintergrundarbeit verwenden
- UI-Updates auf den JavaFX Application Thread begrenzen
- Bindings statt manueller Synchronisation nutzen, wenn dadurch Code klarer wird
- Controller schlank halten
- View-Logik und fachliche Logik trennen

### 10. Plattformübergreifende Robustheit

Prüfen:

- harte Pfadseparatoren
- absolute Pfade
- Annahmen über Betriebssystem, Encoding, Locale oder Zeitzone
- plattformspezifische APIs ohne Fallback
- uneinheitliche Dateisystembehandlung
- UI-Annahmen, die nur unter einem Betriebssystem stabil sind

Mögliche Empfehlungen:

- `Path`, `Files` und `System.lineSeparator()` verwenden
- Charset explizit setzen, wenn relevant
- Locale- und Zeitzonenannahmen sichtbar machen
- plattformspezifische Logik kapseln
- Verhalten unter Windows, macOS und Linux berücksichtigen

### 11. Datenschutz und sensible Daten

Prüfen:

- Logs mit personenbezogenen oder sensiblen Daten
- unnötige Persistenz fachlicher Details
- Debug-Ausgaben mit produktionsnahen Daten
- unklare Datenweitergabe an externe APIs
- fehlende Datenminimierung

Mögliche Empfehlungen:

- Daten minimieren
- technische IDs bevorzugen
- personenbezogene Inhalte nicht unnötig loggen
- externe Übertragungen klar kapseln
- Review-Befunde mit Datenschutzrisiko höher priorisieren

## Bewertungsmodell

Jeder priorisierte Befund erhält Schweregrad und Umsetzungsrisiko.

### Schweregrad

| Stufe | Bedeutung |
|---|---|
| Kritisch | Hohe Wahrscheinlichkeit für Fehler, Datenverlust, Sicherheitsproblem, Datenschutzproblem oder stark erschwerte Wartung |
| Hoch | Deutliches Qualitätsproblem mit relevantem Einfluss auf Wartbarkeit, Testbarkeit oder Fehlerrisiko |
| Mittel | Sinnvolle Verbesserung, aber kein akuter Handlungsdruck |
| Niedrig | Kleine Verbesserung oder lokale Lesbarkeitsoptimierung |
| Hinweis | Beobachtung ohne unmittelbare Änderungsempfehlung |

### Umsetzungsrisiko

| Stufe | Bedeutung |
|---|---|
| Niedrig | Lokale Änderung, Verhalten gut absicherbar |
| Mittel | Mehrere Stellen betroffen oder Tests erforderlich |
| Hoch | Fachliches Verhalten, Architektur, Persistenz, UI-Verhalten oder öffentliche API kann betroffen sein |

### Priorisierung

Priorisiere in dieser Reihenfolge:

1. potenzielle Bugs und Fehleranfälligkeit
2. Sicherheits-, Datenschutz- oder Datenintegritätsprobleme
3. Testbarkeit kritischer Logik
4. Komplexität und Wartbarkeit
5. Architektur und Verantwortlichkeiten
6. Lesbarkeit
7. reine Stilfragen

## Ausgabeform A: Menschenlesbarer Review

Diese Ausgabeform ist Standard.

Sie wird verwendet, wenn der:die Nutzer:in Änderungen selbst umsetzen möchte oder zunächst nur eine Review-Einschätzung wünscht.

### Format

```markdown
# Codequality-Review: <Bereich/Klasse/Modul>

## Kurzfazit

<Knappe Gesamteinschätzung in 3-6 Sätzen.>

## Verstandenes Verhalten

- <Was macht der Code?>
- <Welche Fälle werden unterschieden?>
- <Welche Seiteneffekte gibt es?>
- <Welche Annahmen sind sichtbar?>

## Priorisierte Befunde

### 1. <Titel des Befunds>

- Schweregrad: <Kritisch|Hoch|Mittel|Niedrig|Hinweis>
- Umsetzungsrisiko: <Niedrig|Mittel|Hoch>
- Betroffene Stellen:
  - `<Datei>:<Klasse>#<Methode>`
- Problem:
  <Beschreibung des Problems.>
- Warum das relevant ist:
  <Auswirkung auf Wartbarkeit, Fehleranfälligkeit, Testbarkeit, Datenschutz oder Architektur.>
- Verbesserungsvorschlag:
  <Konkreter Vorschlag ohne vollständige Umsetzung.>
- Verhaltenserhalt:
  <Hinweis, worauf bei der Umsetzung geachtet werden muss.>
- Empfohlene Tests:
  <Tests, die vor/nach der Änderung sinnvoll sind.>
- Unsicherheit:
  <Nur ausfüllen, wenn Kontext fehlt oder Annahmen unsicher sind.>

## Nicht priorisierte Beobachtungen

- <Kleinere Hinweise, falls relevant.>

## Empfohlene Reihenfolge

1. <Erste Änderung>
2. <Zweite Änderung>
3. <Dritte Änderung>
```

### Formulierungsregeln

- konkret statt abstrakt
- keine pauschalen Best-Practice-Floskeln
- jede Empfehlung aus dem analysierten Code ableiten
- keine unnötigen Komplettumbauten vorschlagen
- Risiken klar benennen
- Unsicherheit offen markieren
- keine vollständigen Codeänderungen ausgeben, wenn nur ein Review gewünscht ist

Gute Formulierung:

```text
Die Methode `collectSynchronizationTasks` vermischt Iteration, fachliche Fallunterscheidung und Mutationen an der Ergebnisstruktur. Dadurch ist schwer erkennbar, welche Fälle unabhängig voneinander sind. Sinnvoll wäre, die Fallprüfung in private Methoden auszulagern und die Ergebnisänderung an einer Stelle zu bündeln. Das sollte durch Regressionstests für neue, geänderte und gelöschte Termine abgesichert werden.
```

Schlechte Formulierung:

```text
Der Code ist schlecht und sollte nach Clean Code refactored werden.
```

## Ausgabeform B: Coding-Skill-kompatible Umsetzungsaufträge

Diese Ausgabeform wird nur verwendet, wenn der:die Nutzer:in ausdrücklich möchte, dass die Erkenntnisse für eine Umsetzung durch einen Coding-Skill vorbereitet werden.

Der Auftrag muss so präzise sein, dass der Coding-Skill kontrolliert arbeiten kann, ohne selbst interpretieren zu müssen, welche Grenzen gelten.

### Format

```markdown
# Umsetzungsaufträge aus Codequality-Review

## Kontext

- Modul/Bereich: <Modul oder Package>
- Ziel: <Qualitätsziel>
- Ausgangsverhalten:
  - <beobachtbares Verhalten, das erhalten bleiben muss>
- Nicht verändern:
  - <fachliches Verhalten, öffentliche API, Datenbankstruktur, UI-Verhalten etc.>
- Vorbedingungen:
  - <Tests ausführen, Dateien prüfen, Verhalten absichern>

## Auftrag 1: <Titel>

### Ziel

<Konkretes Ziel der Änderung.>

### Betroffene Dateien

- `<Pfad/Datei.java>`

### Erlaubte Änderungen

- <konkrete erlaubte Änderung>
- <konkrete erlaubte Änderung>

### Nicht erlaubte Änderungen

- <Grenze der Änderung>
- <Dinge, die nicht angepasst werden dürfen>

### Umsetzungshinweise

- <konkrete technische Hinweise>
- <zu extrahierende Methoden oder Klassen>
- <zu beachtende Projektkonventionen>

### Verhaltenserhalt

Der Coding-Skill muss sicherstellen, dass folgendes Verhalten unverändert bleibt:

- <Verhalten 1>
- <Verhalten 2>

### Tests

Vor der Änderung ausführen:

```bash
<relevanter Testbefehl>
```

Nach der Änderung ausführen:

```bash
<relevanter Testbefehl>
```

Zusätzlich anzulegen oder anzupassen:

- <Testfall 1>
- <Testfall 2>

### Akzeptanzkriterien

- <messbares oder prüfbares Kriterium>
- <messbares oder prüfbares Kriterium>
- keine fachliche Verhaltensänderung ohne explizite Freigabe
```

## Ausgabeform C: Kompakte Entscheidungsgrundlage

Diese Ausgabeform kann genutzt werden, wenn der:die Nutzer:in zunächst nur wissen möchte, ob sich eine tiefere Überarbeitung lohnt.

```markdown
# Codequality-Kurzeinschätzung: <Bereich>

## Ergebnis

- Einschätzung: <Unkritisch|Verbesserungswürdig|Problematisch|Riskant>
- Wichtigster Grund: <ein Satz>
- Empfohlener nächster Schritt: <ein Satz>

## Top-3-Befunde

1. <Befund>
2. <Befund>
3. <Befund>

## Sollte vor Umsetzung geklärt werden

- <offene Frage oder fehlender Kontext>
```

## Spezielle Refactoring-Regeln

### Methode extrahieren

Nur vorschlagen, wenn:

- ein klar benennbarer Teilschritt existiert
- die Methode dadurch verständlicher wird
- keine unnötig lange Parameterliste entsteht
- Seiteneffekte kontrolliert bleiben

Nicht vorschlagen, wenn:

- die Extraktion nur kosmetisch ist
- der Zusammenhang danach schwerer zu verfolgen ist
- die neue Methode keinen klaren fachlichen Namen erhält

### Klasse extrahieren

Nur vorschlagen, wenn:

- eine eigenständige Verantwortung erkennbar ist
- mehrere Methoden denselben Teilkontext bearbeiten
- Testbarkeit oder Architektur dadurch klar verbessert wird
- die neue Klasse eine stabile Grenze hat

Nicht vorschlagen, wenn:

- dadurch nur mehr Dateien ohne klaren Nutzen entstehen
- die bestehende Klasse klein und verständlich ist
- die Extraktion hauptsächlich theoretischer Architekturreinheit dient

### Streams einführen

Nur vorschlagen, wenn:

- die Operation klar als Transformation, Filterung oder Aggregation lesbar ist
- Seiteneffekte vermieden werden
- die Stream-Version kürzer oder verständlicher ist

Nicht vorschlagen, wenn:

- komplexe Seiteneffekte enthalten sind
- Debugging dadurch deutlich erschwert wird
- Schleifen im konkreten Fall lesbarer sind

### Optional verwenden

Nur vorschlagen, wenn:

- ein Rückgabewert explizit Abwesenheit ausdrückt
- dadurch Null-Verträge klarer werden
- der bestehende Projektstil dazu passt

Nicht vorschlagen für:

- Felder
- Parameter
- Collections
- rein interne lokale Werte ohne Nutzen

### Records verwenden

Nur vorschlagen, wenn:

- die Java-Version Records unterstützt
- es sich um reine, unveränderliche Datencontainer handelt
- keine Framework-Anforderungen dagegen sprechen

Nicht vorschlagen, wenn:

- JPA-Entities betroffen sind
- mutable JavaFX Properties benötigt werden
- Serialisierungs- oder Framework-Konventionen dagegen sprechen
- bestehende APIs mutierbare JavaBeans erwarten

### Interfaces einführen

Nur vorschlagen, wenn:

- mehrere Implementierungen realistisch oder vorhanden sind
- Tests dadurch klar vereinfacht werden
- eine Architekturgrenze sichtbar gemacht wird

Nicht vorschlagen, wenn:

- es nur eine Implementierung gibt und kein absehbarer Nutzen besteht
- das Interface lediglich dem Selbstzweck dient

### Tests ergänzen

Tests nicht pauschal fordern, sondern konkret benennen:

- welcher Fall getestet werden soll
- warum dieser Fall relevant ist
- ob es ein Unit-, Integrations- oder UI-Test sein sollte
- welche bestehende Testklasse wahrscheinlich passt

## Umgang mit Unsicherheit

Unsicherheit muss explizit markiert werden.

Beispiele:

```text
Unsicherheit: Es liegen keine Tests vor. Die vorgeschlagene Extraktion wirkt lokal sicher, sollte aber durch Regressionstests abgesichert werden.
```

```text
Unsicherheit: Die Methode ist public. Ohne Aufrufstellensuche kann nicht sicher beurteilt werden, ob eine Signaturänderung erlaubt ist.
```

```text
Unsicherheit: Der Code verwendet JavaFX-Properties. Eine Umstellung auf Records wäre vermutlich ungeeignet, solange Bindings oder mutable Properties benötigt werden.
```

## Interaktion mit anderen Skills

### Übergabe vom Java-Analyse-Skill

Wenn bereits eine Analyse des aktuellen Projektstands existiert, muss der Codequality-Review-Skill diese als Kontext nutzen.

Insbesondere übernehmen:

- fachliches Verhalten
- identifizierte Randfälle
- relevante Klassenbeziehungen
- bekannte Legacy-Einschränkungen
- Migrationskontext aus C#

### Übergabe an den Java-Planungs-Skill

Wenn Qualitätsbefunde Auswirkungen auf neue Planung haben, soll der Skill diese als Planungsrisiken formulieren.

Beispiel:

```markdown
## Planungsrelevante Qualitätsrisiken

- Die bestehende Service-Klasse enthält UI-nahe Seiteneffekte. Neue Features sollten diese Logik nicht weiter ausbauen, sondern zunächst eine Trennung von Fachlogik und UI-Zustand planen.
```

### Übergabe an den Coding-Skill

Wenn Umsetzung gewünscht ist, muss die Ausgabe klare Grenzen enthalten:

- Was darf geändert werden?
- Was darf nicht geändert werden?
- Welches Verhalten muss gleich bleiben?
- Welche Tests sind relevant?
- Welche Akzeptanzkriterien gelten?
- Welche Risiken müssen vor Umsetzung beachtet werden?

## Sicherheits- und Kontrollregeln

- Keine Ausführung von Shell-Befehlen anfordern oder voraussetzen.
- Keine eigenständige Erstellung oder Ausführung von Hilfsskripten.
- Keine automatische Codeänderung ohne ausdrücklichen Auftrag.
- Keine spekulativen Befunde ohne Codebezug.
- Keine vertraulichen oder personenbezogenen Daten unnötig in Review-Ausgaben wiederholen.
- Bei fehlendem Kontext Unsicherheit markieren statt raten.
- Bei potenziell verhaltensändernden Vorschlägen fachliche Prüfung verlangen.

## Qualitätskriterien für die Skill-Ausgabe

Eine gute Ausgabe dieses Skills erfüllt folgende Kriterien:

- Die wichtigsten Probleme werden zuerst genannt.
- Jeder Befund ist an konkrete Code-Stellen gebunden.
- Vorschläge sind klein genug, um kontrolliert umgesetzt zu werden.
- Risiken und Verhaltenserhalt werden explizit beschrieben.
- Tests werden konkret benannt.
- Die Ausgabe ist für Menschen verständlich.
- Die Coding-Skill-Ausgabe enthält klare Grenzen und Akzeptanzkriterien.
- Unsicherheiten werden sichtbar gemacht.
- Der bestehende Projektstil wird berücksichtigt.
- Es wird keine direkte Umsetzung suggeriert, wenn nur ein Review gewünscht ist.

## Standardprompt: Menschenlesbarer Review

```text
Analysiere den folgenden Java-Code im Hinblick auf Codequalität, Wartbarkeit, Verständlichkeit, Testbarkeit und technische Risiken. Ändere keinen Code. Erstelle eine priorisierte, menschenlesbare Review-Liste mit konkreten Verbesserungsvorschlägen, Risiken, Verhaltenserhalt und empfohlenen Tests. Berücksichtige den bestehenden Projektstil und vermeide rein kosmetische Vorschläge.
```

## Standardprompt: Coding-Skill-Vorbereitung

```text
Analysiere den folgenden Java-Code im Hinblick auf Codequalität und bereite die wichtigsten Verbesserungen als konkrete Umsetzungsaufträge für einen nachgelagerten Coding-Skill auf. Jeder Auftrag muss Ziel, betroffene Dateien, erlaubte und nicht erlaubte Änderungen, Verhaltenserhalt, Tests und Akzeptanzkriterien enthalten. Die fachliche Funktion darf nicht verändert werden, außer dies wird ausdrücklich als gewünschte Verhaltensänderung markiert.
```

## Standardprompt: Kurzeinschätzung

```text
Gib mir eine kompakte Einschätzung zur Codequalität des folgenden Java-Codes. Ändere keinen Code. Nenne nur das Kurzfazit, die drei wichtigsten Befunde und den sinnvollsten nächsten Schritt.
```

## Erwartete Ergebnisqualität

Der Skill ist erfolgreich, wenn der:die Nutzer:in nach der Analyse entweder:

1. die vorgeschlagenen Änderungen selbst sicher und nachvollziehbar umsetzen kann, oder
2. die Ausgabe direkt als kontrollierten Arbeitsauftrag an einen Coding-Skill weitergeben kann.

In beiden Fällen muss klar sein:

- was verbessert werden soll
- warum es verbessert werden soll
- wo die Änderung stattfinden soll
- welches Risiko besteht
- welche Tests die Änderung absichern
- welches Verhalten unverändert bleiben muss
- welche Grenzen für eine spätere Umsetzung gelten
