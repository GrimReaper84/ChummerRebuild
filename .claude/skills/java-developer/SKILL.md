---
name: java-developer
description: Setzt einen freigegebenen Umsetzungsplan (docs/plaene/<feature>_plan.md) im Chummer-Rebuild in Java/JavaFX-Code um – Modell, XML-Daten, Persistenz, UI und Tests – und verifiziert per ./gradlew build. Verwende diesen Skill immer dann, wenn der Nutzer nach einem fertigen Plan ausdrücklich die Umsetzung freigibt ("leg los", "setz das um", "implementier Schritt 3") oder konkrete Codeänderungen an Features, Review-Befunden oder Bugfixes in diesem Projekt beauftragt. Nicht für reine Analyse oder Planung verwenden.
---

# Java-Umsetzung

Du setzt einen vom Nutzer freigegebenen Plan in Code um. Der Plan ist die führende Grundlage: er
enthält bereits die Scope-Entscheidungen des Nutzers, und die sollen nicht stillschweigend neu
verhandelt werden.

**Zuerst lesen:** `.claude/skills/_gemeinsam/projektregeln.md` (Workflow, GRW-Prüfung, Konventionen,
bewährte Bausteine, Verifikation). Die dort beschriebenen Regeln gelten hier vollständig.

## Voraussetzung

Umsetzen nur mit ausdrücklichem Auftrag des Nutzers. Liegt nur ein Plan vor, aber kein "leg los",
nicht anfangen, sondern nachfragen.

## Eingaben

| Eingabe | Quelle |
|---|---|
| Plan (führend) | `docs/plaene/<feature>_plan.md` — v.a. Abschnitt 11 (Umsetzungsschritte), 12 (Akzeptanzkriterien), 13 (Nutzerentscheidungen) |
| Hintergrund | Plan-Abschnitte 4/5; ältere Features: `docs/analysen/<feature>_analyse.md`, `..._java_analyse.md` (auch im Archiv) |
| Designentscheidungen | `DECISIONS.md` |
| Auftrag | Umfang laut Nutzer (alle Schritte oder einzelne) |

Bei Review-Befunden statt eines Plans gelten die Umsetzungsaufträge des `java-code-quality-reviewer`
(erlaubte/nicht erlaubte Änderungen, Verhaltenserhalt) als Plan.

## Vorgehen

1. **Plan vollständig lesen**, inklusive Abschnitt 13. Die Nutzerentscheidungen dort sind bindend.
2. **Betroffenen Bestand lesen**, bevor du schreibst: die im Plan genannten Vorbildklassen, `Character`
   (relevante Abschnitte), `CharacterViewController` (Abschnitt des Nachbar-Tabs), `CharacterStore`,
   bestehende Tests des Bereichs. Ziel ist, im Stil des Bestands weiterzuschreiben, nicht eine neue
   Architektur einzuführen.
3. **GRW-Vorbehalte aus dem Plan einlösen.** Viele Pläne sagen "exakten Wert vor dem Einbau gegen das
   GRW verifizieren" (Katalogwerte, Namen, BP-Kosten). Das jetzt tun, mit Bildprüfung bei Tabellen
   und Namen (siehe Projektregeln Abschnitt 4). Weicht das GRW vom Plan ab, gilt das GRW. Die
   Abweichung wird berichtet.
4. **Schrittweise umsetzen** in der Reihenfolge des Plans: Datenstrukturen → Fachlogik → Persistenz →
   UI. Nach größeren Schritten kompilieren (`./gradlew compileJava -q`), um Fehler früh zu fangen.
5. **Tests laut Testplan schreiben.** GRW-Rechenbeispiele exakt nachrechnen, Grenzfälle abdecken,
   Tests für die Fachlogik mit synthetischen Testdaten (Records direkt erzeugen), Loader-Tests gegen
   die echten XML-Dateien.
6. **Verifizieren:** `./gradlew build`. Testanzahl und Ergebnis aus `build/test-results` ermitteln. Die
   App nicht starten.
   Im **verschlankten Modus** (Standard, Projektregeln Abschnitt 2) laufen alle Schritte des Plans am
   Stück: nach jedem Schritt `./gradlew build`, kein Halt dazwischen, solange der Build grün ist und
   keine Nutzerentscheidung berührt wird.
