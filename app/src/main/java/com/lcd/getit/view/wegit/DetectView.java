package com.lcd.getit.view.wegit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import entity.detectresult.BaseDetectResult;
import entity.info.resultinfo.MainObjectResultInfo;

import com.lcd.getit.model.DetectListener;


import java.util.HashMap;

public class DetectView extends FrameLayout
{
    private DetectSurfaceView m_detectsurfaceview;
    private DetectFinderView m_detectfinderview;

    public DetectView(Context context)
    {
        super(context);

        initView(context);
    }

    public DetectView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        initView(context);
    }

    public DetectView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        initView(context);
    }

    private void initView(Context context)
    {
        m_detectsurfaceview = new DetectSurfaceView(context);
        m_detectfinderview = new DetectFinderView(context);

        DetectListener detectlistenerMainObject = new DetectListener()
        {
            @Override
            public void onResultDetected(BaseDetectResult basedetectresult)
            {
                MainObjectResultInfo mainobjectresultinfo = null;

                if (basedetectresult.getJSON() != null)
                    mainobjectresultinfo = new MainObjectResultInfo(basedetectresult.getJSON());

                else
                {
                    mainobjectresultinfo = new MainObjectResultInfo();
                    mainobjectresultinfo.setTop(0);
                    mainobjectresultinfo.setLeft(0);
                    mainobjectresultinfo.setWidth(0);
                    mainobjectresultinfo.setHeight(0);
                }

                m_detectfinderview.setFinderLocation(mainobjectresultinfo);
            }
        };

        m_detectsurfaceview.setMainObjectListener(detectlistenerMainObject);

        addView(m_detectsurfaceview);
        addView(m_detectfinderview);
    }

    public void setInterval(int n)
    {
        m_detectsurfaceview.setInterval(n);
    }

    public int getInterval()
    {
        return m_detectsurfaceview.getInterval();
    }

    public void setResultDetectedListener(DetectListener resultdetectedlistener)
    {
        m_detectsurfaceview.setResultDetectedListener(resultdetectedlistener);
    }

    public DetectListener getResultDetectedListener()
    {
        return m_detectsurfaceview.getResultDetectedListener();
    }

    public void setOptions(HashMap<String, String> hashmap)
    {
        m_detectsurfaceview.setOptions(hashmap);
    }

    public HashMap<String, String> getOptions()
    {
        return m_detectsurfaceview.getOptions();
    }

    public void setDetectClass(Class clazz)
    {
        m_detectsurfaceview.setDetectClass(clazz);
    }

    public Class getDetectClass()
    {
        return m_detectsurfaceview.getDetectClass();
    }

    public void setAipImageClassify(String strAppId, String strAppKey, String strSecretKey)
    {
        m_detectsurfaceview.setAipImageClassify(strAppId, strAppKey, strSecretKey);
    }
}
