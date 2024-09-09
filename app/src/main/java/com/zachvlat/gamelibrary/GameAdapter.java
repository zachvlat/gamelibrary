package com.zachvlat.gamelibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> games = new ArrayList<>();

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_game.xml layout for each item in the list
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);
        holder.tvName.setText(game.getName());
        holder.tvGenres.setText("Genres: " + (game.getGenres().isEmpty() ? "N/A" : game.getGenres()));
        holder.tvSources.setText("Sources: " + game.getSources());
        holder.tvCriticScore.setText("Critic Score: " + (game.getCriticScore().isEmpty() ? "N/A" : game.getCriticScore()));
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public void setGames(List<Game> games) {
        this.games = games;
        notifyDataSetChanged();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvGenres, tvSources, tvCriticScore;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the views from the item_game.xml layout
            tvName = itemView.findViewById(R.id.tvName);
            tvGenres = itemView.findViewById(R.id.tvGenres);
            tvSources = itemView.findViewById(R.id.tvSources);
            tvCriticScore = itemView.findViewById(R.id.tvCriticScore);
        }
    }
}
