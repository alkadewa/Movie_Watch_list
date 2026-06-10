package com.example.moviewatchlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WatchlistFragment extends Fragment implements MovieAdapter.OnMovieClickListener {

    private String status;
    private RecyclerView recyclerView;
    private TextView textViewEmpty;
    private List<Movie> movieList = new ArrayList<>();
    private List<Movie> allMovies = new ArrayList<>();
    private MovieAdapter adapter;
    private DatabaseReference mDatabase;
    private String currentQuery = "";

    public static WatchlistFragment newInstance(String status) {
        WatchlistFragment fragment = new WatchlistFragment();
        Bundle args = new Bundle();
        args.putString("status", status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getString("status");
        }
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userId).child("movies");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watchlist, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewMovies);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);

        adapter = new MovieAdapter(movieList, this);
        recyclerView.setAdapter(adapter);

        fetchMoviesFromFirebase();
        setupSwipeGestures();
        return view;
    }

    private void fetchMoviesFromFirebase() {
        if (mDatabase == null) return;

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allMovies.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Movie movie = postSnapshot.getValue(Movie.class);
                    if (movie != null && movie.getStatus().equals(status)) {
                        allMovies.add(movie);
                    }
                }
                filterMovies(currentQuery);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateSearchQuery(String query) {
        this.currentQuery = query.toLowerCase().trim();
        filterMovies(currentQuery);
    }

    private void filterMovies(String query) {
        movieList.clear();
        for (Movie movie : allMovies) {
            if (movie.getTitle().toLowerCase().contains(query)) {
                movieList.add(movie);
            }
        }

        if (movieList.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMovieClick(Movie movie) {
        showMovieDetailDialog(movie);
    }

    private void showMovieDetailDialog(Movie movie) {
        BottomSheetDialog detailDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_movie_detail, null);
        detailDialog.setContentView(view);

        TextView title = view.findViewById(R.id.textViewDetailTitle);
        TextView info = view.findViewById(R.id.textViewDetailGenreRating);
        TextView yearTv = view.findViewById(R.id.textViewDetailSynopsis);
        View reviewLayout = view.findViewById(R.id.layoutDetailReview);
        TextView reviewTv = view.findViewById(R.id.textViewDetailReview);

        title.setText(movie.getTitle());
        if ("Watched".equals(movie.getStatus())) {
            info.setText(movie.getGenre() + " • ⭐ " + movie.getRating());
            reviewLayout.setVisibility(View.VISIBLE);
            reviewTv.setText(movie.getNotes()); // notes stores review
        } else {
            info.setText(movie.getGenre());
            reviewLayout.setVisibility(View.GONE);
        }
        yearTv.setText(movie.getReleaseYear());

        // Move buttons
        view.findViewById(R.id.btnMoveWant).setOnClickListener(v -> updateMovieStatus(movie, "Want to Watch", detailDialog));
        view.findViewById(R.id.btnMoveWatching).setOnClickListener(v -> updateMovieStatus(movie, "Watching", detailDialog));
        view.findViewById(R.id.btnMoveWatched).setOnClickListener(v -> updateMovieStatus(movie, "Watched", detailDialog));

        // Delete button
        view.findViewById(R.id.btnDeleteMovie).setOnClickListener(v -> {
            mDatabase.child(movie.getId()).removeValue().addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Movie removed", Toast.LENGTH_SHORT).show();
                detailDialog.dismiss();
            });
        });

        detailDialog.show();
    }

    private void updateMovieStatus(Movie movie, String newStatus, BottomSheetDialog dialog) {
        if (movie.getStatus().equals(newStatus)) {
            Toast.makeText(getContext(), "Already in this list", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Watched".equals(newStatus)) {
            dialog.dismiss();
            showRatingDialog(movie, newStatus);
        } else {
            movie.setStatus(newStatus);
            mDatabase.child(movie.getId()).setValue(movie).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Moved to " + newStatus, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }
    }

    private void showRatingDialog(Movie movie, String newStatus) {
        BottomSheetDialog ratingDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_rating_dialog, null);
        ratingDialog.setContentView(view);

        RatingBar ratingBar = view.findViewById(R.id.dialogRatingBar);
        com.google.android.material.textfield.TextInputEditText editReview = view.findViewById(R.id.editTextDialogReview);

        view.findViewById(R.id.btnSubmitRating).setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String review = java.util.Objects.requireNonNull(editReview.getText()).toString().trim();

            movie.setStatus(newStatus);
            movie.setRating(rating);
            movie.setNotes(review); // Save review to notes

            mDatabase.child(movie.getId()).setValue(movie).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Movie marked as Watched with rating!", Toast.LENGTH_SHORT).show();
                ratingDialog.dismiss();
            });
        });

        ratingDialog.show();
    }

    private void setupSwipeGestures() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAbsoluteAdapterPosition();
                Movie swipedMovie = movieList.get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    mDatabase.child(swipedMovie.getId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Snackbar.make(recyclerView, swipedMovie.getTitle() + " deleted", Snackbar.LENGTH_LONG)
                                        .setAction("Undo", v -> {
                                            mDatabase.child(swipedMovie.getId()).setValue(swipedMovie);
                                        }).show();
                            });
                } else if (direction == ItemTouchHelper.RIGHT) {
                    String nextStatus = getNextStatus(swipedMovie.getStatus());
                    if ("Watched".equals(nextStatus)) {
                        showRatingDialog(swipedMovie, nextStatus);
                    } else {
                        swipedMovie.setStatus(nextStatus);
                        mDatabase.child(swipedMovie.getId()).setValue(swipedMovie)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Moved to " + nextStatus, Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    private String getNextStatus(String currentStatus) {
        switch (currentStatus) {
            case "Want to Watch": return "Watching";
            case "Watching": return "Watched";
            case "Watched": return "Want to Watch";
            default: return "Want to Watch";
        }
    }
}
