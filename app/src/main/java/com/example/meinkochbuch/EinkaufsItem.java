package com.example.meinkochbuch;

import lombok.Setter;

public class EinkaufsItem {
    private String name;
    private int menge;
    @Setter
    private boolean ausgewaehlt;

    public EinkaufsItem(String name, int menge) {
        this.name = name;
        this.menge = menge;
        this.ausgewaehlt = false;
    }

    public String getName() {
        return name;
    }

    public int getMenge() {
        return menge;
    }

    public boolean isAusgewaehlt() {
        return ausgewaehlt;
    }

}

