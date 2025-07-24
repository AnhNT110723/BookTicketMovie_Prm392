package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.adapters.CinemaManagementAdapter;
import com.example.bookingticketmove_prm392.adapters.MovieFavoriteAdapter;
import com.example.bookingticketmove_prm392.database.dao.CinemaDAO;
import com.example.bookingticketmove_prm392.database.dao.HallCinemaDAO;
import com.example.bookingticketmove_prm392.models.Cinema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CinemaManagementActivity extends AppCompatActivity {
    private static final String TAG = "CinemaManagementActivity";
    private Toolbar toolbar;
    private RecyclerView recyler_view;
    private Button btn_add_cinema;
    private CinemaManagementAdapter adapter;
    private List<Cinema> cinemaList;
    private List<Cinema> filterCinema;
    private SearchView searchView;
    private String searchQuery = "";
    private ActivityResultLauncher<Intent> cinemaEditLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_management);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Create launcher
        cinemaEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        new getCinemaList().execute();
                    }
                }
        );

        initView();
        setupToolbar();
        new getCinemaList().execute();
        setupRecyclerView();
        setupClickButton();
        setupSearchView();
 //check bug

    }

    private void initView(){
        toolbar = findViewById(R.id.toolbar);
        recyler_view = findViewById(R.id.recycler_view);
        btn_add_cinema = findViewById(R.id.btn_add_cinema);
        searchView = findViewById(R.id.search_view);
        filterCinema = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        // Hiển thị nút quay lại
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // icon back
            getSupportActionBar().setTitle("Cinema Management"); // đặt tiêu đề
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
        cinemaList = new ArrayList<>();
        recyler_view.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CinemaManagementAdapter(this, cinemaList);
        adapter.setOnCinemaEditListener(cinema -> {
            Intent intent = new Intent(this, CinemaEditFormActivity.class);
            intent.putExtra("cinema_id", cinema.getCinemaId());
            startActivity(intent);

        });
        adapter.setOnCinemaDeleteListener((Cinema cinema, int position) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to delete this cinema?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        new deleteCinema(cinema.getCinemaId()).execute();
                    })
                    .setNegativeButton("No", null)
                    .show();

        });
        recyler_view.setAdapter(adapter);
    }



    private void setupSearchView(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query.trim();
                filter();
               return true;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText.trim();
                filter();
                return true;
            }
        });
    }

    private void filter(){
        if (filterCinema == null || cinemaList == null || adapter == null) return;
        filterCinema.clear();
        for (Cinema cinema: cinemaList){
            if(searchQuery.isEmpty() ||
                    cinema.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    cinema.getAddress().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    cinema.getContactInfo().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    cinema.getCityName().toLowerCase().contains(searchQuery.toLowerCase())
            ){
                filterCinema.add(cinema);
            }
        }
        adapter.updateData(filterCinema);
    }


    private void setupClickButton(){
        btn_add_cinema.setOnClickListener(v -> {
            Intent intent = new Intent(this, CinemaEditFormActivity.class);
            cinemaEditLauncher.launch(intent);
        });
    }



    public class getCinemaList extends AsyncTask<Void, Void, List<Cinema>> {

        @Override
        protected List<Cinema> doInBackground(Void... voids) {
            try{
                CinemaDAO cinemaDAO = new CinemaDAO();
                return cinemaDAO.getAllCinemas();
            } catch (Exception e) {
                Log.e(TAG, "Error loading list cinema", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Cinema> cinemas) {
            if (cinemas != null) {
                cinemaList.clear();
                cinemaList.addAll(cinemas);
                adapter.notifyDataSetChanged(); // Thông báo cập nhật giao diện
                Log.d(TAG, "Load list cinema success");
            }else{
                recyler_view.setVisibility(View.GONE);
                recyler_view.setVisibility(View.VISIBLE);
                Log.e(TAG, "Fail load list cinema");
            }
        }

    }

    public class deleteCinema extends AsyncTask<Void, Void, Boolean> {
        private int cinemaId;

        public deleteCinema(int cinemaId){
            this.cinemaId = cinemaId;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                CinemaDAO cinemaDAO = new CinemaDAO();
                HallCinemaDAO hallCinemaDAO = new HallCinemaDAO();
                boolean r1 = hallCinemaDAO.deleteCinemaHallByCinemaId(cinemaId);
                boolean r2 = cinemaDAO.deleteCinema(cinemaId);

                return r1 && r2;
                } catch (Exception e) {
                Log.e(TAG, "Error loading list cinema", e);
                return null;
            }
        }
        @Override
        protected void onPostExecute(Boolean result){
            super.onPostExecute(result);
            if(result.booleanValue()){
                for (int i = 0; i < cinemaList.size(); i++) {
                    if (cinemaList.get(i).getCinemaId() == cinemaId) {
                        cinemaList.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }

                Log.d(TAG, "Delete cinema  success");
                Toast.makeText(CinemaManagementActivity.this, "Delete cinema  success", Toast.LENGTH_SHORT).show();
            }
            else{
                Log.e(TAG, "Fail delete cinema ");
                Toast.makeText(CinemaManagementActivity.this, "Fail delete cinema ", Toast.LENGTH_SHORT).show();
            }
        }

    }



}