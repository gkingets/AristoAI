package com.magic.chatai;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Locale;

public class DialogMyPage extends DialogFragment {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAnalytics mFirebaseAnalytics;

    View dialogLayout;
    Button btnLogin, btnLogout;
    String uid;
    SignInButton btnGoogleLogin;
    TextView textViewTermsOfUse, textViewPrivacyPolicy, textViewRequest,
            textId;
    LinearLayout policyLayout;
    CheckBox checkBoxTermsOfUse, checkBoxPrivacyPolicy;
    Integer flagTermsOfUse = 0, flagPrivacyPolicy = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogLayout = inflater.inflate(R.layout.dialog_mypage, null);

        builder.setView(dialogLayout)
                .setPositiveButton("", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        findView();
        changeView();
        changeTermsOfUse();
        changePrivacyPolicy();
        changeCheckBox();
        changeTestId();


        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("ja");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(dialogLayout.getContext(), gso);


        signIn();
        btnLogout();

        return builder.create(); // returnを後で入れる
    }

    public void changeView() {
        if (uid.equals("GUEST")) {
            policyLayout.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
            btnGoogleLogin.setVisibility(View.VISIBLE);
        } else {
            policyLayout.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
            btnGoogleLogin.setVisibility(View.GONE);
        }
    }

    private void changeTestId() {
        textId.setOnClickListener(v -> {
            textId.setText(uid);
        });
    }

    private void changeTermsOfUse() {
        // テキスト内にリンクとなる部分の開始位置と終了位置を取得する
        String textTermsOfUse = textViewTermsOfUse.getText().toString();
        SpannableString SpannableStringTermsOfUse = new SpannableString(textTermsOfUse);

        int linkStartIndexTermsOfUse = textTermsOfUse.indexOf(getString(R.string.terms_of_service));
        int linkEndIndexTermsOfUse = linkStartIndexTermsOfUse + getString(R.string.terms_of_service).length();

        // ClickableSpanオブジェクトを作成する
        ClickableSpan clickableSpanTermsOfUse = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // リンクがクリックされたときの処理を記述する
                Uri uri = Uri.parse(getString(R.string.url_terms_of_service));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        };
        // SpannableStringオブジェクトにClickableSpanオブジェクトを設定する
        SpannableStringTermsOfUse.setSpan(clickableSpanTermsOfUse, linkStartIndexTermsOfUse, linkEndIndexTermsOfUse, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // TextViewにSpannableStringオブジェクトを設定する
        textViewTermsOfUse.setText(SpannableStringTermsOfUse);
        // リンクをクリック可能にするための設定
        textViewTermsOfUse.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void changePrivacyPolicy() {
        // テキスト内にリンクとなる部分の開始位置と終了位置を取得する
        String textPrivacyPolicy = textViewPrivacyPolicy.getText().toString();
        SpannableString SpannableStringPrivacyPolicy = new SpannableString(textPrivacyPolicy);

        int linkStartIndexPrivacyPolicy = textPrivacyPolicy.indexOf(getString(R.string.privacy_policy));
        int linkEndIndexPrivacyPolicy = linkStartIndexPrivacyPolicy + getString(R.string.privacy_policy).length();

        // ClickableSpanオブジェクトを作成する
        ClickableSpan clickableSpanPrivacyPolicy = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // リンクがクリックされたときの処理を記述する
                Uri uri = Uri.parse(getString(R.string.url_privacy_policy));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        };
        // SpannableStringオブジェクトにClickableSpanオブジェクトを設定する
        SpannableStringPrivacyPolicy.setSpan(clickableSpanPrivacyPolicy, linkStartIndexPrivacyPolicy, linkEndIndexPrivacyPolicy, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // TextViewにSpannableStringオブジェクトを設定する
        textViewPrivacyPolicy.setText(SpannableStringPrivacyPolicy);
        // リンクをクリック可能にするための設定
        textViewPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void changeCheckBox() {
        checkBoxTermsOfUse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    flagTermsOfUse = 1;
                    textViewRequest.setVisibility(View.GONE);

                } else {
                    flagTermsOfUse = 0;
                }
            }
        });

        checkBoxPrivacyPolicy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    flagPrivacyPolicy = 1;
                    textViewRequest.setVisibility(View.GONE);
                } else {
                    flagPrivacyPolicy = 0;
                }
            }
        });
    }

    private void btnLogout() {
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            mGoogleSignInClient.signOut();
            getActivity().recreate();
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("genki", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("genki", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("genki", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            FuncFirebase funcFirebase = new FuncFirebase();
                            funcFirebase.createUser(user.getUid());
                            signInFireBase();
                            Log.d("genki", "login|"+user.getUid());
//                            Intent intent = new Intent(getContext(), MainActivity.class);
//                            startActivity(intent);
                            getActivity().recreate();
                            dismiss();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("genki", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void signIn() {
        btnGoogleLogin.setOnClickListener(v -> {
            if (flagTermsOfUse == 0) {
                checkBoxTermsOfUse.requestFocus();
                textViewRequest.setVisibility(View.VISIBLE);
                return;
            } else if (flagPrivacyPolicy == 0) {
                checkBoxPrivacyPolicy.requestFocus();
                textViewRequest.setVisibility(View.VISIBLE);
                return;
            }
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    private void signInFireBase() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }



    // 言語設定を保存してアプリを再起動する
    private void setLocale(String language) {
        SharedPreferences.Editor editor = requireContext().getSharedPreferences("LanguageSwitch", Context.MODE_PRIVATE).edit();
        editor.putString("language", language);
        editor.apply();

        // アプリを再起動する
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        startActivity(intent);
    }

    // 現在の言語設定を取得する
    private String getCurrentLocale() {
        SharedPreferences prefs = requireContext().getSharedPreferences("LanguageSwitch", Context.MODE_PRIVATE);
        return prefs.getString("language", "");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        // 言語設定に合わせてロケールを変更する
        super.onAttach(updateBaseContextLocale(context));
    }

    // ロケールを変更する
    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("LanguageSwitch", Context.MODE_PRIVATE);
        String language = prefs.getString("language", "");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Context newContext = context.createConfigurationContext(getResources().getConfiguration());
        return newContext;
    }

    public void findView() {
        uid = getArguments().getString("uid","GUEST");
        btnLogin = (Button) dialogLayout.findViewById(R.id.mypage_login);
        btnLogout = (Button) dialogLayout.findViewById(R.id.mypage_logout);
        btnGoogleLogin = (SignInButton) dialogLayout.findViewById(R.id.google_sign_in_button);
        textViewTermsOfUse = (TextView) dialogLayout.findViewById(R.id.mypage_text_terms_of_use);
        textViewPrivacyPolicy = (TextView) dialogLayout.findViewById(R.id.mypage_text_privacy_policy);
        policyLayout = dialogLayout.findViewById(R.id.mypage_policy_layout);
        checkBoxTermsOfUse = (CheckBox) dialogLayout.findViewById(R.id.mypage_check_user_policy);
        checkBoxPrivacyPolicy = (CheckBox) dialogLayout.findViewById(R.id.mypage_check_privacy_policy);
        textViewRequest = (TextView) dialogLayout.findViewById(R.id.mypage_text_request);
        textViewRequest.setVisibility(View.GONE);
        textId = (TextView) dialogLayout.findViewById(R.id.mypage_id);


    }


}
