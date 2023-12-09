package com.magic.chatai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class x_TutorialAdapter extends RecyclerView.Adapter<x_TutorialAdapter.ViewHolder> {

    private List<String> dataListWrong;
    private List<String> dataListCorrect;

    public x_TutorialAdapter(List<String> dataListWrong, List<String> dataListCorrect) {
        this.dataListWrong = dataListWrong;
        this.dataListCorrect = dataListCorrect;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tutorial_layout2_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String wrongText = dataListWrong.get(position);
        String correctText = dataListCorrect.get(position);

        holder.textViewWrong.setText(wrongText);
        holder.textViewCorrect.setText(correctText);
    }

    @Override
    public int getItemCount() {
        // Ensure both lists have the same size or adjust accordingly
        return Math.min(dataListWrong.size(), dataListCorrect.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWrong;
        TextView textViewCorrect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewWrong = itemView.findViewById(R.id.tutorial_2_text_wrong);
            textViewCorrect = itemView.findViewById(R.id.tutorial_2_text_correct);
        }
    }
}

