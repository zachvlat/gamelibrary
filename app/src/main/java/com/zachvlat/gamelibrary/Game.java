package com.zachvlat.gamelibrary;

import com.google.gson.annotations.SerializedName;

public class Game {
    @SerializedName("Name")
    private String name;

    @SerializedName("Genres")
    private String genres;

    @SerializedName("Sources")
    private String sources;

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

    public String getSources() {
        return sources;
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
