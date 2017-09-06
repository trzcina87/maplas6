package trzcina.maplas6.pomoc;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import trzcina.maplas6.AppService;
import trzcina.maplas6.MainActivity;
import trzcina.maplas6.lokalizacja.GPXPunktLogger;
import trzcina.maplas6.lokalizacja.PlikiGPX;

@SuppressWarnings("PointlessBooleanExpression")
public class WearListener implements MessageApi.MessageListener {

    private void obsluzPING(MessageEvent messageEvent) {
        if(messageEvent.getPath().startsWith("PING:")) {
            Wear.telefon = messageEvent.getSourceNodeId();
            Wear.wyslijWiadomosc(messageEvent.getPath());
        }
    }

    private void obsluzFILELIST(MessageEvent messageEvent) {
        if(messageEvent.getPath().startsWith("FILELIST:")) {
            String dowyslania = "";
            for(int i = 0; i < PlikiGPX.pliki.size(); i++) {
                dowyslania = dowyslania + PlikiGPX.pliki.get(i).nazwa + "^" + PlikiGPX.pliki.get(i).rozmiarbajty + ":";
            }
            Wear.wyslijWiadomosc(messageEvent.getPath(), dowyslania);
        }
    }

    private void obsluzFILEGET(MessageEvent messageEvent) {
        if(messageEvent.getPath().startsWith("FILEGET_")) {
            String[] tab = messageEvent.getPath().split("_");
            String nazwa = tab[1];
            File plik = new File(Stale.SCIEZKAMAPLAS + nazwa.split(":")[0] + ".gpx");
            try {
                long wielkosc = plik.length();
                byte[] bajty = new byte[(int) wielkosc];
                InputStream inputstream = new FileInputStream(plik);
                Rozne.odczytajZeStrumienia(inputstream, (int) wielkosc, bajty);
                Wear.wyslijWiadomosc(messageEvent.getPath(), bajty);
                inputstream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void obsluzPOINT(MessageEvent messageEvent) {
        if(messageEvent.getPath().startsWith("POINT:")) {
            Location location = AppService.service.czyJestFix();
            if(location != null) {
                String wpis = new String(messageEvent.getData());
                boolean zapis = AppService.service.zapiszPunktPozycjaGPS(wpis, "");
                if(zapis == true) {
                    Wear.wyslijWiadomosc(messageEvent.getPath(), "TRUE");
                } else {
                    Wear.wyslijWiadomosc(messageEvent.getPath(), "FALSE");
                    MainActivity.activity.pokazToast("Blad zapisu! Sprawdz GPS!");
                }
            } else {
                Wear.wyslijWiadomosc(messageEvent.getPath(), "FALSE");
                MainActivity.activity.pokazToast("Blad zapisu! Sprawdz GPS!");
            }
        }
    }

    private void obsluzZAZNACZONE(MessageEvent messageEvent) {
        if(messageEvent.getPath().startsWith("ZAZNACZONE:")) {
            String dowyslania = "";
            for(int i = 0; i < PlikiGPX.pliki.size(); i++) {
                if(PlikiGPX.pliki.get(i).zaznaczony == true) {
                    dowyslania = dowyslania + PlikiGPX.pliki.get(i).nazwa + ":";
                }
            }
            Wear.wyslijWiadomosc(messageEvent.getPath(), dowyslania);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        obsluzPING(messageEvent);
        obsluzFILELIST(messageEvent);
        obsluzFILEGET(messageEvent);
        obsluzPOINT(messageEvent);
        obsluzZAZNACZONE(messageEvent);
    }
}
