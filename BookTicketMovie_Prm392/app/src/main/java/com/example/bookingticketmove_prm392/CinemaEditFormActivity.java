package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.adapters.CinemaHallAdapter;
import com.example.bookingticketmove_prm392.adapters.CinemaManagementAdapter;
import com.example.bookingticketmove_prm392.adapters.MovieFavoriteAdapter;
import com.example.bookingticketmove_prm392.database.dao.CinemaDAO;
import com.example.bookingticketmove_prm392.database.dao.HallCinemaDAO;
import com.example.bookingticketmove_prm392.models.Cinema;
import com.example.bookingticketmove_prm392.models.CinemaHall;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CinemaEditFormActivity extends AppCompatActivity {

    private static final String TAG = "CinemaEditFormActivity";
    //    UI
    private Toolbar toolbar;
    private RecyclerView recyler_view;
    private CinemaHallAdapter adapter;
    private TextInputEditText edt_cinema_name;
    private TextInputEditText edt_cinema_address;
    private TextInputEditText edt_cinema_city;
    private TextInputEditText edt_cinema_phone;
    private Button btn_save_infor;
    private Button btn_cancel_infor;
    private Button btn_cancel_hall;
    private Button btn_save_hall;
    private ImageView icon_add;
    private LinearLayout card_hall_list;


    //Load data
    private Cinema cinema;
    private List<CinemaHall> cinemaHallList;
    private int cinemaId;

    // Fragment
    FragmentManager fragmentManager;
    FragmentTransaction trans;
    AddHallFragment addHallFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cinema);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        initView();
        setupToolbar();
        setupRecyclerView();
        loadCinema();
        setupOnClick();
    }


    private void initView(){
        toolbar = findViewById(R.id.toolbar);
        recyler_view = findViewById(R.id.recycler_view);
        edt_cinema_name = findViewById(R.id.edt_cinema_name);
        edt_cinema_address = findViewById(R.id.edt_cinema_address);
        edt_cinema_city = findViewById(R.id.edt_cinema_city);
        edt_cinema_phone = findViewById(R.id.edt_cinema_phone);
        btn_save_infor = findViewById(R.id.btn_save_infor);
        btn_cancel_infor = findViewById(R.id.btn_cancel_infor);
        btn_cancel_hall = findViewById(R.id.btn_cancel_hall);
        btn_save_hall = findViewById(R.id.btn_save_hall);
        icon_add = findViewById(R.id.icon_add);
        card_hall_list= findViewById(R.id.card_hall_list);
        fragmentManager = getSupportFragmentManager();
        addHallFragment = AddHallFragment.newInstance(null, null);


    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        // Hiển thị nút quay lại
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // icon back
            getSupportActionBar().setTitle("Edit Cinema"); // đặt tiêu đề
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
        cinemaHallList = new ArrayList<>();
        recyler_view.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CinemaHallAdapter(this, cinemaHallList);
        adapter.setOnHallEditClickListener(cinemaHall -> {
           //TODO: handle add cinema hall
        });
        adapter.setOnHallDeleteClickListener((CinemaHall cinema, int position) -> {
            //TODO: handle delete cinema hall
        });
        recyler_view.setAdapter(adapter);
    }

    private void loadCinema(){
        Intent intent = getIntent();
         cinemaId = intent.getIntExtra("cinema_id", -1);
        if(cinemaId != -1){
            new getCinemaById(cinemaId).execute();
            new getCinemaHallByCinemaId(cinemaId).execute();
        }else{
//            btn_cancel_hall.setVisibility(View.GONE);
//            btn_save_hall.setVisibility(View.GONE);
        }
    }

    private void setupOnClick(){
        icon_add.setOnClickListener(v -> {
            if(cinemaId == -1){
                //TODO: handle add cinema hall is add new
                card_hall_list.setVisibility(View.VISIBLE);
                btn_cancel_hall.setVisibility(View.VISIBLE);
                btn_save_hall.setVisibility(View.VISIBLE);
                trans = fragmentManager.beginTransaction();
                trans.replace(R.id.card_hall_list, addHallFragment, "");
                trans.addToBackStack("pushA"); // tên j cx đc
                trans.commit();
            }else{
                //TODO: handle add cinema hall when action is edit
            }

        });
    }

    private class getCinemaHallByCinemaId extends AsyncTask<Void, Void, List<CinemaHall>> {
        private int cinemaId;
        public getCinemaHallByCinemaId(int cinemaId){
            this.cinemaId = cinemaId;
        }
        @Override
        protected List<CinemaHall> doInBackground(Void... voids) {
            try{
                HallCinemaDAO cinemaHallDAO = new HallCinemaDAO();
                return cinemaHallDAO.getAllCinemaHall(cinemaId);
            }catch(Exception e){
                Log.e(TAG, "Error loading list movie", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<CinemaHall> halls) {
            super.onPostExecute(cinemaHallList);
            if (halls != null) {
                cinemaHallList.clear();
                cinemaHallList.addAll(halls);
                adapter.notifyDataSetChanged(); // Thông báo cập nhật giao diện
                Log.d(TAG, "Load list cinema hall success");
            }else{
                Log.e(TAG, "Fail load list cinema hall");
            }
        }
    }

    private class getCinemaById extends AsyncTask<Void, Void, Cinema> {
        private int cinemaId;

        public getCinemaById(int cinemaId){
            this.cinemaId = cinemaId;
        }
        @Override
        protected Cinema doInBackground(Void... voids) {
            try{
                CinemaDAO cinemaDAO = new CinemaDAO();
                return cinemaDAO.getCinemaById(cinemaId);
            }catch (Exception e){
                Log.e(TAG, "Error loading cinema", e);
                return null;
            }
            }

            @Override
            protected void onPostExecute(Cinema c) {
                super.onPostExecute(cinema);
                if(c != null){
                    cinema = c;
                    edt_cinema_name.setText(cinema.getName());
                    edt_cinema_address.setText(cinema.getAddress());
                    edt_cinema_city.setText(cinema.getCityName());
                    edt_cinema_phone.setText(cinema.getContactInfo());
                    Log.d(TAG, "Load cinema success");
                }else{
                    Log.e(TAG, "Fail load cinema");
                }
                }
    }
}