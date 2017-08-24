package trzcina.maplas6.watki;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.util.Log;

import java.util.List;

import trzcina.maplas6.AppService;
import trzcina.maplas6.atlasy.Atlas;
import trzcina.maplas6.atlasy.TmiParser;
import trzcina.maplas6.lokalizacja.GPXPunktLogger;
import trzcina.maplas6.lokalizacja.GPXTrasaLogger;
import trzcina.maplas6.lokalizacja.PlikiGPX;
import trzcina.maplas6.lokalizacja.PunktNaMapie;
import trzcina.maplas6.lokalizacja.PunktWTrasie;
import trzcina.maplas6.pomoc.Bitmapy;
import trzcina.maplas6.pomoc.Painty;
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.pomoc.Stale;

import static android.content.Context.APP_OPS_SERVICE;
import static trzcina.maplas6.MainActivity.activity;

@SuppressWarnings("PointlessBooleanExpression")
public class RysujWatek extends Thread {

    public volatile boolean zakoncz;        //Info czy zakonczyc watek
    public volatile boolean odswiez;        //Info czy odswiezyc obraz
    public volatile boolean przeladujkonfiguracje;
    public Atlas atlas;
    public TmiParser tmiparser;
    private float density;

    String[] opisykol = {"20m", "50m", "100m", "200m", "500m", "1km", "2km", "5km", "10km", "20km", "50km", "100km", "200km"};
    int[] metrykol = {20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000};
    boolean[] rysujkola;
    double[] promieniekol;

    public RysujWatek() {
        zakoncz = false;
        odswiez = true;
        przeladujkonfiguracje = false;
        atlas = null;
        density = activity.getResources().getDisplayMetrics().density;
        rysujkola = new boolean[13];
        promieniekol = new double[13];
    }

    private void przeliczKola() {
        for(int kolo = 0; kolo <= 12; kolo++) {
            promieniekol[kolo] = ((double)metrykol[kolo] / (double)tmiparser.rozpietoscxwmetrach) * (double)tmiparser.rozmiarmapy.x;
            if((promieniekol[kolo] >= 25 * Painty.density) && (promieniekol[kolo] <= Math.max(AppService.service.srodekekranu.x, AppService.service.srodekekranu.y) * 1.1)) {
                rysujkola[kolo] = true;
            } else {
                rysujkola[kolo] = false;
            }
        }
    }

    public void przeladujKonfiguracje() {
        przeladujkonfiguracje = false;
        atlas = AppService.service.atlas;
        odswiez = true;
        tmiparser = null;
        if(atlas != null) {
            tmiparser = AppService.service.tmiparser;
            przeliczKola();
        }
    }

