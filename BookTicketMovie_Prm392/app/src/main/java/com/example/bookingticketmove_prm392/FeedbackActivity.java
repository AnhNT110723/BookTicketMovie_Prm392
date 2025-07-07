package com.example.bookingticketmove_prm392;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.adapters.FeedbackAdapter;
import com.example.bookingticketmove_prm392.database.dao.FeedbackDAO;
import com.example.bookingticketmove_prm392.models.Comment;
import com.example.bookingticketmove_prm392.models.Feedback;
import com.example.bookingticketmove_prm392.models.Movie;
import com.example.bookingticketmove_prm392.models.Vote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";
    //    UI Components
    private Toolbar toolbar;
    private TextView txt_no_comment;
    private RecyclerView recycler_view;
    private ImageView star_1;
    private ImageView star_2;
    private ImageView star_3;
    private ImageView star_4;
    private ImageView star_5;
    private EditText edt_enter_comment;
    private Button btn_submit;

    //    RecyclerView
    private FeedbackAdapter adapter;
    private List<Feedback> feedbackList;
    private int movieId;

    private int userId;
    private int selectedRatingValue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get data from intent
        movieId = getIntent().getIntExtra("movie_id", -1);

        initView(); // ⬅️ Gọi đầu tiên để chắc chắn các view đã được gán
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        selectedRatingValue = 0;
        userId = isLoggin();


        new checkVote(userId, movieId).execute();

        // Load feedback
        new getFeedbackList(movieId).execute();




    }

    private void initView(){
        toolbar = findViewById(R.id.toolbar);
        recycler_view = findViewById(R.id.recycler_view);
        txt_no_comment = findViewById(R.id.txt_no_comment);
        star_1 = findViewById(R.id.star_1);
        star_2 = findViewById(R.id.star_2);
        star_3 = findViewById(R.id.star_3);
        star_4 = findViewById(R.id.star_4);
        star_5 = findViewById(R.id.star_5);
        edt_enter_comment = findViewById(R.id.edt_enter_comment);
        btn_submit = findViewById(R.id.btn_submit);

    }

    public int isLoggin(){
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        return userId;
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        // Hiển thị nút quay lại
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // icon back
            getSupportActionBar().setTitle("All Comments"); // đặt tiêu đề
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

    public void setupRecyclerView(){
        feedbackList = new ArrayList<>();
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeedbackAdapter(this, feedbackList);
        recycler_view.setAdapter(adapter);
    }

    public void updateUIStar(int rating) {
         selectedRatingValue = rating;
        ImageView[] stars = { star_1, star_2, star_3, star_4, star_5 };

        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star); // sao sáng
            } else {
                stars[i].setImageResource(R.drawable.star_border); // sao tối
            }
        }
    }



    public void setupClickListeners(){
        star_1.setOnClickListener(v -> updateUIStar(1));
        star_2.setOnClickListener(v -> updateUIStar(2));
        star_3.setOnClickListener(v -> updateUIStar(3));
        star_4.setOnClickListener(v -> updateUIStar(4));
        star_5.setOnClickListener(v -> updateUIStar(5));
        btn_submit.setOnClickListener(v -> {
            String commentText = edt_enter_comment.getText().toString().trim();
            boolean hasComment = !commentText.isEmpty();
            boolean hasRating = selectedRatingValue > 0;

            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
            int userId = prefs.getInt("userId", -1);

            if (!isLoggedIn || userId == -1) {
                Toast.makeText(this, "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!hasComment && !hasRating) {
                Toast.makeText(this, "Vui lòng nhập bình luận hoặc đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            if (hasRating) {
                new addVote(userId, movieId, selectedRatingValue).execute();
            }

            if (hasComment) {
                new addComment(userId, movieId, commentText).execute();
            }

            // Reset UI
            selectedRatingValue = 0;
            updateUIStar(0);
            edt_enter_comment.setText("");

            // Refresh feedback list
            new getFeedbackList(movieId).execute();
        });


    }


    private class getFeedbackList extends AsyncTask<Void, Void, List<Feedback>> {
        private int movieId;
        public getFeedbackList(int movieId){
            this.movieId = movieId;
        }
        @Override
        protected List<Feedback> doInBackground(Void... voids) {
           try {
               FeedbackDAO feedbackDAO = new FeedbackDAO();
               return feedbackDAO.getAllFeedbackByMovieId(movieId);
           }catch(Exception e){
               Log.e(TAG, "Error loading list feedback", e);
               return null;
           }
        }


        @Override
        protected void onPostExecute(List<Feedback> feedbacks) {
            if (feedbacks != null && feedbacks.size() > 0) {
                feedbackList.clear();
                feedbackList.addAll(feedbacks);
                adapter.notifyDataSetChanged(); // Thông báo cập nhật giao diện
                Log.d(TAG, "Load list movie favorite success");
            } else if(feedbacks.size() == 0) {
                recycler_view.setVisibility(View.GONE);
                txt_no_comment.setVisibility(View.VISIBLE);

            }else{
                Log.e(TAG, "Fail load list movie favorite");
            }
        }
    }

    private class addComment extends AsyncTask<Void, Void, Boolean>{
        private int userId;
        private int movieId;
        private String commentText;
        public addComment(int userId, int movieId, String commentText){
            this.userId = userId;
            this.movieId = movieId;
            this.commentText = commentText;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                FeedbackDAO feedbackDAO = new FeedbackDAO();
                return feedbackDAO.addComment(new Comment(userId, movieId, commentText));
            } catch (Exception e) {
                Log.d(TAG, "Error add comment: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result){
            if (result) {
                new getFeedbackList(movieId).execute();
                adapter.notifyDataSetChanged(); // Thông báo cập nhật giao diện
                Log.d(TAG, "Create comment successful success");
                Toast.makeText(FeedbackActivity.this, "Add comments successfull", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Fail create comment ");
            }
        }
    }

    private class addVote extends AsyncTask<Void, Void, Boolean>{
        private int userId;
        private int movieId;
        private int ratingValue;

        public addVote(int userId, int movieId, int ratingValue){
            this.userId=userId;
            this.movieId=movieId;
            this.ratingValue=ratingValue;
        }


        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                FeedbackDAO feedbackDAO = new FeedbackDAO();
                return feedbackDAO.addVote(new Vote(userId, movieId, ratingValue));
            } catch (Exception e) {
                Log.d(TAG, "Error add comment: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result){
            if (result) {
                Toast.makeText(FeedbackActivity.this, "Vote rating successfull", Toast.LENGTH_SHORT).show();

                adapter.notifyDataSetChanged(); // Thông báo cập nhật giao diện
                Log.d(TAG, "Create vote successful");

            } else {
                Log.e(TAG, "Fail create comment ");
            }
        }
    }

    private class checkVote extends AsyncTask<Void, Void, Boolean>{
        private int userId;
        private int movieId;
        public checkVote(int userId, int movieId){
            this.userId = userId;
            this.movieId = movieId;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                FeedbackDAO feedbackDAO = new FeedbackDAO();
                return feedbackDAO.checkVote(userId, movieId);
            } catch (Exception e) {
                Log.d(TAG, "Error add comment: " + e.getMessage());
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean result){
            if (result) {
                star_1.setVisibility(View.GONE);
                star_2.setVisibility(View.GONE);
                star_3.setVisibility(View.GONE);
                star_4.setVisibility(View.GONE);
                star_5.setVisibility(View.GONE);
                selectedRatingValue = 0;
            }
        }
    }


}