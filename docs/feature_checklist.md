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
| Sonderattribute: EDG (Edge)                                                 | ✅      | `SpecialAttribute`/`SpecialAttributeType`, BP/Karma-Kosten, UI-Sektion, Persistenz; immer sichtbar |
| Sonderattribute: MAG (Magie)                                                | ✅      | Wie EDG, aber nur sichtbar/freigeschaltet wenn Quality mit `GRANTS_MAGIC` vorhanden (`Character.hasMagic()`) |
| Sonderattribute: RES (Resonanz)                                             | ✅      | Analog MAG, über `GRANTS_RESONANCE` (`Character.hasResonance()`)                              |
| Abgeleitetes Attribut: INI (Initiative = INT + REA)                         | ❌      | Nicht modelliert                               |
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
| Default-Flag (erlaubt Würfeln ohne Rating: Attribut − 1)         | ❌      | Flag im Modell fehlt, keine Würfelpool-Anzeige                                               |
| Würfelpool-Anzeige (Rating + Attribut + Modifikatoren)           | ❌      | Nicht vorhanden                                                                              |
| Exotische Fertigkeiten (Spez = Gegenstand, kein eigener Rating)  | ❌      |                                                                                              |
| Kampf-Aktionsfertigkeiten nach GRW                               | ✅      | **Korrigiert 2026-09-28** (GRW S. 147/148, am Bild geprüft): Gruppen Nahkampf (Klingenwaffen, Knüppel, Waffenloser Kampf) und Feuerwaffen (Gewehre, Pistolen, Schnellfeuerwaffen), dazu Ausweichen (REA), Exotische Fern-/Nahkampfwaffe, Projektilwaffen, Schwere Waffen, Wurfwaffen. Entfernt: Klingen, Automatikwaffen, Langwaffen, Schrotflinten (keine GRW-Fertigkeit), Bogenschießen, Gruppe "Schusswaffen". "Sprengstoff" → technische Fertigkeit "Sprengstoffe" (GRW S. 154). Alte Speicherdateien: Werte umbenannter Fertigkeiten werden beim Laden übersprungen |
| Übrige Fertigkeitsbereiche nach GRW (Körperlich, Sozial, Technisch, Fahrzeug) | ❌ | **Befund 2026-09-28:** `skills.xml` weicht auch dort vom GRW S. 149–154 ab, z.B. Sozial GRW: Einschüchtern, Führung, Gebräuche, Überreden, Unterricht, Verhandlung (Gruppe Einfluss); Technisch GRW: Hacking, Hardware, Software, Computer, Datensuche (Gruppe Elektronik/Cracken), Fahrzeug-/Industrie-/Luftfahrt-/Seefahrtmechanik (Gruppe Mechanik), Kybernetik, Schlosser, Waffenbau … Eigener Korrekturauftrag (Abgleich Seite für Seite am Bild) |

---

## 5. Wissensfertigkeiten (Knowledge Skills)

| Feature                                                       | Status | Anmerkung        |
|---------------------------------------------------------------|--------|------------------|
| Wissensfertigkeiten-Pool ((INT + LOG) × 3 freie Punkte)       | ✅      | Korrigierte Formel (nicht INT×2, siehe `docs/analysen/wissensfertigkeiten_analyse.md`) |
| Hinzufügen/Entfernen von Wissensfertigkeiten                  | ✅      | Freitext-Name, Dialog mit Kategorie-Auswahl, Duplikat-Prüfung |
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

---

## 7. Kontakte

| Feature                                                   | Status | Anmerkung        |
|-----------------------------------------------------------|--------|------------------|
| Kontakt hinzufügen (Name, Kurzbeschreibung, Connection, Loyalty) | ✅ | `Contact`-Modell, eigener Tab, Add/Edit-Dialog (Beschreibung als Tooltip), 2026-08-07. Bewusst reduzierter Scope: kein Enemy/Pet-Unterschied, keine Gruppenkontakt-Modifikatoren (kein GRW-Beleg im Kernregelwerk, siehe `docs/analysen/kontakte_analyse.md`) |
| BP-Kosten: 1 BP pro Kontaktpunkt (Connection + Loyalty)   | ✅      | GRW S. 113 bestätigt: "Kosten = Connectionstufe + Loyalitätsstufe", `Character.getContactBP()` |
| ~~Freie Kontaktpunkte: CHA × 2~~                          | ✅      | **Widerlegt** (2026-08-04): Im GRW-Erstellungskapitel zu Connections (S. 113) wird Charisma kein einziges Mal erwähnt — Kontakte werden ganz normal aus dem allgemeinen 400-GP-Budget bezahlt, kein eigener CHA-basierter Freibetrag. Kein Freibetrag implementiert |
| Karrieremodus: Kontakte frei bearbeitbar, ohne Karma-Kosten | ✅ | GRW: Connections werden nicht mit Karma gekauft ("müssen sie sich auf die harte Tour verdienen"), neue `wireFreeIncrementable()`-UI-Methode |

