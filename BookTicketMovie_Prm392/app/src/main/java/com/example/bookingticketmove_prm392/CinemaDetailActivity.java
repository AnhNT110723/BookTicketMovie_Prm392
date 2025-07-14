package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.adapters.MovieAdapter;
import com.example.bookingticketmove_prm392.database.dao.CinemaDAO;
import com.example.bookingticketmove_prm392.database.dao.MovieDAO;
import com.example.bookingticketmove_prm392.database.dao.ShowDAO;
import com.example.bookingticketmove_prm392.models.Cinema;
import com.example.bookingticketmove_prm392.models.Movie;
import com.example.bookingticketmove_prm392.models.Show;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Button;
import android.view.LayoutInflater;

public class CinemaDetailActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView cinemaNameText, cinemaAddressText, cinemaContactText, cinemaCityText;
    private RecyclerView moviesRecyclerView;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private int cinemaId;
    private Cinema cinema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_detail);
        cinemaId = getIntent().getIntExtra("cinema_id", -1);
        if (cinemaId == -1) {
            Toast.makeText(this, "Invalid cinema data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadCinemaDetail();
        loadMoviesByCinema();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cinemaNameText = findViewById(R.id.cinema_name_text);
        cinemaAddressText = findViewById(R.id.cinema_address_text);
        cinemaContactText = findViewById(R.id.cinema_contact_text);
        cinemaCityText = findViewById(R.id.cinema_city_text);
        moviesRecyclerView = findViewById(R.id.movies_recycler_view);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Cinema Details");
        }
    }

    private void setupRecyclerView() {
        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(this, movieList, false, true);
        movieAdapter.setOnMovieDetailClickListener(movie -> {
            Intent intent = new Intent(CinemaDetailActivity.this, MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getMovieId());
            startActivity(intent);
        });
        movieAdapter.setOnMovieShowtimesClickListener(movie -> {
            new AsyncTask<Void, Void, List<Date>>() {
                @Override
                protected List<Date> doInBackground(Void... voids) {
                    try {
                        ShowDAO showDAO = new ShowDAO();
                        return showDAO.getShowDatesByCinemaAndMovie(cinemaId, movie.getMovieId());
                    } catch (Exception e) {
                        Log.e("Showtime", "Error get dates", e);
                        return new ArrayList<>();
                    }
                }
                @Override
                protected void onPostExecute(List<Date> dates) {
                    if (dates.isEmpty()) {
                        Toast.makeText(CinemaDetailActivity.this, "Không có lịch chiếu cho phim này", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showDateDialog(movie, dates);
                }
            }.execute();
        });
        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        moviesRecyclerView.setAdapter(movieAdapter);
    }

    private void loadCinemaDetail() {
        new AsyncTask<Void, Void, Cinema>() {
            @Override
            protected Cinema doInBackground(Void... voids) {
                try {
                    CinemaDAO cinemaDAO = new CinemaDAO();
                    return cinemaDAO.getCinemaById(cinemaId);
                } catch (SQLException e) {
                    Log.e("CinemaDetail", "Error loading cinema detail", e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(Cinema c) {
                if (c != null) {
                    cinema = c;
                    cinemaNameText.setText(cinema.getName());
                    cinemaAddressText.setText(cinema.getAddress());
                    cinemaContactText.setText(cinema.getContactInfo());
                    cinemaCityText.setText(cinema.getCityName());
                } else {
                    Toast.makeText(CinemaDetailActivity.this, "Cinema not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }.execute();
    }

    private void loadMoviesByCinema() {
        new AsyncTask<Void, Void, List<Movie>>() {
            @Override
            protected List<Movie> doInBackground(Void... voids) {
                try {
                    MovieDAO movieDAO = new MovieDAO();
                    return movieDAO.getMoviesByCinemaId(cinemaId);
                } catch (SQLException e) {
                    Log.e("CinemaDetail", "Error loading movies by cinema", e);
                    return new ArrayList<>();
                }
            }
            @Override
            protected void onPostExecute(List<Movie> movies) {
                movieList.clear();
                movieList.addAll(movies);
                movieAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private void showDateDialog(Movie movie, List<Date> dates) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ngày chiếu");
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        Spinner spinner = new Spinner(this);
        String[] dateStrs = new String[dates.size()];
        for (int i = 0; i < dates.size(); i++) {
            dateStrs[i] = dates.get(i).toString();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dateStrs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        builder.setView(spinner);
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            int selected = spinner.getSelectedItemPosition();
            if (selected >= 0 && selected < dates.size()) {
                Date selectedDate = dates.get(selected);
                showShowtimeDialog(movie, selectedDate);
            }
        });
        builder.setNegativeButton("Đóng", null);
        builder.show();
    }

    private void showShowtimeDialog(Movie movie, Date date) {
        new AsyncTask<Void, Void, List<Show>>() {
            @Override
            protected List<Show> doInBackground(Void... voids) {
                try {
                    ShowDAO showDAO = new ShowDAO();
                    return showDAO.getShowsByCinemaMovieAndDate(cinemaId, movie.getMovieId(), date);
                } catch (Exception e) {
                    Log.e("Showtime", "Error get shows", e);
                    return new ArrayList<>();
                }
            }
            @Override
            protected void onPostExecute(List<Show> shows) {
                if (shows.isEmpty()) {
                    Toast.makeText(CinemaDetailActivity.this, "Không có suất chiếu cho ngày này", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] showStrs = new String[shows.size()];
                for (int i = 0; i < shows.size(); i++) {
                    Show s = shows.get(i);
                    showStrs[i] = "Bắt đầu: " + s.getStartTime().toString() + "\nKết thúc: " + s.getEndTime().toString() + "\nGiá: " + s.getPrice();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(CinemaDetailActivity.this);
                builder.setTitle("Suất chiếu ngày " + date.toString());
                builder.setItems(showStrs, null);
                builder.setNegativeButton("Đóng", null);
                builder.show();
            }
        }.execute();
    }
} 