package com.chris.medusacam;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Chris on 1/6/15.
 */
public class SensorClass {

    public static final String dataSplitter = " ";

    /** Turn on sensor */
    public static final int ACC_ON = 1;
    public static final int GRAV_ON = 2;
    public static final int GYRO_ON = 3;
    public static final int LIGHT_ON = 4;
    public static final int LINEAR_ON = 5;
    public static final int MAG_ON = 6;
    public static final int ROT_ON = 7;
    public static final int ALL_ON = 8;

    public static final int GET_AZIMUTH = 0;
    public static final int GET_PITCH = 1;
    public static final int GET_ROLL = 2;

    public SensorManager sensorManager;        // sensor manager

    private float[] valuesAccelerometer;
    private float[] valuesMagneticField;

    private float[] matrixR;
    private float[] matrixI;
    private float[] matrixValues;

    private String sensorData[] = {"","","","","","","",""};
    private String bearingInfo[] = {"","",""};

    /** Sensor related */
    // turn on a sensor
    public void openSensor(int sensorSelect, int sensorFreq){
        int sensorType = 0;
        switch(sensorSelect){
            case ACC_ON:
                sensorType = Sensor.TYPE_ACCELEROMETER;
                break;
            case GRAV_ON:
                sensorType = Sensor.TYPE_GRAVITY;
                break;
            case GYRO_ON:
                sensorType = Sensor.TYPE_GYROSCOPE;
                break;
            case LIGHT_ON:
                sensorType = Sensor.TYPE_LIGHT;
                break;
            case LINEAR_ON:
                sensorType = Sensor.TYPE_LINEAR_ACCELERATION;
                break;
            case MAG_ON:
                sensorType = Sensor.TYPE_MAGNETIC_FIELD;
                break;
            case ROT_ON:
                sensorType = Sensor.TYPE_ROTATION_VECTOR;
                break;
            case ALL_ON:
                sensorType = Sensor.TYPE_ALL;
                break;
            default:
                break;
        }
        sensorManager.registerListener(mySensorListener,
                sensorManager.getDefaultSensor(sensorType),
                sensorFreq);

        valuesAccelerometer = new float[3];
        valuesMagneticField = new float[3];

        matrixR = new float[9];
        matrixI = new float[9];
        matrixValues = new float[3];
    }

    public final SensorEventListener mySensorListener = new SensorEventListener(){
        // overwrite method: onSensorChanged
        public void onSensorChanged(SensorEvent sensorEvent){
            switch(sensorEvent.sensor.getType()){
                // do something here if you get the data of sensors
                case Sensor.TYPE_LIGHT:
                    sensorData[LIGHT_ON] = String.valueOf(sensorEvent.values[0]);
                    // Log.i("SensorClass.sensorListener","light sensor changed");
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    for(int i =0; i < 3; i++){
                        valuesAccelerometer[i] = sensorEvent.values[i];
                    }
                    sensorData[ACC_ON] =
                            String.valueOf(sensorEvent.values[0]) + dataSplitter +
                            String.valueOf(sensorEvent.values[1]) + dataSplitter +
                            String.valueOf(sensorEvent.values[2]);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    for(int i =0; i < 3; i++){
                        valuesMagneticField[i] = sensorEvent.values[i];
                    }
                    sensorData[MAG_ON] =
                            String.valueOf(sensorEvent.values[0]) + dataSplitter +
                            String.valueOf(sensorEvent.values[1]) + dataSplitter +
                            String.valueOf(sensorEvent.values[2]);
                    break;
                default:
                    Log.i("SensorClass.sensorListener","default sensor type");
                    break;
            }

            boolean success = SensorManager.getRotationMatrix(
                    matrixR,
                    matrixI,
                    valuesAccelerometer,
                    valuesMagneticField);

            if(success){
                SensorManager.getOrientation(matrixR, matrixValues);

                double azimuth = Math.toDegrees(matrixValues[0]);
                double pitch = Math.toDegrees(matrixValues[1]);
                double roll = Math.toDegrees(matrixValues[2]);

                bearingInfo[GET_AZIMUTH] = String.valueOf(azimuth);
                bearingInfo[GET_PITCH] = String.valueOf(pitch);
                bearingInfo[GET_ROLL] = String.valueOf(roll);
            }
        }
        // overwrite method: onAccuracyChanged
        public void onAccuracyChanged(Sensor sensor, int accuracy){
            Log.i("SensorClass.sensorListener", "onAccuracyChanged");
        }
    };

    public String getSensorData(int type){
        return sensorData[type];
    }

    public String getBearingInfo() {
        return bearingInfo[0] + dataSplitter + bearingInfo[1] + dataSplitter + bearingInfo[2];
    }

}
