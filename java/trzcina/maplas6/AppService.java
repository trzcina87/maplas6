package trzcina.maplas6;

import android.app.Service;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.Locale;

import trzcina.maplas6.atlasy.Atlas;
import trzcina.maplas6.atlasy.Atlasy;
import trzcina.maplas6.atlasy.TmiParser;
import trzcina.maplas6.lokalizacja.GPSListener;
import trzcina.maplas6.lokalizacja.GPXTrasaLogger;
import trzcina.maplas6.lokalizacja.GPXPunktLogger;
import trzcina.maplas6.lokalizacja.PlikiGPX;
import trzcina.maplas6.lokalizacja.PunktWTrasie;
import trzcina.maplas6.pomoc.Bitmapy;
import trzcina.maplas6.pomoc.Komunikaty;
import trzcina.maplas6.pomoc.Przygotowanie;
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.pomoc.Stale;
import trzcina.maplas6.ustawienia.Ustawienia;
import trzcina.maplas6.watki.DzwiekiWatek;
import trzcina.maplas6.watki.KompasWatek;
import trzcina.maplas6.watki.LuxWatek;
import trzcina.maplas6.watki.RysujWatek;
import trzcina.maplas6.watki.WczytajWatek;

@SuppressWarnings({"PointlessBooleanExpression", "MissingPermission"})
public class AppService extends Service {

    public static AppService service;
    private boolean wystartowany;                           //Czy serwis juz dziala
    public static volatile int widok = Stale.WIDOKBRAK;     //Zmienna odpowiedzialna za aktualny widok programu (mapa, opcje, wybor plikow itp)
    public volatile Point srodekekranu;                     //Piksel na telefonie w ktorym znajduje sie srodek ekranu telefonu, statyczna wartosc
    public volatile Point pixelnamapienadsrodkiem;         //Pixel na mapie nad ktorym znajduje sie wlasnie srodek ekranu
    public volatile Atlas atlas;
    public volatile TmiParser tmiparser;
    public volatile int poziominfo;
    public volatile GPXTrasaLogger obecnatrasa;
    public volatile boolean czakamnapierwszyfix;
    public volatile boolean przesuwajmapezgps;
    public GPSListener gpslistener;
    public LocationManager locationmanager;
    public boolean gpszarejestrowany;
    public volatile int kolorinfo;


    //Watki programu
    public LuxWatek luxwatek;
    public RysujWatek rysujwatek;
    public KompasWatek kompaswatek;
    public WczytajWatek wczytajwatek;
    public HandlerThread gpswatek;
    public Looper loopergps;
    public DzwiekiWatek dzwiekiwatek;

    public volatile boolean przelaczajpogps;
    public volatile boolean wlaczgps;
    public volatile boolean precyzyjnygps;
    public volatile boolean grajdzwieki;

    //Zerowanie watkow
    private void watkiNaNull() {
        luxwatek = null;
        rysujwatek = null;
        kompaswatek = null;
        wczytajwatek = null;
        gpswatek = null;
        loopergps = null;
        dzwiekiwatek = null;
    }

    //Konstruktor
    public AppService() {
        service = this;
        wystartowany = false;
        watkiNaNull();
        srodekekranu = new Point();
        atlas = null;
        kolorinfo = 0;
        tmiparser = null;
        gpslistener = null;
        locationmanager = null;
        czakamnapierwszyfix = false;
        pixelnamapienadsrodkiem = new Point();
        przelaczajpogps = true;
        precyzyjnygps = true;
        wlaczgps = false;
        grajdzwieki = true;
        gpszarejestrowany = false;
        przesuwajmapezgps = false;
        poziominfo = Stale.OPISYPUNKTY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void wystartujWatekGPS() {
        gpswatek = new HandlerThread("gpswatek");
        gpswatek.start();
        loopergps = gpswatek.getLooper();
        gpslistener = new GPSListener();
        gpslistener.zerujZmienne();
        locationmanager = (LocationManager)getSystemService(getApplicationContext().LOCATION_SERVICE);
    }

    private void rejestrujGPS() {
        if(gpszarejestrowany == false) {
            czakamnapierwszyfix = true;
            dzwiekiwatek.zagralemblad = false;
            locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, gpslistener, loopergps);
            locationmanager.addGpsStatusListener(gpslistener);
            dzwiekiwatek.czasostatniejlokalizacji = System.currentTimeMillis();
            gpszarejestrowany = true;
        }
    }

    private void wyrejestrujGPS() {
        if(gpszarejestrowany == true) {
            locationmanager.removeUpdates(gpslistener);
            locationmanager.removeGpsStatusListener(gpslistener);
            czakamnapierwszyfix = false;
            gpszarejestrowany = false;
        }
    }

