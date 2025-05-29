package com.example.meinkochbuch;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meinkochbuch.core.model.Ingredient;
import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.Unit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private TextView cityText;

    private AppBarConfiguration appBarConfiguration;
    private LocationManager locationManager;
    private String city;
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),this::processRequestPermissionResult);

    private final long UPDATE_INTERVAL = TimeUnit.MINUTES.toMillis(5);

    @Deprecated
    private static void test(){
        Log.d("MainActivityTest", "Testing recipes");
        if(!RecipeManager.getInstance().getRecipesByName("Tortilla Columbia").isEmpty()){
            Log.d("MainActivityTest", "Tortilla Columbia exists! Ayyy!");
            return;
        }
        Ingredient ingredient = RecipeManager.getInstance().tryRegisterIngredient("Cocaine");
        Recipe recipe = RecipeManager.getInstance().
                createRecipe("Tortilla Columbia", 10, 2, "Lorem ipsum");
        RecipeManager.getInstance().addIngredient(recipe, ingredient, 5, Unit.PINCH);
        Log.d("MainActivityTest", "Created "+recipe+" and "+ingredient);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecipeManager.init(getApplicationContext());

        test();

        setContentView(R.layout.activity_main);

        // NavController abrufen
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);

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

        ImageButton homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> {
            navController.navigate(R.id.FirstFragment);
        });


        initWeatherCycle();

    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RecipeManager.getInstance().dispose();
    }

    private boolean checkPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void initWeatherCycle(){
        if (checkPermission()) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, 0,
                    location -> {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                city = addresses.get(0).getLocality();
                                Log.d("MainActivity", "City: " + city);
                                new Thread(MainActivity.this::updateWeatherInfo).start();
                            }
                        } catch (IOException e) {
                            Log.w("Weather", "An exception was thrown while building weather service", e);
                        }
                    });
        }
    }

    private void updateWeatherInfo() {
        String url = "https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid="+getString(R.string.open_weather_api_key);
        try{
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                return;
            }
            String response = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining("\n"));
            JSONObject json = new JSONObject(response);
            connection.disconnect();
            Weather weather = new Weather();
            weather.setCity(json.getString("name"));
            if (json.has("main")) {
                JSONObject main = json.getJSONObject("main");
                weather.setTemperature(main.getDouble("temp") - 273.15);
            }
            if(json.has("weather")){
                JSONArray weatherArray = json.getJSONArray("weather");
                if(weatherArray.length() > 0){
                    JSONObject weatherObject = weatherArray.getJSONObject(0);
                    weather.setIcon(retrieveWeateherImage(weatherObject.getString("icon")));
                }
            }

            runOnUiThread(() -> {
                TextView cityView = findViewById(R.id.cityView);
                cityView.setText(weather.getCity());
                TextView temperatureView = findViewById(R.id.tempView);
                temperatureView.setText(String.format("%.1f °C", weather.getTemperature()));
                ImageView weatherImageView = findViewById(R.id.weatherImageView);
                if(weather.getIcon() != null){
                    weatherImageView.setImageBitmap(weather.getIcon());
                }
            });

        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private Bitmap retrieveWeateherImage(String imageName){
        try {
            HttpsURLConnection con = (HttpsURLConnection) new URL("https://openweathermap.org/img/wn/" + imageName + ".png").openConnection();
            Bitmap bitmap = BitmapFactory.decodeStream(con.getInputStream());
            con.disconnect();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void processRequestPermissionResult(Boolean granted) {
        if (granted) {
            // Permission granted, proceed with location access
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            // Permission denied, show a message to the user
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

}
