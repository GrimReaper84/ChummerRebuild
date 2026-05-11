# Chummer4 – Vollständige C#-Codeanalyse

> **Zweck dieser Datei:** Migrationswissensbasis für den Java-Rebuild. Sie beschreibt das gesicherte Ist-Verhalten des originalen Chummer4-Codes, sodass in zukünftigen Gesprächen nicht mehr auf den Originalcode zugegriffen werden muss.
>
> **Quelle:** `/home/luca/Chummer4_OriginalCode/chummer/Chummer/`
> **Analysedatum:** 2026-05-11

---

## Projektüberblick

### Zweck
Chummer4 ist ein Windows-Forms-Charakter-Erstellungs- und -Verwaltungsprogramm für das Pen-and-Paper-Rollenspiel **Shadowrun 4. Edition**. Die Applikation unterstützt zwei Phasen:
- **Character Creation (BP-Modus):** Charaktererstellung mit 400 Build Points (BP)
- **Career Mode (Karma-Modus):** Weiterentwicklung des fertiggestellten Charakters mit Karma

### Solution-Struktur
- **Einzelprojekt:** `Chummer.csproj` in `ChummerCS.sln`
- **Namespace:** `Chummer`
- **Einstiegspunkt:** `Program.cs` → `frmMain`
- **Zielframework:** Windows Forms (.NET), C#

### Zentrale Quelldateien

| Datei | Zeilen | Rolle |
|---|---|---|
| `frmCareer.cs` | 26.950 | UI Career Mode, riesige Monolith-Form |
| `frmCreate.cs` | 23.122 | UI Character Creation |
| `clsEquipment.cs` | 15.949 | Ausrüstungsklassen (Cyberware, Waffen, Rüstung, Fahrzeuge, Gear) |
| `clsUnique.cs` | 8.606 | Attribute, Skill, SkillGroup, Quality, Spirit, Spell, Power, Contact |
| `clsCharacter.cs` | 6.005 | Character-Hauptklasse: Daten, Save/Load, Berechnungen |
| `clsImprovement.cs` | 2.818 | Improvement-Datenklasse + ImprovementManager |
| `clsOptions.cs` | 3.951 | GlobalOptions (Singleton) + CharacterOptions |
| `clsCommon.cs` | 1.354 | CommonFunctions (Hilfsmethoden für Suche/Traversal) |
| `clsExpenses.cs` | ~300 | Expense-Log-Einträge (Karma/Nuyen) |
| `controllers/clsMainController.cs` | 1.043 | Drag-and-Drop-Logik, Mentorengeist, Kontakte |

---

## Datenmodell: `Character`

**Datei:** `clsCharacter.cs`

### Rolle
Zentrales Domain-Objekt. Hält alle Charakterdaten, bietet Save/Load via XML, berechnet abgeleitete Werte.

### Felder (Kernauswahl)

| Feld | Typ | Bedeutung |
|---|---|---|
| `_strFileName` | `string` | Pfad zur gespeicherten XML-Datei |
| `_strSettingsFileName` | `string` | Name der Einstellungsdatei (default: `default.xml`) |
| `_blnCreated` | `bool` | **true** = Career Mode (Charakter ist fertiggestellt) |
| `_intBuildPoints` | `int` | Gesamt-BP für Erstellung (Standard: 400) |
| `_objBuildMethod` | `CharacterBuildMethod` | `BP` oder `Karma` |
| `_intKarma` | `int` | Verfügbares Karma (Career Mode) |
| `_intTotalKarma` | `int` | Gesammt-Karma seit Karrierebeginn |
| `_intNuyen` | `int` | Aktuelles Nuyen-Guthaben |
| `_decNuyenBP` | `decimal` | BP/Karma in Nuyen umgerechnet |
| `_decNuyenMaximumBP` | `decimal` | Maximale BP/Karma für Nuyen (Standard: 50) |
| `_intKnowledgeSkillPoints` | `int` | Freie Wissensfertigkeitspunkte |
| `_intMetatypeBP` | `int` | BP-Kosten des Metatyps |
| `_blnMAGEnabled` | `bool` | MAG-Attribut freigeschaltet |
| `_blnRESEnabled` | `bool` | RES-Attribut freigeschaltet |
| `_blnAdeptEnabled` | `bool` | Adept-Tab sichtbar |
| `_blnMagicianEnabled` | `bool` | Magier-Tab sichtbar |
| `_blnTechnomancerEnabled` | `bool` | Technomancer-Tab sichtbar |
| `_blnInitiationEnabled` | `bool` | Initiations-Tab manuell erzwungen |
| `_blnCritterEnabled` | `bool` | Critter-Tab sichtbar |
| `_blnUneducated` | `bool` | Quality: Technical Active Fertigkeiten doppelt so teuer |
| `_blnUncouth` | `bool` | Quality: Social Active Fertigkeiten doppelt so teuer |
| `_blnInfirm` | `bool` | Quality: Physical Active Fertigkeiten doppelt so teuer |
| `_blnIsCritter` | `bool` | Charakter ist ein Critter (kein normaler Metavolker) |
| `_blnPossessed` | `bool` | Charakter ist besessen |
| `_blnBlackMarket` | `bool` | Quality: Schwarzmarkt-Rabatt |
| `_intInitiateGrade` | `int` | Initiationsstufe des Magiers |
| `_intSubmersionGrade` | `int` | Submersionsstufe des Technomancers |
| `_decEssenceAtSpecialStart` | `decimal` | Essenz zum Zeitpunkt der MAG/RES-Aktivierung |
| `_intPhysicalCMFilled` | `int` | Gefüllte physische Condition-Monitor-Boxen |
| `_intStunCMFilled` | `int` | Gefüllte Stun-Condition-Monitor-Boxen |
| `_intStreetCred` | `int` | Street Cred |
| `_intNotoriety` | `int` | Notoriety |
| `_intPublicAwareness` | `int` | Public Awareness |
| `_intBurntStreetCred` | `int` | Verbrauchte Street Cred |
| `_intMaxAvail` | `int` | Maximale Verfügbarkeit (Standard: 12) |
| `_intResponse` | `int` | Response für A.I.-Charaktere |
| `_intSignal` | `int` | Signal für A.I.-Charaktere |
| `_intMAGMagician` | `int` | MAG-Anteil des Magiers bei Mystic Adepts |
| `_intMAGAdept` | `int` | MAG-Anteil des Adepten bei Mystic Adepts |

### Listen (vollständig)

```
Improvements, Skills, SkillGroups, Contacts, Spirits, Spells, Foci,
StackedFoci, Powers, TechPrograms, MartialArts, MartialArtManeuvers,
Armor, Cyberware, Weapons, Qualities, Lifestyles, Gear, Vehicles,
Metamagics, ExpenseLog, CritterPowers, InitiationGrades,
OldQualities (string), Locations (string), ArmorBundles (string),
WeaponLocations (string), ImprovementGroups (string), Calendar
```

### Events (Delegates)

