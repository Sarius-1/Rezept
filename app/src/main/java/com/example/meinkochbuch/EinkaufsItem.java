package com.example.meinkochbuch;

public class EinkaufsItem {
    private String name;
    private int menge;
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

    public void setAusgewaehlt(boolean ausgewaehlt) {
        this.ausgewaehlt = ausgewaehlt;
    }
}

