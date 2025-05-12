package com.zachvlat.gamelibrary;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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

        holder.gameName.setText(game.getName());
        holder.gamePlatform.setText("Platform: " + (game.getPlatform().isEmpty() ? "N/A" : game.getPlatform()));
        holder.gamePlaytime.setText("Playtime: " + game.getPlaytime());
        holder.gameLastPlayed.setText("Last Played: " + (game.getLastPlayed() == null ? "N/A" : game.getLastPlayed()));
        holder.gameGenres.setText("Genres: " + (game.getGenres() == null || game.getGenres().isEmpty() ? "N/A" : String.join(", ", game.getGenres())));
        holder.gameSources.setText("Sources: " + (game.getSources().isEmpty() ? "N/A" : game.getSources()));

        // Set up the description TextView with a click listener
        holder.gameDescription.setText(game.getDescription());
        holder.gameDescription.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setTitle(game.getName());
            builder.setMessage(game.getDescription());
            builder.setPositiveButton("Close", null);
            builder.show();
        });

        // Load cover art if available using Glide
        if (game.getCoverArtUrl() != null && !game.getCoverArtUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(game.getCoverArtUrl()) // Load the image from the URL
                    .into(holder.gameCoverArt);  // Set the image into the ImageView
        }
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

    public void sortBy(String option) {
        switch (option) {
            case "Name":
                filteredGames.sort((g1, g2) -> g1.getName().compareToIgnoreCase(g2.getName()));
                break;
            case "Playtime":
                filteredGames.sort((g1, g2) -> Integer.compare(g2.getPlaytime(), g1.getPlaytime())); // Descending
                break;
            case "LastPlayed":
                filteredGames.sort((g1, g2) -> {
                    String lp1 = g1.getLastPlayed() != null ? g1.getLastPlayed() : "";
                    String lp2 = g2.getLastPlayed() != null ? g2.getLastPlayed() : "";
                    return lp2.compareTo(lp1); // Descending
                });
                break;
            case "ReleaseDate":
                filteredGames.sort((g1, g2) -> {
                    String r1 = g1.getReleaseDate() != null ? g1.getReleaseDate() : "";
                    String r2 = g2.getReleaseDate() != null ? g2.getReleaseDate() : "";
                    return r2.compareTo(r1); // Descending
                });
                break;
            case "Added":
                filteredGames.sort((g1, g2) -> {
                    String a1 = g1.getAdded() != null ? g1.getAdded() : "";
                    String a2 = g2.getAdded() != null ? g2.getAdded() : "";
                    return a2.compareTo(a1); // Descending
                });
                break;
            default:
                // Default to original order
                filteredGames = new ArrayList<>(games);
                applyFilters();
                return;
        }

        notifyDataSetChanged();
    }


    static class GameViewHolder extends RecyclerView.ViewHolder {

        TextView gameName, gamePlatform, gamePlaytime, gameLastPlayed, gameGenres, gameSources;
        TextView gameDescription; // New property
        TextView gameReleaseDate; // New property
        TextView gameCommunityHubUrl; // New property
        ImageView gameCoverArt; // New property for cover art

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameName = itemView.findViewById(R.id.gameName);
            gamePlatform = itemView.findViewById(R.id.gamePlatform);
            gamePlaytime = itemView.findViewById(R.id.gamePlaytime);
            gameLastPlayed = itemView.findViewById(R.id.gameLastPlayed);
            gameGenres = itemView.findViewById(R.id.gameGenres);
            gameSources = itemView.findViewById(R.id.gameSources);
            gameDescription = itemView.findViewById(R.id.gameDescription); // Initialize new property
            gameReleaseDate = itemView.findViewById(R.id.gameReleaseDate); // Initialize new property
            gameCommunityHubUrl = itemView.findViewById(R.id.gameCommunityHubUrl); // Initialize new property
            gameCoverArt = itemView.findViewById(R.id.gameCoverArt); // Initialize the ImageView for cover art
        }
    }
}
