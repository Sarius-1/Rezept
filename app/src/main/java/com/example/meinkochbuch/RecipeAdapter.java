package com.example.meinkochbuch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meinkochbuch.core.model.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private Context context;
    private List<Recipe> recipeList;
    private NavController navController;

    public RecipeAdapter(Context context, List<Recipe> recipeList, NavController navController) {
        this.context = context;
        this.recipeList = recipeList;
        this.navController = navController;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recept, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.textTitle.setText(recipe.getName());
        holder.rating.setText(String.valueOf(recipe.getRating()));
        holder.button.setOnClickListener(v -> {
            Recipe recipeToSend = recipeList.get(position);
            FirstFragmentDirections.ActionFirstFragmentToRezept action =
                    FirstFragmentDirections.actionFirstFragmentToRezept(recipeToSend);
            navController.navigate(action);
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;

        private TextView rating;

        Button button;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            rating = itemView.findViewById(R.id.textDescription);
            button = itemView.findViewById(R.id.button);
        }

    }
}
