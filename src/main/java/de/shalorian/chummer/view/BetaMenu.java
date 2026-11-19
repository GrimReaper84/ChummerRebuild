package de.shalorian.chummer.view;

import de.shalorian.chummer.model.character.option.RuleOptions;
import de.shalorian.chummer.model.template.DocumentTemplate;
import de.shalorian.chummer.model.template.TemplateKind;
import de.shalorian.chummer.model.template.Templates;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.jspecify.annotations.NonNull;

/**
 * Beta-Menü für Entwicklungsläufe (ADR-063, Vorbild Fallout-Projekt ADR-018): öffnet die Archetypen und
 * Schergengruppen und Critter der Vorlagen sowie die Testfälle unter {@code samples/} des Ordners aus der Systemeigenschaft
 * {@value #PROPERTY}, jeweils als ungespeicherte Kopie. Nur der Gradle-Task {@code run} setzt die Eigenschaft, nicht
 * {@code applicationDefaultJvmArgs} (das käme in Startskripte und Installer); Release-Builds zeigen das Menü daher nie.
 * Die Testfälle werden beim Aufbau und über „Liste neu laden“ gelesen.
 */
final class BetaMenu {

  /** Systemeigenschaft mit dem Ordner, der {@code samples/} enthält. */
  static final @NonNull String PROPERTY = "chummer.beta";

  private static final @NonNull String SAMPLES = "samples";
  private static final @NonNull String JSON = ".json";

  private BetaMenu() {
  }

  /** Der Ordner aus {@value #PROPERTY}, wenn die Eigenschaft gesetzt ist und der Ordner existiert. */
  static @NonNull Optional<Path> folder() {
    String value = System.getProperty(PROPERTY);
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    Path folder = Path.of(value);
    if (!Files.isDirectory(folder)) {
      System.err.println("Beta-Modus aus: Ordner aus " + PROPERTY + " fehlt: " + folder.toAbsolutePath());
      return Optional.empty();
    }
    return Optional.of(folder);
  }

  /**
   * Das Menü „Beta“.
   *
   * @param openTemplate legt ein ungespeichertes Dokument aus einer Vorlage an
   * @param openSample   öffnet einen Testfall als ungespeicherte Kopie
   */
  static @NonNull Menu create(@NonNull Path folder, @NonNull Consumer<DocumentTemplate> openTemplate,
      @NonNull Consumer<Path> openSample) {
    List<DocumentTemplate> templates = Templates.available(new RuleOptions());
    Menu archetypes = templateMenu("Archetypen", TemplateKind.ARCHETYPE, templates, openTemplate);
    Menu groups = templateMenu("Schergengruppen", TemplateKind.NPC_GROUP, templates, openTemplate);
    Menu critters = templateMenu("Critter", TemplateKind.CRITTER, templates, openTemplate);
    Menu samples = new Menu("Testfälle");
    MenuItem reload = new MenuItem("Liste neu laden");
    reload.setOnAction(_ -> samples.getItems().setAll(sampleItems(folder.resolve(SAMPLES), openSample)));
    samples.getItems().setAll(sampleItems(folder.resolve(SAMPLES), openSample));
    return new Menu("Beta", null, archetypes, groups, critters, samples, new SeparatorMenuItem(), reload);
  }

  private static @NonNull Menu templateMenu(@NonNull String title, @NonNull TemplateKind kind,
      @NonNull List<DocumentTemplate> templates, @NonNull Consumer<DocumentTemplate> open) {
    Menu menu = new Menu(title);
    for (DocumentTemplate template : templates) {
      if (template.kind() == kind) {
        MenuItem item = new MenuItem(template.name());
        item.setOnAction(_ -> open.accept(template));
        menu.getItems().add(item);
      }
    }
    if (menu.getItems().isEmpty()) {
      menu.getItems().add(disabled("Keine Vorlagen"));
    }
    return menu;
  }

  private static @NonNull List<MenuItem> sampleItems(@NonNull Path dir, @NonNull Consumer<Path> open) {
    List<Path> files;
    try {
      files = files(dir);
    } catch (IOException e) {
      return List.of(disabled("Ordner nicht lesbar: " + e.getMessage()));
    }
    if (files.isEmpty()) {
      return List.of(disabled("Keine Dateien in " + dir.getFileName()));
    }
    return files.stream().map(file -> {
      MenuItem item = new MenuItem(label(file));
      item.setOnAction(_ -> open.accept(file));
      return item;
    }).toList();
  }

  private static @NonNull MenuItem disabled(@NonNull String text) {
    MenuItem item = new MenuItem(text);
    item.setDisable(true);
    return item;
  }

  /** Die {@code *.json}-Dateien eines Ordners, nach Name sortiert; leer, wenn der Ordner fehlt. */
  static @NonNull List<Path> files(@NonNull Path dir) throws IOException {
    if (!Files.isDirectory(dir)) {
      return List.of();
    }
    try (Stream<Path> entries = Files.list(dir)) {
      return entries.filter(Files::isRegularFile)
          .filter(file -> file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(JSON))
          .sorted().toList();
    }
  }

  /** Menütext eines Testfalls, z.B. {@code karriere-mit-log.json} → „Karriere Mit Log“. */
  static @NonNull String label(@NonNull Path file) {
    String name = file.getFileName().toString();
    String base = name.toLowerCase(Locale.ROOT).endsWith(JSON) ? name.substring(0, name.length() - JSON.length()) : name;
    return Arrays.stream(base.split("[-_ ]+")).filter(word -> !word.isEmpty())
        .map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1))
        .collect(Collectors.joining(" "));
  }
}
