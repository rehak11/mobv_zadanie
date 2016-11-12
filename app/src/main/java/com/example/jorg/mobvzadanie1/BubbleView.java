package com.example.jorg.mobvzadanie1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Jorg on 10.10.2016.
 */

public class BubbleView extends View {
    private int radius = 100;
    private int x;
    private int y;
    private ShapeDrawable bubble;
    private ShapeDrawable arrow;

    public BubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createBubble();
    }

    private void createBubble() {
        bubble = new ShapeDrawable(new OvalShape());
        bubble.getPaint().setColor(0xff00cccc);

        arrow = new ShapeDrawable( new RectShape());
        arrow.getPaint().setColor(0xff000000);

        //this.setRotationX( 45 );
        //this.setRotationY( 45 );
        //this.setRotation( 45 );
    }

    public void rotateBubble(float degrees){
        this.setRotation((degrees + 45) % 360);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        x = this.getWidth()/2;
        y = this.getHeight()/2;

        bubble.setBounds(x-radius, y-radius, x + radius, y + radius);
        int diff = 50;
        arrow.setBounds(x-(radius/2)-diff, y-(radius/2)-diff, x + (radius/2)-diff, y + (radius/2)-diff);

        arrow.draw(canvas);
        bubble.draw(canvas);

    }
}
