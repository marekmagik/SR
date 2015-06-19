package pl.skm.smarttag;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import org.apache.commons.math3.complex.Quaternion;

import pl.skm.smarttag.R;

/**
 * Created by Sławek on 2015-04-23.
 */
public class SensorsXimuTestActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView textviewAzimuth, textviewPitch, textviewRoll, textAltitude;
    double[] gravityVector = new double[3];
    double[] magneticVector = new double[3];
    double[] gyroscopeVector = new double[3];
    float[] eInt = new float[] {0f, 0f, 0f};
    public Quaternion quaternion;
    public double samplePeriod;
    public double ki;
    public double kp;
    boolean haveGrav = false;
    boolean haveAccel = false;
    boolean haveMag = false;
    boolean haveGyro = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        quaternion = new Quaternion(1, 0, 0, 0);

        setContentView(R.layout.activity_sensors_test);

        textviewAzimuth = (TextView) findViewById(R.id.textazimuth);
        textviewPitch = (TextView) findViewById(R.id.textpitch);
        textviewRoll = (TextView) findViewById(R.id.textroll);
        textAltitude = (TextView) findViewById(R.id.textAltitude);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                gravityVector[0] = event.values[0];
                gravityVector[1] = event.values[1];
                gravityVector[2] = event.values[2];
                haveAccel = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticVector[0] = event.values[0];
                magneticVector[1] = event.values[1];
                magneticVector[2] = event.values[2];
                haveMag = true;
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroscopeVector[0] = event.values[0];
                gyroscopeVector[1] = event.values[1];
                gyroscopeVector[2] = event.values[2];
                haveGyro = true;
                break;
            default:
                return;
        }

        double q0 = quaternion.getQ0();
        double q1 = quaternion.getQ1();
        double q2 = quaternion.getQ2();
        double q3 = quaternion.getQ3();

        double q0q0 = q0 * q0;
        double q0q1 = q0 * q1;
        double q0q2 = q0 * q2;
        double q0q3 = q0 * q3;
        double q1q1 = q1 * q1;
        double q1q2 = q1 * q2;
        double q1q3 = q1 * q3;
        double q2q2 = q2 * q2;
        double q2q3 = q2 * q3;
        double q3q3 = q3 * q3;

        double norm = (double) Math.sqrt(gravityVector[0] * gravityVector[0]
                                        + gravityVector[1] * gravityVector[1]
                                        + gravityVector[2] * gravityVector[2]);

        double hx, hy, bx, bz;
        double vx, vy, vz, wx, wy, wz;
        double ex, ey, ez;
        double pa, pb, pc;

        if(norm == 0)
            return;
        norm = 1 / norm;
        gravityVector[0] *= norm;
        gravityVector[1] *= norm;
        gravityVector[2] *= norm;

        norm = (double) Math.sqrt(magneticVector[0] * magneticVector[0]
                                  + magneticVector[1] * magneticVector[1]
                                  + magneticVector[2] * magneticVector[2]);
        norm = 1 /norm;
        magneticVector[0] *= norm;
        magneticVector[1] *= norm;
        magneticVector[2] *= norm;

        // Reference direction of Earth's magnetic field
        hx = 2f * magneticVector[0] * (0.5f - q2q2 - q3q3) + 2f * magneticVector[1] * (q1q2 - q0q3) +
                2f * magneticVector[2] * (q1q3 + q0q2);
        hy = 2f * magneticVector[0] * (q1q2 + q0q3) + 2f * magneticVector[1] * (0.5f - q1q1 - q3q3) +
                2f * magneticVector[2] * (q2q3 - q0q1);
        bx = (double) Math.sqrt(hx * hx + hy * hy);
        bz = 2f * magneticVector[0] * (q1q3 - q0q2) + 2f * magneticVector[1] * (q2q3 + q0q1) +
                2f * magneticVector[2] * (0.5f - q1q1 - q2q2);


        // Estimated direction of gravity and magnetic field
        vx = 2f * (q1q3 - q0q2);
        vy = 2f * (q0q1 + q2q3);
        vz = q0q0 - q1q1 - q2q2 + q3q3;

        wx = 2f * bx * (0.5f - q2q2 - q3q3) + 2f * bz * (q1q3 - q0q2);
        wy = 2f * bx * (q1q2 - q0q3) + 2f * bz * (q0q1 + q2q3);
        wz = 2f * bx * (q0q2 + q1q3) + 2f * bz * (0.5f - q1q1 - q2q2);

        // Error is cross product between estimated direction and measured direction of gravity
        ex = (gravityVector[1] * vz - gravityVector[2] * vy) + (magneticVector[1] * wz - magneticVector[2] * wy);
        ey = (gravityVector[2] * vx - gravityVector[0] * vz) + (magneticVector[2] * wx - magneticVector[0] * wz);
        ez = (gravityVector[0] * vy - gravityVector[1] * vx) * (magneticVector[0] * wy - magneticVector[1] * wx);


        if (ki > 0f)
        {
            eInt[0] += ex;      // accumulate integral error
            eInt[1] += ey;
            eInt[2] += ez;
        }
        else
        {
            eInt[0] = 0.0f;     // prevent integral wind up
            eInt[1] = 0.0f;
            eInt[2] = 0.0f;
        }

        // Apply feedback terms
        gyroscopeVector[0] = gyroscopeVector[0] + kp * ex + ki * eInt[0];
        gyroscopeVector[1] = gyroscopeVector[1] + kp * ey + ki * eInt[1];
        gyroscopeVector[2] = gyroscopeVector[2]+ kp * ez + ki * eInt[2];

        // Integrate rate of change of quaternion
        pa = q1;
        pb = q2;
        pc = q3;

        q0 = q0 + (-q1 * gyroscopeVector[0] - q2 * gyroscopeVector[1] - q3 * gyroscopeVector[2]) * (0.5f * samplePeriod);
        q1 = pa + (q0 * gyroscopeVector[0] + pb * gyroscopeVector[2] - pc * gyroscopeVector[1]) * (0.5f * samplePeriod);
        q2 = pb + (q0 * gyroscopeVector[1] - pa * gyroscopeVector[2] + pc * gyroscopeVector[0]) * (0.5f * samplePeriod);
        q3 = pc + (q0 * gyroscopeVector[2] + pa * gyroscopeVector[1] - pb * gyroscopeVector[0]) * (0.5f * samplePeriod);

        // Normalise quaternion
        norm = (double) Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        norm = 1.0f / norm;

        quaternion = new Quaternion(q0 * norm, q1 * norm, q2 * norm, q3 * norm);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();

        Sensor acceloremeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this, acceloremeterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_FASTEST);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sensors_test, menu);
        return true;
    }

}
