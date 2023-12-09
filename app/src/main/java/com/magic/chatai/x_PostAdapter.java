package com.magic.chatai;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class x_PostAdapter extends RecyclerView.Adapter<x_PostAdapter.PostViewHolder> {
    private final String TAG = "genki";
    private List<x_Post> posts;

    public x_PostAdapter(List<x_Post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.x_history_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        x_Post post = posts.get(position);
        holder.bind(post);
        holder.setArrowFunction();
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        private final String TAG = "genki";
        private TextView textViewQuestion, textViewAnswer, textViewTimestamp, textViewStyle, textViewCopy;
        private ImageView imageViewArrow;
        private LinearLayout linearLayoutAnswer;
        private CardView cardView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewQuestion = itemView.findViewById(R.id.x_history_item_question);
            textViewAnswer = itemView.findViewById(R.id.x_history_item_answer);
            textViewTimestamp = itemView.findViewById(R.id.x_history_item_timestamp);
            textViewStyle = itemView.findViewById(R.id.x_history_item_style);
            textViewCopy = itemView.findViewById(R.id.x_history_item_answer_copy);
            imageViewArrow = itemView.findViewById(R.id.x_history_item_arrow);
            linearLayoutAnswer = itemView.findViewById(R.id.x_history_item_answer_layout);
            cardView = itemView.findViewById(R.id.x_history_item_card_view);
        }

        public void bind(x_Post post) {
            textViewQuestion.setText(post.getQuestion());
            textViewAnswer.setText(Html.fromHtml(x_Utils.highlightText(post.getAnswer())));
            textViewTimestamp.setText(post.getTimeStamp());
            textViewStyle.setText("["+post.getStyle()+"]");
        }

        public void setArrowFunction() {
            linearLayoutAnswer.setVisibility(View.GONE);
            textViewCopy.setVisibility(View.GONE);
            cardView.setOnClickListener(v -> {
                if (linearLayoutAnswer.getVisibility() == View.VISIBLE) {
                    linearLayoutAnswer.setVisibility(View.GONE);
                    textViewCopy.setVisibility(View.GONE);
                    imageViewArrow.setImageResource(R.drawable.ic_arrow_left);
                } else if (linearLayoutAnswer.getVisibility() == View.GONE) {
                    linearLayoutAnswer.setVisibility(View.VISIBLE);
                    textViewCopy.setVisibility(View.VISIBLE);
                    imageViewArrow.setImageResource(R.drawable.ic_arrow_down);
                    textViewCopy.setOnClickListener(v1 -> {
                        x_Utils.getClipboard(itemView.getContext(), x_Utils.removeHighlightText(textViewAnswer.getText().toString()));
                    });
                }
            });
        }
    }
}