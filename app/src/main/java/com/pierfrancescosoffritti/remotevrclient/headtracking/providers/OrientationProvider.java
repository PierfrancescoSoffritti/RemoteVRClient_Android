/**
 * 
 */
package com.pierfrancescosoffritti.remotevrclient.headtracking.providers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.pierfrancescosoffritti.remotevrclient.headtracking.representation.Quaternion;

/**
 * Classes implementing this interface provide an orientation of the device
 * 
 * The orientation is provided as quaternion.
 * 
 * @author Alexander Pacha
 * @author Pierfrancesco Soffritti
 * 
 */
public abstract class OrientationProvider implements SensorEventListener {

    /**
     * The sensor used by this provider
     */
    protected Sensor sensor;

    /**
     * The quaternion that holds the current rotation
     */
    protected final Quaternion currentOrientationQuaternion;

    /**
     * The sensor manager for accessing Android sensors
     */
    protected SensorManager sensorManager;

    /**
     * Initialises a new OrientationProvider
     */
    public OrientationProvider(Context context) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        // Initialise with identity
        currentOrientationQuaternion = new Quaternion();
    }

    /**
     * Starts the sensor (e.g. when resuming the activity)
     */
    public void start() {
        sensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Stops the sensor (e.g. when pausing/suspending the activity)
     */
    public void stop() {
        // make sure to turn our sensors off when the activity is paused
        sensorManager.unregisterListener(this, sensor);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * @return Returns the current rotation of the device in the quaternion format (vector4f)
     */
    public Quaternion getQuaternion() {
        return currentOrientationQuaternion.clone();
    }
}
