package com.example.jorg.mobvzadanie1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Date;

//http://stackoverflow.com/questions/8264518/using-accelerometer-gyroscope-and-compass-to-calculate-devices-movement-in-3d
//https://www.youtube.com/watch?v=C7JQ7Rpwn2k
// todo: pridat namiesto kruhu, ktory ukazuje aktualnu poziciu, to bubbleView, nech vidime kam je user prave otoceny
// todo: spresnit step counter nejak .. nefugunje to velmi dobre
// todo: treba nejak spravit aby ked pouzivatel vyjde mimo obrazovky aby obrazovka sla s nim .. alebo mozno zoom out
public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    DrawingView dv;

    private CompassListener compassListener;
    private StepListener stepListener;

    private static int STEPS_COUNTER = 0;

    StringBuilder builder = new StringBuilder();

    TextView textView;
    TextView textView2;
    TextView textView3;
    TextView textView4;

    float [] history = new float[2];
    String [] direction = {"NONE","NONE"};

    public float azimut;
    public float pitch;
    public float roll;

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
        //This feature tracks total number of steps since the last device reboot and triggers an event on change in the step count.
        if( manager.getSensorList(Sensor.TYPE_STEP_COUNTER).size() != 0 ) {
            Sensor stepCounterSensor = manager.getSensorList(Sensor.TYPE_STEP_COUNTER).get(0);
            manager.registerListener(stepListener, stepCounterSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            //textView3.setText("Sensor.TYPE_STEP_COUNTER missing!");
        }

        //This features analyzes accelerometer input for steps and  triggers an event for every step.
        if( manager.getSensorList(Sensor.TYPE_STEP_DETECTOR).size() != 0 ) {
            Sensor stepDetectorSensor = manager.getSensorList(Sensor.TYPE_STEP_DETECTOR).get(0);
            manager.registerListener(stepListener, stepDetectorSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            //textView4.setText("Sensor.TYPE_STEP_DETECTOR missing!");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        /* todo: zatial netreba

        builder.setLength( 0 );
        builder.append("x: ");
        builder.append(Math.round(event.values[0]*10)/10);
        builder.append("y: ");
        builder.append(Math.round(event.values[0]*10)/10);
        builder.append("z: ");
        builder.append(Math.round(event.values[0]*10)/10);

        textView.setText(builder.toString());
        */
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }

    public class CompassListener implements SensorEventListener {
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

                    azimut = (float) Math.round(Math.toDegrees(orientation[0]));
                    // todo: tieto dve mi zatial nebolo treba
                    //pitch = (float) Math.round(Math.toDegrees(orientation[1]));
                    //roll = (float) Math.round(Math.toDegrees(orientation[2]));
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
            int value = -1;

            if (values.length > 0) {
                value = (int) values[0];
            }

            if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                // todo: mozno je lepsie implementovat cez step_counter
                //textView3.setText("Step Counter Detected : " + value);
            } else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                stepDetectorCounter++;
                dv.draw();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public class DrawingView extends View
    {
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private Path circlePath;
        private Paint mPaint;

        private Point currentPoint;

        public DrawingView(Context c)
        {
            super(c);
            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(Color.GREEN);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(13);

            currentPoint = new Point(120, 50);

            mBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh)
        {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);

            touch_start(w/2, h/2);
            currentPoint = new Point(w/2, h/2);
            draw();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
            canvas.drawPath(circlePath, circlePaint);



            ShapeDrawable arrow = new ShapeDrawable( new RectShape() );
            int radius = 20;
            int diff = 10;
            int x = (int) currentPoint.getX();
            int y = (int) currentPoint.getY();
            System.out.println(x+" : "+y);
            arrow.setBounds(x-(radius/2)-diff, y-(radius/2)-diff, x + (radius/2)-diff, y + (radius/2)-diff);

            arrow.getPaint().setColor(0xff000000);

            //canvas.rotate(45);
            arrow.draw(canvas);
            //canvas.rotate(-45);

            ShapeDrawable bubble = new ShapeDrawable(new OvalShape());
            bubble.getPaint().setColor(0xff00cccc);
            bubble.setBounds(x-radius, y-radius, x + radius, y + radius);

            bubble.draw(canvas);
        }

        private void touch_start(float x, float y)
        {
            mPath.reset();
            mPath.moveTo(x, y);
        }

        public void draw()
        {
            float radians = Utils.azimutToRadians(azimut);

            Point newCurrentPoint = Utils.getNextPoint(currentPoint, radians, Utils.STEP_SIZE);

            mPath.quadTo(newCurrentPoint.getX(), newCurrentPoint.getY(), (currentPoint.getX() + newCurrentPoint.getX()) / 2, (currentPoint.getY() + newCurrentPoint.getY()) / 2);
            currentPoint = newCurrentPoint;

            circlePath.reset();
            //circlePath.addCircle(currentPoint.getX(), currentPoint.getY(), 30, Path.Direction.CW);

            invalidate();
        }
    }
}
