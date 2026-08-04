# Feature-Checkliste: Chummer4 → Java-Rebuild

> Abgleich zwischen dem originalen C#-Verhalten (`chummer4_analyse.md`) und dem aktuellen Java-Implementierungsstand.
>
> **Legende:**
> - ✅ Vollständig und korrekt umgesetzt
> - ⚠️ Teilweise umgesetzt — konkrete Lücken oder Fehler dokumentiert
> - ❌ Nicht umgesetzt

---

## 1. Charaktererstellung (Grundgerüst)

| Feature                                                                                                                         | Status | Anmerkung                                                                                       |
|---------------------------------------------------------------------------------------------------------------------------------|--------|-------------------------------------------------------------------------------------------------|
| Neuer Charakter (Name + Metatyp)                                                                                                | ✅      | Dialog vollständig                                                                              |
| BP-Budget = 400                                                                                                                 | ✅      | Konstante korrekt                                                                               |
| BP-Restanzeige mit Farbwechsel bei Überschreitung                                                                               | ✅      |                                                                                                 |
| Nuyen-Budget in der Erstellung (max. 50 BP × 5.000 ¥)                                                                           | ✅      | `nuyenBP`-Feld in `Character`, UI im Erstellungsmodus, korrekte Konvertierung bei Finalisierung |
| Charakter-Biografie (Alias, Geschlecht, Alter, Augen, Größe, Gewicht, Beschreibung, Hintergrund, Konzept, Notizen, Spielername) | ❌      | Nur `name` vorhanden                                                                            |
| Mugshot / Portrait                                                                                                              | ❌      |                                                                                                 |

---

## 2. Metatypen

| Feature                                                | Status | Anmerkung                                                               |
|--------------------------------------------------------|--------|-------------------------------------------------------------------------|
| 5 Grundrassen (Mensch, Elf, Zwerg, Ork, Troll)         | ✅      |                                                                         |
| Metavarianten                                          | ✅      | Qualities je Metatyp in XML hinterlegt; Anzahl Varianten noch zu prüfen |
| Metatyp-BP-Kosten fließen in BP-Bilanz                 | ✅      | Alle 5 Grundrassen (0/20/25/30/40) gegen GRW-Tabelle S. 105 verifiziert, 2026-08-04 |
| Rassen-Attributspannen korrekt (alle 5 Grundrassen)    | ✅      | Zwerg BOD (2-7) und Ork INT (1-6) am 2026-08-04 korrigiert, Rest stimmte bereits |
| Bewegungsrate (Walking/Running)                        | ❌      | Nicht gespeichert, nicht angezeigt                                      |
| Metatyp-Kategorie (Metahuman, Shapeshifter, Critter …) | ❌      | Nicht modelliert                                                        |

---

## 3. Attribute

| Feature                                                                     | Status | Anmerkung                                      |
|-----------------------------------------------------------------------------|--------|------------------------------------------------|
| 8 Grundattribute (BOD, AGI, REA, STR, CHA, INT, LOG, WIL) mit Rassengrenzen | ✅      |                                                |
| BP-Kosten Grundattribute: (Wert − Min) × 10                                 | ✅      |                                                |
| +15 BP Bonus wenn Attribut auf Rassenmaximum                                | ✅      | Korrigiert in `CharacterAttribute.getBpCost()` |
| Attribut-Minimum durch Rassengrenzen erzwingen                              | ✅      |                                                |
| Karma-Steigerung Attribute: neue Stufe × 5 Karma                            | ✅      |                                                |
| Max. 50% des Gesamtbudgets für Attribute (Erstellung)                       | ✅      | GRW S. 106 verifiziert, `Character.canRaiseAttributeAtCreation()`, 2026-08-04 |
| Nur 1 Attribut auf Rassenmaximum steigerbar (Erstellung)                    | ✅      | GRW S. 106 verifiziert, s.o., 2026-08-04       |
| Augmentiertes Maximum anzeigen (MetatypeMax + ⌊Max/2⌋)                      | ❌      | Weder berechnet noch angezeigt                 |
| Sonderattribute: EDG (Edge)                                                 | ❌      | Nicht modelliert                               |
| Sonderattribute: MAG (Magie)                                                | ❌      | Nicht modelliert                               |
| Sonderattribute: RES (Resonanz)                                             | ❌      | Nicht modelliert                               |
| Abgeleitetes Attribut: INI (Initiative = INT + REA)                         | ❌      | Nicht modelliert                               |
| Abgeleitetes Attribut: ESS (Essenz = 6 − Implantate)                        | ❌      | Nicht modelliert                               |

