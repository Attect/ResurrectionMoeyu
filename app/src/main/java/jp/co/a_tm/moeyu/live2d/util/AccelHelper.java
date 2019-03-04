package jp.co.a_tm.moeyu.live2d.util;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelHelper {
    private final Sensor accelerometer;
    /* access modifiers changed from: private */
    public AccelListener listener;
    private MySensorListener sensorListener = new MySensorListener();
    private SensorManager sensorManager;

    public interface AccelListener {
        void accelUpdated(float f, float f2, float f3);
    }

    private class MySensorListener implements SensorEventListener {
        private MySensorListener() {
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent e) {
            if (e.sensor.getType() == 1 && AccelHelper.this.listener != null) {
                AccelHelper.this.listener.accelUpdated((-e.values[0]) / 9.80665f, (-e.values[1]) / 9.80665f, (-e.values[2]) / 9.80665f);
            }
        }
    }

    public AccelHelper(Activity a) {
        this.sensorManager = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = this.sensorManager.getDefaultSensor(1);
    }

    public void start() {
        try {
            this.sensorManager.registerListener(this.sensorListener, this.accelerometer, 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            this.sensorManager.unregisterListener(this.sensorListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAccelListener(AccelListener lis) {
        this.listener = lis;
    }
}
