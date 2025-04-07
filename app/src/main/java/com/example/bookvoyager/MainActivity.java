package com.example.bookvoyager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fullScreen();

        Button account_button = findViewById(R.id.account_button);

        account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        ImageView search_btn = findViewById(R.id.search_image);

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(MainActivity.this, "search", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        ImageView reward_button = findViewById(R.id.reward_image);

        reward_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RewardActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.bookRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 колонки

        books = new ArrayList<>();
        bookAdapter = new BookAdapter(books);
        recyclerView.setAdapter(bookAdapter);

        loadBooks();

        EditText findBookText = findViewById(R.id.findBook);
        ImageView searchBtn= findViewById(R.id.search_image);

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = findBookText.getText().toString().toLowerCase().trim();

                if(query.isEmpty()){
                    bookAdapter.FilterList(books);
                    return;
                }

                List<Book> filteredBooks = new ArrayList<>();

                for(Book bk: books){
                    if(bk.getTitle().toLowerCase().contains(query) || bk.getAuthor().toLowerCase().contains(query)){
                        filteredBooks.add(bk);
                    }
                }

                bookAdapter.FilterList(filteredBooks);
            }
        });

    }


    private void loadBooks() {
        books.add(new Book("Книга 1", "Автор 1", R.drawable.agata));
        books.add(new Book("Книга 2", "Автор 2", R.drawable.agata));
        books.add(new Book("Книга 3", "Автор 3", R.drawable.agata));
        books.add(new Book("Книга 4", "Автор 4", R.drawable.agata));
        books.add(new Book("Книга 1", "Автор 3", R.drawable.agata));
        books.add(new Book("Книга 2", "Автор 2", R.drawable.agata));
        books.add(new Book("Книга 3", "Автор 1", R.drawable.agata));
        books.add(new Book("Книга 4", "Автор 4", R.drawable.agata));
        bookAdapter.notifyDataSetChanged();
    }

    private void fullScreen(){
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }
}