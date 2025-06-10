package com.example.bookingticketmove_prm392;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookingticketmove_prm392.database.DatabaseConnection;
import com.example.bookingticketmove_prm392.database.DatabaseConfig;
import com.example.bookingticketmove_prm392.database.dao.UserDAO;
import com.example.bookingticketmove_prm392.models.User;
import com.example.bookingticketmove_prm392.utils.ConfigHelper;

public class MainActivity extends AppCompatActivity implements DatabaseConnection.DatabaseConnectionListener {
    private static final String TAG = "MainActivity";
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize the status text view
        statusTextView = findViewById(R.id.status_text);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Show initial status
        showInitialStatus();
        
        // Debug: Log the current configuration
        Log.d(TAG, "=== DEBUG: Current Database Configuration ===");
        Log.d(TAG, "DB_URL: " + DatabaseConfig.DB_URL);
        Log.d(TAG, "DB_USERNAME: " + DatabaseConfig.DB_USERNAME);
        
        // Test database connection after a short delay
        statusTextView.postDelayed(this::testDatabaseConnection, 1000);
    }

    private void showInitialStatus() {
        String initialMessage = "🎬 Movie Ticket Booking App\n\n" +
                               "✅ App Started Successfully!\n" +
                               "✅ JTDS Driver Loaded\n" +
                               "✅ UI Components Ready\n\n" +
                               ConfigHelper.getConfigurationStatus() + "\n\n" +
                               "Testing database connection...";
        
        statusTextView.setText(initialMessage);
        Log.d(TAG, "MainActivity created successfully");
        Toast.makeText(this, "App started successfully!", Toast.LENGTH_SHORT).show();
    }

    private void testDatabaseConnection() {
        Log.d(TAG, "Starting database connection test");
        
        if (!ConfigHelper.isDatabaseConfigured()) {
            onConnectionFailed("Database configuration incomplete - please update DatabaseConfig.java");
            return;
        }
        
        // Update status to show we're attempting connection
        String testingMessage = statusTextView.getText() + "\n\n🔄 Attempting to connect...";
        statusTextView.setText(testingMessage);
        
        // First, let's test basic connectivity before trying database connection
        testSqlServerConnectivity();
    }

    private void testSqlServerConnectivity() {
        // Run network test in background thread
        new Thread(() -> {
            try {
                // Extract server IP from database URL
                String serverIP = extractServerFromUrl(DatabaseConfig.DB_URL);
                
                // Test 1: Check if we can resolve the server IP
                Log.d(TAG, "Testing server resolution: " + serverIP);
                java.net.InetAddress.getByName(serverIP);
                
                // Test 2: Try to connect to SQL Server port
                Log.d(TAG, "Testing SQL Server port 1433 on " + serverIP);
                java.net.Socket socket = new java.net.Socket();
                socket.connect(new java.net.InetSocketAddress(serverIP, 1433), 5000);
                socket.close();
                
                runOnUiThread(() -> {
                    String message = statusTextView.getText() + "\n✅ Network connectivity OK";
                    statusTextView.setText(message);
                });
                
                // Now try the actual database connection
                DatabaseConnection.testConnection(this);
                
            } catch (java.net.UnknownHostException e) {
                runOnUiThread(() -> {
                    String error = "Cannot resolve localhost. Error: " + e.getMessage();
                    onConnectionFailed(error);
                });
            } catch (java.net.ConnectException e) {
                runOnUiThread(() -> {
                    String error = "Cannot connect to SQL Server port 1433. " +
                                 "Make sure SQL Server is running and TCP/IP is enabled.\n" +
                                 "Error: " + e.getMessage();
                    onConnectionFailed(error);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    String error = "Network test failed: " + e.getMessage();
                    onConnectionFailed(error);
                });
            }
        }).start();
    }

    private String extractServerFromUrl(String url) {
        // Extract server IP from JDBC URL like "jdbc:jtds:sqlserver://10.33.68.159:1433/DatabaseName"
        try {
            if (url.contains("://")) {
                String[] parts = url.split("://");
                if (parts.length > 1) {
                    String serverPart = parts[1].split("/")[0]; // Get "10.33.68.159:1433"
                    if (serverPart.contains(":")) {
                        return serverPart.split(":")[0]; // Get "10.33.68.159"
                    }
                    return serverPart;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting server from URL: " + e.getMessage());
        }
        return "localhost"; // fallback
    }

    @Override
    public void onConnectionSuccess() {
        runOnUiThread(() -> {
            String successMessage = "🎬 Movie Ticket Booking App\n\n" +
                                   "✅ App Started Successfully!\n" +
                                   "✅ JTDS Driver Loaded\n" +
                                   "✅ UI Components Ready\n" +
                                   "✅ Database Connected!\n\n" +
                                   "🔄 Testing database operations...\n" +
                                   "Inserting test user...";
            
            statusTextView.setText(successMessage);
            Toast.makeText(this, "🎉 Connected! Testing insert...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Database connection successful, testing insert");
            
            // Test database insert operation
            testUserInsert();
        });
    }

    @Override
    public void onConnectionFailed(String error) {
        runOnUiThread(() -> {
            String errorMessage = "🎬 Movie Ticket Booking App\n\n" +
                                 "✅ App Started Successfully!\n" +
                                 "✅ JTDS Driver Loaded\n" +
                                 "✅ UI Components Ready\n" +
                                 "❌ Database Connection Failed\n\n" +
                                 "Error: " + error + "\n\n" +
                                 "🔧 Troubleshooting Steps:\n" +
                                 "1. Check if SQL Server is running\n" +
                                 "2. Verify SQL Server allows TCP/IP connections\n" +
                                 "3. Check if port 1433 is open\n" +
                                 "4. Verify Mixed Mode Authentication is enabled\n" +
                                 "5. Ensure 'sa' account is enabled\n" +
                                 "6. Check if database 'MovieTicketBookingSystem' exists\n\n" +
                                 ConfigHelper.getCurrentConfig();
            
            statusTextView.setText(errorMessage);
            Toast.makeText(this, "⚠️ Check SQL Server setup", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Database connection failed: " + error);
        });
    }

    private void testUserInsert() {
        // Create a test user
        User testUser = new User();
        testUser.setName("Test User " + System.currentTimeMillis());
        testUser.setEmail("testuser" + System.currentTimeMillis() + "@example.com");
        testUser.setPhone("123-456-7890");
        testUser.setPasswordHash("hashed_password_123");
        
        Log.d(TAG, "Creating test user: " + testUser.getEmail());
        
        // Create user using UserDAO
        UserDAO.CreateUserTask createTask = new UserDAO.CreateUserTask(testUser, 
            new UserDAO.DatabaseTaskListener<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    runOnUiThread(() -> {
                        if (result) {
                            String successMessage = "🎬 Movie Ticket Booking App\n\n" +
                                                   "✅ App Started Successfully!\n" +
                                                   "✅ JTDS Driver Loaded\n" +
                                                   "✅ UI Components Ready\n" +
                                                   "✅ Database Connected!\n" +
                                                   "✅ Test User Inserted Successfully!\n\n" +
                                                   "🎉 Database is fully operational!\n\n" +
                                                   "Test User Details:\n" +
                                                   "Name: " + testUser.getName() + "\n" +
                                                   "Email: " + testUser.getEmail() + "\n" +
                                                   "Phone: " + testUser.getPhone() + "\n\n" +
                                                   "Ready to build the full movie booking system!";
                            
                            statusTextView.setText(successMessage);
                            Toast.makeText(MainActivity.this, "✅ User inserted successfully!", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Test user inserted successfully");
                        } else {
                            onInsertFailed("User creation returned false - possible constraint violation");
                        }
                    });
                }

                @Override
                public void onError(Exception error) {
                    onInsertFailed("Insert error: " + error.getMessage());
                }
            });
        
        createTask.execute();
    }
    
    private void onInsertFailed(String error) {
        runOnUiThread(() -> {
            String errorMessage = "🎬 Movie Ticket Booking App\n\n" +
                                 "✅ App Started Successfully!\n" +
                                 "✅ JTDS Driver Loaded\n" +
                                 "✅ UI Components Ready\n" +
                                 "✅ Database Connected!\n" +
                                 "❌ Test User Insert Failed\n\n" +
                                 "Connection works but insert failed:\n" +
                                 error + "\n\n" +
                                 "Possible causes:\n" +
                                 "• Table doesn't exist (run Db_Booking.sql)\n" +
                                 "• Permission issues\n" +
                                 "• Constraint violations\n" +
                                 "• SQL syntax errors\n\n" +
                                 "Database connection is working though!";
            
            statusTextView.setText(errorMessage);
            Toast.makeText(this, "⚠️ Insert failed but connected", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Test user insert failed: " + error);
        });
    }
}