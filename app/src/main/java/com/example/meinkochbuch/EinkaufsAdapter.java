package com.example.meinkochbuch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class EinkaufsAdapter extends ArrayAdapter<EinkaufsItem> {

    public EinkaufsAdapter(Context context, List<EinkaufsItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EinkaufsItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_einkauf, parent, false);
        }

        TextView mengeView = convertView.findViewById(R.id.item_amount);
        TextView nameView = convertView.findViewById(R.id.item_name);
        CheckBox checkBox = convertView.findViewById(R.id.item_checkbox);

        mengeView.setText(String.valueOf(item.getMenge()));
        nameView.setText(item.getName());
        checkBox.setChecked(item.isAusgewaehlt());

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                item.setAusgewaehlt(isChecked));

        return convertView;
    }
}
