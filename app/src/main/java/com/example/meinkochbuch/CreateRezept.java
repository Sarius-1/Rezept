package com.example.meinkochbuch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.example.meinkochbuch.R;

public class CreateRezept extends Fragment {

    private LinearLayout zutatenContainer;
    private Button btnZutatHinzufuegen;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_rezept, container, false);

        zutatenContainer = rootView.findViewById(R.id.container_zutaten);
        btnZutatHinzufuegen = rootView.findViewById(R.id.btn_zutat_hinzufuegen);

        // Funktion zum Hinzufügen eines neuen Zutaten-Views
        View.OnClickListener addZutatView = v -> {
            View zutatView = inflater.inflate(R.layout.item_zutat, zutatenContainer, false);
            ImageButton btnEntfernen = zutatView.findViewById(R.id.btn_entfernen);
            btnEntfernen.setOnClickListener(v1 -> zutatenContainer.removeView(zutatView));
            zutatenContainer.addView(zutatView);
        };

        // Button klick -> neues Feld hinzufügen
        btnZutatHinzufuegen.setOnClickListener(addZutatView);

        // Direkt ein erstes Feld anzeigen
        addZutatView.onClick(null);

        return rootView;
    }
}