package de.shalorian.chummer.view.shell;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Seitenleiste des Charakterbogens und der Inhaltsbereich daneben: ein Knopf je {@link CharacterSection}, nach
 * {@link SectionGroup} gegliedert; ein Klick zeigt die Seite des Bereichs unter einem einheitlichen Seitentitel. Nicht nutzbare Bereiche (z.B. Magie ohne
 * Gabe) bleiben sichtbar und deaktiviert, ihr Grund steht im Tooltip (ADR-033). Den Tooltip trägt ein Rahmen um den
 * Knopf, weil JavaFX an deaktivierten Elementen keinen Tooltip zeigt.
 */
public final class SectionNavigator {

  private static final String STYLE_SELECTED = "nav-item-selected";

  private final @NonNull StackPane content;
  private final @NonNull Map<CharacterSection, Node> pages;
  private final @NonNull Map<CharacterSection, Button> buttons = new EnumMap<>(CharacterSection.class);
  private final @NonNull Map<CharacterSection, StackPane> holders = new EnumMap<>(CharacterSection.class);
  private final @NonNull Map<CharacterSection, Label> pageTitles = new EnumMap<>(CharacterSection.class);
  private final @NonNull Map<CharacterSection, Tooltip> tooltips = new EnumMap<>(CharacterSection.class);
  private final @NonNull ScrollPane scroll = new ScrollPane();
  private @NonNull CharacterSection selected = CharacterSection.DEFAULT;

  /**
   * @param content der Inhaltsbereich, in dem immer genau die Seite des gewählten Bereichs liegt
   * @param pages   die Seite je Bereich; ein Bereich ohne Seite bekommt keinen Knopf
   */
  public SectionNavigator(@NonNull StackPane content, @NonNull Map<CharacterSection, Node> pages) {
    this.content = content;
    this.pages = new EnumMap<>(CharacterSection.class);
    pages.forEach((section, page) -> this.pages.put(section, withTitle(section, page)));
    VBox nav = new VBox(18);
    nav.getStyleClass().add("side-nav");
    for (SectionGroup group : SectionGroup.values()) {
      VBox box = new VBox(2);
      Label title = new Label(group.getTitle().toUpperCase(Locale.GERMAN));
      title.getStyleClass().add("nav-group");
      title.setMaxWidth(Double.MAX_VALUE);
      VBox.setMargin(title, new Insets(0, 0, 4, 0));
      box.getChildren().add(title);
      for (CharacterSection section : CharacterSection.of(group)) {
        if (this.pages.containsKey(section)) {
          box.getChildren().add(createButton(section));
        }
      }
      nav.getChildren().add(box);
    }
    scroll.setContent(nav);
    scroll.setFitToWidth(true);
    scroll.setFitToHeight(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.getStyleClass().add("side-nav-scroll");
    scroll.setMinWidth(200);
    scroll.setPrefWidth(220);
    showSelected();
  }

  /** Die Seite unter dem Bereichsnamen als Seitentitel; die Seite füllt den Rest der Höhe. */
  private @NonNull Node withTitle(@NonNull CharacterSection section, @NonNull Node page) {
    Label title = new Label(section.getTitle());
    pageTitles.put(section, title);
    title.getStyleClass().add("page-title");
    title.setPadding(new Insets(20, 36, 4, 36));
    VBox.setVgrow(page, Priority.ALWAYS);
    VBox box = new VBox(title, page);
    box.getStyleClass().add("page");
    return box;
  }

  private @NonNull Node createButton(@NonNull CharacterSection section) {
    Button button = new Button(section.getTitle());
    button.getStyleClass().setAll("nav-item");
    button.setMaxWidth(Double.MAX_VALUE);
    button.setOnAction(_ -> select(section));
    StackPane holder = new StackPane(button);
    buttons.put(section, button);
    holders.put(section, holder);
    return holder;
  }

  /** Die Seitenleiste. */
  public @NonNull Node node() {
    return scroll;
  }

  public @NonNull CharacterSection getSelected() {
    return selected;
  }

  /** Zeigt den Bereich, sofern er einen Knopf hat und nutzbar ist. */
  public void select(@NonNull CharacterSection section) {
    if (!isAvailable(section)) {
      return;
    }
    selected = section;
    showSelected();
  }

  /** Ob der Bereich einen Knopf hat, der eingeblendet und nutzbar ist. */
  private boolean isAvailable(@NonNull CharacterSection section) {
    Button button = buttons.get(section);
    StackPane holder = holders.get(section);
    return button != null && holder != null && !button.isDisabled() && holder.isVisible();
  }

  /**
   * Macht einen Bereich nutzbar oder nicht; ein nicht nutzbarer trägt {@code reason} als Tooltip. Fällt der gewählte
   * Bereich weg, wird {@link CharacterSection#DEFAULT} gewählt.
   */
  public void setEnabled(@NonNull CharacterSection section, boolean enabled, @Nullable String reason) {
    Button button = buttons.get(section);
    StackPane holder = holders.get(section);
    if (button == null || holder == null) {
      return;
    }
    button.setDisable(!enabled);
    Tooltip old = tooltips.remove(section);
    if (old != null) {
      Tooltip.uninstall(holder, old);
    }
    if (!enabled && reason != null) {
      Tooltip tooltip = new Tooltip(reason);
      Tooltip.install(holder, tooltip);
      tooltips.put(section, tooltip);
    }
    fallBackIfUnavailable();
  }

  /** Benennt einen Bereich in Seitenleiste und Seitentitel um (Initiation bzw. Wandlung). */
  public void setTitle(@NonNull CharacterSection section, @NonNull String title) {
    Button button = buttons.get(section);
    if (button != null) {
      button.setText(title);
    }
    Label pageTitle = pageTitles.get(section);
    if (pageTitle != null) {
      pageTitle.setText(title);
    }
  }

  /** Blendet den Knopf eines Bereichs ein oder aus (Status nur bei statischer Anzeige). */
  public void setShown(@NonNull CharacterSection section, boolean shown) {
    StackPane holder = holders.get(section);
    if (holder == null) {
      return;
    }
    holder.setVisible(shown);
    holder.setManaged(shown);
    fallBackIfUnavailable();
  }

  private void fallBackIfUnavailable() {
    if (!isAvailable(selected)) {
      selected = CharacterSection.DEFAULT;
    }
    showSelected();
  }

  private void showSelected() {
    buttons.forEach((section, button) -> {
      button.getStyleClass().remove(STYLE_SELECTED);
      if (section == selected) {
        button.getStyleClass().add(STYLE_SELECTED);
      }
    });
    Node page = pages.get(selected);
    if (page != null && !content.getChildren().equals(List.of(page))) {
      content.getChildren().setAll(page);
    }
  }
}
