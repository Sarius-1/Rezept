package com.example.meinkochbuch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.ShoppingListItem;
import com.example.meinkochbuch.core.model.Unit;

import java.util.Iterator;
import java.util.List;

public class EinkaufsAdapter extends RecyclerView.Adapter<EinkaufsAdapter.ViewHolder> {

    private final List<ShoppingListItem> items;

    public EinkaufsAdapter(List<ShoppingListItem> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText anzahl;
        Spinner spinnerEinheit;
        TextView name;
        CheckBox checkbox;

        public ViewHolder(View itemView) {
            super(itemView);
            anzahl = itemView.findViewById(R.id.anzahl);
            spinnerEinheit = itemView.findViewById(R.id.spinner_einheit);
            name = itemView.findViewById(R.id.item_name);
            checkbox = itemView.findViewById(R.id.item_checkbox);

            ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                    itemView.getContext(),
                    android.R.layout.simple_spinner_item,
                    java.util.Arrays.stream(Unit.values())
                            .map(Unit::getLocalizedName)
                            .toArray(String[]::new)
            );
            unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEinheit.setAdapter(unitAdapter);

        }
    }

    @NonNull
    @Override
    public EinkaufsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_einkauf, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EinkaufsAdapter.ViewHolder holder, int position) {
        ShoppingListItem item = items.get(position);

        holder.anzahl.setText(String.valueOf(item.getAmount()));
        int unitIndex = item.getUnit().ordinal();
        holder.spinnerEinheit.setSelection(unitIndex);
        holder.name.setText(item.getIngredient().getName());
        holder.checkbox.setChecked(item.isChecked());

        holder.spinnerEinheit.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                Unit selectedUnit = Unit.values()[pos];
                if (item.getUnit() != selectedUnit) {
                    RecipeManager.getInstance().setShoppingListItemAmount(item, item.getAmount());
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        holder.anzahl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    double value = Double.parseDouble(holder.anzahl.getText().toString());
                    RecipeManager.getInstance().setShoppingListItemAmount(item, value);
                } catch (NumberFormatException e) {
                    holder.anzahl.setError("Ungültige Zahl");
                }
            }
        });

        // Checkbox-Status umbiegen
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
        });

        holder.spinnerEinheit.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                Unit selectedUnit = Unit.values()[pos];
                if (item.getUnit() != selectedUnit) {
                    RecipeManager.getInstance().setShoppingListItemUnit(item, selectedUnit);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void deleteCheckedItems() {
        Iterator<ShoppingListItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            ShoppingListItem item = iterator.next();
            if (item.isChecked()) {
                RecipeManager.getInstance().removeShoppingListItem(item);
                iterator.remove();
            }
        }
        notifyDataSetChanged();
    }
}
