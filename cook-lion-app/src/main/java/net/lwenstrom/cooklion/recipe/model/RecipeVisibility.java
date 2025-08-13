package net.lwenstrom.cooklion.recipe.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecipeVisibility {
    PUBLIC("Public", "Visible to all users"),
    PRIVATE("Private", "Visible only to the creator"),
    SHARED("Shared", "Visible to specific users");

    private final String displayName;
    private final String description;
}
