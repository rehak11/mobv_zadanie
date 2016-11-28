package com.example.jorg.mobvzadanie1;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

//http://stackoverflow.com/questions/8264518/using-accelerometer-gyroscope-and-compass-to-calculate-devices-movement-in-3d
//https://www.youtube.com/watch?v=C7JQ7Rpwn2k
public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    DrawingView dv;

    private CompassListener compassListener;
    private StepListener stepListener;

    public float azimut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dv = new DrawingView(this);
        setContentView(dv);

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        compassListener = new CompassListener();
        Sensor magnetometer = manager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
        manager.registerListener(compassListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        manager.registerListener(compassListener, magnetometer, SensorManager.SENSOR_DELAY_GAME);

        stepListener = new StepListener();

        //This features analyzes accelerometer input for steps and  triggers an event for every step.
        if (manager.getSensorList(Sensor.TYPE_STEP_DETECTOR).size() != 0)
        {
            Sensor stepDetectorSensor = manager.getSensorList(Sensor.TYPE_STEP_DETECTOR).get(0);
            manager.registerListener(stepListener, stepDetectorSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }

    public class CompassListener implements SensorEventListener {
        float[] mGravity;
        float[] mGeomagnetic;
        long eventCount = 0;

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, new float[] {0, 0, 9.76f}, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);// orientation contains: azimut, pitch and roll

                    azimut = (float) Math.round(Math.toDegrees(orientation[0]));
                    eventCount++;
                    if (eventCount % 1 == 0)
                    {
                        eventCount = 0;
                        dv.applyAzimut(azimut);
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public class StepListener implements SensorEventListener {
        int stepDetectorCounter = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;
            float[] values = event.values;

            if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                stepDetectorCounter++;

                dv.move();
                dv.applyAzimut(azimut);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
