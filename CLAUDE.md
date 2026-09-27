# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
./gradlew run          # Start the application
./gradlew build        # Compile + test
./gradlew test         # Run tests only
./gradlew jar          # Build JAR
./gradlew distZip      # Build distributable archive
```

**Java 21** is required (enforced via toolchain). Main class: `de.shalorian.chummer.App`.

## Architecture

**MVC with FXML.** Views are defined in `.fxml` files under `src/main/resources/de/shalorian/chummer/` and paired with
controller classes in `src/main/java/de/shalorian/chummer/view/`.

**Package structure:** `model/character` and `view` are split into feature-area subpackages to keep each package
navigable (each holds only a handful of classes):

- `model.character` (root): `Character` (aggregate root), `CharacterBiography`, `Contact`, `LogEntry`, `CatalogEntry`,
  `DataPaths`
- `model.character.attribute`, `.skill`, `.quality`, `.magic`, `.technomancer`, `.cyberware`, `.metatype`: data model classes for
  the respective feature area (e.g. `Skill`/`SkillDataLoader` in `.skill`, `SpellData`/`Tradition` in `.magic`)
- `view` (root): `MainWindowController`, `CharacterViewController`
- `view.dialog`: all modal dialog controllers and their result/handle types
- `view.converter`: JavaFX `StringConverter` implementations
- `view.grid`: shared row-wiring types (`RowModel`, `RowControls`, `AttributeRowLabel`, `SkillSortMode`)

`persistence` and `utils` remain flat (small enough to stay navigable). When adding a new class, place it in the
subpackage matching its feature area rather than the package root.

**Controller hierarchy:**

- `MainWindowController` — root; handles file menu, opens NewCharacterDialog, loads/saves via `CharacterStore`
- `CharacterViewController` — child injected by main; renders the character sheet (Attributes, Skills, Log tabs)
- `NewCharacterDialogController` / `LogEntryDialogController` — modal dialogs

**UI refresh pattern:** Controllers maintain `List<Runnable>` refresh lists. When model state changes, all registered
refresh callbacks are called to rebuild the grid rows.

**Data loading:** `MetatypeDataLoader` and `SkillDataLoader` parse XML on first access and cache the result. They return
fresh copies of data objects for each new character. XML files live at `src/main/resources/de/shalorian/chummer/data/`.

**Persistence:** `CharacterStore` converts `Character` ↔ `CharacterData` (DTO) and serializes to indented JSON via
Jackson. File extension is `.json`.

## Game Rules (Shadowrun 4e)

The model enforces SR4 rules:

- **Character creation:** 400 BP total; 10 BP per attribute point above racial minimum
- **Skills:** 4 BP/rating (individual) or 10 BP/rating (group); max rating 6
- **Resources:** BP explicitly assigned to resources (max. 50) buy equipment at 5,000 ¥ per BP, i.e. max. 250,000 ¥
  (SR4-Grundregelwerk S. 110, `Character.NUYEN_PER_BP`)
- **Career mode:** After finalizing, the assigned resource BP convert to starting Nuyen; attribute costs become (new
  rating × 5) Karma; skill costs become (new rating × 2) Karma
- **Metatypes:** 15 options (5 base races + 10 variants), each with specific attribute min/max ranges; variant
  attributes override parent attributes

## Language

All user-facing strings, XML data, comments, and Javadoc are in **German**. Identifiers (class, method, field names)
follow the existing code, which uses mostly **English** names (e.g. `Character.getEssence()`, `Cyberware`); German
domain terms appear in texts and data, not in identifiers. Keep this consistent when adding code or data.

## Workflows

While analyzing the old repository to migrate a feature use the following skills:

- **csharp-legacy-analyzer** to analyze the old c# code
- **java-analyzer** to analyze to existing java code
- **javafx-planner** to compare the differences between the two analyzers and plan the javafx structure for the new
  feature
- **java-developer** to implement the newly planned code

While analyzing the existing code to check for code quality and make suggestion to increase the code quality use the
following skill:

- **java-code-quality-reviewer** to review the code and suggest improvements

## RULES FOR CLAUDE

The following rules are imperative for every instance and every action the agent takes.

1. never try to implement code without being explicitly told to do so