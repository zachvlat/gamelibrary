package com.zachvlat.gamelibrary;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Game {

    @SerializedName("Id")
    private String id; // New property

    @SerializedName("Name")
    private String name;

    @SerializedName("Description")
    private String description; // New property

    @SerializedName("Platform")
    private String platform;

    @SerializedName("Playtime")
    private int playtime;

    @SerializedName("LastPlayed")
    private String lastPlayed;

    @SerializedName("Genres")
    private List<String> genres;

    @SerializedName("Sources")
    private String sources;

    @SerializedName("ReleaseDate")
    private String releaseDate; // New property

    @SerializedName("CommunityHubUrl")
    private String communityHubUrl; // New property

    @SerializedName("Added")
    private String added; // New property

    @SerializedName("CoverArtUrl")
    private String coverArtUrl;

    // Getters and setters...

    public String getCoverArtUrl() {
        return coverArtUrl;
    }

    public void setCoverArtUrl(String coverArtUrl) {
        this.coverArtUrl = coverArtUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getCommunityHubUrl() {
        return communityHubUrl;
    }

    public void setCommunityHubUrl(String communityHubUrl) {
        this.communityHubUrl = communityHubUrl;
    }

    public String getAdded() {
        return added;
    }

    public void setAdded(String added) {
        this.added = added;
    }

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

    @SerializedName("CommunityScore")
    private Double communityScore;

    @SerializedName("CriticScore")
    private Integer criticScore;

    @SerializedName("UserScore")
    private Double userScore;

    public Double getCommunityScore() {
        return communityScore != null ? communityScore : 0.0;
    }

    public void setCommunityScore(Double communityScore) {
        this.communityScore = communityScore;
    }

    public Integer getCriticScore() {
        return criticScore != null ? criticScore : 0;
    }

    public void setCriticScore(Integer criticScore) {
        this.criticScore = criticScore;
    }

    public Double getUserScore() {
        return userScore != null ? userScore : 0.0;
    }

    public void setUserScore(Double userScore) {
        this.userScore = userScore;
    }

}