7. **Nacharbeiten** laut Projektregeln Abschnitt 10 (`docs/feature_checklist.md`, `DECISIONS.md`,
   fertigen Plan archivieren).
8. **Abschluss-Review im frischen Kontext**, einmal pro Feature (nicht pro Schritt). Nach grünem
   Build einen eigenen Agenten (Agent-Tool, `general-purpose`, ohne Kontext dieser Sitzung) starten, der den Skill `java-code-quality-reviewer`
   im **Diff-Modus** auf die Änderungen dieser Umsetzung anwendet. Der Auftrag an den Agenten nennt:
   den Plan (`docs/plaene/<feature>_plan.md`), den Basis-Commit bzw. "uncommittete Änderungen", die
   Aufforderung, zuerst `.claude/skills/_gemeinsam/projektregeln.md` und die Reviewer-SKILL.md zu lesen,
   und dass er **nichts ändern** darf, sondern nur berichtet. Grund: Wer den Code gerade geschrieben
   hat, stellt seine eigenen Designentscheidungen kaum infrage; ein frischer Kontext ist kritischer.
   Die Befunde werden **nicht** eigenmächtig umgesetzt, sondern im Abschlussbericht vorgelegt; der
   Nutzer gibt sie frei. **Entfällt**, wenn der Auftrag selbst die Umsetzung von Review-Befunden war
   (sonst Endlosschleife) oder der Nutzer ausdrücklich darauf verzichtet.

## Umgang mit Planabweichungen

Pläne enthalten manchmal Annahmen, die sich am echten Code als unhaltbar erweisen. Beispiel: Ein Plan
wollte MAG/RES per Setter reduzieren, das Maximum war aber `final`, und die Mutation hätte BP-Kosten
verfälscht. Dann gilt:

- Kleinste Abweichung wählen, die das **fachliche Ziel** des Plans erreicht.
- Im Code (Javadoc) und im Abschlussbericht begründen.
- Betrifft die Abweichung eine **Nutzerentscheidung** aus Abschnitt 13 oder den fachlichen Umfang,
  vorher nachfragen statt selbst zu entscheiden.
- Zusätzliche GRW-Regeln, die beim Umsetzen auffallen und klar belegt sind (z.B. "nur Standard-/
  Alphaware bei Erschaffung"), dürfen mit umgesetzt werden, wenn sie klein sind. Sie werden im Bericht
  hervorgehoben.

## Nicht tun

- Keine Refactorings außerhalb des Features, keine globalen Umformatierungen, keine neuen Dependencies
  ohne Plan.
- Keine Tests löschen oder abschwächen.
- Nicht committen, außer der Nutzer bittet darum.
- Bestehende Datenwerte (z.B. in `qualities.xml`) nur ändern, wenn das GRW das belegt. Die Folgen für
  alte Speicherdateien im Bericht nennen.

## Abschlussbericht

Kurz und für den Nutzer lesbar (Deutsch), in dieser Reihenfolge:

1. **Ergebnis in einem Satz** und Build-Status (Tests gesamt/neu, Fehlschläge).
2. **Was jetzt funktioniert:** fachlich formuliert, mit GRW-Seiten der umgesetzten Regeln.
3. **Abweichungen vom Plan** mit Begründung (neue GRW-Funde, technische Zwänge, umbenannte
   Daten und deren Folgen). "Keine", wenn keine.
4. **Offene Punkte:** was bewusst nicht gemacht wurde oder noch geklärt werden muss.
5. **Abschluss-Review (Schritt 8):** die Befunde des Review-Agenten knapp mit Schweregrad, jeweils mit
   eigener Einschätzung (zustimmen/widersprechen, kurz begründet), und die Frage, welche umgesetzt
   werden sollen.
6. Hinweis, dass nicht committet wurde und die UI nur kompiliert, nicht angeschaut wurde.

Keine Dateiliste mit jeder geänderten Datei, außer der Nutzer fragt danach. Die wichtigen neuen
Klassen dürfen im Fließtext genannt werden.
