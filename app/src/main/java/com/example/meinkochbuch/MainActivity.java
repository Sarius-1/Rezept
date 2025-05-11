package com.example.meinkochbuch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView weatherText;
    private Button shoppingListButton;
    private ImageButton homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar einrichten
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            // Prüfen, ob ActionBar nicht null ist, bevor wir darauf zugreifen
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false); // Titel in der Toolbar ausblenden
            }
        } else {
            Log.e("MainActivity", "Toolbar konnte nicht gefunden werden");
        }

        // UI-Elemente initialisieren
        shoppingListButton = findViewById(R.id.shopping_list_button);
        homeButton = findViewById(R.id.home_button);

        // Event-Handler hinzufügen
        if (shoppingListButton != null) {
            shoppingListButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Hier Code für den Einkaufslisten-Button einfügen
                    Toast.makeText(MainActivity.this, "Einkaufsliste geöffnet", Toast.LENGTH_SHORT).show();
                    // z.B. startActivity(new Intent(MainActivity.this, ShoppingListActivity.class));
                }
            });
        }

        if (homeButton != null) {
            homeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Hier Code für den Home-Button einfügen
                    Toast.makeText(MainActivity.this, "Home Button gedrückt", Toast.LENGTH_SHORT).show();
                    // z.B. Navigation zur Startseite oder App neu laden
                }
            });
        }

        // Hier später den Wetter-Text aktualisieren
        updateWeatherInfo();
    }

    private void updateWeatherInfo() {
        // In einer echten App würdest du hier eine Wetter-API abfragen
        // Dies ist nur ein Beispiel
        if (weatherText != null) {
            weatherText.setText("21°C Sonnig");
        }
    }
}