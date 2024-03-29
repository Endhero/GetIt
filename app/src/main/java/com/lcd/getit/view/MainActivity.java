package com.lcd.getit.view;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import com.lcd.getit.Constants;
import com.lcd.getit.R;
import com.lcd.smartdetectview.listener.detectlistener.GeneralObjectDetectListener;
import com.lcd.smartdetectview.wegit.DetectView;

import java.util.HashMap;

import entity.detectresult.GeneralObjectDetectResult;
import imageclassify.AipImageClassify;

import static com.lcd.getit.Constants.API_KEY;
import static com.lcd.getit.Constants.APP_ID;
import static com.lcd.getit.Constants.SECRET_KEY;


public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.PERMISSION_READ_EXTERNAL_STORAGE);
        }
        else
        {
            initView();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.PERMISSION_CAMERA);
        }
    }

    private void initView()
    {
        if (AipImageClassify.getAipImageClassify() == null)
            AipImageClassify.setAipImageClassify(APP_ID, API_KEY, SECRET_KEY);

        DetectView detectview = findViewById(R.id.detectview);
        final TextView textview = findViewById(R.id.textview_content);

        HashMap<String, String> hashmapOptions = new HashMap<String, String>();
        hashmapOptions.put("baike_num", "5");

        GeneralObjectDetectListener generalobjectdetectlisstener = new GeneralObjectDetectListener()
        {
            @Override
            public void onResultDetected(final GeneralObjectDetectResult generalobjectdetectresult)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            textview.setText(generalobjectdetectresult.getJSON().toString(2));
                        }
                        catch (Exception exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                });
            }
        };

        detectview.setOptions(hashmapOptions);
        detectview.setResultDetectedListener(generalobjectdetectlisstener);
        detectview.setIsShowArea(true);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case Constants.PERMISSION_READ_EXTERNAL_STORAGE:

                initView();

                break;
        }
    }
}
