package com.magic.chatai;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.billingclient.api.Purchase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class x_UtilsFirestore {
    final String TAG = "genki";
    final String COLLECTION_NAME = "transactions";
    final String POINT = "_point";
    final String CREATE_DATE = "_createDate";
    final String ARRAY_FILED_NAME = "post";
    final int INITIAL_POINT = 5;
    final String POST_QUESTION = "question";
    final String POST_ANSWER = "answer";
    final String POST_TIME_STAMP = "timeStamp";
    final String POST_UID = "uid";
    final String POST_STYLE = "style";
    final String TERMS_OF_SERVICE = "termsOfServiceSelected";
    final int TERMS_OF_SERVICE_SELECTED = 1;
    final String PRIVACY_POLICY = "privacyPolicySelected";
    final int PRIVACY_POLICY_SELECTED = 1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Void> createUser(String documentId) {
        CollectionReference collection = db.collection(COLLECTION_NAME);
        DocumentReference document = collection.document(documentId);

        // CREATE_DATEとPOINTのフィールドを追加
        Map<String, Object> data = new HashMap<>();
        data.put(CREATE_DATE, x_Utils.getCurrentTimestamp());
        data.put(POINT, INITIAL_POINT);
        data.put(TERMS_OF_SERVICE, TERMS_OF_SERVICE_SELECTED);
        data.put(PRIVACY_POLICY, PRIVACY_POLICY_SELECTED);

        // ドキュメントにデータをセット
        return document.set(data);
    }


    public Task<Void> addMapToDocumentArray(String documentId, Map<String, Object> data) {
        CollectionReference collection = db.collection(COLLECTION_NAME);
        DocumentReference document = collection.document(documentId);

        // ドキュメント内の配列フィールドを取得
        return document.get().continueWithTask(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> postArray = (List<Map<String, Object>>) documentSnapshot.get(ARRAY_FILED_NAME);
                if (postArray == null) {
                    postArray = new ArrayList<>();
                }

                // データを配列に追加
                postArray.add(data);

                // 配列フィールドを更新
                return document.update(ARRAY_FILED_NAME, postArray);
            } else {
                Map<String, Object> newDocumentData = new HashMap<>();
                newDocumentData.put(ARRAY_FILED_NAME, Arrays.asList(data));
                return document.set(newDocumentData);            }
        });
    }

    public Task<List<x_Post>> getPostByUid(String collectionName, String documentId) {
        CollectionReference collection = db.collection(collectionName);
        DocumentReference document = collection.document(documentId);

        return document.get().continueWithTask(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> postArray = (List<Map<String, Object>>) documentSnapshot.get("post");

                if (postArray == null || postArray.isEmpty()) {
                    // Return an empty list if the array is null or empty
                    return Tasks.forResult(new ArrayList<>());
                }

                List<x_Post> posts = new ArrayList<>();
                for (Map<String, Object> postData : postArray) {
                    // Convert each map to x_Post object
                    x_Post post = new x_Post(
                            (String) postData.get(POST_QUESTION),
                            (String) postData.get(POST_ANSWER),
                            (String) postData.get(POST_TIME_STAMP),
                            (String) postData.get(POST_UID),
                            (String) postData.get(POST_STYLE)
                    );
                    posts.add(post);
                }
                Collections.sort(posts, (post1, post2) -> {
                    String timestamp1 = post1.getTimeStamp();
                    String timestamp2 = post2.getTimeStamp();
                    return timestamp2.compareTo(timestamp1);
                });
                return Tasks.forResult(posts);
            } else {
                // Document doesn't exist, return an empty list
                return Tasks.forResult(new ArrayList<>());
            }
        });
    }

    public Task<x_FirestoreData> getFirestoreData(String collectionName, String documentId) {
        CollectionReference collection = db.collection(collectionName);
        DocumentReference document = collection.document(documentId);

        return document.get().continueWithTask(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            if (documentSnapshot.exists()) {
                // Retrieve individual fields from the documentSnapshot
                int point = documentSnapshot.getLong(POINT).intValue();
                String createDate = documentSnapshot.getString(CREATE_DATE);
                List<Map<String, Object>> postArray = (List<Map<String, Object>>) documentSnapshot.get(ARRAY_FILED_NAME);

                if (postArray == null) {
                    postArray = new ArrayList<>();
                }

                // Use the retrieved fields to create a new x_FirestoreData object
                x_FirestoreData firestoreData = new x_FirestoreData(point, createDate, postArray);

                return Tasks.forResult(firestoreData);
            } else {
                // Document doesn't exist, return null or handle accordingly
                return Tasks.forResult(null);
            }
        });
    }

    public static void setPointFromFirestore(Context context, TextView textView) {
        final String TAG = "genki";
        final String COLLECTION_NAME = "transactions";
        final String UID = x_Utils.getUid(context);
        x_UtilsFirestore firestoreUtils = new x_UtilsFirestore();
        firestoreUtils.getFirestoreData(COLLECTION_NAME, UID)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        x_FirestoreData firestoreData = task.getResult();
                        if (firestoreData != null) {
                            textView.setText(""+ firestoreData.getPoint());
                        } else {
                            textView.setText(""+0);
                            Log.d(TAG, "getFirestoreData: DATA NULL");
                        }
                    } else {
                        textView.setText(""+0);
                        Log.d(TAG, "getFirestoreData: ERROR");
                    }
                });
    }

    public Task<Void> updatePoint(String documentId, int currentPoint, int updatePoint) {
        CollectionReference collection = db.collection(COLLECTION_NAME);
        DocumentReference document = collection.document(documentId);

        // Decrement the current point by 1
        int newPoint = Math.max(currentPoint + updatePoint, 0);

        // Update the POINT field in Firestore
        return document.update(POINT, newPoint);
    }

    public void updatePurchase(Purchase purchases, String uid) {
        String timeStamp = x_Utils.getCurrentTimestamp();
        // Create a new user with a first and last name
        Map<String, Object> purchaseMap = new HashMap<>();
        purchaseMap.put("uid", uid);
        purchaseMap.put("timeStamp", timeStamp);
        purchaseMap.put("p_orderId", purchases.getOrderId());
        purchaseMap.put("p_purchaseTime", purchases.getPurchaseTime());
        purchaseMap.put("p_quantity", purchases.getQuantity());
        purchaseMap.put("p_product", purchases.getProducts());
        purchaseMap.put("p_purchaseState", purchases.getPurchaseState());
        db.collection("purchase")
                .add(purchaseMap)
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

}
