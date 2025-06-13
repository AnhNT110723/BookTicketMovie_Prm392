package com.example.bookingticketmove_prm392.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.bookingticketmove_prm392.R;

public class ImageUtils {
    
    /**
     * Load movie poster image with fallback to placeholder
     * @param context The context
     * @param imageView The ImageView to load into
     * @param imageUrl The URL of the image to load
     */
    public static void loadMoviePoster(Context context, ImageView imageView, String imageUrl) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop();
        
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(options)
                    .into(imageView);
        } else {
            // If no URL provided, show placeholder
            imageView.setImageResource(R.drawable.ic_movie_placeholder);
        }
    }
    
    /**
     * Load movie poster image with custom placeholder
     * @param context The context
     * @param imageView The ImageView to load into
     * @param imageUrl The URL of the image to load
     * @param placeholderResId The placeholder resource ID
     */
    public static void loadMoviePoster(Context context, ImageView imageView, String imageUrl, int placeholderResId) {
        RequestOptions options = new RequestOptions()
                .placeholder(placeholderResId)
                .error(placeholderResId)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop();
        
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(options)
                    .into(imageView);
        } else {
            imageView.setImageResource(placeholderResId);
        }
    }
    
    /**
     * Load image with fit center instead of center crop
     * @param context The context
     * @param imageView The ImageView to load into
     * @param imageUrl The URL of the image to load
     */
    public static void loadMoviePosterFitCenter(Context context, ImageView imageView, String imageUrl) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter();
        
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(options)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_movie_placeholder);
        }
    }
}