---

## 8. Magie

> Scope-Entscheidung (2026-08-08): Nur reine **Magier** umgesetzt (Tradition + Zauber-Erlernen).
> Adept-Kräfte und Mystic-Adept-Split sind bewusst zurückgestellt (eigenes Folgefeature). Details:
> `docs/analysen/magie_analyse.md` (C#-Analyse), `docs/analysen/magie_java_analyse.md` (Java-Ist-Analyse),
> `docs/plaene/magie_plan.md` (Umsetzungsplan inkl. aller Entscheidungen).

| Feature                                                | Status | Anmerkung                                                                                                                                                                                                                                                            |
|---------------------------------------------------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Magier-Erkennung (feiner als reines MAG-Attribut)      | ✅      | Neuer `QualityBonusType.GRANTS_SPELLCASTING`, nur auf Quality "Magier" gesetzt — unterscheidet Magier von Adept (beide teilen weiterhin `GRANTS_MAGIC` fürs MAG-Attribut selbst), `Character.isMagician()`                                                        |
| Magie-Tab (Tradition, Zauber)                          | ✅      | Eigener Tab, dynamisch ein-/ausgeblendet (`TabPane.getTabs()`), sichtbar sobald Gabe "Magier" aktiv — funktioniert bereits im Erstellungsmodus, nicht erst im Karrieremodus                                                                                        |
| Tradition                                              | ✅      | Festes Enum `Tradition` (Hermetisch/Schamanistisch, GRW-Kern) mit Drain-Attributformel als Anzeigetext; freie Tradition-Erschaffung bewusst nicht umgesetzt                                                                                                         |
| Zauber-Katalog                                         | ⚠️     | Initiale, direkt aus dem GRW-Text verifizierte Teilmenge (10 Zauber, 2 je GRW-Kategorie: Kampf/Wahrnehmung/Heilung/Illusion/Manipulation). Vollständige GRW-Kern-Liste (84 Zauber im C#-Original) ist offener Folgeauftrag, analog zum offenen Punkt bei Qualities |
| Zauber hinzufügen (3 BP / 5 Karma)                     | ✅      | Fixpreis pro Zauber in beiden Modi, Karrieremodus mit Karma-Bestätigungsdialog                                                                                                                                                                                       |
| Zauber-Cap bei Erstellung (2×höchstes Spellcasting-Rating) | ✅  | `Character.canAddSpellAtCreation()`, kein Cap im Karrieremodus                                                                                                                                                                                                       |
| Magische Aktionsfertigkeiten (Spruchzauberei/Ritualzauberei) | ✅ | Neu in `skills.xml`, eigene `SkillCategory.MAGICAL`, immer im Skills-Tab sichtbar (für alle Charaktere), aber nur für Magier steigerbar — `+`-Button deaktiviert ohne Gabe "Magier" (`Character.canRaiseSkill()`, GRW S. 208), gilt in Erstellung **und** Karrieremodus |
| Magier/Adept/Mystischer-Adept-Exklusivität             | ✅      | `Character.addQuality()` verhindert gleichzeitige Wahl mehrerer `GRANTS_MAGIC`-Qualities                                                                                                                                                                            |
| Magier-Quality-Kosten (Bug-Fix)                        | ✅      | `qualities.xml` von 5 auf 15 BP korrigiert (GRW S. 118)                                                                                                                                                                                                              |
| Adept-Tab (Adeptenkräfte, Power Points = MAG)          | ❌      | Zurückgestellt — eigenes Folgefeature                                                                                                                                                                                                                                |
| Adeptenkräfte mit PP-Kosten                            | ❌      | Zurückgestellt — eigenes Folgefeature                                                                                                                                                                                                                                |
| Mystic-Adept-MAG-Split                                 | ❌      | Zurückgestellt — eigenes Folgefeature                                                                                                                                                                                                                                |
| Initiationsgrade                                       | ❌      | Kein GRW-Kern-Beleg im Grundregelwerk gefunden (vermutlich Sourcebook *Street Magic*), niedrige Priorität                                                                                                                                                            |
| Restliche magische Aktionsfertigkeiten (Antimagie, Beschwören-Gruppe, Arcana, Verzaubern, Askennen, Astralkampf) | ❌ | **Gefunden 2026-08-08** (User-Verdacht bestätigt): `skills.xml` enthält nur Spruchzauberei/Ritualzauberei. GRW-Kapitel "Magische Aktionsfertigkeiten" (S. ~208-211) listet zusätzlich Antimagie (Hexerei-Gruppe), Herbeirufen/Binden/Verbannen (Beschwören-Gruppe), Arcana (LOG-verknüpft, auch für Nicht-Magische lernbar), Verzaubern, Askennen und Astralkampf (beide nur mit Astralwahrnehmung nutzbar). Zusatzbefund: Spruchzauberei/Ritualzauberei sind aktuell nicht als GRW-Fertigkeitsgruppe "Hexerei" gruppiert (kein `<group>`-Knoten in `skills.xml`, anders als bei Nahkampf/Schusswaffen etc.). Scope-Entscheidung noch offen (u.a. abhängig davon, ob Beschwören/Geister-Feature überhaupt geplant ist) |

---

## 9. Technomancer

| Feature                                    | Status | Anmerkung |
|--------------------------------------------|--------|-----------|
| RES-Attribut aktivieren (via Quality)      | ✅      | **Korrektur (2026-08-08):** bereits umgesetzt im Zuge des Sonderattribute-Features (analog zur EDG/MAG/RES-Korrektur vom 2026-08-06) — Quality "Technomant" (5 BP, `qualities.xml`), `QualityBonusType.GRANTS_RESONANCE`, `Character.hasResonance()`/`syncMagicOrResonance()`, Magie/Resonanz-Exklusivität, Persistenz. GRW S. 106 bestätigt. Checkliste war nicht mit dem Code synchron, siehe `docs/analysen/technomancer_java_analyse.md` |
| Technomancer-Tab (Schwund, Komplexe Formen) | ✅      | Eigener Tab, nur sichtbar mit Resonanz (`syncTechnomancerTabVisibility()`); Schwund-Widerstand als feste Anzeige "Willenskraft + Resonanz" — kein Stream-Auswahlkonzept, da das GRW-Kern keine Streams kennt (User-Entscheidung 2026-08-08, `docs/plaene/technomancer_plan.md`) |
| Komplexe Formen (1 GP/Stufenpunkt, Karma wie Wissensfertigkeit) | ✅ | `ComplexForm`/`ComplexFormDataLoader` (20 GRW-Einträge), 1 GP pro Stufenpunkt (GRW S. 112, Beispiel Panzerung 4 = 4 GP), Karma 2 neu / neue Stufe; Stufen-Cap = wirksamer Resonanzwert in Erstellung und Karriere (`canRaiseComplexForm()`), inkl. Tests |
| Anzahl-Cap Komplexe Formen bei Erstellung (Logik × 2) | ❌ | **Lücke gefunden 2026-09-27:** GRW S. 112: "Die maximale Anzahl an komplexen Formen, mit denen ein Charakter das Spiel beginnen kann, entspricht – unabhängig von ihrer Stufe – dem doppelten Logikattribut". Im Code nicht geprüft (nur der Stufen-Cap); kleiner Folgeauftrag analog `canAddSpellAtCreation()` |
| Wandlung (Submersion)                      | ❌      | **Korrektur (2026-08-08):** frühere "0 Treffer"-Recherche suchte nach dem falschen Begriff ("Submersion" statt der deutschen GRW-Übersetzung "Wandlung"). GRW-Kern-Seite 284 bestätigt (PDF-Bild verifiziert): eigener Regelabschnitt, Karmaformel `10+Grad×3`, RES-Maximum `6+Grad`, 5 Basis-Echoes (`source=SR4`). **Damit GRW-Kern-Feature**, nicht mehr auf Niedrig-Priorität/Unwired-only einstufen — bleibt aber reines Karrieremodus-Folgefeature, das ein Initiations-Analogon voraussetzt (siehe `docs/analysen/technomancer_analyse.md`) |

---

## 10. Cyberware / Bioware

| Feature                                                       | Status | Anmerkung        |
|---------------------------------------------------------------|--------|------------------|
| Cyberware kaufen (Nuyen-Kosten)                               | ✅      | Katalog = verifizierte GRW-Teilmenge (13 Cyberware/8 Bioware). Preis zählt bei Erschaffung gegen das Ressourcengeld, im Karrieremodus Abbuchung über das Nuyen-Log mit Bestätigung (2026-09-27) |
| Verfügbarkeit ≤ 12 bei Erschaffung                            | ✅      | GRW S. 111; `Availability` je Katalogeintrag, gesperrte Implantate und einzelne Stufen sichtbar-deaktiviert; Grades ohne Verfügbarkeitsaufschlag (GRW S. 359) |
| Essenz-Kosten berechnen (Cyberware vs. Bioware getrennt)      | ✅      | `Cyberware.getEssenceCost()` (`RatingFormula` × Grade), getrennte Summen am `Character` |
| Essenz-Berechnung: höhere Kategorie voll, niedrigere halbiert | ✅      | GRW S. 111 inkl. Rechenbeispiel als Test; Handicap "Immunabstoßung" (S. 120) verdoppelt Cyberware-Summe |
| Grades (Standard, Alphaware, Betaware, Deltaware)             | ✅      | GRW S. 359; bei Erschaffung nur Standard/Alpha wählbar |
| MAG/RES-Reduktion durch Essenzverlust (Wert + Maximum)        | ✅      | GRW S. 208, abgeleitet (`getEffectiveMagicOrResonanceValue/Max()`), investierter Wert bleibt Kostenbasis |
| Subsystem-Kapazität                                           | ❌      | Bewusst zurückgestellt (inkl. Cybergliedmaßen) |
| Augmented Maximum durch Implantate (Augmented Modifier)       | ❌      | Bewusst zurückgestellt |

---

## 11. Rüstung

| Feature                                       | Status | Anmerkung        |
|-----------------------------------------------|--------|------------------|
| Rüstung kaufen und ausrüsten                  | ✅      | Tab "Straßenausrüstung" → "Rüstung", 13 GRW-Einträge (S. 373), Anlegen/Ablegen, "Alle anlegen/ablegen", Verfügbarkeit ≤ 12 bei Erschaffung, Karriere-Kauf über Nuyen-Log (2026-09-27) |
| Ballistik/Impact-Berechnung (Stacking-Regeln) | ✅      | GRW S. 190/372: höchstes angelegtes Teil + Helm-/Schild-Boni |
| Tragebelastung (Armor Encumbrance)            | ✅      | GRW S. 190 inkl. Rechenbeispiel; reduziert GES/REA in der "(Aug)"-Spalte. Helme/Schilde zählen mit; Untergrenze 1 **vorläufig** (kein GRW-Beleg, Nutzerentscheidung) |
| Kleidung, Lederjacke/-mantel                  | ❌      | Zurückgestellt bis Zusatzbände (Runner's Kompendium/Arsenal) vorliegen |
| Rüstungsmodifikationen, GKP-/UEO-Zubehör      | ❌      | Zurückgestellt |
| Schild-Malus −1 auf körperliche Proben        | ❌      | Zurückgestellt (keine Würfelpools im Modell) |
| Darstellung als Baum + Detailbereich          | ❌      | Nutzerentscheidung 2026-09-30: Waffen-Muster (`docs/plaene/waffen_detailansicht_plan.md`) gefällt, Umstellung der Rüstung aber auf später verschoben. Die CSS-Klassen `detail-*` sind dafür schon allgemein angelegt |

---

## 11a. Lebensstil

| Feature                                   | Status | Anmerkung |
|-------------------------------------------|--------|-----------|
| Lebensstil (6 GRW-Stufen, Monate im Voraus) | ✅      | GRW S. 111; Kosten zählen gegen das Ressourcengeld; Tab "Straßenausrüstung" → "Lebensstil" (2026-09-27) |
| Lebensstil-Ausbaustufen, weitere Lebensstile | ❌      | Zusatzband, zurückgestellt |
| Lebensstilzahlungen im Karrieremodus      | ❌      | Zurückgestellt, manuell über das Nuyen-Log |

---

## 12. Waffen

| Feature                     | Status | Anmerkung        |
|-----------------------------|--------|------------------|
| Waffen kaufen und verwalten | ✅      | Teil A umgesetzt 2026-09-28 (`docs/plaene/waffen_plan.md`): 63 GRW-Kern-Waffen (Tabellen S. 361/363/366, am Bild geprüft) mit Tabellenwerten als Text, Bogen mit Stufe 1–8 (Mindeststärke), Kauf/Entfernen, Nuyen-Budget, Verfügbarkeit ≤ 12 (sichtbar-deaktiviert), Karriere-Kauf über Nuyen-Log, Persistenz; Unter-Tab "Waffen" seit 2026-09-30 als Baum nach Kategorie + Detailbereich wie im Original (`docs/plaene/waffen_detailansicht_plan.md`; vom Nutzer angesehen und für gut befunden) |
| Berechnete Werte (STR-Schaden, Reichweiten, Fertigkeit) | ✅ | Teil B umgesetzt 2026-09-30: Schaden aus Stärke (GRW S. 188, STR÷2 aufgerundet; Bogen nach Mindeststärke S. 361), Reichweiten laut Tabelle S. 179 (am Bild geprüft; `<range>` je Waffe, Abweichungen laut Waffentext: Hämmerli 620S, Mossberg AM-CMDT, Spezielle Waffen S. 362/365), Fertigkeit mit Wert je Kategorie (S. 147/148, 360–366). Tabelle aktualisiert sich bei Attribut-/Fertigkeitsänderung. **Einschränkungen:** Exotische Waffen teilen sich je eine Fertigkeit (GRW: je Waffe eigene); Pfeilstufe nicht modelliert; kein Würfelpool (Roadmap Stufe 3) |
| Zubehör mit Halterungen     | ✅      | Teil C umgesetzt 2026-10-04: 16 GRW-Zubehörteile (Tabelle S. 368, am Bild geprüft) an Halterungen Lauf/Unterlauf/Aufmontiert je Waffenklasse (S. 357), eine pro Halterung, Zubehör ohne Halterung mehrfach; Sonderregeln laut Zubehörtexten S. 367/368 (Gasventil nur Automatikpistolen/MPs/Sturmgewehre/MGs und nicht entfernbar, Schalldämpfer EM/HM ohne Revolver, Schallunterdrücker SM/AM, Schockpolster und Gyrostabilisator nur Gewehre/schwere Waffen, Airburst nur Granat-/Raketenwerfer, Schnelllader nur Revolver, Ersatzladestreifen nur Ladestreifen-Waffen, Projektilwaffen nur Lasermarkierer/Smartgun); mitgeliefertes Zubehör laut Waffentexten S. 362–366 (`<included>`), höchstens ein Gasventil/Smartgunsystem/Schockpolster je Waffe; RK "x (y)" inkl. Zubehör (Gasventil fest, alles andere im Klammerwert); internes Smartgunsystem kostet den Waffenpreis; Budget, Karriere-Kauf, Persistenz. **Einschränkungen:** Spezielle Waffen ohne Halterung (kein GRW-Beleg); Airburst verlangt kein Smartgunsystem; HK-XM30-Umbausätze zurückgestellt (nur `<note>`) |
| Waffenmodifikationen        | ❌      | Nur Arsenal, zurückgestellt |
| Munition, Pfeile/Bolzen, Granaten/Raketen | ❌ | Gehören zum Gear-Feature |

---

## 13. Ausrüstung (Gear)

| Feature                      | Status | Anmerkung        |
|------------------------------|--------|------------------|
| Allgemeine Ausrüstung kaufen | ✅ | Gear Teil A (`docs/plaene/gear_plan.md`, 2026-10-04): Unter-Tab "Ausrüstung" (Baum nach Bereich + Detailbereich), 140 Einträge aus 28 GRW-Tabellen (S. 368, 374–385, 395, am Bild übertragen), Menge in Packungsschritten mit Zusammenführen, Pflicht-Zusatztext (Lizenz mit Vorauswahl), Preisspannen mit Mindestpreis, Karriere-Kauf/Mengenerhöhung über das Nuyen-Log, Persistenz. UI vom Nutzer noch nicht angesehen |
| Stufe ≤ 6 bei Erschaffung (allgemein) | ✅ | `Equipment.isAvailableForPurchase` (GRW S. 111), gilt für alle Kaufkategorien mit Stufe |
| Kommlink mit Prozessor/Signal + Betriebssystem (Firewall/System) | ✅ | Gear Teil B (2026-10-08): 9 Kommlinks, 6 Betriebssysteme (GRW S. 374, am Bild); Betriebssystem als Kindknoten, gekauft über "Betriebssystem installieren", höchstens eins je Kommlink; System ≤ Prozessor (S. 260) mit Hinweis im Detailbereich; Kommlinks/Betriebssysteme sind Einzelstücke (Menge 1, kein Zusammenführen). UI vom Nutzer noch nicht angesehen |
| Matrixprogramme mit Preisbändern | ✅ | Gear Teil B: 6 Zeilen der Tabelle S. 376 (Preis bis Stufe 3 / ab Stufe 4), Programmname als Pflicht-Zusatztext (Freitext, keine Programmliste). Programme "System"/"Firewall" sind reine Käufe ohne Wirkung auf Kommlink-Werte |
| Kommlink-Aufrüstungen, laufende Programme/Matrix-Regeln | ❌ | Zurückgestellt: kein GRW-Preis für Aufrüstungen (kommt mit *Vernetzt*); Programme nicht einem Kommlink zugeordnet |
| Sensoren, Sicht-/Audioverstärkung, Magschloss-Zusätze (Kapazität) | ✅ | Gear Teil C (2026-10-08): 10 visuelle/optische Geräte, 8 Sichtverstärkungen, 3 Audiosensoren, 3 Audioverstärkungen, 3 Sensorpakete (RFID/Mikro/Hand, 0 ¥ mangels GRW-Preis), 12 Sensor-Funktionen (inkl. Kamera/Mikrofon als Sensor), 3 Magschloss-Zusätze (GRW S. 378–382, am Bild). Einbau über "Einbauen" mit Kapazitätsprüfung, Verfügbarkeitszuschläge erhöhen die Verfügbarkeit des Geräts (auch für die Grenze 12 bei Erschaffung). UI vom Nutzer noch nicht angesehen |
| Drohnen-/Fahrzeug-Sensorpakete | ❌ | Mit dem Fahrzeug-Feature (Nutzerentscheidung 2026-10-08) |
| Smartlink erfordert Bildverbindung | ❌ | GRW S. 380; derzeit nur Hinweis, keine Einbauprüfung |
| Mage-Sight-Brille/Periskop "[1]" als Einbau | ❌ | GRW nennt keinen Einbauort; nur als eigenständiges Gerät |
| Orte/Lager-Kategorien        | ❌      | Zurückgestellt (später gemeinsam für Gear, Waffen, Rüstung) |
| Holster, Versteckte Armhalterung, Smart-Feuerplattform | ✅ | Als Gear in Teil A (Tabelle Feuerwaffenzubehör, GRW S. 368); Smart-Feuerplattform ohne Waffenbezug, Werte als Hinweis |
| Foki kaufen | ✅ | Nur Kauf (Stufe = Kraft 1–6); Bindung (GP/Karma) und Wirkung folgen mit dem Magie-Feature |
| Munition, Pfeile/Bolzen, Granaten/Raketen | ✅ | Gear Teil D (2026-10-08, GRW S. 361, 369–371 am Bild): 11 Munitionssorten pro 10 Schuss mit Waffenklasse (Auswahl, GRW S. 368/369), Bolzen/Injektionsbolzen als normale Ausrüstung; Pfeile (Stufe 1–8, Preis fest) im Waffen-Tab am Bogen mit Schaden nach min(Pfeil, Bogen); 7 Granaten (Granate/Minigranate), 3 Raketen, 3 Lenkraketen (Sensorstufe 1–6), 3 Sprengstoffe pro kg, Sprengkapsel; Kampfwerte nur als Anzeige. UI vom Nutzer noch nicht angesehen |
| Munitionswirkung auf Waffenwerte, Granaten/Sprengstoff im Kampf | ❌ | Zurückgestellt (gear_plan.md Abschnitt 13) |
| Pfeilmenge nach dem Kauf ändern | ❌ | Pfeile werden über "Pfeile hinzufügen" nachgekauft (Zusammenführen); Verbrauch nur durch Entfernen des Stapels |
| DIY/Hacked/Gratis/Aufschlag/Schwarzmarkt, Verkauf, "ausgerüstet" | ❌ | Zurückgestellt (gear_plan.md Abschnitt 13) |

---

## 14. Fahrzeuge

| Feature                        | Status | Anmerkung        |
|--------------------------------|--------|------------------|
| Fahrzeuge kaufen und verwalten | ❌      | Komplett fehlend |

---

## 15. Karrieremodus

| Feature                                                               | Status | Anmerkung                                                     |
|-----------------------------------------------------------------------|--------|---------------------------------------------------------------|
| Übergang Creation → Career (Startkapital nach Lebensstil)             | ✅      | GRW S. 114: `Character.finalizeCreation()` bucht (Würfel + Restgeld-Bonus) × Multiplikator, Restgeld verfällt; Abschlussdialog würfelt, Wurf editierbar (2026-09-27, ersetzt die frühere Umrechnung des vollen Budgets) |
| Nuyen-Käufe im Karrieremodus (Cyberware, Rüstung)                     | ✅      | Guthabenprüfung + Bestätigung + negativer Log-Eintrag; Entfernen ohne Erstattung |
| Verkauf von Ausrüstung im Karrieremodus                               | ❌      | Zurückgestellt (keine GRW-Verkaufsregel gesucht) |
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
| Einstellbare Zahlendarstellung (QoL)                                 | ❌      | **Notiz (Nutzerwunsch 2026-09-30):** später umschaltbar machen, ob Tausender mit Punkt gruppiert werden ("1.500" wie im GRW vs. "1500") und ob Dezimalzahlen Komma oder Punkt nutzen (z.B. Essenz "5,8" vs. "5.8"). Derzeit überall ohne Tausenderpunkt (Preise, Reichweiten). Umsetzung dann zentral über eine Formatierungsstelle statt verstreuter String-Verkettung |

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
| Kontakte               | 4 | 0  | 0 |
| Magie                  | 0 | 0  | 6 |
| Technomancer           | 3 | 0  | 2 |
| Cyberware/Bioware      | 6 | 0  | 2 |
| Rüstung                | 3 | 0  | 4 |
| Lebensstil             | 1 | 0  | 2 |
| Waffen                 | 3 | 0  | 2 |
| Gear                   | 4 | 0  | 5 |
| Fahrzeuge              | 0 | 0  | 1 |
| Karrieremodus          | 5 | 0  | 4 |
| Initiative             | 0 | 0  | 4 |
| Condition Monitor      | 0 | 0  | 4 |
| Persistenz             | 8 | 1  | 0 |
| UI/Bedienbarkeit       | 4 | 0  | 2 |

---

## Prioritisierte Reihenfolge für nächste Schritte

> **Regelwerk-Abgleich (2026-08-04):** Die komplette Roadmap wurde gegen das SR4-Grundregelwerk geprüft,
> bevor weitere Features umgesetzt werden — siehe `docs/analysen/regelwerk_abgleich_stufe2_roadmap.md` und
> `docs/analysen/regelwerk_abgleich_stufe3_roadmap.md`. Ergebnis: fast alles ist solide belegt, bis auf zwei
> Punkte ohne GRW-Beleg (s.u., auf Niedrig gesetzt) und zwei korrigierte Annahmen (Kontakte, Komplexe
> Formen, siehe jeweilige Abschnitte oben).

**Stufe 1 – Fehlende Kernfeatures für einen vollständigen Charakter**

1. ~~**Wissensfertigkeiten**~~ ✅ umgesetzt (freier Pool (INT+LOG)×3, Kategorien, Überzug in BP, Karrieremodus-Karma)
2. ~~**Sonderattribute** EDG, MAG, RES~~ ✅ umgesetzt (`SpecialAttribute`, BP/Karma-Kosten, UI-Sektion, Persistenz, MAG/RES-Freischaltung via Quality) — **Essenz-Berechnung bleibt offen** (aktuell fixer Wert 6.0, echter Abzug erst mit Cyberware/Bioware in Stufe 2 möglich)
3. ~~**Spezialisierungen** (+2 BP / Karma)~~ ✅ umgesetzt (aktive Fertigkeiten: Fixpreis; Wissensfertigkeiten: +1 Poolpunkt; Karrieremodus einheitlich 2 Karma ohne Uneducated-Verdopplung; Mindestrating 1; Gruppenauflösung bei gruppierten aktiven Fertigkeiten im Karrieremodus), 2026-08-06
4. ~~**Kontakte**~~ ✅ umgesetzt (2026-08-07) — Name/Beschreibung/Connection/Loyalty, BP-Kosten aus allgemeinem Budget, im Karrieremodus frei ohne Karma-Kosten (s. Abschnitt 7 oben)
5. ~~**Charakter-Biografie**~~ ✅ umgesetzt (2026-08-08) — 15 Freitextfelder in `CharacterBiography`, eigener Tab, Live-Text-Binding, Persistenz via `BiographyData`; Mugshot/Portrait bleibt separater Checklisten-Punkt (s. Abschnitt 1 oben)

Damit ist **Stufe 1 (Kernfeatures für einen vollständigen Charakter) komplett abgeschlossen.**

**Stufe 2 – Erweiterte Features**

1. ~~**Magie**~~ ⚠️ teilweise umgesetzt (2026-08-08) — **nur Magier** (Tradition-Auswahl + Zauber-Erlernen): eigener Magie-Tab (nur sichtbar bei Gabe "Magier", funktioniert bereits im Erstellungsmodus), `Tradition`-Enum (Hermetisch/Schamanistisch), Zauber-Katalog mit initialer verifizierter Teilmenge (10 Zauber, 2 je GRW-Kategorie — volle 84er-GRW-Kern-Liste als Folgeauftrag offen), feste Kosten 3 BP/5 Karma, Erstellungs-Cap 2×höchstes Rating(Spruchzauberei, Ritualzauberei), neue magische Fertigkeiten "Spruchzauberei"/"Ritualzauberei" (nur für Magier sichtbar). Bug-Fix mitgenommen: Magier-Quality-Kosten 5→15 BP korrigiert. **Adeptenkräfte + Mystic-Adept-Split bewusst zurückgestellt** (eigenes Folgefeature, User-Entscheidung)
2. ~~**Technomancer**~~ ✅ umgesetzt (Commit `2a2018f`, `docs/plaene/technomancer_plan.md`): Tab, Schwund-Anzeige, Komplexe Formen mit Resonanz-Cap. Offen: Anzahl-Cap Logik × 2 (GRW S. 112, s. Abschnitt 9), Wandlung (GRW-Kern S. 284, zurückgestellt)
3. ~~**Cyberware/Bioware mit Essenz**~~ ⚠️ Kern umgesetzt (2026-09-27, `docs/plaene/cyberware_plan.md`): Grades, Essenz-Kombinationsregel, MAG/RES-Reduktion, Immunabstoßung, eigener Tab. Nuyen-Abbuchung + Verfügbarkeit ✅ (2026-09-27, `docs/plaene/nuyen_ruestung_plan.md`). Offen: vollständiger Katalog, Cybergliedmaßen/Kapazität, Attributboni durch Implantate
4. ~~**Rüstung + Nuyen-Budget + Lebensstil**~~ ✅ umgesetzt (2026-09-27, `docs/plaene/nuyen_ruestung_plan.md`): Budget/Restgeld, GRW-Startkapital, Verfügbarkeit ≤ 12, Rüstung mit Schutzwert und Behinderung. Offen: Kleidung/Lederware, Rüstungsmods, Verkauf, Lebensstilzahlungen im Karrieremodus
5. **Waffen** ✅ Teil A (2026-09-28), Teil B (2026-09-30) und Teil C Zubehör (2026-10-04) umgesetzt (`docs/plaene/waffen_plan.md`)
6. Gear — Verfügbarkeitsregeln bestätigt, Kommlink-Details bei Umsetzung gezielt nachschlagen. **Vorarbeit erledigt (2026-10-04):** alle Kaufkategorien liegen in `model.character.equipment.Equipment` (`character.getEquipment()`, `docs/plaene/character_aufteilung_plan.md`, Sonar S6539) — Gear gehört dorthin, nicht nach `Character`. **Teil A umgesetzt (2026-10-04)**, offen: Teil B (Kommlinks/Matrix), C (Kapazität), D (Munition/Pfeile/Granaten/Sprengstoffe)

**Stufe 3 – Abgeleitete Werte & Gameplay**

1. Würfelpool-Anzeige für Skills — GRW S. 82 exakt bestätigt, fundamentale Kernmechanik des gesamten Systems
2. Condition Monitor — GRW S. 94 exakt bestätigt (inkl. Wundmodifikator-Formel)
3. Initiative-Anzeige — GRW bestätigt, aber Formel bei Umsetzung präzisieren (Initiativattribut vs. vollständiges Würfelergebnis, s. Abschnitt 16)
4. Undo für Log-Einträge — reines Tool-Feature, kein Regelwerk-Bezug zu erwarten (Priorisierung unabhängig vom GRW-Abgleich)
5. **Drucken / Export — Niedrige Priorität**, keine Charakterbogen-Vorlage im GRW gefunden, reines Tool-Feature
