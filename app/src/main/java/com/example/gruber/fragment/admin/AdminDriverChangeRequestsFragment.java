package com.example.gruber.fragment.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gruber.R;
import com.example.gruber.adapter.AdminDriverChangeRequestAdapter;
import com.example.gruber.services.UserService;
import com.example.gruber.services.callbacks.AuthCallback;
import com.example.gruber.models.enums.UserRole;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminDriverChangeRequestsFragment extends Fragment {

    @Inject
    UserService userService;

    private AdminDriverChangeRequestAdapter adapter;
    private TextView txtEmpty;

    public AdminDriverChangeRequestsFragment() {
        super(R.layout.fragment_admin_driver_change_requests);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rvChangeRequests);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        adapter = new AdminDriverChangeRequestAdapter(new AdminDriverChangeRequestAdapter.ChangeRequestActionListener() {
            @Override
            public void onApprove(String driverEmail) {
                userService.approveDriverChangeRequest(driverEmail, new AuthCallback() {
                    @Override
                    public void onSuccess(String userId, UserRole role) {
                        Toast.makeText(getContext(), R.string.change_request_approved, Toast.LENGTH_SHORT).show();
                        loadRequests();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Toast.makeText(getContext(), R.string.change_request_action_failed, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }

            @Override
            public void onReject(String driverEmail) {
                userService.rejectDriverChangeRequest(driverEmail, new AuthCallback() {
                    @Override
                    public void onSuccess(String userId, UserRole role) {
                        Toast.makeText(getContext(), R.string.change_request_rejected, Toast.LENGTH_SHORT).show();
                        loadRequests();
                    }

                    @Override
                    public void onError(Throwable error) {
                        Toast.makeText(getContext(), R.string.change_request_action_failed, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        userService.getPendingChangeRequests(this::applyRequests);
    }

    private void applyRequests(List<Map<String, Object>> requests) {
        if (adapter != null) {
            adapter.submitList(requests);
        }
        if (txtEmpty != null) {
            txtEmpty.setVisibility(requests == null || requests.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}
