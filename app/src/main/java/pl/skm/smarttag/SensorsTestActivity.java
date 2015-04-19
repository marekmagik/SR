package pl.skm.smarttag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pl.skm.smarttag.sensors.GPSTrackerService;


public class SensorsTestActivity extends Activity implements SensorEventListener {

    private static final String TAG = "SENSORS UPDATE";

    private static final double RIGHT_ANGLE = 90.0;

    private static final double DEG = 180.0 / Math.PI;
    private SensorManager sensorManager;

    float[] gravityVector = new float[3];           // Gravity or accelerometer
    float[] magneticVector = new float[3];           // Magnetometer
    float[] orientation = new float[3];
    float[] rotationMatrix = new float[9];
    float[] R2 = new float[9];
    float[] inclinationMatrix = new float[9];
    boolean haveGrav = false;
    boolean haveAccel = false;
    boolean haveMag = false;


    private TextView textviewAzimuth, textviewPitch, textviewRoll, textAltitude;
    private boolean sersorrunning;

    private static int DISPLAY_STEP = 1;
    private static int counter = DISPLAY_STEP;
    private float[] averageOrientation = new float[3];

    private KalmanFilter kalmanFilter;

    private void initKalmanFilter() {
        // A = [ 1 ]
//        RealMatrix A = new Array2DRowRealMatrix(new double[][] { {1d, dt, 0}, {0, 1d, dt}, {0, 0, 1d} });
        final RealMatrix A = MatrixUtils.createRealMatrix(new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        });

// no control input
//        RealMatrix B = null;
//        RealMatrix B = new Array2DRowRealMatrix(new double[][] { {1d, 0,0}, {0, 1d, 0}, {0, 0, 1d} });

//        RealMatrix B = MatrixUtils.createRealMatrix(3,3);

        RealMatrix B = MatrixUtils.createRealMatrix(new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        });

// H = [ 1 ]
//        RealMatrix H = new Array2DRowRealMatrix(new double[][] { {1d, 0,0}, {0, 1d, 0}, {0, 0, 1d} });
        RealMatrix H = MatrixUtils.createRealMatrix(new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        });

// Q = [ 0 ]
//        RealMatrix Q = new Array2DRowRealMatrix(new double[][] { {0, 0,0, 0},  {0, 0, 0, 0}, {0, 0, 0, 0}, {0,0,0, 0} });
        RealMatrix Q = MatrixUtils.createRealMatrix(new double[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        });


// R = [ 0 ]
//        RealMatrix R = new Array2DRowRealMatrix(new double[][] { {0.01d, 0,0,0},  {0, 0.01d, 0,0}, {0, 0, 0.01d,0},
//                {0,0,0,0.1d}});

        RealMatrix R = MatrixUtils.createRealMatrix(new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        });

        RealMatrix initialErrorCovariance = MatrixUtils.createRealMatrix(new double[][]{
                {100000d, 0, 0},
                {0, 100000d, 0},
                {0, 0, 100000d}
        });

        ProcessModel pm
                = new DefaultProcessModel(A, B, Q, new ArrayRealVector(new double[]{1, 1, 1}),
                initialErrorCovariance);
        MeasurementModel mm = new DefaultMeasurementModel(H, R);
        kalmanFilter = new KalmanFilter(pm, mm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        setContentView(R.layout.activity_sensors_test);
        textviewAzimuth = (TextView) findViewById(R.id.textazimuth);
        textviewPitch = (TextView) findViewById(R.id.textpitch);
        textviewRoll = (TextView) findViewById(R.id.textroll);
        textAltitude = (TextView) findViewById(R.id.textAltitude);

    }

    @Override
    protected void onResume() {
        super.onResume();

        initKalmanFilter();

        Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, gravitySensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magneticFieldSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
/*
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (orientation == null || orientation.length != 3) {
                    return;
                }

                kalmanFilter.predict();

                kalmanFilter.correct(convertToArrayOfDouble(orientation));

                final double[] newOrientation = kalmanFilter.getStateEstimation();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

//                        kalmanFilter.predict();
//                        kalmanFilter.predict(convertToArrayOfDouble(orientation));

//                        kalmanFilter.correct(convertToArrayOfDouble(orientation));

//                        kalmanFilter.predict(convertToArrayOfDouble(orientation));

//                        double[] newOrientation = kalmanFilter.getStateEstimation();

                        textviewAzimuth.setText("Azimuth: " +
                                String.valueOf(/*Math.acos(rotationMatrix[8])*/
/*                                        newOrientation[0] * DEG));
                        textviewPitch.setText("Pitch: " + String.valueOf
                                (newOrientation[2] *
                                        DEG));
                        textviewRoll.setText("Roll: " + String.valueOf(
                                newOrientation[4] * DEG));
                    }
                });// .obtainMessage(1).sendToTarget();
                /*
                kalmanFilter.predict();

                kalmanFilter.correct(convertToArrayOfDouble(orientation));

                double[] newOrientation = kalmanFilter.getStateEstimation();

                textviewAzimuth.setText("Azimuth: " +
                        String.valueOf(/*Math.acos(rotationMatrix[8])
                                newOrientation[0] * DEG));
                textviewPitch.setText("Pitch: " + String.valueOf
                        (newOrientation[1] *
                                DEG));
                textviewRoll.setText("Roll: " + String.valueOf(
                        newOrientation[2] * DEG));

                kalmanFilter.predict();

                */
/*            }
        }, 1L, 1L);
*/
    }

    @Override
    protected void onPause() {
//        timer.cancel();

        Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
/*
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
*/
        Sensor magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.unregisterListener(this, gravitySensor);
//        sensorManager.unregisterListener(this, accelerometerSensor);
        sensorManager.unregisterListener(this, magneticFieldSensor);


        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sensors_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] data;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                //case Sensor.TYPE_ACCELEROMETER:
                gravityVector[0] = event.values[0];
                gravityVector[1] = event.values[1];
                gravityVector[2] = event.values[2];
                haveGrav = true;
                break;
