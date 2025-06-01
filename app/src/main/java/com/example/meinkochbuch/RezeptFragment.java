package com.example.meinkochbuch;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeIngredient;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.ShoppingListItem;
import com.example.meinkochbuch.databinding.FragmentRezeptBinding;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;

public class RezeptFragment extends Fragment {

    private FragmentRezeptBinding binding;
    private Recipe currentRecipe;
    private int standardPortionen = 1;
    private final Map<CheckBox, RecipeIngredient> checkedZutaten = new HashMap<>();
    RecipeManager manager = RecipeManager.getInstance();

    @Getter
    private int currentRating;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentRecipe = RezeptFragmentArgs.fromBundle(requireArguments()).getRecipe();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Binding initialisieren statt inflate mit findViewById
        binding = FragmentRezeptBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (currentRecipe != null) {
            // 1) Rezept-Daten in die gebundenen Views setzen
            binding.textRezeptName.setText(currentRecipe.getName());
            binding.textZubereitung.setText(currentRecipe.getGuideText());
            binding.textZeit.setText(currentRecipe.getProcessingTime() + " Minuten");
            binding.editPortionen.setText(currentRecipe.getPortions()+"");

            standardPortionen = currentRecipe.getPortions();
            binding.editPortionen.setHint(String.valueOf(standardPortionen));
            updateZutatenListe(standardPortionen);

            // 2) Listener: wenn Portionszahl geändert wird, Zutatenliste anpassen
            binding.editPortionen.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    int neuePortionen = standardPortionen;
                    try {
                        neuePortionen = Integer.parseInt(s.toString());
                        if (neuePortionen < 1) neuePortionen = 1;
                    } catch (NumberFormatException ignored) { }
                    updateZutatenListe(neuePortionen);
                }
            });

            // 3) Sterne-Buttons initialisieren
            setupRatingButtons();

            // 4) Button: ausgewählte Zutaten zur Einkaufsliste hinzufügen
            binding.buttonAddToShoppingList.setOnClickListener(v -> {
                for (Map.Entry<CheckBox, RecipeIngredient> entry : checkedZutaten.entrySet()) {
                    if (entry.getKey().isChecked()) {
                        RecipeIngredient ri = entry.getValue();
                        manager.addShoppingListItem(ri.getIngredient(), ri.getAmount(), ri.getUnit());
                        entry.getKey().setChecked(false);
                    }
                }
                Toast.makeText(getContext(), "Zutaten wurden zur Einkaufsliste hinzugefügt", Toast.LENGTH_SHORT).show();
                // EinkaufslisteFragment.refreshShoppingList();
            });
        }

        return view;
    }

    private void setupRatingButtons() {
        int currentRating = currentRecipe.getRating();
        // Sterne-Buttons über Binding referenzieren
        binding.buttonStar1.setImageResource(
                (1 <= currentRating) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        binding.buttonStar2.setImageResource(
                (2 <= currentRating) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        binding.buttonStar3.setImageResource(
                (3 <= currentRating) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        binding.buttonStar4.setImageResource(
                (4 <= currentRating) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        binding.buttonStar5.setImageResource(
                (5 <= currentRating) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

        binding.buttonStar1.setOnClickListener(v -> setRating(1));
        binding.buttonStar2.setOnClickListener(v -> setRating(2));
        binding.buttonStar3.setOnClickListener(v -> setRating(3));
        binding.buttonStar4.setOnClickListener(v -> setRating(4));
        binding.buttonStar5.setOnClickListener(v -> setRating(5));
    }

    private void setRating(int rating) {
        currentRating = rating;
        // Stern-Icons entsprechend aktualisieren
        binding.buttonStar1.setImageResource(
                (1 <= rating) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        binding.buttonStar2.setImageResource(
                (2 <= rating) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        binding.buttonStar3.setImageResource(
                (3 <= rating) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        binding.buttonStar4.setImageResource(
                (4 <= rating) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        binding.buttonStar5.setImageResource(
                (5 <= rating) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

        manager.setRating(currentRecipe, rating);
    }

    private void updateZutatenListe(int portionen) {
        if (currentRecipe == null) return;
        binding.layoutZutatenListe.removeAllViews();
        checkedZutaten.clear();
        float faktor = portionen / (float) standardPortionen;
        for (RecipeIngredient ri : currentRecipe.getIngredients()) {
            double menge = ri.getAmount() * faktor;
            String text = String.format(Locale.getDefault(),
                    "%.2f %s %s",
                    menge,
                    ri.getUnit().getLocalizedName(),
                    ri.getIngredient().getName());
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(text);
            checkBox.setChecked(true);
            binding.layoutZutatenListe.addView(checkBox);
            checkedZutaten.put(checkBox, ri);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
