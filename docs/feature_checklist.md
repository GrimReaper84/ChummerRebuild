# Feature-Checkliste: Chummer4 → Java-Rebuild

> Abgleich zwischen dem originalen C#-Verhalten (`analysen/chummer4_analyse.md`) und dem aktuellen Java-Implementierungsstand.
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
| Nuyen-Budget in der Erstellung (max. 50 BP × 5.000 ¥)                                                                           | ✅      | `getResourceNuyen/getSpentNuyen/getRemainingNuyen()` (GRW S. 110/111): Cyberware, Rüstung und Lebensstil zählen gegen das Budget, Kopfzeile "Ressourcen" + "Übrig" (rot bei < 0), Abschluss mit Restgeld < 0 gesperrt (2026-09-27) |
| Charakter-Biografie (Alias, Geschlecht, Alter, Augen, Größe, Gewicht, Haut, Haare, Beschreibung, Hintergrund, Konzept, Notizen, Spielername, Spielnotizen) | ✅      | `CharacterBiography` (Value-Object, 15 Freitextfelder), eigener Tab "Biografie" (vor "Attribute"), Live-Text-Binding über `bindTextControl()`, Persistenz via `BiographyData`-DTO; Spielnotizen nur im Karrieremodus sichtbar |
| Mugshot / Portrait | ✅ | 2026-11-15 (ADR-059): Biografie, auf 512 px verkleinert, als PNG im Charakter gespeichert |

---

## 2. Metatypen

| Feature                                                | Status | Anmerkung                                                               |
|--------------------------------------------------------|--------|-------------------------------------------------------------------------|
| 5 Grundrassen (Mensch, Elf, Zwerg, Ork, Troll)         | ✅      |                                                                         |
| Metavarianten                                          | ❌      | Entfernt 2026-11-12: nicht im GRW (Runner-Kompendium), samt ihrer 5 RC-Qualities; `<variant>` bleibt im Loader |
| Metatyp-BP-Kosten fließen in BP-Bilanz                 | ✅      | Alle 5 Grundrassen (0/20/25/30/40) gegen GRW-Tabelle S. 105 verifiziert, 2026-08-04 |
| Rassen-Attributspannen korrekt (alle 5 Grundrassen)    | ✅      | Zwerg BOD (2-7) und Ork INT (1-6) am 2026-08-04 korrigiert, Rest stimmte bereits |
| Bewegungsrate (Walking/Running) | ✅ | 2026-11-15 (ADR-059): Gehen/Laufen je Metatyp (S. 177) und Sprinten, seit ADR-065 im Bereich „Cheat-Sheet“ |
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
| Augmentiertes Maximum anzeigen (⌊1,5 × Max⌋)                                | ✅      | GRW S. 91, `Character.getAugmentedMaximum()`, Klammerwert im Bereich, 2026-11-12 |
| Sonderattribute: EDG (Edge)                                                 | ✅      | `SpecialAttribute`/`SpecialAttributeType`, BP/Karma-Kosten, UI-Sektion, Persistenz; immer sichtbar |
| Edgepunkte ausgeben/zurückgewinnen/auffrischen, Edge verheizen | ✅ | 2026-11-15 (ADR-057): im Status-Panel, nur Karrieremodus, gespeichert; Verheizen senkt das Attribut dauerhaft, auch unter das Metatyp-Minimum bis 0 (S. 98/99), Rückkauf über normale Steigerung |
| Sonderattribute: MAG (Magie)                                                | ✅      | Wie EDG, aber nur sichtbar/freigeschaltet wenn Quality mit `GRANTS_MAGIC` vorhanden (`Character.hasMagic()`) |
| Sonderattribute: RES (Resonanz)                                             | ✅      | Analog MAG, über `GRANTS_RESONANCE` (`Character.hasResonance()`)                              |
| Abgeleitetes Attribut: INI (Initiative = INT + REA)                         | ✅      | Status-Panel, siehe Abschnitt 16               |
| Abgeleitetes Attribut: ESS (Essenz = 6 − Implantate)                        | ✅     | `Character.getEssence()` nach GRW-Kombinationsregel (S. 111), 2 Nachkommastellen, 2026-09-27 |

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
| Spezialisierungen (+2 BP / +2 Karma)                             | ✅      | `Skill.specialization`, `Character.getActiveSkillSpecializationBP()`/`getSpecializationKarmaCost()`, Mindestrating 1, Gruppenauflösung im Karrieremodus, 2026-08-06 |
| Default-Flag (erlaubt Würfeln ohne Rating: Attribut − 1)         | ✅      | `default="false"` an den 27 kursiven Fertigkeiten (GRW S. 146, am Bild); Ahnungslos durch Ungebildet/Ungehobelt/Körperliche Schwäche/Inkompetenz (S. 120/122), 2026-11-12 |
| Würfelpool-Anzeige (Rating + Attribut + Modifikatoren)           | ✅      | Spalte „Pool“ (GRW S. 146/147), Spezialisierung „8 (10)“, Verletzungsmodifikator eingerechnet, Tooltip mit Aufschlüsselung; auch Wissensfertigkeiten (ohne Wert Attribut − 1, nicht bei Sprachen, S. 146), 2026-11-12. Fertigkeits-Cap 1,5× (S. 91) zurückgestellt |
| Exotische Fertigkeiten (je Waffe bzw. Fahrzeug) | ✅ | 2026-11-15 (ADR-059): Instanz je Ziel, ohne Spezialisierung, Waffenpool nutzt die passende Instanz (S. 148, 155, 365) |
| Kampf-Aktionsfertigkeiten nach GRW                               | ✅      | **Korrigiert 2026-09-28** (GRW S. 147/148, am Bild geprüft): Gruppen Nahkampf (Klingenwaffen, Knüppel, Waffenloser Kampf) und Feuerwaffen (Gewehre, Pistolen, Schnellfeuerwaffen), dazu Ausweichen (REA), Exotische Fern-/Nahkampfwaffe, Projektilwaffen, Schwere Waffen, Wurfwaffen. Entfernt: Klingen, Automatikwaffen, Langwaffen, Schrotflinten (keine GRW-Fertigkeit), Bogenschießen, Gruppe "Schusswaffen". "Sprengstoff" → technische Fertigkeit "Sprengstoffe" (GRW S. 154). Alte Speicherdateien: Werte umbenannter Fertigkeiten werden beim Laden übersprungen |
| Übrige Fertigkeitsbereiche nach GRW (Körperlich, Sozial, Technisch, Fahrzeug, Resonanz) | ✅ | **Korrigiert 2026-10-09** (GRW S. 146–154, Tabellen und Kategorien am Bild geprüft): `skills.xml` enthält jetzt exakt die 73 Aktionsfertigkeiten der GRW-Tabelle "Fertigkeiten und damit verbundene Attribute" mit den 13 GRW-Gruppen (Athletik, Beschwören, Biotech, Cracken, Einfluss, Elektronik, Feuerwaffen, Heimlichkeit, Hexerei, Mechanik, Nahkampf, Natur, Tasken). Fahrzeugfertigkeiten ohne Gruppe (GRW: "Fertigkeitsgruppe: Keine"; alte Gruppe "Pilotieren" entfernt), Geschütze GES statt LOG. Neue Kategorie Resonanz (Gruppe Tasken, nur Technomancer, GRW S. 151). Abgesichert durch `SkillDataLoaderTest.matchesGrwSkillTableExactly`. Alte Speicherdateien: Werte umbenannter Fertigkeiten werden beim Laden übersprungen |

---

## 5. Wissensfertigkeiten (Knowledge Skills)

