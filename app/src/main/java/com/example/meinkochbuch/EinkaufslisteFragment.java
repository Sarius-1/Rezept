package com.example.meinkochbuch;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;

import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.ShoppingListItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class EinkaufslisteFragment extends Fragment {

    private Collection<ShoppingListItem> einkaufsliste;
    private EinkaufsAdapter adapter;

    private LinkedList<ShoppingListItem> selectedItems = new LinkedList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_einkaufsliste, container, false);

        ListView listView = view.findViewById(R.id.list_einkaufsliste);
        Button deleteButton = view.findViewById(R.id.button_delete_selected);


        einkaufsliste = new ArrayList<>();
        einkaufsliste = RecipeManager.getInstance().getShoppingList();
        adapter = new EinkaufsAdapter(getContext(), einkaufsliste, selectedItems);
        listView.setAdapter(adapter);

        deleteButton.setOnClickListener(v -> {
            List<ShoppingListItem> toRemove = new ArrayList<>(selectedItems);

            for (ShoppingListItem item : toRemove) {
                RecipeManager.getInstance().removeShoppingListItem(item);
            }

            selectedItems.clear();

        });
        return view;
    }}
