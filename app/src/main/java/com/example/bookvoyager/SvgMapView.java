package com.example.bookvoyager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import com.caverock.androidsvg.SVG;

public class SvgMapView extends View {

    private SVG svg;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private OverScroller scroller;

    private float scaleFactor = 2.0f;
    private float posX = 0f, posY = 0f;

    private float lastTouchX, lastTouchY;
    private boolean isDragging = false;

    private final Matrix transformMatrix = new Matrix();

    public SvgMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadSvg(context);
        initGestureDetectors(context);
        scroller = new OverScroller(context);
    }

    private void loadSvg(Context context) {
        try {
            svg = SVG.getFromResource(context, R.raw.map);
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

        if (svg != null) {
            transformMatrix.reset();
            transformMatrix.postTranslate(posX, posY);
            transformMatrix.postScale(scaleFactor, scaleFactor, getWidth() / 2f, getHeight() / 2f);
            canvas.concat(transformMatrix);
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
