package com.magic.chatai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.common.SignInButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robinhood.ticker.TickerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements x_Premium.SubscriptionPurchaseCallback {
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_TIME_LAUNCH = "first_time_launch";
    private static final String PREF_LAYOUT_CAMPAIGN = "pref_layout_campaign";
    private final String TAG = "genki";
    RewardedAd mRewardedAd;
    //    String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"; // TEST
    String AD_UNIT_ID = "ca-app-pub-9264044524321014/9200642522";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    //    private MyTask asyncProgress;
    String uid, docId, question, questionRaw, flagCorrectStyle = "", flagHeader = "rewrite", lastFailure = "";
    Integer point = 0, randomInt;
    Integer animation = 1, secondsLeft = 30;
    TextView textCheck, textAnswerPoint, textFillQuestion, textCopy, textCountDownTimer;
    CountDownTimer countDownTimer;
    TickerView textPoint;
    EditText textQuestion;
    Button btnStart;
    ImageView imageMyPage, imageInfo, imageExample, imageStarPlus, imageClear, imageReset, imageStarIcon, imageCampaignClear;
    LinearLayout layoutCompletion, layoutFailure, layoutRephrase, layoutCampaign;
    TypeWriterView textAnswer;
    CheckBox animeCheckBox;
    View view;
    ProgressBar progressBar;
    ConstraintLayout progressLayout;
    Handler myHandler;
    MaterialButtonToggleGroup toggleGroupRewrite, toggleGroupHeader;
    ChipGroup chip_group;
    Chip chip_a, chip_b, chip_c, chip_d, chip_e, chip_f, chip_g;
    FuncFirebase funcFirebase;
    Func func = new Func();
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    // x_以降
    private BillingClient billingClient;
    private PurchasesUpdatedListener purchasesUpdatedListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // 常にDark theme off
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(this);
//        sharedPref.putBooleanFirstTimeLaunch(true);
        if (sharedPref.getBooleanFirstTimeLaunch()) {
            showTutorial();
//            sharedPref.putBooleanFirstTimeLaunch(false);
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        adBanner();
        setBottomNavigationView();

        x_Premium premiumFragment = new x_Premium();
        premiumFragment.setSubscriptionPurchaseCallback(this);




//        getPoint();
//        loadRewardedAd();
//        chipClick();
//        clearText();
//        getClipboard();
//        setToggleButton();
//        setCampaign();
//        imageMyPage.setOnClickListener(v -> {
//            dialogMyPages();
//        });
//        imageStarPlus.setOnClickListener(v -> {
//            dialogPurchase();
//        }); // スタートを押したときにPointが0の時、ダイアログを出すため別わけ
//        textPoint.setOnClickListener(v -> {
//            dialogPurchase();
//        });
//        imageStarIcon.setOnClickListener(v -> {
//            dialogPurchase();
//        });
//        intentExample();
//        dialogInfo();
//
//        btnStart.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                startButtonClicked();
//            }
//        });

    }
    
    @Override
    protected void onResume() {
        super.onResume();
        billingClientConnect();
    }

    private void setBottomNavigationView() {
        Fragment firstFragment = new x_Home();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.container,
                        firstFragment
                ).addToBackStack(null)
                .commit();

        BottomNavigationView navigationView = findViewById(R.id.bottom_topic);
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selectedItemId = item.getItemId();
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
                Fragment newFragment = null;

                // Check if the selected fragment is the current fragment
                if ((currentFragment instanceof x_Home && selectedItemId == R.id.navigation_home) ||
                        (currentFragment instanceof x_History && selectedItemId == R.id.navigation_history) ||
                        (currentFragment instanceof x_Premium && selectedItemId == R.id.navigation_premium) ||
                        (currentFragment instanceof x_MyPage && selectedItemId == R.id.navigation_my_page)) {
                    return true; // Return without replacing the fragment
                }

                // Replace the current fragment with the new fragment
                if (selectedItemId == R.id.navigation_home) {
                    newFragment = new x_Home();
                } else if (selectedItemId == R.id.navigation_history) {
                    newFragment = new x_History();
                } else if (selectedItemId == R.id.navigation_premium) {
                    newFragment = new x_Premium();
                } else {
                    newFragment = new x_MyPage();
                }

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.container,
                                newFragment
                        ).addToBackStack(null)
                        .commit();
                return true;
            }
        });
    }

    private void adBanner() {
        AdView mAdView = findViewById(R.id.main_adView_banner);

        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(this);
        if (sharedPref.getBooleanPremium()) {
            mAdView.setVisibility(View.GONE);
            return;
        }
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    private void billingClientConnect() {
        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(this);

        PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                // To be implemented in a later section.
            }
        };

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder()
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build(),
                            new PurchasesResponseListener() {
                                public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> purchases) {
                                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && !purchases.isEmpty()) {
                                        sharedPref.putBooleanPremium(true);
                                        Log.d(TAG, "onQueryPurchasesResponse: OK - purchase exist "+purchases);
                                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases.isEmpty()) {
                                        sharedPref.putBooleanPremium(false);
                                        Log.d(TAG, "onQueryPurchasesResponse: OK - purchase empty");
                                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                                        Log.d(TAG, "onQueryPurchasesResponse: USER_CANCELED");
                                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
                                        Log.d(TAG, "onQueryPurchasesResponse: FEATURE_NOT_SUPPORTED");
                                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                                        Log.d(TAG, "onQueryPurchasesResponse: ITEM_ALREADY_OWNED");
                                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                                        Log.d(TAG, "onQueryPurchasesResponse: BILLING_UNAVAILABLE");
                                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
                                        Log.d(TAG, "onQueryPurchasesResponse: DEVELOPER_ERROR");
                                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                                        Log.d(TAG, "onQueryPurchasesResponse: ITEM_UNAVAILABLE");
                                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR) {
                                        Log.d(TAG, "onQueryPurchasesResponse: NETWORK_ERROR");
                                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                                        Log.d(TAG, "onQueryPurchasesResponse: SERVICE_DISCONNECTED");
                                    } else {
                                        Log.d(TAG, "onQueryPurchasesResponse: OTHERS");
                                    }
                                }
                            }
                    );
                } else {
                    Log.d(TAG, "onBillingSetupFinished: Error|" + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d(TAG, "onBillingServiceDisconnected: Error");
            }
        });
    }

    @Override
    public void onSubscriptionPurchaseCompleted() {
        // Perform actions when subscription purchase is completed
        // For example, navigate to a different fragment or update UI
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new x_Home())
                .addToBackStack(null)
                .commit();
    }

