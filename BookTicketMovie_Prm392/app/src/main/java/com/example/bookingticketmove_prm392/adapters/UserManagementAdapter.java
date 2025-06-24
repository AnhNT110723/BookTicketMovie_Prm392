package com.example.bookingticketmove_prm392.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.R;
import com.example.bookingticketmove_prm392.models.User;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {
    
    private Context context;
    private List<User> users;
    private OnUserActionListener listener;
    
    public interface OnUserActionListener {
        void onUserAction(User user, String action);
    }
    
    public UserManagementAdapter(Context context, List<User> users, OnUserActionListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_management, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        
        private CardView userCard;
        private ImageView avatarImageView;
        private TextView usernameTextView;
        private TextView emailTextView;
        private TextView fullNameTextView;
        private TextView joinDateTextView;
        private Chip roleChip;
        private Chip statusChip;
        private ImageView menuImageView;
        
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            
            userCard = itemView.findViewById(R.id.user_card);
            avatarImageView = itemView.findViewById(R.id.avatar_image_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            emailTextView = itemView.findViewById(R.id.email_text_view);
            fullNameTextView = itemView.findViewById(R.id.full_name_text_view);
            joinDateTextView = itemView.findViewById(R.id.join_date_text_view);
            roleChip = itemView.findViewById(R.id.role_chip);
            statusChip = itemView.findViewById(R.id.status_chip);
            menuImageView = itemView.findViewById(R.id.menu_image_view);
        }
        
        public void bind(User user) {
            // Set username
            usernameTextView.setText(user.getUsername());
            
            // Set email
            emailTextView.setText(user.getEmail());
            
            // Set full name
            String fullName = "";
            if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
                fullName = user.getFirstName();
                if (user.getLastName() != null && !user.getLastName().isEmpty()) {
                    fullName += " " + user.getLastName();
                }
            }
            if (fullName.isEmpty()) {
                fullNameTextView.setVisibility(View.GONE);
            } else {
                fullNameTextView.setVisibility(View.VISIBLE);
                fullNameTextView.setText(fullName);
            }
            
            // Set join date
            if (user.getJoinDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                joinDateTextView.setText("Joined: " + dateFormat.format(user.getJoinDate()));
            } else {
                joinDateTextView.setText("Join date: Unknown");
            }
            
            // Set role chip
            if (user.getRole() == 1) {
                roleChip.setText("Admin");
                roleChip.setChipBackgroundColorResource(R.color.accent);
                roleChip.setTextColor(context.getResources().getColor(R.color.white));
            } else {
                roleChip.setText("Customer");
                roleChip.setChipBackgroundColorResource(R.color.primary);
                roleChip.setTextColor(context.getResources().getColor(R.color.white));
            }
            
            // Set status chip
            if (user.isActive()) {
                statusChip.setText("Active");
                statusChip.setChipBackgroundColorResource(android.R.color.holo_green_light);
                statusChip.setTextColor(context.getResources().getColor(android.R.color.white));
            } else {
                statusChip.setText("Inactive");
                statusChip.setChipBackgroundColorResource(android.R.color.holo_red_light);
                statusChip.setTextColor(context.getResources().getColor(android.R.color.white));
            }
            
            // Set avatar (using a simple approach with initials or default icon)
            setUserAvatar(user);
            
            // Set click listeners
            userCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserAction(user, "view_details");
                }
            });
            
            menuImageView.setOnClickListener(v -> showUserMenu(user));
        }        private void setUserAvatar(User user) {
            // For now, use a default avatar
            // You can implement custom avatar logic here
            avatarImageView.setImageResource(user.getRole() == 1 ? android.R.drawable.ic_menu_manage : android.R.drawable.ic_menu_gallery);
        }
        
        private void showUserMenu(User user) {
            androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(context, menuImageView);
            popupMenu.inflate(R.menu.user_item_menu);
            
            // Hide certain options based on user role or status
            if (user.getRole() == 1 && getCurrentUserId() == user.getUserId()) {
                // Hide delete option for current admin user
                popupMenu.getMenu().findItem(R.id.action_delete_user).setVisible(false);
            }
            
            popupMenu.setOnMenuItemClickListener(item -> {
                if (listener == null) return false;
                
                int id = item.getItemId();
                if (id == R.id.action_edit_user) {
                    listener.onUserAction(user, "edit");
                    return true;
                } else if (id == R.id.action_delete_user) {
                    listener.onUserAction(user, "delete");
                    return true;
                } else if (id == R.id.action_toggle_active) {
                    listener.onUserAction(user, "toggle_active");
                    return true;
                } else if (id == R.id.action_change_role) {
                    listener.onUserAction(user, "change_role");
                    return true;
                } else if (id == R.id.action_reset_password) {
                    listener.onUserAction(user, "reset_password");
                    return true;
                } else if (id == R.id.action_view_details) {
                    listener.onUserAction(user, "view_details");
                    return true;
                }
                return false;
            });
            
            popupMenu.show();
        }
        
        private int getCurrentUserId() {
            // Get current user ID from SharedPreferences
            return context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    .getInt("userId", -1);
        }
    }
}
