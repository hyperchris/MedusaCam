package com.chris.medusacam;

import android.media.ExifInterface;

import java.io.IOException;

/**
 * Created by Chris on 1/8/15.
 */
public class ExifClass {
    public void writeExif(String path, String attr, String value){
        ExifInterface exif;
        try {
            exif = new ExifInterface(path);
            exif.setAttribute(attr,value);
            exif.saveAttributes();
            MainActivity.transMsg(MainActivity.POST_MSG, "EXIF stored\n" +
                    "--------------------");
        } catch (IOException e) {
            MainActivity.transMsg(MainActivity.POST_MSG, "EXIF failed to be stored!\n" +
                    "--------------------");
            e.printStackTrace();
        }
    }

    public String readExif(String path, String attr){
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            MainActivity.transMsg(MainActivity.POST_MSG, "EXIF failed to be read!\n" +
                    "--------------------");
            e.printStackTrace();
        }
        String value = exif.getAttribute(attr);
        return value;
    }
}