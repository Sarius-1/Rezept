package com.example.meinkochbuch.model;

import com.example.meinkochbuch.io.SQLModel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Recipe implements SQLModel {

    // -- Primary key --
    private String id;

    // -- Content --
    private String name;
    private int processingTime, portions, rating;
    private String guideText;

    @Override
    public String buildStatement() {
        return String.join("\n",
                "CREATE TABLE Recipe (",
                "ID Text");
    }
}
