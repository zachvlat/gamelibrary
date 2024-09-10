package com.zachvlat.gamelibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> games = new ArrayList<>();
    private List<Game> filteredGames = new ArrayList<>();
    private Set<String> activeSources = new HashSet<>(); // Track active source filters
    private String activeQuery = ""; // Track the current search query

    public GameAdapter() {
        this.filteredGames = new ArrayList<>();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = filteredGames.get(position);
        holder.tvName.setText(game.getName());
        holder.tvGenres.setText("Genres: " + (game.getGenres().isEmpty() ? "N/A" : game.getGenres()));
        holder.tvSources.setText("Sources: " + String.join(", ", game.getSources()));
        holder.tvCriticScore.setText("Critic Score: " + (game.getCriticScore().isEmpty() ? "N/A" : game.getCriticScore()));
    }

    @Override
    public int getItemCount() {
        return filteredGames.size();
    }

    public void setGames(List<Game> games) {
        this.games = new ArrayList<>(games);
        this.filteredGames = new ArrayList<>(games);
        notifyDataSetChanged();
    }

    // Filter games by the search query
    public void filter(String query) {
        activeQuery = query.toLowerCase();
        applyFilters();
    }

    // Filter games by a selected source (when a chip is selected)
    public void filterBySource(String source) {
        activeSources.add(source); // Add this source to the active filters
        applyFilters();
    }

    // Remove source filter (when a chip is deselected)
    public void removeSourceFilter(String source) {
        activeSources.remove(source); // Remove the source from active filters
        applyFilters();
    }

    // Apply both query and source filters
    private void applyFilters() {
        filteredGames.clear();

        for (Game game : games) {
            boolean matchesQuery = game.getName().toLowerCase().contains(activeQuery);
            boolean matchesSource = activeSources.isEmpty() || !activeSources.isEmpty() && containsAnySource(game.getSources(), activeSources);

            if (matchesQuery && matchesSource) {
                filteredGames.add(game);
            }
        }

        notifyDataSetChanged();
    }

    // Utility method to check if a game contains any active source filters
    private boolean containsAnySource(List<String> gameSources, Set<String> activeSources) {
        for (String source : activeSources) {
            if (gameSources.contains(source)) {
                return true;
            }
        }
        return false;
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvGenres, tvSources, tvCriticScore;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvGenres = itemView.findViewById(R.id.tvGenres);
            tvSources = itemView.findViewById(R.id.tvSources);
            tvCriticScore = itemView.findViewById(R.id.tvCriticScore);
        }
    }
}
