package com.example.bookingticketmove_prm392.controllers;

import com.example.bookingticketmove_prm392.models.Movie;
import com.example.bookingticketmove_prm392.repositories.MovieRepository;
import java.util.List;

/**
 * MovieController handles all movie-related business logic
 * Acts as the Controller in MVC pattern for Movie operations
 */
public class MovieController extends BaseController {
    private MovieRepository movieRepository;
    
    public MovieController() {
        this.movieRepository = new MovieRepository();
    }
    
    /**
     * Load all movies
     */
    public void loadAllMovies(ControllerCallback<List<Movie>> callback) {
        notifyLoading(callback, true);
        
        movieRepository.getAllMovies(new MovieRepository.RepositoryCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (callback != null) {
                    callback.onLoading(false);
                    callback.onSuccess(movies);
                }
            }
            
            @Override
            public void onError(Exception error) {
                handleError(callback, error, "Failed to load movies");
            }
        });
    }
    
    /**
     * Load movie details by ID
     */
    public void loadMovieDetails(int movieId, ControllerCallback<Movie> callback) {
        if (movieId <= 0) {
            if (callback != null) {
                callback.onError("Invalid movie ID");
            }
            return;
        }
        
        notifyLoading(callback, true);
        
        movieRepository.getMovieById(movieId, new MovieRepository.RepositoryCallback<Movie>() {
            @Override
            public void onSuccess(Movie movie) {
                if (callback != null) {
                    callback.onLoading(false);
                    if (movie != null) {
                        callback.onSuccess(movie);
                    } else {
                        callback.onError("Movie not found");
                    }
                }
            }
            
            @Override
            public void onError(Exception error) {
                handleError(callback, error, "Failed to load movie details");
            }
        });
    }
    
    /**
     * Search movies by title
     */
    public void searchMovies(String query, ControllerCallback<List<Movie>> callback) {
        if (query == null || query.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Please enter a search term");
            }
            return;
        }
        
        notifyLoading(callback, true);
        
        movieRepository.searchMovies(query.trim(), new MovieRepository.RepositoryCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (callback != null) {
                    callback.onLoading(false);
                    callback.onSuccess(movies);
                }
            }
            
            @Override
            public void onError(Exception error) {
                handleError(callback, error, "Search failed");
            }
        });
    }
    
    /**
     * Load popular movies
     */
    public void loadPopularMovies(ControllerCallback<List<Movie>> callback) {
        notifyLoading(callback, true);
        
        movieRepository.getPopularMovies(new MovieRepository.RepositoryCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (callback != null) {
                    callback.onLoading(false);
                    callback.onSuccess(movies);
                }
            }
            
            @Override
            public void onError(Exception error) {
                handleError(callback, error, "Failed to load popular movies");
            }
        });
    }
    
    /**
     * Add new movie (Admin functionality)
     */
    public void addMovie(Movie movie, ControllerCallback<Boolean> callback) {
        // Validation
        if (movie == null) {
            if (callback != null) {
                callback.onError("Invalid movie data");
            }
            return;
        }
        
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Movie title is required");
            }
            return;
        }
        
        if (movie.getDuration() <= 0) {
            if (callback != null) {
                callback.onError("Please enter a valid duration");
            }
            return;
        }
        
        notifyLoading(callback, true);
        
        movieRepository.addMovie(movie, new MovieRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (callback != null) {
                    callback.onLoading(false);
                    if (success) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError("Failed to add movie");
                    }
                }
            }
            
            @Override
            public void onError(Exception error) {
                handleError(callback, error, "Failed to add movie");
            }
        });
    }
    
    /**
     * Update movie (Admin functionality)
     */
    public void updateMovie(Movie movie, ControllerCallback<Boolean> callback) {
        // Validation
        if (movie == null || movie.getMovieId() <= 0) {
            if (callback != null) {
                callback.onError("Invalid movie data");
            }
            return;
        }
        
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Movie title is required");
            }
            return;
        }
        
        notifyLoading(callback, true);
        
        movieRepository.updateMovie(movie, new MovieRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (callback != null) {
                    callback.onLoading(false);
                    if (success) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError("Failed to update movie");
                    }
                }
            }
            
            @Override
            public void onError(Exception error) {
                handleError(callback, error, "Failed to update movie");
            }
        });
    }
    
    /**
     * Delete movie (Admin functionality)
     */
    public void deleteMovie(int movieId, ControllerCallback<Boolean> callback) {
        if (movieId <= 0) {
            if (callback != null) {
                callback.onError("Invalid movie ID");
            }
            return;
        }
        
        notifyLoading(callback, true);
        
        movieRepository.deleteMovie(movieId, new MovieRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (callback != null) {
                    callback.onLoading(false);
                    if (success) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError("Failed to delete movie");
                    }
                }
            }
            
            @Override
            public void onError(Exception error) {
                handleError(callback, error, "Failed to delete movie");
            }
        });
    }
}
