package com.zachvlat.gamelibrary;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;

public class Game {

    @SerializedName("Name")
    private String name;

    @SerializedName("Genres")
    private String genres;

    @SerializedName("Sources")
    private String sources;  // Comma-separated list

    @SerializedName("Critic Score")
    private String criticScore;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    // Split sources string into a list
    public List<String> getSources() {
        // If the sources string is empty or null, return an empty list
        if (sources == null || sources.isEmpty()) {
            return Arrays.asList(); // Return an empty list
        }
        // Split by comma and trim spaces around each source
        return Arrays.asList(sources.split("\\s*,\\s*"));
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public String getCriticScore() {
        return criticScore;
    }

    public void setCriticScore(String criticScore) {
        this.criticScore = criticScore;
    }
}
