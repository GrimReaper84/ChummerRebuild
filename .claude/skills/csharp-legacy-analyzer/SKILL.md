---
name: legacy-csharp-code-understanding
description: Analysiert bestehenden, insbesondere älteren oder uneinheitlichen C#-Code, extrahiert tatsächliches Laufzeitverhalten, Fachregeln, Datenflüsse, Seiteneffekte und Migrationsrisiken und bereitet die Ergebnisse für einen nachgelagerten Java-Planungs-Skill auf.
allowed-tools: Read, Grep, Glob
---

# Legacy C# Code Understanding

## Zweck

Dieser Skill dient dazu, vorhandenen C#-Code systematisch zu durchsuchen, zu verstehen und in eine migrationsfähige Wissensbasis zu überführen.

Der Fokus liegt auf dem **Ist-Verhalten** des bestehenden Codes. Der Skill schreibt keinen Java-Code, übersetzt C# nicht direkt nach Java und nimmt keine eigenständigen Refactorings vor. Seine Aufgabe ist es, die fachliche und technische Funktionsweise so präzise zu dokumentieren, dass ein separater Java-Planungs-Skill daraus eine belastbare Neuimplementierung planen kann.

## Wann dieser Skill verwendet wird

Verwende diesen Skill, wenn eine oder mehrere der folgenden Situationen vorliegen:

- C#-Code soll später featureweise nach Java migriert werden.
- Der bestehende C#-Code ist alt, uneinheitlich, schwach dokumentiert oder schwer lesbar.
- Die tatsächliche Funktionsweise eines Features muss vor einer Neuimplementierung verstanden werden.
- Fachregeln, Datenflüsse, Statusübergänge oder Seiteneffekte sollen aus dem Code extrahiert werden.
- Der Java-Planungs-Skill benötigt eine strukturierte Übergabe über das Verhalten des Quellsystems.

Verwende diesen Skill nicht für:

- direkte Implementierung von Java-Code
- automatische 1:1-Übersetzung von C# nach Java
- Refactoring des C#-Codes
- Stilkorrekturen ohne Migrationsbezug
- Performance-Optimierung
- spekulative Rekonstruktion nicht vorhandener Fachlogik

## Grundregeln

### 1. Tatsächliches Verhalten vor vermuteter Absicht

Beschreibe, was der Code nachweisbar tut. Vermute nicht, was er vermutlich tun sollte.

Wenn Code widersprüchlich, fehlerhaft oder unvollständig wirkt:

- beschreibe das beobachtbare Verhalten,
- markiere die Stelle als Risiko oder Unklarheit,
- korrigiere das Verhalten nicht stillschweigend.

### 2. Keine direkte Migration

Dieser Skill darf keine Java-Klassen, Java-Methoden oder JavaFX-Strukturen ausarbeiten. Er darf nur migrationsrelevante Erkenntnisse aufbereiten.

Zulässig:

```text
Die Methode gruppiert Einträge faktisch nach `customerId`, verändert dabei aber die Reihenfolge der Einträge. Diese Reihenfolgeabhängigkeit ist migrationsrelevant.
```

Nicht zulässig:

```text
Ersetze die Schleife in Java durch einen Stream mit Collectors.groupingBy.
```

### 3. Keine eigenmächtige Modernisierung

Legacy-Verhalten darf nicht als überflüssig verworfen werden, nur weil es technisch unsauber wirkt. Auch Workarounds, Sonderfälle und historisch gewachsene Abhängigkeiten können fachlich relevant sein.

### 4. Belegbarkeit

Jede relevante Aussage muss auf Code, Konfiguration, Tests oder eindeutig identifizierten Projektartefakten beruhen. Wenn der Beleg fehlt, ist die Aussage entsprechend zu kennzeichnen.

Nutze folgende Einstufungen:

| Label | Bedeutung |
|---|---|
| `Gesichert` | Direkt aus Code, Tests oder Konfiguration ableitbar |
| `Wahrscheinlich` | Plausibel, aber nicht vollständig belegbar |
| `Unklar` | Kontext fehlt oder mehrere Interpretationen sind möglich |
| `Risiko` | Fehleranfällig oder migrationskritisch |
| `Altlast` | Technisch fragwürdig, aber potenziell fachlich relevant |
| `Nicht übernehmen ohne Prüfung` | Verhalten könnte Bug, historischer Workaround oder unbeabsichtigter Nebeneffekt sein |

## Arbeitsweise

### Phase 1: Analyseumfang klären

Bestimme zuerst, was analysiert werden soll:

