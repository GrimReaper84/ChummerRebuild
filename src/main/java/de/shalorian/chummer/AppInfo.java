package de.shalorian.chummer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.jspecify.annotations.NonNull;

/** Name, Version und Links der Anwendung für den Fenstertitel und einen künftigen Über-Dialog. */
public final class AppInfo {

  public static final @NonNull String NAME = "Chummer SR4 Reborn";
  public static final @NonNull String REPOSITORY = "https://github.com/GrimReaper84/ChummerRebuild";
  public static final @NonNull String LICENSE = "GNU General Public License v3.0, Copyright (c) 2026 GrimReaper84";

  private static final @NonNull String UNKNOWN_VERSION = "dev";

  private AppInfo() {}

  /** Fenstertitel ohne Charakter: Name und Version, z.B. „Chummer SR4 Reborn 1.0.0“. */
  public static @NonNull String title() {
    return NAME + " " + version();
  }

  /**
   * Die Version, die der Build aus {@code gradle.properties} in {@code version.properties} geschrieben hat; „dev“,
   * wenn die Ressource fehlt (z.B. Start aus der IDE ohne Gradle).
   */
  public static @NonNull String version() {
    try (InputStream in = AppInfo.class.getResourceAsStream("version.properties")) {
      if (in == null) {
        return UNKNOWN_VERSION;
      }
      Properties properties = new Properties();
      properties.load(in);
      return properties.getProperty("version", UNKNOWN_VERSION);
    } catch (IOException _) {
      return UNKNOWN_VERSION;
    }
  }
}