    public void zmianaTrybuGPS() {
        if(wlaczgps == true) {
            gpslistener.zerujZmienne();
            obecnatrasa = new GPXTrasaLogger();
            rejestrujGPS();
        } else {
            wyrejestrujGPS();
            if(obecnatrasa != null) {
                obecnatrasa.zakonczPlik();
            }
            gpslistener.zerujZmienne();
            obecnatrasa = null;
        }
        odswiezUI();
        rysujwatek.odswiez = true;
    }

    private void wysrodkujDoLokalizacji(Location lokalizacja) {
        float gpsx = Rozne.zaokraglij5((float) lokalizacja.getLongitude());
        float gpsy = Rozne.zaokraglij5((float) lokalizacja.getLatitude());
        int x = tmiparser.obliczPixelXDlaWspolrzednej(gpsx);
        int y = tmiparser.obliczPixelYDlaWspolrzednej(gpsy);
        pixelnamapienadsrodkiem.set(x, y);
        poprawPixelNadSrodkiem();
        odswiezUI();
        rysujwatek.odswiez = true;
    }

    public void zlapalemPierwszyFix() {
        czakamnapierwszyfix = false;
        if(tmiparser != null) {
            Location lokalizacja = czyJestFix();
            if(lokalizacja != null) {
                wysrodkujDoLokalizacji(lokalizacja);
                przesuwajmapezgps = true;
            }
        }
    }

    public void wysrodkujMapeDoGPS() {
        if(tmiparser != null) {
            Location lokalizacja = czyJestFix();
            if(lokalizacja != null) {
                wysrodkujDoLokalizacji(lokalizacja);
                przesuwajmapezgps = true;
            } else {
                MainActivity.activity.pokazToast("Sprawdz GPS!");
            }
        }
    }

    public void przesunMapeZGPS(Location lokalizacja) {
        if(tmiparser != null) {
            if(lokalizacja != null) {
                wysrodkujDoLokalizacji(lokalizacja);
            } else {
                MainActivity.activity.pokazToast("Sprawdz GPS!");
            }
        }
    }

