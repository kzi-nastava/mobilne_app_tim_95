package com.example.gruber.fragment.registration;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

import com.example.gruber.R;
import com.example.gruber.viewModels.AccountViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;

public class RegisterNameFragment extends Fragment {

    private AccountViewModel accountViewModel;

    private TextInputEditText etFirstName;
    private TextInputLayout tilFirstName;
    private TextInputEditText etLastName;
    private TextInputLayout tilLastName;
    private TextInputEditText etPhone;
    private TextInputLayout tilPhone;
    private ShapeableImageView imageView;

    private ActivityResultLauncher<String> pickImageLauncher;

    public RegisterNameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Modern image picker (replaces startActivityForResult/onActivityResult)
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::processImage
        );
        //{
//                    if (uri != null) {
//
//                        accountViewModel.setImage(uri.toString());
//                        // If you have an ImageView, you can set it here too.
//                        // ivProfilePhoto.setImageURI(uri);
//                    }
//                }
//        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register_name, container, false);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        tilFirstName = view.findViewById(R.id.til_reg_first_name);
        tilLastName = view.findViewById(R.id.til_reg_last_name);
        tilPhone = view.findViewById(R.id.til_reg_phone);

        etFirstName = view.findViewById(R.id.et_reg_first_name);
        etFirstName.setText(accountViewModel.getFirstName().getValue());

        etLastName = view.findViewById(R.id.et_reg_last_name);
        etLastName.setText(accountViewModel.getLastName().getValue());

        etPhone = view.findViewById(R.id.et_reg_phone);
        etPhone.setText(accountViewModel.getPhone().getValue());

        imageView = view.findViewById(R.id.imageView);
        Bitmap imageBitmap = bytesToBitmap(accountViewModel.getImage().getValue());
        if (imageBitmap != null) imageView.setImageBitmap(imageBitmap);

        view.findViewById(R.id.btn_reg_add_image).setOnClickListener(v -> onAddImageClicked());
        view.findViewById(R.id.btn_reg_previous_name).setOnClickListener(v -> onPreviousClicked());
        view.findViewById(R.id.btn_reg_next_name).setOnClickListener(v -> onNextClicked());

        return view;
    }

    private void onNextClicked() {
        clearErrors();

        String firstName = etFirstName.getText() != null ? etFirstName.getText().toString() : "";
        String lastName = etLastName.getText() != null ? etLastName.getText().toString() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString() : "";

        if (!isValid(firstName, lastName, phone)) return;

        accountViewModel.setFirstName(firstName);
        accountViewModel.setLastName(lastName);
        accountViewModel.setPhone(phone);

        // Navigate to address step via nav_guest action
        findNavController(this).navigate(R.id.action_registerNameFragment_to_registerAddressFragment);
    }

    private void onPreviousClicked() {
        // Back within nav graph
        String firstName = etFirstName.getText() != null ? etFirstName.getText().toString() : "";
        String lastName = etLastName.getText() != null ? etLastName.getText().toString() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString() : "";

        accountViewModel.setFirstName(firstName);
        accountViewModel.setLastName(lastName);
        accountViewModel.setPhone(phone);
        findNavController(this).navigateUp();
    }

    private void onAddImageClicked() {
        // Launch system picker for images
        pickImageLauncher.launch("image/*");

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
            tilPhone.setError("Please enter your phone number");
            valid = false;
        }
        return valid;
    }

    private void clearErrors() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
    }

    private void processImage(Uri imageUri) {
        if (imageUri != null) {
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);

                Bitmap scaledBitmap = scaleBitmap(bitmap, 200, 200);

                byte[] imageBytes = bitmapToBytes(scaledBitmap);

                accountViewModel.setImage(imageBytes);

//                ImageView imageView = requireView().findViewById(R.id.imageView);
                imageView.setImageBitmap(scaledBitmap);

            } catch (Exception e ) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = Math.min((float) maxWidth/width, (float) maxHeight/height);
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

}
