package com.example.meinkochbuch.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.meinkochbuch.R;
import com.example.meinkochbuch.adapter.ShoppingListAdapter;
import com.example.meinkochbuch.core.model.Ingredient;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.ShoppingListItem;
import com.example.meinkochbuch.core.model.Unit;
import com.example.meinkochbuch.databinding.FragmentShoppingListBinding;

import java.util.ArrayList;
import java.util.List;


public class ShoppingListFragment extends Fragment {



    private static ShoppingListAdapter adapter;
    private static List<ShoppingListItem> einkaufsliste;

    private FragmentShoppingListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // 1) ViewBinding initialisieren
        binding = FragmentShoppingListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // 2) Einkaufsliste aus RecipeManager laden (vorab aus Datenbank)
        einkaufsliste = new ArrayList<>(RecipeManager.getInstance().getShoppingList());

        // 3) Adapter initialisieren und an RecyclerView anhängen
        adapter = new ShoppingListAdapter(einkaufsliste);
        binding.recyclerViewItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewItems.setAdapter(adapter);

        // 4) „Löschen“-Button: markierte Einträge in der Shopping-Liste entfernen
        binding.buttonDeleteSelected.setOnClickListener(v -> adapter.deleteCheckedItems());


        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                java.util.Arrays.stream(Unit.values())
                        .map(Unit::getLocalizedName)
                        .toArray(String[]::new)
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEinheit.setAdapter(unitAdapter);


        binding.buttonAddItem.setOnClickListener(v -> {
            String amountText = binding.etMenge.getText().toString().trim();
            String nameText   = binding.etZutat.getText().toString().trim();

            if (TextUtils.isEmpty(amountText)) {
                Toast.makeText(requireContext(), getString(R.string.bitte_menge_eingeben), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(nameText)) {
                Toast.makeText(requireContext(), getString(R.string.bitte_zutat_eingeben), Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    Toast.makeText(requireContext(), getString(R.string.menge_muss_größer_0_sein), Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), getString(R.string.ungültige_zahl), Toast.LENGTH_SHORT).show();
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

            assert ingredient != null;
            RecipeManager.getInstance()
                    .addShoppingListItem(ingredient, amount, selectedUnit);

            refreshShoppingList();

            binding.etMenge.setText("");
            binding.etZutat.setText("");

            // 6.h) Bestätigung per Toast
            Toast.makeText(requireContext(),
                    getString(R.string.Zutaten) + nameText + "“ (" + amount + " " + selectedUnit.getLocalizedName() + ") hinzugefügt",
                    Toast.LENGTH_SHORT).show();

        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshShoppingList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static void refreshShoppingList() {
        if (adapter == null || einkaufsliste == null) {

            return;
        }
        einkaufsliste.clear();
        einkaufsliste.addAll(RecipeManager.getInstance().getShoppingList());
        adapter.notifyDataSetChanged();
    }
}