/*
            case Sensor.TYPE_ACCELEROMETER:
                //case Sensor.TYPE_GRAVITY:
                if (haveGrav) break;    // don't need it, we have better
                gravityVector[0] = event.values[0];
                gravityVector[1] = event.values[1];
                gravityVector[2] = event.values[2];
                haveAccel = true;
                break;
*/
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticVector[0] = event.values[0];
                magneticVector[1] = event.values[1];
                magneticVector[2] = event.values[2];
                haveMag = true;
                break;
            default:
                return;
        }

        if ((haveGrav || haveAccel) && haveMag) {
            SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityVector, magneticVector);
            SensorManager.remapCoordinateSystem(rotationMatrix,
                    SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, R2);
            // Orientation isn't as useful as a rotation matrix, but
            // we'll show it here anyway.
            SensorManager.getOrientation(R2, orientation);
//            float incl = SensorManager.getInclination(inclinationMatrix);
            //     Log.d(TAG, "mh: " + (orientation[0]*DEG));

            //    Log.d(TAG, "mh: " + (computeCoreSkyAxisToZeroAtNorth(orientation[0] * DEG)));

//            Log.d(TAG, "pitch: " + (orientation[1]*DEG));
//            Log.d(TAG, "roll: " + (orientation[2]*DEG));
            Log.d(TAG, "yaw: " + (orientation[0] * DEG));
//            Log.d(TAG, "inclination: " + (incl*DEG));


//            if(counter == 0) {


            kalmanFilter.predict();

            kalmanFilter.correct(convertToArrayOfDouble(orientation));

            double[] newOrientation = kalmanFilter.getStateEstimation();




            counter = DISPLAY_STEP;

                textviewAzimuth.setText("Azimuth: " +
                        String.valueOf(/*Math.acos(rotationMatrix[8])*/
                                newOrientation[0] * DEG));
                textviewPitch.setText("Pitch: " + String.valueOf
                        (newOrientation[1] *
                        DEG));
                textviewRoll.setText("Roll: " + String.valueOf(
                        newOrientation[2] * DEG));
            /*    textAltitude.setText("Inclination: " + event.accuracy (incl*DEG));
*/





//            kalmanFilter.predict();

/*
                averageOrientation[0] = 0;
                averageOrientation[1] = 0;
                averageOrientation[2] = 0;


            }else{
                averageOrientation[0] += orientation[0];
                averageOrientation[1] += orientation[1];
                averageOrientation[2] += orientation[2];

                counter--;
            }
*/
            haveGrav = false;
            haveMag = false;
        }
    }

    private double computeCoreSkyAxisToZeroAtNorth(double arg) {
        //    if(){

        //    }
        return arg - RIGHT_ANGLE;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private double[] convertToArrayOfDouble(float[] array) {
        int size = 3;//array.length;
        double[] newArray = new double[size];
        for (int i = 0; i < size; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            kalmanFilter.correct(convertToArrayOfDouble(orientation));

            kalmanFilter.predict(convertToArrayOfDouble(orientation));

            double[] newOrientation = kalmanFilter.getStateEstimation();

            textviewAzimuth.setText("Azimuth: " +
                    String.valueOf(/*Math.acos(rotationMatrix[8])*/
                            newOrientation[0] * DEG));
            textviewPitch.setText("Pitch: " + String.valueOf
                    (newOrientation[2] *
                            DEG));
            textviewRoll.setText("Roll: " + String.valueOf(
                    newOrientation[4] * DEG));

        }
    };

}
