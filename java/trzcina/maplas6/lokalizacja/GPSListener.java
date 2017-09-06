package trzcina.maplas6.lokalizacja;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Iterator;

import trzcina.maplas6.AppService;
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.pomoc.Wear;

@SuppressWarnings("MissingPermission")
public class GPSListener implements LocationListener, GpsStatus.Listener {

    public static volatile Location ostatnialokalizacja;
    public static volatile Location ostatnialokalizacjazgps;

    public static volatile int iloscsatelitow;
    public static volatile int iloscaktywnychsatelitow;
    public static volatile int dokladnosc;

    public GPSListener() {
        ostatnialokalizacja = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            GPXTrasaLogger obecnatrasa = AppService.service.obecnatrasa;
            if(obecnatrasa != null) {
                if(obecnatrasa.iloscpunktow == 0) {
                    AppService.service.zaproponujZmianeMapy(new Location(location));
                    Wear.wyslijInfoONowejTrasie();
                }
                obecnatrasa.zapiszPunkt(Rozne.zaokraglij5((float) location.getLongitude()), Rozne.zaokraglij5((float) location.getLatitude()), location.getAccuracy());
                ostatnialokalizacja = location;
                AppService.service.dzwiekiwatek.czasostatniejlokalizacji = System.currentTimeMillis();
            }
            if(AppService.service.czakamnapierwszyfix == true) {
                AppService.service.zlapalemPierwszyFix();
            }
            if(AppService.service.przesuwajmapezgps == true) {
                AppService.service.przesunMapeZGPS(location);
            }
            AppService.service.rysujwatek.odswiez = true;
            AppService.service.odswiezUI();
            Wear.wyslijLokalizacjeDoZegarkaTrasa(location);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
    }

    public void zerujZmienne() {
        iloscaktywnychsatelitow = 0;
        iloscsatelitow = 0;
        ostatnialokalizacjazgps = null;
        ostatnialokalizacja = null;
        dokladnosc = 0;
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                GpsStatus gpsstatus = AppService.service.locationmanager.getGpsStatus(null);
                Iterable<GpsSatellite> satelity = gpsstatus.getSatellites();
                Iterator<GpsSatellite> satelityiterator = satelity.iterator();
                iloscsatelitow = 0;
                iloscaktywnychsatelitow = 0;
                while (satelityiterator.hasNext()) {
                    GpsSatellite satellite = satelityiterator.next();
                    iloscsatelitow++;
                    if(satellite.usedInFix()) {
                        iloscaktywnychsatelitow++;
                    }
                }
                ostatnialokalizacjazgps = AppService.service.locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(ostatnialokalizacja != null) {
                    AppService.service.dzwiekiwatek.czasostatniejlokalizacji = ostatnialokalizacjazgps.getTime();
                    dokladnosc = Math.round(ostatnialokalizacjazgps.getAccuracy());
                }
                AppService.service.rysujwatek.odswiez = true;
                AppService.service.odswiezUI();
                Wear.wyslijLokalizacjeDoZegarka(ostatnialokalizacjazgps);
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
            case GpsStatus.GPS_EVENT_STARTED:
                iloscaktywnychsatelitow = 0;
                iloscsatelitow = 0;
                dokladnosc = 0;
                ostatnialokalizacjazgps = null;
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                iloscaktywnychsatelitow = 0;
                iloscsatelitow = 0;
                dokladnosc = 0;
                ostatnialokalizacjazgps = null;
                break;
        }
    }
}