- gesamtes Projekt
- einzelnes Feature
- einzelnes Modul
- einzelne Klasse
- einzelner Codepfad
- einzelne Methode

Wenn der Umfang nicht ausdrücklich genannt ist, beginne mit einer groben Orientierung im Projekt und priorisiere die wahrscheinlich relevanten Einstiegspunkte.

### Phase 2: Projektstruktur erfassen

Untersuche die vorhandene Struktur, ohne sofort Detailanalysen jeder Datei durchzuführen.

Prüfe insbesondere:

- Solution- und Projektdateien (`.sln`, `.csproj`)
- Namespaces und Modulgrenzen
- zentrale Einstiegspunkte
- UI-Schichten
- Service- oder Manager-Klassen
- Datenzugriffsschichten
- DTOs, Entities und Models
- Konfigurationsdateien
- Tests
- generierte Dateien
- externe Bibliotheken und Frameworks

Ergebnisformat:

```markdown
## Projektüberblick

### Vermuteter Zweck des analysierten Bereichs
...

### Relevante Projekte/Module
- ...

### Zentrale Einstiegspunkte
- ...

### Wichtige Klassen und Verantwortlichkeiten
| Klasse | Vermutete Rolle | Beleg | Relevanz |
|---|---|---|---|

### Externe Abhängigkeiten
- ...

### Fehlender Kontext
- ...
```

### Phase 3: Codepfade priorisieren

Analysiere nicht alle Dateien gleich tief. Priorisiere Code mit hoher fachlicher oder migrationsbezogener Relevanz:

1. öffentliche APIs, Controller, Commands, Event-Handler oder UI-Aktionen
2. Service-, Manager- und Coordinator-Klassen
3. Methoden mit fachlichen Entscheidungen
4. Datenbank-, Datei- oder Netzwerkzugriffe
5. Validierungen
6. Statusänderungen
7. Mapping zwischen Datenmodellen
8. Fehlerbehandlung
9. Tests, sofern vorhanden
10. komplexe Bedingungen, Schleifen oder rekursive Abläufe

Dokumentiere, warum ein Codepfad als relevant betrachtet wird.

### Phase 4: Methoden und Codepfade analysieren

Für jede relevante Methode oder jeden relevanten Codepfad ist folgendes Format zu verwenden:

```markdown
## Codepfad: `ClassName.MethodName(...)`

### Einordnung
- Kategorie: Fachlogik | Persistenz | UI | Infrastruktur | Mapping | Validierung | Sonstiges
- Relevanz: hoch | mittel | niedrig
- Beleg: Datei/Klasse/Methode

### Zweck
...

### Eingaben
| Eingabe | Typ | Bedeutung | Sonderfälle |
|---|---|---|---|

### Ausgaben und Ergebnis
- Rückgabewert:
- veränderte Objekte:
- erzeugte Daten:
- gelöschte Daten:

### Ablauf
1. ...
2. ...
3. ...

### Bedingungen und Sonderfälle
| Bedingung | Verhalten | Migrationsrelevanz |
|---|---|---|

### Seiteneffekte
- Datenbank:
- Dateien:
- Netzwerk/API:
- UI-Zustand:
- globale/statische Zustände:
- Logging:
- Zeit/Datum:

### Fehlerverhalten
- Ausnahmen:
- geschluckte Fehler:
- Rückgabewerte bei Fehlern:
- Logging bei Fehlern:

### Abhängigkeiten
- direkte Aufrufe:
- indirekte Aufrufe:
- Framework-Verhalten:
- Konfiguration:

### Bewertung für spätere Java-Planung
- Zwingend zu übernehmen:
- Fachlich zu prüfen:
- Wahrscheinlich technische Altlast:
- Nicht übernehmen ohne Prüfung:
- Testszenarien, die daraus abgeleitet werden sollten:
```

## Analysebereiche

### Fachlogik

Extrahiere fachliche Regeln unabhängig von der konkreten C#-Syntax.

```markdown
## Extrahierte Fachregeln

### Regel: [Name]

**Status:** Gesichert | Wahrscheinlich | Unklar

**Beschreibung:**
Wenn ..., dann ...

**Quelle:**
- Datei:
- Klasse:
- Methode:
- Codepfad/Bedingung:

**Migrationsrelevanz:**
...

**Offene Fragen:**
- ...
```

### Datenfluss

Dokumentiere, welche Daten gelesen, verändert, erzeugt, gelöscht oder weitergereicht werden.

