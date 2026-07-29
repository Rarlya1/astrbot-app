package com.astrbot.app;
import com.android.astrbot.R;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (navHost != null) {
            navController = navHost.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp();
    }
}
