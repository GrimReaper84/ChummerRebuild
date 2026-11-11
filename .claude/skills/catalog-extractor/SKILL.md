---
name: catalog-extractor
description: Überträgt im Chummer-Rebuild einen Katalog (Qualities, Zauber, Komplexe Formen, Cyberware/Bioware, Waffen, Rüstung, Ausrüstung, Fahrzeuge …) aus dem SR4-Grundregelwerk in die XML-Katalogdateien des Projekts – mit Quelle und gedruckter Seite je Eintrag, Tabellenprüfung am Seitenbild und Abgleich gegen die C#-Daten – und schreibt einen Bericht nach docs/analysen/<katalog>_katalog.md. Verwende diesen Skill immer dann, wenn Katalogdaten angelegt, vervollständigt oder gegen das GRW geprüft werden sollen ("vervollständige die Qualities", "trag alle Kampfzauber ein", "prüf die Waffentabelle gegen das Buch"), auch wenn der Skill nicht ausdrücklich genannt wird. Schreibt keinen Java-Produktivcode.
---

# Katalog-Übertragung

Du überträgst Regelwerksinhalte in Katalogdaten, die die Anwendung zur Laufzeit lädt. Das GRW ist die
Wahrheitsquelle, nicht das C#-Original.

Warum das ein eigener Schritt ist: Shadowrun besteht zu großen Teilen aus Katalogen (Qualities, Zauber,
Implantate, Waffen, Ausrüstung). Die Werte exakt und mit Seitenbeleg zu übertragen ist der größte Teil
der Arbeit und etwas anderes als Planen oder Implementieren. Dazu kommt: Das GRW liegt nur als Scan vor,
jede Zahl und jeder Name kann von der OCR verfälscht sein.

**Zuerst lesen:** `.claude/skills/_gemeinsam/projektregeln.md`, vor allem Abschnitt 4 (Regelwerk) und
5 (Scope). Dazu `DECISIONS.md` (z.B. ADR-002 GRW-Kern zuerst, ADR-004 kleine Kataloge, ADR-005 keine
Halbfunktionen).

## Einordnung im Workflow

Ein Feature-Plan nennt die Kataloge, die er braucht. Ihre Übertragung ist ein eigener, freigegebener
Schritt; im verschlankten Modus darf sie innerhalb der Umsetzung laufen, wenn der Plan sie als Schritt
aufführt. Reine Katalog-Vervollständigungen (z.B. "alle GRW-Qualities") sind eigene Aufträge.

## Eingaben

| Eingabe | Ort |
|---|---|
| Katalog + Umfang | vom Nutzer, z.B. "alle Gaben aus dem GRW", "Kampfzauber", "Pistolen" |
| GRW-Fließtext | `docs/rulebooks/grundregelwerk.txt` (Seiten durch `\f` getrennt) |
| GRW-Tabellen | `docs/rulebooks/grundregelwerk_layout.txt` (gleiche Seitenumbrüche, Spalten in ihrer Zeile) |
| GRW-PDF | `docs/rulebooks/grundregelwerk.pdf` (Seitenbilder) |
| Bestehender Katalog | `src/main/resources/de/shalorian/chummer/data/<katalog>.xml` |
| Loader + Datensatz | `model.character.<bereich>.<X>DataLoader`, `<X>Data` (welche Tags gelesen werden) |
| C#-Daten (nur Abgleich) | `/home/luca/Chummer4_OriginalCode/chummer/Chummer/bin/Release/data/<katalog>.xml` |

## Vorgehen

1. **Seiten finden.** Kapitelüberschrift und einige Eintragsnamen im Fließtext suchen (`grep -n`),
   PDF-Seite berechnen (Anzahl `\f` vor der Zeile + 1) und auf die gedruckte Seite umrechnen
   (Projektregeln Abschnitt 4; im Zweifel am Seitenbild die Seitenzahl im Fuß ablesen). Zitiert wird
   immer die **gedruckte** Seite. Bei 0 Treffern deutsche Übersetzungen und Schreibvarianten probieren
   (Umlaute, Bindestriche, OCR-Fehler wie `rn`/`m`), bevor "nicht im GRW" gefolgert wird.
2. **Den ganzen Abschnitt lesen**, nicht nur die Treffer. Einträge laufen über Seitenumbrüche, und
   Kästen enthalten Ausnahmen ("nur bei Charaktererschaffung", "nicht für Technomancer").
3. **Tabellen** zuerst in `grundregelwerk_layout.txt` lesen. Weil auch das OCR ist, **jede
   Tabellenzeile mit Zahlen und jeden Eigennamen am Seitenbild gegenlesen**, bevor er in die Daten geht:
   ```bash
   gs -dQUIET -dSAFER -dBATCH -dNOPAUSE -sDEVICE=pdfwrite -dFirstPage=<N> -dLastPage=<M> \
      -sOutputFile=<scratchpad>/seiten.pdf docs/rulebooks/grundregelwerk.pdf 2>/dev/null
   ```
   Den Ausschnitt mit dem Read-Tool öffnen (max. ~20 Seiten je Aufruf). Typische OCR-Fehler: `1`↔`l`/`i`,
   `5`↔`S`, `8`↔`B`, `¥` als `Y`/`%`, fehlende Vorzeichen, verschluckte Ziffern. Nie einen Wert raten.
   Eindeutiger Fließtext braucht kein Bild.
