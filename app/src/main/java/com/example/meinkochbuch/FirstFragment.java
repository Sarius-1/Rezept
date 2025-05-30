package com.example.meinkochbuch;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.databinding.FragmentFirstBinding;
import com.example.meinkochbuch.filter.FilterCriteria;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Getter
    @Setter
    private Set<Recipe> recipes;

    @Getter @Setter
    private RecipeAdapter adapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        NavHostFragment navHostFragment = (NavHostFragment) requireActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);

        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        recipes= RecipeManager.getInstance().filter(FilterCriteria.any());
        adapter = new RecipeAdapter(getContext(), new ArrayList<>(recipes), navController);
        Log.d("FirstFragment", "Recipes: " + recipes);
        binding.recipeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recipeRecyclerView.setAdapter(adapter);
        binding.fabAddRecipe.setOnClickListener(v -> {
            navController.navigate(R.id.createRezept);
        });
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Optional: Reaktion auf Enter-Taste
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                FirstFragment.this.setRecipes(RecipeManager.getInstance().filter(FilterCriteria.nameContains(newText)));
                FirstFragment.this.setAdapter(new RecipeAdapter(getContext(), new ArrayList<>(FirstFragment.this.getRecipes()), navController));
                binding.recipeRecyclerView.setAdapter(adapter);
                return true;
            }
        });
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}