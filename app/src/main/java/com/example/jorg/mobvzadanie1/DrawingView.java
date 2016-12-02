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
import android.view.MotionEvent;
import android.view.View;

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
    private Paint startPointPaint;

    private Point currentPoint;

    private int leftBounds;
    private int rightBounds;
    private int topBounds;
    private int bottomBounds;

    private float azimut;

    public DrawingView(Context c)
    {
        super(c);
        context = c;
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
        startPointPaint = new Paint();
        startPointPaint.setColor(Color.GREEN);

        currentPoint = new Point(120, 50);

        mBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        arrow = new ShapeDrawable(new RectShape());
        bubble = new ShapeDrawable(new OvalShape());

        azimut = 0;
    }

    public float mPosX;
    public float mPosY;
    public float mLastTouchX;
    public float mLastTouchY;
    int mActivePointerId;

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                final float x = event.getX();
                final float y = event.getY();
                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = event.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
            {
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;
                mPosX -= dx;
                mPosY -= dy;
                scrollTo((int) mPosX, (int) mPosY);
                invalidate();

                mLastTouchX = x;
                mLastTouchY = y;
                break;
            }
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        touch_start(w / 2, h / 2);
        currentPoint = new Point(w / 2, h / 2);

        mPosX = 0;
        mPosY = 0;

        leftBounds = 200;
        rightBounds = w - 200;
        topBounds = 200;
        bottomBounds = h - 200;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
        canvas.drawPath(circlePath, circlePaint);
        canvas.drawCircle(mBitmap.getWidth()/2,mBitmap.getHeight()/2,18,startPointPaint);

        int radius = 20;
        int diff = 10;
        int x = (int) currentPoint.getX();
        int y = (int) currentPoint.getY();

        arrow.setBounds(x-(radius/2)-diff, y-(radius/2)-diff, x + (radius/2)-diff, y + (radius/2)-diff);
        arrow.getPaint().setColor(0xff000000);

        bubble.getPaint().setColor(0xff00cccc);
        bubble.setBounds(x-radius, y-radius, x + radius, y + radius);



        canvas.save();
        canvas.rotate(((azimut + 135) + 360) % 360, x, y);


        arrow.draw(canvas);
        bubble.draw(canvas);


        canvas.restore();
    }

    private void touch_start(float x, float y)
    {
        mPath.reset();
        mPath.moveTo(x, y);
    }

    public void move()
    {
        float radians = Utils.azimutToRadians(this.azimut);

        Point newCurrentPoint = Utils.getNextPoint(currentPoint, radians, Utils.STEP_SIZE);

        scrollView(newCurrentPoint);

        mPath.quadTo(newCurrentPoint.getX(), newCurrentPoint.getY(), (currentPoint.getX() + newCurrentPoint.getX()) / 2, (currentPoint.getY() + newCurrentPoint.getY()) / 2);
        currentPoint = newCurrentPoint;

        invalidate();
    }

    private ShapeDrawable arrow;
    private ShapeDrawable bubble;

    public void applyAzimut(float azimut)
    {
        this.azimut = azimut;
        invalidate();
    }

    private void scrollView(Point newPoint)
    {
        if (newPoint.getX() < leftBounds)
        {
            scrollBy(-Utils.STEP_SIZE, 0);
            leftBounds -= Utils.STEP_SIZE;
            rightBounds -= Utils.STEP_SIZE;
            mPosX -= Utils.STEP_SIZE;
        }
        if (newPoint.getY() < topBounds)
        {
            scrollBy(0, -Utils.STEP_SIZE);
            topBounds -= Utils.STEP_SIZE;
            bottomBounds -= Utils.STEP_SIZE;
            mPosY -= Utils.STEP_SIZE;
        }
        if (newPoint.getX() > rightBounds)
        {
            scrollBy(Utils.STEP_SIZE, 0);
            rightBounds += Utils.STEP_SIZE;
            leftBounds += Utils.STEP_SIZE;
            mPosX += Utils.STEP_SIZE;
        }
        if (newPoint.getY() > bottomBounds)
        {
            scrollBy(0, Utils.STEP_SIZE);
            bottomBounds += Utils.STEP_SIZE;
            topBounds += Utils.STEP_SIZE;
            mPosY += Utils.STEP_SIZE;
        }
    }
}