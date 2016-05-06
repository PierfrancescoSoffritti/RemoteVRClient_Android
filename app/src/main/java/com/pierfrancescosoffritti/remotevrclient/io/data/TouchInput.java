package com.pierfrancescosoffritti.remotevrclient.io.data;

import android.view.MotionEvent;

import java.nio.ByteBuffer;

/**
 * Represents a touch.
 * <br/><br/>
 * This class is a singleton so I can always use the same {@ByteBuffer} instance.
 *
 * @author Pierfrancesco Soffritti
 */
public class TouchInput implements GameInput<MotionEvent> {
    private static TouchInput ourInstance = new TouchInput();

    public static TouchInput getInstance() {
        return ourInstance;
    }

    private ByteBuffer byteBuffer = ByteBuffer.allocate(PAYLOAD_SIZE);

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
