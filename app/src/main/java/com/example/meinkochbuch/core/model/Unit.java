package com.example.meinkochbuch.core.model;

import lombok.Getter;

@Getter
public enum Unit {
    MILLILITER("Milliliter"),
    LITER("Liter"),
    MILLIGRAM("Milligram"),
    GRAM("Gramm"),
    KILOGRAM("Kilogramm"),
    PINCH("Prise"), //Prise
    WEDGE("Ecke(n)"), //Ecke
    BUNCH("Bund"), //Bund
    PIECE("Stück(e)"), //Stück
    TEA_SPOON("Teelöffel"),
    TABLE_SPOON("Esslöffel"),
    CUP("Becher"),
    FOOTBALL_FIELD("Fußballfeld(er)"),
    FL_OZ("Fluid ounce"),
    CUBE("Würfel"),
    ;

    private final String localizedName;
    Unit(String localizedName) {
        this.localizedName = localizedName;
    }


}
