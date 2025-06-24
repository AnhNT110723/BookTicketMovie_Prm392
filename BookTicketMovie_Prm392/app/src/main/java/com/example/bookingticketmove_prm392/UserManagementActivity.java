package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookingticketmove_prm392.adapters.UserManagementAdapter;
import com.example.bookingticketmove_prm392.database.dao.UserDAO;
import com.example.bookingticketmove_prm392.models.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserManagementActivity extends AppCompatActivity {
    private static final String TAG = "UserManagementActivity";
    
    // UI Components
    private Toolbar toolbar;
    private EditText searchEditText;
    private ChipGroup filterChipGroup;
    private Chip allUsersChip, adminsChip, customersChip, activeUsersChip, inactiveUsersChip;
    private CardView totalUsersCard, adminUsersCard, customerUsersCard, activeUsersCard;
    private TextView totalUsersCount, adminUsersCount, customerUsersCount, activeUsersCount;
    private RecyclerView usersRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddUser;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;
    
    // Data
    private UserManagementAdapter userAdapter;
    private List<User> allUsers;
    private List<User> filteredUsers;
    private SharedPreferences sharedPreferences;
    private String currentFilter = "all";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        
        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        
        // Check admin role
        if (!isUserAdmin()) {
            redirectToLogin();
            return;
        }
        
        // Initialize views and setup
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        setupFilters();
        setupClickListeners();
        
        // Load users
        loadUsers();
    }

    private boolean isUserAdmin() {
        int userRole = sharedPreferences.getInt("userRole", 2);
        return userRole == 1; // Admin role
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchEditText = findViewById(R.id.search_edit_text);
        filterChipGroup = findViewById(R.id.filter_chip_group);
        allUsersChip = findViewById(R.id.chip_all_users);
        adminsChip = findViewById(R.id.chip_admins);
        customersChip = findViewById(R.id.chip_customers);
        activeUsersChip = findViewById(R.id.chip_active_users);
        inactiveUsersChip = findViewById(R.id.chip_inactive_users);
        
        totalUsersCard = findViewById(R.id.total_users_card);
        adminUsersCard = findViewById(R.id.admin_users_card);
        customerUsersCard = findViewById(R.id.customer_users_card);
        activeUsersCard = findViewById(R.id.active_users_card);
        
        totalUsersCount = findViewById(R.id.total_users_count);
        adminUsersCount = findViewById(R.id.admin_users_count);
        customerUsersCount = findViewById(R.id.customer_users_count);
        activeUsersCount = findViewById(R.id.active_users_count);
        
        usersRecyclerView = findViewById(R.id.users_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        fabAddUser = findViewById(R.id.fab_add_user);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        emptyStateText = findViewById(R.id.empty_state_text);
        
        // Initialize data lists
        allUsers = new ArrayList<>();
        filteredUsers = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("👥 User Management");
        }
    }

    private void setupRecyclerView() {
        userAdapter = new UserManagementAdapter(this, filteredUsers, this::onUserAction);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadUsers);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        allUsersChip.setChecked(true);
        
        allUsersChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                currentFilter = "all";
                clearOtherChips(allUsersChip);
                applyFilters();
            }
        });
        
        adminsChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                currentFilter = "admins";
                clearOtherChips(adminsChip);
                applyFilters();
            }
        });
        
        customersChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                currentFilter = "customers";
                clearOtherChips(customersChip);
                applyFilters();
            }
        });
        
        activeUsersChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                currentFilter = "active";
                clearOtherChips(activeUsersChip);
                applyFilters();
            }
        });
        
        inactiveUsersChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                currentFilter = "inactive";
                clearOtherChips(inactiveUsersChip);
                applyFilters();
            }
        });
    }

    private void clearOtherChips(Chip selectedChip) {
        if (selectedChip != allUsersChip) allUsersChip.setChecked(false);
        if (selectedChip != adminsChip) adminsChip.setChecked(false);
        if (selectedChip != customersChip) customersChip.setChecked(false);
        if (selectedChip != activeUsersChip) activeUsersChip.setChecked(false);
        if (selectedChip != inactiveUsersChip) inactiveUsersChip.setChecked(false);
    }

    private void setupClickListeners() {
        fabAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditUserActivity.class);
            startActivityForResult(intent, 1001);
        });
        
        totalUsersCard.setOnClickListener(v -> {
            allUsersChip.setChecked(true);
            Toast.makeText(this, "Showing all users", Toast.LENGTH_SHORT).show();
        });
        
        adminUsersCard.setOnClickListener(v -> {
            adminsChip.setChecked(true);
            Toast.makeText(this, "Showing admin users", Toast.LENGTH_SHORT).show();
        });
        
        customerUsersCard.setOnClickListener(v -> {
            customersChip.setChecked(true);
            Toast.makeText(this, "Showing customer users", Toast.LENGTH_SHORT).show();
        });
        
        activeUsersCard.setOnClickListener(v -> {
            activeUsersChip.setChecked(true);
            Toast.makeText(this, "Showing active users", Toast.LENGTH_SHORT).show();
        });
    }

    private void applyFilters() {
        filteredUsers.clear();
        
        List<User> tempList = new ArrayList<>(allUsers);
        
        // Apply search filter
        if (!currentSearchQuery.isEmpty()) {
            tempList = tempList.stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                                  user.getEmail().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                                  (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(currentSearchQuery.toLowerCase())) ||
                                  (user.getLastName() != null && user.getLastName().toLowerCase().contains(currentSearchQuery.toLowerCase())))
                    .collect(Collectors.toList());
        }
        
        // Apply role/status filter
        switch (currentFilter) {
            case "admins":
                tempList = tempList.stream()
                        .filter(user -> user.getRole() == 1)
                        .collect(Collectors.toList());
                break;
            case "customers":
                tempList = tempList.stream()
                        .filter(user -> user.getRole() == 2)
                        .collect(Collectors.toList());
                break;
            case "active":
                tempList = tempList.stream()
                        .filter(User::isActive)
                        .collect(Collectors.toList());
                break;
            case "inactive":
                tempList = tempList.stream()
                        .filter(user -> !user.isActive())
                        .collect(Collectors.toList());
                break;
            case "all":
            default:
                // No additional filtering needed
                break;
        }
        
        filteredUsers.addAll(tempList);
        userAdapter.notifyDataSetChanged();
        
        // Show/hide empty state
        if (filteredUsers.isEmpty()) {
            usersRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            if (!currentSearchQuery.isEmpty()) {
                emptyStateText.setText("No users found matching \"" + currentSearchQuery + "\"");
            } else {
                emptyStateText.setText("No users found for the selected filter");
            }
        } else {
            usersRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void loadUsers() {
        swipeRefreshLayout.setRefreshing(true);
        new LoadUsersTask().execute();
    }

    private void updateStatistics() {
        int totalUsers = allUsers.size();
        int adminUsers = (int) allUsers.stream().filter(user -> user.getRole() == 1).count();
        int customerUsers = (int) allUsers.stream().filter(user -> user.getRole() == 2).count();
        int activeUsers = (int) allUsers.stream().filter(User::isActive).count();
        
        totalUsersCount.setText(String.valueOf(totalUsers));
        adminUsersCount.setText(String.valueOf(adminUsers));
        customerUsersCount.setText(String.valueOf(customerUsers));
        activeUsersCount.setText(String.valueOf(activeUsers));
    }

    private void onUserAction(User user, String action) {
        switch (action) {
            case "edit":
                editUser(user);
                break;
            case "delete":
                deleteUser(user);
                break;
            case "toggle_active":
                toggleUserActive(user);
                break;
            case "change_role":
                changeUserRole(user);
                break;
            case "reset_password":
                resetUserPassword(user);
                break;
            case "view_details":
                viewUserDetails(user);
                break;
        }
    }

    private void editUser(User user) {
        Intent intent = new Intent(this, AddEditUserActivity.class);
        intent.putExtra("user_id", user.getUserId());
        startActivityForResult(intent, 1002);
    }

    private void deleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user \"" + user.getUsername() + "\"?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new DeleteUserTask(user).execute();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleUserActive(User user) {
        String action = user.isActive() ? "deactivate" : "activate";
        new AlertDialog.Builder(this)
                .setTitle(action.substring(0, 1).toUpperCase() + action.substring(1) + " User")
                .setMessage("Are you sure you want to " + action + " user \"" + user.getUsername() + "\"?")
                .setPositiveButton(action.substring(0, 1).toUpperCase() + action.substring(1), (dialog, which) -> {
                    user.setActive(!user.isActive());
                    new UpdateUserTask(user, "toggle_active").execute();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void changeUserRole(User user) {
        String[] roles = {"Admin", "Customer"};
        int currentRoleIndex = user.getRole() == 1 ? 0 : 1;
        
        new AlertDialog.Builder(this)
                .setTitle("Change User Role")
                .setSingleChoiceItems(roles, currentRoleIndex, (dialog, which) -> {
                    int newRole = which == 0 ? 1 : 2; // 1 = Admin, 2 = Customer
                    if (newRole != user.getRole()) {
                        user.setRole(newRole);
                        new UpdateUserTask(user, "change_role").execute();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetUserPassword(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Reset password for user \"" + user.getUsername() + "\"?\n\nA temporary password will be generated.")
                .setPositiveButton("Reset", (dialog, which) -> {
                    new ResetPasswordTask(user).execute();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void viewUserDetails(User user) {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra("user_id", user.getUserId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_management_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_refresh) {
            loadUsers();
            return true;
        } else if (id == R.id.action_export) {
            exportUsers();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void exportUsers() {
        // TODO: Implement user export functionality
        Toast.makeText(this, "Export functionality coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == 1001) {
                Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show();
                loadUsers();
            } else if (requestCode == 1002) {
                Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
                loadUsers();
            }
        }
    }

    // AsyncTask classes
    private class LoadUsersTask extends AsyncTask<Void, Void, List<User>> {
        @Override
        protected List<User> doInBackground(Void... voids) {
            try {
                UserDAO userDAO = new UserDAO();
                return userDAO.getAllUsers();
            } catch (Exception e) {
                Log.e(TAG, "Error loading users", e);
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<User> users) {
            swipeRefreshLayout.setRefreshing(false);
            allUsers.clear();
            allUsers.addAll(users);
            updateStatistics();
            applyFilters();
        }
    }

    private class DeleteUserTask extends AsyncTask<Void, Void, Boolean> {
        private User user;

        public DeleteUserTask(User user) {
            this.user = user;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                UserDAO userDAO = new UserDAO();
                return userDAO.deleteUser(user.getUserId());
            } catch (Exception e) {
                Log.e(TAG, "Error deleting user", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                allUsers.remove(user);
                updateStatistics();
                applyFilters();
                Toast.makeText(UserManagementActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UserManagementActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateUserTask extends AsyncTask<Void, Void, Boolean> {
        private User user;
        private String action;

        public UpdateUserTask(User user, String action) {
            this.user = user;
            this.action = action;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                UserDAO userDAO = new UserDAO();
                return userDAO.updateUser(user);
            } catch (Exception e) {
                Log.e(TAG, "Error updating user", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                updateStatistics();
                applyFilters();
                String message = action.equals("toggle_active") 
                    ? "User status updated successfully" 
                    : "User role updated successfully";
                Toast.makeText(UserManagementActivity.this, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UserManagementActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ResetPasswordTask extends AsyncTask<Void, Void, String> {
        private User user;

        public ResetPasswordTask(User user) {
            this.user = user;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                UserDAO userDAO = new UserDAO();
                // Generate temporary password
                String tempPassword = "Temp" + System.currentTimeMillis();
                if (userDAO.resetUserPassword(user.getUserId(), tempPassword)) {
                    return tempPassword;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error resetting password", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String tempPassword) {
            if (tempPassword != null) {
                new AlertDialog.Builder(UserManagementActivity.this)
                        .setTitle("Password Reset")
                        .setMessage("Password reset successfully!\n\nTemporary password: " + tempPassword + "\n\nPlease share this with the user securely.")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                Toast.makeText(UserManagementActivity.this, "Failed to reset password", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
