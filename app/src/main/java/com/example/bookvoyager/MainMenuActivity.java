package com.example.bookvoyager;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bookvoyager.databinding.ActivityMainMenuBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainMenuActivity extends AppCompatActivity {

    ActivityMainMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(binding.getRoot());

        replaceFragment(new MyBooksFragment());

        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.ViewBooks){
                replaceFragment(new MyBooksFragment());
            }
            else if(id == R.id.ViewReward){
                replaceFragment(new RewardsFragment());
            }
            else if(id == R.id.ViewMaps){
                replaceFragment(new MapFragment());
            }

            return true;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Toast.makeText(MainMenuActivity.this, "is Successful", Toast.LENGTH_SHORT).show();
        });

        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);
        Menu menu =  navView.getMenu();
        setupIconSize(menu.findItem(R.id.ViewBooks), R.drawable.my_book_icon, 40, 40);
        setupIconSize(menu.findItem(R.id.ViewAnalytics), R.drawable.analytics_icon, 40, 29);
        setupIconSize(menu.findItem(R.id.ViewMaps), R.drawable.map_iocn, 42, 34);
        setupIconSize(menu.findItem(R.id.ViewReward), R.drawable.awards_icon, 36, 36);

    }

    private void setupIconSize(MenuItem item, int iconRes, int widthDp, int heightDp) {

        int widthPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, widthDp, getResources().getDisplayMetrics());
        int heightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, heightDp, getResources().getDisplayMetrics());

        ImageView iconView = (ImageView) LayoutInflater.from(this)
                .inflate(R.layout.nav_icon, null);
        iconView.setImageResource(iconRes);
        iconView.setLayoutParams(new ViewGroup.LayoutParams(widthPx, heightPx));

        item.setActionView(iconView);
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

//    private void fullScreen(){
//        Window window = getWindow();
//        WindowCompat.setDecorFitsSystemWindows(window, false);
//        window.setStatusBarColor(Color.TRANSPARENT);
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            window.setDecorFitsSystemWindows(false);
//        } else {
//            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        }
//    }
}