    //Uwalniamy sufrace w razie bledu
    private void zwolnijCanvas(Canvas canvas) {
        if(canvas != null) {
            try {
                activity.surface.surfaceholder.unlockCanvasAndPost(canvas);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Pobieramy surface
    private Canvas pobierzCanvas() {
        try {
            Canvas canvas = activity.surface.surfaceholder.lockCanvas();
            return canvas;
        } catch (Exception e) {
            return null;
        }
    }

    //Rysujemy czarne tlo
    private void rysujTlo(Canvas canvas) {
        canvas.drawColor(Color.GRAY);
    }

    //Rysujemy srodkowe kolo
    private void rysujKola(Canvas canvas) {
        canvas.drawCircle(AppService.service.srodekekranu.x, AppService.service.srodekekranu.y, Stale.SZEROKOSCSRODKOWEGOKOLA * density, Painty.paintczerwonysrodek);
        for (int kolo = 0; kolo <= 12; kolo++) {
            if (rysujkola[kolo]) {
                canvas.drawCircle(AppService.service.srodekekranu.x, AppService.service.srodekekranu.y, (float) promieniekol[kolo], Painty.paintczerwoneokregi);
                if(AppService.service.kolorinfo == 3) {
                    Rect zarys = new Rect();
                    Painty.painttekst[AppService.service.kolorinfo].getTextBounds(opisykol[kolo], 0, opisykol[kolo].length(), zarys);
                    rysujProstokat(canvas, (float)AppService.service.srodekekranu.x - 20 - 3, (float) (AppService.service.srodekekranu.y + promieniekol[kolo] - 9) - zarys.height() - 4, zarys.width() + 11, zarys.height() + 10, Painty.paintczarnyprostokat);
                }
                canvas.drawText(opisykol[kolo], AppService.service.srodekekranu.x - 20, (float) (AppService.service.srodekekranu.y + promieniekol[kolo] - 9), Painty.painttekst[AppService.service.kolorinfo]);
            }
        }
    }

    private void rysujMape(Canvas canvas) {
        try {
            Point srodekekranu = AppService.service.srodekekranu;
            Point centralnykafel = new Point(AppService.service.wczytajwatek.wyznaczIndexBitmapyX(AppService.service.pixelnamapienadsrodkiem.x), AppService.service.wczytajwatek.wyznaczIndexBitmapyY(AppService.service.pixelnamapienadsrodkiem.y));
            int odlegloscodlewejkrawedzikafla = AppService.service.pixelnamapienadsrodkiem.x - centralnykafel.x * tmiparser.rozmiarkafla.x;
            int odlegloscodgornejkrawedzikafla = AppService.service.pixelnamapienadsrodkiem.y - centralnykafel.y * tmiparser.rozmiarkafla.y;
            int startcentralnegokaflax = srodekekranu.x - odlegloscodlewejkrawedzikafla;
            int startcentralnegokaflay = srodekekranu.y - odlegloscodgornejkrawedzikafla;
            int ilosckafliwlewo = startcentralnegokaflax / tmiparser.rozmiarkafla.x + 1;
            int ilosckafliwgore = startcentralnegokaflay / tmiparser.rozmiarkafla.y + 1;
            for(int i = -ilosckafliwlewo; i <= ilosckafliwlewo + 1; i++) {
                for(int j = -ilosckafliwgore; j <= ilosckafliwgore + 1; j++) {
                    if(AppService.service.wczytajwatek.czyBitmapaWczytana(centralnykafel.x + i, centralnykafel.y + j)) {
                        canvas.drawBitmap(AppService.service.wczytajwatek.bitmapy[centralnykafel.x + i][centralnykafel.y + j], startcentralnegokaflax + i * tmiparser.rozmiarkafla.x, startcentralnegokaflay + j * tmiparser.rozmiarkafla.y, null);
                    } else {
                        odswiez = true;
                    }
                }
            }
        } catch (Exception e) {
            zwolnijCanvas(canvas);
            odswiez = true;
        }
    }

    private void rysujTrasyGPX(Canvas canvas, Point pixelnadsrodkiem) {
        if(AppService.service.poziominfo >= Stale.OPISYPUNKTY) {
            float promienpunktu = 4 * Painty.density;
            for (int i = 0; i < PlikiGPX.pliki.size(); i++) {
                if (PlikiGPX.pliki.get(i).zaznaczony) {
                    List<PunktWTrasie> lista = PlikiGPX.pliki.get(i).trasa;
                    int popy = 0;
                    int popx = 0;
                    PunktWTrasie poprzedni = null;
                    for (int j = 0; j < lista.size(); j++) {
                        PunktWTrasie punkt = lista.get(j);
                        int x = tmiparser.obliczPixelXDlaWspolrzednej(punkt.wspx);
                        int y = tmiparser.obliczPixelYDlaWspolrzednej(punkt.wspy);
                        if ((j == 0) || (j == lista.size() - 1)) {
                            canvas.drawCircle(AppService.service.srodekekranu.x + x - pixelnadsrodkiem.x, AppService.service.srodekekranu.y + y - pixelnadsrodkiem.y, promienpunktu, Painty.paintzielonyokragtrasa);
                        }
                        if (poprzedni != null) {
                            canvas.drawLine(AppService.service.srodekekranu.x + x - pixelnadsrodkiem.x, AppService.service.srodekekranu.y + y - pixelnadsrodkiem.y, AppService.service.srodekekranu.x + popx - pixelnadsrodkiem.x, AppService.service.srodekekranu.y + popy - pixelnadsrodkiem.y, Painty.paintzielonyokragtrasa);
                        }
                        popy = y;
                        popx = x;
                        poprzedni = punkt;
                    }
                }
            }
        }
    }

    private void rysujTraseObecna(Canvas canvas, Point pixelnadsrodkiem) {
        if(AppService.service.poziominfo >= Stale.OPISYPUNKTY) {
            float promienpunktu = 4 * Painty.density;
            GPXTrasaLogger obecnatrasa = AppService.service.obecnatrasa;
            if(obecnatrasa != null) {
                int dlugosc = obecnatrasa.dlugosc;
                int popy = 0;
                int popx = 0;
                PunktWTrasie poprzedni = null;
                for (int j = 0; j < dlugosc; j++) {
                    PunktWTrasie punkt = obecnatrasa.lista[j];
                    int x = tmiparser.obliczPixelXDlaWspolrzednej(punkt.wspx);
                    int y = tmiparser.obliczPixelYDlaWspolrzednej(punkt.wspy);
                    if ((j == 0) || (j == dlugosc - 1)) {
                        canvas.drawCircle(AppService.service.srodekekranu.x + x - pixelnadsrodkiem.x, AppService.service.srodekekranu.y + y - pixelnadsrodkiem.y, promienpunktu, Painty.paintfioletowyokragtrasa);
                    }
                    if (poprzedni != null) {
                        canvas.drawLine(AppService.service.srodekekranu.x + x - pixelnadsrodkiem.x, AppService.service.srodekekranu.y + y - pixelnadsrodkiem.y, AppService.service.srodekekranu.x + popx - pixelnadsrodkiem.x, AppService.service.srodekekranu.y + popy - pixelnadsrodkiem.y, Painty.paintfioletowyokragtrasa);
                    }
                    popy = y;
                    popx = x;
                    poprzedni = punkt;
                }
            }
        }
    }

    private void rysujProstokat(Canvas canvas, Float left, Float top, int width, int height, Paint paint) {
        canvas.drawRect(left, top, left + width, top + height, paint);
    }

    private void rysujPunktNaMapie(PunktNaMapie punkt, Canvas canvas, Paint paint, Point pixelnadsrodkiem, float promienpunktu) {
        int x = tmiparser.obliczPixelXDlaWspolrzednej(punkt.wspx);
        int y = tmiparser.obliczPixelYDlaWspolrzednej(punkt.wspy);
        canvas.drawCircle(AppService.service.srodekekranu.x + x - pixelnadsrodkiem.x, AppService.service.srodekekranu.y + y - pixelnadsrodkiem.y, promienpunktu, paint);
        if(AppService.service.poziominfo >= Stale.OPISYNAZWY) {
            if(punkt.nazwa != null) {
                Rect zarys = new Rect();
                Painty.painttekst[AppService.service.kolorinfo].getTextBounds(punkt.nazwa, 0, punkt.nazwa.length(), zarys);
                if(AppService.service.kolorinfo == 3) {
                    rysujProstokat(canvas, (float) (AppService.service.srodekekranu.x  + x - pixelnadsrodkiem.x - zarys.width() / 2 - 3 + zarys.left), AppService.service.srodekekranu.y + y - pixelnadsrodkiem.y - promienpunktu - 8 - zarys.height() - 3, zarys.width() + 6, zarys.height() + 6, Painty.paintczarnyprostokat);
                }
                canvas.drawText(punkt.nazwa, AppService.service.srodekekranu.x + x - pixelnadsrodkiem.x - zarys.width() / 2, AppService.service.srodekekranu.y + y - pixelnadsrodkiem.y - promienpunktu - 8, Painty.painttekst[AppService.service.kolorinfo]);
            }
            if(AppService.service.poziominfo >= Stale.OPISYKOMENTARZE) {
                if(punkt.opis != null) {
                    Rect zarys = new Rect();
                    Painty.painttekst[AppService.service.kolorinfo].getTextBounds(punkt.opis, 0, punkt.opis.length(), zarys);
                    if(AppService.service.kolorinfo == 3) {
                        rysujProstokat(canvas, (float) (AppService.service.srodekekranu.x  + x - pixelnadsrodkiem.x - zarys.width() / 2 - 3 + zarys.left), AppService.service.srodekekranu.y + y - pixelnadsrodkiem.y + promienpunktu + 6 - 3, zarys.width() + 6, zarys.height() + 6, Painty.paintczarnyprostokat);
                    }
                    canvas.drawText(punkt.opis, AppService.service.srodekekranu.x + x - pixelnadsrodkiem.x - zarys.width() / 2, AppService.service.srodekekranu.y + y - pixelnadsrodkiem.y + promienpunktu + 6 - zarys.top, Painty.painttekst[AppService.service.kolorinfo]);
                }
            }
        }
    }

    private void rysujPunktyObecne(Canvas canvas, Point pixelnadsrodkiem) {
        float promienpunktu = 4 * Painty.density;
        for(int i = 0; i < GPXPunktLogger.lista.size(); i++) {
            if(AppService.service.poziominfo >= Stale.OPISYPUNKTY) {
                rysujPunktNaMapie(GPXPunktLogger.lista.get(i), canvas, Painty.paintfioletowyokrag, pixelnadsrodkiem, promienpunktu);
            }
        }
    }

    private void rysujPunktyGPX(Canvas canvas, Point pixelnadsrodkiem) {
        float promienpunktu = 4 * Painty.density;
        for(int i = 0; i < PlikiGPX.pliki.size(); i++) {
            if(PlikiGPX.pliki.get(i).zaznaczony) {
                if(AppService.service.poziominfo >= Stale.OPISYPUNKTY) {
                    List<PunktNaMapie> lista = PlikiGPX.pliki.get(i).punkty;
                    for (int j = 0; j < lista.size(); j++) {
                        rysujPunktNaMapie(lista.get(j), canvas, Painty.paintzielonyokrag, pixelnadsrodkiem, promienpunktu);
                    }
                }
            }
        }
    }

    //Rysujemy kompas na srodku
    private void rysujKompas(Canvas canvas) {
        int katpolozenia = AppService.service.kompaswatek.kat;
        Matrix macierzobrotu = new Matrix();
        macierzobrotu.setRotate(katpolozenia, Bitmapy.strzalka.getWidth() / 2, Bitmapy.strzalka.getHeight() / 2);
        macierzobrotu.postTranslate(AppService.service.srodekekranu.x - Bitmapy.strzalka.getWidth() / 2, AppService.service.srodekekranu.y - Bitmapy.strzalka.getHeight() / 2);
        canvas.drawBitmap(Bitmapy.strzalka, macierzobrotu, null);
        Rect zarys = new Rect();
        String dokladnosckompasu = String.valueOf(AppService.service.kompaswatek.dokladnosc);
        Painty.painttekst[AppService.service.kolorinfo].getTextBounds(dokladnosckompasu, 0, dokladnosckompasu.length(), zarys);
        int poziom = AppService.service.srodekekranu.x;
        int pion = AppService.service.srodekekranu.y - zarys.height() - 10;
        if(katpolozenia <= 180) {
            poziom = poziom - 15 - zarys.width() - 5;
        } else {
            poziom = poziom + 15;
        }
        if(AppService.service.kolorinfo == 3) {
            rysujProstokat(canvas, (float)poziom - 5, (float)pion - zarys.height() - 5, zarys.width() + 11, zarys.height() + 11, Painty.paintczarnyprostokat);
        }
        canvas.drawText(dokladnosckompasu, poziom, pion, Painty.painttekst[AppService.service.kolorinfo]);
    }

    private void rysujBlad(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(Bitmapy.brakmapy, AppService.service.srodekekranu.x - Bitmapy.brakmapy.getWidth() / 2, AppService.service.srodekekranu.y - Bitmapy.brakmapy.getHeight() / 2, null);
    }

    private void rysujKursorGPS(Canvas canvas, Point pixelnadsrodkiem) {
        Location lokalizacja = AppService.service.czyJestFix();
        if(lokalizacja != null) {
            int x = tmiparser.obliczPixelXDlaWspolrzednej((float) lokalizacja.getLongitude());
            int y = tmiparser.obliczPixelYDlaWspolrzednej((float) lokalizacja.getLatitude());
            canvas.drawBitmap(Bitmapy.kursorgps, AppService.service.srodekekranu.x + x - pixelnadsrodkiem.x - Bitmapy.kursorgps.getWidth() / 2, AppService.service.srodekekranu.y +y - pixelnadsrodkiem.y - Bitmapy.kursorgps.getHeight() / 2, null);
        }
    }

    //Rysujemy zawartosc ekranu
    private void odswiezEkran() {
        Canvas canvas = null;
        try {
            canvas = pobierzCanvas();
            if(canvas != null) {
                if(atlas != null) {
                    Point pixelnadsrodkiem = new Point(AppService.service.pixelnamapienadsrodkiem);
                    rysujTlo(canvas);
                    rysujMape(canvas);
                    rysujTrasyGPX(canvas, pixelnadsrodkiem);
                    rysujPunktyGPX(canvas, pixelnadsrodkiem);
                    rysujKursorGPS(canvas, pixelnadsrodkiem);
                    rysujKola(canvas);
                    rysujTraseObecna(canvas, pixelnadsrodkiem);
                    rysujPunktyObecne(canvas, pixelnadsrodkiem);
                    rysujKompas(canvas);
                    zwolnijCanvas(canvas);
                } else {
                    rysujBlad(canvas);
                    zwolnijCanvas(canvas);
                }
            } else {
                odswiez = true;
            }
        } catch (Exception e) {

            //Jesli jakis blad to odswiez natychmiast
            odswiez = true;
            zwolnijCanvas(canvas);
            e.printStackTrace();
        }
    }

    //Glowna petla watku
    public void run() {
        while(zakoncz == false) {

            //Gdy mamy przeladowac atlas
            if(przeladujkonfiguracje == true) {
                przeladujKonfiguracje();
            }

            //Gdy mamy cos odswiezyc
            if(odswiez == true) {
                if (activity.surface.surfaceholder.getSurface().isValid()) {
                    odswiez = false;
                    odswiezEkran();
                }
            }

            //jesli nie mamy nic rysowac, krotka przerwa
            if(odswiez == false) {
                Rozne.czekaj(5);
            }
        }
    }

}
