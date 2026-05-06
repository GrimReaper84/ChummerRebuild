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
- **Career mode:** After finalizing, remaining BP convert to Nuyen (1 BP = 2000 ¥); attribute costs become (new rating ×
    5) Karma; skill costs become (new rating × 2) Karma
- **Metatypes:** 15 options (5 base races + 10 variants), each with specific attribute min/max ranges; variant
  attributes override parent attributes

## Language

All user-facing strings, XML data, comments, and identifiers are in **German**. Keep this consistent when adding code or
data.

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