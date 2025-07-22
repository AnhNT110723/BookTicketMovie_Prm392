package com.example.bookingticketmove_prm392;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookingticketmove_prm392.database.dao.ContactDAO;
import com.example.bookingticketmove_prm392.models.ContactMessage;

import java.sql.SQLException;

public class ContactUsActivity extends AppCompatActivity {
    private final String TAG = "ContactUsActivity";
    private Toolbar toolbar;
    private EditText edt_email;
    private EditText edt_subject;
    private EditText edt_message;
    private Button btn_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_us);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        setupToolbar();

        edt_email = findViewById(R.id.edt_email);
        edt_subject = findViewById(R.id.edt_subject);
        edt_message = findViewById(R.id.edt_message);
        btn_send = findViewById(R.id.btn_send);

    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        // Hiển thị nút quay lại
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // icon back
            getSupportActionBar().setTitle("Contact Us"); // đặt tiêu đề
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

    public void handleAddContactMessage(View view) {
        String email = edt_email.getText().toString().trim();
        String subject = edt_subject.getText().toString().trim();
        String message = edt_message.getText().toString().trim();

        if (email.isEmpty() || subject.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        new AddContactMessageTask(email, subject, message).execute();

    }

    private class AddContactMessageTask extends AsyncTask<Void, Void, Boolean> {
        private String email, subject, message;

        public AddContactMessageTask(String email, String subject, String message) {
            this.email = email;
            this.subject = subject;
            this.message = message;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                ContactDAO contactDAO = new ContactDAO();
                boolean result = contactDAO.addContactMessage(new ContactMessage(email, subject, message));
                Log.d("ContactUsActivity", "addContactMessage: " + result);
                return result;
            } catch (SQLException e) {
                Log.e(TAG, "Error loading cinemas", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(ContactUsActivity.this, "Gửi liên hệ thành công", Toast.LENGTH_SHORT).show();
                edt_email.setText("");
                edt_subject.setText("");
                edt_message.setText("");
            } else {
                Toast.makeText(ContactUsActivity.this, "Gửi liên hệ thất bại", Toast.LENGTH_SHORT).show();
            }
        }

    }
}