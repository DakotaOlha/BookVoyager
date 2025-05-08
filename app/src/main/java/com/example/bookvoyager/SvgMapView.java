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

    private SVG mapSvg, uaSvg, alSvg, atSvg, beSvg, bgSvg, baSvg, bySvg, chSvg, czSvg, deSvg, dkSvg, eeSvg, fiSvg,
            gbSvg, grSvg, hrSvg, huSvg, ieSvg, itSvg, ltSvg, luSvg, lvSvg, mdSvg, mkSvg, meSvg, nlSvg, noSvg,
            plSvg, ptSvg, roSvg, rsSvg, skSvg, siSvg, seSvg, frSvg, esSvg, koSvg;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private final OverScroller scroller;

    private float scaleFactor = 2.0f;
    private float posX = 0f, posY = 0f;

    private float lastTouchX, lastTouchY;
    private boolean isDragging = false;

    private final Matrix transformMatrix = new Matrix();
    private HashMap<String, Boolean> countries;

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
                            if (locationId != null && countries.containsKey(locationId) && document.getBoolean("ifUnlocked") != false) {
                                countries.put(locationId, true);
                                Log.d("SvgMapView", "LocationId: " + locationId);
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
            atSvg = SVG.getFromResource(context, R.raw.at);
            beSvg = SVG.getFromResource(context, R.raw.be);
            bgSvg = SVG.getFromResource(context, R.raw.bg);
            baSvg = SVG.getFromResource(context, R.raw.ba);
            bySvg = SVG.getFromResource(context, R.raw.by);
            chSvg = SVG.getFromResource(context, R.raw.ch);
            czSvg = SVG.getFromResource(context, R.raw.cz);
            deSvg = SVG.getFromResource(context, R.raw.de);
            dkSvg = SVG.getFromResource(context, R.raw.dk);
            eeSvg = SVG.getFromResource(context, R.raw.ee);
            fiSvg = SVG.getFromResource(context, R.raw.fi);
            gbSvg = SVG.getFromResource(context, R.raw.gb);
            grSvg = SVG.getFromResource(context, R.raw.gr);
            hrSvg = SVG.getFromResource(context, R.raw.hr);
            huSvg = SVG.getFromResource(context, R.raw.hu);
            ieSvg = SVG.getFromResource(context, R.raw.ie);
            itSvg = SVG.getFromResource(context, R.raw.it);
            ltSvg = SVG.getFromResource(context, R.raw.lt);
            luSvg = SVG.getFromResource(context, R.raw.lu);
            lvSvg = SVG.getFromResource(context, R.raw.lv);
            mdSvg = SVG.getFromResource(context, R.raw.md);
            mkSvg = SVG.getFromResource(context, R.raw.mk);
            meSvg = SVG.getFromResource(context, R.raw.me);
            nlSvg = SVG.getFromResource(context, R.raw.nl);
            noSvg = SVG.getFromResource(context, R.raw.no);
            plSvg = SVG.getFromResource(context, R.raw.pl);
            ptSvg = SVG.getFromResource(context, R.raw.pt);
            roSvg = SVG.getFromResource(context, R.raw.ro);
            rsSvg = SVG.getFromResource(context, R.raw.rs);
            skSvg = SVG.getFromResource(context, R.raw.sk);
            siSvg = SVG.getFromResource(context, R.raw.si);
            seSvg = SVG.getFromResource(context, R.raw.se);
            frSvg = SVG.getFromResource(context, R.raw.fr);
            esSvg = SVG.getFromResource(context, R.raw.es);
            koSvg = SVG.getFromResource(context, R.raw.ko);
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
        if (Boolean.TRUE.equals(countries.get("UA"))) drawSvg(uaSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("AL"))) drawSvg(alSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("AT"))) drawSvg(atSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("BE"))) drawSvg(beSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("BG"))) drawSvg(bgSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("BA"))) drawSvg(baSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("BY"))) drawSvg(bySvg, canvas);
        if (Boolean.TRUE.equals(countries.get("CH"))) drawSvg(chSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("CZ"))) drawSvg(czSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("DE"))) drawSvg(deSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("DK"))) drawSvg(dkSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("EE"))) drawSvg(eeSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("FI"))) drawSvg(fiSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("GB"))) drawSvg(gbSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("GR"))) drawSvg(grSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("HR"))) drawSvg(hrSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("HU"))) drawSvg(huSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("IE"))) drawSvg(ieSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("IT"))) drawSvg(itSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("LT"))) drawSvg(ltSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("LU"))) drawSvg(luSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("LV"))) drawSvg(lvSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("MD"))) drawSvg(mdSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("MK"))) drawSvg(mkSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("ME"))) drawSvg(meSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("NL"))) drawSvg(nlSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("NO"))) drawSvg(noSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("PL"))) drawSvg(plSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("PT"))) drawSvg(ptSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("RO"))) drawSvg(roSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("RS"))) drawSvg(rsSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("SK"))) drawSvg(skSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("SI"))) drawSvg(siSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("SE"))) drawSvg(seSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("FR"))) drawSvg(frSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("ES"))) drawSvg(esSvg, canvas);
        if (Boolean.TRUE.equals(countries.get("KO"))) drawSvg(koSvg, canvas);
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