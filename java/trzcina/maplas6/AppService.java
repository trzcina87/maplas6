package trzcina.maplas6;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

import trzcina.maplas6.atlasy.Atlas;
import trzcina.maplas6.atlasy.Atlasy;
import trzcina.maplas6.atlasy.TmiParser;
import trzcina.maplas6.lokalizacja.PlikiGPX;
import trzcina.maplas6.pomoc.Bitmapy;
import trzcina.maplas6.pomoc.Komunikaty;
import trzcina.maplas6.pomoc.Przygotowanie;
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.pomoc.Stale;
import trzcina.maplas6.ustawienia.Ustawienia;
import trzcina.maplas6.watki.KompasWatek;
import trzcina.maplas6.watki.LuxWatek;
import trzcina.maplas6.watki.RysujWatek;
import trzcina.maplas6.watki.WczytajWatek;

@SuppressWarnings("PointlessBooleanExpression")
public class AppService extends Service {

    public static AppService service;
    private boolean wystartowany;                           //Czy serwis juz dziala
    public static volatile int widok = Stale.WIDOKBRAK;     //Zmienna odpowiedzialna za aktualny widok programu (mapa, opcje, wybor plikow itp)
    public volatile Point srodekekranu;                     //Piksel na telefonie w ktorym znajduje sie srodek ekranu telefonu, statyczna wartosc
    public volatile Point pixelnamapienadsrodkiem;         //Pixel na mapie nad ktorym znajduje sie wlasnie srodek ekranu
    public volatile Atlas atlas;
    public volatile TmiParser tmiparser;

    //Watki programu
    public LuxWatek luxwatek;
    public RysujWatek rysujwatek;
    public KompasWatek kompaswatek;
    public WczytajWatek wczytajwatek;

    public volatile boolean przelaczajpogps;

    //Zerowanie watkow
    private void watkiNaNull() {
        luxwatek = null;
        rysujwatek = null;
        kompaswatek = null;
        wczytajwatek = null;
    }