---

## 4. Fertigkeiten (Aktive Skills)

| Feature                                                          | Status | Anmerkung                                                                                    |
|------------------------------------------------------------------|--------|----------------------------------------------------------------------------------------------|
| Fertigkeiten mit Gruppen-Kopplung                                | ✅      |                                                                                              |
| Gruppen-Entkopplung mit Kostenvergleich-Dialog                   | ✅      | Nur im Karrieremodus möglich (GRW S. 109), 2026-08-04                                        |
| BP-Kosten Einzelfertigkeit: Rating × 4                           | ✅      |                                                                                              |
| BP-Kosten Gruppe: Rating × 10                                    | ✅      |                                                                                              |
| Erste Karmapunkt einer neuen Fertigkeit kostet 4 Karma (nicht 2) | ✅      | Korrigiert in `Skill.getKarmaCost()`                                                         |
| Kategorie-Anzeige im UI                                          | ✅      | Gruppiert nach Kategorie sichtbar                                                            |
| Uneducated (Technical Active doppelt teuer)                      | ✅      | `Character.isSkillCostDoubled()`, `Skill.getBpCost()`/`getKarmaCost()` nehmen Doppel-Flag    |
| Uncouth (Social Active doppelt teuer)                            | ✅      | s.o.                                                                                         |
| Infirm (Physical Active doppelt teuer)                           | ✅      | s.o.                                                                                         |
| Erstellungs-Cap: 1×6 ODER 2×5, Rest max. 4                       | ✅      | GRW S. 109 verifiziert, `Character.canRaiseSkillAtCreation()`, 2026-08-04                    |
| Erstellungs-Cap: Fertigkeitsgruppen max. 4                       | ✅      | GRW S. 109 verifiziert, `Character.canRaiseGroupAtCreation()`, 2026-08-04                    |
| Karrieremodus: neue Fertigkeitsgruppe kostet pauschal 10 Karma   | ✅      | `Character.getGroupKarmaCost()`, 2026-08-04                                                  |
| Spezialisierungen (+2 BP / +2 Karma)                             | ❌      | Nicht modelliert                                                                             |
| Default-Flag (erlaubt Würfeln ohne Rating: Attribut − 1)         | ❌      | Flag im Modell fehlt, keine Würfelpool-Anzeige                                               |
| Würfelpool-Anzeige (Rating + Attribut + Modifikatoren)           | ❌      | Nicht vorhanden                                                                              |
| Exotische Fertigkeiten (Spez = Gegenstand, kein eigener Rating)  | ❌      |                                                                                              |

---

## 5. Wissensfertigkeiten (Knowledge Skills)

| Feature                                                       | Status | Anmerkung        |
|---------------------------------------------------------------|--------|------------------|
| Wissensfertigkeiten-Pool ((INT + LOG) × 3 freie Punkte)       | ✅      | Korrigierte Formel (nicht INT×2, siehe `docs/wissensfertigkeiten_analyse.md`) |
| Hinzufügen/Entfernen von Wissensfertigkeiten                  | ✅      | Freitext-Name, Dialog mit Kategorie-Auswahl, Duplikat-Prüfung |
| Kategorie (Akademisch, Hobby, Beruflich, Straße + Sprache)    | ✅      | `KnowledgeSkillCategory`-Enum, GRW-Terminologie: INTEREST→HOBBY umbenannt, 2026-08-04 (kein Migrationspfad für alte Speicherdateien) |
| BP-Kosten: 2 BP/Punkt (nur wenn Pool überschritten)           | ✅      | Gemeinsamer Pool über alle Wissensfertigkeiten, Uneducated verdoppelt Poolverbrauch |
| Karrieremodus-Karma-Kosten                                    | ✅      | Neue Fertigkeit 2 Karma, Steigerung Rating×1 Karma; Uneducated verdoppelt Endpreis |
| Muttersprache ("M"), separates Konzept außerhalb des Pools    | ✅      | Am 2026-08-04 korrigiert: gegen GRW S. 109/110 verifiziert — Symbol ist "M" nicht "N", eigenes `nativeLanguage`-Flag statt Rating-0-Eintrag, kostet keine Wissensfertigkeitspunkte |
| Erstellungs-Cap: 1×6 ODER 2×5, Rest max. 4                    | ✅      | GRW S. 155 verifiziert, `Character.canRaiseKnowledgeSkillAtCreation()`, 2026-08-04 |
| Obergrenze für Zusatzpunkte (max. Pool-Verdopplung)           | ✅      | GRW S. 110 verifiziert, dieselbe Methode wie oben, 2026-08-04 |
| Spezialisierungen bei Wissensfertigkeiten                     | ❌      | Bewusst ausgeklammert, gemeinsam mit Spezialisierungen aktiver Fertigkeiten geplant |

