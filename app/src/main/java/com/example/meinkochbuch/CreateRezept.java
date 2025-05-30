package com.example.meinkochbuch;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.meinkochbuch.core.model.Category;
import com.example.meinkochbuch.core.model.Ingredient;
import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.Unit;
import com.google.android.material.textfield.TextInputEditText;

public class CreateRezept extends Fragment {

    private LinearLayout ingredientContainer;
    private Button btnAddIngredient;
    private Button btnSaveRecipe;

    private CheckBox cbVegan, cbVegetarian, cbGlutenFree, cbLactoseFree;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_rezept, container, false);

        ingredientContainer = rootView.findViewById(R.id.container_zutaten);
        btnAddIngredient = rootView.findViewById(R.id.btn_zutat_hinzufuegen);
        btnSaveRecipe = rootView.findViewById(R.id.btn_rezept_speichern);

        cbVegan = rootView.findViewById(R.id.checkbox_vegan);
        cbVegetarian = rootView.findViewById(R.id.checkbox_vegetarian);
        cbGlutenFree = rootView.findViewById(R.id.checkbox_gluten_free);
        cbLactoseFree = rootView.findViewById(R.id.checkbox_lactose_free);

        btnAddIngredient.setOnClickListener(v -> addIngredientView(inflater));
        addIngredientView(inflater); // default

        btnSaveRecipe.setOnClickListener(v -> saveRecipe(rootView));

        return rootView;
    }

    private void addIngredientView(@NonNull LayoutInflater inflater) {
        View ingredientView = inflater.inflate(R.layout.item_zutat, ingredientContainer, false);

        Spinner spinnerUnit = ingredientView.findViewById(R.id.spinner_einheit);
        ArrayAdapter<Unit> unitAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, Unit.values()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Unit unit = getItem(position);
                if (unit != null) {
                    ((TextView) view).setText(unit.getLocalizedName());
                }
                return view;
            }

            @NonNull
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                Unit unit = getItem(position);
                if (unit != null) {
                    ((TextView) view).setText(unit.getLocalizedName());
                }
                return view;
            }
        };

        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        ImageButton btnRemove = ingredientView.findViewById(R.id.btn_entfernen);
        btnRemove.setOnClickListener(v -> ingredientContainer.removeView(ingredientView));

        ingredientContainer.addView(ingredientView);
    }

    private void saveRecipe(@NonNull View rootView) {
        try {
            String name = getInputText(rootView, R.id.et_rezept_name);
            String description = getInputText(rootView, R.id.et_zubereitung);
            int portions = parseInputInteger(rootView, R.id.et_portionen);
            int time = parseInputInteger(rootView, R.id.et_zubereitungszeit);

            RecipeManager manager = RecipeManager.getInstance();
            Recipe recipe = manager.createRecipe(name, time, portions, description);

            for (int i = 0; i < ingredientContainer.getChildCount(); i++) {
                View ingredientView = ingredientContainer.getChildAt(i);
                if (ingredientView == null) continue;

                String amountStr = getEditText(ingredientView, R.id.et_menge);
                String ingredientName = getEditText(ingredientView, R.id.et_zutat);

                if (amountStr.isEmpty() || ingredientName.isEmpty()) continue;

                int amount = Integer.parseInt(amountStr);
                Spinner spinnerUnit = ingredientView.findViewById(R.id.spinner_einheit);
                Unit unit = (Unit) spinnerUnit.getSelectedItem();

                Ingredient ingredient = manager.tryRegisterIngredient(ingredientName);
                if (ingredient != null) {
                    manager.addIngredient(recipe, ingredient, amount, unit);
                }
            }

            // Kategorien zuweisen
            if (cbVegan.isChecked()) manager.addCategory(recipe, Category.VEGAN);
            if (cbVegetarian.isChecked()) manager.addCategory(recipe, Category.VEGETARIAN);
            if (cbGlutenFree.isChecked()) manager.addCategory(recipe, Category.GLUTEN_FREE);
            if (cbLactoseFree.isChecked()) manager.addCategory(recipe, Category.LACTOSE_FREE);

            Toast.makeText(requireContext(), getString(R.string.toast_recipe_saved), Toast.LENGTH_SHORT).show();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.FirstFragment);

        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.toast_recipe_save_error, e.getMessage()), Toast.LENGTH_LONG).show();
            Log.e("CreateRezept", "Error while saving recipe", e);
        }
    }

    @NonNull
    private String getInputText(@NonNull View rootView, int id) {
        TextInputEditText input = rootView.findViewById(id);
        return input != null && input.getText() != null ? input.getText().toString().trim() : "";
    }

    @NonNull
    private String getEditText(@NonNull View view, int id) {
        EditText editText = view.findViewById(id);
        return editText != null && editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private int parseInputInteger(@NonNull View rootView, int id) throws NumberFormatException {
        String value = getInputText(rootView, id);
        return Integer.parseInt(value);
    }
}