    //Tworzymy i startujemy wszystkie watki programu
    private void wystartujWatkiProgramu() {
        luxwatek = new LuxWatek();
        rysujwatek = new RysujWatek();
        kompaswatek = new KompasWatek();
        wczytajwatek = new WczytajWatek();
        dzwiekiwatek = new DzwiekiWatek();
        luxwatek.start();
        rysujwatek.start();
        kompaswatek.start();
        wczytajwatek.start();
        dzwiekiwatek.start();
        wystartujWatekGPS();
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

                GPXPunktLogger.inicjuj();

                //Przechodzimy do widoku mapy i startujemy watki
                MainActivity.activity.zakonczPrzygotowanie();
                wystartujWatkiProgramu();
                zaczytajOpcje(false, 0, 0);
                pokazPierwszyToast();
            }
        }).start();
    }

    public void zmienPoziomInfo() {
        poziominfo = (poziominfo + 1) % 4;
        if(poziominfo == Stale.OPISYBRAK) {
            MainActivity.activity.pokazToast("Brak punktow");
        }
        if(poziominfo == Stale.OPISYPUNKTY) {
            MainActivity.activity.pokazToast("Tylko punkty");
        }
        if(poziominfo == Stale.OPISYNAZWY) {
            MainActivity.activity.pokazToast("Punkty i nazwa");
        }
        if(poziominfo == Stale.OPISYKOMENTARZE) {
            MainActivity.activity.pokazToast("Punkty nazwy i opisy");
        }
        if(poziominfo == Stale.OPISYBRAK) {
            MainActivity.activity.pokazIkoneOpisowWylaczonych();
        } else {
            MainActivity.activity.pokazIkoneOpisow();
        }
        rysujwatek.odswiez = true;
    }

    private void pokazPierwszyToast() {
        if(atlas == null) {
            MainActivity.activity.pokazToast(Komunikaty.BRAKATLASOW);
        } else {
            MainActivity.activity.pokazToast(atlas.nazwa);
        }
        odswiezUI();
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

    public Location czyJestFix() {
        if(wlaczgps == true) {
            if (gpslistener.ostatnialokalizacjazgps != null) {
                if (gpslistener.ostatnialokalizacjazgps.getTime() + 10000 >= System.currentTimeMillis()) {
                    if(precyzyjnygps == true) {
                        return new Location(gpslistener.ostatnialokalizacjazgps);
                    } else {
                        return new Location(gpslistener.ostatnialokalizacja);
                    }
                }
            }
            if (gpslistener.ostatnialokalizacja != null) {
                if (gpslistener.ostatnialokalizacja.getTime() + 10000 >= System.currentTimeMillis()) {
                    return new Location(gpslistener.ostatnialokalizacja);
                }
            }
            return null;
        } else {
            return null;
        }
    }

    public boolean czyJestFixBool() {
        if(wlaczgps == true) {
            if (gpslistener.ostatnialokalizacjazgps != null) {
                if (gpslistener.ostatnialokalizacjazgps.getTime() + 10000 >= System.currentTimeMillis()) {
                    return true;
                }
            }
            if (gpslistener.ostatnialokalizacja != null) {
                if (gpslistener.ostatnialokalizacja.getTime() + 10000 >= System.currentTimeMillis()) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private void odswiezUIGPS() {
        if(tmiparser != null) {
            if(wlaczgps == false) {
                float gpsx = Rozne.zaokraglij5(tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x));
                float gpsy = Rozne.zaokraglij5(tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y));
                String gps = String.format(Locale.getDefault(), "%.5f", gpsx) + " " + String.format(Locale.getDefault(), "%.5f", gpsy);
                MainActivity.activity.ustawGPSText(gps);
            } else {
                float dystans = obecnatrasa.dlugosctrasy;
                float odlegloscodpoczatku = obecnatrasa.odlegloscodpoczatku;
                float gpsx = Rozne.zaokraglij5(tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x));
                float gpsy = Rozne.zaokraglij5(tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y));
                Location lokalizacja = czyJestFix();
                float odlegloscodkursora = 0;
                if(lokalizacja != null) {
                    odlegloscodkursora = PunktWTrasie.zmierzDystans(new PunktWTrasie(gpsx, gpsy), new PunktWTrasie((float)lokalizacja.getLongitude(), (float) lokalizacja.getLatitude()));
                }
                MainActivity.activity.ustawGPSText(Rozne.formatujDystans(Math.round(dystans)) + " " + Rozne.formatujDystans(Math.round(odlegloscodpoczatku)) + " " + Rozne.formatujDystans(Math.round(odlegloscodkursora)));
            }
        }
    }

    private void odswiezUISatelity() {
        MainActivity.activity.ustawSatelityText(gpslistener.iloscaktywnychsatelitow + "/" + gpslistener.iloscsatelitow + " ");
    }

    private void odswiezUIIkonaSatelity() {
        if(czyJestFixBool()) {
            MainActivity.activity.ustawSateliteZielona();
        } else {
            MainActivity.activity.ustawSateliteCzerwona();
        }
    }

    public void odswiezUI() {
        odswiezUIGPS();
        odswiezUISatelity();
        odswiezUIIkonaSatelity();
    }

    public void zmienKolorInfo() {
        kolorinfo = (kolorinfo + 1) % 4;
        rysujwatek.odswiez = true;
    }

    public void zmianaSurface(int szerokosc, int wysokosc) {
        srodekekranu.set(szerokosc / 2, wysokosc / 2);
        odswiezUI();
        if(wczytajwatek != null) {
            wczytajwatek.odswiez = true;
        }
        if(rysujwatek != null) {
            rysujwatek.przeladujkonfiguracje = true;
            rysujwatek.odswiez = true;
        }
    }

    public boolean zapiszPunktPozycjaKursora(String nazwa, String komentarz) {
        if(tmiparser != null) {
            float gpsx = Rozne.zaokraglij5(tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x));
            float gpsy = Rozne.zaokraglij5(tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y));
            Boolean czyzpias = GPXPunktLogger.zapiszPunkt(gpsx, gpsy, nazwa, komentarz);
            rysujwatek.odswiez = true;
            if(czyzpias == true) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean zapiszPunktPozycjaGPS(String nazwa, String komentarz) {
        return false;
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


    //Konczymy watek wczytaj
    private void zakonczWatekDzwieki() {
        if(dzwiekiwatek != null) {
            dzwiekiwatek.zakoncz = true;
            zakonczWatek(dzwiekiwatek);
        }
    }

    private void zakonczWatekGPS() {
        if(gpswatek != null) {
            gpswatek.quit();
            while(gpswatek.isAlive()) {
                try {
                    gpswatek.join();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    //Konczymy uruchomione watki
    private void zakonczWatki() {
        zakonczWatekLux();
        zakonczWatekRysuj();
        zakonczWatekKompas();
        zakonczWatekWczytaj();
        zakonczWatekDzwieki();
        zakonczWatekGPS();
    }

    public void zakonczUsluge() {
        GPXPunktLogger.zakonczPlik();
        if(obecnatrasa != null) {
            obecnatrasa.zakonczPlik();
        }
        wystartowany = false;
        widok = Stale.WIDOKBRAK;
        wyrejestrujGPS();
        zakonczWatki();
        Toast.makeText(getApplicationContext(), Komunikaty.KONIECPROGRAMU, Toast.LENGTH_SHORT).show();
    }

    //Zakonczenie apliakcji, konczymy watki, zerujemy zmienne i pokazujemy komunikat
    @Override
    public void onDestroy() {
        super.onDestroy();
        zakonczUsluge();
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
