package com.example.gruber.fragment.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.gruber.R;
import com.example.gruber.services.UserService;
import com.example.gruber.services.callbacks.AuthCallback;
import com.example.gruber.models.enums.UserRole;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChangePasswordFragment extends Fragment {

    @Inject
    UserService userService;

    public ChangePasswordFragment() {
        super(R.layout.fragment_change_password);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etOld = view.findViewById(R.id.etOldPassword);
        EditText etNew = view.findViewById(R.id.etNewPassword);
        EditText etConfirm = view.findViewById(R.id.etConfirmPassword);
        TextView txtError = view.findViewById(R.id.txtError);
        Button btnChange = view.findViewById(R.id.btnChangePassword);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnChange.setOnClickListener(v -> {
            String oldPass = etOld.getText().toString();
            String newPass = etNew.getText().toString();
            String confirm = etConfirm.getText().toString();

            txtError.setVisibility(View.GONE);

            if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                showError(txtError, "All fields are required.");
                return;
            }

            if (newPass.length() < 8) {
                showError(txtError, "New password must be at least 8 characters long.");
                return;
            }

            if (!newPass.equals(confirm)) {
                showError(txtError, "New and Confirm password fields don't match.");
                return;
            }

            btnChange.setEnabled(false);
            btnChange.setText(R.string.changing);

            userService.changePassword(oldPass, newPass, new AuthCallback() {
                @Override
                public void onSuccess(String userId, UserRole role) {
                    Toast.makeText(getContext(), "Password changed successfully.", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(ChangePasswordFragment.this).popBackStack();
                }

                @Override
                public void onError(Throwable error) {
                    btnChange.setEnabled(true);
                    btnChange.setText(R.string.change_password);
                    String errorMsg = error.getMessage();
                    if (errorMsg != null && errorMsg.contains("wrong password")) {
                        showError(txtError, "Current password is incorrect.");
                    } else if (errorMsg != null && errorMsg.contains("too many requests")) {
                        showError(txtError, "Too many failed attempts. Try again later.");
                    } else {
                        showError(txtError, "Error: " + (errorMsg != null ? errorMsg : "Unknown error"));
                    }
                }
            });
        });

        btnCancel.setOnClickListener(v -> NavHostFragment.findNavController(ChangePasswordFragment.this)
                .navigate(R.id.action_changePasswordFragment_to_settingsFragment));
    }

    private void showError(TextView txt, String msg) {
        txt.setText(msg);
        txt.setVisibility(View.VISIBLE);
    }
}
