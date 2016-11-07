package com.example.jorg.mobvzadanie1;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

//http://stackoverflow.com/questions/8264518/using-accelerometer-gyroscope-and-compass-to-calculate-devices-movement-in-3d
//https://www.youtube.com/watch?v=C7JQ7Rpwn2k
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private BubbleView bubbleView;
    private CompassListener compassListener;
    private StepListener stepListener;

    private static int STEPS_COUNTER = 0;

    TextView textView;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    StringBuilder builder = new StringBuilder();

    float [] history = new float[2];
    String [] direction = {"NONE","NONE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bubbleView = new BubbleView(this);
        //setContentView( bubbleView );

        bubbleView = (BubbleView) findViewById( R.id.bubbleView );

        textView = (TextView) findViewById( R.id.textView );
        textView2 = (TextView) findViewById( R.id.textView2 );
        textView3 = (TextView) findViewById( R.id.textView3 );
        textView4 = (TextView) findViewById( R.id.textView4 );

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        compassListener = new CompassListener();
        Sensor magnetometer = manager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
        manager.registerListener(compassListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        manager.registerListener(compassListener, magnetometer, SensorManager.SENSOR_DELAY_GAME);

        stepListener = new StepListener();
        //This feature tracks total number of steps since the last device reboot and triggers an event on change in the step count.
        if( manager.getSensorList(Sensor.TYPE_STEP_COUNTER).size() != 0 ) {
            Sensor stepCounterSensor = manager.getSensorList(Sensor.TYPE_STEP_COUNTER).get(0);
            manager.registerListener(stepListener, stepCounterSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            textView3.setText("Sensor.TYPE_STEP_COUNTER missing!");
        }

        //This features analyzes accelerometer input for steps and  triggers an event for every step.
        if( manager.getSensorList(Sensor.TYPE_STEP_DETECTOR).size() != 0 ) {
            Sensor stepDetectorSensor = manager.getSensorList(Sensor.TYPE_STEP_DETECTOR).get(0);
            manager.registerListener(stepListener, stepDetectorSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            textView4.setText("Sensor.TYPE_STEP_DETECTOR missing!");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        builder.setLength( 0 );
        builder.append("x: ");
        builder.append(Math.round(event.values[0]*10)/10);
        builder.append("y: ");
        builder.append(Math.round(event.values[0]*10)/10);
        builder.append("z: ");
        builder.append(Math.round(event.values[0]*10)/10);

        /*final float alpha = 0.8;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];*/

       /* float xChange = history[0] - event.values[0];
        float yChange = history[1] - event.values[1];

        history[0] = event.values[0];
        history[1] = event.values[1];

        if (xChange > 2){
            direction[0] = "LEFT";
        }
        else if (xChange < -2){
            direction[0] = "RIGHT";
        }

        if (yChange > 2){
            direction[1] = "DOWN";
        }
        else if (yChange < -2){
            direction[1] = "UP";
        }*/

        /*builder.setLength(0);
        builder.append("x: ");
        builder.append(direction[0]);
        builder.append(" y: ");
        builder.append(direction[1]);*/

        textView.setText(builder.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }

    class CompassListener implements SensorEventListener {
        float[] mGravity;
        float[] mGeomagnetic;

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);// orientation contains: azimut, pitch and roll

                    float azimut = (float) Math.round(Math.toDegrees(orientation[0]));
                    float pitch = (float) Math.round(Math.toDegrees(orientation[1]));
                    float roll = (float) Math.round(Math.toDegrees(orientation[2]));
                    textView2.setText("azimut: " + azimut + " pitch: " + pitch + " roll: " + roll );

                    bubbleView.setRotation(azimut);
                    bubbleView.setRotationX(pitch);
                    bubbleView.setRotationY(roll);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    class StepListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;
            float[] values = event.values;
            int value = -1;

            if (values.length > 0) {
                value = (int) values[0];
            }

            if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                textView3.setText("Step Counter Detected : " + value);
            } else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                // For test only. Only allowed value is 1.0 i.e. for step taken
                textView4.setText("Step Detector Detected : " + value);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
