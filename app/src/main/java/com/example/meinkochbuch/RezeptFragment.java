package com.example.meinkochbuch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import android.util.Log;


import lombok.Getter;

public class RezeptFragment extends Fragment {

    private static final String ARG_REZEPT = "rezept_objekt";

    private TextView textName, textZutaten, textZubereitung, textZubereitungszeit,textAmount;

    private ImageButton[] starButtons = new ImageButton[5];
    private RatingBar ratingBar;
    private ImageView imageRezept;
    private Recipe currentRecipe;
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
        imageRezept = view.findViewById(R.id.image_rezept);

        if (currentRecipe != null) {
            textName.setText(currentRecipe.getName());
            textZubereitung.setText(currentRecipe.getGuideText());
            textZubereitungszeit.setText(currentRecipe.getProcessingTime() + " Minuten");
            textAmount.setText(currentRecipe.getPortions() + " Portionen");

            StringBuilder zutatenText = new StringBuilder();
            for (RecipeIngredient ri : currentRecipe.getIngredients()) {
                zutatenText.append("- ")
                        .append(ri.getAmount()).append(" ")
                        .append(ri.getUnit().getLocalizedName()).append(" ")
                        .append(ri.getIngredient().getName()).append("\n");
            }
            textZutaten.setText(zutatenText.toString().trim());
            setupRatingButtons(view);

            // Bild anzeigen (lokal oder per URL), z.B. via Glide
           /* if (currentRecipe.getImageUrl() != null && !currentRecipe.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(currentRecipe.getImageUrl())
                        .into(imageRezept);
            } else {
                imageRezept.setImageResource(R.drawable.ic_launcher_foreground); // Fallback
            }*/
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
            starButtons[i].setOnClickListener(v -> {
                setRating(rating);
            });
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
        manager.setRating(currentRecipe,rating);
        }

    }