| Event | Auslöser |
|---|---|
| `MAGEnabledChanged` | MAG wird aktiviert/deaktiviert |
| `RESEnabledChanged` | RES wird aktiviert/deaktiviert |
| `AdeptTabEnabledChanged` | Adept-Tab ein/aus |
| `MagicianTabEnabledChanged` | Magier-Tab ein/aus |
| `TechnomancerTabEnabledChanged` | Technomancer-Tab ein/aus |
| `InitiationTabEnabledChanged` | Initiations-Tab ein/aus |
| `CritterTabEnabledChanged` | Critter-Tab ein/aus |
| `UneducatedChanged` | Quality Uneducated geändert |
| `UncouthChanged` | Quality Uncouth geändert |
| `InfirmChanged` | Quality Infirm geändert |
| `CharacterNameChanged` | Charaktername geändert |
| `BlackMarketEnabledChanged` | Schwarzmarkt-Quality geändert |

---

## Persistenz: `Character.Save()` / `Character.Load()`

**Status:** Gesichert

### Format
- **XML**, proprietäres Format
- Zeichenkodierung: Unicode
- Einrückung: Tab-basiert
- Wurzelelement: `<character>`
- Spieledition-Tag: `<gameedition>SR4</gameedition>`

### Save-Reihenfolge
1. Metadaten (appversion, gameedition, settings)
2. Metatyp-Informationen (metatype, metatypebp, metavariant, metatypecategory, movement)
3. Allgemeine Charakterinfos (name, mugshot als Base64-String, sex, age, eyes, height, weight, skin, hair, description, background, concept, notes, alias, playername, gamenotes)
4. Flags (ignorerules, iscritter, possessed, overridespecialattributeessloss)
5. Ressourcen (karma, totalkarma, streetcred, notoriety, publicawareness, burntstreetcred, maxavail, nuyen)
6. Build (bp, buildkarma, buildmethod, knowpts, nuyenbp, nuyenmaxbp)
7. Mode-Flags (adept, magician, technomancer, initiationoverride, critter, uneducated, uncouth, infirm, blackmarket)
8. Attribute (BOD, AGI, REA, STR, CHA, INT, LOG, WIL, INI, EDG, MAG, RES, ESS; bei A.I. zusätzlich response, signal)
9. Sonderflags (magenabled, initiategrade, resenabled, submersiongrade, groupmember, groupname, groupnotes)
10. Berechneter Essenzwert (totaless) als redundante Info für externe Reader
11. Mystic-Adept-Split (magsplitadept, magsplitmagician)
12. Tradition/Stream
13. Condition Monitor (physicalcmfilled, stuncmfilled)
14. Listen: skillgroups → skills → contacts → spells → foci → stackedfoci → powers → spirits → techprograms → martialarts → martialartmaneuvers → armors → weapons → cyberwares → qualities → lifestyles → gears → vehicles → metamagics → critterpowers → initiationgrades → improvements → expenses → locations → armorbundles → weaponlocations → improvementgroups → calendar

### Load-Besonderheit
- Nahezu alle optionalen Felder werden via `try/catch` geladen (kein explizites Null-Checking)
- **Risiko:** `try { ... } catch { }` verschluckt alle Ausnahmen still. Korrupte Teilfelder werden ignoriert.
- Fixup bei `_decEssenceAtSpecialStart`: Wenn der Wert > EssenceMaximum, wird er durch 10 dividiert (historischer Parsing-Bug).
- Versionsprüfung: Wenn `<gameedition>` nicht leer und nicht "SR4", wird `false` zurückgegeben und Fehlermeldung angezeigt.
- Load ruft intern `ResetCharacter()` auf, bevor Daten geladen werden.

### Gear-Sonderfall beim Speichern
```csharp
if (objGear.GetType() == typeof(Commlink)) // Downcast via typeof-Check
    ((Commlink)objGear).Save(objWriter);
else if (objGear.GetType() == typeof(OperatingSystem))
    ((OperatingSystem)objGear).Save(objWriter);
else
    objGear.Save(objWriter);
```
**Status:** Risiko. Typprüfung via `typeof` statt `is`-Pattern oder Polymorphismus. Subklassen von Commlink werden nicht erkannt.

---

## Datenmodell: `Attribute`

**Datei:** `clsUnique.cs`, Zeile 14

### Felder

| Feld | Typ | Bedeutung |
|---|---|---|
| `_strAbbrev` | `string` | Kürzel: BOD, AGI, REA, STR, CHA, INT, LOG, WIL, INI, EDG, MAG, RES, ESS |
| `_intMetatypeMin` | `int` | Rassenminimum |
| `_intMetatypeMax` | `int` | Rassenmaximum |
| `_intMetatypeAugMax` | `int` | Augmentiertes Rassenmaximum |
| `_intValue` | `int` | Aktueller Basiswert (gespeichert) |
| `_intAugModifier` | `int` | Augmentierungsmodifikator (wird bei Load neu berechnet) |

### Berechnungen

#### `TotalValue`
```
TotalValue = Value + AttributeModifiers
```
- Für BOD/AGI/STR: Cyberlimb-Durchschnittsberechnung wenn Cyberlimbs vorhanden
  - Formel: `floor((Summe aller Limb-Werte) / Anzahl Limbs)`
  - Nicht ersetzte Limbs werden mit dem natürlichen Wert (intMeat) aufgefüllt
- Wird auf TotalAugmentedMaximum nach oben begrenzt
- Minimum: 1 (außer bei Critter, EDG, MAG/RES mit EssenzPenalty, oder MetatypeMax = 0)
- Cyberzombie-Sonderfall: MAG ist immer 1

#### `TotalMinimum`
```
TotalMinimum = MetatypeMinimum + MinimumModifiers
```
- Minimum 1 (außer Critter oder MetatypeMax = 0)
- Mit EssenzPenalty und `ESSLossReducesMaximumOnly`-Option:
  - Wenn Option aktiv: Minimum = 0 wenn EssenzPenalty ≥ TotalMaximum; sonst 1
  - Wenn Option inaktiv: Minimum = 0

#### `TotalMaximum`
```
TotalMaximum = MetatypeMaximum + MaximumModifiers
```

#### `TotalAugmentedMaximum`
- Für EDG, MAG, RES: `TotalMaximum + AugmentedMaximumModifiers`
- Für alle anderen: `TotalMaximum + floor(TotalMaximum / 2) + AugmentedMaximumModifiers`

#### `AttributeModifiers`
Summiert Improvements vom Typ `Attribute` mit `ImprovedName == Abbrev`:
- `Augmented * Rating` wird addiert
- UniqueName-Logik: Gleichnamige Improvements → nur der höchste Wert zählt
- `precedence0`: Nur höchster Wert; `precedence1`: Summe aller precedence1-Werte
- Custom-Improvements werden separat berechnet und addiert

#### `AttributeValueModifiers`
Wie AttributeModifiers, aber sucht nach `ImprovedName == Abbrev + "Base"`. Diese Modifikatoren erhöhen den Basiswert und damit die Karma-Kosten.

#### `CalculatedBP()` – BP-Kosten eines Attributs

