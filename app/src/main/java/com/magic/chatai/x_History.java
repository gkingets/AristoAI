package com.magic.chatai;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class x_History extends Fragment {
    private final String TAG = "genki";
    View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.x_history, container, false);

        BottomNavigationView navigationView = getActivity().findViewById(R.id.bottom_topic);
        navigationView.getMenu().getItem(1).setChecked(true);

        setRecyclerView();
        return view;
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        String currentUserUid = x_Utils.getUid(getContext());
        x_UtilsFirestore firestoreUtils = new x_UtilsFirestore();

        // Assuming your Firestore collection name is "posts" and the document ID is the user's UID
        String collectionName = "transactions";

        firestoreUtils.getPostByUid(collectionName, currentUserUid)
                .addOnSuccessListener(posts -> {
                    // Bind the data to the RecyclerView
                    x_PostAdapter adapter = new x_PostAdapter(posts);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Log.e(TAG, "Error fetching posts: " + e.getMessage());
                });
    }
}
