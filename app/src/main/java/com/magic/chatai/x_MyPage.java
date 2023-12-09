package com.magic.chatai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class x_MyPage extends Fragment {
    private final String TAG = "genki";
    private static final int RC_SIGN_IN = 9001; // Request code for Google Sign-In
    private GoogleSignInClient mGoogleSignInClient; // GoogleSignInClientを使用する

    View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso); // GoogleSignInClientのインスタンスを作成
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.x_my_page, container, false);

        BottomNavigationView navigationView = getActivity().findViewById(R.id.bottom_topic);
        navigationView.getMenu().getItem(3).setChecked(true);

        setLayoutChange();
        adBanner();
        setDialogInfo();

        if (x_Utils.isLoginUser()) {
            setMemberFirestoreData();
            setGuestLogout();
        } else {
            setGuestPolicyText();
            setGuestSignIn();
        }

        return view;
    }

    private void setLayoutChange() {
        LinearLayout linearLayoutGuest = view.findViewById(R.id.x_my_page_layout_guest);
        LinearLayout linearLayoutMember = view.findViewById(R.id.x_my_page_layout_member);
        if (x_Utils.isLoginUser()) {
            linearLayoutGuest.setVisibility(View.GONE);
            linearLayoutMember.setVisibility(View.VISIBLE);
        } else {
            linearLayoutGuest.setVisibility(View.VISIBLE);
            linearLayoutMember.setVisibility(View.GONE);
        }
    }

    private void adBanner() {
        AdView myPageAdView = view.findViewById(R.id.x_my_page_adView_banner);

        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(getActivity());
        if (sharedPref.getBooleanPremium()) {
            myPageAdView.setVisibility(View.GONE);
            return;
        }

        MobileAds.initialize(view.getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        myPageAdView.loadAd(adRequest);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("ja");

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            setLayoutChange();
                            Log.d("genki", "signInWithCredential:success");

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("genki", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void setGuestSignIn() {
        TextView textView = view.findViewById(R.id.x_my_page_text_request);
        textView.setVisibility(View.GONE);
        SignInButton signInButton = view.findViewById(R.id.google_sign_in_button);
        signInButton.setOnClickListener(v -> {
            CheckBox checkBoxUseOfService = view.findViewById(R.id.x_my_page_check_terms_of_use);
            CheckBox checkBoxPrivacyPolicy = view.findViewById(R.id.x_my_page_check_privacy_policy);

            Boolean isCheckedUseOfService = checkBoxUseOfService.isChecked();
            Boolean isCheckedPrivacyPolicy = checkBoxPrivacyPolicy.isChecked();

            if (isCheckedUseOfService && isCheckedPrivacyPolicy) {
                textView.setVisibility(View.GONE);
                signIn();
            } else {
                textView.setVisibility(View.VISIBLE);
            }
        });
    }

    // signInメソッドの修正
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent(); // mGoogleApiClientではなくmGoogleSignInClientを使用
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void setGuestPolicyText() {
        TextView textViewTermsOfService = view.findViewById(R.id.x_my_page_text_terms_of_use);
        TextView textViewPrivacyPolicy = view.findViewById(R.id.x_my_page_text_privacy_policy);
        x_Utils.setGuestTextLink(textViewTermsOfService, getString(R.string.terms_of_service), getString(R.string.url_terms_of_service));
        x_Utils.setGuestTextLink(textViewPrivacyPolicy, getString(R.string.privacy_policy), getString(R.string.url_privacy_policy));

    }


    private void setGuestLogout() {
        Button button = view.findViewById(R.id.x_my_page_logout);
        button.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(),
                    task -> getActivity().recreate()); // GoogleSignInClientを使用してサインアウトし、再作成する
            setLayoutChange();
        });
    }

    private void setMemberFirestoreData() {
        final String COLLECTION_NAME = "transactions";
        final String UID = x_Utils.getUid(getContext());
        x_UtilsFirestore firestoreUtils = new x_UtilsFirestore();
        firestoreUtils.getFirestoreData(COLLECTION_NAME, UID)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        x_FirestoreData firestoreData = task.getResult();
                        if (firestoreData != null) {
                            int point = firestoreData.getPoint();
                            String createDate = firestoreData.getCreateDate();
                            List<Map<String, Object>> post = firestoreData.getPost();

                            Map<String, Object> result = postProcessor(post);
                            setMemberResult(R.id.x_my_page_a, ""+point);
                            setMemberResult(R.id.x_my_page_b, ""+post.stream().mapToInt(Map::size).sum());
                            setMemberResult(R.id.x_my_page_c, result.get("wordsInBrackets").toString());
                            setMemberResult(R.id.x_my_page_d, ""+result.get("ratio")+"%");
                        } else {
                            Log.d(TAG, "getFirestoreData: DATA NULL");
                        }
                    } else {
                        Log.d(TAG, "getFirestoreData: ERROR");
                    }
                });
    }

    private void setMemberResult(int resourceId, String text) {
        TextView textViewA = view.findViewById(resourceId);
        textViewA.setText(text);
    }

    private Map<String, Object> postProcessor(List<Map<String, Object>> posts) {
        Map<String, Integer> totalWordCountMap = new HashMap<>();
        Map<String, Integer> wordCountInBracketsMap = new HashMap<>();

        for (Map<String, Object> post : posts) {
            String answer = (String) post.get("answer");
            String uid = (String) post.get("uid");  // UIDの取得
            // []内の単語数を取得
            int wordsInBrackets = countWordsInBrackets(answer);

            // 全ての単語数を取得
            int totalWords = countTotalWords(answer);

            // マップにデータを追加または更新
            totalWordCountMap.put(uid, totalWordCountMap.getOrDefault(uid, 0) + totalWords);
            wordCountInBracketsMap.put(uid, wordCountInBracketsMap.getOrDefault(uid, 0) + wordsInBrackets);
        }

        // 結果の表示
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : totalWordCountMap.entrySet()) {
            String uid = entry.getKey();
            int totalWords = entry.getValue();
            int wordsInBrackets = wordCountInBracketsMap.getOrDefault(uid, 0);

            int ratio = Math.round((float) wordsInBrackets / totalWords * 100);

            result.put("wordsInBrackets", wordsInBrackets);
            result.put("ratio", ratio);
        }

        return result;
    }


    private int countWordsInBrackets(String text) {
        String[] wordsInBrackets = text.split(" ");
        int count = 0;
        boolean insideBrackets = false;

        for (String word : wordsInBrackets) {
            // 1. [と]の両方が含まれている場合はcount++
            if (word.contains("[") && word.contains("]")) {
                count++;
            } else if (word.contains("[")) {
                // 2. [が含まれている場合はcount++ さらに次以降で]が出てくるまでcount++
                count++;
                insideBrackets = true;
            } else if (word.contains("]")) {
                // 3. ]が含まれている場合は次以降に1.2の条件に当てはまらない場合はcount++しない
                insideBrackets = false;
                count++;
            } else if (insideBrackets) {
                // [が含まれている単語の場合、次以降で]が出てくるまでcount++
                count++;
            }
        }
        return count;
    }

    private static int countTotalWords(String text) {
        String[] words = text.split("\\s+");
        return words.length;
    }

    private void setDialogInfo() {
        LinearLayout linearLayout = view.findViewById(R.id.x_my_page_info);
        linearLayout.setOnClickListener(v -> {
            x_DialogInfo dialog = new x_DialogInfo();
            Bundle args = new Bundle();
            args.putString("key", "value");
            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), "my_dialog");
        });
    }

}