---

## 6. Qualities (Gaben & Handicaps)

| Feature                                                             | Status | Anmerkung                                                                   |
|---------------------------------------------------------------------|--------|-----------------------------------------------------------------------------|
| Positive Qualities (Gaben) hinzufügen und entfernen                 | ✅      | Dialog mit Kategoriefilter, Suchfeld und Gratis-Override (GM)               |
| Negative Qualities (Handicaps) hinzufügen und entfernen             | ✅      |                                                                             |
| BP-Kosten/-Gewinn fließen in BP-Bilanz                              | ✅      |                                                                             |
| Limit: max. 35 BP Gaben / 35 BP Handicaps (nur Charaktererstellung) | ✅      | Limit deaktiviert im Karrieremodus; Gratis-Qualities zählen nicht zum Limit |
| Metatyp-Qualities (kursiv/grau, nicht entfernbar)                   | ✅      | Werden aus Metatyp-XML geladen, nicht in JSON gespeichert                   |
| Bonus-Flags (Uneducated, Uncouth, Infirm)                           | ✅      | Flags gesetzt und Skill-Kostenverdopplung umgesetzt                         |
| Karrieremodus: Karma-Kosten für Gabe-Erwerb / Handicap-Entfernung   | ✅      | GRW verifiziert (doppelte GP-Kosten), `Character.getQualityKarmaCost()`, 2026-08-04 |

---

## 7. Kontakte

| Feature                                                   | Status | Anmerkung        |
|-----------------------------------------------------------|--------|------------------|
| Kontakt hinzufügen (Name, Loyalität, Verbindung)          | ❌      | Komplett fehlend |
| BP-Kosten: 1 BP pro Kontaktpunkt (Loyalität + Verbindung) | ❌      | GRW S. 113 bestätigt: "Kosten = Connectionstufe + Loyalitätsstufe" |
| ~~Freie Kontaktpunkte: CHA × 2~~                          | ❌      | **Widerlegt** (2026-08-04): Im GRW-Erstellungskapitel zu Connections (S. 113) wird Charisma kein einziges Mal erwähnt — Kontakte werden ganz normal aus dem allgemeinen 400-GP-Budget bezahlt, kein eigener CHA-basierter Freibetrag |

---

## 8. Magie

| Feature                                       | Status | Anmerkung                                       |
|-----------------------------------------------|--------|-------------------------------------------------|
| MAG-Attribut aktivieren (via Quality)         | ❌      | Abhängig von fehlenden Qualities & MAG-Attribut |
| Magier-Tab (Tradition, Zauber)                | ❌      |                                                 |
| Adept-Tab (Adeptenkräfte, Power Points = MAG) | ❌      |                                                 |
| Zauber hinzufügen (3 BP/Zauber)               | ❌      |                                                 |
| Adeptenkräfte mit PP-Kosten                   | ❌      |                                                 |
| Initiationsgrade                              | ❌      |                                                 |

---

## 9. Technomancer

