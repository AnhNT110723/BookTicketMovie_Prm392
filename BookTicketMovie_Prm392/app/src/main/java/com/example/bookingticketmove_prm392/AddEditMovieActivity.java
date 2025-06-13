package com.example.bookingticketmove_prm392;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bookingticketmove_prm392.database.dao.MovieDAO;
import com.example.bookingticketmove_prm392.models.Movie;
import com.example.bookingticketmove_prm392.utils.ImageUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditMovieActivity extends AppCompatActivity {
    private static final String TAG = "AddEditMovieActivity";
    public static final String EXTRA_MOVIE_ID = "movie_id";
    public static final int RESULT_MOVIE_SAVED = 1001;
    public static final int RESULT_MOVIE_UPDATED = 1002;

    // UI Components
    private Toolbar toolbar;
    private ImageView posterImageView;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText genreEditText;
    private EditText durationEditText;
    private EditText directorEditText;
    private EditText releaseDateEditText;
    private EditText ratingEditText;
    private EditText posterUrlEditText;
    private EditText trailerUrlEditText;
    private EditText priceEditText;
    private CheckBox isActiveCheckBox;
    private CheckBox isTrendingCheckBox;
    private ProgressBar progressBar;

    // Data
    private Movie currentMovie;
    private int movieId = -1;
    private boolean isEditMode = false;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_movie);

        // Check if we're in edit mode
        movieId = getIntent().getIntExtra(EXTRA_MOVIE_ID, -1);
        isEditMode = movieId != -1;

        initViews();
        setupToolbar();
        setupClickListeners();

        if (isEditMode) {
            loadMovieDetails();
        } else {
            // Set default values for new movie
            setDefaultValues();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        posterImageView = findViewById(R.id.poster_image_view);
        titleEditText = findViewById(R.id.title_edit_text);
        descriptionEditText = findViewById(R.id.description_edit_text);
        genreEditText = findViewById(R.id.genre_edit_text);
        durationEditText = findViewById(R.id.duration_edit_text);
        directorEditText = findViewById(R.id.director_edit_text);
        releaseDateEditText = findViewById(R.id.release_date_edit_text);
        ratingEditText = findViewById(R.id.rating_edit_text);
        posterUrlEditText = findViewById(R.id.poster_url_edit_text);
        trailerUrlEditText = findViewById(R.id.trailer_url_edit_text);
        priceEditText = findViewById(R.id.price_edit_text);
        isActiveCheckBox = findViewById(R.id.is_active_checkbox);
        isTrendingCheckBox = findViewById(R.id.is_trending_checkbox);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Movie" : "Add New Movie");
        }
    }

    private void setupClickListeners() {
        // Date picker for release date
        releaseDateEditText.setOnClickListener(v -> showDatePicker());

        // Load poster image when URL changes
        posterUrlEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                loadPosterImage();
            }
        });

        // Add placeholder poster click to show image picker dialog
        posterImageView.setOnClickListener(v -> {
            // TODO: Implement image picker dialog or URL input dialog
            Toast.makeText(this, "Click on Poster URL field to enter image URL", Toast.LENGTH_SHORT).show();
        });
    }

    private void setDefaultValues() {
        // Set default values for new movie
        selectedDate = Calendar.getInstance();
        updateDateDisplay();
        ratingEditText.setText("0.0");
        priceEditText.setText("10.00");
        isActiveCheckBox.setChecked(true);
        isTrendingCheckBox.setChecked(false);
    }

    private void loadMovieDetails() {
        if (movieId != -1) {
            new LoadMovieDetailsTask().execute(movieId);
        }
    }

    private void loadPosterImage() {
        String posterUrl = posterUrlEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(posterUrl)) {
            ImageUtils.loadMoviePosterFitCenter(this, posterImageView, posterUrl);
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateDisplay();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        releaseDateEditText.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void displayMovieDetails(Movie movie) {
        if (movie == null) {
            Toast.makeText(this, "Movie details not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentMovie = movie;

        // Fill the form with movie data
        titleEditText.setText(movie.getTitle());
        descriptionEditText.setText(movie.getDescription());
        genreEditText.setText(movie.getGenre());
        durationEditText.setText(String.valueOf(movie.getDuration()));
        directorEditText.setText(movie.getDirector());
        
        // Set release date
        if (movie.getReleaseDate() != null) {
            selectedDate.setTime(movie.getReleaseDate());
            updateDateDisplay();
        }
        
        ratingEditText.setText(String.valueOf(movie.getRating()));
        posterUrlEditText.setText(movie.getPosterUrl() != null ? movie.getPosterUrl() : "");
        trailerUrlEditText.setText(movie.getTrailerUrl() != null ? movie.getTrailerUrl() : "");
        priceEditText.setText(String.valueOf(movie.getPrice()));
        isActiveCheckBox.setChecked(movie.isActive());
        isTrendingCheckBox.setChecked(movie.isTrending());

        // Load poster image
        loadPosterImage();
    }

    private boolean validateInput() {
        // Reset errors
        titleEditText.setError(null);
        descriptionEditText.setError(null);
        genreEditText.setError(null);
        durationEditText.setError(null);
        directorEditText.setError(null);
        ratingEditText.setError(null);
        priceEditText.setError(null);

        boolean isValid = true;

        // Validate title
        if (TextUtils.isEmpty(titleEditText.getText().toString().trim())) {
            titleEditText.setError("Title is required");
            isValid = false;
        }

        // Validate description
        if (TextUtils.isEmpty(descriptionEditText.getText().toString().trim())) {
            descriptionEditText.setError("Description is required");
            isValid = false;
        }

        // Validate genre
        if (TextUtils.isEmpty(genreEditText.getText().toString().trim())) {
            genreEditText.setError("Genre is required");
            isValid = false;
        }

        // Validate duration
        String durationStr = durationEditText.getText().toString().trim();
        if (TextUtils.isEmpty(durationStr)) {
            durationEditText.setError("Duration is required");
            isValid = false;
        } else {
            try {
                int duration = Integer.parseInt(durationStr);
                if (duration <= 0) {
                    durationEditText.setError("Duration must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                durationEditText.setError("Invalid duration format");
                isValid = false;
            }
        }

        // Validate director
        if (TextUtils.isEmpty(directorEditText.getText().toString().trim())) {
            directorEditText.setError("Director is required");
            isValid = false;
        }

        // Validate rating
        String ratingStr = ratingEditText.getText().toString().trim();
        if (TextUtils.isEmpty(ratingStr)) {
            ratingEditText.setError("Rating is required");
            isValid = false;
        } else {
            try {
                double rating = Double.parseDouble(ratingStr);
                if (rating < 0 || rating > 10) {
                    ratingEditText.setError("Rating must be between 0 and 10");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                ratingEditText.setError("Invalid rating format");
                isValid = false;
            }
        }

        // Validate price
        String priceStr = priceEditText.getText().toString().trim();
        if (TextUtils.isEmpty(priceStr)) {
            priceEditText.setError("Price is required");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceStr);
                if (price < 0) {
                    priceEditText.setError("Price must be greater than or equal to 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                priceEditText.setError("Invalid price format");
                isValid = false;
            }
        }

        return isValid;
    }

    private Movie createMovieFromForm() {
        Movie movie = isEditMode ? currentMovie : new Movie();

        // Set basic fields
        movie.setTitle(titleEditText.getText().toString().trim());
        movie.setDescription(descriptionEditText.getText().toString().trim());
        movie.setGenre(genreEditText.getText().toString().trim());
        movie.setDuration(Integer.parseInt(durationEditText.getText().toString().trim()));
        movie.setDirector(directorEditText.getText().toString().trim());
        movie.setReleaseDate(selectedDate.getTime());
        movie.setRating(Double.parseDouble(ratingEditText.getText().toString().trim()));
        movie.setPosterUrl(posterUrlEditText.getText().toString().trim());
        movie.setTrailerUrl(trailerUrlEditText.getText().toString().trim());
        movie.setPrice(Double.parseDouble(priceEditText.getText().toString().trim()));
        movie.setActive(isActiveCheckBox.isChecked());
        movie.setTrending(isTrendingCheckBox.isChecked());

        // Set creation date for new movies
        if (!isEditMode) {
            movie.setCreatedDate(new Date());
        }

        return movie;
    }

    private void saveMovie() {
        if (!validateInput()) {
            return;
        }

        Movie movie = createMovieFromForm();
        if (isEditMode) {
            new UpdateMovieTask().execute(movie);
        } else {
            new AddMovieTask().execute(movie);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_edit_movie_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_save) {
            saveMovie();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog if user has made changes
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to discard your changes?")
                .setPositiveButton("Discard", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // AsyncTask to load movie details
    private class LoadMovieDetailsTask extends AsyncTask<Integer, Void, Movie> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Movie doInBackground(Integer... params) {
            try {
                MovieDAO movieDAO = new MovieDAO();
                return movieDAO.getMovieById(params[0]);
            } catch (Exception e) {
                Log.e(TAG, "Error loading movie details", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Movie movie) {
            progressBar.setVisibility(View.GONE);
            displayMovieDetails(movie);
        }
    }

    // AsyncTask to add new movie
    private class AddMovieTask extends AsyncTask<Movie, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Movie... params) {
            try {
                MovieDAO movieDAO = new MovieDAO();
                return movieDAO.addMovie(params[0]);
            } catch (Exception e) {
                Log.e(TAG, "Error adding movie", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressBar.setVisibility(View.GONE);
            
            if (success) {
                Toast.makeText(AddEditMovieActivity.this, "Movie added successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_MOVIE_SAVED);
                finish();
            } else {
                Toast.makeText(AddEditMovieActivity.this, "Failed to add movie", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // AsyncTask to update existing movie
    private class UpdateMovieTask extends AsyncTask<Movie, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Movie... params) {
            try {
                MovieDAO movieDAO = new MovieDAO();
                return movieDAO.updateMovie(params[0]);
            } catch (Exception e) {
                Log.e(TAG, "Error updating movie", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressBar.setVisibility(View.GONE);
            
            if (success) {
                Toast.makeText(AddEditMovieActivity.this, "Movie updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_MOVIE_UPDATED);
                finish();
            } else {
                Toast.makeText(AddEditMovieActivity.this, "Failed to update movie", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
