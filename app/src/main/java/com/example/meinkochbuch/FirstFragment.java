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

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

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
        Set<Recipe> recipes= RecipeManager.getInstance().filter(FilterCriteria.any());
        RecipeAdapter adapter = new RecipeAdapter(getContext(), new ArrayList<>(recipes), navController);
        Log.d("FirstFragment", "Recipes: " + recipes);
        binding.recipeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recipeRecyclerView.setAdapter(adapter);
        binding.fabAddRecipe.setOnClickListener(v -> {
            navController.navigate(R.id.createRezept);
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