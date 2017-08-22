package trzcina.maplas6.watki;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

import trzcina.maplas6.AppService;
import trzcina.maplas6.MainActivity;
import trzcina.maplas6.pomoc.Bitmapy;
import trzcina.maplas6.pomoc.Painty;
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.pomoc.Stale;

@SuppressWarnings("PointlessBooleanExpression")
public class RysujWatek extends Thread {

    public volatile boolean zakoncz;        //Info czy zakonczyc watek
    public volatile boolean odswiez;        //Info czy odswiezyc obraz
    private float density;

    public RysujWatek() {
        zakoncz = false;
        odswiez = true;
        density = MainActivity.activity.getResources().getDisplayMetrics().density;
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
        canvas.drawColor(Color.BLACK);
    }

    //Rysujemy srodkowe kolo
    private void rysujKola(Canvas canvas) {
        canvas.drawCircle(AppService.service.srodekekranu.x, AppService.service.srodekekranu.y, Stale.SZEROKOSCSRODKOWEGOKOLA * density, Painty.paintczerwonysrodek);
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

    //Rysujemy zawartosc ekranu
    private void odswiezEkran() {
        Canvas canvas = null;
        try {
            canvas = pobierzCanvas();
            if(canvas != null) {
                rysujTlo(canvas);
                rysujKola(canvas);
                rysujKompas(canvas);
                zwolnijCanvas(canvas);
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
