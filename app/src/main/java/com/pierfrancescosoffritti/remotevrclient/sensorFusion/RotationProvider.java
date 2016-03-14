package com.pierfrancescosoffritti.remotevrclient.sensorFusion;

import android.content.Context;
import android.hardware.SensorManager;

import com.pierfrancescosoffritti.remotevrclient.sensorFusion.orientationProvider.ImprovedOrientationSensor2Provider;
import com.pierfrancescosoffritti.remotevrclient.sensorFusion.orientationProvider.OrientationProvider;
import com.pierfrancescosoffritti.remotevrclient.sensorFusion.representation.Quaternion;

import rx.Observable;

/**
 * Created by  Pierfrancesco on 14/03/2016.
 */
public class RotationProvider {

    OrientationProvider orientationProvider;

    public RotationProvider(Context context) {
        orientationProvider = new ImprovedOrientationSensor2Provider((SensorManager) context.getSystemService(context.SENSOR_SERVICE));
    }

    public void start() {
        orientationProvider.start();
    }

    public void stop() {
        orientationProvider.stop();
    }

    public Observable<Quaternion> getRotationEmitter() {
        Observable.OnSubscribe<Quaternion> onSubscribe = subscriber -> {
            while(true)
                subscriber.onNext(orientationProvider.getQuaternion());
        };

        return Observable.create(onSubscribe);
    }
}