**BP-Modus:**
- Kosten pro Punkt: 10 BP (`BPAttribute` = 10)
- Formel: `(Value - TotalMinimum) * 10`
- Bonus: Wenn Value == TotalMaximum: +15 BP (`BPAttributeMax` = 15)
- Sonderfall: Wenn TotalMinimum == TotalMaximum → kein Bonus
- Für EDG, MAG, RES: EssenzPenalty wird berücksichtigt

**Karma-Modus:**
- Formel: `Summe von i * KarmaAttribute` für i von (TotalMinimum + 1) bis Value
- KarmaAttribute = 5
- Alternativmethode (`AlternateMetatypeAttributeKarma`): Startet immer bei 1

---

## Datenmodell: `SkillGroup`

**Datei:** `clsUnique.cs`, Zeile 1554

### Felder

| Feld | Typ | Bedeutung |
|---|---|---|
| `_strName` | `string` | Fertigkeitsgruppenname |
| `_intRating` | `int` | Aktuelles Rating der Gruppe |
| `_intRatingMaximum` | `int` | Maximales Rating (Standard: 6) |
| `_blnBroken` | `bool` | true = Gruppe wurde durch Einzelskill-Kauf aufgebrochen |

**Hinweis:** SkillGroup kennt keine eigenen Skill-Referenzen. Zugehörigkeit von Skills zur Gruppe wird aus `skills.xml` gelesen.

---

## Datenmodell: `Skill`

**Datei:** `clsUnique.cs`, Zeile 1776

### Felder

| Feld | Typ | Bedeutung |
|---|---|---|
| `_strName` | `string` | Fertigkeitsname |
| `_strSkillGroup` | `string` | Gruppenname |
| `_strSkillCategory` | `string` | Kategorie (z.B. "Combat Active", "Technical Active") |
| `_blnIsGrouped` | `bool` | true = Fertigkeit ist Teil einer intakten Gruppe |
| `_blnDefault` | `bool` | true = defaulting erlaubt (Würfelpool = Attribut - 1) |
| `_intRating` | `int` | Aktuelles Rating |
| `_intRatingMaximum` | `int` | Maximales Rating (Standard: 6) |
| `_blnKnowledgeSkill` | `bool` | true = Wissensfertigkeit |
| `_blnExoticSkill` | `bool` | true = Exotische Fertigkeit (Spez = Fertigkeitsgegenstand) |
| `_strSkillSpec` | `string` | Spezialisierung |
| `_blnAllowDelete` | `bool` | true = kann gelöscht werden (Wissensfertigkeiten) |
| `_strAttribute` | `string` | Verknüpftes Attribut |
| `_strSource` | `string` | Quellbuch-Kürzel |
| `_strPage` | `string` | Seitennummer |

### Berechnungen

#### `TotalRating` (Würfelpool)
```
TotalRating = CalculatedRating + RatingModifiers + DicePoolModifiers + Attribut.TotalValue + WoundModifiers
```
- Defaulting: `Attribut - 1` (wenn Rating == 0 und Default == true)
- Attributtausch via `SwapSkillAttribute`-Improvement möglich (BOD↔WIL, AGI↔LOG, REA↔INT, STR↔CHA)
- Mystic Adept: Fertigkeiten mit MAG nutzen `MAGMagician` statt `MAG.TotalValue`
- Rating-Cap: Wenn `EnforceMaximumSkillRatingModifier`: Modifikatoren (ohne Attr.) auf max `floor(Rating * 1.5)` begrenzt
- Pool-Cap: Wenn `CapSkillRating`: `min(max(20, (NatürlichesAttribut + Rating) * 2), TotalRating)`
- Skills ohne Default und Rating 0 → TotalRating = 0 (kein negativer Pool)

#### `CalculatedRating`
Berücksichtigt Skillsoft (Activesoft, Knowsoft, Linguasoft):
- Wenn Skillwire aktiv und passender Skillsoft ausgerüstet mit höherem Rating
- Activesoft: `min(Skillsoft.Rating, Skillwire.Rating)`
- Knowsoft/Linguasoft: Skillsoft.Rating ohne Skillwire-Limit

#### `RatingModifiers`
Improvements mit `AddToRating = true` für:
- Einzelne Fertigkeit (`Skill` mit ImprovedName == Name)
- Fertigkeitsgruppe (`SkillGroup` mit ImprovedName == Gruppenname)
- Fertigkeitskategorie (`SkillCategory` mit ImprovedName == Kategorie)
- Attribute-basiert (`SkillAttribute` mit ImprovedName == Attribut)
- Enhanced Articulation: Für Physical Active-Fertigkeiten mit BOD/AGI/REA/STR

#### `DicePoolModifiers`
Improvements mit `AddToRating = false`:
- Gleiche Quellen wie RatingModifiers, aber nur als Würfelpool-Bonus
- UniqueName-Logik: Nur höchster Wert pro UniqueName
- Custom-Improvements werden separat summiert und addiert

#### `CalculatedBP()` – BP/Karma-Kosten einer Fertigkeit

**BP-Modus:**
- Kosten: `Rating * 4 BP` (`BPActiveSkill` = 4)
- Bei Rating > 6: Zusätzlich `(Rating - 6) * 4 BP` (Kosten verdoppelt)
- Bei Uneducated (Technical), Uncouth (Social), Infirm (Physical): Alles × 2
- Spezialisierung: +2 BP (`BPActiveSkillSpecialization`)
- Nur für ungegruppte Skills (`_blnIsGrouped == false`)

**Karma-Modus:**
- Erste Punkt: `KarmaNewActiveSkill` (= 4 Karma)
- Jeder weitere: `i * KarmaImproveActiveSkill` (= 2 Karma) für i von 2 bis Rating
- Bei Rating > 6: Jeder Punkt über 6 nochmals × 2
- Spezialisierung: `KarmaSpecialization` Karma
- Uneducated/Uncouth/Infirm-Malus analog

---

## Fachregeln: Essenz

**Status:** Gesichert

### `Character.Essence`
```
ESS = MetatypeMaximum + ImprovementValue(Essence)
    - max(CyberwareESS, BiowareESS/2) - min(CyberwareESS/2, BiowareESS)
    - EssenceHole
```
Vereinfacht:
- Cyberware und Bioware werden **getrennt** berechnet
- Die höhere der beiden Kategorien zieht vollen Wert ab; die niedrigere nur die Hälfte
- Essence Hole zieht immer voll ab
- Sonderfall: Wenn `CyborgEssence`-Improvement aktiv → ESS = 0.1 (fix)

### `Character.EssencePenalty`
```
EssencePenalty = ceil(EssenceAtSpecialStart + ImprovementValue(EssenceMax) - Essence)
```
- Dieser Wert wird von MAG und RES abgezogen (außer bei Housrule `ESSLossReducesMaximumOnly`)

### `EssenceAtSpecialStart`
- Wird beim ersten Aktivieren von MAG/RES gesetzt
- Beim Übergang zu Career Mode wird EssenzPenalty zur MAG/RES-Value addiert, damit der angezeigte Wert korrekt bleibt

---

## Fachregeln: Rüstung (Ballistic/Impact)

**Status:** Gesichert

