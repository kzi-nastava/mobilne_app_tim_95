package com.example.gruber.fragment.admin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

import com.example.gruber.R;
import com.example.gruber.viewModels.AdminDriverRegistrationViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;

public class AdminDriverRegisterNameFragment extends Fragment {

    private AdminDriverRegistrationViewModel viewModel;

    private TextInputEditText etFirstName;
    private TextInputLayout tilFirstName;
    private TextInputEditText etLastName;
    private TextInputLayout tilLastName;
    private TextInputEditText etPhone;
    private TextInputLayout tilPhone;
    private ImageView profileImage;

    private ActivityResultLauncher<String> pickImageLauncher;

    public AdminDriverRegisterNameFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> processImage(uri)
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register_name, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminDriverRegistrationViewModel.class);

        tilFirstName = view.findViewById(R.id.til_reg_first_name);
        tilLastName = view.findViewById(R.id.til_reg_last_name);
        tilPhone = view.findViewById(R.id.til_reg_phone);

        etFirstName = view.findViewById(R.id.et_reg_first_name);
        etLastName = view.findViewById(R.id.et_reg_last_name);
        etPhone = view.findViewById(R.id.et_reg_phone);
        profileImage = view.findViewById(R.id.imageView);

        view.findViewById(R.id.btn_reg_add_image).setOnClickListener(v -> onAddImageClicked());
        view.findViewById(R.id.btn_reg_previous_name).setOnClickListener(v -> onPreviousClicked());
        view.findViewById(R.id.btn_reg_next_name).setOnClickListener(v -> onNextClicked());

        byte[] existingBytes = viewModel.getPhotoBytes().getValue();
        if (existingBytes != null && existingBytes.length != 0) {
            Bitmap existingBitmap = bytesToBitmap(existingBytes);
            if (existingBitmap != null) {
                profileImage.setImageBitmap(existingBitmap);
            }
        }

        return view;
    }

    private void onNextClicked() {
        clearErrors();

        String firstName = etFirstName.getText() != null ? etFirstName.getText().toString() : "";
        String lastName = etLastName.getText() != null ? etLastName.getText().toString() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString() : "";

        if (!isValid(firstName, lastName, phone)) return;

        viewModel.setFirstName(firstName);
        viewModel.setLastName(lastName);
        viewModel.setPhone(phone);

        findNavController(this).navigate(R.id.action_adminDriverRegisterNameFragment_to_adminDriverRegisterAddressFragment);
    }

    private void onPreviousClicked() {
        String firstName = etFirstName.getText() != null ? etFirstName.getText().toString() : "";
        String lastName = etLastName.getText() != null ? etLastName.getText().toString() : "";

        viewModel.setFirstName(firstName);
        viewModel.setLastName(lastName);
        findNavController(this).navigateUp();
    }

    private void onAddImageClicked() {
        pickImageLauncher.launch("image/*");
    }

    private void processImage(android.net.Uri imageUri) {
        if (imageUri == null) {
            return;
        }
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
            Bitmap scaledBitmap = scaleBitmap(bitmap, 200, 200);
            byte[] imageBytes = bitmapToBytes(scaledBitmap);
            viewModel.setPhotoBytes(imageBytes);
            if (profileImage != null) {
                profileImage.setImageBitmap(scaledBitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    private Bitmap bytesToBitmap(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private boolean isValid(String firstName, String lastName, String phone) {
        boolean valid = true;

        if (firstName.trim().isEmpty()) {
            tilFirstName.setError("First name must not be empty");
            valid = false;
        }
        if (lastName.trim().isEmpty()) {
            tilLastName.setError("Last name must not be empty");
            valid = false;
        }
        if (!Patterns.PHONE.matcher(phone).matches()) {
            tilPhone.setError("Please enter a valid phone number");
            valid = false;
        }
        return valid;
    }

    private void clearErrors() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilPhone.setError(null);
    }
}
