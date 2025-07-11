package com.example.bookingticketmove_prm392;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditCinemaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditCinemaFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_CINEMA = "cinema";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Cinema cinema;

    public EditCinemaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditCinemaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditCinemaFragment newInstance(Cinema cinema) {
        EditCinemaFragment fragment = new EditCinemaFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CINEMA, cinema);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cinema = (Cinema) getArguments().getSerializable(ARG_CINEMA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_edit_cinema, container, false);
        // Ví dụ: EditText edtName = view.findViewById(R.id.edtCinemaName);
        // edtName.setText(cinema.getName());
        // Tương tự cho các trường khác
        return view;
    }
}