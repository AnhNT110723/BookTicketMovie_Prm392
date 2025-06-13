package com.example.bookingticketmove_prm392.controllers;

/**
 * Base Controller class providing common functionality for all controllers
 * Following MVC Architecture Pattern
 */
public abstract class BaseController {
    
    /**
     * Interface for controller callbacks to the view layer
     */
    public interface ControllerCallback<T> {
        void onSuccess(T result);
        void onError(String error);
        void onLoading(boolean isLoading);
    }
    
    /**
     * Helper method to handle common error scenarios
     */
    protected void handleError(ControllerCallback<?> callback, Exception e, String defaultMessage) {
        if (callback != null) {
            callback.onLoading(false);
            String errorMessage = e.getMessage() != null ? e.getMessage() : defaultMessage;
            callback.onError(errorMessage);
        }
    }
    
    /**
     * Helper method to notify loading state
     */
    protected void notifyLoading(ControllerCallback<?> callback, boolean isLoading) {
        if (callback != null) {
            callback.onLoading(isLoading);
        }
    }
}