4. **Gegen die C#-Daten abgleichen.** Einträge mit `<source>SR4</source>` im Original zählen und mit dem
   GRW vergleichen: Was fehlt im GRW (Zusatzband → zurückstellen), wo weichen Werte ab (GRW gilt, im
   Bericht nennen), was fehlt im C#? Die C#-Daten liefern nur Struktur und Vollständigkeitskontrolle,
   **keine Werte**.
5. **Schema prüfen.** Gibt es die Katalogdatei schon, ihre Struktur genau übernehmen; nur Tags
   verwenden, die der Loader liest. Gibt es sie nicht oder fehlt ein nötiges Feld (z.B. `<page>`),
   Schema im Bericht vorschlagen (Tags, Typen, Beispiel) und **anhalten und fragen**: Ein neues Tag
   braucht eine Loader-Änderung, und das ist Java-Code für `java-developer`.
6. **Einträge schreiben** (Konventionen unten), in GRW-Reihenfolge innerhalb einer Kategorie, sofern
   die Datei nichts anderes vorgibt.
7. **Prüfen:** Datei ist wohlgeformt (`python3 -I -c "import sys, xml.dom.minidom as m; m.parse(sys.argv[1])" <datei>`), Namen sind eindeutig, jeder neue
   Eintrag hat Quelle und Seite. Dann `./gradlew build`; die Loader-Tests (z.B. `VehicleDataLoaderTest`
   mit der am Bild übertragenen GRW-Tabelle) müssen grün bleiben. Kaputte Daten nie durch Abschwächen
   eines Tests "reparieren".
8. **Bericht schreiben** nach `docs/analysen/<katalog>_katalog.md`, dann anhalten und kurz berichten:
   Anzahl Einträge (neu/geändert/zurückgestellt), Abweichungen zum C#, offene Fragen.

## Konventionen für Einträge

- **`<name>` exakt wie im deutschen GRW** (Schreibweise, Bindestriche, Klammerzusätze), nicht die
  C#-Übersetzung. Speicherdateien referenzieren Einträge über den Namen (`findByName`), deshalb einen
  bestehenden Namen nur ändern, wenn das GRW es belegt, und die Umbenennung im Bericht nennen (alte
  Speicherdateien verlieren den Eintrag, ADR-013).
- **Quelle und Seite:** `<source>SR4</source>` und `<page>` mit der gedruckten Seite, auf der der Eintrag
  beginnt. Kataloge ohne `<page>`-Tag (derzeit `qualities.xml`, `skills.xml`, `metatypes.xml`): Seite im
  Bericht festhalten und die Schema-Erweiterung vorschlagen (Schritt 5).
- **Mechanische Werte als strukturierte Tags**, nicht als Fließtext: Kosten, Stufen, Verfügbarkeit,
  Essenz, Schaden usw. so, wie die Datei sie schon führt (z.B. `<avail>` + `<legality>`, `<rating>`,
  `<cost>`). Was sich nicht strukturiert abbilden lässt, kommt als `<note>` in eigenen Worten und in den
  Bericht.
- **Effekte werden hier nicht modelliert.** Ob und wie eine Gabe oder ein Implantat rechnerisch wirkt
  (neuer `QualityBonusType`, Attributbonus, Würfelpool-Modifikator), entscheidet der Feature-Plan. Der
  Katalog trägt nur bestehende Bonus-Tags ein (`QualityDataLoader.BONUS_TAGS`); fehlt einer, Eintrag
  mit leerem `<bonus/>` anlegen und im Bericht als "Effekt fehlt" markieren. Einträge, die ohne ihre
  Mechanik irreführend wären, zurückstellen statt halb anbieten (ADR-005).
- **Kommentare** in der XML auf Deutsch, mit Kategorie- und Seitenangaben wie in den bestehenden
  Dateien.
- **Nichts erfinden.** Keine Einträge oder Werte aus dem Gedächtnis. Was im GRW nicht steht, bleibt
  aus den Daten und kommt als offene Frage in den Bericht.

## Berichtsvorlage

```markdown
# Katalog: <katalog> (GRW S. <von>–<bis>)

> Datum, Umfang, Schema-Status (bestehend / vorgeschlagen, bestätigt am …).

## 1. Zusammenfassung        (Einträge neu/geändert/zurückgestellt, offene Fragen)
## 2. Quellen                (gedruckte Seiten | PDF-Seiten | am Bild geprüft: ja/nein je Tabelle)
## 3. Abgleich C#            (Anzahl SR4-Einträge im C# vs. GRW; Abweichungen: Eintrag | C# | GRW | Seite)
## 4. Schema                 (nur wenn neu oder geändert: Tags, Typen, Beispiel)
## 5. Nicht abbildbar        (Effekte/Voraussetzungen nur als Hinweis, mit Grund; fehlende Bonus-Typen)
## 6. Offene Fragen          (Tabelle: Eintrag | Frage | Auswirkung)
```

## Umfang

- Ein Katalog oder ein klar abgegrenzter Teil je Durchlauf. Große Kataloge (Ausrüstung, Waffen) nach
  Kategorien aufteilen; bei mehr als ~20 Seiten Bildprüfung die Arbeit an einen Fork-Agenten geben,
  damit der Hauptkontext frei bleibt.
- Keinen Java-Code ändern, außer Loader-Tests, die eine GRW-Tabelle als Prüfwert festhalten, wenn der
  Nutzer das freigibt. Andere Kataloge nicht anfassen. Git ist schreibgeschützt.
