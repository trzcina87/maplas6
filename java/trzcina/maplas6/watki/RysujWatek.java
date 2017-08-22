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

    public volatile boolean zakoncz;
    public volatile boolean odswiez;
    private float density;

    public RysujWatek() {
        zakoncz = false;
        odswiez = true;
        density = MainActivity.activity.getResources().getDisplayMetrics().density;
    }

    private void zwolnijCanvas(Canvas canvas) {
        if(canvas != null) {
            try {
                MainActivity.activity.surface.surfaceholder.unlockCanvasAndPost(canvas);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Canvas pobierzCanvas() {
        try {
            Canvas canvas = MainActivity.activity.surface.surfaceholder.lockCanvas();
            return canvas;
        } catch (Exception e) {
            return null;
        }
    }

    private void rysujTlo(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
    }

    private void rysujKola(Canvas canvas) {
        canvas.drawCircle(AppService.service.srodekekranu.x, AppService.service.srodekekranu.y, Stale.SZEROKOSCSRODKOWEGOKOLA * density, Painty.paintczerwonysrodek);
    }

    private void rysujKompas(Canvas canvas) {
        int katpolozenia = AppService.service.kompaswatek.kat;
        Matrix macierzobrotu = new Matrix();
        macierzobrotu.setRotate(katpolozenia, Bitmapy.strzalka.getWidth() / 2, Bitmapy.strzalka.getHeight() / 2);
        macierzobrotu.postTranslate(AppService.service.srodekekranu.x - Bitmapy.strzalka.getWidth() / 2, AppService.service.srodekekranu.y - Bitmapy.strzalka.getHeight() / 2);
        canvas.drawBitmap(Bitmapy.strzalka, macierzobrotu, null);
        //canvas.drawText(String.valueOf(activity.kompaswatek.dokladnosc), activity.rozdzielczosc.x + Bitmapy.strzalka.getWidth() / 2 + 5, activity.rozdzielczosc.y, czerwonytekst);
        //activity.kompaswatek.ostatninarysowanykatpolozenia = katpolozenia;
    }

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
            odswiez = true;
            zwolnijCanvas(canvas);
        }
    }

    public void run() {
        while(zakoncz == false) {
            if(odswiez == true) {
                if (MainActivity.activity.surface.surfaceholder.getSurface().isValid()) {
                    odswiez = false;
                    odswiezEkran();
                }
            }
            if(odswiez == false) {
                Rozne.czekaj(5);
            }
        }
    }

}
