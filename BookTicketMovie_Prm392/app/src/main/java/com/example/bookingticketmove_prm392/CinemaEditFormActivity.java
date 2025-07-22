package com.example.bookingticketmove_prm392;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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
import com.example.bookingticketmove_prm392.database.dao.CityDAO;
import com.example.bookingticketmove_prm392.database.dao.HallCinemaDAO;
import com.example.bookingticketmove_prm392.models.Cinema;
import com.example.bookingticketmove_prm392.models.CinemaHall;
import com.example.bookingticketmove_prm392.models.City;
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


    private ImageView icon_add;
    private LinearLayout card_hall_list;
    private CardView card_hall;


    //Load data

    private Cinema cinema;
    private List<CinemaHall> cinemaHallList;
    private int cinemaId;
    private Spinner select_city_spinner;
    private List<City> cityList;
    private int selectedCity;

    // Fragment
    FragmentManager fragmentManager;
    FragmentTransaction trans;
    AddHallFragment addHallFragment;
    HallCinemaFragment hallCinemaFragment;



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
        loadCinema();
        setupToolbar();
        setupRecyclerView();
        setupSpinnerCity();
        setupOnClick();
    }


    private void initView(){
        toolbar = findViewById(R.id.toolbar);
        recyler_view = findViewById(R.id.recycler_view);
        edt_cinema_name = findViewById(R.id.edt_cinema_name);
        edt_cinema_address = findViewById(R.id.edt_cinema_address);
        edt_cinema_phone = findViewById(R.id.edt_cinema_phone);
        btn_save_infor = findViewById(R.id.btn_save_infor);
        btn_cancel_infor = findViewById(R.id.btn_cancel_infor);
//        btn_cancel_hall = findViewById(R.id.btn_cancel_hall);
//        btn_save_hall = findViewById(R.id.btn_save_hall);
        select_city_spinner = findViewById(R.id.select_city_spinner);
        icon_add = findViewById(R.id.icon_add);
        card_hall_list= findViewById(R.id.card_hall_list);
        card_hall = findViewById(R.id.card_hall);
        fragmentManager = getSupportFragmentManager();
        addHallFragment = AddHallFragment.newInstance();
        hallCinemaFragment = hallCinemaFragment.newInstance(cinemaId);
        cityList = new ArrayList<>();


    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        // Hiển thị nút quay lại
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // icon back
            if(cinemaId != -1){
                getSupportActionBar().setTitle("Edit Cinema"); // đặt tiêu đề
            }else{
                getSupportActionBar().setTitle("Add Cinema"); // đặt tiêu đề
                card_hall.setVisibility(View.GONE);
            }

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
            showEditHallDialog(cinemaHall);
        });
        adapter.setOnHallDeleteClickListener( (CinemaHall cinemaHall, int position) -> {
            //TODO: handle delete cinema hall
            int hallId = cinemaHall.getHallId();
            new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to delete this cinema hall?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        new deleteCinemaHall(hallId, position).execute();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
        recyler_view.setAdapter(adapter);
    }

    private void showEditHallDialog(CinemaHall cinemaHall){
        Dialog dialog = new Dialog(this); // context = this (Activity) hoặc getContext() (Fragment)
        dialog.setContentView(R.layout.edit_hall_form);
        dialog.setCancelable(true); // Bấm ngoài để đóng

        TextInputEditText txtNameOfHall = dialog.findViewById(R.id.edt_name_of_hall);
        TextInputEditText txtTotalSeats = dialog.findViewById(R.id.edt_total_seats);
        Button btnOk = dialog.findViewById(R.id.btn_save_hall);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel_hall);

        txtNameOfHall.setText(cinemaHall.getName());
        txtTotalSeats.setText(String.valueOf(cinemaHall.getTotalSeats()));


        btnOk.setOnClickListener(v -> {
            String name = txtNameOfHall.getText().toString();
            int totalSeats = Integer.parseInt(txtTotalSeats.getText().toString());
            CinemaHall updatedHall = new CinemaHall(
                    cinemaHall.getHallId(),
                    cinemaHall.getCinemaId(),
                    name,
                    totalSeats
            );
            new updateCinemaHall(updatedHall).execute();

            dialog.dismiss(); // đóng dialog
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
        // Set kích thước dialog
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

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
                //show fragment add hall cinema
                //icon_add.setVisibility(View.GONE);
                card_hall_list.setVisibility(View.VISIBLE);
                HallCinemaFragment hallCinemaFragment1 = HallCinemaFragment.newInstance(cinemaId);
                trans = fragmentManager.beginTransaction();
                trans.replace(R.id.card_hall_list, hallCinemaFragment1, "");


                hallCinemaFragment1.setOnAddHallSuccessListener(() -> {
                    new getCinemaHallByCinemaId(cinemaId).execute();
                });
                trans.addToBackStack("pushA"); // tên j cx đc
                trans.commit();
        });

        btn_save_infor.setOnClickListener(v -> {
            String name = edt_cinema_name.getText().toString().trim();
            String address = edt_cinema_address.getText().toString().trim();
            String phone = edt_cinema_phone.getText().toString().trim();
            City chooseCity = (City) select_city_spinner.getSelectedItem();
            int cityId = chooseCity.getCityId();

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || cityId == -1) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Cinema cinema = new Cinema(name, address, cityId, phone);
            if(cinemaId == -1){
               new addCinema(cinema).execute();
            }else{
                cinema.setCinemaId(cinemaId);
                new updateCinema(cinema).execute();
            }
        });

        btn_cancel_infor.setOnClickListener(v -> {
            edt_cinema_name.clearFocus();
            edt_cinema_address.clearFocus();
            edt_cinema_phone.clearFocus();
            select_city_spinner.clearFocus();
            if(cinemaId == -1){
                edt_cinema_name.setText("");
                edt_cinema_address.setText("");
                edt_cinema_phone.setText("");
                select_city_spinner.setSelection(-1);
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                new getCinemaById(cinemaId).execute();
            }
        });


    }


    private void setupSpinnerCity(){
        new getCityList().execute();
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
                    edt_cinema_phone.setText(cinema.getContactInfo());
                    selectedCity = cinema.getCityId();

                    // Set giá trị ban đầu dựa trên ID

                    int selectedIndex = 0;

                    for (int i = 0; i < cityList.size(); i++) {
                        if (cityList.get(i).getCityId() == selectedCity) {
                            selectedIndex = i;
                            break;
                        }
                    }

                    select_city_spinner.setSelection(selectedIndex);
                    Log.d(TAG, "Load cinema success");
                }else{
                    Log.e(TAG, "Fail load cinema");
                }
                }
    }

    private class getCityList extends AsyncTask<Void, Void, List<City>> {
        @Override
        protected List<City> doInBackground(Void... voids) {
            try {
                CityDAO cityDAO = new CityDAO();
                return cityDAO.getAllCities();
            } catch (Exception e) {
                Log.e(TAG, "Error loading city list", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<City> cities) {
            super.onPostExecute(cities);
            if (cities != null) {
                cityList.clear();
                cityList.addAll(cities);


                City defaultCity = new City(-1, "Select City");
                cityList.add(0, defaultCity);
                Log.d(TAG, "Load list city success");

                ArrayAdapter<City> adapter = new ArrayAdapter<>(
                        CinemaEditFormActivity.this,
                        android.R.layout.simple_spinner_item,
                        cityList
                );

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                select_city_spinner.setAdapter(adapter);
                new getCinemaById(cinemaId).execute();

            }else{
                Log.e(TAG, "Fail load city list");
            }
            }
    }
    private class addCinema extends AsyncTask<Void, Void, Boolean>{
        private Cinema cinema;
        public addCinema(Cinema cinema){
            this.cinema = cinema;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                CinemaDAO cinemaDAO = new CinemaDAO();
                return cinemaDAO.addCinema(cinema);
            }catch (Exception e){
                Log.e(TAG, "Error add cinema", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result){
                edt_cinema_name.setText("");
                edt_cinema_address.setText("");
                edt_cinema_phone.setText("");
                select_city_spinner.setSelection(-1);

                Log.d(TAG, "Add cinema success");
                new LoadCinemas().execute();
                Toast.makeText(CinemaEditFormActivity.this, "Add cinema success", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }else{
                Log.e(TAG, "Fail add cinema");
                Toast.makeText(CinemaEditFormActivity.this, "Fail add cinema ", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private class updateCinema extends AsyncTask<Void, Void, Boolean>{
        private Cinema cinema;
        public updateCinema(Cinema cinema){
            this.cinema = cinema;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                CinemaDAO cinemaDAO = new CinemaDAO();
                return cinemaDAO.updateCinema(cinema);
            }catch (Exception e){
                Log.e(TAG, "Error update cinema", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result){
                Log.d(TAG, "Update cinema success");
                edt_cinema_name.clearFocus();
                edt_cinema_address.clearFocus();
                edt_cinema_phone.clearFocus();
                select_city_spinner.clearFocus();
                Toast.makeText(CinemaEditFormActivity.this, "Update cinema success", Toast.LENGTH_SHORT).show();
            }else{
                Log.e(TAG, "Fail update cinema");
                Toast.makeText(CinemaEditFormActivity.this, "Fail update cinema ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LoadCinemas extends AsyncTask<Void, Void, List<Cinema>> {
        @Override
        protected List<Cinema> doInBackground(Void... voids) {
            try {
                return new CinemaDAO().getAllCinemas();
            } catch (Exception e) {
                Log.e(TAG, "Error loading cinemas", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Cinema> cinemas) {
            if (cinemas != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class deleteCinemaHall extends AsyncTask<Void, Void, Boolean>{
        private int cinemaHallId;
        private int position;

        public deleteCinemaHall(int cinemaHallId, int position){
            this.cinemaHallId = cinemaHallId;
            this.position = position;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                HallCinemaDAO hallCinemaDAO = new HallCinemaDAO();
                return hallCinemaDAO.deleteCinemaHall(cinemaHallId);
            }catch (Exception e){
                Log.e(TAG, "Error delete cinema hall", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result){
                for (int i = 0; i < cinemaHallList.size(); i++) {
                    if (cinemaHallList.get(i).getHallId() == cinemaHallId) {
                        cinemaHallList.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }

                Log.d(TAG, "Delete cinema hall success");
                Toast.makeText(CinemaEditFormActivity.this, "Delete cinema hall success", Toast.LENGTH_SHORT).show();
            }
            else{
                Log.e(TAG, "Fail delete cinema hall");
                Toast.makeText(CinemaEditFormActivity.this, "Fail delete cinema hall", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class  updateCinemaHall extends AsyncTask<Void, Void, Boolean>{
        private CinemaHall cinemaHall;
        public updateCinemaHall(CinemaHall cinemaHall){
            this.cinemaHall = cinemaHall;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                HallCinemaDAO hallCinemaDAO = new HallCinemaDAO();
                return hallCinemaDAO.updateCinemaHall(cinemaHall);
            } catch (Exception e) {
                Log.e(TAG, "Error update cinema hall", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result){
                for (int i = 0; i < cinemaHallList.size(); i++) {
                    if (cinemaHallList.get(i).getHallId() == cinemaHall.getHallId()) {
                        cinemaHallList.set(i, cinemaHall); // Cập nhật item
                        adapter.notifyItemChanged(i);       // Cập nhật giao diện
                        break;
                    }
                }
                Log.d(TAG, "Update cinema hall success");
                Toast.makeText(CinemaEditFormActivity.this, "Update cinema hall success", Toast.LENGTH_SHORT).show();
            }
            else{
                Log.e(TAG, "Fail update cinema hall");
                Toast.makeText(CinemaEditFormActivity.this, "Fail update cinema hall", Toast.LENGTH_SHORT).show();
            }
        }
    }


}