package com.example.smartpantrymanager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The built-in recipe catalogue used to initialise a local database.
 * Quantities are for one simple serving and use the application's canonical units.
 */
public final class RecipeSeedData {
    private RecipeSeedData() {
        // Utility class.
    }

    public static List<SeedRecipe> getRecipes() {
        return RECIPES;
    }

    private static final List<SeedRecipe> RECIPES = Collections.unmodifiableList(Arrays.asList(
            recipe("Scrambled Eggs", "Whisk the eggs with milk. Cook gently in melted butter until set.",
                    ingredient("Eggs", 2, "pieces"), ingredient("Milk", 30, "ml"), ingredient("Butter", 10, "g")),
            recipe("Cheese Omelette", "Whisk the eggs with milk, cook in butter, and fold in the cheese.",
                    ingredient("Eggs", 2, "pieces"), ingredient("Cheese", 30, "g"), ingredient("Milk", 15, "ml"), ingredient("Butter", 10, "g")),
            recipe("Boiled Eggs", "Place the eggs in boiling water, cook to preference, and cool before serving.",
                    ingredient("Eggs", 2, "pieces")),
            recipe("Fried Eggs", "Melt the butter in a pan and fry the eggs until the whites are set.",
                    ingredient("Eggs", 2, "pieces"), ingredient("Butter", 10, "g")),
            recipe("Egg Sandwich", "Cook the eggs, then layer them with buttered bread and serve.",
                    ingredient("Eggs", 2, "pieces"), ingredient("Bread", 2, "pieces"), ingredient("Butter", 10, "g")),
            recipe("Cheese Sandwich", "Butter the bread, add cheese, and toast until warm and lightly browned.",
                    ingredient("Bread", 2, "pieces"), ingredient("Cheese", 40, "g"), ingredient("Butter", 10, "g")),
            recipe("Tomato Sandwich", "Slice the tomato and layer it with bread and cheese.",
                    ingredient("Bread", 2, "pieces"), ingredient("Tomato", 1, "pieces"), ingredient("Cheese", 20, "g")),
            recipe("French Toast", "Dip bread in beaten egg and milk, then fry in butter until golden.",
                    ingredient("Bread", 2, "pieces"), ingredient("Eggs", 1, "pieces"), ingredient("Milk", 60, "ml"), ingredient("Butter", 10, "g")),
            recipe("Pancakes", "Mix flour, milk, and egg into a batter. Cook portions in butter until golden.",
                    ingredient("Flour", 120, "g"), ingredient("Milk", 180, "ml"), ingredient("Eggs", 1, "pieces"), ingredient("Butter", 10, "g")),
            recipe("Garlic Toast", "Mix garlic with softened butter, spread on bread, and toast until crisp.",
                    ingredient("Bread", 2, "pieces"), ingredient("Garlic", 1, "pieces"), ingredient("Butter", 20, "g")),
            recipe("Mashed Potatoes", "Boil the potatoes until tender, then mash with warm milk and butter.",
                    ingredient("Potatoes", 300, "g"), ingredient("Milk", 60, "ml"), ingredient("Butter", 20, "g")),
            recipe("Baked Potatoes", "Bake the potatoes until tender and finish with melted butter.",
                    ingredient("Potatoes", 300, "g"), ingredient("Butter", 10, "g")),
            recipe("Tomato Pasta", "Cook the pasta. Saute tomato and garlic in butter, then combine with the pasta.",
                    ingredient("Pasta", 100, "g"), ingredient("Tomato", 2, "pieces"), ingredient("Garlic", 1, "pieces"), ingredient("Butter", 10, "g")),
            recipe("Buttered Pasta", "Cook the pasta until tender, drain, and toss with butter and cheese.",
                    ingredient("Pasta", 100, "g"), ingredient("Butter", 20, "g"), ingredient("Cheese", 20, "g")),
            recipe("Rice and Beans", "Cook the rice, warm the beans with tomato, and combine before serving.",
                    ingredient("Rice", 100, "g"), ingredient("Beans", 150, "g"), ingredient("Tomato", 1, "pieces")),
            recipe("Vegetable Rice", "Cook the rice, saute the vegetables, and fold them together.",
                    ingredient("Rice", 100, "g"), ingredient("Carrot", 1, "pieces"), ingredient("Peas", 80, "g"), ingredient("Onion", 1, "pieces")),
            recipe("Fried Rice", "Stir-fry the vegetables, add rice and beaten eggs, and cook until combined.",
                    ingredient("Rice", 150, "g"), ingredient("Eggs", 2, "pieces"), ingredient("Carrot", 1, "pieces"), ingredient("Peas", 80, "g"), ingredient("Onion", 1, "pieces")),
            recipe("Simple Egg Fried Rice", "Stir-fry cooked rice with egg and soy sauce until hot.",
                    ingredient("Rice", 150, "g"), ingredient("Eggs", 2, "pieces"), ingredient("Soy Sauce", 15, "ml")),
            recipe("Tomato Salad", "Dice the vegetables, combine them in a bowl, and serve chilled.",
                    ingredient("Tomato", 2, "pieces"), ingredient("Cucumber", 1, "pieces"), ingredient("Onion", 1, "pieces")),
            recipe("Cucumber Salad", "Slice the cucumber and tomato, stir through yogurt, and serve chilled.",
                    ingredient("Cucumber", 2, "pieces"), ingredient("Tomato", 1, "pieces"), ingredient("Yogurt", 100, "g"))
    ));

    private static SeedRecipe recipe(String name, String instructions, SeedIngredient... ingredients) {
        return new SeedRecipe(name, instructions, Arrays.asList(ingredients));
    }

    private static SeedIngredient ingredient(String name, double quantity, String unit) {
        return new SeedIngredient(name, quantity, unit);
    }

    public static final class SeedRecipe {
        private final String name;
        private final String instructions;
        private final List<SeedIngredient> ingredients;

        private SeedRecipe(String name, String instructions, List<SeedIngredient> ingredients) {
            this.name = name;
            this.instructions = instructions;
            this.ingredients = Collections.unmodifiableList(ingredients);
        }

        public String getName() {
            return name;
        }

        public String getInstructions() {
            return instructions;
        }

        public List<SeedIngredient> getIngredients() {
            return ingredients;
        }
    }

    public static final class SeedIngredient {
        private final String name;
        private final double quantity;
        private final String unit;

        private SeedIngredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getName() {
            return name;
        }

        public double getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }
    }
}
