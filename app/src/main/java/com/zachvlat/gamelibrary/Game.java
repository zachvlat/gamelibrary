package com.zachvlat.gamelibrary;

import com.google.gson.annotations.SerializedName;

public class Game {
    @SerializedName("Name")
    private String name;

    // Comment out or remove all other fields
    /*
    @SerializedName("Categories")
    private String categories;

    @SerializedName("Description")
    private String description;

    @SerializedName("Developers")
    private String developers;

    @SerializedName("Publishers")
    private String publishers;

    @SerializedName("Game Id")
    private int gameId;

    @SerializedName("Genres")
    private String genres;

    @SerializedName("Release Date")
    private String releaseDate;

    @SerializedName("Platforms")
    private String platforms;

    @SerializedName("Sources")
    private String sources;

    @SerializedName("Critic Score")
    private String criticScore;
    */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Remove or comment out the setters and getters for other fields
    /*
    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDevelopers() {
        return developers;
    }

    public void setDevelopers(String developers) {
        this.developers = developers;
    }

    public String getPublishers() {
        return publishers;
    }

    public void setPublishers(String publishers) {
        this.publishers = publishers;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPlatforms() {
        return platforms;
    }

    public void setPlatforms(String platforms) {
        this.platforms = platforms;
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
    */
}