| Feature                                    | Status | Anmerkung |
|--------------------------------------------|--------|-----------|
| RES-Attribut aktivieren (via Quality)      | ❌      | GRW S. 106 bestätigt |
| Technomancer-Tab (Stream, Komplexe Formen) | ❌      |           |
| ~~Komplexe Formen (1 BP/Stück)~~ Komplexe Formen (1 GP/Rating-Punkt) | ❌ | **Formel korrigiert** (2026-08-04): GRW S. 113 sagt "1 GP pro Stufenpunkt" (Beispiel: Panzerung 4 = 4 GP), kein Fixpreis pro Form — strukturell wie Wissensfertigkeiten. Zusatzregel: max. Anzahl bei Erstellung = Logik × 2 |
| Submersiongrade                            | ❌      | **Niedrige Priorität** (2026-08-04): Stichwortsuche im kompletten GRW-Text (72734 Zeilen) ergibt 0 Treffer für "Submersion" — vermutlich Inhalt des Sourcebooks *Unwired*, nicht des Grundregelwerks. Zurückstellen bis Zusatzquelle verfügbar (analog zum Qualities-Vervollständigungs-Problem) |

---

## 10. Cyberware / Bioware

| Feature                                                       | Status | Anmerkung        |
|---------------------------------------------------------------|--------|------------------|
| Cyberware kaufen (Nuyen-Kosten)                               | ❌      | Komplett fehlend |
| Essenz-Kosten berechnen (Cyberware vs. Bioware getrennt)      | ❌      |                  |
| Essenz-Berechnung: höhere Kategorie voll, niedrigere halbiert | ❌      |                  |
| Grades (Standard, Alphaware, Betaware, Deltaware)             | ❌      |                  |
| Subsystem-Kapazität                                           | ❌      |                  |
| Augmented Maximum durch Implantate (Augmented Modifier)       | ❌      |                  |

---

## 11. Rüstung

| Feature                                       | Status | Anmerkung        |
|-----------------------------------------------|--------|------------------|
| Rüstung kaufen und ausrüsten                  | ❌      | Komplett fehlend |
| Ballistik/Impact-Berechnung (Stacking-Regeln) | ❌      |                  |
| Tragebelastung (Armor Encumbrance)            | ❌      |                  |

---

## 12. Waffen

| Feature                     | Status | Anmerkung        |
|-----------------------------|--------|------------------|
| Waffen kaufen und verwalten | ❌      | Komplett fehlend |
| Modifikationen, Zubehör     | ❌      |                  |

---

## 13. Ausrüstung (Gear)

| Feature                      | Status | Anmerkung        |
|------------------------------|--------|------------------|
| Allgemeine Ausrüstung kaufen | ❌      | Komplett fehlend |
| Commlink mit Response/Signal | ❌      |                  |
| Orte/Lager-Kategorien        | ❌      |                  |

---

## 14. Fahrzeuge

| Feature                        | Status | Anmerkung        |
|--------------------------------|--------|------------------|
| Fahrzeuge kaufen und verwalten | ❌      | Komplett fehlend |

---

## 15. Karrieremodus

| Feature                                                               | Status | Anmerkung                                                     |
|-----------------------------------------------------------------------|--------|---------------------------------------------------------------|
| Übergang Creation → Career (explizite Nuyen-Konvertierung)            | ✅      | Nur zugewiesene nuyenBP (max. 50) werden in Nuyen umgerechnet |
| ESS-Penalty-Korrektur bei Übergang (MAG/RES += EssenzPenalty)         | ❌      | Abhängig von fehlenden Sonderattributen                       |
| Karma-Ausgaben für Attribute (neue Stufe × 5 Karma)                   | ✅      |                                                               |
| Karma-Ausgaben für Fertigkeiten (neue Stufe × 2, erste Stufe 4 Karma) | ✅      | Korrigiert in `Skill.getKarmaCost()`                          |
| Karma-Ausgaben für Qualities (2× GP-Kosten, Gabe/Handicap)            | ✅      | `Character.getQualityKarmaCost()`, 2026-08-04                 |
| Karma/Nuyen-Log mit Datum                                             | ✅      |                                                               |
| Undo für Log-Einträge                                                 | ❌      |                                                               |
| Expense-Typen (z.B. ImproveAttribute, AddQuality …)                   | ❌      | Alle Log-Einträge sind freie Texte, keine Typisierung         |