### Grundregel
- Höchster einzelner Rüstungswert (nicht kumulativ), außer für Kleidung und `+`-Rüstungen
- `+`-Rüstungen stapeln auf die höchste Rüstung
- Kleidungsrüstungen (`+`) stapeln untereinander, aber nur wenn höher als die Hauptrüstung
- Form-Fitting Armor: zählt für Stapel mit jeder Rüstung (auch wenn niedriger)
- `TotalBallisticArmorRating` = Höchster + Stacking (nicht Form-Fitting, nicht `+`) + Improvements
- `TotalImpactArmorRating` analog

### Tragebelastung (Armor Encumbrance)
- Schwelle: `BOD * 2` (bei Military Grade: `BOD * 3`)
- Alternative Hausregel: `(BOD * (Multiplikator-1)) + STR`
- Wenn Gesamtballistic > Schwelle: Strafmodifikator = `-ceil((Total - Schwelle) / 2)`
- Form-Fitting Armor zählt als halber Ballistic-Wert
- SoftWeave: Reduziert effektiven Ballistic um `min(HighestBallistic, STR)`
- Hausregel `NoSingleArmorEncumbrance`: Bei nur einer Rüstung kein Malus
- Hausregel `IgnoreArmorEncumbrance`: Immer 0

---

## Improvement-System

**Dateien:** `clsImprovement.cs`

### Zweck
Zentrales Bonus-/Malus-System. Alle Boni von Qualities, Cyberware, Bioware, Ausrüstung, Zaubern etc. werden als `Improvement`-Objekte in der Charakterliste gespeichert.

### Datenmodell `Improvement`

| Feld | Typ | Bedeutung |
|---|---|---|
| `_strImprovedName` | `string` | Name des verbesserten Objekts (z.B. Attributkürzel oder Skill-Name) |
| `_strSourceName` | `string` | GUID oder Name des gewährenden Objekts |
| `_intMin` | `int` | Minimummodifikator |
| `_intMax` | `int` | Maximummodifikator |
| `_intAug` | `int` | Augmentierungsmodifikator |
| `_intAugMax` | `int` | Augmentiertes-Maximum-Modifikator |
| `_intVal` | `int` | Wertmodifikator |
| `_intRating` | `int` | Rating-Multiplikator (Standard: 1) |
| `_strExclude` | `string` | Kommagetrennte Ausnahmeliste (z.B. einzelne Skills aus Gruppen-Bonus ausschließen) |
| `_strUniqueName` | `string` | Unique-Name-Gruppe (nur höchster Wert zählt) |
| `_blnCustom` | `bool` | Manuell erstelltes Improvement |
| `_blnAddToRating` | `bool` | Modifikator geht in das Rating (statt nur den Würfelpool) |
| `_blnEnabled` | `bool` | Ist das Improvement aktiv |
| `_intOrder` | `int` | Sortierreihenfolge für Custom-Improvements |

### ImprovementType (89 Typen, Auswahl)
```
Skill, Attribute, BallisticArmor, ImpactArmor, Reach, Nuyen, Essence,
Reaction, PhysicalCM, StunCM, UnarmedDV, SkillGroup, SkillCategory,
SkillAttribute, Initiative, InitiativePass, MatrixInitiative, Uneducated,
Uncouth, Infirm, Skillwire, SpecialTab, CyberwareEssCost, BiowareEssCost,
GenetechCostMultiplier, AdeptPowerPoints, FreeSpiritPowerPoints,
FreePositiveQualities, FreeNegativeQualities, NuyenMaxBP, CMOverflow,
ArmorEncumbrancePenalty, LivingPersonaResponse/Signal/Firewall/System/Biofeedback,
EssenceMax, CyborgEssence, MovementPercent, SwimPercent, FlyPercent, FlySpeed,
JudgeIntentions, LiftAndCarry, Memory, Composure, DamageResistance,
BlackMarketDiscount, SpellCategory, ComplexFormLimit, SpellLimit,
QuickeningMetamagic, ThrowRange, SkillsoftAccess, AddSprite, SelectWeapon,
SwapSkillAttribute, DrainResistance, FadingResistance, Initiation, Submersion,
IgnoreCMPenaltyStun, IgnoreCMPenaltyPhysical, AdaptSin, SoftWeave, Smartlink...
```

### ImprovementSource (27 Quellen)
```
Quality, Power, Metatype, Cyberware, Metavariant, Bioware, Nanotech, Genetech,
ArmorEncumbrance, Gear, Spell, MartialArtAdvantage, Initiation, Submersion,
Metamagic, Echo, Armor, ArmorMod, EssenceLoss, ConditionMonitor, CritterPower,
ComplexForm, EdgeUse, MutantCritter, Cyberzombie, StackedFocus, AttributeLoss, Custom
```

### `ImprovementManager.ValueOf()`

**Status:** Gesichert

Berechnet den Gesamtwert eines Improvement-Typs für den Charakter:

1. Filtert: `Enabled == true` und `Custom == false`
2. Technomancer-Ausnahme: `MatrixInitiativePass` von Gear wird ignoriert
3. AddToRating-Filter: Nur passende `blnAddToRating`-Werte
4. ImprovedName-Filter: Wenn übergeben, nur passende Namen
5. UniqueName-Logik: Nur höchster Wert pro UniqueName-Gruppe
6. `precedence1`: Alle precedence1-Werte werden summiert (ersetzt andere nicht-precedence Werte)
7. `precedence0`: Nur der höchste precedence0-Wert; alle anderen werden ignoriert
8. Custom-Improvements werden separat berechnet (gleiches Verfahren) und addiert

**Risiko:** UniqueName-Suchalgorithmus enthält einen Bug:
```csharp
foreach (string strName in lstUniqueName) {
    if (strName == objImprovement.UniqueName)
        blnFound = true;
    break;  // ← break ist AUSSERHALB des if, immer nach erstem Element!
}
```
Der `break` bricht nach dem ersten Element ab, unabhängig vom Vergleich. Das bedeutet: Wenn das erste Element in der Liste nicht übereinstimmt, wird `blnFound` nie gesetzt. Bug. **Nicht übernehmen ohne Prüfung.**

### `ImprovementManager.CreateImprovements()`

**Status:** Gesichert

Liest `<bonus>`-XML-Nodes aus Daten-XMLs und erzeugt passende Improvements.

Unterstützte Bonus-Nodes (Auswahl):
- `<selecttext>` → öffnet Dialogfenster, speichert als `Text`-Improvement
- `<selectrestricted>` → öffnet Auswahldialog für Restricted-Items
- `<selectskill>` → Skill-Auswahl mit Filteroptionen (skillgroup, skillcategory, excludecategory, limittoskill)
- `<addattribute>` → aktiviert MAG oder RES
- `<enabletab>` → aktiviert Sonder-Tab (magician, adept, technomancer, critter, initiation)
- `<attribute>` → Attributmodifikator
- `<skill>` → Skillmodifikator
- `<skillgroup>` → Skillgruppenmodifikator
- `<uneducated>`, `<uncouth>`, `<infirm>` → Quality-Flags
- `<nuyen>`, `<nuyenmaxbp>` → Nuyen-Boni

