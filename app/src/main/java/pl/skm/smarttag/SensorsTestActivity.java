package pl.skm.smarttag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        setContentView(R.layout.activity_sensors_test);
        textviewAzimuth = (TextView)findViewById(R.id.textazimuth);
        textviewPitch = (TextView)findViewById(R.id.textpitch);
        textviewRoll = (TextView)findViewById(R.id.textroll);
        textAltitude = (TextView)findViewById(R.id.textAltitude);

        if(true)
            return;
        startService(new Intent(SensorsTestActivity.this,
                GPSTrackerService.class));

        List<Sensor> mySensors = sensorManager.getSensorList(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);

        if(mySensors.size() > 0){
            sensorManager.registerListener(mySensorEventListener,
                    mySensors.get(0), SensorManager.SENSOR_DELAY_FASTEST);
            sersorrunning = true;
            Toast.makeText(this, "Start ORIENTATION Sensor", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "No ORIENTATION Sensor", Toast.LENGTH_SHORT).show();
            sersorrunning = false;
            finish();
        }
    }

    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub

            if(counter == 0) {
                counter = DISPLAY_STEP;
                textviewAzimuth.setText("Azimuth: " + String.valueOf(event.values[0]));
                textviewPitch.setText("Pitch: " + String.valueOf(event.values[1]));
                textviewRoll.setText("Roll: " + String.valueOf(event.values[2]));
                textAltitude.setText("EVENT: " + event.sensor.getName());

            }else{
                counter--;
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(true)
            return;
        stopService(new Intent(SensorsTestActivity.this, GPSTrackerService.class));
        if(sersorrunning){
            sensorManager.unregisterListener(mySensorEventListener);
            Toast.makeText(SensorsTestActivity.this, "unregisterListener",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

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
        switch( event.sensor.getType() ) {
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
            float incl = SensorManager.getInclination(inclinationMatrix);
       //     Log.d(TAG, "mh: " + (orientation[0]*DEG));

        //    Log.d(TAG, "mh: " + (computeCoreSkyAxisToZeroAtNorth(orientation[0] * DEG)));

//            Log.d(TAG, "pitch: " + (orientation[1]*DEG));
//            Log.d(TAG, "roll: " + (orientation[2]*DEG));
            Log.d(TAG, "yaw: " + (orientation[0] * DEG));
//            Log.d(TAG, "inclination: " + (incl*DEG));


            if(counter == 0) {
                counter = DISPLAY_STEP;

                textviewAzimuth.setText("Azimuth: " +
                        String.valueOf(/*Math.acos(rotationMatrix[8])*/
                                averageOrientation[0] / DISPLAY_STEP * DEG));
                textviewPitch.setText("Pitch: " + String.valueOf
                        (averageOrientation[1] / DISPLAY_STEP *
                        DEG));
                textviewRoll.setText("Roll: " + String.valueOf(
                        orientation[2] / DISPLAY_STEP * DEG));
                textAltitude.setText("Inclination: " + event.accuracy /* (incl*DEG)*/);


                averageOrientation[0] = 0;
                averageOrientation[1] = 0;
                averageOrientation[2] = 0;


            }else{
                averageOrientation[0] += orientation[0];
                averageOrientation[1] += orientation[1];
                averageOrientation[2] += orientation[2];

                counter--;
            }

            haveGrav = false;
            haveMag = false;
        }
    }

    private double computeCoreSkyAxisToZeroAtNorth(double arg){
    //    if(){

    //    }
        return arg - RIGHT_ANGLE;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
