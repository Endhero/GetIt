package com.lcd.getit.view.wegit;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.lcd.getit.Constants;
import com.lcd.getit.Utils.FormatTransUtil;

import java.util.HashMap;

import detector.DetectorFactory;
import detector.MainObjectDetector;
import entity.detectresult.BaseDetectResult;
import entity.detectresult.MainObjectDetectResult;
import entity.info.resultinfo.MainObjectResultInfo;
import imageclassify.AipImageClassify;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class DetectSurfaceView extends SurfaceView implements SurfaceHolder.Callback,
        Camera.AutoFocusCallback, Camera.PreviewCallback
{
    private Context m_context;
    private SurfaceHolder m_surfaceholder;
    private Camera m_camera;
    private Boolean m_bDetect;
    private Handler m_hander;
    private int m_nInterval;
    private BaseDetectResult m_basedetectresult;
    private Class m_class;
    private HashMap<String, String> m_hashmapOptions;
    private MainObjectListener m_mainobjectlistener;
    private ResultDetectedListener m_resultdetectedlistener;

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
            m_hander.postDelayed(new Runnable() {
                @Override
                public void run() {
                    m_camera.autoFocus(DetectSurfaceView.this);
                    m_bDetect = true;
                }
            },m_nInterval);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        try
        {
            //data默认NV21格式，需要转换成Yuv格式才能提供给百度AI
            if (m_bDetect)
            {
                Camera.Size size = camera.getParameters().getPreviewSize();

                if (m_mainobjectlistener != null)
                {
                    HashMap<String, String> hashmapOptions = new HashMap<String, String>();
                    hashmapOptions.put("with_face", "0");

                    MainObjectDetectResult mainobjectdetectresult = ((MainObjectDetectResult) detect(MainObjectDetector.class, FormatTransUtil.Nv21toYuv(data, size.width, size.height), hashmapOptions));

                    if (mainobjectdetectresult != null)
                        m_mainobjectlistener.onMainObjectDetected(mainobjectdetectresult.getResultInfo());

                    m_basedetectresult = null;
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

                    detect(m_class, FormatTransUtil.Nv21toYuv(data, size.width, size.height), hashmapOptions);
                }

                if (m_resultdetectedlistener != null && m_basedetectresult != null)
                    m_resultdetectedlistener.onResultDetected(m_basedetectresult);

                m_bDetect = false;
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

   public void setMainObjectListener(MainObjectListener mainobjectlistener)
   {
       m_mainobjectlistener = mainobjectlistener;
   }

   public MainObjectListener getMainObjectListener()
   {
       return m_mainobjectlistener;
   }

    public void setResultDetectedListener(ResultDetectedListener resultdetectedlistener)
    {
        m_resultdetectedlistener = resultdetectedlistener;
    }

    public ResultDetectedListener getResultDetectedListener()
    {
        return m_resultdetectedlistener;
    }

    public void setAipImageClassify(String strAppId, String strAppKey, String strSecretKey)
    {
        AipImageClassify.setAipImageClassify(strAppId, strAppKey, strSecretKey);
    }

    public interface MainObjectListener
    {
        void onMainObjectDetected(MainObjectResultInfo mainobjectresultinfo);
    }

    public interface  ResultDetectedListener
    {
        void onResultDetected(BaseDetectResult basedetectresult);
    }

    private void init(Context context)
    {
        m_hander = new Handler();
        m_context = context;
        m_surfaceholder = getHolder();
        m_surfaceholder.addCallback(this);
        m_bDetect = false;
        m_nInterval = 1000;
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

    private BaseDetectResult detect(final Class clazz, final byte[] b, final HashMap<String ,String> hashmap) throws Exception
    {
        if (AipImageClassify.getAipImageClassify() == null)
            return null;

        m_basedetectresult = null;
        long lTime = System.currentTimeMillis();
        Observable.create(new ObservableOnSubscribe<BaseDetectResult>()
        {
            @Override
            public void subscribe(ObservableEmitter<BaseDetectResult> observableemitter) throws Exception
            {
                observableemitter.onNext(DetectorFactory.createDetector(clazz, b, hashmap).getDetectResult());
                observableemitter.onComplete();
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe(new Observer<BaseDetectResult>()
        {
            @Override
            public void onSubscribe(Disposable disposable)
            {
            }

            @Override
            public void onNext(BaseDetectResult result)
            {
                m_basedetectresult = result;
            }

            @Override
            public void onError(Throwable throwable)
            {
                Toast.makeText(m_context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete()
            {
            }
        });

        while (m_basedetectresult == null)
        {
            if (System.currentTimeMillis() - lTime < 1000 * 10)
                Thread.sleep(50);

            else
            {
                Toast.makeText(m_context, "Detect TimeOut", Toast.LENGTH_SHORT).show();
                break;
            }
        }

        return m_basedetectresult;
    }
}
