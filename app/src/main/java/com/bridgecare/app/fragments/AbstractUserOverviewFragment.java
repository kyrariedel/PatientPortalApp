package com.bridgecare.app.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bridgecare.app.repositories.TaskRepository;
import com.bridgecare.app.utility.UserSessionHelper;

public abstract class AbstractUserOverviewFragment extends Fragment {

    protected UserSessionHelper userSessionHelper;
    protected TaskRepository taskRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userSessionHelper = new UserSessionHelper(requireContext());

        if (!userSessionHelper.isUserLoggedIn()) {
            requireActivity().finish();
            return;
        }

        initDatabase();
    }

    protected void initDatabase() {
        taskRepository = new TaskRepository();
    }

    protected abstract void initViews(View rootView);

    protected abstract void setupListeners();
}
