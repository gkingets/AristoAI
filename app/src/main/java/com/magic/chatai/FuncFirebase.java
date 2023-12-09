package com.magic.chatai;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FuncFirebase {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addDataBaseTransaction(String uid, String question, String answer,
                                       double questionWords, Integer answerWords, Integer point, String flagHeader) {

        String collectionFlag;

        if (uid.equals("GUEST")) {
            collectionFlag = "transaction_guest";
        } else {
            collectionFlag = "transaction";
        }

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        long tempResult = (long) Math.pow(10, 14); // 10の14乗を計算
        long timeStampValue = Long.parseLong(timeStamp); // timeStampをlong型に変換
        long tempTimeStamp = tempResult - timeStampValue;
        String timeStampDocId = tempTimeStamp + "-" + uid.substring(0,5);

        // Create a new user with a first and last name
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("uid", uid);
        transaction.put("timeStamp", timeStamp);
        transaction.put("question", question);
        transaction.put("answer", answer);
        transaction.put("questionWords", questionWords);
        transaction.put("answerWords", answerWords);
        transaction.put("point", point);
        transaction.put("flagHeader", flagHeader);

        DocumentReference documentRef = db.collection(collectionFlag).document(timeStampDocId);
        documentRef.set(transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // Log.d("genki", "DocumentSnapshot added with ID: " + documentReference.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("genki", "Error adding document", e);
            }
        });

//        // Add a new document with a generated ID
//        db.collection(collectionFlag)
//                .add(transaction)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
////                        Log.d("genki", "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w("genki", "Error adding document", e);
//                    }
//                });
    }

    public void updateReuse(String uid) {
        final Integer[] reuse = new Integer[1];
        db.collection("user").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                reuse[0] =  Integer.parseInt(document.getData().get("reuse").toString());
                                Integer reusePlus1 = reuse[0] + 1;

                                DocumentReference Ref = db.collection("user").document(uid);
                                Ref.update("reuse", reusePlus1)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("genki", "updateReuse() | DocumentSnapshot successfully updated!");
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
                    }
                });
    }

    public void losePoint(Integer answerWords, Integer point, String uid) {
        Integer remainingPoint;
        if (point - answerWords < 0) {
            remainingPoint = 0;
        } else {
            remainingPoint = point - answerWords;
        }
        DocumentReference Ref = db.collection("user").document(uid);
        Ref.update("point", remainingPoint)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("genki", "losePoint() | DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("genki", "Error updating document", e);
                    }
                });
    }

    public void createUser(String uid) {

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("registerDate", timeStamp);
        user.put("loginDate", "");
        user.put("lastFailure", "");
        user.put("point", 1000);
        user.put("termsOfUseSelected", 1);
        user.put("privacyPolicySelected", 1);
        user.put("reuse", 0);

        db.collection("user").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // 既存のユーザーの場合、loginDateを更新する
                            DocumentReference Ref = db.collection("user").document(uid);
                            Ref.update("loginDate", timeStamp)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("genki", "createUser() | DocumentSnapshot successfully updated!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("genki", "Error updating document", e);
                                        }
                                    });
                        } else {
                            // 新規ユーザーの場合、ポイントを付与して追加
                            db.collection("user").document(uid)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        // 成功時の処理

                                    })
                                    .addOnFailureListener(e -> {
                                        // 失敗時の処理

                                    });
                        }
                    } else {
                        // 失敗時の処理
                    }
                });
    }



    public Integer getUserPoint(String uid) {
        final Integer[] point = {91};
        // DBからuidとtimestampをもとにデータを取得
        db.collection("user")
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                point[0] = Integer.parseInt(document.getData().get("point").toString());
                            }
                        } else {
                            Log.d("genki", "Error getting documents: ", task.getException());
                        }
                    }
                });
        return point[0];
    }




}
