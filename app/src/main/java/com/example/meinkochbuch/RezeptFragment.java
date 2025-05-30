package com.example.meinkochbuch;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeIngredient;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.ShoppingListItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;

public class RezeptFragment extends Fragment {

    private static final String ARG_REZEPT = "rezept_objekt";

    private TextView textName, textZubereitung, textZubereitungszeit, textAmount;
    private EditText editPortionen;
    private LinearLayout zutatenLayout;
    private Button addToShoppingListButton;

    private ImageButton[] starButtons = new ImageButton[5];
    private ImageView imageRezept;
    private Recipe currentRecipe;
    private int standardPortionen = 1;
    private final Map<CheckBox, RecipeIngredient> checkedZutaten = new HashMap<>();
    RecipeManager manager = RecipeManager.getInstance();

    @Getter
    private int currentRating;

    public static RezeptFragment newInstance(Recipe rezept) {
        RezeptFragment fragment = new RezeptFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REZEPT, (Serializable) rezept);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentRecipe = RezeptFragmentArgs.fromBundle(requireArguments()).getRecipe();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rezept, container, false);

        textName = view.findViewById(R.id.text_rezept_name);
        textZubereitungszeit = view.findViewById(R.id.text_zeit);
        textAmount = view.findViewById(R.id.text_amount);
        textZubereitung = view.findViewById(R.id.text_zubereitung);
        editPortionen = view.findViewById(R.id.edit_portionen);
        imageRezept = view.findViewById(R.id.image_rezept);
        zutatenLayout = view.findViewById(R.id.layout_zutaten_liste);
        addToShoppingListButton = view.findViewById(R.id.button_add_to_shopping_list);

        if (currentRecipe != null) {
            textName.setText(currentRecipe.getName());
            textZubereitung.setText(currentRecipe.getGuideText());
            textZubereitungszeit.setText(currentRecipe.getProcessingTime() + " Minuten");
            textAmount.setText(currentRecipe.getPortions() + " Portionen");

            standardPortionen = currentRecipe.getPortions();
            editPortionen.setHint(String.valueOf(standardPortionen));
            updateZutatenListe(standardPortionen);

            editPortionen.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    int neuePortionen = standardPortionen;
                    try {
                        neuePortionen = Integer.parseInt(s.toString());
                        if (neuePortionen < 1) neuePortionen = 1;
                    } catch (NumberFormatException ignored) {}
                    updateZutatenListe(neuePortionen);
                }
            });

            setupRatingButtons(view);

            addToShoppingListButton.setOnClickListener(v -> {
                for (Map.Entry<CheckBox, RecipeIngredient> entry : checkedZutaten.entrySet()) {
                    if (entry.getKey().isChecked()) {
                        RecipeIngredient ri = entry.getValue();
                        manager.addShoppingListItem(ri.getIngredient(),ri.getAmount(),ri.getUnit());

                    }
                }
            });
        }

        return view;
    }

    private void setupRatingButtons(View view) {
        int currentRating = currentRecipe.getRating();
        starButtons[0] = view.findViewById(R.id.button_star_1);
        starButtons[1] = view.findViewById(R.id.button_star_2);
        starButtons[2] = view.findViewById(R.id.button_star_3);
        starButtons[3] = view.findViewById(R.id.button_star_4);
        starButtons[4] = view.findViewById(R.id.button_star_5);

        for (int i = 0; i < starButtons.length; i++) {
            final int rating = i + 1;
            starButtons[i].setOnClickListener(v -> setRating(rating));
        }

        for (int i = 1; i <= 5; i++) {
            starButtons[i - 1].setImageResource(
                    i <= currentRating ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
            );
        }
    }

    private void setRating(int rating) {
        currentRating = rating;
        for (int i = 1; i <= 5; i++) {
            starButtons[i - 1].setImageResource(
                    i <= rating ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
            );
        }
        manager.setRating(currentRecipe, rating);
    }

    private void updateZutatenListe(int portionen) {
        if (currentRecipe == null) return;
        zutatenLayout.removeAllViews();
        checkedZutaten.clear();
        float faktor = portionen / (float) standardPortionen;
        for (RecipeIngredient ri : currentRecipe.getIngredients()) {
            double menge = ri.getAmount() * faktor;
            String text = String.format(Locale.getDefault(), "%.2f %s %s",
                    menge,
                    ri.getUnit().getLocalizedName(),
                    ri.getIngredient().getName());
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(text);
            zutatenLayout.addView(checkBox);
            checkedZutaten.put(checkBox, ri);
        }
    }
}
