package com.example.smartpantrymanager;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RecipeMatcherTest {
    private long nextRecipeId = 1;

    @Test
    public void exactMatchRequiresEveryIngredient() {
        Recipe recipe = recipe("Pancakes");
        List<RecipeIngredient> requirements = Arrays.asList(
                requirement(recipe, "Eggs", 1, "pieces"),
                requirement(recipe, "Milk", 100, "ml"));
        List<PantryItem> pantry = Arrays.asList(
                pantry("eggs", 1, "pieces"), pantry("milk", 100, "ml"));

        assertTrue(RecipeMatcher.matches(recipe, requirements, pantry));
        assertFalse(RecipeMatcher.matches(recipe, requirements,
                Collections.singletonList(pantry("eggs", 1, "pieces"))));
    }

    @Test
    public void insufficientAndExactQuantitiesAreHandledStrictly() {
        Recipe recipe = recipe("Milk Test");
        List<RecipeIngredient> requirements = Collections.singletonList(
                requirement(recipe, "Milk", 500, "ml"));

        assertTrue(RecipeMatcher.matches(recipe, requirements,
                Collections.singletonList(pantry("Milk", 500, "ml"))));
        assertFalse(RecipeMatcher.matches(recipe, requirements,
                Collections.singletonList(pantry("Milk", 499.99, "ml"))));
    }

    @Test
    public void reducingPantryQuantityRemovesPreviouslyMatchingRecipe() {
        Recipe recipe = recipe("Reduced Quantity Test");
        List<RecipeIngredient> requirements = Collections.singletonList(
                requirement(recipe, "Flour", 200, "g"));

        assertTrue(RecipeMatcher.matches(recipe, requirements,
                Collections.singletonList(pantry("Flour", 200, "g"))));
        assertFalse(RecipeMatcher.matches(recipe, requirements,
                Collections.singletonList(pantry("Flour", 199.99, "g"))));
    }

    @Test
    public void nameNormalizationHandlesWhitespaceCaseAndKnownPluralForms() {
        assertEquals("tomato", RecipeMatcher.normalizeIngredientName("  TOMATOES  "));
        assertEquals("egg", RecipeMatcher.normalizeIngredientName(" eggs "));
        assertEquals("potato", RecipeMatcher.normalizeIngredientName("Potatoes"));
        assertEquals("red apple", RecipeMatcher.normalizeIngredientName(" Red   Apple "));
        assertFalse(RecipeMatcher.normalizeIngredientName("tomato")
                .equals(RecipeMatcher.normalizeIngredientName("tomato sauce")));
    }

    @Test
    public void aliasesMatchButUnrelatedNamesDoNot() {
        Recipe recipe = recipe("Tomato Test");
        List<RecipeIngredient> requirements = Collections.singletonList(
                requirement(recipe, "tomato", 2, "pieces"));

        assertTrue(RecipeMatcher.matches(recipe, requirements,
                Collections.singletonList(pantry("tomatoes", 2, "pieces"))));
        assertFalse(RecipeMatcher.matches(recipe, requirements,
                Collections.singletonList(pantry("tomato sauce", 2, "pieces"))));
    }

    @Test
    public void supportedUnitConversionsAreDimensionSafe() {
        assertEquals(1000.0, UnitConverter.convert(1, "kg", "g"), 0.000001);
        assertEquals(1.0, UnitConverter.convert(1000, "g", "kg"), 0.000001);
        assertEquals(1000.0, UnitConverter.convert(1, "L", "ml"), 0.000001);
        assertEquals(1.0, UnitConverter.convert(1000, "ml", "L"), 0.000001);
        assertEquals(2.0, UnitConverter.convert(2, "pieces", "pieces"), 0.000001);
        assertNull(UnitConverter.convert(100, "g", "ml"));
        assertNull(UnitConverter.convert(2, "pieces", "g"));
    }

    @Test
    public void compatibleQuantitiesAggregateAcrossMultiplePantryRecords() {
        Recipe recipe = recipe("Flour Test");
        List<RecipeIngredient> requirements = Collections.singletonList(
                requirement(recipe, "Flour", 200, "g"));
        List<PantryItem> pantry = Arrays.asList(
                pantry("Flour", 100, "g"), pantry("flour", 0.1, "kg"));

        assertTrue(RecipeMatcher.matches(recipe, requirements, pantry));
    }

    @Test
    public void incompatibleRecordsAreNotUsedForAggregation() {
        Recipe recipe = recipe("Milk Test");
        List<RecipeIngredient> requirements = Collections.singletonList(
                requirement(recipe, "Milk", 200, "ml"));

        assertFalse(RecipeMatcher.matches(recipe, requirements,
                Arrays.asList(pantry("Milk", 100, "ml"), pantry("Milk", 100, "g"))));
    }

    @Test
    public void duplicateRecipeRequirementsAreSummed() {
        Recipe recipe = recipe("Duplicate Test");
        List<RecipeIngredient> requirements = Arrays.asList(
                requirement(recipe, "Egg", 1, "pieces"),
                requirement(recipe, "Eggs", 2, "pieces"));

        assertTrue(RecipeMatcher.matches(recipe, requirements,
                Collections.singletonList(pantry("Eggs", 3, "pieces"))));
        assertFalse(RecipeMatcher.matches(recipe, requirements,
                Collections.singletonList(pantry("Eggs", 2, "pieces"))));
    }

    @Test
    public void invalidOrEmptyRequirementsNeverMatch() {
        Recipe recipe = recipe("Invalid Test");
        List<PantryItem> pantry = Collections.singletonList(pantry("Eggs", 10, "pieces"));

        assertFalse(RecipeMatcher.matches(recipe, Collections.emptyList(), pantry));
        assertFalse(RecipeMatcher.matches(recipe, null, pantry));
        assertFalse(RecipeMatcher.matches(recipe,
                Collections.singletonList(requirement(recipe, "Eggs", 0, "pieces")), pantry));
    }

    @Test
    public void fiveIngredientRecipeFailsWhenOneIngredientIsMissing() {
        Recipe recipe = recipe("Five Ingredient Test");
        List<RecipeIngredient> requirements = Arrays.asList(
                requirement(recipe, "A", 1, "pieces"), requirement(recipe, "B", 1, "pieces"),
                requirement(recipe, "C", 1, "pieces"), requirement(recipe, "D", 1, "pieces"),
                requirement(recipe, "E", 1, "pieces"));
        List<PantryItem> pantry = Arrays.asList(
                pantry("A", 1, "pieces"), pantry("B", 1, "pieces"),
                pantry("C", 1, "pieces"), pantry("D", 1, "pieces"));

        assertFalse(RecipeMatcher.matches(recipe, requirements, pantry));
    }

    @Test
    public void deletedRequiredIngredientRemovesPreviouslyMatchingRecipe() {
        Recipe recipe = recipe("Deletion Test");
        List<RecipeIngredient> requirements = Arrays.asList(
                requirement(recipe, "Eggs", 2, "pieces"),
                requirement(recipe, "Milk", 100, "ml"));
        List<PantryItem> pantry = Arrays.asList(
                pantry("Eggs", 2, "pieces"), pantry("Milk", 100, "ml"));

        assertTrue(RecipeMatcher.matches(recipe, requirements, pantry));
        assertFalse(RecipeMatcher.matches(recipe, requirements,
                Collections.singletonList(pantry("Eggs", 2, "pieces"))));
    }

    @Test
    public void findMatchingRecipesReturnsOnlyFullyMatchedRecipes() {
        Recipe matched = recipe("Matched");
        Recipe missing = recipe("Missing");
        List<Recipe> recipes = Arrays.asList(matched, missing);
        Map<Long, List<RecipeIngredient>> requirements = new HashMap<>();
        requirements.put(matched.getId(), Collections.singletonList(
                requirement(matched, "Eggs", 1, "pieces")));
        requirements.put(missing.getId(), Arrays.asList(
                requirement(missing, "Eggs", 1, "pieces"),
                requirement(missing, "Milk", 1, "ml")));

        List<Recipe> results = RecipeMatcher.findMatchingRecipes(recipes, requirements,
                Collections.singletonList(pantry("Eggs", 1, "pieces")));
        assertEquals(1, results.size());
        assertEquals("Matched", results.get(0).getName());
    }

    @Test
    public void matchingDoesNotModifyPantryRecords() {
        Recipe recipe = recipe("No Mutation Test");
        List<RecipeIngredient> requirements = Collections.singletonList(
                requirement(recipe, "Milk", 1, "L"));
        PantryItem pantryItem = pantry("Milk", 1000, "ml");

        assertTrue(RecipeMatcher.matches(recipe, requirements,
                Collections.singletonList(pantryItem)));
        assertEquals("Milk", pantryItem.getName());
        assertEquals(1000.0, pantryItem.getQuantity(), 0.000001);
        assertEquals("ml", pantryItem.getUnit());
    }

    private Recipe recipe(String name) {
        Recipe recipe = new Recipe(nextRecipeId++, name, "Test instructions");
        return recipe;
    }

    private RecipeIngredient requirement(Recipe recipe, String name, double quantity, String unit) {
        return new RecipeIngredient(recipe.getId(), name, quantity, unit);
    }

    private PantryItem pantry(String name, double quantity, String unit) {
        return new PantryItem(name, quantity, unit, null);
    }
}
