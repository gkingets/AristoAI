package com.magic.chatai;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>{

    private final java.util.List<String> iDocId;
    private final java.util.List<String> iAnswer;
    private final java.util.List<String> iQuestion;
    private final java.util.List<String> iAnswerWords;
    private final java.util.List<String> iTimeStamp;
    List<String> iFlagHeader = new ArrayList<>();
    private Context context;


    public class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        TextView textQuestion, textAnswer, textAnswerWords, textCopy, textTimeStamp;
        LinearLayout layoutExpand, layoutAnswer;
        ImageView imageArrow, textFlagHeader;
        RecyclerView recyclerView;
        String flagHeader, docId;
        CardView cardFlagHeader;

        ViewHolder(View v) {
            super(v);
            textQuestion = v.findViewById(R.id.history_list_question);
            textAnswer = v.findViewById(R.id.history_list_answer);
            textAnswerWords = v.findViewById(R.id.history_list_point);
            layoutExpand = v.findViewById(R.id.history_list_expand);
            imageArrow = v.findViewById(R.id.history_list_arrow);
            textCopy = v.findViewById(R.id.history_list_copy);
            layoutAnswer = v.findViewById(R.id.history_list_answer_layout);
            layoutAnswer.setVisibility(View.GONE);
            textTimeStamp = v.findViewById(R.id.history_list_date);
            recyclerView = v.findViewById(R.id.history_list_recycler_view);
            textFlagHeader = v.findViewById(R.id.history_list_flag_header);
            cardFlagHeader = v.findViewById(R.id.history_list_flag_header_card);
            textCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", textAnswer.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(v.getContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    HistoryAdapter(List<String> iDocId, List<String> iQuestion, List<String> iAnswer, List<String> iAnswerWords,
                   List<String> iTimeStamp, List<String> iFlagHeader) {
        this.iDocId = iDocId;
        this.iQuestion = iQuestion;
        this.iAnswer = iAnswer;
        this.iAnswerWords = iAnswerWords;
        this.iTimeStamp = iTimeStamp;
        this.iFlagHeader = iFlagHeader;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.history_list, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.textQuestion.setText(iQuestion.get(position));
        holder.textAnswer.setText(iAnswer.get(position));
        holder.textAnswerWords.setText(iAnswerWords.get(position));
        holder.textTimeStamp.setText(changeDateFormat(iTimeStamp.get(position)));

        String[] paragraphs = iAnswer.get(position).replace("\n\n","\n").split("\n"); // Split the string based on double newline characters
        List<String> paragraphList = Arrays.asList(paragraphs);
        Log.d("genki", "paragraphList"+paragraphList);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        RecyclerView.Adapter adapter = new HistoryTextAdapter(paragraphList, context);
        holder.recyclerView.setAdapter(adapter);
        Log.d("genki", "iFlagHeader |" + iFlagHeader.get(position));
        if (iFlagHeader.get(position).equals("rewrite")) {
            holder.textFlagHeader.setImageResource(R.drawable.ic_change);
        } else if (iFlagHeader.get(position).equals("rephrase")) {
            holder.textFlagHeader.setImageResource(R.drawable.ic_pen);
        }

        holder.layoutExpand.setOnClickListener(v -> {
            if (holder.layoutAnswer.getVisibility() == View.VISIBLE) {
                holder.layoutAnswer.setVisibility(View.GONE);
                holder.imageArrow.setImageResource(R.drawable.ic_arrow_left);
            } else if (holder.layoutAnswer.getVisibility() == View.GONE) {
                holder.layoutAnswer.setVisibility(View.VISIBLE);
                holder.imageArrow.setImageResource(R.drawable.ic_arrow_down);
            }
        });
    }


    @Override
    public int getItemCount() {
        return iQuestion.size();
    }

    private String changeDateFormat(String timeStamp) {
        String yyyy = timeStamp.substring(0,4);
        String mm = timeStamp.substring(4,6);
        String dd = timeStamp.substring(6,8);
        String yyyymmdd = yyyy+"-"+mm+"-"+dd;
        return yyyymmdd;
    }

}
