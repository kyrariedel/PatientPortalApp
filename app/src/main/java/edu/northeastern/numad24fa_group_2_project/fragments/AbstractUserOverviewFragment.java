package edu.northeastern.numad24fa_group_2_project.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.northeastern.numad24fa_group_2_project.repositories.TaskRepository;
import edu.northeastern.numad24fa_group_2_project.utility.UserSessionHelper;

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
