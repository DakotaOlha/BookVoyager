package com.example.bookvoyager.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bookvoyager.Fragment.EditBookDialogFragment;
import com.example.bookvoyager.Fragment.MapFragment;
import com.example.bookvoyager.Fragment.MyBooksFragment;
import com.example.bookvoyager.Fragment.RewardsFragment;
import com.example.bookvoyager.Fragment.SearchFragment;
import com.example.bookvoyager.R;
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
        Menu menu = navView.getMenu();
        setupIconSize(menu.findItem(R.id.ViewBooks), R.drawable.ic_my_book, 40, 40);
        setupIconSize(menu.findItem(R.id.ViewAnalytics), R.drawable.ic_main_search, 34, 34);
        setupIconSize(menu.findItem(R.id.ViewMaps), R.drawable.ic_map, 42, 34);
        setupIconSize(menu.findItem(R.id.ViewReward), R.drawable.ic_awards, 36, 36);



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
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.add_manually).setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);

            if (currentFragment instanceof MyBooksFragment) {
                MyBooksFragment myBooksFragment = (MyBooksFragment) currentFragment;

                new EditBookDialogFragment(null, null, myBooksFragment::loadUserBooks, "Add the book", "add")
                        .show(getSupportFragmentManager(), "EditBook");
            } else {
                replaceFragment(new MyBooksFragment());
                Toast.makeText(this, "Перехід на вкладку 'Мої книги' для додавання", Toast.LENGTH_SHORT).show();
            }
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.add_by_photo).setOnClickListener(v -> {
            startActivity(new Intent(this, ScanISBNActivity.class));
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
}