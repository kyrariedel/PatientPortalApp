package com.bridgecare.app.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.bridgecare.app.R;
import com.bridgecare.app.adapters.AppointmentAdapter;
import com.bridgecare.app.models.Appointment;
import com.bridgecare.app.utility.FirebaseListenerRegistrar;

public abstract class AbstractUserAppointmentsFragment extends Fragment {

    protected RecyclerView recyclerView;
    protected TextView emptyStateText;
    protected AppointmentAdapter adapter;
    protected List<Appointment> appointmentList;
    protected final FirebaseListenerRegistrar listenerRegistrar = new FirebaseListenerRegistrar();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(appointmentList);
    }

    protected void initRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(getRecyclerViewId());
        emptyStateText = rootView.findViewById(R.id.emptyStateText);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        updateEmptyState();
    }

    protected abstract int getRecyclerViewId();
    protected abstract void fetchAppointments();

    protected void refreshAppointments(List<Appointment> newAppointments) {
        appointmentList.clear();
        appointmentList.addAll(newAppointments);
        notifyAppointmentsChanged();
    }

    protected void notifyAppointmentsChanged() {
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    protected void updateEmptyState() {
        boolean isEmpty = appointmentList == null || appointmentList.isEmpty();
        if (emptyStateText != null) {
            emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        listenerRegistrar.removeAll();
    }

    @Override
    public void onDestroyView() {
        listenerRegistrar.removeAll();
        super.onDestroyView();
    }
}
