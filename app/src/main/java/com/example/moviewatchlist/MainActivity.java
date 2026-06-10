package com.example.moviewatchlist;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.RatingBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private DatabaseReference mDatabase;
    private String userId;
    private RecyclerView recyclerViewLibrary;
    private MyLibraryAdapter libraryAdapter;
    private List<Movie> libraryMovies = new ArrayList<>();
    private List<Movie> allLibraryMovies = new ArrayList<>();
    private String currentSearchQuery = "";
    private ValueEventListener libraryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getUid();

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddMovie);
        recyclerViewLibrary = findViewById(R.id.recyclerViewTrending); // Repurposing this ID

        setupViewPager();
        setupLibraryRecyclerView();
        fetchLibraryMovies();

        TextInputEditText editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                filterMovies(currentSearchQuery);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabAdd.setOnClickListener(v -> showAddMovieDialog());

        findViewById(R.id.cardProfile).setOnClickListener(v -> showProfileMenu(v));
    }

    private void setupLibraryRecyclerView() {
        libraryAdapter = new MyLibraryAdapter(libraryMovies, new MyLibraryAdapter.OnMovieClickListener() {
            @Override
            public void onMovieClick(Movie movie) {
                showMovieDetailDialog(movie);
            }

            @Override
            public void onMoveStatus(Movie movie, String newStatus) {
                if (movie.getStatus().equals(newStatus)) {
                    Toast.makeText(MainActivity.this, "Already in this list", Toast.LENGTH_SHORT).show();
                    return;
                }

                if ("Watched".equals(newStatus)) {
                    showRatingDialog(movie, newStatus);
                } else {
                    mDatabase.child("users").child(userId).child("movies").child(movie.getId()).child("status").setValue(newStatus)
                            .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Moved to " + newStatus, Toast.LENGTH_SHORT).show());
                }
            }
        });
        recyclerViewLibrary.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewLibrary.setAdapter(libraryAdapter);
    }

    private void showRatingDialog(Movie movie, String newStatus) {
        BottomSheetDialog ratingDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_rating_dialog, null);
        ratingDialog.setContentView(view);

        RatingBar ratingBar = view.findViewById(R.id.dialogRatingBar);
        TextInputEditText editReview = view.findViewById(R.id.editTextDialogReview);

        view.findViewById(R.id.btnSubmitRating).setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String review = Objects.requireNonNull(editReview.getText()).toString().trim();

            DatabaseReference movieRef = mDatabase.child("users").child(userId).child("movies").child(movie.getId());
            movieRef.child("status").setValue(newStatus);
            movieRef.child("rating").setValue(rating);
            movieRef.child("notes").setValue(review)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Movie marked as Watched with rating!", Toast.LENGTH_SHORT).show();
                        ratingDialog.dismiss();
                    });
        });

        ratingDialog.show();
    }

    private void fetchLibraryMovies() {
        if (userId == null) return;
        libraryListener = mDatabase.child("users").child(userId).child("movies").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allLibraryMovies.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Movie movie = ds.getValue(Movie.class);
                    if (movie != null) {
                        allLibraryMovies.add(movie);
                    }
                }
                filterMovies(currentSearchQuery);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Ignore errors when logging out
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filterMovies(String query) {
        libraryMovies.clear();
        for (Movie movie : allLibraryMovies) {
            // If searching, show all matching movies. If not, only show "General"
            if (query.isEmpty()) {
                if ("General".equals(movie.getStatus())) {
                    libraryMovies.add(movie);
                }
            } else {
                if (movie.getTitle().toLowerCase().contains(query)) {
                    libraryMovies.add(movie);
                }
            }
        }
        libraryAdapter.notifyDataSetChanged();
        
        // Notify Fragments (they keep their status-specific filtering)
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof WatchlistFragment) {
                ((WatchlistFragment) fragment).updateSearchQuery(query);
            }
        }
    }

    private void showMovieDetailDialog(Movie movie) {
        BottomSheetDialog detailDialog = new BottomSheetDialog(this);
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
            reviewTv.setText(movie.getNotes()); // notes is the review
        } else {
            info.setText(movie.getGenre());
            reviewLayout.setVisibility(View.GONE);
        }
        yearTv.setText(movie.getReleaseYear());

        view.findViewById(R.id.btnMoveWant).setOnClickListener(v -> saveMovieToFirebase(movie.getTitle(), movie.getGenre(), movie.getReleaseYear(), "Want to Watch", detailDialog));
        view.findViewById(R.id.btnMoveWatching).setOnClickListener(v -> saveMovieToFirebase(movie.getTitle(), movie.getGenre(), movie.getReleaseYear(), "Watching", detailDialog));
        view.findViewById(R.id.btnMoveWatched).setOnClickListener(v -> saveMovieToFirebase(movie.getTitle(), movie.getGenre(), movie.getReleaseYear(), "Watched", detailDialog));

        view.findViewById(R.id.btnDeleteMovie).setOnClickListener(v -> {
            mDatabase.child("users").child(userId).child("movies").child(movie.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Movie removed", Toast.LENGTH_SHORT).show();
                        detailDialog.dismiss();
                    });
        });

        detailDialog.show();
    }


    private void showProfileMenu(View view) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.CinematicPopupTheme);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.menu_logout) {
                if (libraryListener != null) {
                    mDatabase.child("users").child(userId).child("movies").removeEventListener(libraryListener);
                }
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText(R.string.tab_want_watch); break;
                case 1: tab.setText(R.string.tab_watching); break;
                case 2: tab.setText(R.string.tab_watched); break;
            }
        }).attach();
    }

    private void showAddMovieDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_add_movie, null);
        bottomSheetDialog.setContentView(view);

        TextInputEditText editTitle = view.findViewById(R.id.editTextAddTitle);
        TextInputEditText editGenre = view.findViewById(R.id.editTextAddGenre);
        AutoCompleteTextView autoCompleteYear = view.findViewById(R.id.autoCompleteYear);

        // Populate Years (Current Year down to 1900)
        ArrayList<String> yearsList = new ArrayList<>();
        int yearNow = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        for (int yearValue = yearNow; yearValue >= 1900; yearValue--) {
            yearsList.add(String.valueOf(yearValue));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown, yearsList);
        autoCompleteYear.setAdapter(yearAdapter);
        
        // Set default value and ensure it is scrollable
        autoCompleteYear.setText(String.valueOf(yearNow), false);
        autoCompleteYear.setDropDownHeight(600); // Fixed height to ensure scrolling

        // Multi-select Genre Logic
        String[] genres = {"Action", "Adventure", "Comedy", "Drama", "Romance", "Horror", 
                "Science Fiction (Sci-Fi)", "Fantasy", "Thriller", "Mystery", "Crime", 
                "Western", "War", "History", "Animation", "Musical", "Documentary"};
        boolean[] checkedGenres = new boolean[genres.length];
        ArrayList<Integer> selectedGenreIndices = new ArrayList<>();

        editGenre.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CinematicAlertDialog);
            builder.setTitle("Select Genres");
            builder.setMultiChoiceItems(genres, checkedGenres, (dialog, which, isChecked) -> {
                if (isChecked) {
                    selectedGenreIndices.add(which);
                } else {
                    selectedGenreIndices.remove(Integer.valueOf(which));
                }
            });

            builder.setPositiveButton("OK", (dialog, which) -> {
                StringBuilder stringBuilder = new StringBuilder();
                Collections.sort(selectedGenreIndices);
                for (int i = 0; i < selectedGenreIndices.size(); i++) {
                    stringBuilder.append(genres[selectedGenreIndices.get(i)]);
                    if (i != selectedGenreIndices.size() - 1) {
                        stringBuilder.append(", ");
                    }
                }
                editGenre.setText(stringBuilder.toString());
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        view.findViewById(R.id.buttonSaveMovie).setOnClickListener(v -> {
            String title = Objects.requireNonNull(editTitle.getText()).toString().trim();
            String genre = Objects.requireNonNull(editGenre.getText()).toString().trim();
            String year = autoCompleteYear.getText().toString().trim();
            
            String status = "General";

            if (title.isEmpty()) {
                editTitle.setError("Title required");
                return;
            }
            
            if (genre.isEmpty()) {
                editGenre.setError("Genre required");
                return;
            }

            saveMovieToFirebase(title, genre, year, status, bottomSheetDialog);
        });

        bottomSheetDialog.show();
    }

    private void saveMovieToFirebase(String title, String genre, String year, String status, BottomSheetDialog dialog) {
        if (userId == null) return;

        DatabaseReference userMoviesRef = mDatabase.child("users").child(userId).child("movies");

        userMoviesRef.orderByChild("title").equalTo(title).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String existingMovieId = ds.getKey();
                        if (existingMovieId != null) {
                            userMoviesRef.child(existingMovieId).child("status").setValue(status);
                            userMoviesRef.child(existingMovieId).child("releaseYear").setValue(year)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MainActivity.this, "Movie updated (Moved to " + status + ")", Toast.LENGTH_SHORT).show();
                                        if (dialog != null) dialog.dismiss();
                                    });
                        }
                    }
                } else {
                    String movieId = userMoviesRef.push().getKey();
                    Movie movie = new Movie(movieId, title, genre, 0.0f, status, "", year);

                    if (movieId != null) {
                        userMoviesRef.child(movieId).setValue(movie)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MainActivity.this, "Movie added to watchlist", Toast.LENGTH_SHORT).show();
                                    if (dialog != null) dialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
