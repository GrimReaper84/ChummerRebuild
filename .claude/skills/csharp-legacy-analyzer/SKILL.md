---
name: csharp-legacy-analyzer
description: Analysiert im Chummer-Rebuild das Verhalten des alten C#-Chummer (cls*.cs, frm*.cs, Daten-XMLs) für ein Feature – Fachregeln, Datenflüsse, Sonderfälle, Altlasten – gleicht jede Regel mit dem SR4-Grundregelwerk ab und schreibt eine Übergabe für die Planung nach docs/analysen/<feature>_analyse.md. Verwende diesen Skill als ersten Schritt, wenn ein Feature aus dem Original migriert werden soll ("wie macht das alte Chummer X", "analysier mal das Cyberware-System", "nächstes Feature angehen"), auch wenn der Nutzer den Skill nicht ausdrücklich nennt. Schreibt keinen Java-Code.
---

# C#-Legacy-Analyse

Du rekonstruierst, was das alte Chummer bei einem Feature **tatsächlich tut**, und ordnest es gegen
das SR4-Grundregelwerk ein. Das Ergebnis ist eine Wissensbasis für `java-analyzer` und
`javafx-planner`. Übersetzt, refaktoriert oder geplant wird hier noch nichts.

Warum der GRW-Abgleich schon hier stattfindet: Das Original enthält Bugs, Hausregeln und viel Inhalt
aus Zusatzbänden. Wenn die Analyse Code-Verhalten und Regelwerk sauber trennt, muss der Planer nicht
mehr raten, was davon fachlich verbindlich ist.

**Zuerst lesen:** `.claude/skills/_gemeinsam/projektregeln.md`, vor allem Abschnitt 3 (Pfade),
4 (GRW-Prüfung) und 5 (Scope).

## Eingaben

- Feature-Name bzw. Frage des Nutzers.
- C#-Code unter `/home/luca/Chummer4_OriginalCode/chummer/Chummer/`. Die Kernlogik steckt meist in
  `clsCharacter.cs`, `clsEquipment.cs`, `clsUnique.cs`, `clsImprovement.cs` sowie in `frmCreate.cs`/
  `frmCareer.cs` (Erstellungs- vs. Karrieremodus) und den `frmSelect*.cs`-Dialogen.
- Daten unter `.../bin/Release/data/*.xml` (Feld `<source>` zeigt, aus welchem Buch ein Eintrag stammt).
- GRW: `docs/rulebooks/Grundregelwerk_SR4_ocr.txt` (+ PDF für Bildprüfung).

## Vorgehen

1. **Einstiegspunkte finden:** UI-Aktion (Button-Handler in `frm*.cs`) → Fachmethoden → Datenzugriff.
   Mit Grep nach Klassen- und Feldnamen suchen, dann gezielt die relevanten Abschnitte lesen. Große
   Dateien wie `frmCreate.cs` (>20.000 Zeilen) nie komplett lesen.
2. **Ist-Verhalten beschreiben**, nicht vermutete Absicht. Wirkt Code falsch, das beobachtete Verhalten
   dokumentieren und als Risiko markieren. Nicht stillschweigend korrigieren.
3. **Modusunterschiede erfassen:** Verhält sich die Logik in `frmCreate` (BP) anders als in `frmCareer`
   (Karma/Nuyen)? Solche Sonderfälle sind häufig und migrationsrelevant.
4. **Datenumfang vermessen:** Wie viele Katalogeinträge gibt es, wie viele davon mit `source=SR4`? Diese
   Zahl hilft dem Planer beim Scope-Schnitt.
5. **Jede Fachregel gegen das GRW prüfen.** Mögliche Ergebnisse: bestätigt (mit Seite + Zitat),
   widerlegt (GRW sagt etwas anderes), nur im C# (kein GRW-Beleg → Hinweis auf Zusatzband oder
   Hausregel) oder unklar. Bei 0 Treffern die deutsche Übersetzung des Begriffs suchen, bevor "kein
   Beleg" gefolgert wird.
6. **Datei schreiben:** `docs/analysen/<feature>_analyse.md`, dann anhalten und dem Nutzer die
   wichtigsten Befunde, Widersprüche C# vs. GRW und offenen Fragen kurz berichten.

Bei großem Umfang (viele Klassen, >1.500 Zeilen relevanter Code) bietet es sich an, die Analyse an
einen Fork-Agenten zu geben. Der Hauptkontext bleibt dann frei.

## Einstufungen

Jede Aussage bekommt eine Einstufung, damit der Planer weiß, worauf er sich verlassen kann:

| Label | Bedeutung |
|---|---|
| `Gesichert (Code+GRW)` | Im Code belegt und vom GRW bestätigt, die stärkste Stufe |
| `Gesichert (Code)` | Im Code belegt, GRW-Bezug fehlt oder nicht geprüft |
| `Wahrscheinlich` | plausibel, nicht vollständig belegbar |
| `Unklar` | Kontext fehlt oder mehrdeutig |
| `Altlast` | technisch fragwürdig (Duplikation, String-Vergleiche statt Flags …), nicht 1:1 übernehmen |
| `Nicht übernehmen ohne Prüfung` | möglicher Bug, Hausregel oder Workaround |

Quellen immer konkret angeben: `clsEquipment.cs:2076-2198`, `GRW S. 111`.

## Worauf bei diesem Code besonders achten

- Formelfelder in XML (`Rating * 0.75`, `FixedValues(...)`), die per XPath ausgewertet werden
- Namensbasierte Sonderbehandlung (`if (name == "Essence Hole")`)
- Improvement-System (`clsImprovement.cs`): Qualities und Ausrüstung wirken indirekt über
  `ImprovementType`-Einträge; der Effekt steht oft weit entfernt von der Definition
- Doppelte oder mehrfach berechnete Summen, `decimal`- vs. `int`-Rundung, Options-Schalter in
  `clsOptions.cs` (Hausregeln, die standardmäßig aus sind)
- UI-Sichtbarkeitsumschaltung in Mehrzweck-Controls (`SkillControl`, `ContactControl`)

## Ausgabe: `docs/analysen/<feature>_analyse.md`

```markdown
# C#-Legacy-Analyse: <Feature>

> Umfang, Datum, was bewusst nicht analysiert wurde.

## Projektüberblick
(relevante Dateien als Tabelle: Datei:Zeilen | Rolle | Einstufung)

## Extrahierte Fachregeln
### Regel: <Name>
**Status:** <Einstufung>
**Beschreibung:** Wenn …, dann …
**Quelle:** Code-Stelle(n); GRW S. … "<Zitat>" bzw. "kein GRW-Beleg (gesucht: …)"
**Migrationsrelevanz:** …
**Testszenarien:** …

## Datenmodell(e)
(Felder als Tabelle: Feld | Typ | Bedeutung | migrationsrelevant?)

## Offene Fragen
(je Frage: Warum relevant, bisherige Beobachtung, empfohlene nächste Suche)

# Übergabe an javafx-planner
- Gesichertes Ist-Verhalten / zwingend zu übernehmen
- Fachregeln-Tabelle: Regel | Status | Quelle | Priorität
- Widersprüche C# ↔ GRW
- Nur im C#/Zusatzband (Vorschlag: zurückstellen)
- Altlasten, die nicht übernommen werden sollten
- Abgeleitete Testszenarien (GRW-Rechenbeispiele zuerst)
- Risiken für die Neuimplementierung
```

Die Übergabe am Ende ist das, was der Planer tatsächlich braucht. Sie muss auch ohne den Rest der
Datei verständlich sein.
