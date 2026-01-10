package com.example.gruber.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.gruber.R;
import com.example.gruber.SessionManager;
import com.example.gruber.models.FakeSession;
import com.example.gruber.models.enums.UserRole;
import com.example.gruber.viewModels.LoginViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoginViewModel loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        loginViewModel.getRole().observe(this, this::onRoleChanged);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);

        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment not found. Check activity_main.xml nav_host id.");
        }

        NavController navController = navHostFragment.getNavController();

        // Pick role (replace with your real session/user)
        UserRole role = FakeSession.currentUser != null
                ? FakeSession.currentUser.getRole()
                : UserRole.GUEST;
        //Inserting dummy data purposes only
//        FakeSession fakeSession = new FakeSession();
//        fakeSession.insertSeed();

        role = UserRole.GUEST;

        int graphRes;
        int menuRes;

        switch (role) {
            case DRIVER:
                graphRes = R.navigation.nav_driver;
                menuRes = R.menu.bottom_nav_driver;
                break;

            case ADMIN:
                graphRes = R.navigation.nav_admin;
                menuRes = R.menu.bottom_nav_admin;
                break;

            case GUEST:
                graphRes = R.navigation.nav_guest;
                menuRes = R.menu.bottom_nav_guest;
                break;

            case USER:
            default:
                graphRes = R.navigation.nav_user;
                menuRes = R.menu.bottom_nav_user;
                break;
        }

        // Set role-specific nav graph
        NavGraph graph = navController.getNavInflater().inflate(graphRes);
        navController.setGraph(graph);

        // Set role-specific bottom menu
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(menuRes);

        // Connect BottomNav with NavController
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    private void onRoleChanged(UserRole role) {
        if (role == null) return;
        NavController navController = Navigation.findNavController(this, R.id.nav_host);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        switch (role) {
            case ADMIN:
                NavGraph graph = navController.getNavInflater().inflate(R.navigation.nav_admin);
                navController.setGraph(graph);
                bottomNav.getMenu().clear();
                bottomNav.inflateMenu(R.menu.bottom_nav_admin);
                NavigationUI.setupWithNavController(bottomNav, navController);
                break;
            case DRIVER:
                navController.setGraph(R.navigation.nav_driver);
                bottomNav.getMenu().clear();
                bottomNav.inflateMenu(R.menu.bottom_nav_driver);
                NavigationUI.setupWithNavController(bottomNav, navController);
                break;
            case USER:
                navController.setGraph(R.navigation.nav_user);
                bottomNav.getMenu().clear();
                bottomNav.inflateMenu(R.menu.bottom_nav_user);
                NavigationUI.setupWithNavController(bottomNav, navController);
                break;

        }
    }
}
