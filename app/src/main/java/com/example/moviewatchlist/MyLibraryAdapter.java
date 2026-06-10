package com.example.moviewatchlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyLibraryAdapter extends RecyclerView.Adapter<MyLibraryAdapter.LibraryViewHolder> {

    private List<Movie> movies;
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
        void onMoveStatus(Movie movie, String newStatus);
    }

    public MyLibraryAdapter(List<Movie> movies, OnMovieClickListener listener) {
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_horizontal, parent, false);
        return new LibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.tvTitle.setText(movie.getTitle());
        holder.tvGenre.setText(movie.getGenre());

        // Show status label
        if (movie.getStatus() != null) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText(movie.getStatus().toUpperCase());
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        // Set status icon
        if ("General".equals(movie.getStatus())) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_today);
        } else if ("Want to Watch".equals(movie.getStatus())) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_add);
        } else if ("Watching".equals(movie.getStatus())) {
            holder.ivIcon.setImageResource(android.R.drawable.ic_media_play);
        } else if ("Watched".equals(movie.getStatus())) {
            holder.ivIcon.setImageResource(android.R.drawable.checkbox_on_background);
        }

        holder.btnDetails.setOnClickListener(v -> listener.onMovieClick(movie));
        holder.itemView.setOnClickListener(v -> listener.onMovieClick(movie));

        holder.btnWant.setOnClickListener(v -> listener.onMoveStatus(movie, "Want to Watch"));
        holder.btnWatching.setOnClickListener(v -> listener.onMoveStatus(movie, "Watching"));
        holder.btnWatched.setOnClickListener(v -> listener.onMoveStatus(movie, "Watched"));
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class LibraryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvGenre, tvStatus;
        ImageView ivIcon;
        Button btnDetails;
        ImageButton btnWant, btnWatching, btnWatched;

        public LibraryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.textViewHorizontalTitle);
            tvGenre = itemView.findViewById(R.id.textViewHorizontalGenre);
            tvStatus = itemView.findViewById(R.id.textViewHorizontalStatusLabel);
            ivIcon = itemView.findViewById(R.id.imageViewHorizontalIcon);
            btnDetails = itemView.findViewById(R.id.btnHorizontalDetails);
            btnWant = itemView.findViewById(R.id.btnMoveWantShort);
            btnWatching = itemView.findViewById(R.id.btnMoveWatchingShort);
            btnWatched = itemView.findViewById(R.id.btnMoveWatchedShort);
        }
    }
}
