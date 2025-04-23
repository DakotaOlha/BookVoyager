package com.example.bookvoyager;

import android.os.Bundle;
import android.os.Handler;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class BookTimerFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_PROGRESS = "progress";
    private static final String ARG_TIME_LOGGED = "time_logged";

    ReadingSessions readingSessions = null;
    private int percent = 80;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String currentUserId;

    private Handler handler = new Handler();
    private Runnable timerRunnable;
    private boolean timerRunning = false;
    private long startTime = 0;
    private long elapsedTime = 0;
    private MaterialButton playButton;
    private MaterialButton continueButton;
    private MaterialButton finishButton;
    private ProgressBar progressBar;
    private TextView percentTextView;
    private TextView timerTextView;
    private ImageView bookCoverImageView;

    public static BookTimerFragment newInstance(String title, String coverUrl) {
        BookTimerFragment fragment = new BookTimerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
//        args.putString(ARG_PROGRESS, progress);
//        args.putString(ARG_TIME_LOGGED, timeLogged);
        args.putString("coverUrl", coverUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFirebase();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_timer, container, false);

        ImageButton backButton = view.findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> {
            if (timerRunning) {
                showExitConfirmationDialog();
            } else {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        bookCoverImageView = view.findViewById(R.id.tvBookCover);

        TextView tvTitle = view.findViewById(R.id.tvBookTitle);

        if (getArguments() != null) {
            tvTitle.setText(getArguments().getString(ARG_TITLE));
            String coverUrl = getArguments().getString("coverUrl");
            if(coverUrl != null)
                loadBookCover(coverUrl);
        }

        playButton = view.findViewById(R.id.playButton);
        timerTextView = view.findViewById(R.id.readingTimer);

        continueButton = view.findViewById(R.id.continueButton);
        finishButton = view.findViewById(R.id.finishButton);

        progressBar = view.findViewById(R.id.tvBookProgressBar);
        percentTextView = view.findViewById(R.id.tvBookPercent);

        playButton.setVisibility(View.VISIBLE);
        continueButton.setVisibility(View.GONE);
        finishButton.setVisibility(View.GONE);

        playButton.setOnClickListener(v -> {
            if (timerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        continueButton.setOnClickListener(v -> {
            continueReading();
        });

        finishButton.setOnClickListener(v -> {
            stopTimerAndSave();
        });

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                elapsedTime = currentTime - startTime;
                updateTimerText(elapsedTime);
                handler.postDelayed(this, 1000);
            }
        };

        getDataFromFirebase();
        return view;
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private void loadBookCover(String coverUrl) {
        if (coverUrl != null && (coverUrl.startsWith("http://") || coverUrl.startsWith("https://"))) {
            Picasso.get()
                    .load(coverUrl)
                    .placeholder(R.drawable.none_cover)
                    .error(R.drawable.none_cover)
                    .into(bookCoverImageView);
        } else if (coverUrl != null && !coverUrl.isEmpty()) {
            int resId = requireContext().getResources()
                    .getIdentifier(coverUrl, "drawable", requireContext().getPackageName());

            if (resId != 0) {
                bookCoverImageView.setImageResource(resId);
            } else {
                bookCoverImageView.setImageResource(R.drawable.none_cover);
            }
        } else {
            bookCoverImageView.setImageResource(R.drawable.none_cover);
        }
    }


    private void startTimer() {
        if (!timerRunning) {
            startTime = System.currentTimeMillis() - elapsedTime;
            handler.postDelayed(timerRunnable, 0);
            timerRunning = true;
            playButton.setVisibility(View.VISIBLE);
            timerTextView.setVisibility(View.VISIBLE);
            percentTextView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void pauseTimer() {
        if (timerRunning) {
            handler.removeCallbacks(timerRunnable);
            timerRunning = false;

            playButton.setVisibility(View.GONE);
            continueButton.setVisibility(View.VISIBLE);
            finishButton.setVisibility(View.VISIBLE);
        }
    }

    private void continueReading() {
        startTimer();
        continueButton.setVisibility(View.GONE);
        finishButton.setVisibility(View.GONE);
        playButton.setVisibility(View.VISIBLE);
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Підтвердження виходу")
                .setMessage("Таймер все ще працює. Ви дійсно хочете вийти?")
                .setPositiveButton("Так", (dialog, which) -> {
                    stopTimer();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .setNegativeButton("Ні", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void stopTimer() {
        if (timerRunning) {
            handler.removeCallbacks(timerRunnable);
            timerRunning = false;
            elapsedTime = 0;
            updateTimerText(0);
        }
    }


    private void stopTimerAndSave() {
        handler.removeCallbacks(timerRunnable);
        timerRunning = false;
        showPageInputDialog();
        percentTextView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.VISIBLE);
    }

    private void updateTimerText(long timeInMillis) {
        int seconds = (int) (timeInMillis / 1000) % 60;
        int minutes = (int) ((timeInMillis / (1000 * 60)) % 60);
        int hours = (int) ((timeInMillis / (1000 * 60 * 60)) % 24);

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        timerTextView.setText(timeFormatted);
    }

    private void showPageInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Час читання");
        builder.setMessage("Час: " + timerTextView.getText() + "\nВведіть сторінку, на якій ви зупинилися:");

        final TextInputEditText input = new TextInputEditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Зберегти", (dialog, which) -> {
            String pageStr = input.getText().toString();
            if (!pageStr.isEmpty()) {
                int currentPage = Integer.parseInt(pageStr);
                //saveReadingProgress(currentPage, elapsedTime);
            }
            // Скидаємо секундомір після збереження
            elapsedTime = 0;
            updateTimerText(0);
        });
        builder.setNegativeButton("Скасувати", (dialog, which) -> {
            dialog.cancel();
            // Скидаємо секундомір, якщо користувач скасував
            elapsedTime = 0;
            updateTimerText(0);
        });

        continueButton.setVisibility(View.GONE);
        finishButton.setVisibility(View.GONE);
        timerTextView.setVisibility(View.GONE);

        builder.show();
    }

    private void saveReadingProgress(int currentPage, long readingTime) {
//        if (readingSessions == null || currentUserId == null) return;
//
//        if (currentPage > readingSessions.getPagesRead()) {
//            readingSessions.setPagesRead(currentPage);
//
//            percent = Math.round(((float) readingSessions.getPagesRead() / readingSessions.getPagesCount()) * 100);
//
//            View view = getView();
//            if (view != null) {
//                TextView tvBookPercent = view.findViewById(R.id.tvBookPercent);
//                tvBookPercent.setText(percent + "%");
//                ProgressBar tvBookProgressBar = view.findViewById(R.id.tvBookProgressBar);
//                tvBookProgressBar.setProgress(percent);
//            }
//
//            Map<String, Object> updates = new HashMap<>();
//            updates.put("pagesRead", currentPage);
//            updates.put("lastReadingTime", System.currentTimeMillis());
//            updates.put("totalReadingTime", readingSessions.getTotalReadingTime() + readingTime);
//
//            db.collection("users")
//                    .document(currentUserId)
//                    .collection("readingSessions")
//                    .whereEqualTo("title", readingSessions.getTitle())
//                    .get()
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
//                            DocumentReference docRef = task.getResult().getDocuments().get(0).getReference();
//                            docRef.update(updates)
//                                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Прогрес збережено", Toast.LENGTH_SHORT).show())
//                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Помилка збереження", Toast.LENGTH_SHORT).show());
//                        }
//                    });
//        }
    }
    private void saveReadingProgress(int currentPage) {
//        if (readingSessions == null || currentUserId == null) return;
//
//        // Update pages read if current page it.svg greater than previous
//        if (currentPage > readingSessions.getPagesRead()) {
//            readingSessions.setPagesRead(currentPage);
//
//            // Calculate new progress
//            percent = Math.round(((float) readingSessions.getPagesRead() / readingSessions.getPagesCount()) * 100);
//
//            // Update UI
//            View view = getView();
//            if (view != null) {
//                TextView tvBookPercent = view.findViewById(R.id.tvBookPercent);
//                tvBookPercent.setText(percent + "%");
//                ProgressBar tvBookProgressBar = view.findViewById(R.id.tvBookProgressBar);
//                tvBookProgressBar.setProgress(percent);
//            }
//
//            // Update in Firebase
//            Map<String, Object> updates = new HashMap<>();
//            updates.put("pagesRead", currentPage);
//            updates.put("lastReadingTime", System.currentTimeMillis());
//            updates.put("totalReadingTime", totalReadingTime);
//
//            db.collection("users")
//                    .document(currentUserId)
//                    .collection("readingSessions")
//                    .whereEqualTo("title", readingSessions.getTitle())
//                    .get()
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
//                            DocumentReference docRef = task.getResult().getDocuments().get(0).getReference();
//                            docRef.update(updates)
//                                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Прогрес збережено", Toast.LENGTH_SHORT).show())
//                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Помилка збереження", Toast.LENGTH_SHORT).show());
//                        }
//                    });
//        }
    }

    private void getDataFromFirebase() {
        db.collection("users")
                .document(currentUserId)
                .collection("readingSessions")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        findBook(task);
                    }
                    else {
                        Toast.makeText(getContext(), "Помилка завантаження книг", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void findBook(Task<QuerySnapshot> task) {
        readingSessions = null;
        for (QueryDocumentSnapshot document : task.getResult()) {
            if (document.get("title").equals(getArguments().getString(ARG_TITLE))) {
                readingSessions = new ReadingSessions(document.get("title").toString(), Integer.parseInt(document.get("pagesCount").toString()), Integer.parseInt(document.get("pagesRead").toString()));
                break;
            }
        }

        View view = getView();
        if (view != null && readingSessions != null) {
            percent = Math.round(((float) readingSessions.getPagesRead() / readingSessions.getPagesCount()) * 100);
            TextView tvBookPercent = view.findViewById(R.id.tvBookPercent);
            tvBookPercent.setText(percent + "%");
            ProgressBar tvBookProgressBar = view.findViewById(R.id.tvBookProgressBar);
            tvBookProgressBar.setProgress(percent);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
    }
}