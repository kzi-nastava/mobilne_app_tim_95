package com.example.gruber.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.gruber.R;
import com.example.gruber.models.FakeSession;
import com.example.gruber.models.enums.UserRole;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}
