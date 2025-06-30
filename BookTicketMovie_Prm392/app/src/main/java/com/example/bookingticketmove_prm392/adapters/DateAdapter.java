package com.example.bookingticketmove_prm392.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder>{
    private Context context;
    private List<LocalDate> dates;
    private OnDateClickListener listener;
    private int selectedPosition = 0; //Biến này để theo dõi vị trí ngày đang được chọn
    public DateAdapter(Context context, List<LocalDate> dates, OnDateClickListener listener) {
        this.context = context;
        this.dates = dates;
        this.listener = listener;
        // Khởi tạo selectedPosition về 0 (ngày đầu tiên) nếu danh sách không rỗng
        if (!dates.isEmpty()) {
            this.selectedPosition = 0;
        }
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        LocalDate date = dates.get(position);
        holder.dateText.setText(date.format(DateTimeFormatter.ofPattern("dd/MM")));
        holder.itemView.setSelected(position == selectedPosition);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Lưu lại vị trí cũ đang được chọn
                int oldSelectedPosition = selectedPosition;
                // Cập nhật vị trí mới được chọn
                selectedPosition = holder.getAdapterPosition(); // Lấy vị trí chính xác của item được click

                // Thông báo cho RecyclerView vẽ lại 2 item:
                // 1. Item cũ (để nó trở về màu mặc định)
                notifyItemChanged(oldSelectedPosition);
                // 2. Item mới (để nó đổi sang màu được chọn)
                notifyItemChanged(selectedPosition);
                listener.onDateClick(date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.date_text); // Sử dụng ID từ item_date.xml
        }
    }
    public interface OnDateClickListener {
        void onDateClick(LocalDate date);
    }
}