Rollback-Mechanismus: `_lstTransaction` speichert alle Improvements eines Aufrufs. Bei Fehlschlag (Dialogabbruch) werden alle zurückgerollt.

### `ImprovementManager.ValueToInt()`
Verarbeitet Formelstrings in Bonus-Nodes:
- `"Rating"` wird durch das übergebene Rating ersetzt
- Attributnamen (BOD, AGI, ...) werden durch TotalValue ersetzt
- `"FixedValues(a,b,c)"` → wählt `[Rating-1]`-Eintrag aus
- Arithmetik wird via XPathNavigator ausgewertet (z.B. `"BOD * 2"`)
- Ergebnis wird immer nach unten gerundet

---

## Datenmodell: `Quality`

**Datei:** `clsUnique.cs`, Zeile 963

### Felder

| Feld | Typ | Bedeutung |
|---|---|---|
| `_guiID` | `Guid` | Interne ID (wird vom Improvement-System als SourceName verwendet) |
| `_strName` | `string` | Qualitätsname |
| `_strExtra` | `string` | Zusatztext (z.B. gewähltes Attribut bei Metagenetic Improvement) |
| `_intBP` | `int` | BP-Kosten (positiv für positive, negativ für negative Qualities) |
| `_objQualityType` | `QualityType` | Positive / Negative |
| `_objQualitySource` | `QualitySource` | Selected / Metatype / MetatypeRemovable |
| `_blnContributeToLimit` | `bool` | Zählt zu Positive/Negative Quality Limit |
| `_blnPrint` | `bool` | Wird auf dem Charakterblatt gedruckt |
| `_strMutant` | `string` | "yes" wenn Mutanten-Quality |
| `_nodBonus` | `XmlNode` | Bonus-XML wird gespeichert und bei Load erneut ausgeführt |
| `_guiWeaponID` | `Guid` | Verknüpfte Waffe (z.B. für Natural Weapon-Qualities) |

### Create-Methode
1. Liest Daten aus `qualities.xml`
2. Fügt ggf. Waffen aus `weapons.xml` hinzu (für Quality mit `<addweapon>`)
3. Ruft `ImprovementManager.CreateImprovements()` für `<bonus>`-Nodes auf
4. Metatyp-Qualities werden grau dargestellt (können nicht entfernt werden)

---

## Datenmodell: `Spirit`

| Feld | Typ | Bedeutung |
|---|---|---|
| `_strName` | `string` | Spirit/Sprite-Typ |
| `_strCritterName` | `string` | Individueller Name |
| `_intServicesOwed` | `int` | Offene Dienste |
| `_intForce` | `int` | Kraftstufe |
| `_blnBound` | `bool` | true = gebunden |
| `_objEntityType` | `SpiritType` | Spirit / Sprite |
| `_strFileName` | `string` | Verlinkter Charakterdatei-Pfad |

---

## Konfiguration: `CharacterOptions`

**Datei:** `clsOptions.cs`, Zeile 700

Wird aus einer XML-Datei im `settings/`-Verzeichnis geladen. Standard: `default.xml`.

### BP-Standardwerte (direkt aus Code)

| Option | Standardwert | Bedeutung |
|---|---|---|
| `BPAttribute` | 10 | BP pro Attributpunkt |
| `BPAttributeMax` | 15 | Zusatz-BP für Attribut auf Rassenmaximum |
| `BPContact` | 1 | BP pro Kontaktpunkt |
| `BPMartialArt` | 5 | BP für Kampfkunst |
| `BPMartialArtManeuver` | 2 | BP pro Manöver |
| `BPSkillGroup` | 10 | BP pro Skillgruppen-Punkt |
| `BPActiveSkill` | 4 | BP pro aktivem Fertigkeitspunkt |
| `BPActiveSkillSpecialization` | 2 | BP pro Spezialisierung |
| `BPKnowledgeSkill` | 2 | BP pro Wissensfertigkeitspunkt |
| `BPSpell` | 3 | BP pro Zauber |
| `BPFocus` | 1 | BP pro Fokus-Punkt |
| `BPSpirit` | 1 | BP pro Geist-Kraftpunkt |
| `BPComplexForm` | 1 | BP pro Komplexer Form |
| `BPComplexFormOption` | 1 | BP pro Komplexer Form-Option |

### Karma-Standardwerte

| Option | Standardwert | Bedeutung |
|---|---|---|
| `KarmaAttribute` | 5 | Karma-Multiplikator für Attribute |
| `KarmaQuality` | 2 | Karma pro BP der Quality |
| `KarmaSpecialization` | 2 | Karma pro Spezialisierung |
| `KarmaNewKnowledgeSkill` | 2 | Karma für neue Wissensfertigkeit |
| `KarmaNewActiveSkill` | 4 | Karma für neue aktive Fertigkeit |
| `KarmaNewSkillGroup` | 10 | Karma für neue Skillgruppe |
| `KarmaImproveKnowledgeSkill` | 1 | Karma × i für Verbesserung Wissensfertigkeit |
| `KarmaImproveActiveSkill` | 2 | Karma × i für Verbesserung aktive Fertigkeit |
| `KarmaImproveSkillGroup` | 5 | Karma × i für Verbesserung Skillgruppe |

### Weitere kritische Optionen

| Option | Typ | Bedeutung |
|---|---|---|
| `NuyenPerBP` | `int` (5000) | Nuyen pro ausgegebenem BP |
| `LimbCount` | `int` (6) | Anzahl Gliedmaßen für Cyberlimb-Durchschnitt |
| `ESSLossReducesMaximumOnly` | `bool` | ESS-Verlust reduziert nur Maximum, nicht Minimum |
| `AllowSkillRegrouping` | `bool` | Fertigkeiten können zu Gruppen re-gruppiert werden |
| `BreakSkillGroupsInCreateMode` | `bool` | Gruppen aufbrechen in Create Mode erlaubt |
| `ExcludeLimbSlot` | `string` | Dieser Limb-Slot zählt nicht für Durchschnitt |
| `FreeContactsMultiplier` | `int` (2) | Freie Kontaktpunkte = CHA * Multiplikator |
| `MetatypeCostsKarma` | `bool` | Metatyp kostet Karma statt BP |
| `MetatypeCostMultiplier` | `int` (1) | Multiplikator für Metatypkosten |
| `UnrestrictedNuyen` | `bool` | Kein Nuyen-BP-Limit |
| `EssenceDecimals` | `int` (2) | Dezimalstellen für Essenz-Anzeige |

### GlobalOptions (Singleton)
Wird aus Windows Registry (`HKCU\Software\Chummer`) geladen.
Enthält: Sprache, automatisches Update, MRU-Liste, Omae-Account-Daten, PDF-Pfad, Standard-Charakterbogen, CultureInfo (immer `en-US`).

---

## Konfiguration: `GlobalOptions.CultureInfo`

**Status:** Gesichert

`CultureInfo` ist immer `en-US` (hardcodiert als static readonly).

