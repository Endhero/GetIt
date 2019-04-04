package com.lcd.getit.view.wegit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import entity.info.resultinfo.MainObjectResultInfo;

public class DetectFinderView extends View
{
    private Paint m_paint;
    private int m_nLeft;
    private int m_nTop;
    private int m_nRight;
    private int m_nBottom;

    public DetectFinderView(Context context)
    {
        super(context);

        init();
    }

    public DetectFinderView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init();
    }

    public DetectFinderView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init()
    {
        m_paint = new Paint();
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        m_paint.reset();
        //防锯齿
        m_paint.setAntiAlias(true);
        //防抖动
        m_paint.setDither(true);
        m_paint.setStyle(Paint.Style.STROKE);
        m_paint.setColor(Color.RED);
        m_paint.setStrokeWidth(1);

        canvas.drawRect(m_nLeft, m_nTop, m_nRight, m_nBottom, m_paint);
        invalidate();
    }

    public void setFinderLocation(MainObjectResultInfo mainobjectresultinfo)
    {
        int nLeft = mainobjectresultinfo.getLeft();
        int nTop = mainobjectresultinfo.getTop();
        int nWidth = mainobjectresultinfo.getWidth();
        int nHeight = mainobjectresultinfo.getHeight();


        setFinderLoaction(nLeft, nTop, nLeft + nWidth, nTop + nHeight);
    }

    public void setFinderLoaction(int nLeft, int nTop, int nRight, int nBottom)
    {
        m_nLeft = nLeft;
        m_nTop = nTop;
        m_nRight = nRight;
        m_nBottom = nBottom;
    }
}