---

## 16. Initiativberechnung

| Feature                                            | Status | Anmerkung |
|----------------------------------------------------|--------|-----------|
| Physische Initiative = INT + REA (+ Modifikatoren) | ❌      | GRW S. 172 bestätigt Basis (Initiativattribut), aber Initiativeergebnis = Attribut + Würfelprobe-Erfolge, keine reine Summe — bei Umsetzung präzisieren |
| Astrale Initiative = INT × 2                       | ❌      | GRW S. ~229 exakt bestätigt |
| Matrix-Initiative (mit Commlink oder Technomancer) | ❌      | GRW S. ~263 deutlich granularer als geplant: 2 Sim-Modi (Kalt/Heiß) mit unterschiedlicher Formel (Prozessor+INT bzw. +1) und unterschiedlicher Initiative-Passes-Zahl (2 bzw. 3) |
| Initiative Passes anzeigen                         | ❌      | Normal = 1, Matrix Kalt Sim = 2, Heiß Sim = 3 (GRW bestätigt) |

---

## 17. Condition Monitor

| Feature                                    | Status | Anmerkung |
|--------------------------------------------|--------|-----------|
| Physischer CM = 8 + ⌈BOD/2⌉                | ❌      |           |
| Stun CM = 8 + ⌈WIL/2⌉                      | ❌      |           |
| CM-Boxen anklicken und füllen              | ❌      |           |
| Wundmodifikator berechnen (−1 pro 3 Boxen) | ❌      |           |

---

## 18. Persistenz (Speichern/Laden)

| Feature                                                    | Status | Anmerkung                                                                    |
|------------------------------------------------------------|--------|------------------------------------------------------------------------------|
| JSON-Speicherung und -Laden (Grundstruktur)                | ✅      |                                                                              |
| Attribute werden korrekt gespeichert/geladen               | ⚠️     | Nur 8 Grundattribute; EDG, MAG, RES fehlen im Modell                         |
| Skills werden korrekt gespeichert/geladen (nur Rating > 0) | ✅      |                                                                              |
| Entkoppelte Gruppen werden gespeichert/geladen             | ✅      |                                                                              |
| Qualities werden gespeichert/geladen (inkl. Gratis-Flag)   | ✅      | Metatyp-Qualities werden nicht gespeichert, sondern beim Laden rekonstruiert |
| Wissensfertigkeiten werden gespeichert/geladen             | ✅      | Freitext-Name, Kategorie, Rating (inkl. Sonderzeichen)                       |
| Karma/Nuyen-Log wird gespeichert/geladen mit Datum         | ✅      |                                                                              |
| Dateiendung `.json`                                        | ✅      |                                                                              |
| Vorgeschlagener Dateiname beim Speichern (charname.json)   | ✅      |                                                                              |

---

## 19. UI / Bedienbarkeit

| Feature                                                              | Status | Anmerkung |
|----------------------------------------------------------------------|--------|-----------|
| Tastaturkürzel (Ctrl+N/O/S/Shift+S/Q)                                | ✅      |           |
| Bestätigungsdialog bei Karma-Ausgaben                                | ✅      |           |
| Disabled-Zustand von Buttons korrekt (BP/Karma zu wenig, Grenzwerte) | ✅      |           |
| Metatyp-Wechsel mit Attribut-Reset in der Erstellung                 | ✅      |           |
| Drucken / Export / Charakterbogen                                    | ❌      | **Niedrige Priorität** (2026-08-04): keine Charakterbogen-Vorlage/Formular im GRW-Text gefunden — reines Tool-Feature ohne Regelwerk-Bezug |

---

## Zusammenfassung