**Risiko:** Alle `Convert.ToDecimal()` und `Convert.ToDouble()`-Aufrufe im Code verwenden `GlobalOptions.Instance.CultureInfo`. Dezimalzahlen werden im Format `6.0` (englischer Punkt) gespeichert. Wenn der Nutzer eine andere OS-Sprache hat, kann beim Laden alter Dateien ohne CultureInfo-Argument ein Bug entstehen (historischer Bug mit EssenceAtSpecialStart wurde durch Divide-by-10-Fix kaschiert).

---

## Persistenz: XML-Daten (Data-Files)

**Pfad:** `data/` (relativ zum Installationsverzeichnis)

| Datei | Inhalt |
|---|---|
| `metatypes.xml` | Metatypen, Metavarianten mit Attributgrenzen, BP-Kosten, Movement, Qualities |
| `skills.xml` | Skillgruppen, Fertigkeitskategorien, alle aktiven und Wissensfertigkeiten |
| `qualities.xml` | Qualities mit BP-Kosten, Typ (positiv/negativ), Bonus-Nodes |
| `cyberware.xml` / `bioware.xml` | Cyberware/Bioware mit Essenzkosten, Kapazität, Bonus, Grades |
| `armor.xml` | Rüstungen mit Ballistic/Impact, Modifikationsslots |
| `weapons.xml` | Waffen mit DV, AP, Reichweite, Modi |
| `gear.xml` | Allgemeine Ausrüstung |
| `powers.xml` | Adepten-Kräfte mit PP-Kosten |
| `spells.xml` | Zaubersprüche |
| `traditions.xml` | Magische Traditionen |
| `streams.xml` | Technomancer-Streams |
| `metamagic.xml` | Metamagie-Techniken |
| `echoes.xml` | Technomancer-Echos |
| `critters.xml` | Critter-Metatypen |
| `critterpowers.xml` | Critter-Kräfte |
| `lifestyles.xml` | Lebensstile |
| `packs.xml` | PACKS Kits |
| `books.xml` | Quellbücher |

### XML-Manager (`clsXmlManager.cs`)
- Singleton mit Caching: Einmal geladene XMLs werden im Memory gehalten
- Custom Content: Dateien aus `data/custom content/` werden automatisch per Merge geladen
- Override-Dateien: Dateien mit Präfix `override_` ersetzen Nodes komplett
- Merge-Strategie: Anhand `<name>`-Element werden gleichnamige Nodes überschrieben

---

## UI-Struktur

### Formenhierarchie

```
frmMain
├── frmCreate  (Character Creation)
│   ├── frmMetatype       (Metatyp-Auswahl)
│   ├── frmSelectQuality  (Qualitäten-Auswahl)
│   ├── frmSelectSkill    (Fertigkeit-Auswahl)
│   ├── frmSelectSkillGroup
│   ├── frmSelectCyberware
│   ├── frmSelectGear
│   ├── frmSelectArmor
│   ├── frmSelectWeapon
│   ├── frmSelectVehicle
│   ├── frmSelectLifestyle
│   ├── frmSelectSpell
│   ├── frmSelectPower
│   └── ... (viele weitere Select-Dialoge)
└── frmCareer  (Career Mode)
    └── (gleiche Select-Dialoge wie frmCreate + Expense-Dialoge)
```

### frmCreate / frmCareer
- Beide Formulare sind `~23.000 / ~27.000` Zeilen lange Monolith-Windows-Forms
- Sie enthalten sowohl UI-Logik als auch Fachlogik
- Charakter wird direkt von der Form mutiert
- Tabs: Attributes, Skills, Contacts/Spirits, Spells, Technomancer, Adept, Initiation, Cyberware/Bioware, Armor, Weapons, Gear, Vehicles, Critter, Background

### UI-Refresh-Pattern
- Kein Data-Binding
- Nach jeder Änderung werden Formularfelder manuell neu gesetzt
- Methoden wie `UpdateCharacterInfo()`, `RefreshSkills()`, `ScheduleCharacterUpdate()` werden aufgerufen
- TreeView für hierarchische Daten (Gear, Waffen, Cyberware)

---

## Karriere-Übergang (Character Created → Career Mode)

**Status:** Gesichert | Datei: `frmCreate.cs`, Zeile 17802

### `SaveCharacterAsCreated()`
1. Wenn BuildMethod == Karma und Karma > 0: Karma-Expense-Eintrag "Starting Karma" anlegen
2. Wenn EssenzPenalty > 0 und MAG/RES aktiv:
   - `MAG.Value += EssencePenalty`
   - `RES.Value += EssencePenalty`
   - (Kompensation damit Career-Mode-Anzeige stimmt)
3. Nuyen-Expense-Eintrag "Starting Nuyen" anlegen
4. Charakter speichern
5. `GlobalOptions.MainForm.LoadCharacter()` aufrufen → öffnet Career Mode

**Migrationsrelevanz:** Der MAG/RES+EssenzPenalty-Trick ist eine technische Korrektur, die im Java-Rebuild explizit behandelt werden muss.

---

## Expense-System

**Datei:** `clsExpenses.cs`

### Zweck
Protokolliert alle Karma- und Nuyen-Ausgaben mit Undo-Information.

### `KarmaExpenseType`-Werte
```
ImproveAttribute, AddQuality, ImproveSkillGroup, AddSkill, ImproveSkill,
SkillSpec, AddMartialArt, ImproveMartialArt, AddMartialArtManeuver,
AddSpell, AddComplexForm, ImproveComplexForm, AddComplexFormOption,
ImproveComplexFormOption, AddMetamagic, ImproveInitiateGrade,
RemoveQuality, ManualAdd, ManualSubtract, BindFocus,
JoinGroup, LeaveGroup, QuickeningMetamagic
```

### `NuyenExpenseType`-Werte
```
AddCyberware, IncreaseLifestyle, AddArmor, AddArmorMod, AddWeapon,
AddWeaponMod, AddWeaponAccessory, AddGear, AddVehicle, AddVehicleMod,
AddVehicleGear, AddVehicleWeapon, AddVehicleWeaponMod, AddVehicleWeaponAccessory,
ManualAdd, ManualSubtract, AddArmorGear, AddVehicleModCyberware,
AddCyberwareGear, AddWeaponGear
```

---

## Fachregeln: Initiative

### Physical Initiative
```
Initiative = INT + REA + AttributeModifiers(INI) + ImprovementValue(Initiative) + WoundModifiers
```
- Maximum: `INI.MetatypeAugmentedMaximum`

### Astral Initiative
```
AstralInitiative = INT * 2 + WoundModifiers
AstralInitiativePasses = 3 (immer fix)
```

### Matrix Initiative (Non-Technomancer)
```
MatrixInit = INT + Commlink.TotalResponse + ImprovementValue(MatrixInitiative)
MatrixInitiativePasses = 1 + ImprovementValue(MatrixInitiativePass)
```

### Matrix Initiative (Technomancer)
```
MatrixInit = INT * 2 + 1 + ImprovementValue(LivingPersonaResponse)
MatrixInitiativePasses = 3 + ImprovementValue(MatrixInitiativePass)
```

