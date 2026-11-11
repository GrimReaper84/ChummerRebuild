---
name: javafx-planner
description: Erstellt im Chummer-Rebuild aus C#-Analyse, Java-Analyse und SR4-Grundregelwerk einen umsetzungsreifen Feature-Plan (docs/plaene/<feature>_plan.md) mit Zielarchitektur, JavaFX-UI, Testplan, kleinen Umsetzungsschritten und den Scope-Entscheidungen des Nutzers. Verwende diesen Skill, sobald ein Feature geplant werden soll – nach den Analyseschritten, bei "plan das mal", "wie würdest du X umsetzen", beim Abgleich eines Tabs gegen die alten Designer.cs-Dateien oder wenn ein bestehender Plan überarbeitet werden soll. Schreibt keinen Produktivcode.
---

# Feature-Planung

Du übersetzt gesichertes Fachverhalten in einen Plan, den `java-developer` ohne erneute
Interpretation des C#-Codes umsetzen kann. Der wichtigste Teil der Arbeit sind die **Entscheidungen**:
Was gehört in diesen Feature-Schnitt, was wird zurückgestellt, wo weicht der Rebuild bewusst vom
Original ab? Der Nutzer trifft diese Entscheidungen, du bereitest sie mit Empfehlungen vor.

**Zuerst lesen:** `.claude/skills/_gemeinsam/projektregeln.md`. Wichtig sind vor allem Abschnitt 4
(GRW), 5 (Scope), 7 (Bausteine) und 8 (UI).

## Eingaben

| Eingabe | Quelle | Wenn sie fehlt |
|---|---|---|
| C#-Verhalten | verschlankt: selbst nach `csharp-legacy-analyzer` erheben, direkt in Abschnitt 4; sonst `docs/analysen/<feature>_analyse.md` | Bei reinen UI-Retrofits oder Features ohne C#-Vorbild entbehrlich |
| Java-Ist | verschlankt: selbst nach `java-analyzer` erheben, direkt in Abschnitt 5; sonst `docs/analysen/<feature>_java_analyse.md` | Knapp halten, wenn der Java-Stand aus der Sitzung bekannt ist |
| Designentscheidungen | `DECISIONS.md` | bindende frühere Entscheidungen mit Begründung; Widersprüche dem Nutzer vorlegen |
| GRW | OCR-Text + PDF (Projektregeln Abschnitt 4) | — |
| Alte UI | passender TabPage-Abschnitt in `frmCreate.Designer.cs`/`frmCareer.Designer.cs` | nur bei UI-Anteil |

## Vorgehen

1. **Grundlagen erheben bzw. lesen.** Im verschlankten Modus (Standard, Projektregeln Abschnitt 2)
   C#-Verhalten und Java-Ist nach den Vorgehen der beiden Analyse-Skills selbst ermitteln und direkt
   in die Abschnitte 4 und 5 des Plans schreiben; sonst beide Analysen vollständig lesen. Dazu
   `DECISIONS.md` auf einschlägige Einträge prüfen. Offene Fragen und "Nicht übernehmen ohne
   Prüfung"-Punkte sammeln.
2. **Jede Fachregel gegen das GRW prüfen**, auch wenn die C#-Analyse sie als gesichert führt. Für jede
   Regel festhalten: GRW-Seite + Kurzzitat, oder "kein GRW-Beleg". Rechenbeispiele aus dem GRW notieren,
   sie werden später Tests.
3. **Scope-Schnitt vorschlagen.** GRW-Kern zuerst, kleiner verifizierter Katalog, keine Halbfunktionen
   (Projektregeln Abschnitt 5). Für jeden Punkt, bei dem vernünftige Menschen unterschiedlich
   entscheiden könnten, eine Empfehlung mit Begründung formulieren.
4. **Entscheidungen einholen.** Die echten Scope- und Architekturfragen dem Nutzer per
   AskUserQuestion vorlegen (Empfehlung als erste Option, max. 4 Fragen pro Runde). Ist der Nutzer
   nicht erreichbar oder bittet darum, den Plan ohne Rückfragen fertigzustellen, die Empfehlungen
   einsetzen und in Abschnitt 13 deutlich als **unbestätigt** markieren.
5. **Bausteine zuordnen.** Für jede neue Komponente das Vorbild aus Projektregeln Abschnitt 7 nennen.
   Neue Infrastruktur nur, wenn kein Vorbild passt, und dann ausdrücklich als "einzige neue
   Infrastruktur" herausstellen.
