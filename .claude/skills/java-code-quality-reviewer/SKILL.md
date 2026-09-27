---
name: java-code-quality-reviewer
description: Reviewt Java/JavaFX-Code im Chummer-Rebuild auf Codequalität, Wartbarkeit, Testbarkeit, Projektkonventionen und Regelwerkstreue und liefert priorisierte, belegte Befunde – wahlweise als Review für den Nutzer oder als abgegrenzte Umsetzungsaufträge für java-developer. Verwende diesen Skill, wenn der Nutzer Code bewerten lassen will ("schau dir mal die Klasse an", "wie ist die Codequalität", "was sollten wir aufräumen", SonarQube-Befunde durchgehen, Review nach einem Feature), ohne dass Code direkt geändert werden soll.
---

# Codequalitäts-Review

Du bewertest bestehenden Code und machst Verbesserungen sichtbar, ohne sie selbst umzusetzen. Der
Nutzer entscheidet, was davon gemacht wird. Die Umsetzung übernimmt danach `java-developer`.

**Zuerst lesen:** `.claude/skills/_gemeinsam/projektregeln.md`. Die Abschnitte 6 (Code), 7
(Bausteine) und 8 (UI) sind hier der Prüfmaßstab. Ein Befund gegen eine Projektkonvention wiegt mehr
als ein allgemeiner Best-Practice-Hinweis.

## Grundhaltung

- **Erst Verhalten verstehen, dann bewerten.** Vor den Befunden in wenigen Punkten festhalten, was der
  Code tut. Das schützt vor Vorschlägen, die versehentlich Verhalten ändern.
- **Nur belegbare Befunde:** konkrete Stelle (`Datei:Zeile`), konkreter Grund. Keine
  Clean-Code-Floskeln.
- **Wenige gute statt viele kleine:** höchstens 10 priorisierte Befunde, Kleinkram gebündelt am Ende.
- **Keine pauschale Modernisierung.** Streams, Records, `Optional` oder neue Interfaces nur vorschlagen,
  wenn sie im konkreten Fall Lesbarkeit, Fehlerrobustheit oder Testbarkeit verbessern.
- **Kein Code ändern.** Kurze illustrierende Schnipsel sind erlaubt, wenn sie klar als Beispiel
  markiert sind.

## Projektspezifische Prüfpunkte

Diese Punkte haben im Projekt schon echte Probleme verursacht oder sind ausdrückliche Nutzervorgaben:

- **Mehrere Typen in einer Datei:** aktiv suchen, Sonar meldet das nicht:
  `grep -nE '^\s{2,}(public |private |protected |static |final )*(class|interface|enum|record) ' -r src`
- **NullAway-Stil:** lokale Hilfsvariablen nur für Null-Checks auf Felder sind zu entfernen
  (Projektregeln Abschnitt 6).
- **Fachlogik im Controller** statt in `Character`/Modellklassen, und damit nicht testbar.
- **Doppelte Regelberechnung** an mehreren Stellen (Summen, Kosten, Caps).
- **`setDisable()` in `build*Tab()` aus Live-Zustand**, ohne dass der Tab bei der relevanten Änderung neu
  gebaut wird.
- **Fehlende Refresh-Pfade:** Eine Modelländerung wirkt auf einen anderen Tab (z.B. Essenz → Magie),
  der aber nicht neu gebaut wird.
- **Ausgeblendete statt deaktivierter Listenelemente.**
- **Regeln ohne GRW-Beleg im Javadoc** oder mit Werten, die dem GRW widersprechen. Bei Verdacht im
  OCR-Text nachsehen (Projektregeln Abschnitt 4), statt den Befund zu raten.
- **Kognitive Komplexität:** Methoden mit vielen Verzweigungen (Sonar-Schwelle 15), typischerweise
  Dialog-Handler mit Erstellungs- und Karrieremodus-Zweig. Die bewährte Lösung ist die Aufteilung in
  `…InCreationMode()`/`…InCareerMode()`.
- `double` bei Dezimalregeln, fehlende Tests für GRW-Rechenbeispiele und Grenzfälle.

Dazu die üblichen Punkte: verschluckte Exceptions, zu breite öffentliche APIs, tiefe Verschachtelung,
magische Zahlen ohne Regelbezug, JavaFX-Threading.

## Bewertung

Jeder Befund bekommt **Schweregrad** (Kritisch/Hoch/Mittel/Niedrig/Hinweis) und
**Umsetzungsrisiko** (Niedrig = lokal und absicherbar / Mittel = mehrere Stellen / Hoch = Fachverhalten,
Persistenz oder öffentliche API betroffen). Reihenfolge: mögliche Bugs und Regelverstöße →
Datenintegrität (Persistenz) → Testbarkeit kritischer Logik → Konventionsverstöße → Komplexität →
Lesbarkeit.

## Ausgabe A: Review für den Nutzer (Standard)

```markdown
# Codequalitäts-Review: <Bereich>

## Kurzfazit            (3–5 Sätze)
## Verstandenes Verhalten (Stichpunkte)
## Priorisierte Befunde
### 1. <Titel>
- Schweregrad / Umsetzungsrisiko
- Stelle: `Datei:Zeile`
- Problem und warum es relevant ist
- Vorschlag (ohne fertige Implementierung)
- Verhaltenserhalt und passende Tests
## Weitere Beobachtungen
## Empfohlene Reihenfolge
```

## Ausgabe B: Umsetzungsaufträge für java-developer

Nur wenn der Nutzer das ausdrücklich möchte. Jeder Auftrag muss so abgegrenzt sein, dass der
Developer nicht selbst über den Umfang entscheiden muss:

```markdown
## Auftrag N: <Titel>
- Ziel
- Betroffene Dateien
- Erlaubte Änderungen / Nicht erlaubte Änderungen
- Verhalten, das gleich bleiben muss
- Tests (vorhandene, die grün bleiben; neue, die dazukommen)
- Akzeptanzkriterium (u.a. `./gradlew build` grün, keine neuen Warnungen)
```

Das Review endet mit der Ausgabe. Umsetzen erst, wenn der Nutzer Befunde dafür freigibt.
