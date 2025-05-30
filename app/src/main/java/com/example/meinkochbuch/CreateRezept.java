package com.example.meinkochbuch;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.meinkochbuch.core.model.Category;
import com.example.meinkochbuch.core.model.Ingredient;
import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.Unit;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;

public class CreateRezept extends Fragment {

    private LinearLayout ingredientContainer;
    private Button btnAddIngredient;
    private Button btnSaveRecipe;
    private ImageView imageView;
    private FloatingActionButton fabBildAuswaehlen;
    private Uri selectedImageUri;
    private Uri tempImageUri;

    private CheckBox cbVegan, cbVegetarian, cbGlutenFree, cbLactoseFree;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imageView.setImageURI(selectedImageUri);
                }
            });

    private final ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && tempImageUri != null) {
                    selectedImageUri = tempImageUri;
                    imageView.setImageURI(tempImageUri);
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchCameraIntent();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.toast_camera_permission_denied), Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_rezept, container, false);

        ingredientContainer = rootView.findViewById(R.id.container_zutaten);
        btnAddIngredient = rootView.findViewById(R.id.btn_zutat_hinzufuegen);
        btnSaveRecipe = rootView.findViewById(R.id.btn_rezept_speichern);
        imageView = rootView.findViewById(R.id.image_rezept);
        fabBildAuswaehlen = rootView.findViewById(R.id.fab_bild_auswaehlen);

        cbVegan = rootView.findViewById(R.id.checkbox_vegan);
        cbVegetarian = rootView.findViewById(R.id.checkbox_vegetarian);
        cbGlutenFree = rootView.findViewById(R.id.checkbox_gluten_free);
        cbLactoseFree = rootView.findViewById(R.id.checkbox_lactose_free);

        btnAddIngredient.setOnClickListener(v -> addIngredientView(inflater));
        fabBildAuswaehlen.setOnClickListener(v -> showImagePickerDialog());
        btnSaveRecipe.setOnClickListener(v -> saveRecipe(rootView));

        addIngredientView(inflater);

        return rootView;
    }

    private void addIngredientView(@NonNull LayoutInflater inflater) {
        View ingredientView = inflater.inflate(R.layout.item_zutat, ingredientContainer, false);

        Spinner spinnerUnit = ingredientView.findViewById(R.id.spinner_einheit);
        ArrayAdapter<Unit> unitAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, Unit.values()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Unit unit = getItem(position);
                if (unit != null) ((TextView) view).setText(unit.getLocalizedName());
                return view;
            }

            @NonNull
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                Unit unit = getItem(position);
                if (unit != null) ((TextView) view).setText(unit.getLocalizedName());
                return view;
            }
        };
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        ImageButton btnRemove = ingredientView.findViewById(R.id.btn_entfernen);
        btnRemove.setOnClickListener(v -> ingredientContainer.removeView(ingredientView));

        ingredientContainer.addView(ingredientView);
    }

    private void saveRecipe(@NonNull View rootView) {
        try {
            String name = getInputText(rootView, R.id.et_rezept_name);
            String description = getInputText(rootView, R.id.et_zubereitung);
            String strPortions = getInputText(rootView, R.id.et_portionen);
            String strTime = getInputText(rootView, R.id.et_zubereitungszeit);

            if (name.isEmpty()) {
                showValidationError(getString(R.string.validation_recipe_name_missing));
                return;
            }
            if (strPortions.isEmpty()) {
                showValidationError(getString(R.string.validation_portions_missing));
                return;
            }
            if (strTime.isEmpty()) {
                showValidationError(getString(R.string.validation_time_missing));
                return;
            }
            if (description.isEmpty()) {
                showValidationError(getString(R.string.validation_description_missing));
                return;
            }

            int portions = Integer.parseInt(strPortions);
            int time = Integer.parseInt(strTime);

            RecipeManager manager = RecipeManager.getInstance();
            Recipe recipe = manager.createRecipe(name, time, portions, description);

            boolean hasValidIngredient = false;
            for (int i = 0; i < ingredientContainer.getChildCount(); i++) {
                View ingredientView = ingredientContainer.getChildAt(i);
                if (ingredientView == null) continue;

                String amountStr = getEditText(ingredientView, R.id.et_menge);
                String ingredientName = getEditText(ingredientView, R.id.et_zutat);

                if (amountStr.isEmpty() || ingredientName.isEmpty()) continue;

                int amount = Integer.parseInt(amountStr);
                Spinner spinnerUnit = ingredientView.findViewById(R.id.spinner_einheit);
                Unit unit = (Unit) spinnerUnit.getSelectedItem();

                Ingredient ingredient = manager.tryRegisterIngredient(ingredientName);
                if (ingredient != null) {
                    manager.addIngredient(recipe, ingredient, amount, unit);
                    hasValidIngredient = true;
                }
            }

            if (!hasValidIngredient) {
                showValidationError(getString(R.string.validation_ingredient_missing));
                return;
            }

            if (cbVegan.isChecked()) manager.addCategory(recipe, Category.VEGAN);
            if (cbVegetarian.isChecked()) manager.addCategory(recipe, Category.VEGETARIAN);
            if (cbGlutenFree.isChecked()) manager.addCategory(recipe, Category.GLUTEN_FREE);
            if (cbLactoseFree.isChecked()) manager.addCategory(recipe, Category.LACTOSE_FREE);

            if (selectedImageUri != null) {
                boolean success = manager.setImage(recipe, selectedImageUri);
                if (!success) {
                    Toast.makeText(requireContext(), getString(R.string.toast_image_save_failed), Toast.LENGTH_SHORT).show();
                }
            }

            manager.setRating(recipe, 0);

            Toast.makeText(requireContext(), getString(R.string.toast_recipe_saved), Toast.LENGTH_SHORT).show();
            resetForm();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.FirstFragment);

        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.toast_recipe_save_error, e.getMessage()), Toast.LENGTH_LONG).show();
            Log.e("CreateRezept", "Error while saving recipe", e);
        }
    }

    private void showValidationError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void resetForm() {
        ((TextInputEditText) requireView().findViewById(R.id.et_rezept_name)).setText("");
        ((TextInputEditText) requireView().findViewById(R.id.et_zubereitung)).setText("");
        ((TextInputEditText) requireView().findViewById(R.id.et_zubereitungszeit)).setText("");
        ((TextInputEditText) requireView().findViewById(R.id.et_portionen)).setText("");

        ingredientContainer.removeAllViews();
        addIngredientView(getLayoutInflater());

        cbVegan.setChecked(false);
        cbVegetarian.setChecked(false);
        cbGlutenFree.setChecked(false);
        cbLactoseFree.setChecked(false);

        selectedImageUri = null;
        tempImageUri = null;
        imageView.setImageDrawable(null);
    }

    private void showImagePickerDialog() {
        String[] options = {getString(R.string.option_gallery), getString(R.string.option_camera)};
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_select_image_title))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        imagePickerLauncher.launch(galleryIntent);
                    } else {
                        requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
                    }
                })
                .show();
    }

    private void launchCameraIntent() {
        File imageFile = new File(requireContext().getCacheDir(), "temp_image.jpg");
        tempImageUri = FileProvider.getUriForFile(requireContext(), "com.example.meinkochbuch.fileprovider", imageFile);
        takePhotoLauncher.launch(tempImageUri);
    }

    @NonNull
    private String getInputText(@NonNull View rootView, int id) {
        TextInputEditText input = rootView.findViewById(id);
        return input != null && input.getText() != null ? input.getText().toString().trim() : "";
    }

    @NonNull
    private String getEditText(@NonNull View view, int id) {
        EditText editText = view.findViewById(id);
        return editText != null && editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}