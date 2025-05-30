package com.example.meinkochbuch;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.meinkochbuch.core.model.Ingredient;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.databinding.FragmentFilterBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FilterFragment extends Fragment {

    private FilterViewModel mViewModel;
    private FragmentFilterBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(FilterViewModel.class);
        NavController navController = ((NavHostFragment) requireActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main)).getNavController();

        setupSpinners();
        restoreIngredientFields();
        setFields();

        binding.buttonApply.setOnClickListener(v -> applyFilters(navController));
        binding.buttonReset.setOnClickListener(v -> {
            mViewModel.resetFilters();
            clearInputs();
        });

        binding.buttonAddZutat.setOnClickListener(v -> addIngredientRow(null));
    }

    private void setupSpinners() {
        List<Integer> ratings = Arrays.asList(0, 1, 2, 3, 4, 5);
        ArrayAdapter<Integer> ratingAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, ratings);
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRatingMin.setAdapter(ratingAdapter);
        binding.spinnerRatingMax.setAdapter(ratingAdapter);
        binding.spinnerRatingMin.setSelection(0);
        binding.spinnerRatingMax.setSelection(5);
    }

    private void applyFilters(NavController navController) {
        Integer ratingMin = parseInteger(binding.spinnerRatingMin.getSelectedItem().toString());
        Integer ratingMax = parseInteger(binding.spinnerRatingMax.getSelectedItem().toString());
        Integer timeMin = parseInteger(binding.editTextTimeMin.getText().toString());
        Integer timeMax = parseInteger(binding.editTextTimeMax.getText().toString());

        if (timeMin != null && timeMax != null && timeMin > timeMax) {
            Toast.makeText(getContext(), "Zeit: Min darf nicht größer als Max sein!", Toast.LENGTH_SHORT).show();
            return;
        }

        mViewModel.getRatingMin().setValue(ratingMin);
        mViewModel.getRatingMax().setValue(ratingMax);
        mViewModel.getTimeMin().setValue(timeMin);
        mViewModel.getTimeMax().setValue(timeMax);

        // Kategorie-Spinner wurde entfernt – Auswahl über Checkboxen geregelt

        List<Ingredient> selectedIngredients = new ArrayList<>();
        for (int i = 0; i < binding.zutatenFilterContainer.getChildCount(); i++) {
            View row = binding.zutatenFilterContainer.getChildAt(i);
            Spinner spinner = row.findViewById(R.id.spinnerZutaten);
            String name = (String) spinner.getSelectedItem();
            if (name != null) {
                Ingredient ingredient = RecipeManager.getInstance().getIngredientByName(name);
                if (ingredient != null) selectedIngredients.add(ingredient);
            }
        }
        mViewModel.getSelectedIngredients().setValue(selectedIngredients);

        mViewModel.getIsVegan().setValue(((CheckBox) requireView().findViewById(R.id.checkbox_vegan)).isChecked());
        mViewModel.getIsVegetarian().setValue(((CheckBox) requireView().findViewById(R.id.checkbox_vegetarian)).isChecked());
        mViewModel.getIsGlutenFree().setValue(((CheckBox) requireView().findViewById(R.id.checkbox_gluten_free)).isChecked());
        mViewModel.getIsLactoseFree().setValue(((CheckBox) requireView().findViewById(R.id.checkbox_lactose_free)).isChecked());



        mViewModel.onApplyFiltersClicked();
        navController.navigate(R.id.FirstFragment);
    }

    private void restoreIngredientFields() {
        binding.zutatenFilterContainer.removeAllViews();
        List<Ingredient> savedIngredients = mViewModel.getSelectedIngredients().getValue();
        if (savedIngredients != null && !savedIngredients.isEmpty()) {
            for (Ingredient ingredient : savedIngredients) {
                addIngredientRow(ingredient);
            }
        }
    }

    private void addIngredientRow(@Nullable Ingredient ingredientToSelect) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View row = inflater.inflate(R.layout.zutaten_filter_container, binding.zutatenFilterContainer, false);

        Spinner spinner = row.findViewById(R.id.spinnerZutaten);
        List<String> ingredientNames = RecipeManager.getInstance().getAllIngredients()
                .stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, ingredientNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (ingredientToSelect != null) {
            int index = ingredientNames.indexOf(ingredientToSelect.getName());
            if (index >= 0) spinner.setSelection(index);
        }

        ImageButton removeButton = row.findViewById(R.id.btn_remove);
        removeButton.setOnClickListener(v -> {
           binding.zutatenFilterContainer.removeView(row);
        });

        binding.zutatenFilterContainer.addView(row);
    }

    private void setFields() {
        if (mViewModel.getRatingMin().getValue() != null)
            binding.spinnerRatingMin.setSelection(((ArrayAdapter<Integer>) binding.spinnerRatingMin.getAdapter()).getPosition(mViewModel.getRatingMin().getValue()));

        if (mViewModel.getRatingMax().getValue() != null)
            binding.spinnerRatingMax.setSelection(((ArrayAdapter<Integer>) binding.spinnerRatingMax.getAdapter()).getPosition(mViewModel.getRatingMax().getValue()));

        binding.editTextTimeMin.setText(mViewModel.getTimeMin().getValue() != null ? mViewModel.getTimeMin().getValue().toString() : "");
        binding.editTextTimeMax.setText(mViewModel.getTimeMax().getValue() != null ? mViewModel.getTimeMax().getValue().toString() : "");
        binding.checkboxVegan.setChecked(Boolean.TRUE.equals(mViewModel.getIsVegan().getValue()));
        binding.checkboxVegetarian.setChecked(Boolean.TRUE.equals(mViewModel.getIsVegetarian().getValue()));
        binding.checkboxGlutenFree.setChecked(Boolean.TRUE.equals(mViewModel.getIsGlutenFree().getValue()));
        binding.checkboxLactoseFree.setChecked(Boolean.TRUE.equals(mViewModel.getIsLactoseFree().getValue()));

    }

    private void clearInputs() {
        binding.spinnerRatingMin.setSelection(0);
        binding.spinnerRatingMax.setSelection(5);
        binding.editTextTimeMin.setText("");
        binding.editTextTimeMax.setText("");
        // Kategorie-Spinner wurde entfernt – kein setSelection notwendig
        binding.zutatenFilterContainer.removeAllViews();

        // Checkboxen zurücksetzen
        ((CheckBox) requireView().findViewById(R.id.checkbox_vegan)).setChecked(false);
        ((CheckBox) requireView().findViewById(R.id.checkbox_vegetarian)).setChecked(false);
        ((CheckBox) requireView().findViewById(R.id.checkbox_gluten_free)).setChecked(false);
        ((CheckBox) requireView().findViewById(R.id.checkbox_lactose_free)).setChecked(false);
    }

    private Integer parseInteger(String input) {
        if (TextUtils.isEmpty(input)) return null;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
