package com.pierfrancescosoffritti.remotevrclient.io.data;

import com.pierfrancescosoffritti.remotevrclient.sensorFusion.representation.Quaternion;

import java.nio.ByteBuffer;

/**
 * Created by  Pierfrancesco on 25/04/2016.
 */
public class GyroInput implements GameInput<Quaternion> {
    private static GyroInput ourInstance = new GyroInput();

    public static GyroInput getInstance() {
        return ourInstance;
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    private GyroInput() {
    }

    @Override
    public byte getType() {
        return GYRO;
    }

    @Override
    public ByteBuffer getPayload() {
        return byteBuffer;
    }

    @Override
    public GyroInput putPayload(Quaternion payload) {
        byteBuffer.clear();
        byteBuffer
                .putFloat(payload.getX())
                .putFloat(payload.getY())
                .putFloat(payload.getZ())
                .putFloat(payload.getW());

        return ourInstance;
    }
}
