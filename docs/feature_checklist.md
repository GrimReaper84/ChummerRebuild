# Feature-Checkliste: Chummer4 → Java-Rebuild

> Abgleich zwischen dem originalen C#-Verhalten (`chummer4_analyse.md`) und dem aktuellen Java-Implementierungsstand.
>
> **Legende:**
> - ✅ Vollständig und korrekt umgesetzt
> - ⚠️ Teilweise umgesetzt — konkrete Lücken oder Fehler dokumentiert
> - ❌ Nicht umgesetzt

---

## 1. Charaktererstellung (Grundgerüst)

| Feature | Status | Anmerkung |
|---|---|---|
| Neuer Charakter (Name + Metatyp) | ✅ | Dialog vollständig |
| BP-Budget = 400 | ✅ | Konstante korrekt |
| BP-Restanzeige mit Farbwechsel bei Überschreitung | ✅ | |
| Nuyen-Budget in der Erstellung (max. 50 BP × 5.000 ¥) | ❌ | Fehlt komplett; nur Karriere-Restkonvertierung vorhanden |
| Charakter-Biografie (Alias, Geschlecht, Alter, Augen, Größe, Gewicht, Beschreibung, Hintergrund, Konzept, Notizen, Spielername) | ❌ | Nur `name` vorhanden |
| Mugshot / Portrait | ❌ | |

---

## 2. Metatypen

| Feature | Status | Anmerkung |
|---|---|---|
| 5 Grundrassen (Mensch, Elf, Zwerg, Ork, Troll) | ✅ | |
| Metavarianten (aktuell 8) | ⚠️ | Vorhanden, aber Metavarianten-Qualities fehlen komplett (z.B. Nartaki bekommt gratis „Shiva Arms" + „Distinctive Style") |
| Metatyp-BP-Kosten fließen in BP-Bilanz | ✅ | |
| Bewegungsrate (Walking/Running) | ❌ | Nicht gespeichert, nicht angezeigt |
| Metatyp-Kategorie (Metahuman, Shapeshifter, Critter …) | ❌ | Nicht modelliert |

---

## 3. Attribute

| Feature | Status | Anmerkung |
|---|---|---|
| 8 Grundattribute (BOD, AGI, REA, STR, CHA, INT, LOG, WIL) mit Rassengrenzen | ✅ | |
| BP-Kosten Grundattribute: (Wert − Min) × 10 | ✅ | |
| **+15 BP Bonus wenn Attribut auf Rassenmaximum** | ❌ | Fehlt im `getBpCost()` — führt zu systematisch zu niedrigen BP-Kosten bei Maxing |
| Attribut-Minimum durch Rassengrenzen erzwingen | ✅ | |
| Augmentiertes Maximum anzeigen (MetatypeMax + ⌊Max/2⌋) | ❌ | Weder berechnet noch angezeigt |
| Sonderattribute: EDG (Edge) | ❌ | Nicht modelliert |
| Sonderattribute: MAG (Magie) | ❌ | Nicht modelliert |
| Sonderattribute: RES (Resonanz) | ❌ | Nicht modelliert |
| Abgeleitetes Attribut: INI (Initiative = INT + REA) | ❌ | Nicht modelliert |
| Abgeleitetes Attribut: ESS (Essenz = 6 − Implantate) | ❌ | Nicht modelliert |
| Karma-Steigerung Attribute: neue Stufe × 5 Karma | ✅ | `(value+1)×5` ist korrekt für den nächsten Schritt |

---

## 4. Fertigkeiten (Aktive Skills)

