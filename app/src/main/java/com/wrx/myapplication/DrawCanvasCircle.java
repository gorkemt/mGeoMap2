package com.wrx.myapplication;

/**
 * Created by gtolan on 12/2/2015.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawCanvasCircle extends View{
    public DrawCanvasCircle(Context mContext) {
        super(mContext);
    }
    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.BLUE);
        canvas.drawCircle(20, 20, 15, paint);
    }

}
