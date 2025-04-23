package com.example.bookvoyager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import com.caverock.androidsvg.SVG;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

public class SvgMapView extends View {

    private SVG mapSvg, uaSvg, alSvg;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private OverScroller scroller;

    private float scaleFactor = 2.0f;
    private float posX = 0f, posY = 0f;

    private float lastTouchX, lastTouchY;
    private boolean isDragging = false;

    private final Matrix transformMatrix = new Matrix();
    private HashMap<String, Boolean> countries;
    private boolean showUa = false;

    private FirebaseFirestore db;
    private String currentUserId;

    public SvgMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        fillingCountries();
        initCountries();
        loadSvgs(context);
        initGestureDetectors(context);
        scroller = new OverScroller(context);
    }

    private void fillingCountries() {
        countries = new HashMap<>();
        countries.put("AL", false);
        countries.put("AT", false);
        countries.put("BE", false);
        countries.put("BG", false);
        countries.put("BA", false);
        countries.put("BY", false);
        countries.put("CH", false);
        countries.put("CZ", false);
        countries.put("DE", false);
        countries.put("DK", false);
        countries.put("EE", false);
        countries.put("FI", false);
        countries.put("GB", false);
        countries.put("GR", false);
        countries.put("HR", false);
        countries.put("HU", false);
        countries.put("IE", false);
        countries.put("IS", false);
        countries.put("IT", false);
        countries.put("LT", false);
        countries.put("LU", false);
        countries.put("LV", false);
        countries.put("MD", false);
        countries.put("MK", false);
        countries.put("ME", false);
        countries.put("NL", false);
        countries.put("NO", false);
        countries.put("PL", false);
        countries.put("PT", false);
        countries.put("RO", false);
        countries.put("RS", false);
        countries.put("SK", false);
        countries.put("SI", false);
        countries.put("SE", false);
        countries.put("UA", false);
        countries.put("FR", false);
        countries.put("ES", false);
        countries.put("KO", false);
    }

    private void initCountries(){
        db = FirebaseFirestore.getInstance();

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        db.collection("users")
                .document(currentUserId)
                .collection("locationSpot")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String locationId = document.getString("locationId");
                            if (locationId != null && countries.containsKey(locationId)) {
                                countries.put(locationId, true);
                            }
                        }
                        invalidate();
                    }
                    else {
                        Log.e("SvgMapView", "Error loading countries", task.getException());
                    }
                });
    }

    private void loadSvgs(Context context) {
        try {
            mapSvg = SVG.getFromResource(context, R.raw.map);
            uaSvg = SVG.getFromResource(context, R.raw.ua);
            alSvg = SVG.getFromResource(context, R.raw.al);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initGestureDetectors(Context context) {
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 5.0f));
                invalidate();
                return true;
            }
        });

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                if (!scroller.isFinished()) {
                    scroller.forceFinished(true);
                }
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                scroller.fling(
                        (int) posX,
                        (int) posY,
                        (int) velocityX,
                        (int) velocityY,
                        Integer.MIN_VALUE, Integer.MAX_VALUE,
                        Integer.MIN_VALUE, Integer.MAX_VALUE
                );
                invalidate();
                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (scroller.computeScrollOffset()) {
            posX = scroller.getCurrX();
            posY = scroller.getCurrY();
            invalidate();
        }

        transformMatrix.reset();
        transformMatrix.postTranslate(posX, posY);
        transformMatrix.postScale(scaleFactor, scaleFactor, getWidth() / 2f, getHeight() / 2f);
        canvas.concat(transformMatrix);

        drawSvg(mapSvg, canvas);
        if (countries.get("UA")) drawSvg(uaSvg, canvas);
        if (countries.get("AL")) drawSvg(alSvg, canvas);
    }

    private void drawSvg(SVG svg, Canvas canvas) {
        if (svg != null) {
            svg.setDocumentWidth(getWidth());
            svg.setDocumentHeight(getHeight());
            svg.renderToCanvas(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        final int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isDragging = true;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (!scaleDetector.isInProgress() && isDragging) {
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;

                    posX += dx * 0.4f;
                    posY += dy * 0.4f;

                    lastTouchX = event.getX();
                    lastTouchY = event.getY();

                    invalidate();
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                isDragging = false;
                break;
            }
        }

        return true;
    }
}