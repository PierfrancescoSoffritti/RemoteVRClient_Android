package com.pierfrancescosoffritti.remotevrclient.io.data;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

/**
 * Created by  Pierfrancesco on 25/04/2016.
 */
public interface GameInput<T> {

    // both GameInputType buffers have the same size, so I can always read them with a single read call on the server.
    int BUFFER_SIZE = 4*4;

    byte GYRO = 0;
    byte TOUCH = 1;

    @IntDef({GYRO, TOUCH})
    @Retention(RetentionPolicy.SOURCE)
    @interface GameInputType {}

    @GameInputType byte getType();
    ByteBuffer getPayload();
    GameInput<T> putPayload(T payload);
}
