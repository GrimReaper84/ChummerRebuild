package de.shalorian.chummer.model.character.initiation;

import de.shalorian.chummer.model.character.Character;
import de.shalorian.chummer.model.character.attribute.SpecialAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Initiation (Charaktere mit Magie, GRW S. 232/233) bzw. Wandlung (Technomancer, S. 284) eines Charakters: Grad und
 * gelernte Metamagien bzw. Echos (Teilobjekt von {@link Character}, Vorbild {@code AdeptPowers}).
 *
 * <ul>
 *   <li>Nur im Spiel, nicht bei der Erschaffung (S. 112); jeder neue Grad kostet 10 + neuer Grad × 3 Karma.</li>
 *   <li>Der Grad darf den wirksamen Magie- bzw. Resonanzwert (nach Essenzverlust) nicht übersteigen.</li>
 *   <li>Das natürliche Maximum von Magie bzw. Resonanz steigt auf 6 + Grad (S. 233, 284).</li>
 *   <li>Je Grad, auch beim ersten, eine Technik; nachholen ist erlaubt, es zählt nur, dass es nicht mehr Techniken als
 *       Grade gibt. Reine Adepten lernen nur vier Metamagien (S. 232); Echos teils bis zu dreimal (S. 284).</li>
 *   <li>Sinkt der wirksame Wert später unter den Grad, verfallen laut GRW Grade und Techniken. Hier bleibt beides
 *       erhalten und {@link #isAboveAttribute()} meldet es (Nutzerentscheidung), die Spielrunde entscheidet.</li>
 *   <li>Wie alles im Karrieremodus wird nur gesteigert, nichts zurückgenommen.</li>
 * </ul>
 */
public class Initiation {

  private static final int BASE_KARMA = 10;
  private static final int KARMA_PER_GRADE = 3;

  private final @NonNull Character owner;
  private int grade;
  private final List<TechniqueData> techniques = new ArrayList<>();

  /** Initiation für {@code owner}; ruft im Konstruktor nichts auf dem Charakter auf. */
  public Initiation(@NonNull Character owner) {
    this.owner = owner;
  }

  /** Metamagie bei Magie, Echo bei Resonanz, sonst {@code null} (dann gibt es keine Initiation). */
  public @Nullable TechniqueKind getKind() {
    if (owner.hasMagic()) {
      return TechniqueKind.METAMAGIC;
    }
    return owner.hasResonance() ? TechniqueKind.ECHO : null;
  }

  public int getGrade() {
    return grade;
  }

  /** Karmakosten des nächsten Grades: 10 + neuer Grad × 3 (S. 232, 284). */
  public int getNextGradeKarmaCost() {
    return BASE_KARMA + (grade + 1) * KARMA_PER_GRADE;
  }

  /** Warum der Grad nicht gesteigert werden kann, oder {@code null}. Die Karmabuchung übernimmt die Oberfläche. */
  public @Nullable String getRaiseProblem() {
    TechniqueKind kind = getKind();
    if (kind == null) {
      return "Nur mit Magie oder Resonanz";
    }
    if (!owner.isCreated()) {
      return kind.getProcessName() + " erst im Karrieremodus (S. 112)";
    }
    int attribute = owner.getEffectiveMagicOrResonanceValue();
    if (grade + 1 > attribute) {
      return "%s %d darf %s %d nicht übersteigen".formatted(kind.getGradeName(), grade + 1, kind.getAttributeName(),
          attribute);
    }
    return null;
  }

  /**
   * Steigert den Grad um 1 und hebt damit das Maximum von Magie bzw. Resonanz.
   *
   * @throws IllegalStateException wenn {@link #getRaiseProblem()} einen Grund liefert
   */
  public void raiseGrade() {
    String problem = getRaiseProblem();
    if (problem != null) {
      throw new IllegalStateException(problem);
    }
    grade++;
    applyMaximum();
  }

  /** Gelernte Techniken in Lernreihenfolge; mehrfach gelernte stehen mehrfach darin. */
  public @NonNull List<TechniqueData> getTechniques() {
    return Collections.unmodifiableList(techniques);
  }

  /** Wie oft {@code data} gelernt wurde. */
  public int count(@NonNull TechniqueData data) {
    return (int) techniques.stream().filter(data::equals).count();
  }

  /** Noch freie Techniken: je Grad eine, abzüglich der gelernten. */
  public int getFreeChoices() {
    return Math.max(0, grade - techniques.size());
  }

  /** Warum {@code data} nicht gelernt werden kann, oder {@code null}. */
  public @Nullable String getLearnProblem(@NonNull TechniqueData data) {
    TechniqueKind kind = getKind();
    if (kind != data.kind()) {
      return "Nur bei der " + data.kind().getProcessName();
    }
    boolean pureAdept = owner.hasAdeptPowers() && !owner.isMagician();
    if (data.noAdept() && pureAdept) {
      return "Adepten können diese Metamagie nicht lernen";
    }
    if (data.adeptOnly() && !owner.hasAdeptPowers()) {
      return "Nur für Adepten und Magieradepten";
    }
    int learned = count(data);
    if (learned >= data.maxTimes()) {
      return data.maxTimes() == 1 ? "Bereits gelernt" : "Höchstens %d-mal".formatted(data.maxTimes());
    }
    return getNoFreeChoiceProblem();
  }

  /** Grund, wenn keine Technik mehr frei ist („Je Grad ein Echo; zuerst den Grad steigern“), sonst {@code null}. */
  public @Nullable String getNoFreeChoiceProblem() {
    TechniqueKind kind = getKind();
    if (kind == null || getFreeChoices() > 0) {
      return null;
    }
    return "Je Grad " + kind.getOneTechnique() + "; zuerst den Grad steigern";
  }

  /**
   * Lernt {@code data} (ohne Karma, die Wahl gehört zum Grad).
   *
   * @throws IllegalStateException wenn {@link #getLearnProblem} einen Grund liefert
   */
  public void learn(@NonNull TechniqueData data) {
    String problem = getLearnProblem(data);
    if (problem != null) {
      throw new IllegalStateException(problem);
    }
    techniques.add(data);
  }

  /** Ob der Grad über dem wirksamen Magie- bzw. Resonanzwert liegt (laut GRW verfielen dann Grade und Techniken). */
  public boolean isAboveAttribute() {
    return getKind() != null && grade > owner.getEffectiveMagicOrResonanceValue();
  }

  /**
   * Hinweis, wenn der Grad über dem wirksamen Wert liegt (z.B. nach Essenzverlust), sonst {@code null}: Laut GRW
   * verfallen dann Grade und Techniken; hier entscheidet die Spielrunde.
   */
  public @Nullable String getAboveAttributeHint() {
    TechniqueKind kind = getKind();
    if (kind == null || !isAboveAttribute()) {
      return null;
    }
    return "%s %d liegt über %s %d: laut GRW verfallen die überzähligen Grade samt %s (S. %s)".formatted(
        kind.getGradeName(), grade, kind.getAttributeName(), owner.getEffectiveMagicOrResonanceValue(),
        kind.getPluralName(), kind.getPage());
  }

  /**
   * Zeile für die Rückfrage beim Entfernen der Gabe: was mit Magie bzw. Resonanz verloren geht, oder {@code null} ohne
   * Grad.
   */
  public @Nullable String getLossLine() {
    TechniqueKind kind = getKind();
    if (kind == null || grade == 0) {
      return null;
    }
    return "%s %d samt %d %s geht verloren".formatted(kind.getGradeName(), grade, techniques.size(),
        techniques.size() == 1 ? kind.getTechniqueName() : kind.getPluralName());
  }

  /** Stellt einen gespeicherten Stand ohne Prüfungen wieder her (Ladepfad) und setzt das Maximum. */
  public void restore(int savedGrade, @NonNull List<TechniqueData> savedTechniques) {
    grade = getKind() == null ? 0 : Math.max(0, savedGrade);
    techniques.clear();
    if (grade > 0) {
      savedTechniques.stream().filter(t -> t.kind() == getKind()).forEach(techniques::add);
    }
    applyMaximum();
  }

  /** Verwirft Grad und Techniken, wenn der Charakter Magie bzw. Resonanz verliert. */
  public void clear() {
    grade = 0;
    techniques.clear();
  }

  /** Hebt das natürliche Maximum von Magie bzw. Resonanz auf 6 + Grad. */
  private void applyMaximum() {
    SpecialAttribute attribute = owner.getMagicOrResonance();
    if (attribute != null) {
      attribute.setMaxBonus(grade);
    }
  }
}
