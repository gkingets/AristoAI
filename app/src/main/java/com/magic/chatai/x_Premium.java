package com.magic.chatai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class x_Premium extends Fragment {
    private final String TAG = "genki";
    View view;
    private SubscriptionPurchaseCallback subscriptionPurchaseCallback;

    final private String ITEM_15 = "star_15";
    final private String ITEM_50 = "star_50";
    final private String ITEM_PREMIUM = "premium";
    final private String TEST = "plan_a";
    final private String MANAGE_SUB_LINK = "https://play.google.com/store/account/subscriptions";

    private BillingClient billingClient;
    private PurchasesUpdatedListener purchasesUpdatedListener;

    @Override
    public void onResume() {
        super.onResume();
        billingClient = BillingClient.newBuilder(view.getContext())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder()
                                    .setProductType(BillingClient.ProductType.INAPP)
                                    .build(),
                            (billingResults, list) -> {
                                if (billingResults.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    for (Purchase purchase : list) {
                                        Log.d(TAG, "onBillingSetupFinished: "+purchase.getPurchaseState());
                                    }
                                }
                            }
                    );
                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder()
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build(),
                            new PurchasesResponseListener() {
                                public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> purchases) {
                                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                        for (Purchase purchase : purchases) {
                                            Log.d(TAG, "onQueryPurchasesResponse: "+purchase);
                                            Log.d(TAG, "onQueryPurchasesResponse: "+purchase.getPurchaseState());
                                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                                                Log.d(TAG, "onQueryPurchasesResponse: OK "+purchase.getOrderId());
                                            }
                                        }
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.x_premium, container, false);

        BottomNavigationView navigationView = getActivity().findViewById(R.id.bottom_topic);
        navigationView.getMenu().getItem(2).setChecked(true);

        billingClientConnect();
        setInitialView();
        setGuestLayout();

        return view;
    }

    private void setInitialView() {
        TextView textViewPoint = view.findViewById(R.id.x_premium_point);
        TextView textViewManageLink = view.findViewById(R.id.x_premium_manage_link);
        ImageView imageViewPremium = view.findViewById(R.id.x_premium_point_infinite);
        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(getActivity());
        if (sharedPref.getBooleanPremium()) { // Premium User
            textViewPoint.setVisibility(View.GONE);
            textViewManageLink.setVisibility(View.VISIBLE);
            setLink(textViewManageLink, MANAGE_SUB_LINK);
            imageViewPremium.setVisibility(View.VISIBLE);
        } else { // Guest
            textViewPoint.setVisibility(View.VISIBLE);
            textViewManageLink.setVisibility(View.GONE);
            imageViewPremium.setVisibility(View.GONE);
            x_UtilsFirestore.setPointFromFirestore(view.getContext(), textViewPoint);
        }
    }

    private void setGuestLayout() {
        LinearLayout linearLayout = view.findViewById(R.id.x_premium_login_layout);
        if (x_Utils.isLoginUser()) {
            linearLayout.setVisibility(View.GONE);
        } else {
            linearLayout.setVisibility(View.VISIBLE);
        }
        linearLayout.setOnClickListener(v -> {
            sendToMyPage();
        });
    }

    private void setLink(TextView textView, String url) {
        // 既存のテキストを設定
        CharSequence existingText = getString(R.string.manage_your_subscription);

        // ClickableSpanを作成して、リンクがクリックされたときの挙動を定義
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // リンクがクリックされたときの処理
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        };

        // ClickableSpanを既存のテキストに適用
        SpannableString spannableString = new SpannableString(existingText);
        spannableString.setSpan(clickableSpan, 0, existingText.length(), 0);

        // TextViewにSpannableStringを設定し、クリック可能にする設定
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }



    private void billingClientConnect() {
        purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (Purchase purchase : purchases) {
                        if (purchase.getProducts().get(0).equals(ITEM_15) || purchase.getProducts().get(0).equals(ITEM_50)) {
                            handleUnitPurchase(purchase);
                            Log.d(TAG, "onPurchasesUpdated: UnitPurchase");
                        } else if (purchase.getProducts().get(0).equals(ITEM_PREMIUM)) {
                            handleSubscriptionPurchase(purchase);
                            Log.d(TAG, "onPurchasesUpdated: SubscriptionPurchase");
                        }
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    Log.d(TAG, "onPurchasesUpdated: USER_CANCELED");
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
                    Log.d(TAG, "onPurchasesUpdated: FEATURE_NOT_SUPPORTED");
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                    Log.d(TAG, "onPurchasesUpdated: ITEM_ALREADY_OWNED");
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                    Log.d(TAG, "onPurchasesUpdated: BILLING_UNAVAILABLE");
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
                    Log.d(TAG, "onPurchasesUpdated: DEVELOPER_ERROR");
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                    Log.d(TAG, "onPurchasesUpdated: ITEM_UNAVAILABLE");
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR) {
                    Log.d(TAG, "onPurchasesUpdated: NETWORK_ERROR");
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                    Log.d(TAG, "onPurchasesUpdated: SERVICE_DISCONNECTED");
                } else {
                    Log.d(TAG, "onPurchasesUpdated: OTHERS");
                }
            }
        };

        billingClient = BillingClient.newBuilder(view.getContext())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    showUnitItems();
                    showPremiumItem();
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


    private void showUnitItems() {
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(
                                ImmutableList.of(
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId(ITEM_15)
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId(ITEM_50)
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build()
                                ))
                        .build();
        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult,
                                                         List<ProductDetails> productDetailsList) {

                        Button button15 = view.findViewById(R.id.x_premium_plan_15);
                        button15.setText(productDetailsList.get(0).getOneTimePurchaseOfferDetails().getFormattedPrice());
                        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(getActivity());
                        if (sharedPref.getBooleanPremium()) {
                            button15.setEnabled(false);
                        }
                        button15.setOnClickListener(v -> {
                            launchUnitPurchase(productDetailsList.get(0));
                        });
                    }
                }
        );
        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult,
                                                         List<ProductDetails> productDetailsList) {

                        Button button50 = view.findViewById(R.id.x_premium_plan_50);
                        button50.setText(productDetailsList.get(1).getOneTimePurchaseOfferDetails().getFormattedPrice());
                        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(getActivity());
                        if (sharedPref.getBooleanPremium()) {
                            button50.setEnabled(false);
                        }
                        button50.setOnClickListener(v -> {
                            launchUnitPurchase(productDetailsList.get(1));
                        });
                    }
                }
        );
    }

    private void showPremiumItem() {
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(
                                ImmutableList.of(
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId(ITEM_PREMIUM)
                                                .setProductType(BillingClient.ProductType.SUBS)
                                                .build()
                                ))
                        .build();
        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult,
                                                         List<ProductDetails> productDetailsList) {

//                        Log.d(TAG, "onProductDetailsResponse: "+ productDetailsList.get(0));

                        TextView textView = view.findViewById(R.id.x_premium_plan_premium);
                        Button button = view.findViewById(R.id.x_premium_plan_join);
                        textView.setText(productDetailsList.get(0).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice());
                        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(getActivity());
                        if (sharedPref.getBooleanPremium()) {
                            button.setText("You are already a user");
                            button.setEnabled(false);
                            return;
                        }
                        button.setText("Join the Premium Membership");
                        button.setOnClickListener(v -> {
                            launchSubscriptionPurchase(productDetailsList.get(0));
                        });
                    }
                }
        );
    }

    private void launchUnitPurchase(ProductDetails productDetails) {
        TextView textView = view.findViewById(R.id.x_premium_point);
        int currentPoint = Integer.parseInt(textView.getText().toString());
        if (!x_Utils.isLoginUser()) {
            sendToMyPage();
            Toast.makeText(getActivity(), "Sing in is required in advance.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentPoint > 5) {
            Toast.makeText(getActivity(), "Please consume Star to 5 or less.", Toast.LENGTH_SHORT).show();
            return;
        }

        ImmutableList productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
//                                .setOfferToken(skuDetailsToken)
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        BillingResult billingResult = billingClient.launchBillingFlow(getActivity(), billingFlowParams);
    }

    private void launchSubscriptionPurchase(ProductDetails productDetails) {
        Log.d(TAG, "launchSubscriptionPurchase: ");
        if (!x_Utils.isLoginUser()) {
            sendToMyPage();
            Toast.makeText(getActivity(), "Sing in is required in advance.", Toast.LENGTH_SHORT).show();
            return;
        }

        String skuDetailsToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();

        ImmutableList productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(skuDetailsToken)
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        BillingResult billingResult = billingClient.launchBillingFlow(getActivity(), billingFlowParams);
    }

    private void sendToMyPage() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, new x_MyPage());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void handleUnitPurchase(Purchase purchase) {
        Log.d(TAG, "handleUnitPurchase: ");
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                    completeUnitPurchase(purchase);
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
    }

    private void completeUnitPurchase(Purchase purchase) {
        TextView textView = view.findViewById(R.id.x_premium_point);
        x_UtilsFirestore.setPointFromFirestore(view.getContext(), textView);
        int currentPoint = Integer.parseInt(textView.getText().toString());

        Log.d(TAG, "test | getQuantity() | "+purchase.getQuantity());
        Log.d(TAG, "test | getPurchaseState() | "+purchase.getPurchaseState());
        Log.d(TAG, "test | getOrderId() | "+purchase.getOrderId());
        Log.d(TAG, "test | getPurchaseToken() | "+purchase.getPurchaseToken());
        Log.d(TAG, "test | getPurchaseTime() | "+purchase.getPurchaseTime());
        Log.d(TAG, "test | getPackageName() | "+purchase.getPackageName());
        Log.d(TAG, "test | getSignature() | "+purchase.getSignature());
        Log.d(TAG, "test | getProducts() | "+purchase.getProducts());
        final String ITEM_NAME = purchase.getProducts().get(0);

        int additionalPoints = 0;
        if (ITEM_NAME.equals(ITEM_15)) {
            additionalPoints += 15;
        } else if (ITEM_NAME.equals(ITEM_50)) {
            additionalPoints += 50;
        } else {
            additionalPoints = 0;
        }

        x_UtilsFirestore utilsFirestore = new x_UtilsFirestore();
        utilsFirestore.updatePoint(x_Utils.getUid(getContext()), currentPoint, additionalPoints);
        utilsFirestore.updatePurchase(purchase, x_Utils.getUid(view.getContext()));
        onResume();
    }

    private void handleSubscriptionPurchase(Purchase purchase) {
        x_UtilsSharedPref sharedPref = new x_UtilsSharedPref(getActivity());
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "onAcknowledgePurchaseResponse: Complete");
                    sharedPref.putBooleanPremium(true);
                }
            }

        };
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);

                if (subscriptionPurchaseCallback != null) {
                    subscriptionPurchaseCallback.onSubscriptionPurchaseCompleted();
                }
            }
        }
    }

    public void setSubscriptionPurchaseCallback(SubscriptionPurchaseCallback callback) {
        this.subscriptionPurchaseCallback = callback;
    }


    public interface SubscriptionPurchaseCallback {
        void onSubscriptionPurchaseCompleted();
    }


}
