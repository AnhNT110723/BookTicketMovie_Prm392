package com.example.bookingticketmove_prm392;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.bookingticketmove_prm392.database.dao.CinemaDAO;
import com.example.bookingticketmove_prm392.database.dao.HallCinemaDAO;
import com.example.bookingticketmove_prm392.models.Cinema;
import com.example.bookingticketmove_prm392.models.CinemaHall;
import com.example.bookingticketmove_prm392.models.City;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddHallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddHallFragment extends Fragment {

    private static final String TAG = "AddHallFragment";
    private Spinner select_cinema_spinner;
    private TextInputEditText edt_hall_name;
    private TextInputEditText edt_total_seats;
    private Button btn_cancel_hall;
    private Button btn_save_hall;
    private LinearLayout fragment_layout;
    private List<Cinema> cinemaList;

    public AddHallFragment() {
        // Required empty public constructor
    }


    public static AddHallFragment newInstance() {
        AddHallFragment fragment = new AddHallFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_hall, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        new getCinemaList().execute();
        setupOnClick();


    }

private void initView(View view){
    edt_hall_name = view.findViewById(R.id.edt_name_hall);
    edt_total_seats = view.findViewById(R.id.edt_total_seats);
    btn_cancel_hall = view.findViewById(R.id.btn_cancel_hall);
    btn_save_hall = view.findViewById(R.id.btn_save_hall);
    fragment_layout = view.findViewById(R.id.fragment_layout);
    select_cinema_spinner = view.findViewById(R.id.select_cinema_spinner);
    cinemaList = new ArrayList<>();
}

private void setupOnClick(){
    btn_save_hall.setOnClickListener(v -> {

        //Check empty
        if (edt_hall_name.getText().toString().trim().isEmpty() ||
                edt_total_seats.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }


            int totalSeats = Integer.parseInt(edt_total_seats.getText().toString().trim());


            fragment_layout.setVisibility(View.VISIBLE);
            String nameHall = edt_hall_name.getText().toString().trim();
            Cinema selectedCinema = (Cinema) select_cinema_spinner.getSelectedItem();
            int cinemaId = selectedCinema.getCinemaId();
             CinemaHall cinemaHall = new CinemaHall( cinemaId, nameHall, totalSeats);
             new addHall(cinemaHall).execute();
       
    });

    btn_cancel_hall.setOnClickListener(v -> {
        edt_hall_name.setText("");
        edt_total_seats.setText("");
        fragment_layout.setVisibility(View.GONE);
    });

    }

    private class addHall extends AsyncTask<Void, Void, Boolean> {
        private CinemaHall cinemaHall;
        public addHall(CinemaHall cinemaHall){
            this.cinemaHall = cinemaHall;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                return new HallCinemaDAO().addCinemaHall(cinemaHall);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                edt_hall_name.setText("");
                edt_total_seats.setText("");
                select_cinema_spinner.setSelection(-1);
                Log.d("addHall", "Add hall cinema success");
                Toast.makeText(getContext(), "Add hall cinema success", Toast.LENGTH_SHORT).show();

            } else {
                Log.e("addHall", "Add hall cinema failed");
                Toast.makeText(getContext(), "Add hall cinema failed", Toast.LENGTH_SHORT).show();
            }
        }
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

                Cinema defaultCinema = new Cinema(-1, "Select Cinema");
                cinemaList.add(0, defaultCinema);

                ArrayAdapter<Cinema> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        cinemaList
                );

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                select_cinema_spinner.setAdapter(adapter);

                Log.d(TAG, "Load list cinema success");
            }else{

                Log.e(TAG, "Fail load list cinema");
            }
        }

    }
}