package com.example.jorg.mobvzadanie1;

/**
 * Created by GabrielK on 12-Nov-16.
 */

public class Point
{
    private float x;
    private float y;

    public Point(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    @Override
    public String toString()
    {
        return "x => " + x + " y => " + y;
    }
}
