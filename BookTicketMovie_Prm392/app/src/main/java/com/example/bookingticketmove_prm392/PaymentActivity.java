package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.bookingticketmove_prm392.database.dao.BaseDAO;
import com.example.bookingticketmove_prm392.database.dao.BookingDAO;
import com.example.bookingticketmove_prm392.models.Booking;
import com.example.bookingticketmove_prm392.models.BookingSeat;
import com.example.bookingticketmove_prm392.models.Payment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PaymentActivity extends AppCompatActivity {
    //component
    private Toolbar toolbar;
    private TextView tvAmount;
    private TextView tvContent;
    private Button btnVnpayPayment;
    private WebView webView;


    //config vnpay
    private static final String VNPAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNPAY_TMN_CODE = "0OQU4OY7";
    private static final String VNPAY_HASH_SECRET = "FCPH7HLLOPYBVYH1ZX84XXMBJH8JMW98";
    private static final String VNPAY_RETURN_URL = "https://yourapp.com/return";

    //Data
    private String amount;
    private String movie_title;
    private String cinema_name;
    private String startTime;
    private String endTime;
    private String showDate;
    private double movie_price;
    private TextView tvMessage;
    private int hallId;
    private int showId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Check if user is logged in
        int userId = getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Bạn cần đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        initViews();
        setupToolbar();

        // Nhận dữ liệu từ intent
        amount = String.format("%.0f", getIntent().getDoubleExtra("total_price", 0));
        movie_title = getIntent().getStringExtra("movie_title");
        cinema_name = getIntent().getStringExtra("cinema_name");
        showDate = getIntent().getStringExtra("showtime_date");
        startTime = getIntent().getStringExtra("showtime_starttime");
        endTime = getIntent().getStringExtra("showtime_endtime");
        movie_price = getIntent().getDoubleExtra("movie_price", 0);
        hallId = getIntent().getIntExtra("hall_id", 0);
        showId = getIntent().getIntExtra("show_id", 0);

        ArrayList<String> selectedSeatNames = getIntent().getStringArrayListExtra("selected_seat_names");
        ArrayList<String> selectedSeatTypes = getIntent().getStringArrayListExtra("selected_seat_types");
        ArrayList<Integer> selectedSeatIds = getIntent().getIntegerArrayListExtra("selected_seat_ids");

        // Hiển thị thông tin
        tvAmount.setText("Số tiền: " + amount + " VND");
        tvContent.setText("Thanh toán vé xem phim: " + movie_title + " tại " + cinema_name);

        // Xử lý nút Trang chủ và Quay lại
        findViewById(R.id.btn_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
       // findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnVnpayPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPayment("ORDER_" + System.currentTimeMillis());
            }
        });

    }


    private void requestPayment(String orderId) {
        try {
            // Kiểm tra amount có hợp lệ không
            if (amount == null || amount.isEmpty()) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Loại bỏ phần thập phân nếu có
            String cleanAmount = amount.replaceAll("[^\\d]", "");
            int amountInt = Integer.parseInt(cleanAmount);
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", VNPAY_TMN_CODE);
            vnpParams.put("vnp_Amount", String.valueOf(amountInt * 100)); // VNPay yêu cầu số tiền nhân 100
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", orderId);
            vnpParams.put("vnp_OrderInfo", "Thanh toán vé xem phim: " + movie_title);
            vnpParams.put("vnp_OrderType", "billpayment");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", VNPAY_RETURN_URL);
            vnpParams.put("vnp_IpAddr", "192.168.1.1"); // Thay bằng IP thực tế nếu cần
            vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()));

            // Sắp xếp tham số theo thứ tự alphabet
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            ArrayList<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString())).append('&');
                    query.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString())).append('&');
                }
            }
            // Xóa ký tự '&' cuối cùng
            if (hashData.length() > 0) {
                hashData.setLength(hashData.length() - 1);
                query.setLength(query.length() - 1);
            }

            // Tạo chữ ký (Secure Hash)
            String secureHash = hmacSHA512(VNPAY_HASH_SECRET, hashData.toString());
            query.append("&vnp_SecureHash=").append(secureHash);

            // Tạo URL thanh toán
            String paymentUrl = VNPAY_URL + "?" + query.toString();


            // Tải URL vào WebView
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(paymentUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo URL thanh toán", Toast.LENGTH_SHORT).show();
        }
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacData) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void handleReturnUrl(Uri returnUrl) {
        if (returnUrl != null) {
            String vnpResponseCode = returnUrl.getQueryParameter("vnp_ResponseCode");
            String vnpTransactionNo = returnUrl.getQueryParameter("vnp_TransactionNo");
            String vnpAmount = returnUrl.getQueryParameter("vnp_Amount");
            String vnpOrderInfo = returnUrl.getQueryParameter("vnp_OrderInfo");


            if ("00".equals(vnpResponseCode)) {
                int userId = getUserId();
                //Lưu vô db [Tạo booking]
                BookingDAO bookingDAO = new BookingDAO();
                Booking booking = new Booking();
                booking.setUserID(userId); // Use the retrieved userId
                booking.setShowID(showId);
                booking.setBookingTime(new Date());
                ArrayList<String> selectedSeatNames = getIntent().getStringArrayListExtra("selected_seat_names");
                booking.setNumberOfSeats(selectedSeatNames != null ? selectedSeatNames.size() : 0);
                booking.setTotalPrice(BigDecimal.valueOf(Double.valueOf(amount)));
                booking.setStatus("Confirmed");
                booking.setQrCodeData("");
                bookingDAO.createBooking(booking, new BaseDAO.DatabaseTaskListener<Integer>() {
                    @Override
                    public void onSuccess(Integer bookingId) {
                        if (bookingId == -1) {
                            showError("Lỗi khi tạo booking");
                            return;
                        }

                        // 3. Create Booked Seats
                        // Create Booked Seats
                        ArrayList<Integer> selectedSeatIds = getIntent().getIntegerArrayListExtra("selected_seat_ids");
                        if (selectedSeatIds == null || selectedSeatIds.isEmpty()) {
                            showError("Không tìm thấy SeatID");
                            return;
                        }
                        List<BookingSeat> bookedSeats = new ArrayList<>();
                        for (Integer seatId : selectedSeatIds) {
                            BookingSeat bookedSeat = new BookingSeat();
                            bookedSeat.setBookingId(bookingId);
                            bookedSeat.setSeatId(seatId);
                            bookedSeat.setShowId(showId);
                            bookedSeat.setReserved(true);
                            bookedSeat.setReservedByUserId(userId);
                            bookedSeat.setReservationTimestamp(new Date());
                            bookedSeats.add(bookedSeat);
                        }

                        bookingDAO.createBookedSeats(bookedSeats, new BaseDAO.DatabaseTaskListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean success) {
                                if (!success) {
                                    showError("Lỗi khi tạo booked seats");
                                    return;
                                }

                                // Create Payment
                                Payment payment = new Payment();
                                payment.setBookingId(bookingId);
                                payment.setAmount(vnpAmount != null ? Integer.parseInt(vnpAmount) / 100.0 : 0.0);
                                payment.setPaymentTime(new Date());
                                payment.setPaymentStatus("Success");
                                payment.setPaymentMethod("VNPay");
                                payment.setTransactionId(vnpTransactionNo != null ? vnpTransactionNo : "N/A");
                                bookingDAO.createPayment(payment, new BaseDAO.DatabaseTaskListener<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean success) {
                                        if (!success) {
                                            showError("Lỗi khi tạo payment");
                                            return;
                                        }

                                        // chuyen huong toi  SuccessPaymentActivity
                                        Intent intent = new Intent(PaymentActivity.this, SuccessPaymentActivity.class);
                                        intent.putExtra("transaction_no", vnpTransactionNo != null ? vnpTransactionNo : "N/A");
                                        intent.putExtra("amount", vnpAmount != null ? String.valueOf(Integer.parseInt(vnpAmount) / 100) : "N/A");
                                        intent.putExtra("order_info", vnpOrderInfo != null ? vnpOrderInfo : "N/A");
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onError(Exception error) {
                                        showError("Lỗi khi tạo payment: " + error.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception error) {
                                showError("Lỗi khi tạo booked seats: " + error.getMessage());
                            }

                        });

                    }

                    @Override
                    public void onError(Exception error) {

                    }

                });
            } else {
                Log.e("Payment", "Return URL is null");

                    tvMessage.setText("Thanh toán thất bại: Không nhận được dữ liệu");
                    Toast.makeText(this, "Thanh toán thất bại!", Toast.LENGTH_SHORT).show();
                    webView.setVisibility(View.GONE);

            }
        }
    }

    private int getUserId() {
        return getSharedPreferences("UserSession", MODE_PRIVATE)
                .getInt("userId", -1); // Return -1 if userId is not found
    }

    private void showError(String message) {
        Log.e("Payment", message);
        Toast.makeText(PaymentActivity.this, message, Toast.LENGTH_SHORT).show();
        webView.setVisibility(View.GONE);
    }

    private void initViews() {

        toolbar = findViewById(R.id.toolbar);
        tvAmount = findViewById(R.id.tv_payment_amount);
        tvContent = findViewById(R.id.tv_payment_content);
        tvMessage = findViewById(R.id.tv_message);

        btnVnpayPayment = findViewById(R.id.btn_vnpay_payment);
        webView = findViewById(R.id.webview_payment);
        // Cấu hình WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(VNPAY_RETURN_URL)) {
                    handleReturnUrl(Uri.parse(url));
                    return true;
                }
                return false;
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Payment booking");
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

}



