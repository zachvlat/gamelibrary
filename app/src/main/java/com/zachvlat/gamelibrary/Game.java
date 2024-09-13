package com.zachvlat.gamelibrary;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;

public class Game {

    @SerializedName("Name")
    private String name;

    @SerializedName("Platform")
    private String platform;

    @SerializedName("Playtime")
    private int playtime;

    @SerializedName("LastPlayed")
    private String lastPlayed;

    @SerializedName("Genres")
    private List<String> genres;  // This is a list now

    @SerializedName("Sources")
    private String sources;  // This is a single string

    // Getters and setters...

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public int getPlaytime() {
        return playtime;
    }

    public void setPlaytime(int playtime) {
        this.playtime = playtime;
    }

    public String getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(String lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }
}
