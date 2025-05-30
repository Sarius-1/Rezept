package com.example.meinkochbuch;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.meinkochbuch.core.model.Ingredient;

import lombok.Getter;


import java.util.List;

@Getter
public class FilterViewModel extends ViewModel {

    // Bewertung von - bis
    private final MutableLiveData<Integer> ratingMin = new MutableLiveData<>();
    private final MutableLiveData<Integer> ratingMax = new MutableLiveData<>();

    // Zeit von - bis
    private final MutableLiveData<Integer> timeMin = new MutableLiveData<>();
    private final MutableLiveData<Integer> timeMax = new MutableLiveData<>();

    // Zutaten-Auswahl
    private final MutableLiveData<List<Ingredient>> selectedIngredients = new MutableLiveData<>();

    // Checkboxen: Ernährungstypen
    private final MutableLiveData<Boolean> isVegan = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isVegetarian = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isGlutenFree = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLactoseFree = new MutableLiveData<>(false);

    // Event: Anwenden
    private final MutableLiveData<Void> applyFiltersEvent = new MutableLiveData<>();

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();

    // Methoden
    public void onApplyFiltersClicked() {
        applyFiltersEvent.setValue(null); // Signal an die UI
    }

    public void resetFilters() {
        ratingMin.setValue(null);
        ratingMax.setValue(null);
        timeMin.setValue(null);
        timeMax.setValue(null);
        selectedIngredients.setValue(null);
        isVegan.setValue(false);
        isVegetarian.setValue(false);
        isGlutenFree.setValue(false);
        isLactoseFree.setValue(false);
        applyFiltersEvent.setValue(null);
    }
}
