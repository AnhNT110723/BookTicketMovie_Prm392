package com.example.bookingticketmove_prm392.repositories;

import com.example.bookingticketmove_prm392.database.dao.BaseDAO;

/**
 * Base Repository class that provides common data access patterns
 * Repositories act as a bridge between Controllers and DAOs
 * Following Repository Pattern for clean separation of concerns
 */
public abstract class BaseRepository {
    
    /**
     * Interface for repository callbacks
     */
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
    
    /**
     * Helper method to execute database operations safely
     */
    protected <T> void executeAsync(BaseDAO.DatabaseTask<T> task, RepositoryCallback<T> callback) {
        BaseDAO.DatabaseTaskListener<T> listener = new BaseDAO.DatabaseTaskListener<T>() {
            @Override
            public void onSuccess(T result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(Exception error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        };
        
        // Set the listener and execute
        task.listener = listener;
        task.execute();
    }
}
