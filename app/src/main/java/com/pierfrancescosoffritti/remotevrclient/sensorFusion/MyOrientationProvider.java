package com.pierfrancescosoffritti.remotevrclient.sensorFusion;

import android.content.Context;
import android.hardware.SensorManager;

import com.pierfrancescosoffritti.remotevrclient.sensorFusion.orientationProvider.ImprovedOrientationSensor1Provider;
import com.pierfrancescosoffritti.remotevrclient.sensorFusion.orientationProvider.OrientationProvider;
import com.pierfrancescosoffritti.remotevrclient.sensorFusion.representation.Quaternion;

/**
 * Wrapper of the {@link OrientationProvider } class
 *
 * @author Pierfrancesco Soffritti
 */
public class MyOrientationProvider {

    OrientationProvider orientationProvider;

    public MyOrientationProvider(Context context) {
        orientationProvider = new ImprovedOrientationSensor1Provider((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
    }

    public void start() {
        orientationProvider.start();
    }

    public void stop() {
        orientationProvider.stop();
    }

    public Quaternion getQuaternion() {
        return orientationProvider.getQuaternion();
    }
}
