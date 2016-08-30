package com.chris.medusacam;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Exchanger;

/**
 * Author: Xiaochen (Chris) Liu
 * Date: 0602
 * Version: v1.2
 * Function: Record the metadata of photo and video
 * Comments:
 *      Not added yet: blurry detection
 *      Manual tag: vehicle, indoor/outdoor
 *      Problem: N/A
 */
public class MainActivity extends Activity {

    /** Final Values */
    public static final String JPEG_FILE_PREFIX = "IMG_";
    public static final String JPEG_FILE_SUFFIX = ".jpg";
    public static final String MP4_FILE_PREFIX = "VID_";
    public static final String MP4_FILE_SUFFIX = ".mp4";
    public static final String CAMERA_DIR = "/DCIM/";
    public static final String DATA_DIR = "MedusaCam_data";
    public static final String ALBUM_NAME = "Camera";
    public static final String PHOTO_RECORD_NAME = "Metadata.txt";

    public static final int ACTION_TAKE_PHOTO = 1;      // taking pictures
    public static final int ACTION_TAKE_VIDEO = 2;      // taking videos
    public static final int ACTION_META_VIDEO = 3;
    public static final int ACTION_TAKE_DATA = 4;


    public static final int POST_MSG = 1;               // send message to handler
    public static final int POST_TOAST = 2;
    public static final int ADD_GALLERY = 3;

    /** Functional Classes */
    public static Handler msgHandler;           // handler for displaying debug text
    public static SensorClass sensorClass;      // class of sensor controller [modify if need to process sensor data]
    public static ThreadClass threadClass;      // thread controlling class [modify if need to define the func of thread]
    public static FileClass fileClass;          // file controlling class
    public static ExifClass exifClass;          // editing EXIF data
    public static FaceClass faceClass;          // detect faces in a photo
    public static BlurClass blurClass;          // detect whether the photo is blurry or not

    private Camera mCamera;

    public static String filePath;               // path that the photo is stored in
    public static String dataPath;
    private LocationManager locMan;
    private boolean locGood;
    private MyLocationListener locLis;
    public static double lng,lat,acc;             // gps values
    public static double thetaH;                  // camera parameters
    private boolean dataRecording;                  // indicator of the status of photo/video recording

    /** Variables */
    public static String sceneTag;
    public static int cars;                       // number or cars
    public static boolean showGPSLocation = false; //boolean to determine whether to display GPS location or not
    public static boolean showAccSensor = false;
    public static boolean showAngleOfViewSensor = false;
    public static boolean showBearingInfoSensor = false;
    public static boolean showCarSensor = false;
    public static boolean showLightSensor = false;
    public static boolean showMagSensor = false;
    public static boolean isDataRecording = false;


    public static Timer timerClassObj;             //timer class to for displaying sensor data after every second
    public static Timer dataRecordingTimerClassObj; //timer class for recording data multiple times per second
    public int counter = 0;


    /** UI widgets */
    private TextView debug_txt;        // textView that shows debug information
    private Button photo_btn;          // button
    private Button video_btn;          // button
    private Button data_btn;           //Button to record data
    private ScrollView scroll;
    private RadioGroup radioGroup;
    private RadioButton radioButton1, radioButton2;
    private TextView sb_name;
    private SeekBar seekBar;
    //Text Views
    private TextView gpsLocationText;    //textView to show the GPS Location
    private TextView numberOfCarsText;
    private TextView angleOfViewText;
    private TextView lightSensorText;
    private TextView accSensorText;
    private TextView magSensorText;
    private TextView bearingInfoText;

    /** Utility Functions */

    // public func for posting info in debug text
    public static void transMsg(int msgType, String msgString){
        Message msg = msgHandler.obtainMessage(msgType);
        Bundle bundle = new Bundle();
        bundle.putString("transMsg", msgString);
        msg.setData(bundle);
        msgHandler.sendMessage(msg);
    }

