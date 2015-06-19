package pl.skm.smarttag;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Timer;
import java.util.TimerTask;

import pl.skm.smarttag.ahrs.MahonyAHRS;
import pl.skm.smarttag.ahrs.QuaternionData;


public class SensorsTestActivity extends Activity implements SensorEventListener {

    private static final String TAG = "SENSORS UPDATE";

    private static final double RIGHT_ANGLE = 90.0;

    private static final double DEG = 1;// 180.0 / Math.PI;
    private SensorManager sensorManager;

    float[] gravityVector = new float[3];           // Gravity or accelerometer
    float[] magneticVector = new float[3];           // Magnetometer
    float[] orientation = new float[3];

    float[] gyroVector = new float[3];

    float[] rotationMatrix = new float[9];
    float[] R2 = new float[9];
    float[] inclinationMatrix = new float[9];
    boolean haveGrav = false;
    boolean haveAccel = false;
    boolean haveMag = false;


    private TextView textviewAzimuth, textviewPitch, textviewRoll, textAltitude;

    private Timer timer;
    private KalmanFilter kalmanFilter;

    private MahonyAHRS ahrs;

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
                {0.8, 0, 0},
                {0, 0.8, 0},
                {0, 0, 0.8}
        });

        RealMatrix R = MatrixUtils.createRealMatrix(new double[][]{
                {50, 0, 0},
                {0, 50, 0},
                {0, 0, 50}
        });

        RealMatrix initialErrorCovariance = MatrixUtils.createRealMatrix(new double[][]{
                {1000, 0, 0},
                {0, 1000, 0},
                {0, 0, 1000}
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

        ahrs = new MahonyAHRS(1f / 256f, 5f);

//        Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        Sensor magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        final Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);

//        sensorManager.registerListener(this, gravitySensor,
//                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
//        sensorManager.registerListener(this, magneticFieldSensor,
//                SensorManager.SENSOR_DELAY_FASTEST);

        timer = new Timer();
        if(true)
            return;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (orientation == null || orientation.length != 3) {
                    return;
                }
                ahrs.Update(gyroVector[0], gyroVector[1], gyroVector[2], gravityVector[0], gravityVector[1],
                        gravityVector[2]);

                             Log.d(TAG, "mh: " + ( ahrs.getQuaternion()[0]*DEG));
            }
        }, 1L, 1L);

    }

    @Override
    protected void onPause() {

        Sensor magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.unregisterListener(this, magneticFieldSensor);
        sensorManager.unregisterListener(this, gyroSensor);

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
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                gravityVector[0] = event.values[0];
                gravityVector[1] = event.values[1];
                gravityVector[2] = event.values[2];
                haveGrav = true;
                break;

            case Sensor.TYPE_ACCELEROMETER:
                if (haveGrav) break;    // don't need it, we have better
                gravityVector[0] = event.values[0];
                gravityVector[1] = event.values[1];
                gravityVector[2] = event.values[2];
                haveAccel = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
//                magneticVector[0] = event.values[0];
//                magneticVector[1] = event.values[1];
//                magneticVector[2] = event.values[2];

                gravityVector[0] = event.values[0];
                gravityVector[1] = event.values[1];
                gravityVector[2] = event.values[2];

                haveMag = true;
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroVector[0] = event.values[0];
                gyroVector[1] = event.values[1];
                gyroVector[2] = event.values[2];
                break;
            default:
                return;
        }

        ahrs.Update(gyroVector[0], gyroVector[1], gyroVector[2], gravityVector[0], gravityVector[1],
                gravityVector[2]);

        float[] quaternion;

        try {
           quaternion = new QuaternionData(ahrs.getQuaternion()).ConvertToConjugate().convertToEulerAngles();

            Log.d(TAG, "mh: " + ( quaternion[2]));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private double[] convertToArrayOfDouble(float[] array) {

        double DEG = 180.0 / Math.PI;

        int size = 3;
        double[] newArray = new double[size];
        for (int i = 0; i < size; i++) {
            newArray[i] = array[i] * DEG;
        }
        return newArray;
    }

}
