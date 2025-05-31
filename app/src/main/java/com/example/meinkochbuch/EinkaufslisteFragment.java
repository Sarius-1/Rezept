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

    private static final String TAG = "EinkaufslisteFragment";
    private Collection<ShoppingListItem> einkaufsliste;
    private static EinkaufsAdapter adapter;
    private LinkedList<ShoppingListItem> selectedItems = new LinkedList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_einkaufsliste, container, false);

        ListView listView = view.findViewById(R.id.list_einkaufsliste);
        Button deleteButton = view.findViewById(R.id.button_delete_selected);

        // Debug: Einkaufsliste laden und Größe prüfen
        Log.d(TAG, "Fragment onCreateView aufgerufen");
        einkaufsliste = RecipeManager.getInstance().getShoppingList();
        Log.d(TAG, "Einkaufsliste Größe beim Laden: " + einkaufsliste.size());
        Log.d(TAG, "RecipeManager Instanz: " + RecipeManager.getInstance().toString());

        // Debug: Inhalt der Liste ausgeben
        for (ShoppingListItem item : einkaufsliste) {
            Log.d(TAG, "Item: " + item.getIngredient().getName() + " - " + item.getAmount());
        }

        adapter = new EinkaufsAdapter(getContext(), einkaufsliste, selectedItems);
        listView.setAdapter(adapter);

        // Debug: Adapter-Count prüfen
        Log.d(TAG, "Adapter Count nach setAdapter: " + adapter.getCount());

        deleteButton.setOnClickListener(v -> {
            List<ShoppingListItem> toRemove = new ArrayList<>(selectedItems);
            Log.d(TAG, "Zu löschende Items: " + toRemove.size());

            for (ShoppingListItem item : toRemove) {
                RecipeManager.getInstance().removeShoppingListItem(item);
            }

            selectedItems.clear();
            adapter.refreshData();

            // Debug: Nach dem Löschen
            Log.d(TAG, "Adapter Count nach Löschen: " + adapter.getCount());
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Fragment wird wieder sichtbar");
        if (adapter != null) {
            Log.d(TAG, "onResume - Aktualisiere Adapter");
            adapter.refreshData();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && adapter != null) {
            Log.d(TAG, "Fragment ist jetzt sichtbar - aktualisiere Liste");
            adapter.refreshData();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && adapter != null) {
            Log.d(TAG, "Fragment ist nicht mehr versteckt - aktualisiere Liste");
            adapter.refreshData();
        }
    }

    // Öffentliche Methode zum Aktualisieren der Liste
    public static void refreshShoppingList() {
        Log.d(TAG, "refreshShoppingList aufgerufen");
        if (adapter != null) {
            Collection<ShoppingListItem> currentList = RecipeManager.getInstance().getShoppingList();
            Log.d(TAG, "Aktuelle Liste hat " + currentList.size() + " Elemente");
            adapter.refreshData();
        }
    }
}