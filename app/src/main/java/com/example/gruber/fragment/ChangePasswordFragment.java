package com.example.gruber.fragment;

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

public class ChangePasswordFragment extends Fragment {

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

        btnChange.setOnClickListener(v -> {
            String oldPass = etOld.getText().toString();
            String newPass = etNew.getText().toString();
            String confirm = etConfirm.getText().toString();

            txtError.setVisibility(View.GONE);

            if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                showError(txtError, "Sva polja su obavezna.");
                return;
            }

            if (newPass.length() < 8) {
                showError(txtError, "Nova lozinka mora imati najmanje 8 karaktera.");
                return;
            }

            if (!newPass.equals(confirm)) {
                showError(txtError, "Nova lozinka i potvrda se ne poklapaju.");
                return;
            }

            // Fake success
            Toast.makeText(getContext(), "Lozinka je uspešno promenjena", Toast.LENGTH_SHORT).show();

            NavHostFragment.findNavController(ChangePasswordFragment.this)
                    .popBackStack();
        });
    }

    private void showError(TextView txt, String msg) {
        txt.setText(msg);
        txt.setVisibility(View.VISIBLE);
    }
}
