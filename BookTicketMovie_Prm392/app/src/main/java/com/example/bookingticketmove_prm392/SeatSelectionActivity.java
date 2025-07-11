package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookingticketmove_prm392.Common.Constants;
import com.example.bookingticketmove_prm392.Enums.SeatType;
import com.example.bookingticketmove_prm392.models.Seat;
import com.example.bookingticketmove_prm392.database.dao.BaseDAO;
import com.example.bookingticketmove_prm392.database.dao.SeatDAO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SeatSelectionActivity extends AppCompatActivity {
    private static final String TAG = "SeatSelectionActivity";

    //component
    LinearLayout seatGridContainer;
    private Toolbar toolbar;
    private TextView tvCinema;
    private TextView tvMovieName;
    private TextView tvMovieInfor;
    private TextView tvTotalPrice;
    private TextView tvSelectedSeatsCount;
    private com.google.android.material.button.MaterialButton btnCompletePayment;

    // Data
    private String movie_title;
    private String cinema_name;
    private String startTime;
    private String endTime;
    private String DateTime;
    private double movie_price;



    private List<Seat> allSeats = new ArrayList<>(); // Danh sách tất cả các ghế
    private List<Seat> selectedSeats = new ArrayList<>(); // Danh sách các ghế đang được chọn
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        int hallId = getIntent().getIntExtra("HALL_ID", -1); // -1 là giá trị mặc định nếu không tìm thấy
        int showId = getIntent().getIntExtra("showtime_id", -1); // -1 là giá trị mặc định nếu không tìm thấy
        movie_title = getIntent().getStringExtra("movie_title");
        cinema_name = getIntent().getStringExtra("cinema_name");
        startTime = getIntent().getStringExtra("showtime_starttime");
        endTime = getIntent().getStringExtra("showtime_endtime");
        DateTime = getIntent().getStringExtra("showtime_date");
        movie_price = getIntent().getDoubleExtra("movie_price", -1);

        if (hallId == -1 || showId == -1) {
            Toast.makeText(this, "Thông tin phòng chiếu hoặc suất chiếu không hợp lệ.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "HALL_ID or SHOW_ID not provided in Intent.");
            finish(); // Đóng Activity nếu thiếu thông tin
            return;
        }

        initViews();
        setupToolbar();
        btnCompletePayment.setEnabled(false);
        loadAndDisplaySeats(hallId, showId);

        btnCompletePayment.setOnClickListener(v -> {
            if (!selectedSeats.isEmpty()) {
                // Có ghế được chọn, tiến hành chuyển sang màn hình thanh toán
                proceedToPayment();
            } else {
                Toast.makeText(this, "Vui lòng chọn ít nhất một ghế.", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void initViews() {
        seatGridContainer = findViewById(R.id.seat_grid_container);
        toolbar = findViewById(R.id.toolbar);
        tvSelectedSeatsCount = findViewById(R.id.tv_selected_seats_count);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvMovieName = findViewById(R.id.movie_name);
        tvCinema = findViewById(R.id.tv_movie_cinema);
        tvMovieInfor = findViewById(R.id.tv_movie_info);
        btnCompletePayment = findViewById(R.id.btn_complete_payment);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Select Seat");
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

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }


    private void loadAndDisplaySeats(int hallId, int showId) {
        // Khởi tạo lại danh sách ghế trước khi tải mới
        allSeats.clear();
        selectedSeats.clear();
        updateSummary(); // Cập nhật hiển thị tổng tiền về 0

        // Bắt đầu task để lấy dữ liệu ghế từ DB
        new SeatDAO.GetSeatsForBookingTask(hallId, showId, new BaseDAO.DatabaseTaskListener<List<Seat>>() {
            @Override
            public void onSuccess(List<Seat> result) {
                if (result != null && !result.isEmpty()) {
                    allSeats.addAll(result); // Thêm ghế vào danh sách chung
                    displaySeats(allSeats); // Hiển thị ghế lên UI
                } else {
                    Toast.makeText(SeatSelectionActivity.this, "Không tìm thấy ghế cho phòng này hoặc suất chiếu này.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "No seats found for HallID: " + hallId + " and ShowID: " + showId);
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(SeatSelectionActivity.this, "Lỗi khi tải ghế: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error fetching seats: ", error);
            }
        }).execute();
    }

    /**
     * Hiển thị danh sách ghế trên giao diện người dùng.
     * @param seats Danh sách các đối tượng Seat.
     */
    private void displaySeats(List<Seat> seats) {
        seatGridContainer.removeAllViews();

        char currentRowChar = ' ';
        LinearLayout currentRowLayout = null;
        GridLayout currentGridLayout = null;

        for (Seat seat : seats) {
            char seatRowChar = seat.getRowNumber().charAt(0);
            if (seatRowChar != currentRowChar) {
                // Hàng mới, thêm hàng cũ vào container nếu có
                if (currentRowLayout != null) {
                    seatGridContainer.addView(currentRowLayout);
                }

                // Khởi tạo layout cho hàng mới
                currentRowChar = seatRowChar;
                currentRowLayout = new LinearLayout(this);
                currentRowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                currentRowLayout.setOrientation(LinearLayout.HORIZONTAL);
                currentRowLayout.setGravity(Gravity.CENTER_VERTICAL);
                ((LinearLayout.LayoutParams) currentRowLayout.getLayoutParams()).bottomMargin = (int) dpToPx(8);

                // Thêm TextView cho nhãn hàng (A, B, C...)
                TextView rowLabel = new TextView(this);
                rowLabel.setText(String.valueOf(currentRowChar));
                rowLabel.setTextColor(getResources().getColor(R.color.white));
                rowLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                labelParams.setMarginEnd((int) dpToPx(8));
                rowLabel.setLayoutParams(labelParams);
                currentRowLayout.addView(rowLabel);

                // Khởi tạo GridLayout cho các ghế trong hàng
                currentGridLayout = new GridLayout(this);
                // Dùng getMaxColumnsInRow để tự động điều chỉnh số cột cho GridLayout
                currentGridLayout.setColumnCount(getMaxColumnsInRow(seats, currentRowChar));
                currentGridLayout.setRowCount(1);
                currentGridLayout.setOrientation(GridLayout.HORIZONTAL);
                currentGridLayout.setUseDefaultMargins(true);
                currentGridLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                ((LinearLayout.LayoutParams) currentGridLayout.getLayoutParams()).gravity = Gravity.CENTER;
                currentRowLayout.addView(currentGridLayout);
            }

            // Tạo ImageView cho ghế
            ImageView seatView = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = (int) dpToPx(22);
            params.height = (int) dpToPx(22);
            seatView.setLayoutParams(params);

            // Gán đối tượng Seat vào tag của ImageView để dễ dàng truy xuất
            seatView.setTag(seat);

            // Lấy drawable từ thuộc tính theme cho hiệu ứng ripple
            // Đã sửa lỗi: android.R.attr.selectableItemBackgroundBorderless
            TypedValue outValue = new TypedValue();
            this.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
            seatView.setBackgroundResource(outValue.resourceId);

            // Cập nhật hình ảnh/màu sắc của ghế dựa trên trạng thái và loại ghế
            updateSeatView(seatView, seat);

             //Xử lý sự kiện click
            seatView.setOnClickListener(v -> {
                ImageView clickedSeatView = (ImageView) v;
                Seat clickedSeat = (Seat) clickedSeatView.getTag();

                if (clickedSeat.getStatus().equals("AVAILABLE")) {
                    clickedSeat.setStatus("SELECTED");
                    selectedSeats.add(clickedSeat);
                    //Toast.makeText(this, "Đã chọn ghế: " + clickedSeat.getRowNumber() + clickedSeat.getColumnNumber(), Toast.LENGTH_SHORT).show();
                } else if (clickedSeat.getStatus().equals("SELECTED")) {
                    clickedSeat.setStatus("AVAILABLE");
                    selectedSeats.remove(clickedSeat);
                    //Toast.makeText(this, "Đã hủy chọn ghế: " + clickedSeat.getRowNumber() + clickedSeat.getColumnNumber(), Toast.LENGTH_SHORT).show();
                }
                // Nếu là "SOLD", không làm gì cả

                updateSeatView(clickedSeatView, clickedSeat); // Cập nhật UI của ghế
                updateSummary(); // Cập nhật tổng tiền và số ghế
            });

            // Đảm bảo currentGridLayout không null trước khi addView
            if (currentGridLayout != null) {
                currentGridLayout.addView(seatView);
            }
        }

        // Đảm bảo thêm hàng cuối cùng sau vòng lặp
        if (currentRowLayout != null) {
            seatGridContainer.addView(currentRowLayout);
        }
        updateSummary(); // Cập nhật tổng tiền/số ghế ban đầu (thường là 0)
    }

    /**
     * Cập nhật hình ảnh/màu sắc của ImageView ghế dựa trên trạng thái và loại ghế
     * @param seatView ImageView của ghế
     * @param seat Đối tượng Seat
     */
    private void updateSeatView(ImageView seatView, Seat seat) {
        // Sử dụng một drawable cơ bản cho tất cả các ghế
        seatView.setImageResource(R.drawable.ic_seat_available);

        int seatColor;
        switch (seat.getStatus()) {
            case "SOLD":
                seatColor = getResources().getColor(R.color.red_sold_seat);
                seatView.setEnabled(false); // Vô hiệu hóa click
                seatView.setClickable(false); // Đảm bảo không click được
                seatView.setFocusable(false);
                break;
            case "SELECTED":
                seatColor = getResources().getColor(R.color.green_selected_seat);
                seatView.setEnabled(true); // Đảm bảo có thể click lại để hủy chọn
                seatView.setClickable(true);
                seatView.setFocusable(true);
                break;
            case "AVAILABLE":
            default: // Mặc định là AVAILABLE, chọn màu theo loại ghế
                seatView.setEnabled(true); // Đảm bảo có thể click để chọn
                seatView.setClickable(true);
                seatView.setFocusable(true);
                switch (seat.getSeatType()) {
                    case REGULAR:
                        seatColor = getResources().getColor(R.color.gray_regular_seat);
                        break;
                    case PREMIUM:
                        seatColor = getResources().getColor(R.color.yellow_premium_seat);
                        break;
                    case VIP:
                        seatColor = getResources().getColor(R.color.purple_vip_seat);
                        break;
                    default:
                        seatColor = getResources().getColor(R.color.gray_regular_seat); // Màu mặc định nếu SeatType không xác định
                }
                break;
        }
        seatView.setColorFilter(seatColor, PorterDuff.Mode.SRC_IN);
        //seatView.setBackgroundResource(R.color.input_border);
    }

    /**
     * Tính toán tổng tiền của các ghế đã chọn
     * @return Tổng tiền
     */
    private double calculateTotalPrice() {
        double total = 0;
        for (Seat seat : selectedSeats) {
            switch (seat.getSeatType()) {
                case REGULAR:
                    total += movie_price;
                    break;
                case PREMIUM:
                    total += movie_price + Constants.PREMIUM_ADD_ON;
                    break;
                case VIP:
                    total += movie_price + Constants.VIP_ADD_ON;
                    break;
            }
        }
        return total;
    }

    /**
     * Cập nhật hiển thị tổng tiền và số ghế đã chọn
     */
    private void updateSummary() {
        tvSelectedSeatsCount.setText(selectedSeats.size() + " ghế");
        String formattedPrice = String.format("%,.0fđ", calculateTotalPrice()).replace(",", ".");
        tvTotalPrice.setText(formattedPrice);
        tvMovieName.setText(movie_title);
        tvCinema.setText(cinema_name);
        String formattedDate = convertDateFormat(DateTime);
        tvMovieInfor.setText(formattedDate +" " + startTime + " ~ " + endTime);

        if (selectedSeats.isEmpty()) {
            btnCompletePayment.setEnabled(false); // Vô hiệu hóa nút
            btnCompletePayment.setBackgroundTintList(getResources().getColorStateList(R.color.gray_disabled_button)); // Đổi màu xám
        } else {
            btnCompletePayment.setEnabled(true); // Kích hoạt nút
            btnCompletePayment.setBackgroundTintList(getResources().getColorStateList(R.color.green_enabled_button)); // Đổi màu xanh
        }

    }

    private void proceedToPayment() {
        Intent intent = new Intent(SeatSelectionActivity.this, ShowtimeSelectionActivity.class); // Thay PaymentActivity.class bằng tên Activity thanh toán của bạn

        // Truyền thông tin phim và suất chiếu
        intent.putExtra("movie_title", movie_title);
        intent.putExtra("cinema_name", cinema_name);
        intent.putExtra("showtime_date", DateTime);
        intent.putExtra("showtime_starttime", startTime);
        intent.putExtra("showtime_endtime", endTime);
        intent.putExtra("movie_price", movie_price); // Giá vé gốc

        // Truyền thông tin các ghế đã chọn
        ArrayList<String> selectedSeatNames = new ArrayList<>();
        ArrayList<String> selectedSeatTypes = new ArrayList<>(); // Để tính giá ở màn hình Payment
        double totalCalculatedPrice = 0; // Tính toán lại tổng tiền để đảm bảo

        for (Seat seat : selectedSeats) {
            selectedSeatNames.add(seat.getRowNumber() + seat.getColumnNumber());
            selectedSeatTypes.add(seat.getSeatType().name()); // Lưu tên enum

            // Tính lại tổng tiền để truyền qua, hoặc bạn có thể truyền totalCalculatedPrice trực tiếp
            // nếu tin tưởng hàm calculateTotalPrice() luôn đúng
            switch (seat.getSeatType()) {
                case REGULAR:
                    totalCalculatedPrice += movie_price;
                    break;
                case PREMIUM:
                    totalCalculatedPrice += movie_price + Constants.PREMIUM_ADD_ON ;
                    break;
                case VIP:
                    totalCalculatedPrice += movie_price + Constants.VIP_ADD_ON;
                    break;
            }
        }

        intent.putStringArrayListExtra("selected_seat_names", selectedSeatNames);
        intent.putStringArrayListExtra("selected_seat_types", selectedSeatTypes);
        intent.putExtra("total_price", totalCalculatedPrice); // Truyền tổng tiền cuối cùng

        startActivity(intent);
    }

    /**
     * Tìm số cột lớn nhất trong một hàng để thiết lập columnCount cho GridLayout
     * @param seats Danh sách tất cả ghế
     * @param rowChar Ký tự của hàng
     * @return Số cột lớn nhất trong hàng đó
     */
    private int getMaxColumnsInRow(List<Seat> seats, char rowChar) {
        int maxCol = 0;
        for (Seat seat : seats) {
            if (seat.getRowNumber().charAt(0) == rowChar) {
                if (seat.getColumnNumber() > maxCol) {
                    maxCol = seat.getColumnNumber();
                }
            }
        }
        return maxCol > 0 ? maxCol : 1; // Trả về ít nhất 1 để tránh lỗi GridLayout nếu hàng rỗng
    }

    // Hàm giúp chuyển đổi định dạng ngày từ "YYYY/MM/DD" sang "DD tháng MM, YYYY"
    private String convertDateFormat(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = inputFormat.parse(dateString);

            // Định dạng đầu ra mong muốn
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd 'tháng' MM, yyyy", new Locale("vi", "VN"));
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString; // Trả về chuỗi gốc nếu có lỗi
        }
    }

}