| Feature | Status | Anmerkung |
|---|---|---|
| Fertigkeiten mit Gruppen-Kopplung | ✅ | |
| Gruppen-Entkopplung mit Kostenvergleich-Dialog | ✅ | |
| BP-Kosten Einzelfertigkeit: Rating × 4 | ✅ | |
| BP-Kosten Gruppe: Rating × 10 | ✅ | |
| **Erste Karmapunkt einer neuen Fertigkeit kostet 4 Karma (nicht 2)** | ❌ | Java berechnet `(0+1)×2 = 2 Karma` — C# hat `KarmaNewActiveSkill = 4` für den ersten Punkt |
| Spezialisierungen (+2 BP / +2 Karma) | ❌ | Nicht modelliert |
| Uneducated (Technical Active doppelt teuer) | ❌ | Qualität und Kostenlogik fehlen |
| Uncouth (Social Active doppelt teuer) | ❌ | Qualität und Kostenlogik fehlen |
| Infirm (Physical Active doppelt teuer) | ❌ | Qualität und Kostenlogik fehlen |
| Default-Flag (erlaubt Würfeln ohne Rating: Attribut − 1) | ❌ | Flag im Modell fehlt, keine Würfelpool-Anzeige |
| Würfelpool-Anzeige (Rating + Attribut + Modifikatoren) | ❌ | Nicht vorhanden |
| Exotische Fertigkeiten (Spez = Gegenstand, kein eigener Rating) | ❌ | |
| Kategorie-Anzeige im UI | ✅ | Gruppiert nach Kategorie sichtbar |

---

## 5. Wissensfertigkeiten (Knowledge Skills)

| Feature | Status | Anmerkung |
|---|---|---|
| Wissensfertigkeiten-Pool (INT × 2 freie Punkte) | ❌ | Komplett fehlend |
| Hinzufügen/Entfernen von Wissensfertigkeiten | ❌ | |
| Kategorie (Akademisch, Interesse, Sprache, Beruflich, Straße) | ❌ | |
| BP-Kosten: 2 BP/Punkt (nur wenn Pool überschritten) | ❌ | |

---

## 6. Qualities (Vorteile & Nachteile)

| Feature | Status | Anmerkung |
|---|---|---|
| Positive Qualities kaufen | ❌ | Komplett fehlend |
| Negative Qualities nehmen | ❌ | |
| BP-Kosten/Gewinn fließen in BP-Bilanz | ❌ | |
| Limit: max. 25 BP positive / 25 BP negative Qualities (konfigurierbar) | ❌ | |
| Metatyp-Qualities (grau, nicht entfernbar) | ❌ | Besonders wichtig für Metavarianten |
| Bonus-System (z.B. Uneducated, Uncouth setzt Flags) | ❌ | |

---

## 7. Kontakte

| Feature | Status | Anmerkung |
|---|---|---|
| Kontakt hinzufügen (Name, Loyalität, Verbindung) | ❌ | Komplett fehlend |
| BP-Kosten: 1 BP pro Kontaktpunkt (Loyalität + Verbindung) | ❌ | |
| Freie Kontaktpunkte: CHA × 2 | ❌ | |

---

## 8. Magie

| Feature | Status | Anmerkung |
|---|---|---|
| MAG-Attribut aktivieren (via Quality) | ❌ | Abhängig von fehlenden Qualities & MAG-Attribut |
| Magier-Tab (Tradition, Zauber) | ❌ | |
| Adept-Tab (Adeptenkräfte, Power Points = MAG) | ❌ | |
| Zauber hinzufügen (3 BP/Zauber) | ❌ | |
| Adeptenkräfte mit PP-Kosten | ❌ | |
| Initiationsgrade | ❌ | |

---

## 9. Technomancer

| Feature | Status | Anmerkung |
|---|---|---|
| RES-Attribut aktivieren (via Quality) | ❌ | |
| Technomancer-Tab (Stream, Komplexe Formen) | ❌ | |
| Komplexe Formen (1 BP/Stück) | ❌ | |
| Submersiongrade | ❌ | |

---

## 10. Cyberware / Bioware

