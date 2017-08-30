package trzcina.maplas6.watki;

import android.location.Location;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import trzcina.maplas6.AppService;
import trzcina.maplas6.MainActivity;
import trzcina.maplas6.R;
import trzcina.maplas6.lokalizacja.GPXTrasaLogger;
import trzcina.maplas6.pomoc.Rozne;

public class NotyfikacjaWatek extends Thread {

    public volatile boolean zakoncz;
    DateFormat formatczasu;
    public int iloscupdate;
    public int numernotyfikacji;

    public NotyfikacjaWatek() {
        zakoncz = false;
        formatczasu = new SimpleDateFormat("HH:mm:ss");
        formatczasu.setTimeZone(TimeZone.getTimeZone("UTC"));
        iloscupdate = 0;
        numernotyfikacji = 100;

    }

    private void ustawIkoneInternetu() {
        if(AppService.service.wlaczgps == false) {
            AppService.service.notyfikacjaUstawIkoneInternet(R.mipmap.internetok);
        } else {
            if(AppService.service.internetwyslij == true) {
                if (System.currentTimeMillis() >= AppService.service.internetwyslijwatek.ostatniawysylka + 150 * 1000) {
                    AppService.service.notyfikacjaUstawIkoneInternet(R.mipmap.internetfail);
                } else {
                    AppService.service.notyfikacjaUstawIkoneInternet(R.mipmap.internetok);
                }
            } else {
                AppService.service.notyfikacjaUstawIkoneInternet(R.mipmap.internetfail);
            }
        }
    }

    public void run() {
        AppService.service.utworzNotyfikacje(numernotyfikacji);
        while (zakoncz == false) {
            AppService.service.notyfikacjaUstawCzas("");
            AppService.service.notyfikacjaUstawSzczegoly("");
            AppService.service.notyfikacjaUstawSatelity("");
            AppService.service.notyfikacjaUstawStanGPS("");
            AppService.service.notyfikacjaUstawIkoneGPS(R.mipmap.satelitaczerwony);
            ustawIkoneInternetu();
            if (AppService.service.wlaczgps) {
                Location lokalizacja = AppService.service.czyJestFix();
                if (lokalizacja != null) {
                    String wspolrzedne = Rozne.zaokraglij5((float) lokalizacja.getLongitude()) + " " + Rozne.zaokraglij5((float) lokalizacja.getLatitude());
                    AppService.service.notyfikacjaUstawStanGPS(wspolrzedne);
                    AppService.service.notyfikacjaUstawIkoneGPS(R.mipmap.satelitazielony);
                } else {
                    AppService.service.notyfikacjaUstawStanGPS("Brak sygnalu...");
                    AppService.service.notyfikacjaUstawIkoneGPS(R.mipmap.satelitaczerwony);
                }
                GPXTrasaLogger obecnatrasa = AppService.service.obecnatrasa;
                AppService.service.notyfikacjaUstawSatelity(AppService.service.gpslistener.iloscaktywnychsatelitow + "/" + AppService.service.gpslistener.iloscsatelitow);
                if(obecnatrasa != null) {
                    float dystans = obecnatrasa.dlugosctrasy;
                    float odlegloscodpoczatku = obecnatrasa.odlegloscodpoczatku;
                    String szczegoly = Rozne.formatujDystans(Math.round(dystans)) + " " + Rozne.formatujDystans(Math.round(odlegloscodpoczatku));
                    AppService.service.notyfikacjaUstawSzczegoly(szczegoly);
                    AppService.service.notyfikacjaUstawCzas(formatczasu.format(System.currentTimeMillis() - obecnatrasa.czasstart));
                }
            } else {
                AppService.service.notyfikacjaUstawStanGPS("GPS wylaczony...");
            }
            AppService.service.notyfikacjaZatwierdz(numernotyfikacji);
            iloscupdate = iloscupdate + 1;
            if(iloscupdate >= 600) {
                AppService.service.notificationmanager.cancel(numernotyfikacji);
                AppService.service.unregisterReceiver(AppService.service.odbiorznotyfikacji);
                numernotyfikacji = numernotyfikacji + 1;
                AppService.service.utworzNotyfikacje(numernotyfikacji);
                iloscupdate = 0;
            } else {
                Rozne.czekaj(2000);
            }
        }
        AppService.service.notificationmanager.cancelAll();
        AppService.service.unregisterReceiver(AppService.service.odbiorznotyfikacji);
    }
}