```markdown
## Datenfluss: [Name]

### Quellen
- ...

### Transformationen
1. ...
2. ...

### Ziele
- ...

### Mutationen
| Objekt/Feld | Vorher | Nachher | Auslöser | Quelle |
|---|---|---|---|---|

### Kritische Details
- Nullbehandlung:
- Defaultwerte:
- Formatierung/Parsing:
- Reihenfolgeabhängigkeiten:
- Seiteneffekte:
```

### Datenmodelle und Mapping

Bei DTOs, Entities, ViewModels oder Persistenzmodellen:

```markdown
## Datenmodell: `ClassName`

### Rolle
DTO | Entity | ViewModel | Domain Model | Persistenzmodell | Sonstiges

### Felder
| Feld | C#-Typ | Bedeutung | Nullable | Default | Wird gelesen in | Wird geschrieben in |
|---|---|---|---|---|---|---|

### Beziehungen
- Verweist auf:
- Wird verwendet von:
- Wird gemappt auf:
- Wird persistiert in:

### Mapping-Regeln
| Quelle | Ziel | Transformation | Sonderfall |
|---|---|---|---|

### Hinweise für Java-Planung
- Relevante Java-Typentscheidung:
- Nullability-Risiko:
- Format-/Parsing-Risiko:
- Identitäts-/Equals-Risiko:
```

### Statusmodelle und Zustandsübergänge

Bei Enums, Flags, Statusfeldern oder booleschen Steuerfeldern:

```markdown
## Statusmodell: [Name]

### Bekannte Statuswerte
| Status/Flag | Bedeutung | Quelle | Sicherheit |
|---|---|---|---|

### Übergänge
| Von | Nach | Auslöser | Bedingung | Seiteneffekt | Quelle |
|---|---|---|---|---|---|

### Auffälligkeiten
- Status wird gesetzt, aber nicht gelesen:
- Status wird gelesen, aber im analysierten Bereich nicht gesetzt:
- Widersprüchliche Flags:
- Implizite Defaultzustände:
```

### Persistenz und externe Systeme

Bei Datenbank-, Datei-, Netzwerk- oder API-Zugriffen:

```markdown
## Externe Abhängigkeit: [Name]

### Art
Datenbank | Datei | Netzwerk | API | Registry | Betriebssystem | Sonstiges

### Zugriffsmuster
- Lesen:
- Schreiben:
- Löschen:
- Transaktion:
- Fehlerverhalten:

### Datenstruktur
- Tabellen/Dateien/Endpunkte:
- Schlüssel/IDs:
- relevante Felder:

### Migrationsrisiken
- Transaktionsgrenzen:
- Lazy Loading / implizite Nachladung:
- Seiteneffekte durch Tracking:
- Reihenfolgeabhängigkeiten:
- Fehlerfälle:
```

### UI-Verhalten

Wenn C#-Code UI-Logik enthält, analysiere sie als Verhalten, nicht als Zielarchitektur.

Zu erfassen sind:

- sichtbare Zustandsänderungen
- aktivierte/deaktivierte Controls
- Validierung vor Benutzeraktionen
- Event-Handler-Reihenfolge
- implizite Bindings
- Seiteneffekte durch UI-Aktionen
- Abhängigkeiten zwischen UI und Fachlogik

Keine JavaFX-Struktur planen. Nur Verhalten dokumentieren.

## Besondere Prüfpunkte für alten C#-Code

Achte insbesondere auf:

- `null` vs. leerer String
- `DateTime.Kind`, lokale Zeit, UTC und Zeitzonen
- kulturabhängiges Parsing oder Formatting
- `decimal` vs. `double`
- Integer-Division
- mutable DTOs
- statische Helper mit verstecktem Zustand
- globale Konfiguration
- Magic Numbers
- hart codierte Strings
- leere oder zu breite `catch`-Blöcke
- Logging statt echter Fehlerbehandlung
- Rückgabewerte als Fehlerindikatoren
- Events und Delegates
- `async`/`await`, `.Result`, `.Wait()`
- LINQ mit Seiteneffekten
- mehrfach enumerierte `IEnumerable`
- deferred execution
- Entity Framework Tracking und Lazy Loading
- Transaktionsgrenzen
- partial classes
- extension methods
- operator overloads
- reflection
- `dynamic`
- bedingte Kompilierung mit `#if`
- generierter Code
- Designer-Dateien
- veraltete Framework-Konventionen
- implizite Abhängigkeiten zu Betriebssystem, Dateipfaden oder Registry

## Umgang mit Unsicherheit

Wenn eine Information nicht sicher ableitbar ist:

