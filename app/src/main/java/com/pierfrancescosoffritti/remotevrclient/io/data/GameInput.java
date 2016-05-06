package com.pierfrancescosoffritti.remotevrclient.io.data;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

/**
 * Defines a game input
 *
 * @author Pierfrancesco Soffritti
 */
public interface GameInput<T> {

    /**
     *  Payloads of different {@link GameInputType} must be of the same size.
     */
    int PAYLOAD_SIZE = 4*4;

    byte GYRO = 0;
    byte TOUCH = 1;

    /**
     * Defines the different inputs accepted by the game.
     */
    @IntDef({GYRO, TOUCH})
    @Retention(RetentionPolicy.SOURCE)
    @interface GameInputType {}

    @GameInputType byte getType();
    ByteBuffer getPayload();
    GameInput<T> putPayload(T payload);
}
