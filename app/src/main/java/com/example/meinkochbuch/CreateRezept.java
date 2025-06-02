package com.example.meinkochbuch;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.meinkochbuch.core.model.Category;
import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeIngredient;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.Unit;
import com.example.meinkochbuch.databinding.FragmentCreateRezeptBinding;
import com.example.meinkochbuch.databinding.ItemZutatBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateRezept extends Fragment {
    private FragmentCreateRezeptBinding binding;
    private CreateRezeptViewModel viewModel;
    // … innerhalb von CreateRezept, oberhalb von onCreateView(…):
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == requireActivity().RESULT_OK
                                && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            viewModel.setImageUri(uri);
                            binding.imageRezept.setImageURI(uri);
                        }
                    });

    private final ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    success -> {
                        Uri temp = viewModel.getImageUri().getValue();
                        if (success && temp != null) {
                            binding.imageRezept.setImageURI(temp);
                        }
                    });

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            launchCameraIntent();
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    getString(R.string.toast_camera_permission_denied),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentCreateRezeptBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity())
                .get(CreateRezeptViewModel.class);

        // 1) Prüfen, ob beim Navigieren ein Recipe mitgegeben wurde
        Recipe passedRecipe;
        if (getArguments() != null) {
            passedRecipe = CreateRezeptArgs.fromBundle(getArguments()).getCurrentEditedRecipe();
        } else {
            passedRecipe = null;
        }

        if (passedRecipe != null) {
            // Name, Portions, Time, Description
            viewModel.setName(passedRecipe.getName());
            viewModel.setPortions(String.valueOf(passedRecipe.getPortions()));
            viewModel.setTime(String.valueOf(passedRecipe.getProcessingTime()));
            viewModel.setDescription(passedRecipe.getGuideText());


            Bitmap image = RecipeManager.getInstance().getRecipeImage(passedRecipe);
            if (image != null) {
                viewModel.setImage(image);
            }

            // Kategorien
            viewModel.setVegan(passedRecipe.getCategories().contains(Category.VEGAN));
            viewModel.setVegetarian(passedRecipe.getCategories().contains(Category.VEGETARIAN));
            viewModel.setGlutenFree(passedRecipe.getCategories().contains(Category.GLUTEN_FREE));
            viewModel.setLactoseFree(passedRecipe.getCategories().contains(Category.LACTOSE_FREE));

            // Zutaten‐Einträge: aus RecipeIngredient in IngredientEntry übersetzen
            List<CreateRezeptViewModel.IngredientEntry> entries = new ArrayList<>();
            for (RecipeIngredient ri : passedRecipe.getIngredients()) {
                CreateRezeptViewModel.IngredientEntry entry = new CreateRezeptViewModel.IngredientEntry();
                entry.setMenge(String.valueOf(ri.getAmount()));
                entry.setUnit(ri.getUnit());
                entry.setZutatName(ri.getIngredient().getName());
                entries.add(entry);
            }
            // Mindestens eine Zeile, falls Recipe keine Zutaten hat
            if (entries.isEmpty()) {
                entries.add(new CreateRezeptViewModel.IngredientEntry());
            }
            viewModel.setIngredients(entries);
            binding.btnRezeptSpeichern.setText(getString(R.string.rezept_aktualisieren));
        }

        // 3) Falls noch keine IngredientEntry im ViewModel ist (z.B. erster Aufruf ohne Recipe)
        if (viewModel.getIngredients().getValue() == null) {
            viewModel.addIngredientEntry();
        }

        // 4) Alle LiveData‐Felder aus dem ViewModel in die UI zurückschreiben:
        if (viewModel.getName().getValue() != null) {
            binding.etRezeptName.setText(viewModel.getName().getValue());
        }
        if (viewModel.getPortions().getValue() != null) {
            binding.etPortionen.setText(viewModel.getPortions().getValue());
        }
        if (viewModel.getTime().getValue() != null) {
            binding.etZubereitungszeit.setText(viewModel.getTime().getValue());
        }
        if (viewModel.getDescription().getValue() != null) {
            binding.etZubereitung.setText(viewModel.getDescription().getValue());
        }
        if (viewModel.getImageUri().getValue() != null) {
            binding.imageRezept.setImageURI(viewModel.getImageUri().getValue());
        }
        if (viewModel.getImage().getValue() != null) {
            binding.imageRezept.setImageBitmap(viewModel.getImage().getValue());
        }
        binding.checkboxVegan.setChecked(Boolean.TRUE.equals(viewModel.getIsVegan().getValue()));
        binding.checkboxVegetarian.setChecked(Boolean.TRUE.equals(viewModel.getIsVegetarian().getValue()));
        binding.checkboxGlutenFree.setChecked(Boolean.TRUE.equals(viewModel.getIsGlutenFree().getValue()));
        binding.checkboxLactoseFree.setChecked(Boolean.TRUE.equals(viewModel.getIsLactoseFree().getValue()));

        // 5) Sobald der Nutzer tippt, die LiveData im ViewModel aktualisieren
        binding.etRezeptName.addTextChangedListener(new SimpleTextWatcher(s ->
                viewModel.setName(s)
        ));
        binding.etPortionen.addTextChangedListener(new SimpleTextWatcher(s ->
                viewModel.setPortions(s)
        ));
        binding.etZubereitungszeit.addTextChangedListener(new SimpleTextWatcher(s ->
                viewModel.setTime(s)
        ));
        binding.etZubereitung.addTextChangedListener(new SimpleTextWatcher(s ->
                viewModel.setDescription(s)
        ));
        binding.checkboxVegan.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setVegan(isChecked)
        );
        binding.checkboxVegetarian.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setVegetarian(isChecked)
        );
        binding.checkboxGlutenFree.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setGlutenFree(isChecked)
        );
        binding.checkboxLactoseFree.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setLactoseFree(isChecked)
        );

        // 6) Zutaten‐Einträge neu aufbauen (erstes Mal)
        refreshIngredientViews();

        // 7) “Zutat hinzufügen“ → ViewModel updaten + UI neu zeichnen
        binding.btnZutatHinzufuegen.setOnClickListener(v -> {
            viewModel.addIngredientEntry();
            refreshIngredientViews();
        });

        // 8) Bild‐Auswahl‐Button
        binding.fabBildAuswaehlen.setOnClickListener(v -> showImagePickerDialog());

        // 9) “Rezept speichern“
        binding.btnRezeptSpeichern.setOnClickListener(v -> {
            // a) ImageUri ist schon aktuell im ViewModel (durch Listener oben),
            //    Name/Portions/... sind ebenfalls aktuell, da wir sie live mitschreiben.
            // b) Zutaten‐Einträge aus UI ins ViewModel kopieren
            List<CreateRezeptViewModel.IngredientEntry> entries =
                    viewModel.getIngredients().getValue();
            if (entries != null) {
                for (int i = 0; i < binding.containerZutaten.getChildCount(); i++) {
                    ItemZutatBinding itemBinding = ItemZutatBinding.bind(
                            binding.containerZutaten.getChildAt(i));

                    String mengeText = itemBinding.etMenge.getText() != null
                            ? itemBinding.etMenge.getText().toString().trim()
                            : "";
                    entries.get(i).setMenge(mengeText);

                    String zutatText = itemBinding.etZutat.getText() != null
                            ? itemBinding.etZutat.getText().toString().trim()
                            : "";
                    entries.get(i).setZutatName(zutatText);

                    Unit selectedUnit = (Unit) itemBinding.spinnerEinheit.getSelectedItem();
                    entries.get(i).setUnit(selectedUnit);
                }
                viewModel.setIngredients(entries);
            }

            if (passedRecipe == null) {
                String error = viewModel.saveRecipe();
                if (error != null) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    return;
                }

                // d) Erfolgsmeldung & Reset
                Toast.makeText(requireContext(),
                        getString(R.string.toast_recipe_saved),
                        Toast.LENGTH_SHORT).show();
                resetForm();
            }
            else {
                // c) Rezept aktualisieren
                String error = viewModel.updateRecipe(passedRecipe);
                if (error != null) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    return;
                }

                // d) Erfolgsmeldung & Reset
                Toast.makeText(requireContext(),
                        getString(R.string.toast_recipe_saved),
                        Toast.LENGTH_SHORT).show();
                resetForm();
            }
            // e) Zurück navigieren
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.FirstFragment);
        });

        // 10) „Alles löschen“-Button ganz unten rechts (falls vorhanden)
        binding.buttonClearAll.setOnClickListener(v -> {
            resetForm();
        });

        return binding.getRoot();
    }



    /** Baut alle IngredientEntry‐Zeilen aus dem ViewModel in den Container. */
    private void refreshIngredientViews() {
        binding.containerZutaten.removeAllViews();
        List<CreateRezeptViewModel.IngredientEntry> list =
                viewModel.getIngredients().getValue();
        if (list == null) return;

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < list.size(); i++) {
            ItemZutatBinding itemBinding =
                    ItemZutatBinding.inflate(inflater, binding.containerZutaten, false);
            CreateRezeptViewModel.IngredientEntry entry = list.get(i);

            // 1) Menge aus ViewModel in EditText
            String mengeVal = entry.getMenge().getValue();
            itemBinding.etMenge.setText(mengeVal != null ? mengeVal : "");

            // 2) Spinner füllen + selektierten Wert setzen
            ArrayAdapter<Unit> unitAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    Unit.values()
            ) {
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    Unit u = getItem(position);
                    if (u != null) ((android.widget.TextView) v).setText(u.getLocalizedName());
                    return v;
                }
                @NonNull
                @Override
                public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                    View v = super.getDropDownView(position, convertView, parent);
                    Unit u = getItem(position);
                    if (u != null) ((android.widget.TextView) v).setText(u.getLocalizedName());
                    return v;
                }
            };
            unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            itemBinding.spinnerEinheit.setAdapter(unitAdapter);

            Unit cur = entry.getUnit().getValue();
            if (cur != null) {
                itemBinding.spinnerEinheit.setSelection(cur.ordinal());
            }
            itemBinding.spinnerEinheit.setOnItemSelectedListener(
                    new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent,
                                                   View view, int pos, long id) {
                            entry.setUnit(Unit.values()[pos]);
                        }
                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) { }
                    });

            // 3) Zutat‐Name in EditText setzen
            String zName = entry.getZutatName().getValue();
            itemBinding.etZutat.setText(zName != null ? zName : "");

            // 4) Löschen‐Button: Diese Zeile aus ViewModel entfernen + UI neu zeichnen
            int index = i;
            itemBinding.btnEntfernen.setOnClickListener(v -> {
                viewModel.removeIngredientEntry(index);
                refreshIngredientViews();
            });

            binding.containerZutaten.addView(itemBinding.getRoot());
        }
    }

    private void showImagePickerDialog() {
        String[] options = { getString(R.string.option_gallery),
                getString(R.string.option_camera) };
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_select_image_title))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent galleryIntent = new Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        );
                        imagePickerLauncher.launch(galleryIntent);
                    } else {
                        requestCameraPermissionLauncher.launch(
                                android.Manifest.permission.CAMERA);
                    }
                })
                .show();
    }

    private void launchCameraIntent() {
        File imageFile = new File(requireContext().getCacheDir(), "temp_image.jpg");
        Uri temp = FileProvider.getUriForFile(
                requireContext(),
                "com.example.meinkochbuch.fileprovider",
                imageFile
        );
        viewModel.setImageUri(temp);
        takePhotoLauncher.launch(temp);
    }

    private void resetForm() {
        // 1) EditTexts, CheckBoxen, ImageView leeren
        binding.etRezeptName.setText("");
        binding.etZubereitung.setText("");
        binding.etZubereitungszeit.setText("");
        binding.etPortionen.setText("");
        binding.imageRezept.setImageDrawable(null);

        binding.checkboxVegan.setChecked(false);
        binding.checkboxVegetarian.setChecked(false);
        binding.checkboxGlutenFree.setChecked(false);
        binding.checkboxLactoseFree.setChecked(false);
        viewModel.setImageUri(null);
        viewModel.setImage(null);

        // 2) ViewModel: Zutatenliste zurücksetzen
        List<CreateRezeptViewModel.IngredientEntry> list =
                viewModel.getIngredients().getValue();
        if (list != null) {
            list.clear();
        }
        viewModel.addIngredientEntry();
        refreshIngredientViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Binding freigeben
    }

    /** Ein einfacher TextWatcher, der nur afterTextChanged(String) kennt. */
    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final java.util.function.Consumer<String> callback;
        protected SimpleTextWatcher(java.util.function.Consumer<String> callback) {
            this.callback = callback;
        }
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
        @Override public void afterTextChanged(android.text.Editable s) {
            callback.accept(s.toString());
        }
    }
}
