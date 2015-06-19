package pl.skm.smarttag.sensors;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import pl.skm.smarttag.game.GameEngineALO;

/**
 * Created by marekmagik on 2015-03-31.
 */
public class GPSTrackerService extends Service {

    private final static GameEngineALO gameEngine = new GameEngineALO();
    private final static long ONE_SECOND_FACTOR = 1000;
    private final static long MINIMUM_INTERVAL_BETWEEN_HANDLER_LAUNCH = 100;
    private final static float MINIMUM_DISTANCE_BETWEEN_HANDLER_LAUNCH = 0; // 0.05f;
    private final static long MAXIMUM_PERIOD_WITHOUT_GPS = 1 * ONE_SECOND_FACTOR;

    private static double longitudeFactor;
    private static double latitudeFactor;
    private static double altitudeFactor;
    private static int round;
    private static final int GPS_ROUNDS_FOR_COMPUTING_AVERAGE = 8;

    private LocationManager locationManager;
    private Location lastGPSLocation = null;
    private long lastUpdateTimestamp = 0;
    private Toast info;

    private Timer updatingTimer;
    private TimerTask updateTask = new TimerTask() {

        @Override
        public void run() {
            if (isPositionExpired()) {
                handleGpsSignalLost();
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        // Implementacja nie jest konieczna.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
/*
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            isGPSOn = true;
        else
            isGPSOn = false;
*/
        lastGPSLocation = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MINIMUM_INTERVAL_BETWEEN_HANDLER_LAUNCH, MINIMUM_DISTANCE_BETWEEN_HANDLER_LAUNCH, locationListener);

        updatingTimer.scheduleAtFixedRate(updateTask, 0,
                MAXIMUM_PERIOD_WITHOUT_GPS * 5);

        Log.i("GPSTRACK", "started");
        return START_STICKY;
    }

    @SuppressLint("ShowToast")
    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        updatingTimer = new Timer();

        info = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListener);
        updatingTimer.cancel();

        super.onDestroy();
    }

    private boolean isPositionExpired() {
        return System.currentTimeMillis() - lastUpdateTimestamp > MAXIMUM_PERIOD_WITHOUT_GPS;
    }


    private void handleGpsSignalLost() {
        /*
        lastGPSLocation = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        */
        Bundle bundle = new Bundle();
        bundle.putString("message", "Not updated");
        Message message = new Message();
        message.setData(bundle);
        displayToast.sendMessage(message);
        //showToast("Not updated!");
    }

    private LocationListener locationListener = new LocationListener() {

        public void onProviderEnabled(String arg0) {
            gameEngine.gpsConnected();
            Log.i("GPSTRACK", "provider enabled");
            showToast("provider enabled");
        }

        public void onProviderDisabled(String arg0) {
            gameEngine.gpsDisconnected();
            Log.i("GPSTRACK", "provider disabled");
            showToast("provider disabled");
        }

        public void onLocationChanged(Location location) {
            lastGPSLocation = location;
            lastUpdateTimestamp = location.getTime();
            Log.i("GPSTRACK", "Longitude: " + location.getLongitude() + ", latitude: " + location.getLatitude());

            if(round == 0) {
                latitudeFactor = location.getLatitude();
                longitudeFactor = location.getLongitude();
                altitudeFactor = location.getAltitude();
            }else{
                latitudeFactor += location.getLatitude();
                longitudeFactor += location.getLongitude();
                altitudeFactor += location.getAltitude();
            }
            round++;

            if(round == GPS_ROUNDS_FOR_COMPUTING_AVERAGE){
                gameEngine.updateGpsCoordinates(location);
                showToast("Longitude: " + longitudeFactor / GPS_ROUNDS_FOR_COMPUTING_AVERAGE + ", " +
                        "latitude: " + latitudeFactor / GPS_ROUNDS_FOR_COMPUTING_AVERAGE + ", " +
                        "altitude: " + altitudeFactor /
                        GPS_ROUNDS_FOR_COMPUTING_AVERAGE);

                round = 0;
            }
/*
            gameEngine.updateGpsCoordinates(location);
            showToast("Longitude: " + PositionInCoordinateSystem
                    .getXCoordinate(location) + ", latitude: " + PositionInCoordinateSystem.getYCoordinate(location) + ", altitude: " + location.getAltitude());
*/
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
/*
            if (status == LocationProvider.AVAILABLE) {
                gameEngine.gpsConnected();
            } else {
                gameEngine.gpsDisconnected();
            }
*/
        }
    };

    private Handler displayToast = new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            showToast(msg.getData().getString("message"));
        }
    };

    public void showToast(String text) {
        info.setText(text);
        info.show();
    }
}
