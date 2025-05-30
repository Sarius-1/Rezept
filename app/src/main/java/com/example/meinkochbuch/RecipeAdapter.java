package com.example.meinkochbuch;

import android.content.Context;
import android.graphics.Picture;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meinkochbuch.core.model.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>{
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
        setStars(holder, recipe.getRating());
        holder.button.setOnClickListener(v -> {
            Recipe recipeToSend = recipeList.get(position);
            FirstFragmentDirections.ActionFirstFragmentToRezept action =
                    FirstFragmentDirections.actionFirstFragmentToRezept(recipeToSend);
            navController.navigate(action);
        });
    }

    private void setStars(RecipeViewHolder holder, int rating) {
        for (int i = 1; i <= 5; i++) {
            holder.stars[i - 1].setImageResource(
                    i <= rating ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
            );
        }
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;

        ImageView[] stars = new ImageView[5];

        Button button;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            stars[0] = itemView.findViewById(R.id.oneStar);
            stars[1] = itemView.findViewById(R.id.twoStar);
            stars[2] = itemView.findViewById(R.id.threeStar);
            stars[3] = itemView.findViewById(R.id.fourStar);
            stars[4] = itemView.findViewById(R.id.fiveStar);
            button = itemView.findViewById(R.id.button);
        }

    }
}