| Feature                                                       | Status | Anmerkung        |
|---------------------------------------------------------------|--------|------------------|
| Wissensfertigkeiten-Pool ((INT + LOG) × 3 freie Punkte)       | ✅      | Korrigierte Formel (nicht INT×2, siehe `docs/analysen/archiv/wissensfertigkeiten_analyse.md`) |
| Hinzufügen/Entfernen von Wissensfertigkeiten                  | ✅      | Dialog mit Kategorie und Vorschlagsliste (GRW S. 110/155/156 und Sprachen, ADR-059), Freitext möglich, Duplikat-Prüfung |
| Kategorie (Akademisch, Hobby, Beruflich, Straße + Sprache)    | ✅      | `KnowledgeSkillCategory`-Enum, GRW-Terminologie: INTEREST→HOBBY umbenannt, 2026-08-04 (kein Migrationspfad für alte Speicherdateien) |
| BP-Kosten: 2 BP/Punkt (nur wenn Pool überschritten)           | ✅      | Gemeinsamer Pool über alle Wissensfertigkeiten, Uneducated verdoppelt Poolverbrauch |
| Karrieremodus-Karma-Kosten                                    | ✅      | Neue Fertigkeit 2 Karma, Steigerung Rating×1 Karma; Uneducated verdoppelt Endpreis |
| Muttersprache ("M"), separates Konzept außerhalb des Pools    | ✅      | Am 2026-08-04 korrigiert: gegen GRW S. 109/110 verifiziert — Symbol ist "M" nicht "N", eigenes `nativeLanguage`-Flag statt Rating-0-Eintrag, kostet keine Wissensfertigkeitspunkte |
| Erstellungs-Cap: 1×6 ODER 2×5, Rest max. 4                    | ✅      | GRW S. 155 verifiziert, `Character.canRaiseKnowledgeSkillAtCreation()`, 2026-08-04 |
| Obergrenze für Zusatzpunkte (max. Pool-Verdopplung)           | ✅      | GRW S. 110 verifiziert, dieselbe Methode wie oben, 2026-08-04 |
| Spezialisierungen bei Wissensfertigkeiten                     | ✅      | +1 Poolpunkt (flach, nicht Uneducated-verdoppelt) bei Erstellung, 2 Karma im Karrieremodus; Mindestrating 1, 2026-08-06 |

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
| Vollständiger GRW-Katalog (32 Gaben, 23 Handicaps mit Stufen/Varianten, Seite + Kurzwirkung) | ✅ | **2026-11-12**, `qualities.xml` nach Tabelle S. 106 (Bild geprüft), Namen wie im GRW; Nicht-GRW-Gaben entfernt; alte Namen in Speicherdateien werden übersetzt (`QualityNameMigration`) |
| Auswahl je Quality (Fertigkeit, Attribut, Text), Voraussetzungen, Ausschlüsse, Metatyp-Bedingungen, eine Stufe je Gruppe | ✅ | `Character.getQualityBlockReason`/`getQualityChoiceProblem`, Dialog zeigt Grund und Kurzwirkung, Tab zeigt Auswahl in Klammern |
| Effekte: Außergewöhnliches Attribut, Glück, Talentiert (bis 7, letzter Schritt doppelt), Inkompetenz, Gruppensperren Ungebildet/Ungehobelt/Körperliche Schwäche, Abhängigkeit (Ausgebrannt) Essenz −1 | ✅ | 2026-11-12; seit dem Würfelpool-Feature auch Hohe/Niedrige Schmerztoleranz, Überlebenswille, Astralzugang (Zauberer). Situative Würfelpool- und Matrix-Wirkungen nur als Hinweis |

---

## 7. Kontakte

| Feature                                                   | Status | Anmerkung        |
|-----------------------------------------------------------|--------|------------------|
| Kontakt hinzufügen (Name, Kurzbeschreibung, Connection, Loyalty) | ✅ | `Contact`-Modell, eigener Tab, Add/Edit-Dialog (Beschreibung als Tooltip), 2026-08-07. Bewusst reduzierter Scope: kein Enemy/Pet-Unterschied, keine Gruppenkontakt-Modifikatoren (kein GRW-Beleg im Kernregelwerk, siehe `docs/analysen/archiv/kontakte_analyse.md`) |
| BP-Kosten: 1 BP pro Kontaktpunkt (Connection + Loyalty)   | ✅      | GRW S. 113 bestätigt: "Kosten = Connectionstufe + Loyalitätsstufe", `Character.getContactBP()` |
| ~~Freie Kontaktpunkte: CHA × 2~~                          | ✅      | **Widerlegt** (2026-08-04): Im GRW-Erstellungskapitel zu Connections (S. 113) wird Charisma kein einziges Mal erwähnt — Kontakte werden ganz normal aus dem allgemeinen 400-GP-Budget bezahlt, kein eigener CHA-basierter Freibetrag. Kein Freibetrag implementiert |
| Karrieremodus: Kontakte frei bearbeitbar, ohne Karma-Kosten | ✅ | GRW: Connections werden nicht mit Karma gekauft (sie müssen im Spiel erarbeitet werden), neue `wireFreeIncrementable()`-UI-Methode |

---

## 8. Magie