### A.I.-Sonderfall
```
MatrixInit = INT + Response
MatrixInitiativePasses = 3 (immer)
```

---

## Fachregeln: Condition Monitor

### Physischer CM
```
PhysicalCM = 8 + ceil(BOD / 2) + ImprovementValue(PhysicalCM)
```

### Stun CM
```
StunCM = 8 + ceil(WIL / 2) + ImprovementValue(StunCM)
```

### Overflow
```
OverflowCM = BOD + ImprovementValue(CMOverflow)
```

### CM-Threshold
```
CMThreshold = 3 + ImprovementValue(CMThreshold)
```

### Wound Modifiers
Berechnung aus gefüllten CM-Boxen: `-(floor(gefüllteBoxen / Threshold))`
- Ignoriert wenn `IgnoreCMPenaltyStun` / `IgnoreCMPenaltyPhysical` aktiv

---

## MRU-System

**Status:** Gesichert | **Altlast**

- Bis zu 10 normale MRU-Einträge in Registry: `HKCU\Software\Chummer\mru1`...`mru10`
- Bis zu 10 sticky MRU-Einträge: `stickymru1`...`stickymru10`
- Sticky MRU verhindert, dass die Datei in die normale MRU aufgenommen wird
- Keine Cross-Platform-Kompatibilität (Windows Registry)

---

## Bekannte Risiken und Altlasten

| # | Risiko | Ursache | Migrationsrelevanz |
|---|---|---|---|
| R1 | Globaler `try/catch`-Fehlerfressen beim Load | Nahezu jedes Feld wird in eigenem try/catch geladen, Exceptions werden nie geloggt | Sehr hoch – fehlerhafte Dateien können unbemerkt teilkorrupt laden |
| R2 | UniqueName-Bug im ImprovementManager | `break` außerhalb des `if` bei Duplikat-Prüfung | Mittel – Bonus-Stapelung kann in Sonderfällen falsch berechnet werden |
| R3 | Gear-Typ-Dispatch via `typeof` | `typeof` erkennt keine Subklassen | Mittel – Unterklassen von Commlink/OperatingSystem werden falsch gespeichert |
| R4 | EssenceAtSpecialStart / 10 Fixup | Historischer Parsing-Bug mit Dezimalzahlen | Hoch – Kompatibilität zu alten Dateien muss berücksichtigt werden |
| R5 | CultureInfo immer `en-US` | Hardcodiert als static | Mittel – JSON/XML mit Komma-Dezimaltrennzeichen würde Fehler erzeugen |
| R6 | MAG/RES +EssenzPenalty beim Career-Übergang | Technischer Trick für korrekte Anzeige | Hoch – muss im Java-Rebuild explizit modelliert werden |
| R7 | Monolith-Forms (23k/27k Zeilen) | Keine Trennung von UI und Fachlogik | Hoch – Fachlogik steckt in Event-Handlern der Forms |
| R8 | Windows-Registry-Abhängigkeit (MRU, Einstellungen) | GlobalOptions liest/schreibt Registry direkt | Hoch – nicht portable, muss durch Datei-basierte Konfiguration ersetzt werden |
| R9 | ImprovementManager öffnet UI-Dialoge | CreateImprovements() ruft `frmSelectText.ShowDialog()` etc. auf | Hoch – Verknüpfung von Fachlogik und UI-Schicht |
| R10 | `XPathNavigator`-Auswertung für Formelstrings | Arithmetik via XML-XPath-Evaluierung | Mittel – muss durch eigene Formelauswertung ersetzt werden |
| R11 | Kein Undo/Redo-System (außer Expense-Log) | Nur Expense-Einträge haben Undo-Info | Niedrig – derzeit kein vollständiges Undo vorgesehen |
| R12 | Zwei redundante Berechnungspfade (Custom/Non-Custom) | Improvements werden zweimal iteriert | Niedrig – leicht vereinfachbar in Java |

---

# Übergabe an Java-Planungs-Skill

## Analyseumfang
- **Projekt:** Chummer4 (gesamtes Projekt)
- **Analysierte Dateien:** clsCharacter.cs, clsUnique.cs, clsImprovement.cs, clsOptions.cs, clsExpenses.cs, clsCommon.cs, Program.cs, controllers/clsMainController.cs, frmCreate.cs (relevante Ausschnitte), frmCareer.cs (relevante Ausschnitte), metatypes.xml, skills.xml
- **Nicht analysierte, aber relevante Bereiche:** clsEquipment.cs (Cyberware, Waffen, Rüstung, Fahrzeuge – sehr groß), frmMetatype.cs (Metatyp-Auswahl-UI), clsXmlManager.cs (vollständig), alle frmSelect*.cs

## Fachlicher Zweck
SR4-Charakter-Tool: Erstellung (BP) und Karriereverwaltung (Karma) mit vollständigen Spielregeln, Persistenz und Druckfunktion.

## Gesichertes Ist-Verhalten

- `Character.Save()` erzeugt proprietäres XML, alle Daten werden explizit in fester Reihenfolge geschrieben
- `Character.Load()` liest via `try/catch` ohne Fehlerprotokoll; fehlende optionale Felder werden ignoriert
- Alle abgeleiteten Werte (TotalValue, BP-Kosten etc.) werden zur Laufzeit berechnet, nicht gespeichert (außer als externe Lesefreundlichkeit)
- Das Improvement-System vermittelt alle Boni – kein direktes Schreiben in Attributwerte durch Ausrüstung
- Der Karriere-Übergang erhöht MAG/RES explizit um EssenzPenalty als Korrekturmaßnahme

## Zwingend zu übernehmendes Verhalten

1. ESS-Berechnung: Cyberware und Bioware getrennt, höhere Kategorie voll, niedrigere halbiert
2. Augmented Maximum = MetatypeMax + floor(MetatypeMax/2) für normale Attribute
3. BP-Kosten Attribute: 10 BP pro Punkt, +15 BP für Rassenmaximum
4. Karma-Kosten Attribute: Summiere i × 5 für i von (Min+1) bis Value
5. Skill-Dice-Pool: Rating + Attribut + Modifikatoren + WoundMods; 0 wenn kein Default und Rating 0
6. Cyberlimb-Durchschnitt bei BOD/AGI/STR
7. UniqueName-Logik: Nur höchster Wert pro Gruppe (Bug korrigiert übernehmen)
8. precedence0/precedence1-Logik im Improvement-System
9. Armor-Stacking-Regeln (höchste + `+`-Stackings; Kleidung separat)
10. Career-Übergang: EssenzPenalty auf MAG/RES-Basiswert aufaddieren

## Fachregeln