1. Nicht raten.
2. Den fehlenden Kontext benennen.
3. Die wahrscheinlich betroffenen Codebereiche nennen.
4. Konkrete nächste Suchpunkte formulieren.

Format:

```markdown
## Offene Frage

**Frage:** ...

**Warum relevant:** ...

**Bisherige Beobachtung:** ...

**Benötigter Kontext:** ...

**Empfohlene nächste Suche:**
- ...
```

## Abschließende Übergabe an den Java-Planungs-Skill

Jede Analyse muss mit folgendem Übergabeblock enden. Dieser Block ist das primäre Ergebnis für den nachgelagerten Java-Planungs-Skill.

```markdown
# Übergabe an Java-Planungs-Skill

## Analyseumfang
- Projekt/Modul/Feature:
- Analysierte Dateien/Klassen:
- Nicht analysierte, aber relevante Bereiche:

## Fachlicher Zweck
...

## Gesichertes Ist-Verhalten
- ...

## Zwingend zu übernehmendes Verhalten
- ...

## Fachregeln
| Regel | Status | Quelle | Migrationsrelevanz |
|---|---|---|---|

## Eingaben
- ...

## Ausgaben
- ...

## Datenmodelle
- ...

## Datenflüsse
- ...

## Statusmodelle und Übergänge
- ...

## Seiteneffekte
- ...

## Fehlerfälle
- ...

## Reihenfolgeabhängigkeiten
- ...

## Nicht offensichtliche Altlogik
- ...

## Risiken für die Java-Neuimplementierung
| Risiko | Ursache | Mögliche Folge | Empfohlene Absicherung |
|---|---|---|---|

## Abgeleitete Testszenarien
- ...

## Offene Fragen
- ...

## Strukturelle Hinweise für Java-Planung
Keine Implementierung. Nur Hinweise für die spätere Planung.

- mögliche Service-Grenzen:
- mögliche Domänenobjekte:
- notwendige Repository-/Gateway-Abstraktionen:
- UI-Verhalten, das erhalten bleiben muss:
- technische Altlasten, die nicht blind übernommen werden sollten:
```

## Qualitätskriterien für die Ausgabe

Eine gute Ausgabe dieses Skills:

- trennt Beobachtung, Interpretation und Risiko sauber,
- benennt Quellen im Code,
- beschreibt Verhalten in fachlich verständlicher Sprache,
- dokumentiert Seiteneffekte und Fehlerfälle,
- macht Null-, Zeit-, Format- und Statusverhalten explizit,
- ist für einen Java-Planungs-Skill direkt weiterverwendbar,
- enthält keine Java-Implementierung,
- enthält keine unbelegte Spekulation.

Eine schlechte Ausgabe dieses Skills:

- übersetzt C# direkt nach Java,
- ignoriert Sonderfälle,
- vereinfacht Legacy-Verhalten ohne Beleg,
- beschreibt nur Klassennamen statt Verhalten,
- vermischt Refactoring-Vorschläge mit Ist-Analyse,
- lässt Fehlerbehandlung und Seiteneffekte aus,
- behandelt unklare Stellen als gesichert.

## Beispielaufrufe

### Analyse eines Features

```text
Use the legacy-csharp-code-understanding skill.

Analyze the C# implementation of the selected feature.
Do not translate it to Java.
Do not refactor it.
Extract actual behavior, business rules, data flow, side effects, error handling and migration risks.
End with the handoff block for the Java planning skill.
```

### Analyse einer einzelnen Klasse

```text
Use the legacy-csharp-code-understanding skill.

Analyze `CustomerExportService.cs` for later Java reimplementation planning.
Focus on actual runtime behavior, inputs, outputs, data mutations, null handling, error behavior and extracted business rules.
Do not produce Java code.
```

### Analyse eines UI-Codepfads

```text
Use the legacy-csharp-code-understanding skill.

Analyze the UI action triggered by `[action name]`.
Document visible UI behavior, validation, called services, state changes, side effects and business rules.
Do not design the JavaFX implementation yet.
```

## Einordnung im Gesamtworkflow

```text
Legacy C# Codebase
   ↓
Legacy C# Code Understanding Skill
   ↓
strukturierte Ist-Analyse und Übergabe
   ↓
Java Planning Skill
   ↓
Java Implementation Skill
```

Der C#-Analyse-Skill verantwortet ausschließlich die belastbare Beschreibung des bestehenden Systems. Architekturentscheidungen, Modernisierung und Java-spezifische Umsetzung gehören in nachgelagerte Skills.
