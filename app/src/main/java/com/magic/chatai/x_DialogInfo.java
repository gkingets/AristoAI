package com.magic.chatai;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class x_DialogInfo extends DialogFragment {
    View dialogLayout;
    TextView textTermsOfServiceEnglish, textTermsOfServiceJapanese,textPrivacyPolicyEnglish, textPrivacyPolicyJapanese;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogLayout = inflater.inflate(R.layout.x_dialog_info, null);

        builder.setView(dialogLayout)
                .setPositiveButton("", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        setLink();

        return builder.create(); // returnを後で入れる
    }

    private void setLink() {
        textTermsOfServiceEnglish = dialogLayout.findViewById(R.id.info_text_terms_of_use_english);
        textTermsOfServiceJapanese = dialogLayout.findViewById(R.id.info_text_terms_of_use_japanese);
        textPrivacyPolicyEnglish = dialogLayout.findViewById(R.id.info_text_privacy_policy_english);
        textPrivacyPolicyJapanese = dialogLayout.findViewById(R.id.info_text_privacy_policy_japanese);

        setUrlToTextView("English", textTermsOfServiceEnglish, "https://docs.google.com/document/d/e/2PACX-1vTNIqsyh06m6pqXk8aB7lBMDTMB8tmbQdFK9aRUJMHff0HwAr1m62C29cNZUl2zoZHWNV0tCxmYJCFN/pub");
        setUrlToTextView("Japanese", textTermsOfServiceJapanese, "https://docs.google.com/document/d/e/2PACX-1vQxvz6kTCw62d6NTM5LawXl6bBO9PjUkAz6P3wH8FCdr3cuVPVz6H-fzLwxIUKEGe5S9-iGwSMxgtwY/pub");
        setUrlToTextView("English", textPrivacyPolicyEnglish, "https://docs.google.com/document/d/e/2PACX-1vR5V3uTXEIlfEC06nNRjt1aGO__wjDaysGjct03He_RD7XgYN6QuLvAWEKiZTQ_3TClsAqRzzhhGQoF/pub");
        setUrlToTextView("Japanese", textPrivacyPolicyJapanese, "https://docs.google.com/document/d/e/2PACX-1vRItA4HmWpQIS4aWOk6FvB-D9z8jkqLhNYL7PSRNpisI50owNq_BId1ChfI5D7yAkHXnjy5AjoRUrHM/pub");

    }

    private void setUrlToTextView(String text, TextView textView, String url) {
        String linkText = text;
        String linkUrl = url;
        SpannableString spannableString;
        spannableString = new SpannableString(linkText);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                // リンクがクリックされた時の処理をここに追加する
                Uri uri = Uri.parse(linkUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        };
        spannableString.setSpan(clickableSpan, 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }




}