//    public void startButtonClicked() {
//        textAnswer.setText("");
//        questionRaw = textQuestion.getText().toString();
//
//        if (TextUtils.isEmpty(questionRaw)) {
//            textQuestion.setError(getString(R.string.write_question));
//            textQuestion.requestFocus();
//            return;
//        }
//        if (!(lastFailure.equals(""))){
//            try {
//                // 現在時刻を取得する
//                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
//                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//                Date now = format.parse(timeStamp);
//
//                // lastFailureNumを日付型に変換する
//                Date failureDate = format.parse(lastFailure);
//
//                // 現在時刻とlastFailureの差分を計算する
//                long diffInMillis = now.getTime() - failureDate.getTime();
//
//                // 差分が10分以内であれば、"error"というメッセージと共にreturn
//                if (diffInMillis <= 600000) {
//                    Toast.makeText(this, "Please wait 10 minutes", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//        if (flagHeader.equals("rewrite")) {
//            question = "Modify the English below "
//                    + flagCorrectStyle
//                    + ". "
//                    + "Answer with a following format [Correct], [Alternative], then [Feedback] on the original sentence shortly. Answer only 1 time. Separate format into a paragraph. "
//                    + ":"
//                    + questionRaw;
//        } else if (flagHeader.equals("rephrase")){
//            Slider rephraseSlider = (Slider) findViewById(R.id.main_rephrase_slider);
//            int numRephrase = (int) rephraseSlider.getValue();
//            question = "Give "
//                    + numRephrase
//                    + " examples by rephrasing the following English: "
//                    + questionRaw;
//        } else {
//            question = "Describe the sentences in English. The parts that need to be filled in by the user should be [] :"
//                    + questionRaw;
//        }
//        Log.d("genki", "Question: "+question);
//
//        textQuestion.clearFocus();
//        textQuestion.setError(null);
//
//        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(textQuestion.getWindowToken(), 0);
//
//        if (uid.equals("GUEST")) {
//            dialogAdAlertMake();
//        } else if (point == 0) {
//            dialogPurchase();
//        } else {
////            startThread();
//            chatGPTAsync();
//        }
//    }
//
//    public void chatGPTAsync() {
//        // ProgressBarを表示する
//        progressBar.setVisibility(View.VISIBLE);
//
//        countDownStartTimer();
//
//        // スレッドを作成する
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String answer = chatGPT(question);
//                // 結果をメインスレッドに戻すために、Handlerを使用する
//                Handler handler = new Handler(Looper.getMainLooper());
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        showResult(answer);
//                        // ProgressBarを非表示にする
//                        progressBar.setVisibility(View.INVISIBLE);
//                        countDownStopTimer();
//                    }
//                });
//            }
//        });
//        // スレッドを開始する
//        thread.start();
//    }
//
//    private void showResult(String result) {
//        Log.d("genki", "API Result: "+result);
//        if (result.equals("error!")) {
//            func.setFadeFailure(layoutFailure, 5);
//
//            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//            DocumentReference Ref = db.collection("user").document(uid);
//            Ref.update("lastFailure", timeStamp)
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.d("genki", "DocumentSnapshot successfully updated!");
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.d("genki", "Error updating document", e);
//                        }
//                    });
//
//            Bundle bundle = new Bundle();
//            bundle.putString("flagHeader", flagHeader);
//            mFirebaseAnalytics.logEvent("api_failure", bundle);
//            return;
//        }
//
//        Integer questionWords = 0;
//        questionWords = question.length();
//
//        Integer answerWords = 0;
//        answerWords = result.length();
//
//        Integer total = questionWords + answerWords;
//        textCheck.setText("Q: " + questionWords + " + A: " + answerWords + " + T: " + total);
//
//        textAnswer.setWithMusic(false);
//        textAnswer.setDelay(1);
//
//        if (animation == 0 || answerWords > 100) {
//            // 3秒後に処理を実行する
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    textAnswer.animateText(result);
//                    textAnswer.removeAnimation();
//                }
//            }, 1800);
//            Log.d("genki", "answerWords | " + answerWords.toString());
//        } else {
//            textAnswer.animateText(result);
//        }
//
//
//        FuncFirebase funcFirebase = new FuncFirebase();
//        funcFirebase.addDataBaseTransaction(uid, questionRaw, result, questionWords, answerWords, point, flagHeader);
//        if (!uid.equals("GUEST")) {
//            funcFirebase.losePoint(answerWords, point, uid);
//            funcFirebase.updateReuse(uid);
//        }
//
//
//        Bundle bundle = new Bundle();
//        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, flagHeader);
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
//
////        dialogPostExecute(answerWords);
//        func.setFadeCompletion(layoutCompletion, textAnswerPoint, answerWords);
//        getPoint();
//    }
//
//    public String chatGPT(String question) {
//        String answer;
////        String questions = "Write a travel plan which shows the reasons why going to UK is the best. List 3 reasons shortly.";
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
//        OpenAiService service = new OpenAiService("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", Duration.ofSeconds(30));
//
//        List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
//        chatMessages.add(new ChatMessage("user", question));
//
//        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
//                .model("gpt-3.5-turbo")
//                .messages(chatMessages)
//                .maxTokens(4000)
//                .build();
//
//        try {
//            ChatCompletionChoice choice = service.createChatCompletion(completionRequest).getChoices().get(0);
//            answer = choice.getMessage().getContent();
//        } catch (Exception e) {
//            answer = "error!";
//            Log.d("genki", "error | " + e);
//
//        }
//        return answer;
////        return "error!";
//    }
//
//    private void countDownStartTimer() {
//        textCountDownTimer.setVisibility(View.VISIBLE);
//        countDownTimer = new CountDownTimer(secondsLeft * 1000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                secondsLeft--;
//                countDownUpdateTimerText();
//            }
//
//            @Override
//            public void onFinish() {
//                // Countdown timer finished, reset the timer
//                secondsLeft = 30;
//                countDownUpdateTimerText();
//            }
//        };
//
//        countDownTimer.start();
//    }
//
//    private void countDownUpdateTimerText() {
//        textCountDownTimer.setText(""+secondsLeft);
//    }
//
//    private void countDownStopTimer() {
//        textCountDownTimer.setVisibility(View.INVISIBLE);
//        countDownTimer.cancel();
//        secondsLeft = 30;
//    }
//
//    private void getPoint() {
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (uid.equals("GUEST")) {
//                    textPoint.setText("GUEST");
//                } else {
//                    db.collection("user")
//                            .document(uid)
//                            .get()
//                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                    if (task.isSuccessful()) {
//                                        DocumentSnapshot document = task.getResult();
//                                        if (document.exists()) {
//                                            point = Integer.parseInt(document.getData().get("point").toString());
//                                            lastFailure = document.getData().get("lastFailure").toString();
//                                            docId = document.getId();
//                                        } else {
//                                            Log.d("genki", "Error getting document: ", task.getException());
//                                        }
//                                        DecimalFormat formatter = new DecimalFormat("#,###,###");
//                                        try {
//                                            String pointString = formatter.format(point);
//                                            textPoint.setText(pointString);
//                                        } catch (Exception e) { // ログインしてもPointが入ってない場合がある
//                                            FirebaseAuth.getInstance().signOut();
//                                            textPoint.setText("GUEST");
//                                        }
//                                    }
//                                }
//                            });
//                }
//            }
//        }, 100);
//
//    }
//
//    private void dialogMyPages() {
//        DialogMyPage dialogRight = new DialogMyPage();
//        // 渡す値をセット
//        Bundle args = new Bundle();
//
//        args.putString("uid", uid);
//
//        dialogRight.setArguments(args);
//        dialogRight.show(getSupportFragmentManager(), "my_dialog");
////            uid = func.getUid(uid);
//    }
//
//    private void dialogPurchase() {
//        DialogPurchase dialogRight = new DialogPurchase();
//        Bundle args = new Bundle();
//
//        args.putString("uid", uid);
//        args.putInt("currentPoint", point);
//
//        dialogRight.setArguments(args);
//        dialogRight.show(getSupportFragmentManager(), "my_dialog");
//    }
//
//    public void dialogInfo() {
//        imageInfo.setOnClickListener(v -> {
//            DialogInfo dialogRight = new DialogInfo();
//            Bundle args = new Bundle();
//
//            args.putString("docId", docId);
//
//            dialogRight.setArguments(args);
//            dialogRight.show(getSupportFragmentManager(), "my_dialog");
//
//        });
//    }
//
//    public void intentExample() {
//        imageExample.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, History.class);
//            intent.putExtra("UID", uid);
//            startActivity(intent);
//        });
//    }
//
//    private void dialogAdAlertMake() {
//        AlertDialog.Builder builder;
//        builder = new AlertDialog.Builder(this);
//        builder.setTitle(R.string.show_ads);
//        builder.setMessage("Sign in to get 1,000 stars and run without ads!");
//        builder.setPositiveButton(R.string.YES, (dialog, id) -> showRewardedAd());
//        builder.setNeutralButton("SING IN",(dialog, id) -> dialogMyPages());
//        builder.setNegativeButton(R.string.CANCEL, (dialog, id) -> loadRewardedAd());
//        builder.create();
//        builder.show();
//    }
//
//    public void showRewardedAd() {
//        if (mRewardedAd != null) {
//            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                @Override
//                public void onAdClicked() {
//                    // Called when a click is recorded for an ad.
//                    Log.d("genki", "Ad was clicked.");
//                }
//
//                @Override
//                public void onAdDismissedFullScreenContent() {
//                    // Called when ad is dismissed.
//                    // Set the ad reference to null so you don't show the ad a second time.
//                    Log.d("genki", "Ad dismissed fullscreen content.");
//                    mRewardedAd = null;
//                    loadRewardedAd();
//                }
//
//                @Override
//                public void onAdFailedToShowFullScreenContent(AdError adError) {
//                    // Called when ad fails to show.
//                    Log.e("genki", "Ad failed to show fullscreen content.");
//                    mRewardedAd = null;
//                }
//
//                @Override
//                public void onAdImpression() {
//                    // Called when an impression is recorded for an ad.
//                    Log.d("genki", "Ad recorded an impression.");
//
//                }
//
//                @Override
//                public void onAdShowedFullScreenContent() {
//                    // Called when ad is shown.
//                    Log.d("genki", "Ad showed fullscreen content.");
//                }
//            });
//            Activity activityContext = this;
//            mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
//                @Override
//                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
//                    // Handle the reward.
//                    Log.d("genki", "The user earned the reward.");
//                    chatGPTAsync();
//                }
//            });
//        } else {
//            Log.d("genki", "The rewarded ad wasn't ready yet.");
//            loadRewardedAd();
//        }
//    }
//
//    public void loadRewardedAd() {
//        AdRequest adRequest = new AdRequest.Builder().build();
//        RewardedAd.load(this, AD_UNIT_ID,
//                adRequest, new RewardedAdLoadCallback() {
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        // Handle the error.
////                        Log.d("genki", "loadAdError.toString()" + loadAdError.toString());
//                        mRewardedAd = null;
//                    }
//
//                    @Override
//                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
//                        mRewardedAd = rewardedAd;
//                        Log.d("genki", "Ad was loaded.");
//                    }
//                });
//    }
//
//    private void clearText() {
//        imageClear.setOnClickListener(v -> {
//            question = "";
//            textQuestion.setText("");
//        });
//    }
//
//    private void getClipboard() {
//        textCopy.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                cm.setText(textAnswer.getText());
//            }
//        });
//    }
//
//    public void setToggleButton() {
//        // Toggle Button のクリック処理
//        toggleGroupRewrite.setSelectionRequired(true);
//        // 初期設定を終日にする
//        toggleGroupRewrite.check(R.id.main_rewrite_normal);
//
//        toggleGroupRewrite.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
//            @Override
//            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
//                if (isChecked) {
//                    if (checkedId == R.id.main_rewrite_normal) {
//                        flagCorrectStyle = "";
//                    } else if (checkedId == R.id.main_rewrite_casual) {
//                        flagCorrectStyle = "with broken native English";
//                    } else if (checkedId == R.id.main_rewrite_formal) {
//                        flagCorrectStyle = "formal and polite";
//                    } else if (checkedId == R.id.main_rewrite_mail) {
//                        flagCorrectStyle = "like a business mail";
//                    }
//                }
//            }
//        });
//
//        // Toggle Button のクリック処理
//        toggleGroupHeader.setSelectionRequired(true);
//        // 初期設定を終日にする
//        toggleGroupHeader.check(R.id.main_header_rewrite);
//        MaterialButton rewriteButton = findViewById(R.id.main_header_rewrite);
//        rewriteButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_theme_dark_onErrorContainer)));
//
//        toggleGroupHeader.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
//            @SuppressLint("ResourceAsColor")
//            @Override
//            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
//                if (isChecked) {
//                    for (int i = 0; i < group.getChildCount(); i++) {
//                        MaterialButton button = (MaterialButton) group.getChildAt(i);
//                        if (button.getId() == checkedId) {
//                            // Set the background color of the clicked button
//                            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_theme_dark_onErrorContainer)));
////                            button.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white_text)));
//                        } else {
//                            // Reset the background color of other buttons
//                            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.transparent)));
////                            button.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white_text)));
//                        }
//                    }
//                    }
//                if (isChecked) {
//                    if (checkedId == R.id.main_header_rewrite) {
//                        flagHeader = "rewrite";
//                        toggleGroupRewrite.setVisibility(View.VISIBLE);
////                        chip_group.setVisibility(View.GONE);
//                        layoutRephrase.setVisibility(View.GONE);
//                    } else if (checkedId == R.id.main_rephrase) {
//                        flagHeader = "rephrase";
//                        toggleGroupRewrite.setVisibility(View.GONE);
////                        chip_group.setVisibility(View.VISIBLE);
//                        layoutRephrase.setVisibility(View.VISIBLE);
//                    }
//                }
//            }
//        });
//    }
//
//    public void chipClick() {
//        chip_a.setOnClickListener(v -> {
//            textQuestion.setText("Write a sample self-introduction");
//        });
//        chip_b.setOnClickListener(v -> {
//            textQuestion.setText("Write a sample apology");
//        });
//        chip_c.setOnClickListener(v -> {
//            textQuestion.setText("Write a sample appreciation.");
//        });
//        chip_d.setOnClickListener(v -> {
//            textQuestion.setText("Write a sample greeting");
//        });
//        chip_e.setOnClickListener(v -> {
//            textQuestion.setText("Write an example of praise");
//        });
//        chip_f.setOnClickListener(v -> {
//            textQuestion.setText("Write an example inquiry.");
//        });
//    }
//
    // チュートリアルを表示する
    private void showTutorial() {
        setContentView(R.layout.tutorial_layout1);

        // レイアウト1を表示する
        // ユーザーが「次へ」ボタンを押すと、次のレイアウトを表示する
        // 最後のレイアウトの「完了」ボタンを押すと、メインアクティビティに戻る
        Button nextButton = findViewById(R.id.tutorial_1_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.tutorial_layout2);
                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                List<String> dataListWrong = new ArrayList<>();
                dataListWrong.add("I am not good at English");
                dataListWrong.add("I have many mistakes in my writing.");
                dataListWrong.add("I want to be better at writing.");
                dataListWrong.add("I go to the store yesterday.");
                dataListWrong.add("They doesn't know about the plan.");
                dataListWrong.add("My brother ain't going to the party.");

                List<String> dataListCorrect = new ArrayList<>();
                dataListCorrect.add("English is not my strong suit.");
                dataListCorrect.add("There are numerous errors in my writing.");
                dataListCorrect.add("I aim to improve my writing skills.");
                dataListCorrect.add("Yesterday, I ventured to the store.");
                dataListCorrect.add("The plan remains unknown to them.");
                dataListCorrect.add("My brother isn't planning to attend the party.");

                x_TutorialAdapter adapter = new x_TutorialAdapter(dataListWrong, dataListCorrect);
                recyclerView.setAdapter(adapter);
                // レイアウト2を表示する
                // ユーザーが「次へ」ボタンを押すと、次のレイアウトを表示する
                // 最後のレイアウトの「完了」ボタンを押すと、メインアクティビティに戻る
                Button nextButton2 = findViewById(R.id.tutorial_2_next_button);
                nextButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setContentView(R.layout.tutorial_layout3);
                        // レイアウト3を表示する
                        TextView textViewTermsOfService = findViewById(R.id.x_tutorial_3_text_terms_of_use);
                        TextView textViewPrivacyPolicy = findViewById(R.id.x_tutorial_3_text_privacy_policy);
                        x_Utils.setGuestTextLink(textViewTermsOfService, getString(R.string.terms_of_service), getString(R.string.url_terms_of_service));
                        x_Utils.setGuestTextLink(textViewPrivacyPolicy, getString(R.string.privacy_policy), getString(R.string.url_privacy_policy));
                        // ユーザーが「次へ」ボタンを押すと、次のレイアウトを表示する
                        // 最後のレイアウトの「完了」ボタンを押すと、メインアクティビティに戻る
                        setTutorialDone();
                    }
                });
            }
        });
    }

    private void setTutorialDone() {
        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(MainActivity.this);

        CheckBox checkBoxUseOfService = findViewById(R.id.x_tutorial_3_check_terms_of_use);
        CheckBox checkBoxPrivacyPolicy = findViewById(R.id.x_tutorial_3_check_privacy_policy);
        TextView textView = findViewById(R.id.x_tutorial_3_text_request);

        textView.setVisibility(View.GONE);

        SignInButton signInButton = findViewById(R.id.x_tutorial_3google_sign_in_button);
        Button buttonDone = findViewById(R.id.done_button);

        View.OnClickListener onClickListener = v -> {
            Boolean isBoxChecked = checkBoxUseOfService.isChecked() && checkBoxPrivacyPolicy.isChecked();
            if (isBoxChecked) {
                checkBoxPrivacyPolicy.requestFocus();
                sharedPref.putBooleanFirstTimeLaunch(false);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, new Bundle());
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                textView.setVisibility(View.INVISIBLE);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textView.setVisibility(View.VISIBLE);
                    }
                }, 300);
            }
        };

        signInButton.setOnClickListener(onClickListener);
        buttonDone.setOnClickListener(onClickListener);
    }


