package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.adapters.MovieFavoriteAdapter;
import com.example.bookingticketmove_prm392.database.dao.MovieDAO;
import com.example.bookingticketmove_prm392.database.dao.MovieFavoriteDAO;
import com.example.bookingticketmove_prm392.models.Movie;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovieFavoriteActivity extends AppCompatActivity {

    private static final String TAG = "MovieFavoriteActivity";
    private RecyclerView recyler_view;
    private MovieFavoriteAdapter adapter;
    private List<Movie> movieList;
    private Toolbar toolbar;

   private int userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_favorite);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = isLoggin();

        //Initialize view
        initView();

        //load movie favorite data
        new getMovieFavoriteList(userId).execute();
        //loadMovieList();

        //set up toolbar
        setupToolbar();

        //set up recycler view
        setupRecyclerView();

    }

    private void initView(){
        toolbar = findViewById(R.id.toolbar);
        recyler_view = findViewById(R.id.recycler_view);

    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        // Hiển thị nút quay lại
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // icon back
            getSupportActionBar().setTitle("Favorite Movie"); // đặt tiêu đề
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setupRecyclerView(){
        movieList = new ArrayList<>();
        recyler_view.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MovieFavoriteAdapter(this, movieList);
        adapter.setOnItemClickListener(movie -> {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getMovieId());
            startActivity(intent);
        });

        adapter.setOnItemDeleteClickListener((movie, position) -> {
            new deleteMovieFavorite(userId, movie.getMovieId(), position).execute();
        });

        recyler_view.setAdapter(adapter);
    }

private int isLoggin(){
    // Check if user is logged in
    SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
    boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
    if(isLoggedIn){
        int userId = prefs.getInt("userId", -1);
        return userId;
    }
    return -1;
}

    private void loadMovieList() {

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if(isLoggedIn){
            int userId = prefs.getInt("userId", -1);
            new getMovieFavoriteList(userId).execute();
        }

    }

    private class getMovieFavoriteList extends AsyncTask<Void, Void, List<Movie>> {
        private int userId;
        public getMovieFavoriteList(int userId){
            this.userId = userId;
        }

        @Override
        protected List<Movie> doInBackground(Void... voids) {
            try {
                MovieDAO movieDAO = new MovieDAO();
                return movieDAO.getMovieFavorite(userId);
            }catch(Exception e){
                Log.e(TAG, "Error loading list movie", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if (movies != null) {
                movieList.clear();
                movieList.addAll(movies);
                adapter.notifyDataSetChanged(); // Thông báo cập nhật giao diện
                Log.d(TAG, "Load list movie favorite success");
            } else {
                Log.e(TAG, "Fail load list movie favorite");
            }
        }
    }

    private class deleteMovieFavorite extends AsyncTask<Void, Void, Boolean> {
        private int movieId;
        private int userId;
        private int position;
        public deleteMovieFavorite(int userId, int movieId, int position){
            this.movieId = movieId;
            this.userId = userId;
            this.position = position;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                MovieFavoriteDAO movieFavoriteDAO = new MovieFavoriteDAO();
                return movieFavoriteDAO.deleteMovieFavorite(userId, movieId);
            }catch(Exception e) {
                Log.e(TAG, "Error loading list movie", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                for (int i = 0; i < movieList.size(); i++) {
                    if (movieList.get(i).getMovieId() == movieId) {
                        movieList.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }
                Toast.makeText(MovieFavoriteActivity.this, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Delete movie favorite success");
            }
            }
    }

}