    // scroll the scroll to the bottom
    private void scrollToBottom (final ScrollView scroll, final View inner) {
        if (scroll == null || inner == null)
            return;
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    // init msg Handler
    private void initMsgHandler(){
        msgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String comingMsg = msg.getData().getString("transMsg");
                switch (msg.what) {
                    case POST_MSG:              // display debug msg on screen
                        debug_txt.append("\n" + comingMsg);
                        break;
                    case POST_TOAST:            // display toast on screen
                        Toast.makeText(MainActivity.this, comingMsg, Toast.LENGTH_SHORT).show();
                        break;
                    case ADD_GALLERY:
                        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                        File f = new File(comingMsg);
                        Uri contentUri = Uri.fromFile(f);
                        mediaScanIntent.setData(contentUri);
                        sendBroadcast(mediaScanIntent);
                        break;
                    default:
                        break;
                }
                scrollToBottom(scroll, debug_txt);      // scroll to bottom
            }
        };
    }

    /*
     GPS related
     */
    public class MyLocationListener implements LocationListener {



        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                lng = loc.getLongitude();
                lat = loc.getLatitude();
                acc = loc.getAccuracy();
              //  Log.i("main GPS", "loc changed: " + lng + " " + lat + " " + acc);

                double lngDec = (double) Math.round(lng * 10d) / 10d;
                double latDec = (double) Math.round(lat * 10d) / 10d;
                double accDec = (double) Math.round(acc * 10d) / 10d;


                if (lng == 0.0 || lat == 0.0) {
                    transMsg(POST_MSG, "GPS initializing......... please wait");
                    locGood = false;
                    photo_btn.setEnabled(false);
                    video_btn.setEnabled(false);
                }
                else {
                    if (locGood == false) {
                        locGood = true;
                        transMsg(POST_MSG, "GPS ready! You can take photos, videos or data :)");
                    }
                    photo_btn.setEnabled(true);
                    video_btn.setEnabled(true);
                    data_btn.setEnabled(true);
                }
            }
        }
        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    public void showSensorData (View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId())
        {
            case R.id.gps_location_check:
                if (checked)
                    showGPSLocation = true;
                else
                    showGPSLocation = false;
                break;

            case R.id.accSensor_check:
                if (checked)
                    showAccSensor = true;
                else
                    showAccSensor = false;
                break;

            case R.id.angleOfView_check:
                if (checked)
                    showAngleOfViewSensor = true;
                else
                    showAngleOfViewSensor = false;
                break;

            case R.id.bearingInfo_check:
                if (checked)
                    showBearingInfoSensor = true;
                else
                    showBearingInfoSensor = false;
                break;

           /* case R.id.cars_check:
                if (checked)
                    showCarSensor = true;
                else
                    showCarSensor = false;
                break;
*/
            case R.id.lightSensor_check:
                if (checked)
                    showLightSensor = true;
                else
                    showLightSensor = false;
                break;

            case R.id.magSensor_check:
                if (checked)
                    showMagSensor = true;
                else
                    showMagSensor = false;
                break;

            default:
                Log.d ("Error", "something wrong");
                        break;


        }

       }

    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){      //if gps is disabled
            Log.i("main","try to turn on GPS");
            Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("enabled", true);
            sendBroadcast(intent);
            transMsg(POST_TOAST,"GPS is on");
        }
    }

    /*
     SYSTEM FUNCTIONS
     */

    @Override
    protected void onResume() {
        super.onResume();
        if(sensorClass != null && dataRecording == false) {
            sensorClass.openSensor(SensorClass.LIGHT_ON, SensorManager.SENSOR_DELAY_NORMAL);
            sensorClass.openSensor(SensorClass.ACC_ON, SensorManager.SENSOR_DELAY_NORMAL);
            sensorClass.openSensor(SensorClass.MAG_ON, SensorManager.SENSOR_DELAY_NORMAL);
        }
/*
        String currentProvider = LocationManager.NETWORK_PROVIDER;
        Log.d("on resume", "CurrentProvider: " + currentProvider);
        Location lastKnownLocation = locMan.getLastKnownLocation(currentProvider);
        if (lastKnownLocation != null) {
            Log.d("on resume", "LastKnownLocation: "
                    + lastKnownLocation.getLongitude() + " "
                    + lastKnownLocation.getLatitude() + " "
                    + lastKnownLocation.getAccuracy());
        } else {
            Log.d("on resume", "Last Location unknown!");
        }
        locMan.requestLocationUpdates(currentProvider, 0, 0, locLis);

        */
    }

    @Override
    protected void onDestroy ()
    {
        Log.d("on Destroy", "Start Destroying....");
                if (locMan != null)
                    locMan.removeUpdates(locLis);
        Log.d("on pause", "LocationListener removed");
        super.onDestroy();
    }



    @Override
    protected void onPause() {
        super.onPause();
        if(sensorClass != null && dataRecording == false) {
            sensorClass.sensorManager.unregisterListener(sensorClass.mySensorListener);
        }
       // locMan.removeUpdates(locLis);
       // Log.d("on pause", "LocationListener removed.");
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        Log.i("main.onCreate","on create");

        // initiate UI
        debug_txt = (TextView)findViewById(R.id.debug_txt);
        photo_btn = (Button)findViewById(R.id.photo_btn);
        video_btn = (Button)findViewById(R.id.video_btn);
        data_btn = (Button) findViewById(R.id.data_btn);
        scroll = (ScrollView)findViewById(R.id.scroll);
        radioGroup = (RadioGroup)findViewById(R.id.radiogroup);
        radioButton1 = (RadioButton)findViewById(R.id.radiobutton1);
        radioButton2 = (RadioButton)findViewById(R.id.radiobutton2);
        sb_name = (TextView)findViewById(R.id.sb_name);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        gpsLocationText = (TextView) findViewById(R.id.gps_location_text);
     //   numberOfCarsText = (TextView) findViewById(R.id.cars_text);
        angleOfViewText = (TextView) findViewById(R.id.angleOfView_text);
        lightSensorText = (TextView) findViewById(R.id.lightSensor_text);
        accSensorText = (TextView) findViewById(R.id.accSensor_text);
        magSensorText = (TextView) findViewById(R.id.magSensor_text);
        bearingInfoText = (TextView) findViewById(R.id.bearingInfo_text);



        photo_btn.setEnabled(false);
        video_btn.setEnabled(false);        // video function disabled
        data_btn.setEnabled(false);

        initMsgHandler();           // create msg handler

        // Specify the UI widgets
        sceneTag = "Indoor";
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == radioButton1.getId()) { sceneTag = "Indoor"; }
                else if (checkedId == radioButton2.getId()) { sceneTag = "Outdoor"; }
            }
        });

        //One Second Timer to Display Sensor Reading
        timerClassObj = new Timer ();

        timerClassObj.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        displaySensorData ();
                    }
                },
                300,
                300
        );


        dataRecordingTimerClassObj = new Timer();

        dataRecordingTimerClassObj.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            takeData(ALBUM_NAME);
                        }
                        catch (Exception e) {}
                    }
                },
                5,
                1
        );


        /*
          init functional classes
         */
        sensorClass = new SensorClass();
        threadClass = new ThreadClass();
        fileClass = new FileClass();
        // exifClass = new ExifClass();
        faceClass = new FaceClass();
        blurClass = new BlurClass();

        /*
          set click listener
         */
        photo_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                try {
                    takePhoto(ALBUM_NAME);      // input album name
                }catch (IOException e) {
                    e.printStackTrace();
                    transMsg(POST_MSG,"cannot take photo...");
                }
            }
        });

        video_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                try {
                    takeVideo(ALBUM_NAME);      // input album name
                }catch (IOException e) {
                    e.printStackTrace();
                    transMsg(POST_MSG,"cannot take photo...");
                }
            }
        });

        data_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                try {
                    Log.v("Data Button", "Clicked");
                    isDataRecording = !isDataRecording;
                    if (isDataRecording)
                        transMsg(POST_MSG, "Started recording data");
                    else
                        transMsg(POST_MSG, "Stopped recording data");
                    //takeData(ALBUM_NAME);      // input album name
                }catch (Exception e) {
                    e.printStackTrace();
                    transMsg(POST_MSG,"cannot record data...");
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cars = seekBar.getProgress();
                if(cars < 6) {
                    sb_name.setText("\n" + cars + " cars in the photo");
                }
                else {
                    sb_name.setText("\n" + "6 or more cars in the photo");
                }
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
        });

        // sensors
        sensorClass.sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorClass.openSensor(SensorClass.LIGHT_ON, SensorManager.SENSOR_DELAY_NORMAL);
        sensorClass.openSensor(SensorClass.ACC_ON, SensorManager.SENSOR_DELAY_NORMAL);
        sensorClass.openSensor(SensorClass.MAG_ON, SensorManager.SENSOR_DELAY_NORMAL);

        // gps
        turnGPSOn();
        locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locLis = new MyLocationListener();
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locLis);

        // camera
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        thetaH = Math.toRadians(parameters.getHorizontalViewAngle());
        mCamera.release();

        // init the data storage path
        dataPath = Environment.getExternalStorageDirectory() + CAMERA_DIR + DATA_DIR;

        transMsg(POST_MSG, "GPS initializing......... please wait");
        locGood = false;
    }


    /*
    * Function to Store Sensor Data multiple times per Second*/







    /*



    Function to display the Sensor Data on the Screen after one Second


     */


    public void displaySensorData ()
    {







        runOnUiThread(new Runnable() {
            public void run() {

               /* Log.v("Debug info", cars +                                                          // num of cars
                        "\n Scene Tag " + sceneTag +                                                // scene tag
                        "\n Theta " + String.valueOf(MainActivity.thetaH) +                     // angle of view
                        "\n Light Sensor " + sensorClass.getSensorData(SensorClass.LIGHT_ON) +         // light sensor
                        "\n Acc Value " + sensorClass.getSensorData(SensorClass.ACC_ON) +           // acc value (3) - x,y,z
                        "\n Mag Value " + sensorClass.getSensorData(SensorClass.MAG_ON) +           // mag value (3) - x,y,z
                        "\n Bearing Info " + sensorClass.getBearingInfo() +                            // bearing (3) - azimuth, pitch, roll
                        "\n GPS Location " + lng + " " + lat + " " + acc);

*/




                String accSensor = sensorClass.getSensorData(sensorClass.ACC_ON);
                String delimiter = "[ ]+";

                String angleOfViewValue = String.valueOf(MainActivity.thetaH);

                String bearingInfoValue = sensorClass.getBearingInfo();

                String lightSensorValue = sensorClass.getSensorData(SensorClass.LIGHT_ON);

                String  magSensorValue = sensorClass.getSensorData(sensorClass.MAG_ON);


                String[] accSensorTokens = accSensor.split(delimiter);
                String[] bearingInfoTokens = bearingInfoValue.split(delimiter);
                String[] magValueTokens = magSensorValue.split(delimiter);



                StringBuffer accSensorString = new StringBuffer("");
                for (int valueCounter = 0; valueCounter < accSensorTokens.length; valueCounter++)
                    accSensorString.append( String.format("%.2f", (Float.parseFloat(accSensorTokens[valueCounter]))) + " ");

                StringBuffer bearingString = new StringBuffer("");
                for (int valueCounter = 0; valueCounter < bearingInfoTokens.length; valueCounter++)
                    bearingString.append( String.format("%.2f", (Float.parseFloat(bearingInfoTokens[valueCounter]))) + " ");


                StringBuffer magString = new StringBuffer("");
               // for (int valueCounter = 0; valueCounter < magValueTokens.length; valueCounter++)
                  //  magString.append( String.format("%.2f", (Float.parseFloat(magValueTokens[valueCounter]))) + " ");




                if (showGPSLocation)
                MainActivity.this.gpsLocationText.setText(String.format("%.2f", lat) + " " + String.format("%.2f", lng) + " " + String.format("%.2f", acc));

                if (showAccSensor)
                    MainActivity.this.accSensorText.setText(accSensorString);

                if (showAngleOfViewSensor)
                    MainActivity.this.angleOfViewText.setText (String.format("%.2f",MainActivity.thetaH));

                if (showBearingInfoSensor)
                    MainActivity.this.bearingInfoText.setText(bearingString);

                //if (showCarSensor)
                  //  MainActivity.this.numberOfCarsText.setText("" + cars);

                if (showLightSensor)
                    MainActivity.this.lightSensorText.setText("" + sensorClass.getSensorData(SensorClass.LIGHT_ON));

                if (showMagSensor)
                    MainActivity.this.magSensorText.setText(magString);




                /*

                cars +                                                          // num of cars
                        " " + faceNum +                                                 // num of faces
                        " " + blurry +                                                  // blurry or not (0 or 1)
                        " " + sceneTag +                                                // scene tag
                        " " + String.valueOf(MainActivity.thetaH) +                     // angle of view
                        " " + sensorClass.getSensorData(SensorClass.LIGHT_ON) +         // light sensor
                        " " + sensorClass.getSensorData(SensorClass.ACC_ON) +           // acc value (3) - x,y,z
                        " " + sensorClass.getSensorData(SensorClass.MAG_ON) +           // mag value (3) - x,y,z
                        " " + sensorClass.getBearingInfo() +                            // bearing (3) - azimuth, pitch, roll
                        " " + lng + " " + lat + " " + acc;

                 */
            }
        });


    }






    /*
     Action when hit "take photo" button
     */
    public void takePhoto(String albumName) throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f;
        String tag = "Camera Profiler:";
        Log.d (tag, "Initializing");
        try {
            Log.v (tag, "Attemping to take a picture");
            dataRecording = true;
            f = fileClass.setUpMediaFile(albumName, ACTION_TAKE_PHOTO);
            if (f == null)
                return;
            filePath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            Log.v (tag, "Failed to take a picture");
            transMsg(POST_MSG,"cannot take photo...io problem");
            e.printStackTrace();
            return;
        }
        startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
    }

    /*
     Action when hit "take video" button
     */
    public void takeVideo(String albumName) throws IOException {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File f;
        try {
            dataRecording = true;
            f = fileClass.setUpMediaFile(albumName, ACTION_TAKE_VIDEO);
            if (f == null)
                return;
            filePath = f.getAbsolutePath();
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            transMsg(POST_MSG,"cannot take video...io problem");
            e.printStackTrace();
            return;
        }
        threadClass.startThread(ThreadClass.PROCESS_VIDEO, filePath, dataPath, PHOTO_RECORD_NAME);
        startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
    }

    /**
     * Action on Take Data Button
     * */


    public void takeData(String albumName) throws IOException {

        //Not much to do in this function
        //
        // Only present to keep the same function calls for video, photo and data
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());

        if (isDataRecording)
        {
            saveDataToFileSystem(timeStamp + "");
        }
        else;
    }

    public void saveDataToFileSystem (String customTimeStamp)
    {

        try {
            MainActivity.fileClass.appendFile(dataPath, "Data.txt",
                    "MID_" + customTimeStamp +
                            "_" +
                            getMetadata(ACTION_TAKE_DATA) +
                            "\n");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /*
     Action when finish taking photo/video
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        dataRecording = false;
        if (requestCode == 0){                  // activity returned error
            transMsg(POST_MSG, "Activity returned error!");
        }
        else if (requestCode == ACTION_TAKE_PHOTO || requestCode == ACTION_TAKE_VIDEO) {            // valid return
            if (requestCode == ACTION_TAKE_VIDEO)
                threadClass.stopThread(ThreadClass.PROCESS_VIDEO);
            File mediaFile = new File(filePath);
            if(!mediaFile.exists()){
                transMsg(POST_MSG,"No File!\n--------------------\n");
                return;
            }
            if(mediaFile.length() < 100){ // photo not taken
                transMsg(POST_MSG,"Photo not taken!\n--------------------");
                if (mediaFile.delete())
                    transMsg(POST_MSG, "temp image file deleted\n\n");
                else
                    transMsg(POST_MSG, "cannot delete temp image file!\n\n");
                return;
            }
            transMsg(POST_MSG, "File size: " + mediaFile.length()/1000 + "KB");
            transMsg(ADD_GALLERY, filePath);
            if (requestCode == ACTION_TAKE_PHOTO)
                threadClass.startThread(ThreadClass.PROCESS_PHOTO, filePath, dataPath, PHOTO_RECORD_NAME);    // process photo
        }
    }

    /*
      get metadata: Use two set of metadata to seperately represent photo and video
     */
    public static String getMetadata (int type) {

        switch (type) {
            case ACTION_TAKE_PHOTO:     // Show
                int faceNum = faceClass.faceDetect(MainActivity.filePath);
                int blurry = blurClass.blurDetect(MainActivity.filePath);
                return
                        cars +                                                          // num of cars
                        " " + faceNum +                                                 // num of faces
                        " " + blurry +                                                  // blurry or not (0 or 1)
                        " " + sceneTag +                                                // scene tag
                        " " + String.valueOf(MainActivity.thetaH) +                     // angle of view
                        " " + sensorClass.getSensorData(SensorClass.LIGHT_ON) +         // light sensor
                        " " + sensorClass.getSensorData(SensorClass.ACC_ON) +           // acc value (3) - x,y,z
                        " " + sensorClass.getSensorData(SensorClass.MAG_ON) +           // mag value (3) - x,y,z
                        " " + sensorClass.getBearingInfo() +                            // bearing (3) - azimuth, pitch, roll
                        " " + lng + " " + lat + " " + acc;                              // GPS (3) - lng, lat, accuracy



            case ACTION_TAKE_DATA: //Data
                return
                        System.currentTimeMillis() +	" " +                             // timestamp (in sec)
                        cars +                                                          // num of cars
                                " " + sceneTag +                                                // scene tag
                                " " + String.valueOf(MainActivity.thetaH) +                     // angle of view
                                " " + sensorClass.getSensorData(SensorClass.LIGHT_ON) +         // light sensor
                                " " + sensorClass.getSensorData(SensorClass.ACC_ON) +           // acc value (3) - x,y,z
                                " " + sensorClass.getSensorData(SensorClass.MAG_ON) +           // mag value (3) - x,y,z
                                " " + sensorClass.getBearingInfo() +                            // bearing (3) - azimuth, pitch, roll
                                " " + lng + " " + lat + " " + acc;                              // GPS (3) - lng, lat, accuracy


            case ACTION_TAKE_VIDEO:     // the format of video's metadata is same as that of photo
                return
                        0 +                                                                 // num of cars (default)
                                " " + 0 +                                                           // num of faces (default)
                                " " + 0 +                                                           // blurry or not (default)
                                " " + 0 +                                                           // scene tag (default)
                                " " + System.currentTimeMillis() +		              		// Video end time
                                " 0 0 0" +                                                          // light sensor (default)
                                " 0 0 0" +                                                          // acc value (3) - x,y,z (default)
                                " 0 0 0" +                                                          // mag value (3) - x,y,z (default)
                                " 0 0 0" +                                                          // bearing (3) (default)
                                " 0 0 0";                                                           // GPS (3) (default)

            case ACTION_META_VIDEO:     // The metadata recorded in the file of each video
                return
                        System.currentTimeMillis() +	                             // timestamp (in sec)
                        " " + sensorClass.getSensorData(SensorClass.LIGHT_ON) +         // light sensor
                        " " + sensorClass.getSensorData(SensorClass.ACC_ON) +           // acc value (3) - x,y,z
                        " " + sensorClass.getSensorData(SensorClass.MAG_ON) +           // mag value (3) - x,y,z
                        " " + sensorClass.getBearingInfo() +                            // bearing (3) - azimuth, pitch, roll
                        " " + lng + " " + lat + " " + acc;                              // GPS (3) - lng, lat, accuracy


            default:
                return "error metadata!";
        }


    }
}