//    private void setCampaign() {
//        if (uid.equals("GUEST")) {
//            imageCampaignClear.setOnClickListener(v -> {
//                imageCampaignClear.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // layoutCampaignをGONEに設定
//                        layoutCampaign.setVisibility(View.GONE);
//                        // SharedPreferencesに保存
//                        saveLayoutCampaignState(true);
//                    }
//                });
//            });
//            // SharedPreferencesから保存された値を読み込んでlayoutCampaignの表示状態を設定
//            boolean isLayoutCampaignGone = getLayoutCampaignState();
//            if (isLayoutCampaignGone) {
//                layoutCampaign.setVisibility(View.GONE);
//            }
//        } else {
//            layoutCampaign.setVisibility(View.GONE);
//        }
//    }
//
//    private void saveLayoutCampaignState(boolean isGone) {
//        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(PREF_LAYOUT_CAMPAIGN, isGone);
//        editor.apply();
//    }
//
//    private boolean getLayoutCampaignState() {
//        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        return sharedPreferences.getBoolean(PREF_LAYOUT_CAMPAIGN, false);
//    }
//
//    public void findView() {
//        textQuestion = (EditText) findViewById(R.id.main_question);
//        textAnswer = (TypeWriterView) findViewById(R.id.main_answer);
//        textAnswer.setMovementMethod(new ScrollingMovementMethod());
//        btnStart = (Button) findViewById(R.id.main_start_button);
//        imageMyPage = (ImageView) findViewById(R.id.main_mypage);
//        imageInfo = (ImageView) findViewById(R.id.main_info);
//        imageExample = (ImageView) findViewById(R.id.main_example);
//        textCheck = (TextView) findViewById(R.id.main_check);
//        animeCheckBox = (CheckBox) findViewById(R.id.main_animation_check);
//        imageStarPlus = (ImageView) findViewById(R.id.main_star_plus);
//        textFillQuestion = (TextView) findViewById(R.id.fill_question);
//        textPoint = (TickerView) findViewById(R.id.main_point);
//        textPoint.setCharacterLists(TickerUtils.provideNumberList());
//        textPoint.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);
//        textPoint.setAnimationDuration(1000);
//        imageClear = (ImageView) findViewById(R.id.main_clear);
//        imageReset = (ImageView) findViewById(R.id.main_replay);
//        progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
//        progressBar.setVisibility(View.INVISIBLE);
//        textCountDownTimer = findViewById(R.id.main_timer);
//        textCountDownTimer.setVisibility(View.INVISIBLE);
//        view = getWindow().getDecorView().findViewById(android.R.id.content);
////        toggleGroupLetters = findViewById(R.id.main_letters_group); // 消す
//        myHandler = new Handler(Looper.getMainLooper());
//        layoutCompletion = (LinearLayout) findViewById(R.id.main_completion_layout);
//        textAnswerPoint = (TextView) findViewById(R.id.main_completion_answer_num);
//        Resources res = getResources();
//        textCopy = (TextView) findViewById(R.id.main_answer_copy);
//        layoutFailure = (LinearLayout) findViewById(R.id.main_failure_layout);
//        toggleGroupRewrite = findViewById(R.id.main_proofreading_toggle_group);
//        toggleGroupHeader = findViewById(R.id.main_header_toggle_group);
//        imageStarIcon = (ImageView) findViewById(R.id.main_star_icon);
//        layoutRephrase = (LinearLayout) findViewById(R.id.main_rephrase_slider_layout);
//        layoutRephrase.setVisibility(View.GONE);
//        layoutCampaign = (LinearLayout) findViewById(R.id.main_campaign_layout);
//        imageCampaignClear = (ImageView) findViewById(R.id.main_campaign_clear);
//
//        chip_group = (ChipGroup) findViewById(R.id.main_chip_group);
//        chip_group.setVisibility(View.GONE);
//        chip_a = (Chip) findViewById(R.id.main_chip_a);
//        chip_b = (Chip) findViewById(R.id.main_chip_b);
//        chip_c = (Chip) findViewById(R.id.main_chip_c);
//        chip_d = (Chip) findViewById(R.id.main_chip_d);
//        chip_e = (Chip) findViewById(R.id.main_chip_e);
//        chip_f = (Chip) findViewById(R.id.main_chip_f);
//
//    }


}