package de.shalorian.chummer.view.dialog;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record ContactEditResult(
    @NonNull String name,
    @Nullable String description
) {}