| Regel | Status | Quelle | Migrationsrelevanz |
|---|---|---|---|
| BP-Budget: 400 | Gesichert | clsCharacter.cs:87 | Hoch |
| Attribut: 10 BP/Punkt | Gesichert | clsOptions.cs:775 | Hoch |
| Attribut auf Maximum: +15 BP | Gesichert | clsUnique.cs:852 | Hoch |
| Skill: 4 BP/Punkt | Gesichert | clsOptions.cs:782 | Hoch |
| Skillgruppe: 10 BP/Punkt | Gesichert | clsOptions.cs:780 | Hoch |
| Spezialisierung: 2 BP | Gesichert | clsOptions.cs:783 | Mittel |
| Uneducated/Uncouth/Infirm: Doppelte Kosten | Gesichert | clsUnique.cs:3158 | Hoch |
| ESS Max: 6 (Human) | Gesichert | metatypes.xml | Hoch |
| Cyberware > Bioware: Cyberware voll, Bioware halb | Gesichert | clsCharacter.cs:3554 | Hoch |
| InitiativePass Standard: 1 | Gesichert | clsCharacter.cs:3752 | Mittel |
| AugMax = Max + floor(Max/2) für BOD/AGI/... | Gesichert | clsUnique.cs:634 | Hoch |
| MAG/RES AugMax = Max (kein +50%) | Gesichert | clsUnique.cs:631 | Hoch |

## Datenmodelle
- `Character` (zentral, alle Listen)
- `Attribute` (BOD, AGI, REA, STR, CHA, INT, LOG, WIL, INI, EDG, MAG, RES, ESS)
- `Skill` (aktive, Wissens-, exotische Fertigkeiten)
- `SkillGroup`
- `Quality` (positiv/negativ, Quelle: Selected/Metatype/MetatypeRemovable)
- `Improvement` (universelles Bonus-Objekt)
- `Spirit/Sprite`, `Spell`, `Power` (Adept)
- `Contact`
- `ExpenseLogEntry` + `ExpenseUndo`
- `CharacterOptions` + `GlobalOptions`
- `Gear`, `Commlink`, `OperatingSystem` (Subklassen)
- `Cyberware` (inkl. Bioware, Nanotech)
- `Armor` (inkl. ArmorMod)
- `Weapon` (inkl. WeaponMod, WeaponAccessory)
- `Vehicle` (inkl. VehicleMod, VehicleWeapon)
- `Lifestyle`
- `TechProgram` (Komplexe Formen)
- `MartialArt` + `MartialArtManeuver` + `MartialArtAdvantage`
- `Metamagic`, `Echo`, `InitiationGrade`
- `CritterPower`
- `Focus`, `StackedFocus`
- `CalendarWeek`

## Seiteneffekte

- `ImprovementManager.CreateImprovements()` öffnet UI-Dialoge direkt
- `Character.Load()` → kann `Character.Save()` aufrufen (wenn Movement-Feld leer, wird Movement aus XML geladen und sofort gespeichert)
- `Character.MAGEnabled = true` feuert `MAGEnabledChanged`-Event → UI-Update
- Alle Special-Tab-Flags feuern Änderungs-Events

## Fehlerfälle

- Ungültige Spieledition: Load gibt `false` zurück, zeigt Fehlerdialog
- Settings-Datei nicht gefunden: Load gibt `false` zurück
- Dialog-Abbruch in CreateImprovements: `Rollback()` wird aufgerufen, `false` zurückgegeben
- Korrupte XML-Felder: Werden still ignoriert (try/catch)

## Reihenfolgeabhängigkeiten

- Beim Load: `ResetCharacter()` muss vor dem Laden der Listen aufgerufen werden
- Beim Career-Übergang: EssenzPenalty-Korrektur muss **vor** dem Speichern erfolgen
- Improvement-Transaktionen müssen atomar sein (Rollback bei Fehler)

## Nicht offensichtliche Altlogik

- EssenceAtSpecialStart / 10 Bug-Fix beim Load
- Mystic Adept: MAG wird in MAGAdept und MAGMagician aufgeteilt; Magier-Skills nutzen nur MAGMagician
- A.I.-Charaktere berechnen Rating als Durchschnitt von CHA+INT+LOG+WIL / 4 (aufgerundet)
- Cyberzombies: MAG immer 1
- Sprite-Metatypen: MatrixInit aus MetatypeMinimum statt Berechnung
- `Hold-Outs`→`Holdouts` Daten-Fix beim Skill-Load (Umbenennung in alten Dateien)

## Risiken für die Java-Neuimplementierung

| Risiko | Ursache | Mögliche Folge | Empfohlene Absicherung |
|---|---|---|---|
| UniqueName-Bug | break außerhalb if | Falsche Bonus-Stapelung | Eigene korrekte Implementierung mit Test |
| CultureInfo en-US | Hardcodiert | Dezimal-Parsing-Fehler | Immer explizit Locale bei Parse übergeben |
| try/catch Fehlerfressen | Legacy-Muster | Korrupte Dateien unbemerkt | Explizite Null-Checks, Fehlerlogs |
| MAG/RES+EssenzPenalty | Career-Übergang | Falscher Attributwert im Career | Explizite Berechnung bei Moduswechsel |
| UI-Logik in ImprovementManager | Architekturproblem | Nicht testbar | Service-Klasse ohne UI-Abhängigkeit |
| XPath-Arithmetik | Formelstrings | Komplexe Abhängigkeit | Eigene einfache Formelauswertung |

## Abgeleitete Testszenarien

- BP-Berechnung: Human mit allen Attributen auf Minimum → 0 BP; Human mit BOD 6 → 10 * 5 + 15 = 65 BP
- Essenz: Cyberware 3.0 + Bioware 1.0 → ESS = 6 - 3.0 - 0.5 = 2.5
- UniqueName: Zwei Improvements gleicher Gruppe → nur höchster Wert zählt
- Cyberlimb: Zwei Arme mit AGI 8, natürliches AGI 4 (6 Limbs) → floor((8+8+4+4+4+4)/6) = floor(32/6) = 5
- EssenzPenalty: ESS = 4.0, EssenceAtSpecialStart = 6.0 → EssenzPenalty = ceil(6.0 - 4.0) = 2
- Skill defaulting: LOG 4, Rating 0, Default erlaubt → TotalRating = 3
- Skill nicht defaultbar: Rating 0, Default verboten → TotalRating = 0

## Strukturelle Hinweise für Java-Planung

- **Service-Grenzen:** ImprovementManager sollte UI-frei als reiner Berechnungsservice implementiert werden; Character sollte nur Daten halten
- **Domänenobjekte:** Character, Attribute, Skill, SkillGroup, Quality, Improvement, Spirit, Spell, Power, Contact (analog zu C#)
- **Repository-Abstraktionen:** XmlDataLoader (metatypes, skills, qualities...) als separate Service-Klassen; CharacterStore für Persistenz
- **UI-Verhalten das erhalten bleiben muss:** Tab-Freischaltung via Events; Career-/Creation-Moduswechsel als expliziten Zustandsübergang; Improvement-Rollback bei Dialog-Abbruch
- **Technische Altlasten die nicht blind übernommen werden sollten:**
  - Registry-basierte Konfiguration → Datei-basiert in Java
  - Monolith-Forms → Controller-/Presenter-Pattern
  - try/catch ohne Logging → explizite Fehlerbehandlung
  - XPath-Formelauswertung → eigene einfache Expression-Engine
  - typeof-Dispatch → Polymorphismus oder Visitor-Pattern