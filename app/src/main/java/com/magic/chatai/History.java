package com.magic.chatai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    View dialogLayout;
    String uid, iUID;
    RecyclerView recyclerView;
    Button btnBack;

    List<String> iDocId = new ArrayList<>();
    List<String> iQuestion = new ArrayList<>();
    List<String> iAnswer = new ArrayList<>();
    List<String> iAnswerWords = new ArrayList<>();
    List<String> iTimeStamp = new ArrayList<>();
    List<String> iFlagHeader = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
        findView();
        btnReturn();

        if (uid.equals("GUEST")) {
            return;
        }

        try {
            db.collection("transaction")
                    .whereEqualTo("uid", uid)
//                    .orderBy("timeStamp", Query.Direction.DESCENDING)
                    .limit(30)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    iUID = document.getData().get("uid").toString();
                                    if (iUID.equals(uid)) {
                                        iDocId.add(document.getId());
                                        iQuestion.add(document.getData().get("question").toString());
                                        iAnswer.add(document.getData().get("answer").toString());
                                        iAnswerWords.add(document.getData().get("answerWords").toString());
                                        iTimeStamp.add(document.getData().get("timeStamp").toString());
                                        iFlagHeader.add(document.getData().get("flagHeader").toString());
                                    }
                                }
                                if (iQuestion.isEmpty()) {
                                    return;
                                } else {
                                    HistoryAdapter adapter = new HistoryAdapter(iDocId, iQuestion, iAnswer, iAnswerWords, iTimeStamp, iFlagHeader);
                                    recyclerView.setAdapter(adapter);
                                }
                            } else {
                                Log.d("genki", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.d("genki", "Error getting history");
        }

    }

    public void btnReturn() {
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    public void findView() {
        recyclerView = (RecyclerView) findViewById(R.id.history_recycler_view);
        btnBack = (Button) findViewById(R.id.history_back);
        Intent intent = getIntent();
        uid = intent.getStringExtra("UID");
    }
}
