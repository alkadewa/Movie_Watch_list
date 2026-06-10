package com.example.moviewatchlist;

import java.io.Serializable;

public class Movie implements Serializable {
    private String id;
    private String title;
    private String genre;
    private float rating;
    private String status; // "Want to Watch", "Watching", "Watched"
    private String notes; // Used for Review
    private String releaseYear;

    public Movie() {
        // Required for Firebase
    }

    public Movie(String id, String title, String genre, float rating, String status, String notes, String releaseYear) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.rating = rating;
        this.status = status;
        this.notes = notes;
        this.releaseYear = releaseYear;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getReleaseYear() { return releaseYear; }
    public void setReleaseYear(String releaseYear) { this.releaseYear = releaseYear; }
}
