package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.adapters.CinemaAdapter;
import com.example.bookingticketmove_prm392.adapters.DateAdapter;
import com.example.bookingticketmove_prm392.database.dao.CinemaDAO;
import com.example.bookingticketmove_prm392.models.Cinema;
import com.example.bookingticketmove_prm392.models.CinemaWithShowtimes;
import com.example.bookingticketmove_prm392.models.Showtime;
import com.example.bookingticketmove_prm392.utils.ImageUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CinemaSelectionActivity extends AppCompatActivity implements CinemaAdapter.OnShowtimeSelectedListener {
    private static final String TAG = "CinemaSelectionActivity";

    // UI Components
    private Toolbar toolbar;
    private ImageView posterImageView;
    private TextView movieTitleText;
    private TextView moviePriceText;
    private RecyclerView cinemasRecyclerView;
    private DateAdapter dateAdapter;
    private CinemaAdapter cinemaAdapter;
    private RecyclerView dateRecyclerView;
    private ChipGroup cityChipGroup;

    private TextView noCinemaText;
    // Data
    private int movieId;
    private String movieTitle;
    private double moviePrice;
    private String moviePoster;

    private List<Cinema> cinemaList;
    private List<CinemaWithShowtimes> cinemaWithShowtimesList;

    private List<LocalDate> dates;
    private List<String> cities;
    private LocalDate selectedDate;
      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_selection);

        // Get data from intent
        movieId = getIntent().getIntExtra("movie_id", -1);
        movieTitle = getIntent().getStringExtra("movie_title");
        moviePrice = getIntent().getDoubleExtra("movie_price", 0.0);
        moviePoster = getIntent().getStringExtra("movie_image");
        if (movieId == -1 || movieTitle == null) {
            Toast.makeText(this, "Invalid movie data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadData();
        setupRecyclerView();
        displayMovieInfo();
        //loadCinemas();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        movieTitleText = findViewById(R.id.movie_title);
        moviePriceText = findViewById(R.id.movie_price_text);
        cinemasRecyclerView = findViewById(R.id.cinema_recycler_view);
        dateRecyclerView = findViewById(R.id.date_recycler_view);
        cityChipGroup = findViewById(R.id.city_chip_group);
        posterImageView = findViewById(R.id.movie_poster);
        noCinemaText = findViewById(R.id.no_cinema_text);

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
        dates = new ArrayList<>();
        // Tạo danh sách 7 ngày tiếp theo (tạm thời, sẽ thay bằng dữ liệu từ DB)
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            dates.add(today.plusDays(i));
        }

        selectedDate = dates.get(0); // Mặc định chọn ngày đầu tiên
        dateAdapter = new DateAdapter(this, dates, date -> {
            selectedDate = date;
            loadCinemasByCity(getSelectedCity()); // Tải lại khi đổi ngày
        });
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dateRecyclerView.setAdapter(dateAdapter);
        cinemaWithShowtimesList = new ArrayList<>(); // Thay cinemaList bằng cinemaWithShowtimesList
        cinemaAdapter = new CinemaAdapter(this, cinemaWithShowtimesList, movieId, selectedDate);
        cinemaAdapter.setOnShowtimeSelectedListener(this);
        cinemasRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cinemasRecyclerView.setAdapter(cinemaAdapter);



    }

    private void loadData() {
        new LoadDataTask().execute();
    }

    private void loadCinemasByCity(String cityName) {
        new LoadCinemasTask(cityName, selectedDate).execute();
    }
    private void displayMovieInfo() {
        movieTitleText.setText(movieTitle);
        // Thêm kiểm tra null trước khi tải ảnh
        if (posterImageView != null && moviePoster != null && !moviePoster.isEmpty()) {
            ImageUtils.loadMoviePosterFitCenter(this, posterImageView, moviePoster);
        } else {
            Log.e(TAG, "Movie poster URL is null or empty, or posterImageView is null.");
        }
    }

