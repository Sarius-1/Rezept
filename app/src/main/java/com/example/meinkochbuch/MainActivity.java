package com.example.meinkochbuch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private TextView weatherText;
    private Button shoppingListButton;
    private ImageButton homeButton;

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // NavController abrufen
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);

        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment not found. Check your layout file.");
        }

        NavController navController = navHostFragment.getNavController();

        // Toolbar einrichten (falls vorhanden)
        Toolbar toolbar = findViewById(R.id.toolbar_include);
        setSupportActionBar(toolbar);

        // AppBarConfiguration einrichten
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        // Toolbar mit NavController verbinden
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        Button shoppingListButton = findViewById(R.id.shopping_list_button);
        shoppingListButton.setOnClickListener(v -> {
            navController.navigate(R.id.einkaufsliste);
        });

        homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> {
            navController.navigate(R.id.FirstFragment);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateWeatherInfo() {
        // In a real app, you would fetch data from a weather API
        // This is just a mock update for demonstration
        if (weatherText != null) {
            weatherText.setText("21°C Sonnig");
        }
    }
}
