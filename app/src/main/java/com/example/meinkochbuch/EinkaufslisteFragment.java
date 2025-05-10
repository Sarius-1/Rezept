package com.example.meinkochbuch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EinkaufslisteFragment extends Fragment {

    private List<EinkaufsItem> einkaufsliste;
    private EinkaufsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_einkaufsliste, container, false);

        ListView listView = view.findViewById(R.id.list_einkaufsliste);
        Button deleteButton = view.findViewById(R.id.button_delete_selected);

        einkaufsliste = new ArrayList<>();
        einkaufsliste.add(new EinkaufsItem("Milch", 2));
        einkaufsliste.add(new EinkaufsItem("Eier", 10));
        einkaufsliste.add(new EinkaufsItem("Brot", 1));

        adapter = new EinkaufsAdapter(getContext(), einkaufsliste);
        listView.setAdapter(adapter);

        deleteButton.setOnClickListener(v -> {
            Iterator<EinkaufsItem> iterator = einkaufsliste.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().isAusgewaehlt()) {
                    iterator.remove();
                }
            }
            adapter.notifyDataSetChanged();
        });

        return view;
    }
}
