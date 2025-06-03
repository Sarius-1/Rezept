package com.example.meinkochbuch.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.meinkochbuch.viewModel.FilterViewModel;
import com.example.meinkochbuch.R;
import com.example.meinkochbuch.adapter.RecipeAdapter;
import com.example.meinkochbuch.core.model.Category;
import com.example.meinkochbuch.core.model.Ingredient;
import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.databinding.FragmentMainBinding;
import com.example.meinkochbuch.filter.FilterCriteria;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    @Getter
    @Setter
    private Set<Recipe> recipes;

    @Getter
    @Setter
    private RecipeAdapter adapter;

    private FilterViewModel filterViewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        filterViewModel = new ViewModelProvider(requireActivity()).get(FilterViewModel.class);
        NavHostFragment navHostFragment = (NavHostFragment) requireActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);

        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        binding = FragmentMainBinding.inflate(inflater, container, false);

        if (filterViewModel.getIsVegan().getValue() == null) filterViewModel.getIsVegan().setValue(true);
        if (filterViewModel.getIsVegetarian().getValue() == null) filterViewModel.getIsVegetarian().setValue(true);
        if (filterViewModel.getIsGlutenFree().getValue() == null) filterViewModel.getIsGlutenFree().setValue(true);
        if (filterViewModel.getIsLactoseFree().getValue() == null) filterViewModel.getIsLactoseFree().setValue(true);

        applyFilter(navController);

        binding.fabAddRecipe.setOnClickListener(v -> {
            MainFragmentDirections.ActionFirstFragmentToCreateRezept action =
                    MainFragmentDirections.actionFirstFragmentToCreateRezept( null);
            navController.navigate(action);
        });
        binding.btnFilter.setOnClickListener(v -> navController.navigate(R.id.filterFragment));
        String query = filterViewModel.getSearchQuery().getValue();
        binding.searchView.setQuery(query != null ? query : "", false);
        binding.searchView.setIconified(false);
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterViewModel.getSearchQuery().setValue(newText);
                applyFilter(navController);
                return true;
            }
        });

        return binding.getRoot();
    }

    private void applyFilter(NavController navController) {
        List<FilterCriteria> criteriaList = new ArrayList<>();

        if (filterViewModel.getRatingMin().getValue() != null && filterViewModel.getRatingMax().getValue() != null) {
            criteriaList.add(FilterCriteria.minMaxRating(
                    filterViewModel.getRatingMin().getValue(),
                    filterViewModel.getRatingMax().getValue()
            ));
        }

        if (filterViewModel.getTimeMin().getValue() != null && filterViewModel.getTimeMax().getValue() != null) {
            criteriaList.add(FilterCriteria.minMaxTime(
                    filterViewModel.getTimeMin().getValue(),
                    filterViewModel.getTimeMax().getValue()
            ));
        }

        List<Ingredient> selectedIngredients = filterViewModel.getSelectedIngredients().getValue();
        if (selectedIngredients != null && !selectedIngredients.isEmpty()) {
            criteriaList.add(FilterCriteria.containsIngredients(selectedIngredients.toArray(new Ingredient[0])));
        }

        List<Category> categories = new ArrayList<>();

        if (Boolean.TRUE.equals(filterViewModel.getIsVegan().getValue())) {
            categories.add(Category.VEGAN);
        }
        if (Boolean.TRUE.equals(filterViewModel.getIsVegetarian().getValue())) {
            categories.add(Category.VEGETARIAN);
        }
        if (Boolean.TRUE.equals(filterViewModel.getIsGlutenFree().getValue())) {
            categories.add(Category.GLUTEN_FREE);
        }
        if (Boolean.TRUE.equals(filterViewModel.getIsLactoseFree().getValue())) {
            categories.add(Category.LACTOSE_FREE);
        }

        if (!categories.isEmpty()) {
            criteriaList.add(FilterCriteria.categorizes(categories.toArray(new Category[0])));
        }

        String searchText = filterViewModel.getSearchQuery().getValue();
        if (searchText != null && !searchText.isEmpty()) {
            criteriaList.add(FilterCriteria.nameContains(searchText));
        }

        FilterCriteria combinedCriteria = recipe -> criteriaList.stream().allMatch(c -> c.accept(recipe));

        recipes = RecipeManager.getInstance().filter(combinedCriteria);
        adapter = new RecipeAdapter(getContext(), new ArrayList<>(recipes), navController);
        binding.recipeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recipeRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
