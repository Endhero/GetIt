package com.lcd.getit.Utils;

import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;

public class FormatTransUtil
{
    public static byte[] Nv21toYuv(byte[] b, int nWidth, int nHeight)
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        YuvImage yuvimage = new YuvImage(b, ImageFormat.NV21, nWidth, nHeight, null);
        yuvimage.compressToJpeg(new Rect(0, 0, nWidth, nHeight), 100, bytearrayoutputstream);// 80--JPG图片的质量[0-100],100最高

        return bytearrayoutputstream.toByteArray();
    }
}
