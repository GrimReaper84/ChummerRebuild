package de.shalorian.chummer.view.document;

import de.shalorian.chummer.App;
import de.shalorian.chummer.model.npc.NpcGroup;
import de.shalorian.chummer.persistence.NpcGroupStore;
import de.shalorian.chummer.view.npc.NpcGroupViewController;
import de.shalorian.chummer.view.tab.TabServices;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;

/** Eine offene Schergengruppe bzw. ein Critter-Rudel (ADR-062, ADR-064) als Dokument (ADR-066). */
public final class NpcGroupDocument extends OpenDocument {

  private final @NonNull NpcGroupViewController view;
  private final @NonNull NpcGroup group;

  private NpcGroupDocument(@NonNull DocumentState state, @NonNull NpcGroupViewController view,
      @NonNull NpcGroup group) {
    super(state, FontAwesomeSolid.USERS);
    this.view = view;
    this.group = group;
  }

  public static @NonNull NpcGroupDocument create(@NonNull NpcGroup group, @NonNull DocumentState state)
      throws IOException {
    FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(App.class.getResource("NpcGroupView.fxml")));
    loader.load();
    NpcGroupViewController view = loader.getController();
    NpcGroupDocument document = new NpcGroupDocument(state, view, group);
    TabServices services = new TabServices(() -> view.node().getScene().getWindow(), () -> { },
        () -> group.getGrunt().getCharacter().getRuleOptions());
    view.setGroup(group, services, document::refreshLabel);
    document.attach(view.node());
    return document;
  }

  @Override
  public @NonNull String name() {
    return group.getName();
  }

  @Override
  protected byte @NonNull [] serialize() {
    return NpcGroupStore.serialize(group);
  }

  @Override
  protected void write(@NonNull File file) throws IOException {
    NpcGroupStore.save(group, file);
  }

  @Override
  public void rebuild() {
    view.rebuild();
  }
}
