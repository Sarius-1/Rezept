package com.example.meinkochbuch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.meinkochbuch.core.model.Ingredient;
import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.Unit;
import com.google.android.material.textfield.TextInputEditText;

/**
 * A Fragment that allows the user to create a new recipe.
 * Users can enter recipe metadata and dynamically add ingredients with unit and amount.
 */
public class CreateRezept extends Fragment {

    private LinearLayout zutatenContainer;
    private Button btnZutatHinzufuegen, btnRezeptSpeichern;

    /**
     * Inflates the layout and sets up listeners for adding ingredients and saving the recipe.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The root View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_rezept, container, false);

        zutatenContainer = rootView.findViewById(R.id.container_zutaten);
        btnZutatHinzufuegen = rootView.findViewById(R.id.btn_zutat_hinzufuegen);
        btnRezeptSpeichern = rootView.findViewById(R.id.btn_rezept_speichern);

        // Listener to dynamically add ingredient input fields
        View.OnClickListener addZutatView = v -> {
            View zutatView = inflater.inflate(R.layout.item_zutat, zutatenContainer, false);

            Spinner spinnerEinheit = zutatView.findViewById(R.id.spinner_einheit);
            ArrayAdapter<Unit> adapter = new ArrayAdapter<Unit>(requireContext(), android.R.layout.simple_spinner_item, Unit.values()) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    ((android.widget.TextView) view).setText(getItem(position).getLocalizedName());
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    ((android.widget.TextView) view).setText(getItem(position).getLocalizedName());
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEinheit.setAdapter(adapter);

            ImageButton btnEntfernen = zutatView.findViewById(R.id.btn_entfernen);
            btnEntfernen.setOnClickListener(v1 -> zutatenContainer.removeView(zutatView));

            zutatenContainer.addView(zutatView);
        };

        btnZutatHinzufuegen.setOnClickListener(addZutatView);
        addZutatView.onClick(null); // Add first ingredient field by default

        // Listener to save the recipe
        btnRezeptSpeichern.setOnClickListener(v -> {
            try {
                String name = ((TextInputEditText) rootView.findViewById(R.id.et_rezept_name)).getText().toString().trim();
                String beschreibung = ((TextInputEditText) rootView.findViewById(R.id.et_zubereitung)).getText().toString().trim();
                int portionen = Integer.parseInt(((TextInputEditText) rootView.findViewById(R.id.et_portionen)).getText().toString());
                int zeit = Integer.parseInt(((TextInputEditText) rootView.findViewById(R.id.et_zubereitungszeit)).getText().toString());

                RecipeManager manager = RecipeManager.getInstance();
                Recipe recipe = manager.createRecipe(name, zeit, portionen, beschreibung);

                for (int i = 0; i < zutatenContainer.getChildCount(); i++) {
                    View zutatView = zutatenContainer.getChildAt(i);

                    EditText etMenge = zutatView.findViewById(R.id.et_menge);
                    Spinner spinnerEinheit = zutatView.findViewById(R.id.spinner_einheit);
                    EditText etZutat = zutatView.findViewById(R.id.et_zutat);

                    String mengeStr = etMenge.getText().toString().trim();
                    String zutatName = etZutat.getText().toString().trim();

                    if (mengeStr.isEmpty() || zutatName.isEmpty()) continue;

                    int menge = Integer.parseInt(mengeStr);
                    Unit einheit = (Unit) spinnerEinheit.getSelectedItem();

                    Ingredient ingredient = manager.tryRegisterIngredient(zutatName);
                    if (ingredient != null) {
                        manager.addIngredient(recipe, ingredient, menge, einheit);
                    }
                }

                Toast.makeText(getContext(), getString(R.string.toast_recipe_saved), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getContext(), getString(R.string.toast_recipe_save_error, e.getMessage()), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        return rootView;
    }
}
