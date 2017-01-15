package com.nn.roomx.view.seekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.nn.roomx.R;

/**
 * Created by user on 2017-01-15.
 */

public class CustomSeekBar2  extends SeekBar {
    private Paint selected, unselected;
    private int halfSize = 10;
    private RectF position;

    public CustomSeekBar2(Context context, AttributeSet attrs) {
        super(context, attrs);
        selected = new Paint(Paint.ANTI_ALIAS_FLAG);
        selected.setColor(getContext().getResources().getColor(R.color.colorPrimary));
        selected.setStyle(Paint.Style.FILL);

        unselected = new Paint(Paint.ANTI_ALIAS_FLAG);
        unselected.setColor(getContext().getResources().getColor(android.R.color.darker_gray));
        selected.setStyle(Paint.Style.FILL);
        position = new RectF();
    }


    public CustomSeekBar2(Context context) {
        super(context);
        selected = new Paint(Paint.ANTI_ALIAS_FLAG);
        selected.setColor(getContext().getResources().getColor(R.color.colorPrimary));
        selected.setStyle(Paint.Style.FILL);
        unselected = new Paint(Paint.ANTI_ALIAS_FLAG);
        unselected.setColor(getContext().getResources().getColor(android.R.color.darker_gray));


        selected.setStyle(Paint.Style.FILL);
        position = new RectF();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        float margin = (canvas.getWidth() - (paddingLeft + getPaddingRight())) / 4;
        float halfHeight = (canvas.getHeight() + paddingTop) * .5f;
        for (int i = 0; i < 5; i++) {
            position.set(
                    paddingLeft + (i * margin) - halfSize,
                    halfHeight - halfSize,
                    paddingLeft + (i * margin) + halfSize,
                    halfHeight + halfSize);

            if(i == getProgress()){

            }else {

                canvas.drawOval(position, (i < getProgress()) ? selected : unselected);
            }
        }

    }

    public int getDotsSize() {
        return halfSize * 2;
    }

    public void setDotsSize(int dotsSize) {
        this.halfSize = dotsSize / 2;
    }
}