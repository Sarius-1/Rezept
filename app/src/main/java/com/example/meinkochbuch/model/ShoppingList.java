package com.example.meinkochbuch.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShoppingList {

    private String id, ingredientTableId, unitTableId;
    private boolean checked;

}
