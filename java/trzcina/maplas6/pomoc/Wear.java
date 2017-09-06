package trzcina.maplas6.pomoc;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import trzcina.maplas6.MainActivity;
import trzcina.maplas6.lokalizacja.PlikiGPX;
import trzcina.maplas6.lokalizacja.PunktNaMapie;

@SuppressWarnings("PointlessBooleanExpression")
public class Wear {

    private static WearListener wearlistener = null;
    private static GoogleApiClient gac = null;
    public static String telefon = null;

    public static void wyslijLokalizacjeDoZegarka(Location location) {
        if(telefon != null) {
            String dane = location.getLongitude() + ":" + location.getLatitude() + ":" + location.getTime();
            wyslijWiadomosc("GPS", dane);
        }
    }

    public static void wyslijLokalizacjeDoZegarkaTrasa(Location location) {
        if(telefon != null) {
            String dane = location.getLongitude() + ":" + location.getLatitude() + ":" + location.getTime();
            wyslijWiadomosc("GPSTRASA", dane);
        }
    }

    public static void wyslijPunktDoZegarka(PunktNaMapie punkt) {
        if(telefon != null) {
            String dane = punkt.nazwa + ":" + punkt.opis + ":" + punkt.wspx + ":" + punkt.wspy;
            wyslijWiadomosc("GPSPUNKT", dane);
        }
    }

    public static void wyslijInfoONowejTrasie() {
        if(telefon != null) {
            wyslijWiadomosc("NOWATRASA");
        }
    }

    public static void wyslijNoweZaznaczone() {
        if(telefon != null) {
            String dowyslania = "";
            for(int i = 0; i < PlikiGPX.pliki.size(); i++) {
                if(PlikiGPX.pliki.get(i).zaznaczony == true) {
                    dowyslania = dowyslania + PlikiGPX.pliki.get(i).nazwa + ":";
                }
            }
            Wear.wyslijWiadomosc("NOWEZAZNACZONE", dowyslania);
        }
    }

    public static void wyslijWiadomosc(String wiadomosc) {
        Wearable.MessageApi.sendMessage(gac, telefon, wiadomosc, null);
    }

    public static void wyslijWiadomosc(String wiadomosc, String dane) {
        Wearable.MessageApi.sendMessage(gac, telefon, wiadomosc, dane.getBytes());
    }

    public static void wyslijWiadomosc(String wiadomosc, byte[] dane) {
        Wearable.MessageApi.sendMessage(gac, telefon, wiadomosc, dane);
    }

    public static GoogleApiClient ustawApi() {
        gac = new GoogleApiClient.Builder(MainActivity.activity).addApi(Wearable.API).build();
        gac.blockingConnect(Stale.GACCONNTIMEOUT, TimeUnit.SECONDS);
        if(gac.isConnected()) {
            wearlistener = new WearListener();
            Wearable.MessageApi.addListener(gac, wearlistener);
            return gac;
        } else {
            return null;
        }
    }

    public static void wylaczAPI() {
        if(wearlistener != null) {
            Wearable.MessageApi.removeListener(gac, wearlistener);
        }
        if(gac != null) {
            gac.disconnect();
        }
        wearlistener = null;
        gac = null;
        telefon = null;
    }
}
