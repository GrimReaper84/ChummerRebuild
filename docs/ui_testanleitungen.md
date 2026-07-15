# UI-Testanleitungen

Sammlung von Klickanleitungen zum manuellen Durchtesten von UI-Änderungen. Es gibt
(noch) keine automatisierten UI-/Klick-Tests für diese JavaFX-App; diese Datei ist
die Ablage dafür, bis das nachgeholt wird.

**Regeln für Einträge in dieser Datei:**

- Jede Anleitung startet **immer** beim frisch gestarteten Programm (`./gradlew run`),
  nie mitten in einem angenommenen Zustand.
- Schritte sind einzeln nummeriert und nennen exakte Button-/Feld-/Tab-Beschriftungen,
  wie sie im UI stehen.
- Am Ende jeder Anleitung steht kurz, was das erwartete Ergebnis ist (worauf beim
  Durchklicken geachtet werden soll).
- Neue Anleitungen werden unten angehängt, ältere bleiben stehen (auch wenn der
  zugehörige Code sich später nochmal ändert) — sie dokumentieren, welcher Ablauf zu
  welcher Änderung gehörte.

---

## Anleitung 1: Dialog-Öffnen-Refactoring (Quality- und Log-Dialoge)

**Kontext:** `CharacterViewController#openQualitySelectDialog` und `#openLogEntryDialog`
teilen sich seit diesem Refactoring eine gemeinsame private Hilfsmethode `openDialog(...)`
für den FXML-/Stage-Aufbau. Diese Anleitung prüft, dass beide Dialoge sich weiterhin
identisch verhalten wie vor der Extraktion.

1. App starten: `./gradlew run`. Das Hauptfenster "Chummer – SR4 Charakter Manager"
   öffnet sich mit der Menüleiste (Datei-Menü).
2. Menü **Datei → Neu** (oder `Strg+N`). Der Dialog "Neuer Charakter" öffnet sich.
3. Im Feld **Name** einen beliebigen Namen eingeben, z. B. `Testperson`.
4. Im Dropdown **Metatyp** einen beliebigen Metatyp auswählen, z. B. `Mensch`.
5. Auf **Erstellen** klicken. Der Dialog schließt sich, das Hauptfenster zeigt jetzt
   die Charakteransicht mit den Tabs **Attribute**, **Skills**, **Gaben & Handicaps**,
   **Log**.
6. Tab **Gaben & Handicaps** öffnen.
7. Im Abschnitt "Gaben" auf **Hinzufügen** klicken.
   - **Erwartung:** Ein modaler Dialog mit Titel "Quality hinzufügen" öffnet sich,
     mit Kategorie-Dropdown, Suchfeld, Liste der Qualities, Kosten-Anzeige,
     Checkboxen "Nur wählbare anzeigen" / "Gratis (GM-Override)" sowie den Buttons
     "Abbrechen", "OK & Weiteres", "OK". Das Fenster ist mit dem Chummer-Stylesheet
     gestylt (nicht die unformatierte Standard-JavaFX-Optik).
8. Auf **Abbrechen** klicken. Der Dialog schließt sich ohne Änderung an der
   Gaben-Liste.
9. Schritt 7–8 im Abschnitt "Handicaps" wiederholen.
   - **Erwartung:** Gleiches Verhalten, gleicher Dialog (nur mit dem entsprechenden
     Kategorie-Filter für Handicaps).
10. Tab **Log** öffnen.
11. Im Abschnitt "Karma" auf **+ Eintrag** klicken.
    - **Erwartung:** Ein modaler Dialog mit Titel "Karma-Eintrag hinzufügen" öffnet
      sich, mit Feldern **Datum**, **Typ** (Einnahme +/Ausgabe −), **Menge**,
      **Bezeichnung** sowie Buttons "Abbrechen"/"Hinzufügen". Auch hier: gestylt,
      nicht die JavaFX-Standardoptik.
12. Menge `5`, Bezeichnung `Testeintrag` eingeben, auf **Hinzufügen** klicken.
    - **Erwartung:** Dialog schließt sich, in der Karma-Tabelle erscheint ein neuer
      Eintrag mit heutigem Datum, "Testeintrag", "+5". Der Karma-Gesamtwert oben
      rechts sowie in der Statusleiste unten erhöht sich um 5.
13. Im Abschnitt "Nuyen" auf **+ Eintrag** klicken.
    - **Erwartung:** Gleicher Dialogaufbau, aber Titel "Nuyen-Eintrag hinzufügen".
14. Beliebigen Eintrag hinzufügen oder mit **Abbrechen** schließen.

**Was insgesamt zu prüfen ist:** Beide Dialogtypen (Quality-Auswahl, Log-Eintrag)
öffnen sich weiterhin mit korrektem Titel, angewendetem Stylesheet, modalem Verhalten
(Hauptfenster ist währenddessen nicht bedienbar) und funktionieren nach "Abbrechen"
sowie nach echtem Hinzufügen wie gewohnt. Keine Exception/Stacktrace in der Konsole,
in der `./gradlew run` läuft.