| Bereich                | ✅ | ⚠️ | ❌ |
|------------------------|---|----|---|
| Erstellungsgrundgerüst | 4 | 0  | 2 |
| Metatypen              | 3 | 0  | 2 |
| Attribute              | 5 | 0  | 6 |
| Aktive Fertigkeiten    | 9 | 0  | 4 |
| Wissensfertigkeiten    | 6 | 0  | 1 |
| Qualities              | 6 | 0  | 0 |
| Kontakte               | 0 | 0  | 3 |
| Magie                  | 0 | 0  | 6 |
| Technomancer           | 0 | 0  | 4 |
| Cyberware/Bioware      | 0 | 0  | 6 |
| Rüstung                | 0 | 0  | 3 |
| Waffen                 | 0 | 0  | 2 |
| Gear                   | 0 | 0  | 3 |
| Fahrzeuge              | 0 | 0  | 1 |
| Karrieremodus          | 4 | 0  | 3 |
| Initiative             | 0 | 0  | 4 |
| Condition Monitor      | 0 | 0  | 4 |
| Persistenz             | 8 | 1  | 0 |
| UI/Bedienbarkeit       | 4 | 0  | 1 |

---

## Prioritisierte Reihenfolge für nächste Schritte

> **Regelwerk-Abgleich (2026-08-04):** Die komplette Roadmap wurde gegen das SR4-Grundregelwerk geprüft,
> bevor weitere Features umgesetzt werden — siehe `docs/regelwerk_abgleich_stufe2_roadmap.md` und
> `docs/regelwerk_abgleich_stufe3_roadmap.md`. Ergebnis: fast alles ist solide belegt, bis auf zwei
> Punkte ohne GRW-Beleg (s.u., auf Niedrig gesetzt) und zwei korrigierte Annahmen (Kontakte, Komplexe
> Formen, siehe jeweilige Abschnitte oben).

**Stufe 1 – Fehlende Kernfeatures für einen vollständigen Charakter**

1. ~~**Wissensfertigkeiten**~~ ✅ umgesetzt (freier Pool (INT+LOG)×3, Kategorien, Überzug in BP, Karrieremodus-Karma)
2. **Sonderattribute** EDG, MAG, RES + Essenz-Berechnung (Grundlage für alles weitere) — GRW S. 106/107 vollständig bestätigt
3. **Spezialisierungen** (+2 BP / Karma) — inkl. Wissensfertigkeiten-Spezialisierungen (bewusst zurückgestellt bei Schritt 1); GRW bestätigt
4. **Kontakte** — GRW bestätigt, aber "CHA×2 freie Punkte"-Annahme widerlegt (s. Abschnitt 7 oben), Formel vor Umsetzung anpassen
5. **Charakter-Biografie** (alle Textfelder) — kein Regelwerk-Bezug, reine Textfelder

**Stufe 2 – Erweiterte Features**

1. Magie (MAG-Aktivierung, Tradition, Zauber, Adeptenkräfte) — GRW vollständig bestätigt
2. Technomancer (RES-Aktivierung, Stream, Komplexe Formen) — RES + Komplexe Formen bestätigt (Formel korrigiert, s. Abschnitt 9); **Submersionsgrade auf Niedrig-Priorität**, kein GRW-Beleg
3. Cyberware/Bioware mit Essenz — GRW bestätigt (Essenz-Split, Grades)
4. Rüstung mit Stacking-Regeln — GRW bestätigt (Addition mehrerer Rüstungsteile + Tragebelastungsformel)
5. Waffen — Kapitel vorhanden, Details bei Umsetzung gezielt nachschlagen
6. Gear — Verfügbarkeitsregeln bestätigt, Kommlink-Details bei Umsetzung gezielt nachschlagen

**Stufe 3 – Abgeleitete Werte & Gameplay**

1. Würfelpool-Anzeige für Skills — GRW S. 82 exakt bestätigt, fundamentale Kernmechanik des gesamten Systems
2. Condition Monitor — GRW S. 94 exakt bestätigt (inkl. Wundmodifikator-Formel)
3. Initiative-Anzeige — GRW bestätigt, aber Formel bei Umsetzung präzisieren (Initiativattribut vs. vollständiges Würfelergebnis, s. Abschnitt 16)
4. Undo für Log-Einträge — reines Tool-Feature, kein Regelwerk-Bezug zu erwarten (Priorisierung unabhängig vom GRW-Abgleich)
5. **Drucken / Export — Niedrige Priorität**, keine Charakterbogen-Vorlage im GRW gefunden, reines Tool-Feature
