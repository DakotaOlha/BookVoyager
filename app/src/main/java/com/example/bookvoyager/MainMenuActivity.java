package com.example.bookvoyager;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
            else if(id == R.id.ViewAnalytics){
                replaceFragment(new SearchFragment());
            }

            return true;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            showAddBookMenu(view);
        });

        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);
        Menu menu =  navView.getMenu();
        setupIconSize(menu.findItem(R.id.ViewBooks), R.drawable.my_book_icon, 40, 40);
        setupIconSize(menu.findItem(R.id.ViewAnalytics), R.drawable.main_search_icon, 34, 34);
        setupIconSize(menu.findItem(R.id.ViewMaps), R.drawable.map_iocn, 42, 34);
        setupIconSize(menu.findItem(R.id.ViewReward), R.drawable.awards_icon, 36, 36);



    }

    private void showAddBookMenu(View anchor){

        View popupView = LayoutInflater.from(this).inflate(R.layout.custom_add_book_menu, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setAnimationStyle(R.style.PopupSlideUpAnimation);

        popupView.findViewById(R.id.add_by_isbn).setOnClickListener(v -> {
            replaceFragment(new SearchFragment());
            Toast.makeText(this, "Add by name", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.add_manually).setOnClickListener(v -> {
            Toast.makeText(this, "Add manually selected", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.add_by_photo).setOnClickListener(v -> {
            Toast.makeText(this, "Add by photo selected", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight = popupView.getMeasuredHeight();

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int anchorX = location[0];
        int anchorY = location[1];

        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        int xOffset = (screenWidth - popupWidth) / 2;
        int yOffset = anchorY - popupHeight;

        popupWindow.setElevation(10);
        popupWindow.showAtLocation(anchor, 0, xOffset, yOffset);
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