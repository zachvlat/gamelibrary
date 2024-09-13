package com.zachvlat.gamelibrary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> games = new ArrayList<>();
    private List<Game> filteredGames = new ArrayList<>();
    private Set<String> activeSources = new HashSet<>();
    private String activeQuery = "";

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
        holder.tvPlatform.setText("Platform: " + (game.getPlatform().isEmpty() ? "N/A" : game.getPlatform()));
        holder.tvPlaytime.setText("Playtime: " + game.getPlaytime());
        holder.tvLastPlayed.setText("Last Played: " + (game.getLastPlayed() == null ? "N/A" : game.getLastPlayed()));
        holder.tvGenres.setText("Genres: " + (game.getGenres() == null || game.getGenres().isEmpty() ? "N/A" : String.join(", ", game.getGenres())));
        holder.tvSources.setText("Sources: " + (game.getSources().isEmpty() ? "N/A" : game.getSources()));
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

    public void filter(String query) {
        activeQuery = query.toLowerCase();
        applyFilters();
    }

    public void filterBySource(String source) {
        activeSources.add(source);
        applyFilters();
    }

    public void removeSourceFilter(String source) {
        activeSources.remove(source);
        applyFilters();
    }

    private void applyFilters() {
        filteredGames.clear();

        for (Game game : games) {
            boolean matchesQuery = game.getName().toLowerCase().contains(activeQuery);
            boolean matchesSource = activeSources.isEmpty() || activeSources.contains(game.getSources());

            if (matchesQuery && matchesSource) {
                filteredGames.add(game);
            }
        }

        notifyDataSetChanged();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPlatform, tvPlaytime, tvLastPlayed, tvGenres, tvSources;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPlatform = itemView.findViewById(R.id.tvPlatform);
            tvPlaytime = itemView.findViewById(R.id.tvPlaytime);
            tvLastPlayed = itemView.findViewById(R.id.tvLastPlayed);
            tvGenres = itemView.findViewById(R.id.tvGenres);
            tvSources = itemView.findViewById(R.id.tvSources);
        }
    }
}
