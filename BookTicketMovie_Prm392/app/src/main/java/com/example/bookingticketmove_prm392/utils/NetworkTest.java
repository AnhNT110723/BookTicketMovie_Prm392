package com.example.bookingticketmove_prm392.utils;

import android.util.Log;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class NetworkTest {
    private static final String TAG = "NetworkTest";
    
    public interface NetworkTestListener {
        void onTestResult(String host, int port, boolean success, String message);
    }
    
    public static void testConnection(String host, int port, NetworkTestListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());
        
        executor.execute(() -> {
            boolean success = false;
            String message = "";
            
            try {
                Log.d(TAG, "Testing connection to " + host + ":" + port);
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), 5000); // 5 second timeout
                socket.close();
                success = true;
                message = "Connection successful";
                Log.d(TAG, "✅ Connection to " + host + ":" + port + " successful");
            } catch (IOException e) {
                message = "Connection failed: " + e.getMessage();
                Log.e(TAG, "❌ Connection to " + host + ":" + port + " failed: " + e.getMessage());
            }
            
            final boolean finalSuccess = success;
            final String finalMessage = message;
            
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onTestResult(host, port, finalSuccess, finalMessage);
                }
            });
        });
    }
    
    public static void testMultipleHosts(NetworkTestListener listener) {
        // Test multiple possible SQL Server locations
        String[] hosts = {
            "10.33.68.159",  // Your actual IP
            "10.0.2.2",      // Android emulator host mapping
            "127.0.0.1",     // Localhost
            "localhost"      // Hostname localhost
        };
        
        for (String host : hosts) {
            testConnection(host, 1433, listener);
        }
    }
}
