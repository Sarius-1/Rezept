package com.example.meinkochbuch;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.meinkochbuch.core.model.Category;
import com.example.meinkochbuch.core.model.Ingredient;
import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.Unit;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ViewModel für CreateRezept. Kapselt alle Eingabefelder,
 * die Ingredient‐Einträge, und führt die Speichern‐Logik aus.
 */
@Getter
public class CreateRezeptViewModel extends AndroidViewModel {

    // 1) LiveData für die Felder
    private final MutableLiveData<String> name = new MutableLiveData<>("");
    private final MutableLiveData<String> portions = new MutableLiveData<>("");
    private final MutableLiveData<String> time = new MutableLiveData<>("");
    private final MutableLiveData<String> description = new MutableLiveData<>("");
    private final MutableLiveData<Uri> imageUri = new MutableLiveData<>(null);

    // 2) LiveData für Kategorien‐Checkboxen
    private final MutableLiveData<Boolean> isVegan = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isVegetarian = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isGlutenFree = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLactoseFree = new MutableLiveData<>(false);

    // 3) IngredientEntry: für jede Zeile (Menge | Einheit | Zutat‐Name)
    @NoArgsConstructor
    public static class IngredientEntry {
        @Getter private final MutableLiveData<String> menge = new MutableLiveData<>("");
        @Getter private final MutableLiveData<Unit> unit = new MutableLiveData<>(Unit.PIECE);
        @Getter private final MutableLiveData<String> zutatName = new MutableLiveData<>("");

        public void setMenge(String m) { menge.setValue(m); }
        public void setUnit(Unit u) { unit.setValue(u); }
        public void setZutatName(String z) { zutatName.setValue(z); }
    }

    // 4) MutableLiveData für die Liste der IngredientEntry
    private final MutableLiveData<List<IngredientEntry>> ingredients =
            new MutableLiveData<>(new ArrayList<>());

    public CreateRezeptViewModel(@NonNull Application application) {
        super(application);
        // direkt einen initialen leeren IngredientEntry hinzufügen
        List<IngredientEntry> list = ingredients.getValue();
        if (list != null) {
            list.add(new IngredientEntry());
            ingredients.setValue(list);
        }
    }

    // Setter für Form‐Felder
    public void setName(String s)        { name.setValue(s); }
    public void setPortions(String s)    { portions.setValue(s); }
    public void setTime(String s)        { time.setValue(s); }
    public void setDescription(String s) { description.setValue(s); }
    public void setImageUri(Uri uri)     { imageUri.setValue(uri); }

    public void setVegan(boolean b)      { isVegan.setValue(b); }
    public void setVegetarian(boolean b) { isVegetarian.setValue(b); }
    public void setGlutenFree(boolean b) { isGlutenFree.setValue(b); }
    public void setLactoseFree(boolean b){ isLactoseFree.setValue(b); }

    // Methoden für Ingredient‐Einträge
    public void addIngredientEntry() {
        List<IngredientEntry> list = ingredients.getValue();
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(new IngredientEntry());
        ingredients.setValue(list);
    }

    public void removeIngredientEntry(int index) {
        List<IngredientEntry> list = ingredients.getValue();
        if (list != null && index >= 0 && index < list.size()) {
            list.remove(index);
            ingredients.setValue(list);
        }
    }

    /** Externe Methode, um die gesamte Liste zu überschreiben. */
    public void setIngredients(List<IngredientEntry> newList) {
        ingredients.setValue(newList);
    }

    /**
     * Speichert das Rezept über RecipeManager. Gibt bei Fehlern eine Fehlermeldung (String) zurück,
     * oder null, wenn alles erfolgreich gespeichert wurde.
     */
    public String saveRecipe() {
        String nameStr     = name.getValue()        != null ? name.getValue().trim()        : "";
        String portionsStr = portions.getValue()    != null ? portions.getValue().trim()    : "";
        String timeStr     = time.getValue()        != null ? time.getValue().trim()        : "";
        String descStr     = description.getValue() != null ? description.getValue().trim() : "";

        if (nameStr.isEmpty()) {
            return getApplication().getString(R.string.validation_recipe_name_missing);
        }
        if (portionsStr.isEmpty()) {
            return getApplication().getString(R.string.validation_portions_missing);
        }
        if (timeStr.isEmpty()) {
            return getApplication().getString(R.string.validation_time_missing);
        }
        if (descStr.isEmpty()) {
            return getApplication().getString(R.string.validation_description_missing);
        }

        int portionsInt = 0, timeInt = 0;
        try {
            portionsInt = Integer.parseInt(portionsStr);
            timeInt     = Integer.parseInt(timeStr);
        } catch (NumberFormatException e) {
            Log.e("CreateRezeptVM", "NumberFormatException", e);
        }

        RecipeManager manager = RecipeManager.getInstance();
        Recipe recipe = manager.createRecipe(nameStr, timeInt, portionsInt, descStr);

        boolean hasValidIngredient = false;
        List<IngredientEntry> list = ingredients.getValue();
        if (list != null) {
            for (IngredientEntry entry : list) {
                String mengeStr   = entry.getMenge().getValue()    != null
                        ? entry.getMenge().getValue().trim() : "";
                String zutatName  = entry.getZutatName().getValue()!= null
                        ? entry.getZutatName().getValue().trim() : "";
                Unit unit         = entry.getUnit().getValue();

                if (mengeStr.isEmpty() || zutatName.isEmpty()) {
                    continue;
                }

                double mengeDbl;
                try {
                    mengeDbl = Double.parseDouble(mengeStr);
                } catch (NumberFormatException e) {
                    continue;
                }

                Ingredient ing = manager.tryRegisterIngredient(zutatName);
                if (ing != null) {
                    manager.addIngredient(recipe, ing, mengeDbl, unit);
                    hasValidIngredient = true;
                }
            }
        }

        if (!hasValidIngredient) {
            return getApplication().getString(R.string.validation_ingredient_missing);
        }

        if (Boolean.TRUE.equals(isVegan.getValue())) {
            manager.addCategory(recipe, Category.VEGAN);
        }
        if (Boolean.TRUE.equals(isVegetarian.getValue())) {
            manager.addCategory(recipe, Category.VEGETARIAN);
        }
        if (Boolean.TRUE.equals(isGlutenFree.getValue())) {
            manager.addCategory(recipe, Category.GLUTEN_FREE);
        }
        if (Boolean.TRUE.equals(isLactoseFree.getValue())) {
            manager.addCategory(recipe, Category.LACTOSE_FREE);
        }

        Uri img = imageUri.getValue();
        if (img != null) {
            boolean ok = manager.setImage(recipe, img);
            if (!ok) {
                return getApplication().getString(R.string.toast_image_save_failed);
            }
        }

        manager.setRating(recipe, 0);
        return null;
    }
}