6. **UI planen.** Bei neuen Tabs zuerst die alte Designer.cs-Struktur extrahieren (Controls,
   Gruppierung, Reihenfolge) und festhalten, was übernommen, verbessert oder bewusst weggelassen wird.
   Sichtbarkeitsregeln (Tab ausblenden vs. Zeile deaktivieren) und Erstellungs- vs. Karrieremodus
   explizit planen.
7. **Plan schreiben** nach der Vorlage unten, speichern unter `docs/plaene/<feature>_plan.md`.
8. **Anhalten und berichten:** Kurzfazit, getroffene und offene Entscheidungen, Hinweis, dass die
   Umsetzung eine eigene Freigabe braucht.

## Plan-Vorlage

Die Nummerierung ist fest, weil `java-developer` und spätere Sitzungen gezielt auf "Abschnitt 11"
und "Abschnitt 13" verweisen. Abschnitte, die nicht zutreffen, kurz mit Begründung füllen, nicht
weglassen.

```markdown
# Umsetzungsplan: <Feature> (Scope: <Kurzbeschreibung des Schnitts>)

> Grundlage, Stand, Entscheidungsstatus (bestätigt am … / unbestätigt), bewusst nicht Teil des Plans.

## 1. Kurzfazit
## 2. Eingangsquellen            (Tabelle: Quelle | Status | Relevanz)
## 3. Fachliche Zielsetzung
## 4. Gesichertes Altverhalten   (je Regel: Beschreibung, GRW-Seite + Zitat oder "kein GRW-Beleg",
                                   C#-Altlasten, die NICHT übernommen werden)
## 5. Bestehende Java-Struktur   (Tabelle: Baustein | Rolle als Vorbild)
## 6. Zielarchitektur            (Package-Baum + Tabelle: Komponente | Package | Verantwortung |
                                   Abhängigkeiten | Javadoc | Tests)
## 7. JavaFX-Planung             (Tab/Dialog, Designer.cs-Abgleich, Sichtbarkeit, Modi, Refresh-Pfade;
                                   oder "Keine JavaFX-Anpassung erforderlich.")
## 8. Plattformcheck             (nur echte Risiken; Standardfall: Classpath-Ressourcen, UTF-8)
## 9. Javadoc-Plan               (welche Regelbelege wohin; im Javadoc Seite + eigene Worte, kein Wortlaut)
## 10. Testplan                  (Tabelle: Test | Ebene | Zweck | Grundlage – GRW-Beispiele zuerst)
## 11. Umsetzungsschritte für den Coding-Agenten
## 12. Akzeptanzkriterien
## 13. Offene Fragen und Annahmen (Tabelle: Typ | Beschreibung | Auswirkung;
                                   Typ = Entscheidung (bestätigt/unbestätigt) | Annahme | Offene Frage | Risiko)
```

Format je Schritt in Abschnitt 11:

```text
Schritt N: <Name>
Ziel: <Was soll erreicht werden?>
Änderungen: <Dateien/Klassen, neu oder geändert>
Vorgaben: <Vorbild-Baustein, Regeln, zu verifizierende GRW-Werte>
Tests: <konkrete Testklassen und Fälle>
Akzeptanzkriterium: <prüfbar>
Risiken: <was schiefgehen kann>
```

Schritte klein halten: jeder soll für sich kompilieren und testbar sein. Typische Reihenfolge:
Enums/Hilfstypen → Katalog + Loader → Katalogdaten → Instanzklasse → Fachlogik in `Character` →
Persistenz → UI → Sonderfälle (z.B. Quality-Effekte).

## Qualitätsmaßstab

Ein guter Plan lässt `java-developer` nur noch zwei Dinge offen: technische Details, die sich aus dem
Code ergeben, und ausdrücklich markierte GRW-Nachprüfungen ("exakten Wert vor dem Einbau am PDF-Bild
verifizieren"). Nicht umsetzbar ist ein Plan, bei dem der Developer fachlich raten müsste. Gleiches gilt
für einen Plan, der Architektur vorschreibt, die am echten Code nicht hält. Deshalb vor dem Festlegen
kurz in die betroffenen Klassen schauen, etwa ob ein Feld `final` ist oder eine Methode bereits
existiert.
