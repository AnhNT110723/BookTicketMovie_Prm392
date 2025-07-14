package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.adapters.CinemaAdapter;
import com.example.bookingticketmove_prm392.database.dao.CinemaDAO;
import com.example.bookingticketmove_prm392.database.dao.CityDAO;
import com.example.bookingticketmove_prm392.models.Cinema;
import com.example.bookingticketmove_prm392.models.City;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CinemaSelectionActivity extends AppCompatActivity implements CinemaAdapter.OnCinemaClickListener {
    private static final String TAG = "CinemaSelectionActivity";
    
    // UI Components
    private Toolbar toolbar;
    private TextView movieTitleText;
    private TextView moviePriceText;
    private RecyclerView cinemasRecyclerView;
    private CinemaAdapter cinemaAdapter;
    private Spinner spinnerCityFilter;
    private ArrayAdapter<City> cityAdapter;
    private List<City> cityList;
    private int selectedCityId = -1;
    
    // Data
    private int movieId;
    private String movieTitle;
    private double moviePrice;
    private List<Cinema> cinemaList;
    private static final int ALL_CITY_ID = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_selection);
        // Get data from intent (có thể không có)
        movieId = getIntent().getIntExtra("movie_id", -1);
        movieTitle = getIntent().getStringExtra("movie_title");
        moviePrice = getIntent().getDoubleExtra("movie_price", 0.0);
        // Không kiểm tra dữ liệu phim nữa
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupCitySpinner();
        displayMovieInfo();
        loadCities();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        movieTitleText = findViewById(R.id.movie_title_text);
        moviePriceText = findViewById(R.id.movie_price_text);
        cinemasRecyclerView = findViewById(R.id.cinemas_recycler_view);
        spinnerCityFilter = findViewById(R.id.spinner_city_filter);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Select Cinema");
        }
    }
    
    private void setupRecyclerView() {
        cinemaList = new ArrayList<>();
        cinemaAdapter = new CinemaAdapter(this, cinemaList);
        cinemaAdapter.setOnCinemaClickListener(this);
        
        cinemasRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cinemasRecyclerView.setAdapter(cinemaAdapter);
    }
    
    private void setupCitySpinner() {
        cityList = new ArrayList<>();
        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCityFilter.setAdapter(cityAdapter);
        spinnerCityFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                City selectedCity = cityAdapter.getItem(position);
                if (selectedCity != null) {
                    selectedCityId = selectedCity.getCityId();
                    if (selectedCityId == ALL_CITY_ID) {
                        loadAllCinemas();
                    } else {
                        loadCinemasByCity(selectedCityId);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void displayMovieInfo() {
        if (movieTitle != null && !movieTitle.isEmpty()) {
            movieTitleText.setText(movieTitle);
            moviePriceText.setText(String.format("$%.2f", moviePrice));
            movieTitleText.setVisibility(View.VISIBLE);
            moviePriceText.setVisibility(View.VISIBLE);
        } else {
            movieTitleText.setVisibility(View.GONE);
            moviePriceText.setVisibility(View.GONE);
        }
    }
    
    private void loadCities() {
        new LoadCitiesTask().execute();
    }
    
    private void loadCinemasByCity(int cityId) {
        new LoadCinemasByCityTask(cityId).execute();
    }
    
    private void loadAllCinemas() {
        new LoadAllCinemasTask().execute();
    }
    
    @Override
    public void onCinemaClick(Cinema cinema) {
        if (movieTitle != null && movieId != -1) {
            Intent intent = new Intent(this, ShowtimeSelectionActivity.class);
            intent.putExtra("movie_id", movieId);
            intent.putExtra("movie_title", movieTitle);
            intent.putExtra("movie_price", moviePrice);
            intent.putExtra("cinema_id", cinema.getCinemaId());
            intent.putExtra("cinema_name", cinema.getName());
            startActivity(intent);
        } else {
            // Chuyển sang CinemaDetailActivity
            Intent intent = new Intent(this, CinemaDetailActivity.class);
            intent.putExtra("cinema_id", cinema.getCinemaId());
            startActivity(intent);
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
      private class LoadCitiesTask extends AsyncTask<Void, Void, List<City>> {
        @Override
        protected List<City> doInBackground(Void... voids) {
            try {
                CityDAO cityDAO = new CityDAO();
                return cityDAO.getAllCities();
            } catch (SQLException e) {
                Log.e(TAG, "Error loading cities", e);
                return new ArrayList<>();
            }
        }
        
        @Override
        protected void onPostExecute(List<City> cities) {
            cityList.clear();
            // Thêm mục 'Tất cả' vào đầu danh sách
            City allCity = new City(ALL_CITY_ID, "Tất cả");
            cityList.add(allCity);
            cityList.addAll(cities);
            cityAdapter.notifyDataSetChanged();
            spinnerCityFilter.setSelection(0);
        }
    }

    private class LoadCinemasByCityTask extends AsyncTask<Void, Void, List<Cinema>> {
        private int cityId;
        public LoadCinemasByCityTask(int cityId) { this.cityId = cityId; }
        @Override
        protected List<Cinema> doInBackground(Void... voids) {
            try {
                CinemaDAO cinemaDAO = new CinemaDAO();
                return cinemaDAO.getCinemasByCity(cityId);
            } catch (SQLException e) {
                Log.e(TAG, "Error loading cinemas by city", e);
                return new ArrayList<>();
            }
        }
        @Override
        protected void onPostExecute(List<Cinema> cinemas) {
            cinemaList.clear();
            cinemaList.addAll(cinemas);
            cinemaAdapter.notifyDataSetChanged();
        }
    }

    private class LoadAllCinemasTask extends AsyncTask<Void, Void, List<Cinema>> {
        @Override
        protected List<Cinema> doInBackground(Void... voids) {
            try {
                CinemaDAO cinemaDAO = new CinemaDAO();
                return cinemaDAO.getAllCinemas();
            } catch (SQLException e) {
                Log.e(TAG, "Error loading all cinemas", e);
                return new ArrayList<>();
            }
        }
        @Override
        protected void onPostExecute(List<Cinema> cinemas) {
            cinemaList.clear();
            cinemaList.addAll(cinemas);
            cinemaAdapter.notifyDataSetChanged();
        }
    }
}
