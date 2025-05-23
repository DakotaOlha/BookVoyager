package com.example.bookvoyager.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.bookvoyager.Class.Book;
import com.example.bookvoyager.Firebase.AddBookCallback;
import com.example.bookvoyager.Firebase.BookLibraryManager;
import com.example.bookvoyager.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class ScanISBNActivity extends AppCompatActivity {
    private static final int BARCODE_CAMERA_REQUEST = 1001;
    private static final Pattern ISBN_PATTERN = Pattern.compile("^(97(8|9))?\\d{9}(\\d|X)$");

    private BookLibraryManager bookLibraryManagerl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_isbn);
        initializeFirebase();
        checkCameraPermission();
    }

    private void initializeFirebase() {
        bookLibraryManagerl = new BookLibraryManager(this);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, BARCODE_CAMERA_REQUEST);
        } else {
            startScanner();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BARCODE_CAMERA_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        } else {
            Toast.makeText(this, "Camera permission it.svg required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startScanner() {
        new IntentIntegrator(this)
                .setPrompt("Scan ISBN barcode")
                .setOrientationLocked(false)
                .setBeepEnabled(true)
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            processScannedCode(result.getContents());
        } else {
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void processScannedCode(String scannedCode) {
        String cleanIsbn = scannedCode.replaceAll("[^0-9X]", "");
        if (ISBN_PATTERN.matcher(cleanIsbn).matches()) {
            fetchBookFromISBN(cleanIsbn);
        } else {
            Toast.makeText(this, "Invalid ISBN format", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void fetchBookFromISBN(String isbn) {
        String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> parseBookData(response, isbn),
                error -> {
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                    finish();
                });

        queue.add(request);
    }

    private void parseBookData(JSONObject response, String isbn) {
        try {
            if (!response.has("items") || response.getJSONArray("items").length() == 0) {
                Toast.makeText(this, "Book not found in Google Books", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            JSONObject item = response.getJSONArray("items").getJSONObject(0);
            JSONObject volumeInfo = item.getJSONObject("volumeInfo");

            String title = volumeInfo.getString("title");
            String authors = volumeInfo.has("authors") ?
                    joinJsonArray(volumeInfo.getJSONArray("authors"), ", ") : "Unknown author";

            String coverUrl = "";
            if (volumeInfo.has("imageLinks")) {
                JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");

                if (imageLinks.has("thumbnail")) {
                    coverUrl = imageLinks.getString("thumbnail");
                } else if (imageLinks.has("smallThumbnail")) {
                    coverUrl = imageLinks.getString("smallThumbnail");
                }

                if (!coverUrl.isEmpty()) {
                    coverUrl = coverUrl.replace("http://", "https://");

                }
            }

            int pageCount = volumeInfo.has("pageCount") ? volumeInfo.getInt("pageCount") : 0;
            String description = volumeInfo.has("description") ? volumeInfo.getString("description") : "No description available";

            String country = "Unknown country";
            if (item.has("saleInfo")) {
                JSONObject saleInfo = item.getJSONObject("saleInfo");
                if (saleInfo.has("country")) {
                    country = saleInfo.getString("country");
                }
            }

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm:ss a z", Locale.US);
            String addedDate = sdf.format(new Date());

            saveBookToFirestore(title, authors, coverUrl, isbn, addedDate, pageCount, description, country);

        } catch (JSONException e) {
            Toast.makeText(this, "Error parsing data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private String joinJsonArray(JSONArray array, String delimiter) throws JSONException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            if (i > 0) result.append(delimiter);
            result.append(array.getString(i));
        }
        return result.toString();
    }

    private void saveBookToFirestore(String title, String authors, String coverUrl,
                                     String isbn, String addedDate, int pageCount, String description, String country) {
        Book book = new Book(isbn, title, authors, coverUrl);
        book.setCountry(country);
        book.setPageCount(pageCount);
        book.setDescription(description);

        bookLibraryManagerl.saveNewBookToFirestore(book, new AddBookCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ScanISBNActivity.this, "Book added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ScanISBNActivity.this, "Failed to add book: " + errorMessage, Toast.LENGTH_LONG).show();
                finish();
            }
        });
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        HashMap<String, Object> book = new HashMap<>();
//        book.put("title", title);
//        book.put("authors", authors);
//        book.put("coverUrl", coverUrl);
//        book.put("isbn", isbn);
//        book.put("addedDate", addedDate);
//        book.put("pageCount", pageCount);
//        book.put("description", description);
//        book.put("country", country);
//
//        System.out.println("Saving book with coverUrl: " + coverUrl);
//
//        db.collection("users")
//                .document(userId)
//                .collection("books")
//                .add(book)
//                .addOnSuccessListener(documentReference -> {
//
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Failed to add book: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                    e.printStackTrace();
//                    finish();
//                });
//
//        Map<String, Object> sessions = new HashMap<>();
//        sessions.put("title", title);
//        sessions.put("pagesRead", 0);
//        sessions.put("pagesCount", pageCount);
//
//        db.collection("users")
//                .document(userId)
//                .collection("readingSessions")
//                .document()
//                .set(sessions);
//
//        db.collection("users")
//                .document(userId)
//                .collection("locationSpot")
//                .whereEqualTo("locationId", country)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            long currentCount = document.getLong("countRequiredBooks") != null
//                                    ? document.getLong("countRequiredBooks")
//                                    : 0;
//
//                            db.collection("users")
//                                    .document(userId)
//                                    .collection("locationSpot")
//                                    .document(document.getId())
//                                    .update("countRequiredBooks", currentCount + 1);
//                        }
//                    } else {
//                        Map<String, Object> countryData = new HashMap<>();
//                        countryData.put("locationId", country);
//                        countryData.put("countRequiredBooks", 1);
//                        countryData.put("ifUnlocked", true);
//
//                        db.collection("users")
//                                .document(userId)
//                                .collection("locationSpot")
//                                .document()
//                                .set(countryData);
//                    }
//                });
    }
}