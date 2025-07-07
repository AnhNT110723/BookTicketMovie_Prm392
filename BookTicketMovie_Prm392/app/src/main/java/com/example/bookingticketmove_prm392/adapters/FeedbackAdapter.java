package com.example.bookingticketmove_prm392.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.R;
import com.example.bookingticketmove_prm392.models.Feedback;


import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackHolder> {
    private Context context;
    private List<Feedback> feedbackList;
    public FeedbackAdapter(Context context, List<Feedback> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context)
               .inflate(R.layout.item_comment, parent, false);
       return new FeedbackHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        holder.bind(feedback);
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public class FeedbackHolder extends RecyclerView.ViewHolder {
        TextView txt_name;
        TextView txt_comment;
        ImageView star_1;
        ImageView star_2;
        ImageView star_3;
        ImageView star_4;
        ImageView star_5;

        public FeedbackHolder(@NonNull View itemView) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_comment = itemView.findViewById(R.id.txt_comment);
            star_1 = itemView.findViewById(R.id.star_1);
            star_2 = itemView.findViewById(R.id.star_2);
            star_3 = itemView.findViewById(R.id.star_3);
            star_4 = itemView.findViewById(R.id.star_4);
            star_5 = itemView.findViewById(R.id.star_5);
        }

        public void bind(Feedback feedback) {
            txt_name.setText(feedback.getName());
            txt_comment.setText(feedback.getCommentText());
            // Reset all stars to "off" state (ví dụ: star_border = mờ)
            star_1.setImageResource(R.drawable.star_border);
            star_2.setImageResource(R.drawable.star_border);
            star_3.setImageResource(R.drawable.star_border);
            star_4.setImageResource(R.drawable.star_border);
            star_5.setImageResource(R.drawable.star_border);

            Integer ratingValue = feedback.getRatingValue();
            // Nếu có rating, thì set lại các sao sáng tương ứng
            if (ratingValue > 0) {
                int rating = feedback.getRatingValue();
                if (rating >= 1) star_1.setImageResource(R.drawable.ic_star);
                if (rating >= 2) star_2.setImageResource(R.drawable.ic_star);
                if (rating >= 3) star_3.setImageResource(R.drawable.ic_star);
                if (rating >= 4) star_4.setImageResource(R.drawable.ic_star);
                if (rating >= 5) star_5.setImageResource(R.drawable.ic_star);
            }

        }

    }
}
