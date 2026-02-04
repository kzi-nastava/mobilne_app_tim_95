package com.example.gruber.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gruber.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ReportDialogFragment extends DialogFragment {

    public interface ReportCallback {
        void onSubmit(String note);
    }

    private ReportCallback callback;

    public ReportDialogFragment(ReportCallback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_report_dialog, container, false);

        TextInputEditText etNote = view.findViewById(R.id.et_report_note);

        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);
        MaterialButton btnSubmit = view.findViewById(R.id.btn_submit);

        // Cancel closes dialog
        btnCancel.setOnClickListener(v -> dismiss());

        // Submit calls callback and closes dialog
        btnSubmit.setOnClickListener(v -> {
            String note = etNote.getText() != null
                    ? etNote.getText().toString()
                    : "";

            if (callback != null) {
                callback.onSubmit(note);
            }

            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

}
