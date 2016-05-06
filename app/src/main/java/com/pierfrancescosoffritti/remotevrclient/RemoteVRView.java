package com.pierfrancescosoffritti.remotevrclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.pierfrancescosoffritti.remotevrclient.utils.SwipeDetector;

import rx.subjects.PublishSubject;

/**
 * Custom View responsible for showing the game images and receiving game inputs
 */
public class RemoteVRView extends View {

    /**
     * current bitmap
     */
    private Bitmap mBitmap;

    /**
     * {@link PublishSubject} used to handle the touch events on the view as a stream
     */
    private PublishSubject<MotionEvent> publishSubject = PublishSubject.create();

    public RemoteVRView(Context context) {
        super(context);
    }

    public RemoteVRView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
    }

    public RemoteVRView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Sets a new bitmap and invalidate the view
     * @param image the new bitmap to be drawn
     */
    public void updateImage(Bitmap image) {
        mBitmap = image;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap != null) {
            drawBitmap(mBitmap, canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        publishSubject.onNext(event);

        swipeDetector.onTouchEvent(event);

        // return, so we can detect ACTION_UP
        if(event.getAction() == MotionEvent.ACTION_DOWN)
            return true;

        return super.onTouchEvent(event);
    }

    /**
     * Swipe detector responsible for detecting top-bottom swipes
     */
    private final GestureDetector swipeDetector = new GestureDetector(getContext(), new SwipeDetector(){
        @Override
        public void onSwipeBottom() {
            EventBus.getInstance().post(new Events.RemoteView_SwipeTopBottom());
        }
    });

    /**
     * @return a {@link PublishSubject} representing touch events on the view as a stream
     */
    public PublishSubject<MotionEvent> getPublishSubject() {
        return publishSubject;
    }

    /**
     *  draws the bitmap on the canvas, with the appropriate scale
     * @param bitmap the image to draw
     * @param canvas canvas to draw on
     * @return the scale of the drawn image
     */
    private double drawBitmap(Bitmap bitmap, Canvas canvas ) {
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = bitmap.getWidth();
        double imageHeight = bitmap.getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

        Rect destBounds = new Rect( 0, 0, (int) ( imageWidth * scale ), (int) ( imageHeight * scale ) );
        canvas.drawBitmap(bitmap, null, destBounds, null);
        return scale;
    }
}