//    private void loadCinemas() {
//        new LoadCinemasTask().execute();
//    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onShowtimeSelected(CinemaWithShowtimes cinema, Showtime showtime, LocalDate selectedDate, boolean isHeaderClick) {
        if (isHeaderClick) {
            // Đây là click vào CardView của rạp (header), chỉ để mở rộng/thu gọn
            // Cập nhật trạng thái mở rộng vào Map để ghi nhớ
            Log.d(TAG, "Cinema Header Clicked: " + cinema.getCinema().getName() + ", Expanded: " + cinema.isExpanded());

        } else {
            // Đây là click vào một khung giờ chiếu phim cụ thể, chuyển sang màn hình chọn ghế
            Log.d(TAG, "Showtime Clicked: " + cinema.getCinema().getName() + " - " + showtime.getStartTime().toLocalTime().toString());

            Intent intent = new Intent(CinemaSelectionActivity.this, ShowtimeSelectionActivity.class);
            intent.putExtra("movie_id", movieId);
            intent.putExtra("movie_title", movieTitle);
            intent.putExtra("movie_price", moviePrice);
            intent.putExtra("movie_image", moviePoster);
            intent.putExtra("cinema_id", cinema.getCinema().getCinemaId());
            intent.putExtra("cinema_name", cinema.getCinema().getName());
            intent.putExtra("showtime_id", showtime.getShowtimeId());
            intent.putExtra("showtime_time", showtime.getStartTime().toLocalTime().toString());
            intent.putExtra("showtime_date", selectedDate.toString());
            startActivity(intent);
        }
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                CinemaDAO cinemaDAO = new CinemaDAO();
                cities = cinemaDAO.getCityNames();
                runOnUiThread(() -> {
                    cityChipGroup.removeAllViews();
                    for (String city : cities) {
                        Chip chip = new Chip(CinemaSelectionActivity.this);
                        chip.setText(city);
                        chip.setCheckable(true);
                        chip.setClickable(true);
                        chip.setOnClickListener(v -> loadCinemasByCity(city));
                        cityChipGroup.addView(chip);
                        if (city.equals("Hà Nội")) chip.setChecked(true); // Mặc định chọn Long Khánh
                    }
                });
                return true;
            } catch (SQLException e) {
                Log.e(TAG, "Error loading cities", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                loadCinemasByCity("Hà Nội"); // Load rạp mặc định
            } else {
                Toast.makeText(CinemaSelectionActivity.this, "Error loading cities", Toast.LENGTH_SHORT).show();
            }
        }
    }

      private class LoadCinemasTask extends AsyncTask<Void, Void, List<CinemaWithShowtimes>> {
          private String cityName;
          private LocalDate selectedDate;

          public LoadCinemasTask(String cityName, LocalDate selectedDate) {
              this.cityName = cityName;
              this.selectedDate = selectedDate;
          }
        @Override
        protected List<CinemaWithShowtimes> doInBackground(Void... voids) {
            try {

                CinemaDAO cinemaDAO = new CinemaDAO();
                int cityId = cinemaDAO.getCityIdByName(cityName);
                return cinemaDAO.getCinemasWithShowtimesByCityAndDate(cityId, movieId, selectedDate);
            } catch (SQLException e) {
                Log.e(TAG, "Error loading cinemas", e);
                return new ArrayList<>();
            }
        }

              @Override
              protected void onPostExecute(List<CinemaWithShowtimes> cinemas) {
                  if (cinemas != null && !cinemas.isEmpty()) {
                      noCinemaText.setVisibility(View.GONE);
                      cinemaWithShowtimesList.clear();
                      cinemaWithShowtimesList.addAll(cinemas);
                      cinemaAdapter.updateDate(selectedDate); // Cập nhật ngày để refresh khung giờ
                      cinemaAdapter.notifyDataSetChanged();
                      Log.d(TAG, "Cinemas loaded: " + cinemas.size());
                  } else {
                      cinemaWithShowtimesList.clear(); // Clear danh sách rạp
                      cinemaAdapter.notifyDataSetChanged(); // Cập nhật giao diện
                      noCinemaText.setVisibility(View.VISIBLE); // Hiển thị thông báo
                      Log.w(TAG, "No cinemas available for city: " + cityName + ", date: " + selectedDate);
                  }
              }
    }
    private String getSelectedCity() {
        for (int i = 0; i < cityChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) cityChipGroup.getChildAt(i);
            if (chip.isChecked()) return chip.getText().toString();
        }
        return cities != null && !cities.isEmpty() ? cities.get(0) : "Hà Nội"; // Mặc định
    }
}
