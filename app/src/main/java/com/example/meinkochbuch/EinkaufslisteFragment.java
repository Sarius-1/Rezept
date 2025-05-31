package com.example.meinkochbuch;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.meinkochbuch.core.model.Ingredient;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.ShoppingListItem;
import com.example.meinkochbuch.core.model.Unit;
import com.example.meinkochbuch.databinding.FragmentEinkaufslisteBinding;

import java.util.ArrayList;
import java.util.List;


public class EinkaufslisteFragment extends Fragment {

    private static final String TAG = "EinkaufslisteFragment";

    // Adapter und Liste statisch, damit andere Klassen (z.B. RezeptFragment) sie aktualisieren können
    private static EinkaufsAdapter adapter;
    private static List<ShoppingListItem> einkaufsliste;

    // Binding-Instanz
    private FragmentEinkaufslisteBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // 1) ViewBinding initialisieren
        binding = FragmentEinkaufslisteBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // 2) Einkaufsliste aus RecipeManager laden (vorab aus Datenbank)
        einkaufsliste = new ArrayList<>(RecipeManager.getInstance().getShoppingList());

        // 3) Adapter initialisieren und an RecyclerView anhängen
        adapter = new EinkaufsAdapter(einkaufsliste);
        binding.recyclerViewItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewItems.setAdapter(adapter);

        // 4) „Löschen“-Button: markierte Einträge in der Shopping-Liste entfernen
        binding.buttonDeleteSelected.setOnClickListener(v -> adapter.deleteCheckedItems());

        // 5) Spinner mit allen Einheiten („Unit“) befüllen
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                java.util.Arrays.stream(Unit.values())
                        .map(Unit::getLocalizedName)
                        .toArray(String[]::new)
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEinheit.setAdapter(unitAdapter);

        // 6) „Hinzufügen“-Button: neues ShoppingListItem anlegen
        binding.buttonAddItem.setOnClickListener(v -> {
            String amountText = binding.etMenge.getText().toString().trim();
            String nameText   = binding.etZutat.getText().toString().trim();

            if (TextUtils.isEmpty(amountText)) {
                Toast.makeText(requireContext(), "Bitte Menge eingeben", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(nameText)) {
                Toast.makeText(requireContext(), "Bitte Zutat eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    Toast.makeText(requireContext(), "Menge muss größer 0 sein", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Ungültige Zahl", Toast.LENGTH_SHORT).show();
                return;
            }


            String selectedLabel = (String) binding.spinnerEinheit.getSelectedItem();
            Unit selectedUnit = null;
            for (Unit u : Unit.values()) {
                if (u.getLocalizedName().equals(selectedLabel)) {
                    selectedUnit = u;
                    break;
                }
            }
            Ingredient ingredient = RecipeManager.getInstance().tryRegisterIngredient(nameText);

            RecipeManager.getInstance()
                    .addShoppingListItem(ingredient, amount, selectedUnit);

            refreshShoppingList();

            binding.etMenge.setText("");
            binding.etZutat.setText("");

            // 6.h) Bestätigung per Toast
            Toast.makeText(requireContext(),
                    "Zutat „" + nameText + "“ (" + amount + " " + selectedUnit.getLocalizedName() + ") hinzugefügt",
                    Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume – Adapter aktualisieren");
        refreshShoppingList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Wichtig: Binding-Referenz freigeben, um Memory Leaks zu vermeiden
        binding = null;
    }

    public static void refreshShoppingList() {
        if (adapter == null || einkaufsliste == null) {
            Log.w(TAG, "refreshShoppingList: Adapter oder Liste noch nicht initialisiert.");
            return;
        }
        Log.d(TAG, "refreshShoppingList: Lade aktuelle Einträge");
        einkaufsliste.clear();
        einkaufsliste.addAll(RecipeManager.getInstance().getShoppingList());
        adapter.notifyDataSetChanged();
    }
}
