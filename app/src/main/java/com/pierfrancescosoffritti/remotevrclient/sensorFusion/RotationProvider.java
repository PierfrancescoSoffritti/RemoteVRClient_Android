package com.pierfrancescosoffritti.remotevrclient.sensorFusion;

import android.content.Context;
import android.hardware.SensorManager;

import com.pierfrancescosoffritti.remotevrclient.sensorFusion.orientationProvider.ImprovedOrientationSensor2Provider;
import com.pierfrancescosoffritti.remotevrclient.sensorFusion.orientationProvider.OrientationProvider;
import com.pierfrancescosoffritti.remotevrclient.sensorFusion.representation.Quaternion;

/**
 * Created by  Pierfrancesco on 14/03/2016.
 */
public class RotationProvider {

    OrientationProvider rotationProvider;

    public RotationProvider(Context context) {
        rotationProvider = new ImprovedOrientationSensor2Provider((SensorManager) context.getSystemService(context.SENSOR_SERVICE));
    }

    public void start() {
        rotationProvider.start();
    }

    public void stop() {
        rotationProvider.stop();
    }

    public Quaternion getQuaternion() {
        return rotationProvider.getQuaternion();
    }
}
