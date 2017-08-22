package trzcina.maplas6;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;

import trzcina.maplas6.atlasy.Atlasy;
import trzcina.maplas6.pomoc.Bitmapy;
import trzcina.maplas6.pomoc.Komunikaty;
import trzcina.maplas6.pomoc.Przygotowanie;
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.pomoc.Stale;
import trzcina.maplas6.watki.KompasWatek;
import trzcina.maplas6.watki.LuxWatek;
import trzcina.maplas6.watki.RysujWatek;

public class AppService extends Service {

    public static AppService service;
    private boolean wystartowany;
    public static volatile int widok = Stale.WIDOKBRAK;
    public volatile Point srodekekranu;

    public LuxWatek luxwatek;
    public RysujWatek rysujwatek;
    public KompasWatek kompaswatek;

    private void watkiNaNull() {
        luxwatek = null;
        rysujwatek = null;
        kompaswatek = null;
    }

    public AppService() {
        service = this;
        wystartowany = false;
        watkiNaNull();
        srodekekranu = new Point();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void wystartujWatkiProgramu() {
        luxwatek = new LuxWatek();
        rysujwatek = new RysujWatek();
        kompaswatek = new KompasWatek();
        luxwatek.start();
        rysujwatek.start();
        kompaswatek.start();
    }

    private void wystartujWatekPrzygotowania() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Rozne.czekaj(100);
                MainActivity.activity.ustawProgressPrzygotowanie(1);
                MainActivity.activity.ustawInfoPrzygotowanie("Tworze katalogi...");
                Przygotowanie.utworzKatalogi();
                MainActivity.activity.ustawProgressPrzygotowanie(2);
                MainActivity.activity.ustawInfoPrzygotowanie("Usuwam stare pliki...");
                Przygotowanie.usunKosz();
                MainActivity.activity.ustawProgressPrzygotowanie(3);
                MainActivity.activity.ustawInfoPrzygotowanie("Wczytuje bitmapy...");
                Bitmapy.inicjujBitmapy();
                MainActivity.activity.ustawProgressPrzygotowanie(4);
                MainActivity.activity.ustawInfoPrzygotowanie("Wczytuje atlasy...");
                try {
                    Atlasy.szukajAtlasow();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MainActivity.activity.zakonczPrzygotowanie();
                wystartujWatkiProgramu();
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(wystartowany == false) {
            wystartowany = true;
            wystartujWatekPrzygotowania();
        }
        return START_NOT_STICKY;
    }

    private void zakonczWatek(Thread watek) {
        watek.interrupt();
        while(watek.isAlive()) {
            try {
                watek.join();
            } catch (InterruptedException e) {
            }
        }
    }

    private void zakonczWatekLux() {
        if(luxwatek != null) {
            luxwatek.zakoncz = true;
            zakonczWatek(luxwatek);
        }
    }

    private void zakonczWatekRysuj() {
        if(rysujwatek != null) {
            rysujwatek.zakoncz = true;
            zakonczWatek(rysujwatek);
        }
    }

    private void zakonczWatekKompas() {
        if(kompaswatek != null) {
            kompaswatek.zakoncz = true;
            zakonczWatek(kompaswatek);
        }
    }

    private void zakonczWatki() {
        zakonczWatekLux();
        zakonczWatekRysuj();
        zakonczWatekKompas();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        zakonczWatki();
        wystartowany = false;
        widok = Stale.WIDOKBRAK;
        Toast.makeText(getApplicationContext(), Komunikaty.KONIECPROGRAMU, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    @Override
    public void onLowMemory() {
    }

    @Override
    public void onTrimMemory(int level) {
    }

}