> Scope-Entscheidung (2026-08-08): Nur reine **Magier** umgesetzt (Tradition + Zauber-Erlernen).
> Adeptenkräfte und Magieradept-Aufteilung folgten am 2026-11-12 (ADR-046, `docs/plaene/archiv/adepten_plan.md`). Details:
> `docs/analysen/archiv/magie_analyse.md` (C#-Analyse), `docs/analysen/archiv/magie_java_analyse.md` (Java-Ist-Analyse),
> `docs/plaene/archiv/magie_plan.md` (Umsetzungsplan inkl. aller Entscheidungen).

| Feature                                                | Status | Anmerkung                                                                                                                                                                                                                                                            |
|---------------------------------------------------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Magier-Erkennung (feiner als reines MAG-Attribut)      | ✅      | Neuer `QualityBonusType.GRANTS_SPELLCASTING`, nur auf Quality "Magier" gesetzt — unterscheidet Magier von Adept (beide teilen weiterhin `GRANTS_MAGIC` fürs MAG-Attribut selbst), `Character.isMagician()`                                                        |
| Magie-Tab (Tradition, Zauber)                          | ✅      | Eigener Tab, dynamisch ein-/ausgeblendet (`TabPane.getTabs()`), sichtbar sobald Gabe "Magier" aktiv — funktioniert bereits im Erstellungsmodus, nicht erst im Karrieremodus                                                                                        |
| Tradition                                              | ✅      | Festes Enum `Tradition` (Hermetisch/Schamanistisch, GRW-Kern) mit Drain-Attributformel als Anzeigetext; freie Tradition-Erschaffung bewusst nicht umgesetzt                                                                                                         |
| Zauber-Katalog                                         | ✅      | Alle 84 GRW-Zauber (Strassengrimoire S. 238–249, am Bild geprüft 2026-11-12; Heilung 12, Illusion 15, Kampf 18, Manipulation 22, Wahrnehmung 17), Entzug als (K÷2)±x. Offen: Zauber mit Platzhalter ([Attribut], [Critter], [Lebensform], [Objekt]) brauchen eine Auswahl, damit mehrere Ausprägungen lernbar sind; Zusatzband-Zauber (Straßenmagie) bewusst nicht |
| Zauber hinzufügen (3 BP / 5 Karma)                     | ✅      | Fixpreis pro Zauber in beiden Modi, Karrieremodus mit Karma-Bestätigungsdialog                                                                                                                                                                                       |
| Zauber-Cap bei Erstellung (2×höchstes Spellcasting-Rating) | ✅  | `Character.canAddSpellAtCreation()`, kein Cap im Karrieremodus                                                                                                                                                                                                       |
| Geister (gebunden/herbeigerufen)                       | ✅      | 2026-11-12 (ADR-053): Abschnitt im Bereich Magie, 6 Geisterarten mit Werten (S. 347/348), fünf je Tradition (S. 213); Erschaffung 1 BP je Dienst, Kraft = Magie, Dienste ≤ Herbeirufen, gebundene ≤ Charisma (S. 112); Karriere nur eintragen; optionale Kräfte wählbar, Eintrag aufklappbar (ADR-068) |
| Schutzpatrone | ✅ | 2026-11-13 (ADR-055): 19 Patrone (S. 234–237, am Bild) als Auswahl der Gabe, beide Vorteile, „nach Wahl“ beim Kauf (Katze, Drachentöter), rechnende Nachteile; Rollenspiel-Nachteile als Text; Vorteile/Nachteile im Detail des Auswahldialogs |
| Situative Würfelpools | ✅ | 2026-11-13 (ADR-055): Foki (stärkster je Probe, S. 233), Patrone und sieben Adeptenkräfte (Kampfsinn, Magieresistenz, Natürliche Immunität, Beschleunigte Heilung, Großer Sprung, Körpersprache, Kritischer Schlag auf waffenlosen Schaden, sichtbar an der festen Zeile „Waffenloser Angriff“ im Waffen-Tab, S. 188) als Liste im Magie-/Adepten-Tab und Status |
| Magische Aktionsfertigkeiten (Spruchzauberei/Ritualzauberei) | ✅ | Neu in `skills.xml`, eigene `SkillCategory.MAGICAL`, immer im Skills-Tab sichtbar (für alle Charaktere), aber nur für Magier steigerbar — `+`-Button deaktiviert ohne Gabe "Magier" (`Character.canRaiseSkill()`, GRW S. 208), gilt in Erstellung **und** Karrieremodus |
| Magier/Adept/Mystischer-Adept-Exklusivität             | ✅      | `Character.addQuality()` verhindert gleichzeitige Wahl mehrerer `GRANTS_MAGIC`-Qualities                                                                                                                                                                            |
| Magier-Quality-Kosten (Bug-Fix)                        | ✅      | `qualities.xml` von 5 auf 15 BP korrigiert (GRW S. 118)                                                                                                                                                                                                              |
| Adept-Tab (Adeptenkräfte, Power Points = MAG)          | ✅      | 2026-11-12 (ADR-046): Tab „Adeptenkräfte“ nur für Adept/Magieradept, Kraftpunkte = wirksame Magie mit Bruchteilen, Warnung bei überzogenen Punkten |
| Adeptenkräfte mit PP-Kosten                            | ✅      | 19 GRW-Kräfte (S. 229–232), Auswahldialog; rechnend: Gesteigerte Reflexe, Gesteigertes körperliches Attribut, Verbesserte Fertigkeit, Verbesserte Wahrnehmung, Schmerzresistenz, Mystischer Panzer, Astrale Wahrnehmung. Kritischer Schlag im waffenlosen Schaden ✅ (ADR-055). Offen: Adrenalinkick als Rechnung, Todeskralle im waffenlosen Schaden, Geschärfte Sinne als Liste. Initiation/Metamagie ✅ (ADR-052) |
| Mystic-Adept-MAG-Split                                 | ✅      | Im Tab Adeptenkräfte (S. 229); Zauberpools mit Rest-Magie, Höchststufe nach voller Magie |
| Initiationsgrade                                       | ✅      | 2026-11-12 (ADR-052): Bereich „Initiation“, nur Karrieremodus (S. 112), 10 + Grad × 3 Karma, Grad ≤ wirksame Magie, Maximum 6 + Grad, sechs Metamagien (S. 232/233) mit Adepten-Beschränkung, Wirkung als Hinweis. Offen: Gruppeninitiation/Prüfungen (Zusatzband), Intensivierung als Karma-Verwaltung |
| Restliche magische Aktionsfertigkeiten (Antimagie, Beschwören-Gruppe, Arcana, Verzaubern, Askennen, Astralkampf) | ✅ | **Umgesetzt 2026-10-09** (GRW S. 148–150): Gruppen Hexerei (Antimagie, Ritualzauberei, Spruchzauberei) und Beschwören (Binden, Herbeirufen, Verbannen), dazu Arcana, Askennen, Astralkampf, Verzaubern. Zugang über `Character.isSkillAccessible()`/`canRaiseGroup()`: nur Magier, Ausnahme Arcana (auch Mundane, GRW S. 148). Beim Entfernen der Gabe Magier/Technomant: Ja/Nein-Abfrage, bestätigt werden die gesperrten Fertigkeiten samt Spezialisierung auf 0 gesetzt (`Character.getSkillsLockedByRemovingQuality()`). Seit 2026-11-12: Askennen/Astralkampf nur mit astraler Wahrnehmung (Zauberer oder Kraft Astrale Wahrnehmung, S. 108/149). Hexerei/Beschwören als Wissensfertigkeit für Nicht-Magier (GRW S. 109) nicht gesondert abgebildet |

---

## 9. Technomancer

| Feature                                    | Status | Anmerkung |
|--------------------------------------------|--------|-----------|
| RES-Attribut aktivieren (via Quality)      | ✅      | **Korrektur (2026-08-08):** bereits umgesetzt im Zuge des Sonderattribute-Features (analog zur EDG/MAG/RES-Korrektur vom 2026-08-06) — Quality "Technomant" (5 BP, `qualities.xml`), `QualityBonusType.GRANTS_RESONANCE`, `Character.hasResonance()`/`syncMagicOrResonance()`, Magie/Resonanz-Exklusivität, Persistenz. GRW S. 106 bestätigt. Checkliste war nicht mit dem Code synchron, siehe `docs/analysen/archiv/technomancer_java_analyse.md` |
| Technomancer-Tab (Schwund, Komplexe Formen) | ✅      | Eigener Tab, nur sichtbar mit Resonanz (`syncTechnomancerTabVisibility()`); Schwund-Widerstand als feste Anzeige "Willenskraft + Resonanz" — kein Stream-Auswahlkonzept, da das GRW-Kern keine Streams kennt (User-Entscheidung 2026-08-08, `docs/plaene/archiv/technomancer_plan.md`) |
| Komplexe Formen (1 GP/Stufenpunkt, Karma wie Wissensfertigkeit) | ✅ | `ComplexForm`/`ComplexFormDataLoader` (alle 21 GRW-Programme S. 271–273 inkl. Panzerung, Seiten am Bild geprüft 2026-11-12), 1 GP pro Stufenpunkt (GRW S. 112, Beispiel Panzerung 4 = 4 GP), Karma 2 neu / neue Stufe; Stufen-Cap = wirksamer Resonanzwert in Erstellung und Karriere (`canRaiseComplexForm()`), inkl. Tests |
| Anzahl-Cap Komplexe Formen bei Erstellung (Logik × 2) | ✅ | GRW S. 112, `Character.canAddComplexFormAtCreation()`, Hinweis im Technomancer-Tab, 2026-11-12 |
| Lebende Persona | ✅ | 2026-11-13 (ADR-054): Firewall WIL, Prozessor INT (+1 in VR), Signal RES ÷ 2 aufgerundet, System LOG, je ≤ RES; Biofeedback-Filter CHA (S. 279, am Bild) |
| Sprites (registriert/kompiliert)                       | ✅      | 2026-11-12 (ADR-053): Abschnitt im Bereich Technomancer, 5 Sprite-Arten mit Werten (S. 282); Erschaffung 1 BP je Dienst, Stufe = Resonanz, Dienste ≤ Kompilieren, registrierte ≤ Charisma (S. 113); Karriere nur eintragen; optionale Komplexe Formen wählbar (ADR-068) |
| Wandlung (Submersion)                      | ✅      | 2026-11-12 (ADR-052): gleicher Bereich, Titel „Wandlung“, fünf Echos (S. 284), Firewall-/System-Upgrade und Neurofilter bis 3×, Wirkung als Hinweis. Offen: Lebende Persona als Rechnung |

---

## 10. Cyberware / Bioware

| Feature                                                       | Status | Anmerkung        |
|---------------------------------------------------------------|--------|------------------|
| Cyberware kaufen (Nuyen-Kosten)                               | ✅      | Katalog vollständig nach GRW: 51 Cyberware, 25 Bioware (2026-11-12, ADR-048), dazu Cybergliedmaßen (ADR-051). Kosmetische Modifikation mit Preiseingabe, Wertlisten je Stufe (Reflexbooster, Basissysteme). Preis zählt bei Erschaffung gegen das Ressourcengeld, im Karrieremodus Abbuchung über das Nuyen-Log mit Bestätigung (2026-09-27) |
| Verfügbarkeit ≤ 12 bei Erschaffung                            | ✅      | GRW S. 111; `Availability` je Katalogeintrag, gesperrte Implantate und einzelne Stufen sichtbar-deaktiviert; Grades ohne Verfügbarkeitsaufschlag (GRW S. 359) |
| Essenz-Kosten berechnen (Cyberware vs. Bioware getrennt)      | ✅      | `Cyberware.getEssenceCost()` (`RatingFormula` × Grade), getrennte Summen am `Character` |
| Essenz-Berechnung: höhere Kategorie voll, niedrigere halbiert | ✅      | GRW S. 111 inkl. Rechenbeispiel als Test; Handicap "Immunabstoßung" (S. 120) verdoppelt Cyberware-Summe |
| Grades (Standard, Alphaware, Betaware, Deltaware)             | ✅      | GRW S. 359; bei Erschaffung nur Standard/Alpha wählbar |
| MAG/RES-Reduktion durch Essenzverlust (Wert + Maximum)        | ✅      | GRW S. 208, abgeleitet (`getEffectiveMagicOrResonanceValue/Max()`), investierter Wert bleibt Kostenbasis |
| Subsystem-Kapazität                                           | ✅      | Cyberaugen/Cyberohren als Basissysteme (Stufe × 4), Zubehör ohne Essenz mit Grade des Basissystems, einzeln implantiert mit Essenz; beides zugleich gesperrt (S. 386/387), 2026-11-12. Kapazität in Cybergliedmaßen ✅ (ADR-051) |
| Implantat-Wirkungen                                           | ✅      | Attributboni bis zum augmentierten Maximum (mit Adeptenkräften), Initiativedurchgänge, Panzerung ohne Behinderung, Poolboni (Athletik, sozial, körperlich, Wissen, Reflexrekorder), Schadenskompensator; situative Wirkungen als Hinweis (2026-11-12, ADR-048) |
| Unverträglichkeiten                                           | ✅      | Datengetrieben, im Dialog gesperrt mit Grund, in beide Richtungen; Gesteigerte Reflexe ↔ technische Initiativesteigerung (S. 230) |
| Kommlink-Headware                                             | ✅      | Kommlink-Modell aus dem Ausrüstungskatalog wird eingebaut, samt Betriebssystem (S. 386) |
| Cybergliedmaßen, Cyberwaffen                                  | ✅      | 2026-11-12 (ADR-051): 14 Glieder mit Stelle, An-/Abnehmen, Maßanfertigung, Verbesserungen, 5 Zubehörteile, 4 Cyber-Nahkampfwaffen, Head-/Bodyware mit Gliedkapazität, Durchschnitt über fünf Körperteile, Monitor +1 je Glied, Gliedpanzerung (Summe wie C#, per Einstellung Durchschnitt, ADR-061), Waffen mit Glied-STR. Offen: Cyberguns (GRW ohne Kosten), Schockhand-Ladungen |
| Datenbuchse, Kommlink, Sim-Modul, Simrig für VR | ✅ | 2026-11-13 (ADR-054): Neuralinterface bzw. Sim-Modul (kalt/heiß) als Implantat-Wirkung, zählen für den Matrixzugang (S. 258, 386) |
| Smartlink-Bonus auf den Waffenpool | ✅ | 2026-11-15 (ADR-056) |
| Talentleitungen mit Aktionssofts, Wissens- und Linguasofts | ✅ | 2026-11-15 (ADR-059): Schalter „geladen“, Leitungsstufe und Doppelte-Limit, Neuralinterface für Lingua-/Wissenssoft (S. 377, 389) |

---

## 11. Rüstung

| Feature                                       | Status | Anmerkung        |
|-----------------------------------------------|--------|------------------|
| Rüstung kaufen und ausrüsten                  | ✅      | Tab "Straßenausrüstung" → "Rüstung", 13 GRW-Einträge (S. 373), Anlegen/Ablegen, "Alle anlegen/ablegen", Verfügbarkeit ≤ 12 bei Erschaffung, Karriere-Kauf über Nuyen-Log (2026-09-27) |
| Ballistik/Impact-Berechnung (Stacking-Regeln) | ✅      | GRW S. 190/372: höchstes angelegtes Teil + Helm-/Schild-Boni |
| Tragebelastung (Armor Encumbrance)            | ✅      | GRW S. 190 inkl. Rechenbeispiel; reduziert GES/REA in der "(Aug)"-Spalte. Helme/Schilde zählen mit; Untergrenze 1 **vorläufig** (kein GRW-Beleg, Nutzerentscheidung) |
| Kleidung, Feedback-Kleidung, Lederjacke/-mantel | ✅      | 2026-11-12 (ADR-047): Kategorie „Kleidung“ (GRW S. 373), Kleidungspreis 20–100.000 ¥ beim Kauf eingegeben, Feedback-Kleidung/Lederjacke mit Aufschlag +500/+200 ¥ |
| Rüstungsmodifikationen, GKP-/UEO-Zubehör      | ✅      | 2026-11-12 (ADR-050): `armor_mods.xml` mit 4 Zubehörteilen und 6 Modifikationen (S. 373), Einbau-Dialog, Zuschläge zur Verfügbarkeit, Sonderschutz gegen Elektrizität/Feuer/Kälte, Toxinwiderstand durch Chemische Isolierung und Wärmedämpfung im Detail und Status |
| Schild-Malus −1 auf körperliche Proben        | ✅      | 2026-11-12: Fertigkeiten mit körperlichem Attribut und körperliche Attributproben (S. 372); Schilde und Schockstreifen als Waffe im Waffenbereich |
| Darstellung als Baum + Detailbereich          | ✅      | 2026-11-12: `ArmorTabBuilder` (Kategorie → Teil → Einbauten) wie Waffen (ADR-035) |

---

## 11a. Lebensstil

| Feature                                   | Status | Anmerkung |
|-------------------------------------------|--------|-----------|
| Lebensstil (6 GRW-Stufen, Monate im Voraus) | ✅      | GRW S. 111; Kosten zählen gegen das Ressourcengeld; Tab "Straßenausrüstung" → "Lebensstil" (2026-09-27) |
| Lebensstil-Ausbaustufen, weitere Lebensstile | ❌      | Zusatzband, zurückgestellt |
| Lebensstilzahlungen im Karrieremodus      | ✅      | 2026-11-15 (ADR-058): „Monat bezahlen“ und „Stufe wechseln“ (erster Monat) buchen Nuyen als Lebensstil (S. 310) |
| Versäumte Lebensstilzahlungen (GRW S. 312: W6 gegen ausgesetzte Monate, Abstieg, Schulden) | ✅ | 2026-11-18 (ADR-061): Regel-Einstellung je Charakter; „Monat aussetzen“ würfelt, Rückstand und Schulden, „Schulden tilgen“ |

---

## 12. Waffen

| Feature                     | Status | Anmerkung        |
|-----------------------------|--------|------------------|
| Waffen kaufen und verwalten | ✅      | Teil A umgesetzt 2026-09-28 (`docs/plaene/archiv/waffen_plan.md`): 63 GRW-Kern-Waffen (Tabellen S. 361/363/366, am Bild geprüft) mit Tabellenwerten als Text, Bogen mit Stufe 1–8 (Mindeststärke), Kauf/Entfernen, Nuyen-Budget, Verfügbarkeit ≤ 12 (sichtbar-deaktiviert), Karriere-Kauf über Nuyen-Log, Persistenz; Unter-Tab "Waffen" seit 2026-09-30 als Baum nach Kategorie + Detailbereich wie im Original (`docs/plaene/archiv/waffen_detailansicht_plan.md`; vom Nutzer angesehen und für gut befunden) |
| Berechnete Werte (STR-Schaden, Reichweiten, Fertigkeit) | ✅ | Teil B umgesetzt 2026-09-30: Schaden aus Stärke (GRW S. 188, STR÷2 aufgerundet; Bogen nach Mindeststärke S. 361), Reichweiten laut Tabelle S. 179 (am Bild geprüft; `<range>` je Waffe, Abweichungen laut Waffentext: Hämmerli 620S, Mossberg AM-CMDT, Spezielle Waffen S. 362/365), Fertigkeit mit Wert je Kategorie (S. 147/148, 360–366). Tabelle aktualisiert sich bei Attribut-/Fertigkeitsänderung. **Einschränkungen:** Exotische Waffen teilen sich je eine Fertigkeit (GRW: je Waffe eigene); Pfeilstufe nicht modelliert |
| Würfelpool je Waffe, Smartlink, Lasermarkierer | ✅ | 2026-11-15 (ADR-056): Pool der Waffenfertigkeit (S. 177/186), Spezialisierung „8 (10)“ (S. 147), Smartlink +2 mit Smartgunsystem, sonst Lasermarkierer +1, nicht kumulativ (S. 180); jede Waffe, auch eingebaute |
| Geladene Munition | ✅ | 2026-11-15 (ADR-056): eine Sorte je Waffe aus passender Munition der Ausrüstung; Schaden, PB und Panzerung nach Tabelle S. 369 (Gel, Schocker, Flechette-Ausnahme bei (f)); Leuchtspur nur Vollautomatik, Salvenbonus als Hinweis; Munition für feste Waffen nicht ladbar |
| Zubehör mit Halterungen     | ✅      | Teil C umgesetzt 2026-10-04: 16 GRW-Zubehörteile (Tabelle S. 368, am Bild geprüft) an Halterungen Lauf/Unterlauf/Aufmontiert je Waffenklasse (S. 357), eine pro Halterung, Zubehör ohne Halterung mehrfach; Sonderregeln laut Zubehörtexten S. 367/368 (Gasventil nur Automatikpistolen/MPs/Sturmgewehre/MGs und nicht entfernbar, Schalldämpfer EM/HM ohne Revolver, Schallunterdrücker SM/AM, Schockpolster und Gyrostabilisator nur Gewehre/schwere Waffen, Airburst nur Granat-/Raketenwerfer, Schnelllader nur Revolver, Ersatzladestreifen nur Ladestreifen-Waffen, Projektilwaffen nur Lasermarkierer/Smartgun); mitgeliefertes Zubehör laut Waffentexten S. 362–366 (`<included>`), höchstens ein Gasventil/Smartgunsystem/Schockpolster je Waffe; RK "x (y)" inkl. Zubehör (Gasventil fest, alles andere im Klammerwert); internes Smartgunsystem kostet den Waffenpreis; Budget, Karriere-Kauf, Persistenz. **Einschränkungen:** Spezielle Waffen ohne Halterung (kein GRW-Beleg); Airburst verlangt kein Smartgunsystem; HK-XM30-Umbausätze zurückgestellt (nur `<note>`) |
| Waffenmodifikationen        | ❌      | Nur Arsenal, zurückgestellt |
| Munition, Pfeile/Bolzen, Granaten/Raketen | ✅ | Im Gear-Feature umgesetzt (Gear Teil D, 2026-10-08), siehe Abschnitt 13 |

---

## 13. Ausrüstung (Gear)

| Feature                      | Status | Anmerkung        |
|------------------------------|--------|------------------|
| Allgemeine Ausrüstung kaufen | ✅ | Gear Teil A (`docs/plaene/archiv/gear_plan.md`, 2026-10-04): Unter-Tab "Ausrüstung" (Baum nach Bereich + Detailbereich), 140 Einträge aus 28 GRW-Tabellen (S. 368, 374–385, 395, am Bild übertragen), Menge in Packungsschritten mit Zusammenführen, Pflicht-Zusatztext (Lizenz mit Vorauswahl), Preisspannen mit Mindestpreis, Karriere-Kauf/Mengenerhöhung über das Nuyen-Log, Persistenz. UI vom Nutzer noch nicht angesehen |
| Stufe ≤ 6 bei Erschaffung (allgemein) | ✅ | `Equipment.isAvailableForPurchase` (GRW S. 111), gilt für alle Kaufkategorien mit Stufe |
| Kommlink mit Prozessor/Signal + Betriebssystem (Firewall/System) | ✅ | Gear Teil B (2026-10-08): 9 Kommlinks, 6 Betriebssysteme (GRW S. 374, am Bild); Betriebssystem als Kindknoten, gekauft über "Betriebssystem installieren", höchstens eins je Kommlink; System ≤ Prozessor (S. 260) mit Hinweis im Detailbereich; Kommlinks/Betriebssysteme sind Einzelstücke (Menge 1, kein Zusammenführen). UI vom Nutzer noch nicht angesehen |
| Matrixprogramme | ✅ | 2026-11-13 (ADR-054): 7 Standard-, 15 Hackingprogramme und Agent einzeln (S. 271–273, am Bild), Preisbänder der Tabelle S. 272/376; einzeln kaufbar oder ins Kommlink einbaubar, Stufe wirkt höchstens mit dem System. Programmpakete (Basic User …) bleiben draußen |
| Kommlink-Aufrüstungen, laufende Programme, aktives Kommlink | ✅ | 2026-11-13 (ADR-054): Prozessor-/Signal-Modul 1–6 (S. 261, am Bild), Firewall-/System-Paket (S. 260/376), +2 nur auf Hardware, eins je Attribut; Schalter „läuft“ mit Prozessorlimit (S. 261); Sim-Modul im Kommlink; aktives Kommlink (auch Headware) wird im Status-Panel gewählt |
| Sensoren, Sicht-/Audioverstärkung, Magschloss-Zusätze (Kapazität) | ✅ | Gear Teil C (2026-10-08): 10 visuelle/optische Geräte, 8 Sichtverstärkungen, 3 Audiosensoren, 3 Audioverstärkungen, 3 Sensorpakete (RFID/Mikro/Hand, 0 ¥ mangels GRW-Preis), 12 Sensor-Funktionen (inkl. Kamera/Mikrofon als Sensor), 3 Magschloss-Zusätze (GRW S. 378–382, am Bild). Einbau über "Einbauen" mit Kapazitätsprüfung, Verfügbarkeitszuschläge erhöhen die Verfügbarkeit des Geräts (auch für die Grenze 12 bei Erschaffung). UI vom Nutzer noch nicht angesehen |
| Drohnen-/Fahrzeug-Sensorpakete | ✅ | Fahrzeug-Feature (2026-10-09): Stationär/kleine Drohne 5/4, Mittlere Drohne 6/4, Große Drohne 8/4, Fahrzeug 12/5 (Kapazität/Signal, GRW S. 381); "Sensorpaket (Hand)" heißt jetzt "Sensorpaket (Hand/Minidrohne)" wie die GRW-Zeile (alte Speicherdateien verlieren diesen Eintrag beim Laden) |
| Autosoft-Typ als Auswahl | ✅ | 2026-10-09: Abwehr, Clearsight, Elektronische Kriegsführung, Steuerung, Zielerfassung (GRW S. 287); ohne Unterangabe Waffe/Fahrzeugtyp (Nutzerentscheidung) |
| Smartlink erfordert Bildverbindung | ✅ | GRW S. 380: ein Smartlink im Gerät zählt für den Smartlink-Bonus nur mit Bildverbindung im selben Gerät (ADR-056); Einbau selbst bleibt erlaubt |
| Mage-Sight-Brille/Periskop "[1]" als Einbau | ❌ | GRW nennt keinen Einbauort; nur als eigenständiges Gerät |
| Orte/Lager-Kategorien        | ❌      | Zurückgestellt (später gemeinsam für Gear, Waffen, Rüstung) |
| Holster, Versteckte Armhalterung, Smart-Feuerplattform | ✅ | Als Gear in Teil A (Tabelle Feuerwaffenzubehör, GRW S. 368); Smart-Feuerplattform ohne Waffenbezug, Werte als Hinweis |
| Foki kaufen, binden, aktivieren | ✅ | 2026-11-13 (ADR-055): Fokusart und Zusatz als Auswahl (Zauberkategorie, Geisterart, Nahkampffertigkeit); Bindung im Detail des Fokus (Erschaffung 1 BP je Kraft, Karriere Kraft × Faktor Karma, S. 112/234), höchstens Magie Foki und Kraft 5 × Magie, aktiv höchstens Logik (S. 233/234). Zauberspeicher ohne Mechanik |
| Munition, Pfeile/Bolzen, Granaten/Raketen | ✅ | Gear Teil D (2026-10-08, GRW S. 361, 369–371 am Bild): 11 Munitionssorten pro 10 Schuss mit Waffenklasse (Auswahl, GRW S. 368/369; Taserpfeil, Sturmkanonen-Geschosse, Injektionspfeile mit fester Waffe), Bolzen/Injektionsbolzen als normale Ausrüstung; Pfeile (Stufe 1–8, Preis fest) im Waffen-Tab am Bogen mit Schaden nach min(Pfeil, Bogen) und Mengen-Stepper; 7 Granaten (Standard/Minigranate), 3 Raketen, 3 Lenkraketen (Sensorstufe 1–6), 3 Sprengstoffe pro kg, Sprengkapsel; Kampfwerte nur als Anzeige. UI vom Nutzer noch nicht angesehen |
| Munitionswirkung auf Waffenwerte | ✅ | 2026-11-15 (ADR-056), siehe Abschnitt 12 |
| Granaten/Sprengstoff im Kampf | ❌ | Zurückgestellt (gear_plan.md Abschnitt 13); Roadmap 15 (optional) |
| Wirkung von Drogen und Toxinen, Sucht | ❌ | Nur Katalogeinträge; Roadmap 15 (optional) |
| DIY/Hacked/Gratis/Aufschlag/Schwarzmarkt, Verkauf, "ausgerüstet" | ❌ | Zurückgestellt (gear_plan.md Abschnitt 13) |

---

## 14. Fahrzeuge

| Feature                        | Status | Anmerkung        |
|--------------------------------|--------|------------------|
| Fahrzeuge und Drohnen kaufen | ✅ | 2026-10-09 (`docs/plaene/archiv/fahrzeuge_plan.md`): eigener Haupttab "Fahrzeuge & Drohnen" (Baum nach GRW-Kategorie + Detailbereich), 37 Einträge der GRW-Tabelle S. 397 (am Bild, Werte aus dem GRW statt C#: Everglades 25.500 ¥, Sensor der Luftfahrzeuge), Verf. ≤ 12 sichtbar-deaktiviert, Restgeld bzw. Karriere-Abbuchung, Persistenz. Anzeige: Zustandsmonitor 8 + ⌈Rumpf/2⌉ (S. 197), Gerätestufe 3/4 (S. 261), Sensor mit Ø eingebauter Sensoren (S. 380). UI vom Nutzer noch nicht angesehen |
| Riggeradapter, Waffenhalterungen | ✅ | GRW S. 395/396: je 2.500 ¥ (Verf. 4 bzw. 8V); Drohnen haben den Riggeradapter (S. 398); Halterungen ≤ Rumpf ÷ 3 inkl. mitgelieferter (Doberman, Steel Lynx, Wasp, Citymaster, Cutlass) |
| Waffen in Halterungen | ✅ | Feuerwaffen bis LMG + Granatwerfer (GRW S. 395 + Nutzerentscheidung), eine je Halterung, nur Tabellenwerte (ohne Fertigkeit/Würfelpool); gehören nicht zu den Charakterwaffen |
| Ausrüstung im Fahrzeug | ✅ | Beliebige Ausrüstung, mitgeliefertes Sensorpaket mit Einbau von Sensor-Funktionen, mitgelieferte Autosofts (Doberman, Steel Lynx, Stormcloud, Fly-Spy) und Medkit 4 (AutoDoc) kostenlos und nicht entfernbar |
| Zubehör an Fahrzeugwaffen | ❌ | Zurückgestellt (Nutzerentscheidung 2026-10-09) |
| Matrix-Upgrades (Prozessor/Signal/Firewall/System) | ✅ | 2026-11-13 (ADR-054): Gerätestufe als Ausgangswert, dieselben Module und Pakete wie beim Kommlink (S. 260/261) |
| Pilot-Upgrade | ❌ | Kein eindeutiger GRW-Beleg, zurückgestellt |
| Eigenname, Notizen, Orte | ❌ | Zurückgestellt (gemeinsam mit Gear/Waffen) |
| Würfelpools Rigger/Drohnen | ✅ | 2026-11-15 (ADR-056): Tabelle S. 287 (am Bild) in drei Spalten: hineingesprungen (Fertigkeiten des Riggers, Prozessor/Sensor des Fahrzeugs, Riggerkontrolle +2, S. 286/385, nur mit Riggeradapter), autonom (Pilot + Autosoft, nur Drohnen, Angriff nur mit Zielerfassung S. 202), Fernsteuerung (laufendes Befehl auf dem aktiven Kommlink); Fahrzeugprobe des Fahrers mit Handling (S. 198); Fahrzeugfertigkeit je Fahrzeug im Katalog |
| Schaden am Zustandsmonitor | ✅ | 2026-11-15 (ADR-056): Kästchen im Fahrzeugdetail, nur Karrieremodus, gespeichert, ohne Verletzungsmodifikator (S. 197) |
| Fahrzeugkampf-Manöver (Rammen, Verfolgungsjagd) | ❌ | Zurückgestellt |
| Arsenal-Inhalte (Slots, Mod-Boni, weitere Fahrzeuge/Mods), Gebrauchtwagen, Verkauf | ❌ | Zusatzband bzw. kein GRW-Beleg, zurückgestellt |


---

## 15. Karrieremodus

| Feature                                                               | Status | Anmerkung                                                     |
|-----------------------------------------------------------------------|--------|---------------------------------------------------------------|
| Übergang Creation → Career (Startkapital nach Lebensstil)             | ✅      | GRW S. 114: `Character.finalizeCreation()` bucht (Würfel + Restgeld-Bonus) × Multiplikator, Restgeld verfällt; Abschlussdialog würfelt, Wurf editierbar (2026-09-27, ersetzt die frühere Umrechnung des vollen Budgets) |
| Nuyen-Käufe im Karrieremodus (Cyberware, Rüstung)                     | ✅      | Guthabenprüfung + Bestätigung + negativer Log-Eintrag; Entfernen ohne Erstattung |
| Verkauf von Ausrüstung im Karrieremodus                               | ✅      | 2026-11-15 (ADR-058): Waffen, Rüstung, Ausrüstung, Fahrzeuge; Erlös mit 30 % vorbelegt und änderbar (S. 358), Gutschrift als Verkauf; keine Cyberware |
| Essenzverlust senkt Magie/Resonanz (statt ESS-Penalty-Korrektur beim Übergang) | ✅ | GRW S. 208/111: `Character.getEssencePenalty()` senkt wirksamen Wert und Maximum in beiden Modi gleich; die C#-Übergangskorrektur entfällt damit |
| Karma-Ausgaben für Attribute (neue Stufe × 5 Karma)                   | ✅      |                                                               |
| Karma-Ausgaben für Fertigkeiten (neue Stufe × 2, erste Stufe 4 Karma) | ✅      | Korrigiert in `Skill.getKarmaCost()`                          |
| Karma-Ausgaben für Qualities (2× GP-Kosten, Gabe/Handicap)            | ✅      | `Character.getQualityKarmaCost()`, 2026-08-04                 |
| Karma/Nuyen-Log mit Datum                                             | ✅      |                                                               |
| Undo für Log-Einträge                                                 | ✅      | 2026-11-15 (ADR-058): letzter Schritt, beliebig oft, über den gemerkten Stand vor der Buchung; nur in der laufenden Sitzung |
| Expense-Typen (z.B. ImproveAttribute, AddQuality …)                   | ✅      | 2026-11-15 (ADR-058): zehn Arten (`LogCategory`, auch „Kontakt“) als Spalte „Art“, gespeichert; alte Einträge „Manuell“ |
| Ruf: Straßenruf, Schlechter Ruf, Prominenz | ✅ | 2026-11-15 (ADR-059): im Tab Kontakte, Spielleiter-Punkte und Opfern im Karrieremodus (S. 308/309) |
| Adept, Zauberer, Magieradept, Technomancer nicht nachträglich wählbar (S. 313) | ✅ | 2026-11-15 (ADR-059); per Regel-Einstellung „Spätes Erwachen“ aufhebbar (ADR-061) |

---

## 16. Initiativberechnung

| Feature                                            | Status | Anmerkung |
|----------------------------------------------------|--------|-----------|
| Physische Initiative = INT + REA (+ Modifikatoren) | ✅      | Initiativeattribut (GRW S. 172) mit wirksamen Werten, gekappt auf ⌊1,5 × (REA-Max + INT-Max)⌋ (S. 91), Verletzungsmodifikator abgezogen; seit ADR-065 im Status-Panel, 2026-11-12 |
| Astrale Initiative = INT × 2                       | ✅      | GRW S. 227, 3 Durchgänge, nur mit Astralzugang (Gabe Zauberer; Magieradept nicht, S. 116) |
| Matrix-Initiative (mit Commlink oder Technomancer) | ✅      | 2026-11-13 (ADR-054): AR = körperlich (S. 265); VR kalt Prozessor + INT / 2, heiß + 1 / 3 (S. 276), braucht Sim-Modul und Neuralinterface (S. 258); Technomancer (INT × 2) + 1 / 3 (S. 279) |
| Initiative Passes anzeigen                         | ✅      | 1 + Gesteigerte Reflexe (S. 230); Reflexbooster noch nicht; astral 3 |

---

## 17. Condition Monitor

| Feature                                    | Status | Anmerkung |
|--------------------------------------------|--------|-----------|
| Physischer CM = 8 + ⌈BOD/2⌉                | ✅      | GRW S. 94, verstärkte KON (seit ADR-046); Überzahl = KON + Überlebenswille (S. 192, 117) |
| Stun CM = 8 + ⌈WIL/2⌉                      | ✅      | GRW S. 94, verstärkte WIL |
| CM-Boxen anklicken und füllen              | ✅      | Nur im Karrieremodus, gespeichert; geistiger Überlauf wird nicht automatisch übertragen |
| Wundmodifikator berechnen (−1 pro 3 Boxen) | ✅      | GRW S. 193, je Spalte, mit Hoher/Niedriger Schmerztoleranz (S. 116/120) und Schmerzresistenz (S. 231); wirkt auf alle angezeigten Pools und die Initiative |

---

## 18. Persistenz (Speichern/Laden)

| Feature                                                    | Status | Anmerkung                                                                    |
|------------------------------------------------------------|--------|------------------------------------------------------------------------------|
| JSON-Speicherung und -Laden (Grundstruktur)                | ✅      |                                                                              |
| Attribute werden korrekt gespeichert/geladen               | ✅      | Grundattribute, Edge (samt ausgegebenen und verheizten Punkten), Magie bzw. Resonanz |
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
| Seitenleiste, Kopf- und Fußzeile, Status-Panel (statisch umschaltbar) | ✅      | UI-Rework 2026-11-12, ADR-049 |
| Einheitliche Optik (Farbvariablen, Schriften, Kartenstil, Icon)      | ✅      | UI-Rework 2026-11-12, ADR-049 |
| Häufige Proben im Status, einzeln ein-/ausblendbar                   | ✅      | 2026-11-18 (ADR-061): sieben Proben, Karte „PROBEN“; seit ADR-065 Attributproben fest im Status, Wahrnehmung/Ausweichen/Bewegung wählbar im Cheat-Sheet |
| Einstellung: spätes Erwachen erlauben (latente Charaktere)          | ✅      | 2026-11-18 (ADR-061): Regel-Einstellung je Charakter |
| Einstellung für Regelauslegungen: Gliedpanzerung als Durchschnitt statt Summe | ✅      | 2026-11-18 (ADR-061): Regel-Einstellung je Charakter; Berechnung nur in `Implants#getLimbArmor` |
| Einstellungsdialog (Anzeige global, Regeln je Charakter)            | ✅      | 2026-11-18 (ADR-061): Menü Einstellungen › „Einstellungen…“; „Status statisch“ dort statt im Menü |
| Regelwerke je Charakter zuschaltbar                                  | ✅      | 2026-11-18 (ADR-061): `books.xml` (bisher nur GRW), Filter in den Auswahldialogen, Hinweis bei Bestand aus abgeschalteten Regelwerken |
| Bestätigungsdialog bei Karma-Ausgaben                                | ✅      |           |
| Disabled-Zustand von Buttons korrekt (BP/Karma zu wenig, Grenzwerte) | ✅      |           |
| Metatyp-Wechsel mit Attribut-Reset in der Erstellung                 | ✅      |           |
| Drucken / Export / Charakterbogen                                    | ✅      | 2026-11-19 (ADR-067): PDF nach dem GRW-Bogen (S. 406/407) mit Pools, Bleistiftfelder leer, Fortsetzungsseiten; gedruckt wird aus dem PDF-Viewer |
| Spielleiter-Modus (Korrekturen ohne Kosten und Sperren) | ✅ | 2026-11-15 (ADR-060): Menü Einstellungen, nicht gespeichert, GRW-Obergrenzen bleiben; Käufe bei der Erschaffung gratis (ADR-070) |
| Status-Panel und Zustandstab ohne Doppelungen                        | ✅      | 2026-11-19 (ADR-065): Monitor/Initiative/Attributproben nur im Status, Bereich „Cheat-Sheet“ mit wählbaren Inhalten |
| Mehrere Charaktere gleichzeitig öffnen                               | ✅      | 2026-11-19 (ADR-066): Tabs im Hauptfenster, Stern und Rückfrage bei ungespeicherten Änderungen |
| Zuletzt geöffnete Dateien                                            | ❌      | Nutzerentscheidung 2026-11-19: nach dem Release (Plan Mehrere Dokumente) |
| Kategorie-Dropdowns alphabetisch                                     | ✅      | Nutzervorgabe 2026-10-08: zentral in `CatalogSelectDialogController.refreshCategoryOptions()` (deutsche Sortierung, "Alle" oben); ersetzt die GRW-Reihenfolge der Ausrüstungs-/Waffen-Pläne |
| Icon für "Pfeile hinzufügen" (Waffen-Tab)                           | ❌      | **Notiz (Nutzerwunsch 2026-10-08):** das Bullseye-Icon passt nicht; ein anderes FontAwesome-Icon suchen oder den Button als Text-Button belassen |
| Einstellbare Zahlendarstellung (QoL)                                 | ✅      | 2026-11-18 (ADR-061): Tausenderpunkt und Dezimaltrennzeichen, zentral über `utils.Numbers` |
| Vorlagen im Dialog „Neu“ (Archetypen, Schergengruppen)                | ✅      | 2026-11-19 (ADR-063): eine Auswahl mit Quelle und Seite; 16 GRW-Archetypen (S. 124–139) in der Erschaffung, alle mit genau 400 GP |
| Beta-Menü und Durchklick-Checkliste                                   | ✅      | 2026-11-19 (ADR-063): nur mit `./gradlew run`; Testfälle über `./gradlew generateSamples`; `docs/beta_checkliste.md` |

---

## 20. NSCs und Critter

| Feature | Status | Anmerkung |
|---|---|---|
| Schergengruppen (eigene Dateiart, Werteblock-Bogen) | ✅ | 2026-11-18 (ADR-062): Professionalitätsstufe, Mitglieder, Schergen- und Anführer-Werteblock aus den Katalogen mit Pools |
| Kampfverwaltung für Schergen | ✅ | Monitor je Mitglied (S. 324), Gruppen-Edge, Führungsprobe, Fluchthinweis (S. 325) |
| Beispiel-Schergen des GRW als Vorlage | ✅ | sieben Gruppen (S. 325–328), `npcs.xml` |
| Top-Runner | ❌ | Nutzerentscheidung 2026-11-18: als normalen Charakter bauen |
| Bogen-Export für Schergengruppen und Critter | ❌ | Nutzerentscheidung 2026-11-19: nach dem Charakterbogen, eigener Punkt |
| Critter mit Kräften und Schwächen | ✅ | 2026-11-19 (ADR-064): 19 GRW-Critter (S. 343–351) als Vorlage, einzeln oder als Rudel auf dem Schergen-Bogen; Werte ±3 (S. 337), Kräfte und Schwächen mit Tooltip, natürliche Waffen und Panzer gerechnet |
| Kräftekatalog für Critter, Geister und Sprites | ✅ | 2026-11-19 (ADR-064): `critter_powers.xml` (48 Kräfte, S. 283, 337–342, 348, 351), Wirkung als Tooltip je Kraft im aufgeklappten Geist (ADR-068) |

---

## Zusammenfassung

Stand 2026-11-19, aus den Tabellen oben gezählt (❌ sind überwiegend bewusst zurückgestellte Zusatzband-Inhalte).

| Bereich                | ✅ | ⚠️ | ❌ |
|------------------------|----|----|----|
| Erstellungsgrundgerüst |  6 |  0 |  0 |
| Metatypen              |  4 |  0 |  2 |
| Attribute              | 14 |  0 |  0 |
| Aktive Fertigkeiten    | 18 |  0 |  0 |
| Wissensfertigkeiten    |  9 |  0 |  0 |
| Qualities              | 10 |  0 |  0 |
| Kontakte               |  4 |  0 |  0 |
| Magie                  | 17 |  0 |  0 |
| Technomancer           |  7 |  0 |  0 |
| Cyberware/Bioware      | 14 |  0 |  0 |
| Rüstung                |  7 |  0 |  0 |
| Lebensstil             |  3 |  0 |  1 |
| Waffen                 |  6 |  0 |  1 |
| Ausrüstung (Gear)      | 13 |  0 |  5 |
| Fahrzeuge              |  7 |  0 |  5 |
| Karrieremodus          | 12 |  0 |  0 |
| Initiative             |  4 |  0 |  0 |
| Zustandsmonitor        |  4 |  0 |  0 |
| Persistenz             |  9 |  0 |  0 |
| UI/Bedienbarkeit       | 19 |  0 |  2 |
| NSCs und Critter       |  5 |  0 |  2 |

---

## Prioritisierte Reihenfolge für nächste Schritte

**Roadmap bis zum ersten Release (Nutzerentscheidung 2026-11-13, Reihenfolge fest; Punkt 9 ergänzt 2026-11-15, ab Punkt 11
angepasst nach der GRW-Bestandsaufnahme 2026-11-15; Drogen/Toxine/Granaten ans Ende gestellt):** Die Punkte 1–5 der
README-Roadmap sind erledigt; es folgen:

6. ✅ **Matrix und Kommlinks (2026-11-13, ADR-054):** Hardware-Aufrüstungen (S. 260/261) und Firewall/System (S. 376), Programme einem
   Kommlink zuordnen, Matrix-Initiative (S. 265/276/279, Abschnitt 16) mit Sim-Modul (Abschnitt 10), Lebende
   Persona, Matrix-Upgrades für Fahrzeuge (Abschnitt 14). Plan: `docs/plaene/archiv/matrix_kommlinks_plan.md`.
7. ✅ **Magie abrunden (2026-11-13, ADR-055):** Foki binden (Karma) und ihre Wirkung (Abschnitt 13), Katalog der Schutzpatrone mit Boni
   (bisher nur Freitext an der Gabe), situative Adeptenkräfte als Rechnung.
8. ✅ **Kampfwerte vervollständigen (2026-11-15, ADR-056):** Munitionswirkung auf Waffenwerte (Abschnitt 13), Smartlink-Bonus auf den Pool (aus Punkt 6 verschoben, Nutzerentscheidung 2026-11-13),
   Würfelpools für Rigger und Drohnen, Fahrzeugschaden am Zustandsmonitor (Abschnitt 14).
9. ✅ **Status-Panel leicht überarbeiten (2026-11-15, ADR-057):** Modus im Kopf nur bei der Erschaffung, Trenner „|“
   an jeder Stufe des Verletzungsmodifikators im Zustandsmonitor, Edgepunkte und Edge verheizen.
10. ✅ **Karrieremodus ausbauen (2026-11-15, ADR-058):** typisierte Log-Einträge mit Rückgängig, Lebensstilzahlungen (Abschnitt 11a), Verkauf
   von Ausrüstung (GRW-Beleg noch suchen, Abschnitt 15).
11. ✅ **Restliche GRW-Inhalte (2026-11-15, ADR-059):** Exotische Fertigkeiten (Abschnitt 4), Bewegungsrate (Abschnitt 2), Ruf mit Straßenruf,
    Schlechtem Ruf und Prominenz (ca. S. 308, Straßenruf aus dem verdienten Karma, Abschnitt 15), Talentleitungen mit
    Aktions-, Wissens- und Linguasofts (Abschnitt 10), Sperre für Adept/Zauberer/Magieradept/Technomancer im
    Karrieremodus (S. 313, Abschnitt 15; aufhebbar über Punkt 12), Vorschlagsliste für Wissensfertigkeiten (ADR-032),
    Portrait.
12. ✅ **Einstellungen (2026-11-18, ADR-061):** Einstellungsdialog mit Zahlenformat, Proben im Status, Status statisch
    (Anzeige, global) sowie Gliedpanzerung als Durchschnitt, spätes Erwachen, versäumte Lebensstilzahlungen und
    Regelwerk-Auswahl (Regeln, je Charakter).
13. ✅ **Charakterbogen (2026-11-19, ADR-067):** PDF-Export nach dem GRW-Bogen mit Würfelpools und Fortsetzungsseiten.
14. **NSCs:** ✅ Teil 1 Schergengruppen mit GRW-Vorlagen und Kampfverwaltung (2026-11-18, ADR-062); Teil 2 Critter mit
    ihren Kräften (2026-11-19, ADR-064).
15. **Drogen, Toxine und Granaten (optional):** Wirkung, Sucht und Einsatz im Kampf statt nur Katalogeinträge
    (Abschnitt 13); nur, wenn das Werkzeug das abbilden soll.
16. ✅ **Status-Panel und Zustandstab entdoppeln (2026-11-19, ADR-065):** Zustandsmonitor mit Überzahl, Initiative und
    Attributproben nur im Status; der Bereich „Zustand“ wird zum „Cheat-Sheet“ mit wählbaren Inhalten (eigener Tab in
    den Einstellungen).
17. ✅ **Mehrere Charaktere gleichzeitig öffnen (2026-11-19, ADR-066):** Tabs im Hauptfenster, Stern und Rückfrage
    „Speichern / Verwerfen / Abbrechen“ bei ungespeicherten Änderungen, bereits offene Datei → ihr Tab.

**Release:** Das erste Release (Tag `v1.0.0`) kommt erst, wenn alle Funktionen des Grundregelwerks umgesetzt sind
(Nutzerentscheidung 2026-11-11); die Release-Technik steht seit ADR-043. Vor dem Tag: erster Lauf von `release.yml` auf
Windows und macOS (bisher nur Linux lokal gebaut), Installer und portables ZIP einmal von Hand starten, die UI
nach `docs/beta_checkliste.md` durchklicken (Beta-Menü mit `./gradlew run`).

**Nach dem Release:** Als erster Punkt kommt die **Erschaffung mit Karma** statt BP (Nutzerentscheidung 2026-11-19):
Erschaffungsart im Dialog „Neu“ wählbar, Kosten nach Karma statt BP, eigene Grenzen. Die Regeln stehen im
Runner-Kompendium (Seiten beim Planen am Buch prüfen), das C#-Original kennt die Erschaffungsart. Dafür muss das
Buch über die Regelwerk-Auswahl (ADR-061) zuschaltbar sein.

**Danach (zurückgestellt, Reihenfolge offen):** Fahrzeugkampf-Manöver (Abschnitt 14), Zubehör an
Fahrzeugwaffen, Lagerorte und Eigennamen für Ausrüstung, Waffen, Rüstung und Fahrzeuge (Abschnitte 13/14),
DIY/Hacked/Schwarzmarkt (Abschnitt 13), Cyberguns (Abschnitt 10, GRW ohne Kosten), Zusatzbände über die
Regelwerk-Auswahl (ADR-061; Kandidaten: Waffenmodifikationen, Lebensstil-Ausbaustufen, Arsenal-Fahrzeuge).

**Zusatzbände (Ideen vom 2026-11-18):**

- **Reihenfolge der Bücher:** 1. Runner-Kompendium (Erschaffung: Metavarianten, Gaben/Handicaps, Kontakte, Lebensstil),
  2. Straßenmagie (Traditionen, Mentorgeister, Metamagie, Geasa), 3. Arsenal 2070 (Waffen, Ausrüstung,
  Modifikationen), 4. Vernetzt (Matrix, Echos, Sprites; rückt vor, sobald jemand Hacker spielt), 5. Bodytech; danach
  Parazoologie (mehr Critter in der Vorlagenliste), Fronteinsatz, Parabotanik. Quellen sind die deutschen Ausgaben.
- **Vorher:** eine Übersicht aller vorhandenen Bücher, je Buch eingeordnet als „nur Katalog“, „ändert Regeln“ oder
  „nur Setting“, mit Umfang.
- **Katalog-Rezept:** `catalog-extractor` um einen Ablauf „Neues Buch aufnehmen“ erweitern, geschrieben am ersten
  echten Buch. Inhalt: Einordnung (Regeln laufen über den Plan-Workflow, nur Katalog über das Rezept),
  `books.xml`-Eintrag mit Seitenversatz, Übertragung am Seitenbild, Abgleich mit den C#-Daten (nur Werte, keine
  Seiten, weil Chummer die englischen Ausgaben referenziert), Zuordnung Buchtitel deutsch ↔ Chummer-Kürzel und
  englischer Name je Eintrag (nützt dem Chummer4-Konverter), Loader-Test je Quelle, fester Bericht.
- **Chummer4-Konverter:** Entwickler-Tool `.chum` → `.json`, kein App-Feature; Grobplan
  `docs/plaene/chum_konverter_plan.md`, kurz vor dem Release.

**Regelfragen (Ideen vom 2026-11-18):**

- **Globale Katalogsuche** („In welchem Regelwerk finde ich …?“): ein Suchfeld über alle Kataloge, Treffer mit Quelle,
  Seite und eigener Kurzbeschreibung; offline, ohne Buchtexte, klein (2–3 Schritte).
- **Regel-Assistent** („Erkläre mir den Zauber …“), voraussichtlich nicht umgesetzt: eigene PDFs aus einem lokalen
  Ordner indizieren, passende Ausschnitte an ein Sprachmodell (eigener API-Schlüssel), Antwort mit Buch und Seite. Groß
  (6–8 Schritte); Hindernisse: OCR-Qualität und Tabellen, Netz und Kosten, Ausschnitte gehen an einen externen Dienst.

Frühere Roadmaps (Stufen 1–3, Kandidatenliste vom 2026-11-11) sind abgearbeitet und stehen in der Git-Historie dieser
Datei; Begründungen in `DECISIONS.md`, Pläne in `docs/plaene/archiv/`.
