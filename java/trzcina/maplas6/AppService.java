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

@SuppressWarnings("PointlessBooleanExpression")
public class AppService extends Service {

    public static AppService service;
    private boolean wystartowany;                           //Czy serwis juz dziala
    public static volatile int widok = Stale.WIDOKBRAK;     //Zmienna odpowiedzialna za aktualny widok programu (mapa, opcje, wybor plikow itp)
    public volatile Point srodekekranu;                     //Piksel w ktorym znajduje sie srodek ekranu

    //Watki programu
    public LuxWatek luxwatek;
    public RysujWatek rysujwatek;
    public KompasWatek kompaswatek;

    //Zerowanie watkow
    private void watkiNaNull() {
        luxwatek = null;
        rysujwatek = null;
        kompaswatek = null;
    }

    //Konstruktor
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

    //Tworzymy i startujemy wszystkie watki programu
    private void wystartujWatkiProgramu() {
        luxwatek = new LuxWatek();
        rysujwatek = new RysujWatek();
        kompaswatek = new KompasWatek();
        luxwatek.start();
        rysujwatek.start();
        kompaswatek.start();
    }

    private void wystartujWatekPrzygotowania() {

        //Watek przygotowawczy dziala w tle i komunikuje sie z Activity
        new Thread(new Runnable() {
            @Override
            public void run() {

                //Tworzymy niezbene katalogi
                MainActivity.activity.ustawProgressPrzygotowanie(1);
                MainActivity.activity.ustawInfoPrzygotowanie("Tworze katalogi...");
                Przygotowanie.utworzKatalogi();

                //Usuwamy stare pliki z kosza
                MainActivity.activity.ustawProgressPrzygotowanie(2);
                MainActivity.activity.ustawInfoPrzygotowanie("Usuwam stare pliki...");
                Przygotowanie.usunKosz();

                //Wczytujemy bitmapy do pamieci
                MainActivity.activity.ustawProgressPrzygotowanie(3);
                MainActivity.activity.ustawInfoPrzygotowanie("Wczytuje bitmapy...");
                Bitmapy.inicjujBitmapy();

                //Wczytujemy dostepne atlasy
                MainActivity.activity.ustawProgressPrzygotowanie(4);
                MainActivity.activity.ustawInfoPrzygotowanie("Wczytuje atlasy...");
                Atlasy.szukajAtlasow();

                //Przechodzimy do widoku mapy i startujemy watki
                MainActivity.activity.zakonczPrzygotowanie();
                wystartujWatkiProgramu();
            }
        }).start();
    }

    //Serwis startuje (po starcie MainActivity)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //Nie uruchmiamy jest juz jest uruchomiony
        if(wystartowany == false) {
            wystartowany = true;
            wystartujWatekPrzygotowania();
        }

        //Nie wzniawiamy serwisu automatycznie
        return START_NOT_STICKY;
    }

    //Czeka na zakonczenie watku
    private void zakonczWatek(Thread watek) {
        watek.interrupt();
        while(watek.isAlive()) {
            try {
                watek.join();
            } catch (InterruptedException e) {
            }
        }
    }

    //Konczymy watek swiatlomierza
    private void zakonczWatekLux() {
        if(luxwatek != null) {
            luxwatek.zakoncz = true;
            zakonczWatek(luxwatek);
        }
    }

    //Konczymy watek rysowania
    private void zakonczWatekRysuj() {
        if(rysujwatek != null) {
            rysujwatek.zakoncz = true;
            zakonczWatek(rysujwatek);
        }
    }

    //Konczymy watek kompasu
    private void zakonczWatekKompas() {
        if(kompaswatek != null) {
            kompaswatek.zakoncz = true;
            zakonczWatek(kompaswatek);
        }
    }

    //Konczymy uruchomione watki
    private void zakonczWatki() {
        zakonczWatekLux();
        zakonczWatekRysuj();
        zakonczWatekKompas();
    }

    //Zakonczenie apliakcji, konczymy watki, zerujemy zmienne i pokazujemy komunikat
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