| Feature | Status | Anmerkung |
|---|---|---|
| Cyberware kaufen (Nuyen-Kosten) | ❌ | Komplett fehlend |
| Essenz-Kosten berechnen (Cyberware vs. Bioware getrennt) | ❌ | |
| Essenz-Berechnung: höhere Kategorie voll, niedrigere halbiert | ❌ | |
| Grades (Standard, Alphaware, Betaware, Deltaware) | ❌ | |
| Subsystem-Kapazität | ❌ | |
| Augmented Maximum durch Implantate (Augmented Modifier) | ❌ | |

---

## 11. Rüstung

| Feature | Status | Anmerkung |
|---|---|---|
| Rüstung kaufen und ausrüsten | ❌ | Komplett fehlend |
| Ballistik/Impact-Berechnung (Stacking-Regeln) | ❌ | |
| Tragebelastung (Armor Encumbrance) | ❌ | |

---

## 12. Waffen

| Feature | Status | Anmerkung |
|---|---|---|
| Waffen kaufen und verwalten | ❌ | Komplett fehlend |
| Modifikationen, Zubehör | ❌ | |

---

## 13. Ausrüstung (Gear)

| Feature | Status | Anmerkung |
|---|---|---|
| Allgemeine Ausrüstung kaufen | ❌ | Komplett fehlend |
| Commlink mit Response/Signal | ❌ | |
| Orte/Lager-Kategorien | ❌ | |

---

## 14. Fahrzeuge

| Feature | Status | Anmerkung |
|---|---|---|
| Fahrzeuge kaufen und verwalten | ❌ | Komplett fehlend |

---

## 15. Karrieremodus

| Feature | Status | Anmerkung |
|---|---|---|
| Übergang Creation → Career (BP → Nuyen-Konvertierung) | ⚠️ | Konvertierung funktioniert, aber: alle verbleibenden BP werden in Nuyen gewandelt statt nur der explizit zugewiesenen. Kein expliziter Nuyen-BP-Slot in der Erstellung vorhanden (siehe Feature 1) |
| ESS-Penalty-Korrektur bei Übergang (MAG/RES += EssenzPenalty) | ❌ | Abhängig von fehlenden Sonderattributen |
| Karma-Ausgaben für Attribute (neue Stufe × 5) | ✅ | |
| Karma-Ausgaben für Fertigkeiten (neue Stufe × 2, erste Stufe 4 Karma) | ⚠️ | Erste Stufe kostet falsch 2 statt 4 Karma |
| Karma/Nuyen-Log mit Datum und Undo | ⚠️ | Log vorhanden, Undo fehlt |
| Expense-Typen (z.B. ImproveAttribute, AddQuality …) | ❌ | Alle Log-Einträge sind freie Texte, keine Typisierung |

---

## 16. Initiativberechnung

| Feature | Status | Anmerkung |
|---|---|---|
| Physische Initiative = INT + REA (+ Modifikatoren) | ❌ | |
| Astrale Initiative = INT × 2 | ❌ | |
| Matrix-Initiative (mit Commlink oder Technomancer) | ❌ | |
| Initiative Passes anzeigen | ❌ | |

---

## 17. Condition Monitor

| Feature | Status | Anmerkung |
|---|---|---|
| Physischer CM = 8 + ⌈BOD/2⌉ | ❌ | |
| Stun CM = 8 + ⌈WIL/2⌉ | ❌ | |
| CM-Boxen anklicken und füllen | ❌ | |
| Wundmodifikator berechnen (−1 pro 3 Boxen) | ❌ | |

---

## 18. Persistenz (Speichern/Laden)

| Feature | Status | Anmerkung |
|---|---|---|
| JSON-Speicherung und -Laden (Grundstruktur) | ✅ | |
| Attribute werden korrekt gespeichert/geladen | ⚠️ | Nur 8 Grundattribute; EDG, MAG, RES fehlen im Modell |
| Skills werden korrekt gespeichert/geladen (nur Rating > 0) | ✅ | |
| Entkoppelte Gruppen werden gespeichert/geladen | ✅ | |
| Karma/Nuyen-Log wird gespeichert/geladen mit Datum | ✅ | |
| Dateiendung `.json` | ✅ | |
| Vorgeschlagener Dateiname beim Speichern (charname.json) | ✅ | |

