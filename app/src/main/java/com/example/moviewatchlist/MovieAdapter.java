package com.example.moviewatchlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movies;
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public MovieAdapter(List<Movie> movies, OnMovieClickListener listener) {
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.textViewTitle.setText(movie.getTitle());
        holder.textViewGenre.setText(movie.getGenre());

        // Show status label (useful for search)
        if (movie.getStatus() != null) {
            holder.textViewStatus.setVisibility(View.VISIBLE);
            holder.textViewStatus.setText(movie.getStatus().toUpperCase());
        } else {
            holder.textViewStatus.setVisibility(View.GONE);
        }

        // Show rating only for Watched movies
        if ("Watched".equals(movie.getStatus())) {
            holder.textViewRating.setVisibility(View.VISIBLE);
            holder.textViewRating.setText("⭐ " + movie.getRating());
        } else {
            holder.textViewRating.setVisibility(View.GONE);
        }

        // Set icon based on status
        if ("Want to Watch".equals(movie.getStatus())) {
            holder.imageViewIcon.setImageResource(android.R.drawable.ic_menu_add);
        } else if ("Watching".equals(movie.getStatus())) {
            holder.imageViewIcon.setImageResource(android.R.drawable.ic_media_play);
        } else if ("Watched".equals(movie.getStatus())) {
            holder.imageViewIcon.setImageResource(android.R.drawable.checkbox_on_background);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewGenre, textViewRating, textViewStatus;
        ImageView imageViewIcon;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewMovieTitle);
            textViewGenre = itemView.findViewById(R.id.textViewMovieGenre);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textViewStatus = itemView.findViewById(R.id.textViewMovieStatusLabel);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
        }
    }
}
