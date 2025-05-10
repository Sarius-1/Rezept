package com.example.meinkochbuch;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateRezept#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateRezept extends Fragment {

    // Parameter-Argumente für die Initialisierung
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Parameter-Variablen
    private String mParam1;
    private String mParam2;

    public CreateRezept() {
        // Erforderlicher leerer öffentlicher Konstruktor
    }

    /**
     * Factory-Methode zur Erstellung einer neuen Instanz dieses Fragments
     * mit den angegebenen Parametern.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return Eine neue Instanz des Fragments CreateRezept.
     */
    public static CreateRezept newInstance(String param1, String param2) {
        CreateRezept fragment = new CreateRezept();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Layout für dieses Fragment aufblähen
        return inflater.inflate(R.layout.fragment_create_rezept, container, false);
    }
}