    //Konstruktor
    public AppService() {
        service = this;
        wystartowany = false;
        watkiNaNull();
        srodekekranu = new Point();
        atlas = null;
        tmiparser = null;
        pixelnamapienadsrodkiem = new Point();
        przelaczajpogps = true;
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
        wczytajwatek = new WczytajWatek();
        luxwatek.start();
        rysujwatek.start();
        kompaswatek.start();
        wczytajwatek.start();
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

                //Wczytujemy dostepne pliki
                MainActivity.activity.ustawProgressPrzygotowanie(5);
                MainActivity.activity.ustawInfoPrzygotowanie("Wczytuje pliki...");
                PlikiGPX.szukajPlikow();

                //Przechodzimy do widoku mapy i startujemy watki
                MainActivity.activity.zakonczPrzygotowanie();
                wystartujWatkiProgramu();
                zaczytajOpcje(false, 0, 0);
                pokazPierwszyToast();
            }
        }).start();
    }

    private void pokazPierwszyToast() {
        if(atlas == null) {
            MainActivity.activity.pokazToast(Komunikaty.BRAKATLASOW);
        } else {
            MainActivity.activity.pokazToast(atlas.nazwa);
        }
    }

    public void zaczytajOpcje(boolean zachowajwspolrzene, float gpsx, float gpsy) {
        Atlas nowyatlas = Atlasy.znajdzAtlasPoNazwie(Ustawienia.atlas.wartosc);
        if((nowyatlas != atlas) || (nowyatlas == null)) {
            atlas = nowyatlas;
            if (atlas != null) {
                tmiparser = atlas.parserytmi.get(atlas.parserytmi.size() - 1);
                if(zachowajwspolrzene == false) {
                    pixelnamapienadsrodkiem.set(tmiparser.rozmiarmapy.x / 2, tmiparser.rozmiarmapy.y / 2);
                } else {
                    pixelnamapienadsrodkiem.set(tmiparser.obliczPixelXDlaWspolrzednej(gpsx), tmiparser.obliczPixelYDlaWspolrzednej(gpsy));
                }
            }
            wczytajwatek.przeladujkonfiguracje = true;
            rysujwatek.przeladujkonfiguracje = true;
        }
        rysujwatek.odswiez = true;
    }

    public void poprawPixelNadSrodkiem() {
        if(pixelnamapienadsrodkiem.x < 0) {
            pixelnamapienadsrodkiem.x = 0;
        }
        if(pixelnamapienadsrodkiem.y < 0) {
            pixelnamapienadsrodkiem.y = 0;
        }
        if(atlas != null) {
            if(pixelnamapienadsrodkiem.x >= tmiparser.rozmiarmapy.x) {
                pixelnamapienadsrodkiem.x = tmiparser.rozmiarmapy.x - 1;
            }
            if(pixelnamapienadsrodkiem.y >= tmiparser.rozmiarmapy.y) {
                pixelnamapienadsrodkiem.y = tmiparser.rozmiarmapy.y - 1;
            }
        }
    }

    private void odswiezUIGPS() {
        if(tmiparser != null) {
            float gpsx = Rozne.zaokraglij5(tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x));
            float gpsy = Rozne.zaokraglij5(tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y));
            String gps = String.format(Locale.getDefault(), "%.5f", gpsx) + " " + String.format(Locale.getDefault(), "%.5f", gpsy);
            MainActivity.activity.ustawGPSText(gps);
        }
    }

    public void odswiezUI() {
        odswiezUIGPS();
    }

    public void zmianaSurface(int szerokosc, int wysokosc) {
        srodekekranu.set(szerokosc / 2, wysokosc / 2);
        odswiezUI();
        wczytajwatek.odswiez = true;
        rysujwatek.odswiez = true;
    }

    public void wczytajKolejnaMape() {
        if(Atlasy.atlasy.size() > 0) {
            int index = Atlasy.atlasy.indexOf(atlas);
            int kolejny = 0;
            if (index >= 0) {
                kolejny = index + 1;
            }
            if (kolejny == Atlasy.atlasy.size()) {
                kolejny = 0;
            }
            float gpsx = 0;
            float gpsy = 0;
            if(AppService.service.przelaczajpogps == true) {
                gpsx = tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x);
                gpsy = tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y);
                int proby = 0;
                int limit = Atlasy.atlasy.size() + 3;
                while((proby < limit) && (Atlasy.atlasy.get(kolejny).czyWspolrzedneWewnatrz(gpsx, gpsy) == false)) {
                    proby = proby + 1;
                    kolejny = kolejny + 1;
                    if(kolejny == Atlasy.atlasy.size()) {
                        kolejny = 0;
                    }
                }
                if(proby == limit) {
                    kolejny = index;
                }
            }
            String nazwa = Atlasy.atlasy.get(kolejny).nazwa;
            Ustawienia.atlas.wartosc = nazwa;
            Ustawienia.atlas.zapiszWartoscDoUstawien();
            MainActivity.activity.pokazToast(nazwa);
            odswiezUI();
            if(AppService.service.przelaczajpogps == true) {
                zaczytajOpcje(true, gpsx, gpsy);
            } else {
                zaczytajOpcje(false, 0, 0);
            }
        } else {
            MainActivity.activity.pokazToast(Komunikaty.BRAKATLASOW);
            zaczytajOpcje(false, 0, 0);
        }
    }

    public void pomniejszMape() {
        if(atlas == null) {
            MainActivity.activity.pokazToast(Komunikaty.BRAKATLASOW);
        } else {
            int index = atlas.parserytmi.indexOf(tmiparser);
            if(index == 0) {
                MainActivity.activity.pokazToast(Komunikaty.BLADODDALANIA);
            } else {
                float gpsx = tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x);
                float gpsy = tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y);
                tmiparser = atlas.parserytmi.get(index - 1);
                pixelnamapienadsrodkiem.set(tmiparser.obliczPixelXDlaWspolrzednej(gpsx), tmiparser.obliczPixelYDlaWspolrzednej(gpsy));
                poprawPixelNadSrodkiem();
                odswiezUI();
                wczytajwatek.przeladujkonfiguracje = true;
                rysujwatek.przeladujkonfiguracje = true;
                rysujwatek.odswiez = true;
            }
        }
    }

    public void powiekszMape() {
        if(atlas == null) {
            MainActivity.activity.pokazToast(Komunikaty.BRAKATLASOW);
        } else {
            int index = atlas.parserytmi.indexOf(tmiparser);
            if(index == atlas.parserytmi.size() - 1) {
                MainActivity.activity.pokazToast(Komunikaty.BLADPRZYBLIZANIA);
            } else {
                float gpsx = tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x);
                float gpsy = tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y);
                tmiparser = atlas.parserytmi.get(index + 1);
                pixelnamapienadsrodkiem.set(tmiparser.obliczPixelXDlaWspolrzednej(gpsx), tmiparser.obliczPixelYDlaWspolrzednej(gpsy));
                poprawPixelNadSrodkiem();
                odswiezUI();
                wczytajwatek.przeladujkonfiguracje = true;
                rysujwatek.przeladujkonfiguracje = true;
                rysujwatek.odswiez = true;
            }
        }
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

    //Konczymy watek wczytaj
    private void zakonczWatekWczytaj() {
        if(wczytajwatek != null) {
            wczytajwatek.zakoncz = true;
            zakonczWatek(wczytajwatek);
        }
    }

    //Konczymy uruchomione watki
    private void zakonczWatki() {
        zakonczWatekLux();
        zakonczWatekRysuj();
        zakonczWatekKompas();
        zakonczWatekWczytaj();
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
