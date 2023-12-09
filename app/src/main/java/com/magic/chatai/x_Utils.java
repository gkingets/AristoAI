package com.magic.chatai;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class x_Utils {
    private final String TAG = "genki";

    public static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        return timestamp;
    }

    public static String getUid(Context context) {
        String uid;
        try {
            String firebaseUser = getFirebaseUserId();
            if (firebaseUser != null) {
                uid = firebaseUser;
            } else {
                // Firebaseユーザーがnullの場合、デバイスIDを取得
                uid = getDeviceId(context);
            }
        } catch (Exception e) {
            uid = "GUEST";
        }
        return uid;
    }

    public static boolean isLoginUser() {
        return getFirebaseUserId() != null;
    }

    private static String getFirebaseUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    private static String getDeviceId(Context context) {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return deviceId;
    }


    public static void getClipboard(Context context, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied Text", text);
        clipboardManager.setPrimaryClip(clipData);
    }

    public static String highlightText(String text) {
        final String tag_start = String.format("<font color='%s'>", "#F25C05");
        final String tag_end = "</font>";
        return text.replace("[", tag_start).replace("]", tag_end);
    }
    public static String removeHighlightText(String text) {
        return text.replace("[", "").replace("]", "");
    }

    public static void setGuestTextLink(TextView textView, String text, String url) {
        String totalText = textView.getText().toString();
        SpannableString spannableString = new SpannableString(totalText);

        int linkStartIndex = totalText.indexOf(text);
        int linkEndIndex = linkStartIndex + text.length();

        // ClickableSpanオブジェクトを作成する
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // リンクがクリックされたときの処理を記述する
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                textView.getContext().startActivity(intent);
            }
        };

        spannableString.setSpan(clickableSpan, linkStartIndex, linkEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
