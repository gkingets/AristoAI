package com.magic.chatai;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableList;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogPurchase extends DialogFragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    View dialogLayout;
    String uid, docId, iapName;
    Integer point, currentPoint, giveWords;
    Button btnPurchase1, btnPurchase2, btnPurchase3;
    BillingClient billingClient;

    List<ProductDetails> productDetailsList;
    String TAG = "genki";
    Handler handler;
    List<String> productIds;
    List<Integer> coins;
    FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());


    @Override
    public void onResume() {
        super.onResume();
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                (billingResult, list) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : list) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                verifyPurchase(purchase);
                            }
                        }
                    }
                }
        );
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogLayout = inflater.inflate(R.layout.dialog_purchase, null);

        builder.setView(dialogLayout)
                .setPositiveButton("", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        uid = getArguments().getString("uid", "");
        currentPoint = getArguments().getInt("currentPoint", 0);



        handler = new Handler();
        productIds = new ArrayList<>();
        coins = new ArrayList<>();
        productDetailsList = new ArrayList<>();

        findView();


        billingClient = BillingClient.newBuilder(getContext())
                .enablePendingPurchases()
                .setListener(
                        new PurchasesUpdatedListener() {
                            @Override
                            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                    for (Purchase purchase : list) {
                                        verifyPurchase(purchase);
                                        Log.d("genki", "onPurchasesUpdated");
                                    }
                                }
                            }
                        }
                ).build();

        connectGooglePlayBilling();

        btnPurchase1.setOnClickListener(v -> {
            clickButton(0);
        });

        btnPurchase2.setOnClickListener(v -> {
            clickButton(1);
        });

        btnPurchase3.setOnClickListener(v -> {
            clickButton(2);
        });


        return builder.create(); // returnを後で入れる
    }

    public void clickButton(Integer num) {
        if (uid.equals("GUEST")) {
            Toast.makeText(getActivity(), "Sign in required to purchase", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        } else if (currentPoint >= 20000) {
            Toast.makeText(getActivity(), "Cannot purchase more than 20,000", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        
        
        try {
            launchPurchaseFlow(productDetailsList.get(num));
        } catch (Exception e) {
            Toast.makeText(dialogLayout.getContext(), "Sorry an error occurred", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    void connectGooglePlayBilling() {

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                connectGooglePlayBilling();
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    showProducts();
                }
            }
        });

    }

    @SuppressLint("SetTextI18n")
    void showProducts() {
        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("plan_a")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("plan_b")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("plan_c")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("plan_d")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
            //Clear the list
            productDetailsList.clear();

            Log.d(TAG, "Size " + list.size());

            //Handler to delay by two seconds to wait for google play to return the list of products.
            handler.postDelayed(() -> {
                //Adding new productList, returned from google play
                productDetailsList.addAll(list);

                try {
                    //Since we have one product, we use index zero (0) from list
                    btnPurchase1.setText(list.get(0).getOneTimePurchaseOfferDetails().getFormattedPrice());
                    btnPurchase2.setText(list.get(1).getOneTimePurchaseOfferDetails().getFormattedPrice());
                    btnPurchase3.setText(list.get(2).getOneTimePurchaseOfferDetails().getFormattedPrice());

                    Bundle bundle = new Bundle();
                    bundle.putString("btnPurchase1", btnPurchase1.getText().toString());
                    bundle.putString("btnPurchase2", btnPurchase2.getText().toString());
                    bundle.putString("btnPurchase3", btnPurchase3.getText().toString());
                    mFirebaseAnalytics.logEvent("purchase_dialog", bundle);
                } catch (Exception e){
                    btnPurchase1.setText("Click!");
                    btnPurchase2.setText("Click!");
                    btnPurchase3.setText("Click!");
                }

            }, 200);
        });
    }

    void launchPurchaseFlow(ProductDetails productDetails) {
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();
        billingClient.launchBillingFlow(getActivity(), billingFlowParams);
    }

    void verifyPurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        ConsumeResponseListener listener = (billingResult, s) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                giveUserCoins(purchase);
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
    }


    public void findView() {
        btnPurchase1 = (Button) dialogLayout.findViewById(R.id.purchase_plan_a);
        btnPurchase2 = (Button) dialogLayout.findViewById(R.id.purchase_plan_b);
        btnPurchase3 = (Button) dialogLayout.findViewById(R.id.purchase_plan_c);
    }

    @SuppressLint("SetTextI18n")
    void giveUserCoins(Purchase purchase) {
        //set coins
//        prefs.setInt("coins",(coins.get(0) * purchase.getQuantity()) + prefs.getInt("coins",0));
        //Update UI
//        clicks.setText("You have "+prefs.getInt("coins",0)+ " coins(s)");
        Log.d("genki", "test | getQuantity() | "+purchase.getQuantity());
        Log.d("genki", "test | getPurchaseState() | "+purchase.getPurchaseState());
        Log.d("genki", "test | getOrderId() | "+purchase.getOrderId());
        Log.d("genki", "test | getPurchaseToken() | "+purchase.getPurchaseToken());
        Log.d("genki", "test | getPurchaseTime() | "+purchase.getPurchaseTime());
        Log.d("genki", "test | getPackageName() | "+purchase.getPackageName());
        Log.d("genki", "test | getSignature() | "+purchase.getSignature());
        Log.d("genki", "test | getProducts() | "+purchase.getProducts());
        iapName = purchase.getProducts().get(0);
        if (iapName.equals("plan_a")) {
            giveWords = 7000;
        } else if (iapName.equals("plan_b")) {
            giveWords = 36000;
        } else if (iapName.equals("plan_c")) {
            giveWords = 100000;
        } else {
            giveWords = 0;
        }
        Log.d("genki", "test | iapName | "+iapName);
        Log.d("genki", "test | giveWords | "+giveWords);

        getPoint();
        updatePurchase(purchase);

    }

    private void getPoint() {
        db.collection("user")
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                point = Integer.parseInt(document.getData().get("point").toString());
                                docId = document.getId();
                            }
                        } else {
                            Log.d("genki", "Error getting documents: ", task.getException());
                        }
                        updatePoint();
                        getActivity().recreate();
                        dismiss();
                    }
                });
    }

    public void updatePurchase(Purchase purchases) {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // Create a new user with a first and last name
        Map<String, Object> purchase = new HashMap<>();
        purchase.put("uid", uid);
        purchase.put("timeStamp", timeStamp);
        purchase.put("orderId", purchases.getOrderId());
        purchase.put("purchaseToken", purchases.getPurchaseToken());
        purchase.put("purchaseTime", purchases.getPurchaseTime());
        purchase.put("iapName", iapName);
        purchase.put("giveWords", giveWords);

        // Add a new document with a generated ID
        db.collection("purchase")
                .add(purchase)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d("genki", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("genki", "Error adding document", e);
                    }
                });
    }

    private void updatePoint() {
        point += giveWords;
        DocumentReference Ref = db.collection("user").document(docId);
        Ref.update(
                "point", point
                )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("genki", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("genki", "Error updating document", e);
                    }
                });
    }



}