---

## 19. UI / Bedienbarkeit

| Feature | Status | Anmerkung |
|---|---|---|
| Tastaturkürzel (Ctrl+N/O/S/Shift+S/Q) | ✅ | |
| Bestätigungsdialog bei Karma-Ausgaben | ✅ | |
| Disabled-Zustand von Buttons korrekt (BP/Karma zu wenig, Grenzwerte) | ✅ | |
| Metatyp-Wechsel mit Attribut-Reset in der Erstellung | ✅ | |
| Drucken / Export / Charakterbogen | ❌ | |

---

## Zusammenfassung

| Bereich | ✅ | ⚠️ | ❌ |
|---|---|---|---|
| Erstellungsgrundgerüst | 2 | 0 | 4 |
| Metatypen | 2 | 1 | 2 |
| Attribute | 2 | 1 | 6 |
| Aktive Fertigkeiten | 4 | 1 | 5 |
| Wissensfertigkeiten | 0 | 0 | 4 |
| Qualities | 0 | 0 | 5 |
| Kontakte | 0 | 0 | 3 |
| Magie | 0 | 0 | 6 |
| Technomancer | 0 | 0 | 4 |
| Cyberware/Bioware | 0 | 0 | 6 |
| Rüstung | 0 | 0 | 3 |
| Waffen | 0 | 0 | 2 |
| Gear | 0 | 0 | 3 |
| Fahrzeuge | 0 | 0 | 1 |
| Karrieremodus | 2 | 2 | 2 |
| Initiative | 0 | 0 | 4 |
| Condition Monitor | 0 | 0 | 4 |
| Persistenz | 5 | 1 | 1 |
| UI/Bedienbarkeit | 5 | 0 | 1 |

---

## Prioritisierte Reihenfolge für nächste Schritte

**Stufe 1 – Bestehende Features fertigstellen (⚠️ → ✅)**

1. **+15 BP Bonus** für Attribut auf Rassenmaximum in `CharacterAttribute.getBpCost()` korrigieren
2. **Erster Fertigkeit-Karmapunkt** kostet 4 Karma statt 2 (in `Skill.getKarmaCost()`)
3. **Nuyen-Budget in der Erstellung** (max. 50 BP → bis zu 250.000 ¥, explizite Zuweisung, nicht automatische Restkonvertierung)
4. **Metavariant-Qualities** (gratis Positiv/Negativ-Qualities beim Metatyp)

**Stufe 2 – Fehlende Kernfeatures für einen vollständigen Charakter**

5. **Qualities** (Vorteile/Nachteile mit BP-Kosten, Limit, Bonus-Flags wie Uneducated/Uncouth)
6. **Wissensfertigkeiten** (freier Pool INT×2, Kategorien, Überzug in BP)
7. **Sonderattribute** EDG, MAG, RES + Essenz-Berechnung (Grundlage für alles weitere)
8. **Spezialisierungen** (+2 BP / Karma)
9. **Kontakte**
10. **Charakter-Biografie** (alle Textfelder)

**Stufe 3 – Erweiterte Features**

11. Magie (MAG-Aktivierung, Tradition, Zauber, Adeptenkräfte)
12. Technomancer (RES-Aktivierung, Stream, Komplexe Formen)
13. Cyberware/Bioware mit Essenz
14. Rüstung mit Stacking-Regeln
15. Waffen
16. Gear

**Stufe 4 – Abgeleitete Werte & Gameplay**

17. Initiative-Anzeige
18. Condition Monitor
19. Würfelpool-Anzeige für Skills
20. Drucken / Export
