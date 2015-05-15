package com.chris.medusacam;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.FaceDetector;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Chris on 2/5/15.
 */
public class FaceClass {
    private static String TAG = "FaceClass";
    private static final int MAX_FACES = 10;

    public FaceClass(){}

    public int faceDetect(String img) {

        // read in bitmap and get related parameters
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(img);
        } catch (FileNotFoundException e) {
            MainActivity.transMsg(MainActivity.POST_MSG, "face detect: unable to read image!");
            e.printStackTrace();
        }
        Bitmap b  = BitmapFactory.decodeStream(fis);
        Bitmap bitmap = b.copy(Bitmap.Config.RGB_565, true);
        b.recycle();

        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        int faceNum = 0;
        Log.i(TAG, "img wid: " + imgWidth + "img hei: " + imgHeight);

        // setup face detector
        FaceDetector fd;
        FaceDetector.Face [] faces = new FaceDetector.Face[MAX_FACES];

        // do detection
        try {
            fd = new FaceDetector(imgWidth, imgHeight, MAX_FACES);
            faceNum = fd.findFaces(bitmap, faces);
            MainActivity.transMsg(MainActivity.POST_MSG, "there is(are) " + faceNum + " face(s)." );
        } catch (Exception e) {
            MainActivity.transMsg(MainActivity.POST_MSG, "error in face detection!");
            return 0;
        }

        // return result
        return faceNum;
    }

}
