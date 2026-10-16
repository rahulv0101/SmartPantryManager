package com.example.smartpantrymanager;

/**
 * A recipe definition. Recipe records will be seeded in a later milestone.
 */
public class Recipe {
    private long id;
    private String name;
    private String instructions;

    public Recipe(String name, String instructions) {
        this(0, name, instructions);
    }

    public Recipe(long id, String name, String instructions) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
