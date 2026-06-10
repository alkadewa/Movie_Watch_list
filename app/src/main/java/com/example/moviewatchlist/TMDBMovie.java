package com.example.moviewatchlist;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TMDBMovie {
    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("overview")
    private String overview;
    @SerializedName("vote_average")
    private float voteAverage;
    @SerializedName("genre_ids")
    private List<Integer> genreIds;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("backdrop_path")
    private String backdropPath;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public float getVoteAverage() { return voteAverage; }
    public List<Integer> getGenreIds() { return genreIds; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
}

class TMDBResponse {
    @SerializedName("results")
    private List<TMDBMovie> results;
    public List<TMDBMovie> getResults() { return results; }
}
