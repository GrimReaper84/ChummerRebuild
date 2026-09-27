---
name: java-analyzer
description: Ermittelt im Chummer-Rebuild den Ist-Stand des Java-Codes für ein Feature – was schon existiert (auch unerwartet), welche Bausteine wiederverwendbar sind, was fehlt – gleicht ihn mit der C#-Analyse ab und schreibt docs/analysen/<feature>_java_analyse.md. Verwende diesen Skill als zweiten Workflow-Schritt nach der C#-Analyse oder immer dann, wenn vor einer Planung geklärt werden soll, wie weit ein Feature im Java-Projekt schon umgesetzt ist ("was haben wir davon schon", "ist X schon drin"). Ändert keinen Code.
---

# Java-Ist-Analyse

Du stellst fest, was im Java-Rebuild für ein Feature **tatsächlich** vorhanden ist, und bereitest es so
auf, dass `javafx-planner` darauf aufbauen kann.

Warum dieser Schritt wichtig ist: `docs/feature_checklist.md` und die Memory hinken dem Code
regelmäßig hinterher. Mehrfach galten Dinge als offen, die längst umgesetzt und getestet waren, z.B.
die Sonderattribute oder die RES-Freischaltung. Deshalb zählt nur der Code, nicht die Doku.

**Zuerst lesen:** `.claude/skills/_gemeinsam/projektregeln.md`, vor allem Abschnitt 3 (Pfade) und 7
(bewährte Bausteine).

## Eingaben

| Eingabe | Quelle |
|---|---|
| C#-Analyse | `docs/analysen/<feature>_analyse.md`, vor allem der Übergabeblock. Fehlt sie, trotzdem analysieren und die fehlende Vergleichsbasis vermerken |
| Java-Code | `src/main/java/de/shalorian/chummer/...`, Tests unter `src/test/...`, Daten unter `src/main/resources/.../data/` |

## Vorgehen

1. **Top-down und bottom-up suchen:** vom Einstieg (`Character`, `CharacterViewController`,
   `CharacterStore`) zu den Fachklassen, und umgekehrt per Grep nach Fachbegriffen (deutsch *und*
   englisch), XML-Tags, `QualityBonusType`-Werten und Testnamen. Fehlanzeige erst nach beiden Richtungen
   melden.
2. **Pro relevanter C#-Erkenntnis einen Status vergeben** (Tabelle unten). Die Kernfrage ist, ob Java
   dieselbe *fachliche* Verantwortung erfüllt, nicht ob eine Klasse ähnlich heißt.
3. **Wiederverwendbare Bausteine benennen:** Welche vorhandenen Klassen sind das Vorbild (Projektregeln
   Abschnitt 7), und wo passen sie *nicht* ganz? Beispiel: Ein Feld ist `final`, ein Dialog hat keinen
   Platz für eine Zusatzauswahl. Genau diese Stellen werden später Planungsentscheidungen.
4. **Tests nach abgesichertem Verhalten bewerten**, nicht nur nach Existenz.
5. **Doku-Drift festhalten:** Stimmt `docs/feature_checklist.md` nicht mit dem Code überein, das
   benennen. Die Korrektur der Checkliste gehört zum Ergebnis dazu.
6. **Datei schreiben:** `docs/analysen/<feature>_java_analyse.md`, dann anhalten und kurz berichten.
   Wichtigster Fund zuerst, besonders wenn schon mehr existiert als erwartet.

Bei kleinem Umfang oder wenn der Java-Stand aus der laufenden Sitzung schon bekannt ist, reicht die
Kompaktform (Abschnitte 1, 2, 5, 6 unten).

## Status je C#-Erkenntnis

| Status | Bedeutung |
|---|---|
| Vorhanden | fachlich abgedeckt (Beleg: Klasse/Methode/Test) |
| Teilweise | Ansätze da, Umfang fehlt |
| Abweichend | vorhanden, verhält sich relevant anders (bewusst? GRW-konform?) |
| Nicht vorhanden | gezielt gesucht, nichts gefunden (Suchbegriffe nennen) |
| Nicht relevant | C#-Verhalten ist Altlast oder Zusatzband |

## Ausgabe: `docs/analysen/<feature>_java_analyse.md`

```markdown
# Java-Analyse: <Feature>

## 1. Kurzfazit
(Umsetzungsgrad in einem Satz; überraschende Funde zuerst)

## 2. Wiederverwendbare Bausteine
(je Baustein: Klasse, wofür sie Vorbild ist, wo sie nicht passt)

## 3. Relevante Java-Komponenten
(Tabelle: Komponente | Pfad | Verantwortung | Tests)

## 4. Abgleich mit C#-Analyse
(Tabelle: C#-Erkenntnis | Java-Entsprechung | Status | Planungsimplikation)

## 5. Was fehlt
(nummeriert, fachlich formuliert, mit betroffenen Stellen, z.B. `Character.getEssence()` Platzhalter)

## 6. Übergabe an javafx-planner
- Gesicherte Ausgangslage
- Zu nutzende Bausteine
- Zu implementieren / anzupassen
- Zu entscheiden (fachlich/technisch), mit den Stellen im Code, die die Entscheidung beeinflussen
- Doku-Drift (Checkliste/Memory falsch)
```

Hier keinen Code und keine fertigen Klassenentwürfe schreiben. Hinweise auf Signaturen oder
Strukturen sind nur so weit erlaubt, wie der Planer sie für eine Entscheidung braucht.
