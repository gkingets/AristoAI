package com.magic.chatai;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

public class DialogPostExecute  extends DialogFragment {
    View dialogLayout;
    Integer answerWords;
    TextView textPostNumber;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogLayout = inflater.inflate(R.layout.dialog_post_execute, null);

        builder.setView(dialogLayout)
                .setPositiveButton("", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        findView();
        answerWords = getArguments().getInt("answerWords", 0);
        textPostNumber.setText(answerWords+"");

        // Dialog を透明にする方法
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        return dialog;
    }

    public void findView() {
        textPostNumber = (TextView) dialogLayout.findViewById(R.id.post_number);
    }


}
