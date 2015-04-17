package pl.skm.smarttag.sensors;

import android.location.Location;

import static java.lang.Math.*;

/**
 * Created by marekmagik on 2015-04-01.
 */

/**
 * http://www.kircherelectronics.com/blog/index.php/11-android/sensors/15-android-gyroscope-basics
 * <p/>
 * Układ współrzędnych w jakich ulokowany jest obiekt (urządzenie z androidem) jest zgodny z układem ENU
 * z podanego linku.
 * Żyroskop będzie wyznaczał wektor znormalizowany w układzie własnym (Local Coordinate System).
 */
public class PositionInCoordinateSystem {

    private static final double METERS_FACTOR = 1000.0;
    public static final double DEGREES_TO_RADIANS = PI / 180.0;
    public static final double EARTH_RADIUS = 6371.01;

    private static double computeDistanceInMeters(double lat1, double long1, double lat2, double long2) {
        double phi1 = lat1 * DEGREES_TO_RADIANS;
        double phi2 = lat2 * DEGREES_TO_RADIANS;
        double lam1 = long1 * DEGREES_TO_RADIANS;
        double lam2 = long2 * DEGREES_TO_RADIANS;

        return EARTH_RADIUS * acos(sin(phi1) * sin(phi2) + cos(phi1) * cos(phi2) * cos(lam2 - lam1)) * METERS_FACTOR;
    }

    public static double getXCoordinate(Location location) {
        return computeDistanceInMeters(location.getLatitude(), location.getLongitude(), location.getLatitude(), 0);
    }

    public static double getYCoordinate(Location location) {
        return computeDistanceInMeters(location.getLatitude(), location.getLongitude(), 0, location.getLongitude());
    }

}
