package com.lcd.getit.model.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.lcd.getit.Constants;

import java.util.HashMap;

import detector.DetectorFactory;
import entity.detectresult.BaseDetectResult;
import imageclassify.AipImageClassify;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DetectService extends Service
{
    private IBinder m_binder = new DetectBinder();
    private BaseDetectResult m_basedetectresult;

    public void onCreate()
    {
        super.onCreate();

        if (AipImageClassify.getAipImageClassify() == null)
            AipImageClassify.setAipImageClassify(Constants.APP_ID, Constants.API_KEY, Constants.SECRET_KEY);
    }

    public class DetectBinder extends Binder
    {
        public BaseDetectResult getDetectResult(Class clazz, String str, HashMap<String ,String> hashmap) throws Exception
        {
            return DetectService.this.getDetectResult(clazz, str, hashmap);
        }

        public BaseDetectResult getDetectResult(Class clazz, byte[] b, HashMap<String ,String> hashmap) throws Exception
        {
            return DetectService.this.getDetectResult(clazz, b, hashmap);
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return m_binder;
    }

    private BaseDetectResult getDetectResult(final Class clazz, final String str, final HashMap<String ,String> hashmap) throws Exception
    {
        m_basedetectresult = null;
        long lTime = System.currentTimeMillis();
        Observable.create(new ObservableOnSubscribe<BaseDetectResult>()
        {
            @Override
            public void subscribe(ObservableEmitter<BaseDetectResult> observableemitter) throws Exception
            {
                observableemitter.onNext(DetectorFactory.createDetector(clazz, str, hashmap).getDetectResult());
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
                Toast.makeText(getApplicationContext(), "DetectService Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete()
            {
            }
        });

        while (m_basedetectresult == null)
        {
            if (System.currentTimeMillis() - lTime < 1000 * 10)
                Thread.sleep(100);

            else
            {
                Toast.makeText(getBaseContext(), "DetectService TimeOut", Toast.LENGTH_SHORT).show();
                break;
            }
        }

        return m_basedetectresult;
    }

    private BaseDetectResult getDetectResult(final Class clazz, final byte[] b, final HashMap<String ,String> hashmap) throws Exception
    {
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
            }

            @Override
            public void onComplete()
            {
            }
        });

        while (m_basedetectresult == null)
        {
            if (System.currentTimeMillis() - lTime < 1000 * 10)
                Thread.sleep(100);

            else
            {
                Toast.makeText(getBaseContext(), "DetectService TimeOut", Toast.LENGTH_SHORT).show();
                break;
            }
        }

        return m_basedetectresult;
    }
}
