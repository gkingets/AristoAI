package com.magic.chatai;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryTextAdapter extends RecyclerView.Adapter<HistoryTextAdapter.ViewHolder> {
    private List<String> paragraphList;
    private Context context;

    public HistoryTextAdapter(List<String> paragraphList, Context context) {
        this.paragraphList = paragraphList;
        this.context = context;
    }

    // Create the ViewHolder class to hold the views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView paragraphTextView;
        public ImageView imageCopy;

        public ViewHolder(View itemView) {
            super(itemView);
            paragraphTextView = itemView.findViewById(R.id.paragraphTextView);
            imageCopy = itemView.findViewById(R.id.history_list_copy);
        }
    }

    // Create new ViewHolders
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_list_text, parent, false);
        return new ViewHolder(view);
    }

    // Bind data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String paragraph = paragraphList.get(position);
        holder.paragraphTextView.setText(paragraph);
        holder.imageCopy.setOnClickListener(v -> {
            getClipboard(paragraph);
        });
    }

    // Return the size of the list
    @Override
    public int getItemCount() {
        return paragraphList.size();
    }

    private void getClipboard(String text) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        Func func = new Func();
        cm.setText(func.changeAnswer(text));
    }
}

