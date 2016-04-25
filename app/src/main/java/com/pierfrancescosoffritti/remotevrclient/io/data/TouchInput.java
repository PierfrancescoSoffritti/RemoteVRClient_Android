package com.pierfrancescosoffritti.remotevrclient.io.data;

import android.view.MotionEvent;

import java.nio.ByteBuffer;

/**
 * Created by  Pierfrancesco on 25/04/2016.
 */
public class TouchInput implements GameInput<MotionEvent> {
    private static TouchInput ourInstance = new TouchInput();

    public static TouchInput getInstance() {
        return ourInstance;
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    private TouchInput() {
    }

    @Override
    public byte getType() {
        return TOUCH;
    }

    @Override
    public ByteBuffer getPayload() {
        return byteBuffer;
    }

    @Override
    public TouchInput putPayload(MotionEvent payload) {
        byteBuffer.clear();
        byteBuffer.putInt(payload.getAction());

        return ourInstance;
    }
}
