package com.pierfrancescosoffritti.remotevrclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by  Pierfrancesco on 02/03/2016.
 */
public class RemoteVRView extends View {
    private Bitmap mBitmap;

    public RemoteVRView(Context context) {
        super(context);
    }

    public RemoteVRView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
    }

    public RemoteVRView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void updateImage(Bitmap bImage) {
        mBitmap = bImage;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap != null) {
            drawBitmap(canvas);
        }
    }

    private double drawBitmap( Canvas canvas ) {
        // TODO images will have 2 known sizes, for landscape and portrait, so this will be moved out from here
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = mBitmap.getWidth();
        double imageHeight = mBitmap.getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

        Rect destBounds = new Rect( 0, 0, (int) ( imageWidth * scale ), (int) ( imageHeight * scale ) );
        canvas.drawBitmap(mBitmap, null, destBounds, null);
        return scale;
    }
}
