package com.example.jorg.mobvzadanie1;

import android.util.Log;

/**
 * Created by GabrielK on 12-Nov-16.
 */

public class Utils
{
    public static final float RADIANS_PER_DEGREE = 0.01745329252f;
    public static final int STEP_SIZE = 25;

    // pocitanie podla: http://stackoverflow.com/questions/839899/how-do-i-calculate-a-point-on-a-circle-s-circumference
    public static Point getNextPoint(Point currentPoint, double radians, int stepSize)
    {
        float newX = (float)(currentPoint.getX() + stepSize*Math.cos(radians));
        float newY = (float)(currentPoint.getY() + stepSize*Math.sin(radians));

        Point newPoint = new Point(newX, newY);

        return new Point(newX, newY);
    }

    public static float azimutToRadians(float azimut)
    {
        float degrees = azimutToDegrees(azimut);

        return degreesToRadians(degrees);
    }

    private static float azimutToDegrees(float azimut)
    {
        if (azimut < 0)
            return azimut + 360;

        return azimut;
    }

    private static float degreesToRadians(float degrees)
    {
        return RADIANS_PER_DEGREE * degrees;
    }
}
