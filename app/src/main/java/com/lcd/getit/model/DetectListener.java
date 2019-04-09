package com.lcd.getit.model;

import entity.detectresult.BaseDetectResult;

public interface DetectListener extends BaseListener
{
    void onResultDetected(BaseDetectResult basedetectresult);
}
