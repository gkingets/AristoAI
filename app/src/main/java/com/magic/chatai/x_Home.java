package com.magic.chatai;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class x_Home extends Fragment implements x_DialogStyle.DialogClosedListener {
    private final String TAG = "genki";
    private RewardedAd mRewardedAd;
//    private final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"; // TEST
    private final String AD_UNIT_ID = "ca-app-pub-9264044524321014/9200642522";
    private final int POINT_ZERO = 0;

    View view;

    @Override
    public void onDialogClosed(String selectedStyle) {
        setStyleButton();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.x_home, container, false);

        BottomNavigationView navigationView = getActivity().findViewById(R.id.bottom_topic);
        navigationView.getMenu().getItem(0).setChecked(true);

        x_UtilsKeyboard.setHideSoftKeyboard(view, getActivity());
        view.findViewById(R.id.x_home_answer_frame).setVisibility(View.VISIBLE);

        createUser();
        setInitialView();
        startValidation();
        setQuestionClear();
        setStyleDialog();
        setStyleButton();
        setClipboardButton();
        return view;
    }


    private void createUser() {
        x_UtilsSharedPref utilsSharedPref = new x_UtilsSharedPref(getActivity());
//        utilsSharedPref.putBooleanCreateUser(false);
        Boolean isCreateUserDone = utilsSharedPref.getBooleanCreateUser();
        if (isCreateUserDone) {
            return;
        }

        x_UtilsFirestore utilsFirestore = new x_UtilsFirestore();
        Task<Void> addDataTask = utilsFirestore.createUser(x_Utils.getUid(getContext()));

        addDataTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "データが正常に追加されました。");
                utilsSharedPref.putBooleanCreateUser(true);
            } else {
                Log.e(TAG, "データの追加中にエラーが発生しました。", task.getException());
            }
        });
    }

    private void setInitialView() {
        TextView textViewPoint = view.findViewById(R.id.x_home_point);
        ImageView imageViewPremium = view.findViewById(R.id.x_home_point_infinite);
        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(getActivity());
        if (sharedPref.getBooleanPremium()) {
            textViewPoint.setVisibility(View.GONE);
            imageViewPremium.setVisibility(View.VISIBLE);
        } else {
            textViewPoint.setVisibility(View.VISIBLE);
            imageViewPremium.setVisibility(View.GONE);
            x_UtilsFirestore.setPointFromFirestore(view.getContext(), textViewPoint);
        }
    }


    private String craftQuestion(String input) {
        final String TEMPLATE =
                "You are a native English speaker. " +
                        "Correct the following English sentences so that it makes sense as a %s sentence, " +
                        "and put [] before and after the modified part." +
                        "No your comments. Sentences: %s";

        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(getActivity());
        String writingStyle = sharedPref.getWritingStyle();

        String question = String.format(TEMPLATE, writingStyle, input);
        Log.d(TAG, "craftQuestion: " + question);
        return question;
    }

    private  void startValidation() {
        ImageButton button = view.findViewById(R.id.x_home_button_generate);
        button.setOnClickListener(v -> {
            TextView textViewQuestion = view.findViewById(R.id.x_home_question);
            TextView textViewAnswer = view.findViewById(R.id.x_home_answer);
            textViewAnswer.setText("");
            String rawQuestion = textViewQuestion.getText().toString();

            if (rawQuestion.isEmpty()) {
                textViewQuestion.setError("Question cannot be empty");
                textViewQuestion.requestFocus();
                return;
            }

            TextView textView = view.findViewById(R.id.x_home_point);
            int point = Integer.parseInt(textView.getText().toString());
            if (point <= POINT_ZERO) {
                dialogAdAlertMake();
                return;
            }

            startGenerate();
        });
    }
    private void startGenerate() {
        TextView textViewQuestion = view.findViewById(R.id.x_home_question);
        String rawQuestion = textViewQuestion.getText().toString();
        String question = craftQuestion(rawQuestion);
        chatGPTAsync(question);
    }

    private void chatGPTAsync(String question) {

        ProgressBar progressBar = view.findViewById(R.id.x_home_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        x_ChatGPT chatGPTInstance = new x_ChatGPT();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String answer = chatGPTInstance.chatGPT(question);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showResultWithFadeIn(answer);
                        addFirestore(answer);
                        progressBar.setVisibility(View.INVISIBLE);
                        updatePoint();

                    }
                });
            }
        });
        thread.start();
    }

    private void showResultWithFadeIn(String answer) {
        Log.d(TAG, "showResult: " + answer);
        TextView textViewAnswer = view.findViewById(R.id.x_home_answer);

        String highlightedText = x_Utils.highlightText(answer);

        // アルファ値を変化させるアニメーションを作成
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(3000); // フェードインの時間を設定（ミリ秒単位）

        // アニメーションが終了したらテキストを設定
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                TextView textViewFrame = view.findViewById(R.id.x_home_answer_frame);
                textViewFrame.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // アニメーション終了時にテキストを設定
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // アニメーションのリピート時の処理（ここでは何もしない）
            }
        });
        textViewAnswer.setText(Html.fromHtml(highlightedText));

        // アニメーションをテキストビューに適用
        textViewAnswer.startAnimation(fadeIn);
    }

    private void setQuestionClear() {
        TextView textView = view.findViewById(R.id.x_home_question);
        ImageView imageView = view.findViewById(R.id.x_home_button_clear);
        imageView.setOnClickListener(v -> {
            textView.setText("");
        });

    }

    private void setStyleDialog() {
        TextView textView = view.findViewById(R.id.x_home_style);
        textView.setOnClickListener(v -> {
            x_DialogStyle dialog = new x_DialogStyle();
            dialog.setDialogClosedListener(this); // リスナーを設定
            dialog.show(getActivity().getSupportFragmentManager(), "my_dialog");
        });
    }


    public void setStyleButton() {
        TextView textView = view.findViewById(R.id.x_home_style);
        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(getActivity());
        String selectedStyle = sharedPref.getWritingStyle();
        if (selectedStyle.equals("")) {
            sharedPref.putWritingStyle("standard");
        }
        switch (selectedStyle) {
            case "business":
                textView.setText("Business");
                break;
            case "academic":
                textView.setText("Academic");
                break;
            case "casual":
                textView.setText("Casual");
                break;
            default:
                textView.setText("Standard");
                break;
        }
    }

    private void addFirestore(String answer) {
        TextView textViewQuestion = view.findViewById(R.id.x_home_question);
        String question = textViewQuestion.getText().toString();
        x_UtilsFirestore utilsFirestore = new x_UtilsFirestore();
        String documentId = x_Utils.getUid(getContext()); // ドキュメントID
        String timestamp = x_Utils.getCurrentTimestamp();
        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(getActivity());
        String style = sharedPref.getWritingStyle();

        Map<String, Object> data = new HashMap<>();
        data.put("question", question);
        data.put("answer", answer);
        data.put("timeStamp", timestamp);
        data.put("style", style);

        // Firestoreにデータを追加
        Task<Void> addDataTask = utilsFirestore.addMapToDocumentArray(documentId, data);

        addDataTask.addOnSuccessListener(aVoid -> {
            Log.d(TAG, "データが正常に追加されました。");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "データの追加中にエラーが発生しました。", e);
        });
    }

    private void updatePoint() {
        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(getActivity());
        if (sharedPref.getBooleanPremium()) {
            return;
        }

        final int CREATE_POINT_DECREASE = -1;
        TextView textView = view.findViewById(R.id.x_home_point);
        int currentPoint = Integer.parseInt(textView.getText().toString());
        x_UtilsFirestore utilsFirestore = new x_UtilsFirestore();
        utilsFirestore.updatePoint(x_Utils.getUid(getContext()), currentPoint, CREATE_POINT_DECREASE);

        textView.setText(""+(currentPoint + CREATE_POINT_DECREASE));
    }

    private void setClipboardButton() {
        ImageView imageView = view.findViewById(R.id.x_home_button_clipboard);
        TextView textView = view.findViewById(R.id.x_home_answer);
        String textToCopy = textView.getText().toString();

        imageView.setOnClickListener(v -> {
            x_Utils.getClipboard(view.getContext(), textToCopy);
        });

    }

    private void dialogAdAlertMake() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.show_ads);
        builder.setMessage("Sign in to get 1,000 stars and run without ads!");
        builder.setPositiveButton(R.string.YES, (dialog, id) -> showRewardedAd());
        builder.setNegativeButton(R.string.CANCEL, (dialog, id) -> loadRewardedAd());
        builder.create();
        builder.show();
    }

    public void showRewardedAd() {
        if (mRewardedAd != null) {
            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    // Called when a click is recorded for an ad.
                    Log.d("genki", "Ad was clicked.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    Log.d("genki", "Ad dismissed fullscreen content.");
                    mRewardedAd = null;
                    loadRewardedAd();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when ad fails to show.
                    Log.e("genki", "Ad failed to show fullscreen content.");
                    mRewardedAd = null;
                }

                @Override
                public void onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Log.d("genki", "Ad recorded an impression.");

                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.d("genki", "Ad showed fullscreen content.");
                }
            });
            Activity activityContext = getActivity();
            mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d("genki", "The user earned the reward.");
                    startGenerate();
                }
            });
        } else {
            Log.d("genki", "The rewarded ad wasn't ready yet.");
            loadRewardedAd();
        }
    }

    public void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(getContext(), AD_UNIT_ID,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d(TAG, "loadAdError.toString()" + loadAdError.toString());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d("genki", "Ad was loaded.");
                    }
                });
    }
}
