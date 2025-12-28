package com.example.gruber.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.gruber.R;
import com.example.gruber.fragment.RegisterAccountFragment;
import com.example.gruber.fragment.RegisterAddressFragment;
import com.example.gruber.models.Address;
import com.example.gruber.viewModels.AccountViewModel;

public class RegistrationActivity extends AppCompatActivity implements RegisterAddressFragment.OnAddressSubmitted {

    private AccountViewModel accountViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        accountViewModel = new ViewModelProvider(this)
                .get(AccountViewModel.class);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.registration_fragment_container, new RegisterAccountFragment())
                    .commit();
        }
    }

    @Override
    public void onAddressSubmitted(Address address) {
        accountViewModel.setAddress(address);
        //Intent intent = new Intent(...)
    }
}