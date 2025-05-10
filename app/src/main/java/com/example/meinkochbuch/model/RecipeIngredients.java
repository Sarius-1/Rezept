package com.example.meinkochbuch.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecipeIngredients {

    private String ingredientTableId, recipesTableId, unitTableId;
    private int amount;

}
