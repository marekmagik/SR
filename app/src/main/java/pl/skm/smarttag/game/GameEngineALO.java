package pl.skm.smarttag.game;

import android.location.Location;
import android.util.Log;

/**
 * Created by marekmagik on 2015-03-31.
 */

/**
 * Najwyższa warstwa - obsługuje logikę rozgrywki - w tym celu wykorzystuje objekty niższych warstw (SLO, DAO, itd.).
 */
public class GameEngineALO {

    public void gpsDisconnected(){
        //TODO: metoda, która będzie obsługiwać sytuację gdy utracony zostanie sygnał GPS w trakcie gry.
        Log.i("GPSTRACK", "brak sygnału gps");
    }

    public void gpsConnected(){
        //TODO: analogicznie, dla uruchomionego GPSu
        Log.i("GPSTRACK", "gps aktywny");
    }

    public void updateGpsCoordinates(Location location){


        Log.i("GPSTRACK", "pozycja zaktualizowana");
    }

}
