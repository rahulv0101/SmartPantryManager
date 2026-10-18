package com.example.smartpantrymanager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Pure-Java strict matcher for recipe requirements and pantry quantities. */
public final class RecipeMatcher {
    private static final Map<String, String> NAME_ALIASES = createNameAliases();

    private RecipeMatcher() {
        // Utility class.
    }

    public static List<Recipe> findMatchingRecipes(
            List<Recipe> recipes,
            Map<Long, List<RecipeIngredient>> requirementsByRecipe,
            List<PantryItem> pantryItems) {
        List<Recipe> matches = new ArrayList<>();
        if (recipes == null || requirementsByRecipe == null || pantryItems == null) {
            return matches;
        }

        for (Recipe recipe : recipes) {
            List<RecipeIngredient> requirements = requirementsByRecipe.get(recipe.getId());
            if (matches(recipe, requirements, pantryItems)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    public static boolean matches(Recipe recipe, List<RecipeIngredient> requirements,
                                  List<PantryItem> pantryItems) {
        if (recipe == null || requirements == null || requirements.isEmpty()
                || pantryItems == null) {
            return false;
        }

        Map<String, List<RecipeIngredient>> groupedRequirements = new HashMap<>();
        for (RecipeIngredient requirement : requirements) {
            if (requirement == null || normalizeIngredientName(requirement.getIngredientName()).isEmpty()
                    || !Double.isFinite(requirement.getRequiredQuantity())
                    || requirement.getRequiredQuantity() <= 0
                    || requirement.getUnit() == null || requirement.getUnit().trim().isEmpty()) {
                return false;
            }
            String normalizedName = normalizeIngredientName(requirement.getIngredientName());
            groupedRequirements.computeIfAbsent(normalizedName, ignored -> new ArrayList<>())
                    .add(requirement);
        }

        for (Map.Entry<String, List<RecipeIngredient>> entry : groupedRequirements.entrySet()) {
            List<RecipeIngredient> ingredientRequirements = entry.getValue();
            String requiredUnit = ingredientRequirements.get(0).getUnit();
            BigDecimal requiredTotal = BigDecimal.ZERO;
            for (RecipeIngredient requirement : ingredientRequirements) {
                BigDecimal convertedRequirement = UnitConverter.convertAsBigDecimal(
                        requirement.getRequiredQuantity(), requirement.getUnit(), requiredUnit);
                if (convertedRequirement == null) {
                    return false;
                }
                requiredTotal = requiredTotal.add(convertedRequirement);
            }

            BigDecimal availableTotal = BigDecimal.ZERO;
            for (PantryItem pantryItem : pantryItems) {
                if (pantryItem == null || !entry.getKey().equals(
                        normalizeIngredientName(pantryItem.getName()))) {
                    continue;
                }
                BigDecimal convertedAvailable = UnitConverter.convertAsBigDecimal(
                        pantryItem.getQuantity(), pantryItem.getUnit(), requiredUnit);
                if (convertedAvailable != null) {
                    availableTotal = availableTotal.add(convertedAvailable);
                }
            }

            if (availableTotal.compareTo(requiredTotal) < 0) {
                return false;
            }
        }
        return true;
    }

    public static String normalizeIngredientName(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.US);
        return NAME_ALIASES.getOrDefault(normalized, normalized);
    }

    private static Map<String, String> createNameAliases() {
        Map<String, String> aliases = new HashMap<>();
        addPluralAlias(aliases, "egg", "eggs");
        addPluralAlias(aliases, "tomato", "tomatoes");
        addPluralAlias(aliases, "potato", "potatoes");
        addPluralAlias(aliases, "onion", "onions");
        addPluralAlias(aliases, "carrot", "carrots");
        addPluralAlias(aliases, "bean", "beans");
        addPluralAlias(aliases, "pea", "peas");
        addPluralAlias(aliases, "cucumber", "cucumbers");
        return aliases;
    }

    private static void addPluralAlias(Map<String, String> aliases, String singular, String plural) {
        aliases.put(singular, singular);
        aliases.put(plural, singular);
    }
}
