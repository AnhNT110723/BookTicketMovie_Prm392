package com.example.bookingticketmove_prm392.repositories;

import com.example.bookingticketmove_prm392.database.dao.BaseDAO;
import com.example.bookingticketmove_prm392.database.dao.MovieDAO;
import com.example.bookingticketmove_prm392.models.Movie;
import java.util.List;

/**
 * MovieRepository handles all movie-related data operations
 * Implements Repository Pattern for Movie entity
 */
public class MovieRepository extends BaseRepository {
    private MovieDAO movieDAO;
    
    public MovieRepository() {
        this.movieDAO = new MovieDAO();
    }
    
    /**
     * Get all movies
     */
    public void getAllMovies(RepositoryCallback<List<Movie>> callback) {
        BaseDAO.DatabaseTask<List<Movie>> task = new BaseDAO.DatabaseTask<List<Movie>>(null) {
            @Override
            protected List<Movie> doInBackground(Void... voids) {
                try {
                    return movieDAO.getAllMovies();
                } catch (Exception e) {
                    exception = e;
                    return null;
                }
            }
        };
        
        executeAsync(task, callback);
    }
    
    /**
     * Get movie by ID
     */
    public void getMovieById(int movieId, RepositoryCallback<Movie> callback) {
        BaseDAO.DatabaseTask<Movie> task = new BaseDAO.DatabaseTask<Movie>(null) {
            @Override
            protected Movie doInBackground(Void... voids) {
                try {
                    return movieDAO.getMovieById(movieId);
                } catch (Exception e) {
                    exception = e;
                    return null;
                }
            }
        };
        
        executeAsync(task, callback);
    }
    
    /**
     * Search movies by title
     */
    public void searchMovies(String searchQuery, RepositoryCallback<List<Movie>> callback) {
        BaseDAO.DatabaseTask<List<Movie>> task = new BaseDAO.DatabaseTask<List<Movie>>(null) {
            @Override
            protected List<Movie> doInBackground(Void... voids) {
                try {
                    return movieDAO.searchMovies(searchQuery);
                } catch (Exception e) {
                    exception = e;
                    return null;
                }
            }
        };
        
        executeAsync(task, callback);
    }
    
    /**
     * Add new movie (Admin only)
     */
    public void addMovie(Movie movie, RepositoryCallback<Boolean> callback) {
        BaseDAO.DatabaseTask<Boolean> task = new BaseDAO.DatabaseTask<Boolean>(null) {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return movieDAO.addMovie(movie);
                } catch (Exception e) {
                    exception = e;
                    return false;
                }
            }
        };
        
        executeAsync(task, callback);
    }
    
    /**
     * Update movie (Admin only)
     */
    public void updateMovie(Movie movie, RepositoryCallback<Boolean> callback) {
        BaseDAO.DatabaseTask<Boolean> task = new BaseDAO.DatabaseTask<Boolean>(null) {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return movieDAO.updateMovie(movie);
                } catch (Exception e) {
                    exception = e;
                    return false;
                }
            }
        };
        
        executeAsync(task, callback);
    }
    
    /**
     * Delete movie (Admin only)
     */
    public void deleteMovie(int movieId, RepositoryCallback<Boolean> callback) {
        BaseDAO.DatabaseTask<Boolean> task = new BaseDAO.DatabaseTask<Boolean>(null) {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return movieDAO.deleteMovie(movieId);
                } catch (Exception e) {
                    exception = e;
                    return false;
                }
            }
        };
        
        executeAsync(task, callback);
    }
    
    /**
     * Get popular movies
     */
    public void getPopularMovies(RepositoryCallback<List<Movie>> callback) {
        BaseDAO.DatabaseTask<List<Movie>> task = new BaseDAO.DatabaseTask<List<Movie>>(null) {
            @Override
            protected List<Movie> doInBackground(Void... voids) {
                try {
                    return movieDAO.getTrendingMovies();
                } catch (Exception e) {
                    exception = e;
                    return null;
                }
            }
        };
        
        executeAsync(task, callback);
    }
}
