package com.example.accel;

import static java.lang.Math.sqrt;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    
    TextView tvText;
    SensorManager sensorManager;
    Sensor sensorAccel;
    Sensor sensorLinAccel;
    Sensor sensorGravity;
    
    boolean fallen_maybe = false;
    boolean fallen = false;
    
    StringBuilder sb = new StringBuilder();
    
    Timer timer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tvText = findViewById(R.id.tvText);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorLinAccel = sensorManager
                .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensorAccel,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(listener, sensorLinAccel,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(listener, sensorGravity,
                SensorManager.SENSOR_DELAY_FASTEST);
        
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(MainActivity.this::showInfo);
            }
        };
        timer.schedule(task, 0, 100);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listener);
        timer.cancel();
    }
    
    String addSpaces(float val) {
        StringBuilder valS = new StringBuilder(String.format(Locale.getDefault(), "%1$.1f", val));
        for (int i = valS.length(); i < 4; i++) {
            valS.insert(0, " ");
        }
        return valS.toString();
    }
    
    String format(float[] values) {
        return addSpaces(values[0]) + " | " + addSpaces(values[1]) + " | " + addSpaces(values[2]);
    }
    
    float vectorLength(float[] vec) {
        return (float) sqrt(vec[0] * vec[0] * vec[1] * vec[1] + vec[2] * vec[2]);
    }
    
    void showInfo() {
        sb.setLength(0);
        sb.append("Accelerometer: ").append(format(valuesAccel))
                .append("\nAccel motion: ").append(format(valuesAccelMotion))
                .append("\nAccel gravity : ").append(format(valuesAccelGravity))
                .append("\nLin accel : ").append(format(valuesLinAccel))
                .append("\nGravity : ").append(format(valuesGravity));
        if (fallen) {
            sb.append("\n\n_______FALLEN_______");
        }
        if (fallen_maybe && vectorLength(valuesAccel) < 20) {
            fallen = true;
        } else {
            fallen_maybe = false;
        }
        if (vectorLength(valuesAccel) > 60) {
            fallen_maybe = true;
        }
        System.out.println(vectorLength(valuesAccel) + "\t" + vectorLength(valuesLinAccel) + "\t" + vectorLength(valuesGravity));
        tvText.setText(sb);
    }
    
    float[] valuesAccel = new float[3];
    float[] valuesAccelMotion = new float[3];
    float[] valuesAccelGravity = new float[3];
    float[] valuesLinAccel = new float[3];
    float[] valuesGravity = new float[3];
    
    SensorEventListener listener = new SensorEventListener() {
        
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    for (int i = 0; i < 3; i++) {
                        valuesAccel[i] = event.values[i];
                        valuesAccelGravity[i] = (float) (0.1 * event.values[i] + 0.9 * valuesAccelGravity[i]);
                        valuesAccelMotion[i] = event.values[i] - valuesAccelGravity[i];
                    }
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    System.arraycopy(event.values, 0, valuesLinAccel, 0, 3);
                    break;
                case Sensor.TYPE_GRAVITY:
                    System.arraycopy(event.values, 0, valuesGravity, 0, 3);
                    break;
            }
            
        }
        
    };
    
}