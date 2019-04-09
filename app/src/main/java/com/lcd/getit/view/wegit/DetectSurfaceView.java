package com.lcd.getit.view.wegit;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.lcd.getit.Utils.TransUtil;
import com.lcd.getit.model.DetectListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import detector.DetectorFactory;
import detector.MainObjectDetector;
import entity.detectresult.BaseDetectResult;
import entity.detectresult.MainObjectDetectResult;
import imageclassify.AipImageClassify;

import static android.content.Context.SENSOR_SERVICE;


public class DetectSurfaceView extends SurfaceView implements SurfaceHolder.Callback,
        Camera.AutoFocusCallback, Camera.PreviewCallback, SensorEventListener
{
    private static final int TYPE_SELF = 1;
    private static final int TYPE_OTHER = 2;

    private Context m_context;
    private SurfaceHolder m_surfaceholder;
    private Camera m_camera;
    private Boolean m_bNeedDetect;
    private Handler m_hander;
    private int m_nInterval;
    private BaseDetectResult m_basedetectresult;
    private Class m_class;
    private HashMap<String, String> m_hashmapOptions;
    private DetectListener m_detectlistenerMainObject;
    private DetectListener m_detectlistenerResult;
    private long m_lCurrentTime;
    private ExecutorService m_executerservice;
    private SensorManager m_sensormanager;

    public DetectSurfaceView(Context context)
    {
        super(context);

        init(context);
    }

    public DetectSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init(context);
    }

    public DetectSurfaceView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    @Override
    public void onAutoFocus(boolean bSuccess, Camera camera)
    {
        if (bSuccess)
        {
            m_camera.cancelAutoFocus();
            m_hander.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    m_camera.autoFocus(DetectSurfaceView.this);
                    m_bNeedDetect = true;
                }
            }, m_nInterval);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        try
        {
            if (System.currentTimeMillis() -  m_lCurrentTime < 200)
                return;

            else
            {
                m_lCurrentTime = System.currentTimeMillis();
            }

            //data默认NV21格式，需要转换成Yuv格式才能提供给百度AI
            if (m_bNeedDetect)
            {
                Camera.Size size = camera.getParameters().getPreviewSize();


                if (m_detectlistenerMainObject != null)
                {
                    HashMap<String, String> hashmapOptions = new HashMap<String, String>();
                    hashmapOptions.put("with_face", "0");

                    byte[] bRotate = null;

                    //竖屏需要先将图片旋转90度以正确获取图像主体区域
                    if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
                        bRotate = TransUtil.rotateYUV420Degree90(data, size.width, size.height);

                    else
                    {
                        bRotate = data;
                    }

                    MainObjectDetectResult mainobjectdetectresult = new MainObjectDetectResult();
                    detect(mainobjectdetectresult, m_detectlistenerMainObject, TYPE_SELF, MainObjectDetector.class, TransUtil.Nv21toYuv(bRotate, size.width, size.height), hashmapOptions);
                }

                if (m_class != null)
                {
                    HashMap<String, String> hashmapOptions = new HashMap<String, String>();

                    if (m_hashmapOptions == null)
                        hashmapOptions.put("baike_num", "5");

                    else
                    {
                        hashmapOptions = m_hashmapOptions;
                    }

                    detect(m_basedetectresult, m_detectlistenerResult, TYPE_OTHER, m_class, TransUtil.Nv21toYuv(data, size.width, size.height), hashmapOptions);
                }
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        m_executerservice = Executors.newSingleThreadExecutor();
        initCamera(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        initCamera(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        if (m_camera != null)
        {
            m_camera.setPreviewCallback(null);
            m_camera.autoFocus(null);
            m_camera.stopPreview();// 停止预览
            m_camera.release(); // 释放摄像头资源
            m_camera = null;
        }

        if (!m_executerservice.isShutdown())
        {
            m_executerservice.shutdownNow();
            m_executerservice = null;
        }

        if (m_sensormanager != null && m_sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
        {
            m_sensormanager.unregisterListener(this);
            m_sensormanager = null;
        }

        m_hander.removeCallbacksAndMessages(null);
    }

    public void setInterval(int n)
    {
        m_nInterval = n;
    }

    public int getInterval()
    {
        return m_nInterval;
    }

    public void setOptions(HashMap<String, String> hashmap)
    {
        m_hashmapOptions = hashmap;
    }

    public HashMap<String, String> getOptions()
    {
        return m_hashmapOptions;
    }

    public void setDetectClass(Class clazz)
    {
        m_class = clazz;
    }

    public Class getDetectClass()
    {
        return m_class;
    }

   public void setMainObjectListener(DetectListener detectlistenerMainObject)
   {
       m_detectlistenerMainObject = detectlistenerMainObject;
   }

   public DetectListener getMainObjectListener()
   {
       return m_detectlistenerMainObject;
   }

    public void setResultDetectedListener(DetectListener resultdetectedlistener)
    {
        m_detectlistenerResult = resultdetectedlistener;
    }

    public DetectListener getResultDetectedListener()
    {
        return m_detectlistenerResult;
    }

    public void setAipImageClassify(String strAppId, String strAppKey, String strSecretKey)
    {
        AipImageClassify.setAipImageClassify(strAppId, strAppKey, strSecretKey);
    }

    private void init(Context context)
    {
        m_hander = new Handler();
        m_context = context;
        m_surfaceholder = getHolder();
        m_surfaceholder.addCallback(this);
        m_bNeedDetect = false;
        m_nInterval = 1000;
        m_lCurrentTime = System.currentTimeMillis();
        m_sensormanager = (SensorManager) m_context.getSystemService(SENSOR_SERVICE);
    }

    private void initCamera(SurfaceHolder holder)
    {
        try
        {
            if (m_camera == null)
            {
                if (Camera.getNumberOfCameras() < 1)
                {
                    Toast.makeText(m_context,"不支持拍照", Toast.LENGTH_LONG).show();
                    return;
                }

                //注册传感器(加速度)
                if (m_sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
                    m_sensormanager.registerListener(this, m_sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

                m_camera = Camera.open(0);

                Camera.Parameters parameters = m_camera.getParameters();

                //自动对焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                //照片竖屏
                parameters.set("orientation", "portrait");
                parameters.set("rotation", 90);

                m_camera.setParameters(parameters);

                //预览方向
                if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
                    m_camera.setDisplayOrientation(90);

                else
                    m_camera.setDisplayOrientation(0);

                //开启预览
                m_camera.setPreviewDisplay(holder);
                m_camera.startPreview();
                //设置对焦回调，必须在开启预览之后
                m_camera.autoFocus(this);
                //设置预览回调
                m_camera.setPreviewCallback(this);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void detect(BaseDetectResult basedetectresult, DetectListener detectlistener, int nType, Class clazz, byte[] b, HashMap<String ,String> hashmap) throws Exception
    {
        if (AipImageClassify.getAipImageClassify() == null)
            return;

        DetectThread detectthread = new DetectThread(basedetectresult, detectlistener, nType, clazz, b, hashmap);

        m_executerservice.execute(detectthread);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
//        float fX = Math.abs(event.values[0]);
////        float fY = Math.abs(event.values[1]);
////        float fZ = Math.abs(event.values[2]);
////
////        if (fX > 1 || fY > 4)
////        {
////            MainObjectDetectResult mainobjectdetectresult = new MainObjectDetectResult();
////
////            if (m_detectlistenerMainObject != null)
////                m_detectlistenerMainObject.onResultDetected(mainobjectdetectresult);
////        }
////
////        Log.d("Detect", "X:" + fX);
////        Log.d("Detect", "Y:" + fY);
////        Log.d("Detect", "Z:" + fZ);
    }

    //传感器精度变化
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    class DetectThread extends Thread
    {
        private BaseDetectResult m_basedetectresult;
        private Class m_class;
        private byte[] m_byte;
        private HashMap<String, String> m_hashmapOptions;
        private DetectListener m_detectlistener;
        private int m_nType;

        public DetectThread(BaseDetectResult basedetectresult, DetectListener detectListener, int nType, Class clazz, byte[] bData, HashMap<String, String> hashmap)
        {
            m_basedetectresult = basedetectresult;
            m_class = clazz;
            m_byte = bData;
            m_hashmapOptions = hashmap;
            m_detectlistener =detectListener;
            m_nType = nType;
        }

        @Override
        public void run()
        {
            try
            {
                if (m_bNeedDetect)
                {
                    m_basedetectresult = null;

                    long lTime = System.currentTimeMillis();
                    Log.d("Detect", "DetectStart:" + m_nType + " " + System.currentTimeMillis());
                    m_basedetectresult = DetectorFactory.createDetector(m_class, m_byte, m_hashmapOptions).getDetectResult();

                    while (m_basedetectresult == null)
                    {
                        if (System.currentTimeMillis() - lTime < 1000 * 1)
                            Thread.sleep(50);

                        else
                        {
                            Toast.makeText(m_context, "Detect TimeOut", Toast.LENGTH_SHORT).show();

                            break;
                        }
                    }

                    if (m_detectlistener != null && m_basedetectresult != null)
                    {
                        Log.d("Detect", "DetectFinish:" + m_nType + " " + System.currentTimeMillis());
                        m_detectlistener.onResultDetected(m_basedetectresult);

                        if (m_nType == TYPE_OTHER)
                        {
                            m_bNeedDetect = false;

                            if (!m_executerservice.isShutdown())
                            {
                                m_executerservice.shutdownNow();
                                m_executerservice = Executors.newSingleThreadExecutor();
                                System.gc();
                            }
                        }
                    }
                }
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }
}
