package com.example.bookvoyager;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditBookDialogFragment extends DialogFragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String MODE_ADD = "add";
    private static final String MODE_EDIT = "edit";

    private String mode = MODE_EDIT;
    private Book book;
    private String documentId, dialogTitle;
    private OnBookUpdatedListener listener;
    private Uri imageUri;
    private Bitmap coverBitmap;
    private ImageView imageBookCover;
    TextView dialogTitleView;
    EditText editTitle, editAuthor, editIsbn, editPages, editCountry, editDescription;
    Button saveButton, uploadButton;
    ImageButton backButton;

    public EditBookDialogFragment(Book book, String documentId, OnBookUpdatedListener listener, String dialogTitle, String mode) {
        this.book = book;
        this.documentId = documentId;
        this.listener = listener;
        this.dialogTitle = dialogTitle;
        this.mode = mode;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_book, container, false);
        initializeViews(view);
        loadExistingCover();
        setTextForEditText();
        setupEventListeners();

        return view;
    }

    private void setupEventListeners() {
        uploadButton.setOnClickListener(v -> openImageChooser());
        saveButton.setOnClickListener(v -> saveData(v));
        backButton.setOnClickListener(v -> dismiss());
    }

    private void saveData(View v) {
        String newTitle = editTitle.getText().toString().trim();
        String newAuthor = editAuthor.getText().toString().trim();
        String newIsbn = editIsbn.getText().toString().trim();
        String newPagesStr = editPages.getText().toString().trim();
        String newCountry = editCountry.getText().toString().trim();
        String newDescription = editDescription.getText().toString().trim();

        if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(newAuthor)) {
            Toast.makeText(getContext(), "Заповніть обов'язкові поля", Toast.LENGTH_SHORT).show();
            return;
        }

        int newPages = 0;
        if (!TextUtils.isEmpty(newPagesStr)) {
            try {
                newPages = Integer.parseInt(newPagesStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Невірний формат кількості сторінок", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (coverBitmap != null) {
            uploadImageAndSaveBook(newTitle, newAuthor, newIsbn, newPages, newCountry, newDescription);
        } else {
            if (MODE_EDIT.equals(mode)) {
                updateBookInFirestore(newTitle, newAuthor, newIsbn, newPages, newCountry, newDescription, book.getCoverUrl());
            } else {
                saveNewBookToFirestore(newTitle, newAuthor, newIsbn, newPages, newCountry, newDescription, "");
            }
        }
    }

    private void setTextForEditText() {
        dialogTitleView.setText(dialogTitle);
        if (MODE_EDIT.equals(mode) && book != null) {
            editTitle.setText(book.getTitle());
            editAuthor.setText(book.getAuthor());
            editIsbn.setText(book.getISBN());
            editPages.setText(book.getPageCount() > 0 ? String.valueOf(book.getPageCount()) : "");
            editCountry.setText(book.getCountry());
            editDescription.setText(book.getDescription());
        }
    }

    private void loadExistingCover() {
        if (MODE_EDIT.equals(mode) && book != null && book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(this)
                    .load(book.getCoverUrl())
                    .placeholder(R.drawable.none_cover)
                    .into(imageBookCover);
        }
    }

    private void initializeViews(View view) {
        dialogTitleView = view.findViewById(R.id.dialog_title);
        imageBookCover = view.findViewById(R.id.image_book_cover);
        editTitle = view.findViewById(R.id.edit_book_title);
        editAuthor = view.findViewById(R.id.edit_book_author);
        editIsbn = view.findViewById(R.id.edit_book_isbn);
        editPages = view.findViewById(R.id.edit_book_pages);
        editCountry = view.findViewById(R.id.edit_book_country);
        editDescription = view.findViewById(R.id.edit_book_description);
        saveButton = view.findViewById(R.id.button_save);
        uploadButton = view.findViewById(R.id.button_upload_cover);
        backButton = view.findViewById(R.id.button_back);
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Оберіть обкладинку"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                coverBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                imageBookCover.setImageBitmap(coverBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageAndSaveBook(String title, String author, String isbn, int pages,
                                        String country, String description) {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("book_covers")
                .child(userId)
                .child(UUID.randomUUID().toString() + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        coverBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageData = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                if (MODE_EDIT.equals(mode)) {
                    updateBookInFirestore(title, author, isbn, pages, country, description, imageUrl);
                } else {
                    saveNewBookToFirestore(title, author, isbn, pages, country, description, imageUrl);
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Помилка при завантаженні зображення", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateBookInFirestore(String title, String author, String isbn, int pages,
                                       String country, String description, String coverUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .collection("books")
                .document(documentId)
                .update(
                        "title", title,
                        "authors", author,
                        "isbn", isbn,
                        "pageCount", pages,
                        "country", country,
                        "description", description,
                        "coverUrl", coverUrl
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Книгу оновлено", Toast.LENGTH_SHORT).show();
                    listener.onBookUpdated();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Помилка при оновленні", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveNewBookToFirestore(String title, String authors, String isbn, int pages,
                                        String country, String description, String coverUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> bookData = new HashMap<>();
        bookData.put("title", title);
        bookData.put("authors", authors);
        bookData.put("isbn", isbn);
        bookData.put("pageCount", pages);
        bookData.put("country", country);
        bookData.put("description", description);
        bookData.put("coverUrl", coverUrl);

        db.collection("users")
                .document(userId)
                .collection("books")
                .add(bookData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Книгу додано", Toast.LENGTH_SHORT).show();
                    listener.onBookUpdated();
                    dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Помилка при додаванні", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Point size = new Point();
            dialog.getWindow().getWindowManager().getDefaultDisplay().getSize(size);
            dialog.getWindow().setLayout(size.x, size.y);
        }
    }

    public interface OnBookUpdatedListener {
        void onBookUpdated();
    }
}