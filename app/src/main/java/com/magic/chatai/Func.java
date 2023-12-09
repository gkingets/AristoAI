package com.magic.chatai;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Func {

    public String getUid(String uid) {
        // Get UID
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            uid = user.getUid();
        } catch (Exception e) {
            uid = "GUEST";
        }
        return uid;
    }

    public String getUidSendLogin(String uid, Context context) {
        // Get UID
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            uid = user.getUid();
        } catch (Exception e) {
            uid = "GUEST";
            new Intent(context, MyPageLogin.class);
        }
        return uid;
    }

    public void setFadeCompletion(LinearLayout linearLayout, TextView textView,
                                  Integer answerWords) {
        linearLayout.setVisibility(View.VISIBLE);
        textView.setText("-"+String.format("%,d",answerWords));

        // フェードアウトするアニメーションを作成する
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(2000); // アニメーションの持続時間を設定する（1秒）

        // アニメーションが終了したら、ImageView を非表示にする
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                linearLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        // ImageView にアニメーションを適用する
        linearLayout.startAnimation(fadeOut);
    }

    public void setFadeFailure(LinearLayout linearLayout, Integer sec) {
        linearLayout.setVisibility(View.VISIBLE);

        // フェードアウトするアニメーションを作成する
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(sec*1000); // アニメーションの持続時間を設定する（1秒）

        // アニメーションが終了したら、ImageView を非表示にする
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                linearLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        // ImageView にアニメーションを適用する
        linearLayout.startAnimation(fadeOut);
    }

    public void setFadeTutorialImage(ImageView image, int sec) {
        // AlphaAnimationのインスタンスを作成
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(sec*1000); // アニメーションの時間を設定

        // アニメーションのリスナーを設定
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // アニメーション開始時の処理を記述
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // アニメーション終了時の処理は空にする
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // アニメーションリピート時の処理を記述
            }
        });

//        image.setAlpha(0.0f); // CardViewを透明にする
        image.startAnimation(alphaAnimation); // アニメーションを開始
//        image.setAlpha(1.0f); // アニメーション終了後にCardViewを表示する
    }

    public String changeAnswer(String text) {
        for (int i = 1; i <= 15; i++) {
            String item = i + ". ";
            text = text.replace(item,"");
        }

        text = text.replace("[Correct] ","");
        text = text.replace("[Alternative] ","");
        text = text.replace("[Feedback] ","");
        text = text.replace("[Correct]: ","");
        text = text.replace("[Alternative]: ","");
        text = text.replace("[Feedback]: ","");

        text = text.replace("\"","");

        return text;
    }



}
