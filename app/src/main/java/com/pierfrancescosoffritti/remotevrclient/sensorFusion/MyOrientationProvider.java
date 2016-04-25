package com.pierfrancescosoffritti.remotevrclient.sensorFusion;

import android.content.Context;
import android.hardware.SensorManager;

import com.pierfrancescosoffritti.remotevrclient.sensorFusion.orientationProvider.ImprovedOrientationSensor1Provider;
import com.pierfrancescosoffritti.remotevrclient.sensorFusion.orientationProvider.OrientationProvider;
import com.pierfrancescosoffritti.remotevrclient.sensorFusion.representation.Quaternion;

/**
 * Created by  Pierfrancesco on 14/03/2016.
 *
 * Wrapper of the OrientationProvider class
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
