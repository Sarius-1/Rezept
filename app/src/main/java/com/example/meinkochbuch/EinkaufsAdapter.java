package com.example.meinkochbuch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.ShoppingListItem;

import java.util.Iterator;
import java.util.List;

public class EinkaufsAdapter extends RecyclerView.Adapter<EinkaufsAdapter.ViewHolder> {

    private final List<ShoppingListItem> items;

    public EinkaufsAdapter(List<ShoppingListItem> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText anzahl;
        TextView name;
        CheckBox checkbox;

        public ViewHolder(View itemView) {
            super(itemView);
            anzahl = itemView.findViewById(R.id.anzahl);
            name = itemView.findViewById(R.id.item_name);
            checkbox = itemView.findViewById(R.id.item_checkbox);
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
        holder.name.setText(item.getIngredient().getName());
        holder.checkbox.setChecked(item.isChecked());

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
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
