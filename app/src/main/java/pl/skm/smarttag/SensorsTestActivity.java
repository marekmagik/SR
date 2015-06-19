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
        final RealMatrix A = MatrixUtils.createRealMatrix(new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        });

        RealMatrix B = MatrixUtils.createRealMatrix(new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        });

        RealMatrix H = MatrixUtils.createRealMatrix(new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        });

        RealMatrix Q = MatrixUtils.createRealMatrix(new double[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        });

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
    }

    @Override
    protected void onPause() {
        Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
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
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                gravityVector[0] = event.values[0];
                gravityVector[1] = event.values[1];
                gravityVector[2] = event.values[2];
                haveGrav = true;
                break;

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
            Log.d(TAG, "yaw: " + (orientation[0] * DEG));

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
            haveGrav = false;
            haveMag = false;
        }
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
