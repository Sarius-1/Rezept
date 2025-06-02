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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeIngredient;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.databinding.FragmentRezeptBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;

public class RezeptFragment extends Fragment {

    private FragmentRezeptBinding binding;
    private Recipe currentRecipe;
    private int standardPortionen = 1;
    private final Map<CheckBox, RecipeIngredient> checkedZutaten = new HashMap<>();
    private final RecipeManager manager = RecipeManager.getInstance();

    @Getter
    private int currentRating;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Argument per SafeArgs auslesen
        if (getArguments() != null) {
            currentRecipe = RezeptFragmentArgs.fromBundle(requireArguments()).getRecipe();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // 1) Binding initialisieren
        binding = FragmentRezeptBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Jetzt ist dieses Fragment an den NavHost angehängt ⇒ Navigation.findNavController(view) funktioniert
        NavController navController = Navigation.findNavController(view);

        if (currentRecipe != null) {
            // 2) Rezept‐Daten in die gebundenen Views setzen
            binding.textRezeptName.setText(currentRecipe.getName());
            binding.textZubereitung.setText(currentRecipe.getGuideText());
            binding.textZeit.setText(currentRecipe.getProcessingTime() + getString(R.string.minuten));
            binding.editPortionen.setText(String.valueOf(currentRecipe.getPortions()));

            standardPortionen = currentRecipe.getPortions();
            binding.editPortionen.setHint(String.valueOf(standardPortionen));
            updateZutatenListe(standardPortionen);

            // 3) Listener: wenn Portionszahl geändert wird, Zutatenliste anpassen
            binding.editPortionen.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
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

            binding.imageRezept.setImageBitmap(manager.getRecipeImage(currentRecipe));

            binding.fabDeleteRezept.setOnClickListener(v -> {
                Snackbar.make(requireView(), getString(R.string.wirklich_löschen), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.ja), v2 -> {
                            boolean deleted = manager.deleteRecipe(currentRecipe);
                            if (deleted) {
                                Toast.makeText(requireContext(), getString(R.string.rezept_gelöscht), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.löschen_fehlgeschlagen), Toast.LENGTH_SHORT).show();
                            }
                            navController.navigate(R.id.FirstFragment);
                        })
                        .show();
            });


            // 4) Sterne‐Buttons initialisieren
            setupRatingButtons();

            // 5) Button: Zutaten zur Einkaufsliste hinzufügen
            binding.buttonAddToShoppingList.setOnClickListener(v -> {
                for (Map.Entry<CheckBox, RecipeIngredient> entry : checkedZutaten.entrySet()) {
                    if (entry.getKey().isChecked()) {
                        RecipeIngredient ri = entry.getValue();
                        manager.addShoppingListItem(ri.getIngredient(), ri.getAmount(), ri.getUnit());
                        entry.getKey().setChecked(false);
                    }
                }
                Toast.makeText(requireContext(),
                        getString(R.string.zutaten_wurden_zur_einkaufsliste_hinzugefügt),
                        Toast.LENGTH_SHORT).show();
            });
        }

        binding.floatingActionButton.setOnClickListener(v -> {
            RezeptFragmentDirections.ActionRezeptToCreateRezept action =
                    RezeptFragmentDirections
                            .actionRezeptToCreateRezept(currentRecipe);
            navController.navigate(action);
        });
    }

    private void setupRatingButtons() {
        int rating = currentRecipe.getRating();
        // Stern‐Icons setzen
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

        // Klick‐Listener
        binding.buttonStar1.setOnClickListener(v -> setRating(1));
        binding.buttonStar2.setOnClickListener(v -> setRating(2));
        binding.buttonStar3.setOnClickListener(v -> setRating(3));
        binding.buttonStar4.setOnClickListener(v -> setRating(4));
        binding.buttonStar5.setOnClickListener(v -> setRating(5));
    }

    private void setRating(int rating) {
        this.currentRating = rating;
        // Stern‐Icons updaten
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
        binding = null; // Remember to free the binding reference
    }
}
