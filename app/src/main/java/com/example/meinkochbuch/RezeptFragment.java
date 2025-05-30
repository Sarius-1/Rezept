package com.example.meinkochbuch;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeIngredient;
import com.example.meinkochbuch.core.model.RecipeManager;

import java.io.Serializable;
import java.util.Locale;

import lombok.Getter;

public class RezeptFragment extends Fragment {

    private static final String ARG_REZEPT = "rezept_objekt";

    private TextView textName, textZutaten, textZubereitung, textZubereitungszeit, textAmount;
    private EditText editPortionen;

    private ImageButton[] starButtons = new ImageButton[5];
    private RatingBar ratingBar;
    private ImageView imageRezept;
    private Recipe currentRecipe;
    private int standardPortionen = 1;
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

        Log.d("RezeptFragment", "Rating: " + currentRecipe);
        textName = view.findViewById(R.id.text_rezept_name);
        textZubereitungszeit = view.findViewById(R.id.text_zeit);
        textAmount = view.findViewById(R.id.text_amount);
        textZutaten = view.findViewById(R.id.text_zutaten);
        textZubereitung = view.findViewById(R.id.text_zubereitung);
        editPortionen = view.findViewById(R.id.edit_portionen);
        imageRezept = view.findViewById(R.id.image_rezept);

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
        StringBuilder zutatenText = new StringBuilder();
        float faktor = portionen / (float) standardPortionen;
        for (RecipeIngredient ri : currentRecipe.getIngredients()) {
            double menge = ri.getAmount() * faktor;
            zutatenText.append("- ")
                    .append(String.format(Locale.getDefault(), "%.2f", menge)).append(" ")
                    .append(ri.getUnit().getLocalizedName()).append(" ")
                    .append(ri.getIngredient().getName()).append("\n");
        }
        textZutaten.setText(zutatenText.toString().trim());
    }
}
