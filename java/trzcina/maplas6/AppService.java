package trzcina.maplas6;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

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
import trzcina.maplas6.pomoc.OdbiorZNotyfikacji;
import trzcina.maplas6.pomoc.Przygotowanie;
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.pomoc.Stale;
import trzcina.maplas6.ustawienia.Ustawienia;
import trzcina.maplas6.watki.DzwiekiWatek;
import trzcina.maplas6.watki.InternetWyslijWatek;
import trzcina.maplas6.watki.KompasWatek;
import trzcina.maplas6.watki.LuxWatek;
import trzcina.maplas6.watki.NotyfikacjaWatek;
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
    private DateFormat formatczasu;


    //Watki programu
    public LuxWatek luxwatek;
    public RysujWatek rysujwatek;
    public KompasWatek kompaswatek;
    public WczytajWatek wczytajwatek;
    public HandlerThread gpswatek;
    public Looper loopergps;
    public DzwiekiWatek dzwiekiwatek;
    public NotyfikacjaWatek notyfikacjawatek;
    public InternetWyslijWatek internetwyslijwatek;

    public volatile boolean przelaczajpogps;
    public volatile boolean wlaczgps;
    public volatile boolean precyzyjnygps;
    public volatile boolean grajdzwieki;
    public volatile boolean trybsamochodowy;
    public volatile boolean internetwyslij;

    //Notyfikcja
    public volatile RemoteViews widokmalejnotyfikacji;
    public volatile NotificationManager notificationmanager;
    public volatile Notification notyfikacja;
    public volatile OdbiorZNotyfikacji odbiorznotyfikacji;

    public List<String> listaplikowaplikacji;

    public long[] plikmerkatora;

    public volatile int zoom;

    //Zerowanie watkow
    private void watkiNaNull() {
        luxwatek = null;
        rysujwatek = null;
        kompaswatek = null;
        wczytajwatek = null;
        gpswatek = null;
        loopergps = null;
        dzwiekiwatek = null;
        notyfikacjawatek = null;
        internetwyslijwatek = null;

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
        trybsamochodowy = false;
        gpszarejestrowany = false;
        przesuwajmapezgps = false;
        internetwyslij = false;
        poziominfo = Stale.OPISYPUNKTY;
        widokmalejnotyfikacji = null;
        notificationmanager = null;
        notyfikacja = null;
        odbiorznotyfikacji = null;
        plikmerkatora = null;
        zoom = 10;
        formatczasu = new SimpleDateFormat("H:mm");
        formatczasu.setTimeZone(TimeZone.getTimeZone("UTC"));
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
            if(trybsamochodowy == false) {
                locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Stale.GPSREGISTERCZAS, Stale.GPSREGISTERMETRYPIESZY, gpslistener, loopergps);
            } else {
                locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Stale.GPSREGISTERCZAS, Stale.GPSREGISTERMETRYSAMOCHODOWY, gpslistener, loopergps);
            }
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

    public void zmianaTrybuGPS(boolean usunacplik) {
        if(wlaczgps == true) {
            gpslistener.zerujZmienne();
            obecnatrasa = new GPXTrasaLogger();
            rejestrujGPS();
        } else {
            wyrejestrujGPS();
            if(obecnatrasa != null) {
                if(usunacplik == false) {
                    obecnatrasa.zakonczPlik();
                } else {
                    obecnatrasa.zakonczPlikIOdrzuc();
                }
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
        notyfikacjawatek = new NotyfikacjaWatek();
        internetwyslijwatek = new InternetWyslijWatek();
        luxwatek.start();
        rysujwatek.start();
        kompaswatek.start();
        wczytajwatek.start();
        dzwiekiwatek.start();
        notyfikacjawatek.start();
        internetwyslijwatek.start();
        wystartujWatekGPS();
    }

    private void wystartujWatekPrzygotowania() {

        //Watek przygotowawczy dziala w tle i komunikuje sie z Activity
        new Thread(new Runnable() {
            @Override
            public void run() {

                listaplikowaplikacji = Arrays.asList(MainActivity.activity.fileList());

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

                //Merkator
                //Wczytujemy bitmapy do pamieci
                MainActivity.activity.ustawProgressPrzygotowanie(4);
                MainActivity.activity.ustawInfoPrzygotowanie("Wczytuje plik Merkatora...");
                wczytajPlikMerkatora();

                //Wczytujemy dostepne atlasy
                MainActivity.activity.ustawProgressPrzygotowanie(5);
                MainActivity.activity.ustawInfoPrzygotowanie("Wczytuje atlasy...");
                Atlasy.szukajAtlasow();

                //Wczytujemy dostepne pliki
                MainActivity.activity.ustawProgressPrzygotowanie(6);
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

    public void przelaczNaMape(String nazwa, float wspx, float wspy) {
        Ustawienia.atlas.wartosc = nazwa;
        Ustawienia.atlas.zapiszWartoscDoUstawien();
        MainActivity.activity.pokazToast(nazwa);
        if (AppService.service.przelaczajpogps == true) {
            zaczytajOpcje(true, wspx, wspy);
        } else {
            zaczytajOpcje(false, 0, 0);
        }
        odswiezUI();
    }

    public void zaproponujZmianeMapy(final Location location) {
        final Atlas propozycja = Atlasy.szukajNajlepszejMapy(location);
        if(propozycja != null) {
            if(atlas == null) {
                przelaczNaMape(propozycja.nazwa, (float)location.getLongitude(), (float)location.getLatitude());
            } else {
                if (atlas.czyWspolrzedneWewnatrz((float) location.getLongitude(), (float) location.getLatitude())) {
                    if(propozycja.pobierzDokladnosc() < atlas.pobierzDokladnosc()) {
                        MainActivity.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(MainActivity.activity).setIcon(android.R.drawable.ic_dialog_alert).setTitle(Komunikaty.ZMIANAMAPY).setMessage("Mapa " + propozycja.nazwa + Komunikaty.DOKLADNIEJSZAMAPA).setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        przelaczNaMape(propozycja.nazwa, (float)location.getLongitude(), (float)location.getLatitude());
                                    }
                                }).setNegativeButton("Nie", null).show();
                            }
                        });
                    }
                } else {
                    przelaczNaMape(propozycja.nazwa, (float)location.getLongitude(), (float)location.getLatitude());
                }
            }
        }
    }

    public void zmienPoziomInfo() {
        poziominfo = (poziominfo + 1) % 6;
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
        if(poziominfo == Stale.OPISYODLEGLOSCI) {
            MainActivity.activity.pokazToast("Punkty nazwy i odległości");
        }
        if(poziominfo == Stale.OPISYODLEGLOSCIKOMENTARZE) {
            MainActivity.activity.pokazToast("Punkty nazwy opisy i odległości");
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
        int nasycenie = Ustawienia.nasycenie.wartosc;
        int kontrast = Ustawienia.kontrast.wartosc;
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
            zoom = 10;
            wczytajwatek.przeladujkonfiguracje = true;
            rysujwatek.przeladujkonfiguracje = true;
        }
        if((nasycenie != wczytajwatek.nasycenie) || (kontrast != wczytajwatek.kontrast)) {
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
                    if(gpslistener.ostatnialokalizacjazgps.getTime() >= obecnatrasa.czasstart) {
                        if (precyzyjnygps == true) {
                            return new Location(gpslistener.ostatnialokalizacjazgps);
                        } else {
                            return new Location(gpslistener.ostatnialokalizacja);
                        }
                    } else {
                        return null;
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
                    if(gpslistener.ostatnialokalizacjazgps.getTime() >= obecnatrasa.czasstart) {
                        return true;
                    } else {
                        return false;
                    }
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
                    odlegloscodkursora = PunktWTrasie.zmierzDystans(new PunktWTrasie(gpsx, gpsy, 0), new PunktWTrasie((float)lokalizacja.getLongitude(), (float) lokalizacja.getLatitude(), 0));
                }
                long czas = System.currentTimeMillis() - obecnatrasa.czasstart;
                String czasstring = formatczasu.format(czas);
                MainActivity.activity.ustawGPSText(Rozne.formatujDystans(Math.round(dystans)) + " " + Rozne.formatujDystans(Math.round(odlegloscodpoczatku)) + " " + Rozne.formatujDystans(Math.round(odlegloscodkursora)) + " " + czasstring + "h");
            }
        }
    }

    private void odswiezUISatelity() {
        MainActivity.activity.ustawSatelityText(gpslistener.iloscaktywnychsatelitow + "/" + gpslistener.iloscsatelitow + " (" + gpslistener.dokladnosc + "m)");
    }

    private void odswiezUIIkonaSatelity() {
        if(czyJestFixBool()) {
            MainActivity.activity.ustawSateliteZielona();
        } else {
            MainActivity.activity.ustawSateliteCzerwona();
        }
    }

    private void odswiezUIIkonaInternet() {
        if(wlaczgps == false) {
            MainActivity.activity.ustawInternetOK();
        } else {
            if(internetwyslij == true) {
                if (System.currentTimeMillis() >= internetwyslijwatek.ostatniawysylka + 150 * 1000) {
                    MainActivity.activity.ustawInternetFail();
                } else {
                    MainActivity.activity.ustawInternetOK();
                }
            } else {
                MainActivity.activity.ustawInternetFail();
            }
        }
    }

    public void odswiezUIZoom() {
        if(zoom == 10) {
            MainActivity.activity.ustawStatusZoom("");
        } else {
            MainActivity.activity.ustawStatusZoom(String.valueOf(zoom / (float)10 + "x "));
        }
    }

    public void odswiezUI() {
        if(MainActivity.activity.activitywidoczne == true) {
            odswiezUIGPS();
            odswiezUISatelity();
            odswiezUIIkonaSatelity();
            odswiezUIZoom();
            odswiezUIIkonaInternet();
        }
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

    private void przypiszPowrotDoAplikacjiDlaIkony() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingintent = PendingIntent.getActivity(this, 0, intent, 0);
        widokmalejnotyfikacji.setOnClickPendingIntent(R.id.notlassmall, pendingintent);
    }

    public void utworzNotyfikacje(int numer) {
        RemoteViews widoknotyfikacji = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notyfikacjalayout);
        int ikonywlayout[] = {R.id.icon0, R.id.icon1, R.id.icon2, R.id.icon3, R.id.icon4, R.id.icon5, R.id.icon6, R.id.icon7, R.id.icon8, R.id.icon9, R.id.icon10, R.id.icon11, R.id.icon12, R.id.icon13, R.id.icon14, R.id.icon15, R.id.icon16, R.id.icon17, R.id.icon18, R.id.icon19};
        int ikony[] = {R.mipmap.prawdziwek, R.mipmap.rydz, R.mipmap.kania, R.mipmap.kurka, R.mipmap.kowal, R.mipmap.piaskowiec, R.mipmap.czarnylepek, R.mipmap.maslak, R.mipmap.gaska, R.mipmap.kozlarek, R.mipmap.poziomka, R.mipmap.jagoda, R.mipmap.zurawina, R.mipmap.jerzyna, R.mipmap.parking, R.mipmap.atrakcja, R.mipmap.pomnik, R.mipmap.osrodek, R.mipmap.widok, R.mipmap.znacznik2};
        String opisy[] = {"Prawdziwek", "Rydz", "Kania", "Kurka", "Kowal", "Piaskowiec", "Czarny Lepek", "Maslak", "Gaska", "Kozlarek", "Poziomka", "Jagoda", "Zurawina", "Jerzyna", "Parking", "Atrakcja", "Pomnik", "Osrodek", "Widok", "Punkt"};
        for(int i = 0; i < 20; i++) {
            widoknotyfikacji.setImageViewResource(ikonywlayout[i], ikony[i]);
        }
        Intent intenty[] = new Intent[ikony.length];
        PendingIntent pendingintenty[] = new PendingIntent[ikony.length];
        for(int i = 0; i < ikony.length; i++) {
            intenty[i] = new Intent(opisy[i]);
            pendingintenty[i] = PendingIntent.getBroadcast(this, 0, intenty[i], 0);
            widoknotyfikacji.setOnClickPendingIntent(ikonywlayout[i], pendingintenty[i]);
        }
        widokmalejnotyfikacji = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notyfikacjasmalllayout);
        widokmalejnotyfikacji.setImageViewResource(R.id.notlassmall, R.mipmap.ikona);
        widokmalejnotyfikacji.setImageViewResource(R.id.znaczniksmall, R.mipmap.znacznik2);
        widokmalejnotyfikacji.setImageViewResource(R.id.notyfikacjaikonasatelity, R.mipmap.satelitaczerwony);
        widokmalejnotyfikacji.setOnClickPendingIntent(R.id.znaczniksmall, pendingintenty[19]);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.mipmap.ikona);
        builder = (NotificationCompat.Builder) builder.setCustomBigContentView(widoknotyfikacji).setContent(widokmalejnotyfikacji).setOngoing(true).setPriority(Notification.PRIORITY_MAX).setVisibility(Notification.VISIBILITY_PUBLIC);
        notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notyfikacja = builder.build();
        notificationmanager.notify(numer, notyfikacja);
        odbiorznotyfikacji = new OdbiorZNotyfikacji();
        IntentFilter[] filtry = new IntentFilter[ikony.length];
        for(int i = 0; i < ikony.length; i++) {
            filtry[i] = new IntentFilter(opisy[i]);
            registerReceiver(odbiorznotyfikacji, filtry[i]);
        }
        przypiszPowrotDoAplikacjiDlaIkony();
    }

    public void notyfikacjaUstawStanGPS(final String string) {
        try {
            widokmalejnotyfikacji.setTextViewText(R.id.notyfikacjastangps, string);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notyfikacjaUstawSatelity(final String string) {
        try {
            widokmalejnotyfikacji.setTextViewText(R.id.notyfikacjailoscsatelit, string);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notyfikacjaUstawSzczegoly(final String string) {
        try {
            widokmalejnotyfikacji.setTextViewText(R.id.notyfikacjaszczegolytrasy, string);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notyfikacjaUstawCzas(final String string) {
        try {
            widokmalejnotyfikacji.setTextViewText(R.id.notyfikacjaczas, string);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notyfikacjaUstawIkoneGPS(final int zasob) {
        try {
            widokmalejnotyfikacji.setImageViewResource(R.id.notyfikacjaikonasatelity, zasob);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notyfikacjaUstawIkoneInternet(final int zasob) {
        try {
            widokmalejnotyfikacji.setImageViewResource(R.id.internetimageviewsmall, zasob);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notyfikacjaZatwierdz(int numer) {
        try {
            notificationmanager.notify(numer, notyfikacja);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean zapiszPunktPozycjaKursora(String nazwa, String komentarz) {
        if(tmiparser != null) {
            float gpsx = Rozne.zaokraglij5(tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x));
            float gpsy = Rozne.zaokraglij5(tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y));
            Boolean czyzpias = GPXPunktLogger.zapiszPunkt(gpsx, gpsy, nazwa, komentarz, -1);
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
        Location location = czyJestFix();
        if(location != null) {
            Boolean czyzapis = GPXPunktLogger.zapiszPunkt(Rozne.zaokraglij5((float) location.getLongitude()), Rozne.zaokraglij5((float) location.getLatitude()), nazwa, komentarz, location.getAccuracy());
            rysujwatek.odswiez = true;
            if(czyzapis == true) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
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
                Location lokalizacja = czyJestFix();
                if(lokalizacja == null) {
                    gpsx = tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x);
                    gpsy = tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y);
                } else {
                    gpsx = (float)lokalizacja.getLongitude();
                    gpsy = (float)lokalizacja.getLatitude();
                }
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
            if(AppService.service.przelaczajpogps == true) {
                zaczytajOpcje(true, gpsx, gpsy);
            } else {
                zaczytajOpcje(false, 0, 0);
            }
            odswiezUI();
        } else {
            MainActivity.activity.pokazToast(Komunikaty.BRAKATLASOW);
            zaczytajOpcje(false, 0, 0);
        }
    }


    public void pomniejszMape() {
        if(atlas == null) {
            MainActivity.activity.pokazToast(Komunikaty.BRAKATLASOW);
        } else {
            if(zoom > 10) {
                zoom = zoom - 5;
                rysujwatek.przeladujkonfiguracje = true;
                rysujwatek.odswiez = true;
            } else {
                int index = atlas.parserytmi.indexOf(tmiparser);
                if (index == 0) {
                    MainActivity.activity.pokazToast(Komunikaty.BLADODDALANIA);
                } else {
                    float gpsx = tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x);
                    float gpsy = tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y);
                    tmiparser = atlas.parserytmi.get(index - 1);
                    pixelnamapienadsrodkiem.set(tmiparser.obliczPixelXDlaWspolrzednej(gpsx), tmiparser.obliczPixelYDlaWspolrzednej(gpsy));
                    poprawPixelNadSrodkiem();
                    wczytajwatek.przeladujkonfiguracje = true;
                    rysujwatek.przeladujkonfiguracje = true;
                    rysujwatek.odswiez = true;
                }
            }
        }
        odswiezUI();
    }

    private boolean sprawdzCacheMerkatora() {
        if(listaplikowaplikacji.contains(Stale.SUFFIXCACHEMERKATOR)) {
            return true;
        } else {
            return false;
        }
    }

    private void zapiszPlikMerkatoraDoCache(long[] plikmerkatora) {
        ByteBuffer bajtytablicy = ByteBuffer.allocate(8 * plikmerkatora.length);
        LongBuffer longbajtytablicy = bajtytablicy.asLongBuffer();
        longbajtytablicy.put(plikmerkatora);
        try {
            FileOutputStream plikcachetab = MainActivity.activity.openFileOutput(Stale.SUFFIXCACHEMERKATOR, Context.MODE_PRIVATE);
            plikcachetab.write(bajtytablicy.array(), 0, 8 * plikmerkatora.length);
            plikcachetab.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parsujPlikMerkatora() {
        plikmerkatora = new long[900002];
        InputStream raw = getResources().openRawResource(R.raw.merkator5);
        BufferedReader r = new BufferedReader(new InputStreamReader(raw));
        String line;
        int i = 0;
        try {
            while ((line = r.readLine()) != null) {
                String[] lines = line.split(",");
                plikmerkatora[i] = Long.valueOf(lines[1]);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        zapiszPlikMerkatoraDoCache(plikmerkatora);
    }

    private void wczytajCacheMerkatora() {
        plikmerkatora = new long[900002];
        try {
            FileInputStream plikcachetab = MainActivity.activity.openFileInput(Stale.SUFFIXCACHEMERKATOR);
            ByteBuffer bajtytablicy = ByteBuffer.allocate(8 * plikmerkatora.length);
            Rozne.odczytajZPliku(plikcachetab, 8 * plikmerkatora.length, bajtytablicy.array());
            plikcachetab.close();
            LongBuffer longbajtytablicy = bajtytablicy.asLongBuffer();
            longbajtytablicy.get(plikmerkatora, 0, plikmerkatora.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void wczytajPlikMerkatora() {
        if(sprawdzCacheMerkatora() == false) {
            parsujPlikMerkatora();
        } else {
            wczytajCacheMerkatora();
        }
    }

    public void powiekszMape() {
        if(atlas == null) {
            MainActivity.activity.pokazToast(Komunikaty.BRAKATLASOW);
        } else {
            int index = atlas.parserytmi.indexOf(tmiparser);
            if(index == atlas.parserytmi.size() - 1) {
                if(zoom < 80) {
                    zoom = zoom + 5;
                    rysujwatek.przeladujkonfiguracje = true;
                    rysujwatek.odswiez = true;
                } else {
                    MainActivity.activity.pokazToast(Komunikaty.BLADPRZYBLIZANIA);
                }
            } else {
                float gpsx = tmiparser.obliczWspolrzednaXDlaPixela(pixelnamapienadsrodkiem.x);
                float gpsy = tmiparser.obliczWspolrzednaYDlaPixela(pixelnamapienadsrodkiem.y);
                tmiparser = atlas.parserytmi.get(index + 1);
                pixelnamapienadsrodkiem.set(tmiparser.obliczPixelXDlaWspolrzednej(gpsx), tmiparser.obliczPixelYDlaWspolrzednej(gpsy));
                poprawPixelNadSrodkiem();
                wczytajwatek.przeladujkonfiguracje = true;
                rysujwatek.przeladujkonfiguracje = true;
                rysujwatek.odswiez = true;
            }
        }
        odswiezUI();
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

    //Konczymy watek notyfikacji
    private void zakonczNotyfikacjaWatek() {
        if(notyfikacjawatek != null) {
            notyfikacjawatek.zakoncz = true;
            zakonczWatek(notyfikacjawatek);
        }
    }

    //Konczymy watek internet wyslij
    private void zakonczInternetWyslijWatek() {
        if(internetwyslijwatek != null) {
            internetwyslijwatek.zakoncz = true;
            zakonczWatek(internetwyslijwatek);
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
        zakonczNotyfikacjaWatek();
        zakonczInternetWyslijWatek();
        zakonczWatekGPS();
    }

    public void zakonczUsluge() {
        try {
            GPXPunktLogger.zakonczPlik();
            if (obecnatrasa != null) {
                obecnatrasa.zakonczPlik();
                obecnatrasa = null;
            }
            wystartowany = false;
            widok = Stale.WIDOKBRAK;
            wyrejestrujGPS();
            zakonczWatki();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
