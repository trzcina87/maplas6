package trzcina.maplas6.watki;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.Log;

import java.util.List;

import trzcina.maplas6.AppService;
import trzcina.maplas6.MainActivity;
import trzcina.maplas6.atlasy.Atlas;
import trzcina.maplas6.atlasy.TmiParser;
import trzcina.maplas6.lokalizacja.PlikiGPX;
import trzcina.maplas6.lokalizacja.PunktNaMapie;
import trzcina.maplas6.lokalizacja.PunktWTrasie;
import trzcina.maplas6.pomoc.Bitmapy;
import trzcina.maplas6.pomoc.Painty;
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.pomoc.Stale;

@SuppressWarnings("PointlessBooleanExpression")
public class RysujWatek extends Thread {

    public volatile boolean zakoncz;        //Info czy zakonczyc watek
    public volatile boolean odswiez;        //Info czy odswiezyc obraz
    public volatile boolean przeladujkonfiguracje;
    public Atlas atlas;
    public TmiParser tmiparser;
    private float density;

    public RysujWatek() {
        zakoncz = false;
        odswiez = true;
        przeladujkonfiguracje = false;
        atlas = null;
        density = MainActivity.activity.getResources().getDisplayMetrics().density;
    }

    public void przeladujKonfiguracje() {
        przeladujkonfiguracje = false;
        atlas = AppService.service.atlas;
        odswiez = true;
        tmiparser = null;
        if(atlas != null) {
            tmiparser = AppService.service.tmiparser;
        }
    }

