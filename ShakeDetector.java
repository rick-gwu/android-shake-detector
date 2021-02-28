package com.anjosoft.shakedetector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import java.lang.UnsupportedOperationException;

public class ShakeDetector implements SensorEventListener, Shake {
    private static final int magnitude = 360, interval = 100, timeout = 500, duration = 500, count = 4;
    private float lastX = -1.0f, lastY = -1.0f, lastZ = -1.0f;
    private long lastShakeTime, lastShake;
    private int shakeCount = 0;

    private SensorManager sensorManager;
    private Shake shake;
    private Context context;

    public ShakeDetector(Context context) {
        this.context = context;
        resume();
    }

    public void setOnShakeListener(Shake shake) {
        this.shake = shake;
    }

    public void resume() {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager == null) {
            throw new UnsupportedOperationException("Motion sensors are off or not supported on this device");
        }

        try {
            boolean supported = sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    sensorManager.SENSOR_DELAY_FASTEST);

            if (!supported && (sensorManager != null)) {
                sensorManager.unregisterListener(this);
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException("Motion sensors are off or not supported on this device");
        }
    }

    public void pause() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

        long now = System.currentTimeMillis();

        if ((now - lastShakeTime) > timeout) {
            shakeCount = 0;
        }

        if ((now - lastShakeTime) > interval) {

            float speed = Math.abs(
                (
                    event.values[0] /* DATA_X */ +
                    event.values[1] /* DATA_Y */ +
                    event.values[2] /* DATA_Z */
                )
                - (lastX + lastY + lastZ)
            ) / (now - lastShakeTime) * 10000;

            if (speed > magnitude) {
                if ((++shakeCount >= count) && (now - lastShake > duration)) {
                    lastShake = now;
                    shakeCount = 0;

                    if (shake != null) {
                        shake.onShake();
                    }
                }
            }

            lastShakeTime = now;
            lastX = event.values[0]; // DATA_X
            lastY = event.values[1]; // DATA_Y
            lastZ = event.values[2]; // DATA_Z
        }
    }

    @Override
    public void onShake() {
    }
}
