package com.bridgecare.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.bridgecare.app.adapters.TaskAdapter;
import com.bridgecare.app.models.Task;

public abstract class AbstractUserTasksFragment extends Fragment {

    protected RecyclerView recyclerView;
    protected TaskAdapter adapter;
    protected List<Task> taskList;
    protected boolean isPhysician = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            isPhysician = getArguments().getBoolean("isPhysician", false);
        }

        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, isPhysician, getContext());
    }

    protected void initRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(getRecyclerViewId());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    protected void fetchTasksFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(getFirebaseTaskPath());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Task taskItem = parseTask(dataSnapshot);
                    if (taskItem != null) {
                        taskList.add(taskItem);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    protected abstract int getRecyclerViewId();

    protected abstract String getFirebaseTaskPath();

    protected abstract Task parseTask(DataSnapshot snapshot);
}