    //Uwalniamy sufrace w razie bledu
    private void zwolnijCanvas(Canvas canvas) {
        if(canvas != null) {
            try {
                MainActivity.activity.surface.surfaceholder.unlockCanvasAndPost(canvas);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Pobieramy surface
    private Canvas pobierzCanvas() {
        try {
            Canvas canvas = MainActivity.activity.surface.surfaceholder.lockCanvas();
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
        float promienpunktu = 4 * Painty.density;
        for(int i = 0; i < PlikiGPX.pliki.size(); i++) {
            if (PlikiGPX.pliki.get(i).zaznaczony) {
                List<PunktWTrasie> lista = PlikiGPX.pliki.get(i).trasa;
                int popy = 0;
                int popx = 0;
                PunktWTrasie poprzedni = null;
                for(int j = 0; j < lista.size(); j++) {
                    PunktWTrasie punkt = lista.get(j);
                    int x = tmiparser.obliczPixelXDlaWspolrzednej(punkt.wspx);
                    int y = tmiparser.obliczPixelYDlaWspolrzednej(punkt.wspy);
                    if((j == 0) || (j == lista.size() - 1)) {
                        canvas.drawCircle(AppService.service.srodekekranu.x + x - pixelnadsrodkiem.x, AppService.service.srodekekranu.y + y - pixelnadsrodkiem.y, promienpunktu, Painty.paintzielonyokragtrasa);
                    }
                    if(poprzedni != null) {
                        canvas.drawLine(AppService.service.srodekekranu.x + x - pixelnadsrodkiem.x, AppService.service.srodekekranu.y + y - pixelnadsrodkiem.y, AppService.service.srodekekranu.x + popx - pixelnadsrodkiem.x, AppService.service.srodekekranu.y + popy - pixelnadsrodkiem.y, Painty.paintzielonyokragtrasa);
                    }
                    popy = y;
                    popx = x;
                    poprzedni = punkt;
                }
            }
        }
    }

    private void rysujPunktyGPX(Canvas canvas, Point pixelnadsrodkiem) {
        float promienpunktu = 4 * Painty.density;
        for(int i = 0; i < PlikiGPX.pliki.size(); i++) {
            if(PlikiGPX.pliki.get(i).zaznaczony) {
                List<PunktNaMapie> lista = PlikiGPX.pliki.get(i).punkty;
                for (int j = 0; j < lista.size(); j++) {
                    PunktNaMapie punkt = lista.get(j);
                    int x = tmiparser.obliczPixelXDlaWspolrzednej(punkt.wspx);
                    int y = tmiparser.obliczPixelYDlaWspolrzednej(punkt.wspy);
                    canvas.drawCircle(AppService.service.srodekekranu.x + x - pixelnadsrodkiem.x, AppService.service.srodekekranu.y + y - pixelnadsrodkiem.y, promienpunktu, Painty.paintzielonyokrag);
                }
            }
        }
        /*for(int i = 0; i < punktyzgpx.size(); i++) {
            if(activity.opisypunktowgpx > 0) {
                if(activity.opisypunktowgpx > 1) {
                    if(! punktyzgpx.get(i).obliczoneszerokosci) {
                        if(punktyzgpx.get(i).nazwa != null) {
                            bialytekst.getTextBounds(punktyzgpx.get(i).nazwa, 0, punktyzgpx.get(i).nazwa.length(), punktyzgpx.get(i).canvasnazwa);
                        }
                        if(punktyzgpx.get(i).komentarz != null) {
                            bialytekst.getTextBounds(punktyzgpx.get(i).komentarz, 0, punktyzgpx.get(i).komentarz.length(), punktyzgpx.get(i).canvaskom);
                        }
                        punktyzgpx.get(i).obliczoneszerokosci = true;
                    }
                    if(punktyzgpx.get(i).nazwa != null) {
                        if(activity.kolorinfo == 3) {
                            rysujProstokat(canvas, (float)(activity.rozdzielczosc.x + zoomact * (punktyzgpx.get(i).xnamapie - tmp.x) - punktyzgpx.get(i).canvasnazwa.width() / 2 - 3) + punktyzgpx.get(i).canvasnazwa.left, (float)(activity.rozdzielczosc.y + zoomact * (punktyzgpx.get(i).ynamapie - tmp.y) - promienpunktu - 8 - punktyzgpx.get(i).canvasnazwa.height() - 3), punktyzgpx.get(i).canvasnazwa.width() + 6, punktyzgpx.get(i).canvasnazwa.height() + 6, czarnyprostokat);
                        }
                        canvas.drawText(punktyzgpx.get(i).nazwa, (float) (activity.rozdzielczosc.x + zoomact * (punktyzgpx.get(i).xnamapie - tmp.x) - punktyzgpx.get(i).canvasnazwa.width() / 2), (float) (activity.rozdzielczosc.y + zoomact * (punktyzgpx.get(i).ynamapie - tmp.y) - promienpunktu - 8), kolory[activity.kolorinfo]);
                    }
                    if(activity.opisypunktowgpx > 2) {
                        if(punktyzgpx.get(i).komentarz != null) {
                            if(activity.kolorinfo == 3) {
                                rysujProstokat(canvas, (float)(activity.rozdzielczosc.x + zoomact * (punktyzgpx.get(i).xnamapie - tmp.x) - punktyzgpx.get(i).canvaskom.width() / 2 - 3) + punktyzgpx.get(i).canvaskom.left, (float) (activity.rozdzielczosc.y + zoomact * (punktyzgpx.get(i).ynamapie - tmp.y) + promienpunktu + 6 + punktyzgpx.get(i).canvaskom.height() - punktyzgpx.get(i).canvaskom.height() - 3), punktyzgpx.get(i).canvaskom.width() + 6, punktyzgpx.get(i).canvaskom.height() + 6, czarnyprostokat);
                            }
                            canvas.drawText(punktyzgpx.get(i).komentarz, (float) (activity.rozdzielczosc.x + zoomact * (punktyzgpx.get(i).xnamapie - tmp.x) - punktyzgpx.get(i).canvaskom.width() / 2), (float) (activity.rozdzielczosc.y + zoomact * (punktyzgpx.get(i).ynamapie - tmp.y) + promienpunktu + 6 - punktyzgpx.get(i).canvaskom.top), kolory[activity.kolorinfo]);
                        }
                    }
                }
            }
        }*/
    }

    //Rysujemy kompas na srodku
    private void rysujKompas(Canvas canvas) {
        int katpolozenia = AppService.service.kompaswatek.kat;
        Matrix macierzobrotu = new Matrix();
        macierzobrotu.setRotate(katpolozenia, Bitmapy.strzalka.getWidth() / 2, Bitmapy.strzalka.getHeight() / 2);
        macierzobrotu.postTranslate(AppService.service.srodekekranu.x - Bitmapy.strzalka.getWidth() / 2, AppService.service.srodekekranu.y - Bitmapy.strzalka.getHeight() / 2);
        canvas.drawBitmap(Bitmapy.strzalka, macierzobrotu, null);
        //canvas.drawText(String.valueOf(activity.kompaswatek.dokladnosc), activity.rozdzielczosc.x + Bitmapy.strzalka.getWidth() / 2 + 5, activity.rozdzielczosc.y, czerwonytekst);
        //activity.kompaswatek.ostatninarysowanykatpolozenia = katpolozenia;
    }

    private void rysujBlad(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(Bitmapy.brakmapy, AppService.service.srodekekranu.x - Bitmapy.brakmapy.getWidth() / 2, AppService.service.srodekekranu.y - Bitmapy.brakmapy.getHeight() / 2, null);
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
                    rysujKola(canvas);
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
                if (MainActivity.activity.surface.surfaceholder.getSurface().isValid()) {
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
