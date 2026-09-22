package edu.northeastern.numad24fa_group_2_project.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad24fa_group_2_project.adapters.AppointmentAdapter;
import edu.northeastern.numad24fa_group_2_project.models.Appointment;

public abstract class AbstractUserAppointmentsFragment extends Fragment {

    protected RecyclerView recyclerView;
    protected AppointmentAdapter adapter;
    protected List<Appointment> appointmentList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(appointmentList);
    }

    protected void initRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(getRecyclerViewId());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    protected abstract int getRecyclerViewId();
    protected abstract void fetchAppointments();
    protected void refreshAppointments(List<Appointment> newAppointments) {
        appointmentList.clear();
        appointmentList.addAll(newAppointments);
        adapter.notifyDataSetChanged();
    }
}
