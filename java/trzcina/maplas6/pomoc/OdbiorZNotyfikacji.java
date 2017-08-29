package trzcina.maplas6.pomoc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import trzcina.maplas6.AppService;
import trzcina.maplas6.MainActivity;
import trzcina.maplas6.lokalizacja.GPXPunktLogger;

public class OdbiorZNotyfikacji extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Location lokalizacja = AppService.service.czyJestFix();
        if(lokalizacja != null) {
            boolean zapis = GPXPunktLogger.zapiszPunkt(Rozne.zaokraglij5((float) lokalizacja.getLongitude()), Rozne.zaokraglij5((float) lokalizacja.getLatitude()), intent.getAction(), "", lokalizacja.getAccuracy());
            if(zapis) {
                MainActivity.activity.pokazToast("Zapisano: " + intent.getAction());
            } else {
                MainActivity.activity.pokazToast("Blad zapisu!");
            }
        } else {
            MainActivity.activity.pokazToast("Sprawdz GPS!");
        }
    }
}