---

## Anleitung 2: MigLayout-Umstellung Attribute-/Skills-Tab + Creation/Career-Verzweigung

**Kontext:** `attributeGrid` (Attribute-Tab) und die pro Kategorie erzeugten Skill-Grids
(Skills-Tab) laufen seit diesem Refactoring über `org.tbee.javafx.scene.layout.MigPane`
statt `GridPane`, mit `hidemode 3` (unsichtbare Komponenten nehmen keinen Platz im Grid
ein). Gleichzeitig wurde die bislang in `addAttributeRow`, `addGroupHeaderRow` und
`addSkillRow` dreifach duplizierte Creation/Career-Verzweigungslogik (Refresh, +/−-Handler,
BP-/Karma-Kostenprüfung) in eine gemeinsame Methode `wireIncrementable` extrahiert. Diese
Anleitung prüft, dass optisch nichts kaputt ist und dass `hidemode 3` den Minus-Button im
Karrieremodus wirklich lückenlos entfernt (der eigentliche Grund für den Wechsel zu
MigLayout).

1. App starten: `./gradlew run`. Das Hauptfenster "Chummer – SR4 Charakter Manager"
   öffnet sich.
2. Menü **Datei → Neu** (oder `Strg+N`). Dialog "Neuer Charakter" öffnet sich.
3. Name `Testperson`, Metatyp `Mensch`, auf **Erstellen** klicken.
4. Tab **Attribute** öffnen.
   - **Erwartung:** Acht Attributzeilen (BOD, AGI, REA, STR, CHA, INT, LOG, WIL,
     jeweils mit deutschem Anzeigenamen), jede mit Spalten Attribut / Kürzel / `−` /
     Wert / `+` / Bereich / BP-Kosten. Spaltenbreiten und Ausrichtung sehen aus wie
     vorher (Name linksbündig breit, Kürzel/Wert/Bereich/BP-Spalten kompakt,
     zentrierte Zahlen bei Wert).
5. Bei einem Attribut mehrfach auf **+** klicken, bis der BP-Verbrauch oben rechts
   sichtbar steigt.
   - **Erwartung:** Wert steigt, BP-Kosten-Spalte aktualisiert sich, **−**-Button wird
     aktiv sobald Wert über dem Rassenminimum liegt.
6. Tab **Skills** öffnen, eine Fertigkeit erhöhen.
   - **Erwartung:** Gleiches Verhalten wie bei Attributen; bei gekoppelten
     Skillgruppen ist stattdessen die Gruppenzeile bedienbar, die einzelnen
     Skill-Zeilen der Gruppe zeigen nur den aktuellen Rating-Wert ohne aktive
     Buttons.
7. Eine Skillgruppe **auskoppeln** (Button "Auskoppeln" in der Gruppenkopfzeile).
   - **Erwartung:** Bestätigungsdialog mit Kosteninfo (falls Rating > 0), nach
     Bestätigen sind die einzelnen Skills der Gruppe unabhängig voneinander mit
     eigenem BP-Preis bedienbar.
8. Zurück im Hauptfenster: **Charakter abschließen** klicken, im Bestätigungsdialog
   mit OK bestätigen. Die Ansicht wechselt in den Karrieremodus (Metatyp wird nur noch
   als Text angezeigt, kein BP-Bereich mehr sichtbar).
9. Tab **Attribute** erneut ansehen.
   - **Erwartung (Kernpunkt von `hidemode 3`):** In jeder Zeile fehlt der
     **−**-Button komplett — und zwar **ohne Lücke**: der Wert steht direkt nach dem
     Attributkürzel, nicht mit sichtbarem Leerraum an der Stelle, wo vorher der
     Minus-Button war. Rechts steht statt "BP-Kosten" jetzt "Karma" mit den
     Karma-Kosten für den nächsten Schritt.
10. Tab **Skills** erneut ansehen, für eine ungruppierte Fertigkeit dasselbe prüfen
    wie in Schritt 9 (kein Minus-Button, keine Lücke).
11. Bei einem Attribut oder Skill auf **+** klicken (genug Karma vorausgesetzt).
    - **Erwartung:** Bestätigungsdialog "Karma ausgeben" mit korrektem Text
      (`<Name> <alt> → <neu>`), nach Bestätigen sinkt der Karma-Gesamtwert um die
      angezeigten Kosten, Wert erhöht sich.

**Was insgesamt zu prüfen ist:** Optisch keine Regression gegenüber dem
GridPane-Stand (Spaltenbreiten/Ausrichtung wie gewohnt), und im Karrieremodus lässt
`hidemode 3` den ausgeblendeten Minus-Button tatsächlich keine Lücke — das war der
Hauptgrund für den Wechsel zu MigLayout und lässt sich nur visuell prüfen. Keine
Exception/Stacktrace in der Konsole.
