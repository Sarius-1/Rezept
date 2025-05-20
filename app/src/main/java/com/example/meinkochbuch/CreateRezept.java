package com.example.meinkochbuch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.example.meinkochbuch.R;
import com.google.android.material.textfield.TextInputEditText;

public class CreateRezept extends Fragment {

    private LinearLayout zutatenContainer;
    private Button btnZutatHinzufuegen;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_rezept, container, false);

        zutatenContainer = view.findViewById(R.id.container_zutaten);
        btnZutatHinzufuegen = view.findViewById(R.id.btn_zutat_hinzufuegen);

        btnZutatHinzufuegen.setOnClickListener(v -> {
            View zutatView = inflater.inflate(R.layout.item_zutat, null);
            zutatenContainer.addView(zutatView);
        });

        return view;
    }
}