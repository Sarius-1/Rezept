package com.example.meinkochbuch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.ShoppingListItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class EinkaufsAdapter extends ArrayAdapter<ShoppingListItem> {

    private static final String TAG = "EinkaufsAdapter";
    private LinkedList<ShoppingListItem> selectedItems;

    public EinkaufsAdapter(Context context, Collection<ShoppingListItem> items, LinkedList<ShoppingListItem> selectedItem) {
        super(context, 0, new ArrayList<>(items));
        this.selectedItems = selectedItem;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ShoppingListItem item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_einkauf, parent, false);
        }

        TextView mengeView = convertView.findViewById(R.id.item_amount);
        TextView nameView = convertView.findViewById(R.id.item_name);
        CheckBox checkBox = convertView.findViewById(R.id.item_checkbox);

        mengeView.setText(String.valueOf(item.getAmount()+" "+item.getUnit().getLocalizedName()));
        nameView.setText(item.getIngredient().getName());
        checkBox.setChecked(selectedItems.contains(item));

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedItems.add(item);
            } else {
                selectedItems.remove(item);
            }
        });

        return convertView;
    }

    // Methode zum Aktualisieren der Daten
    public void refreshData() {
        Collection<ShoppingListItem> newData = RecipeManager.getInstance().getShoppingList();

        clear();
        addAll(newData);
        notifyDataSetChanged();

    }
}