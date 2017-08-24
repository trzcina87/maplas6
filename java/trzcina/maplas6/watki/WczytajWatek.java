package trzcina.maplas6.watki;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import java.io.IOException;
import java.io.RandomAccessFile;

import trzcina.maplas6.AppService;
import trzcina.maplas6.atlasy.Atlas;
import trzcina.maplas6.atlasy.TmiParser;
import trzcina.maplas6.pomoc.Rozne;

@SuppressWarnings("PointlessBooleanExpression")
public class WczytajWatek extends Thread {

    public volatile boolean zakoncz;
    public volatile boolean przeladujkonfiguracje;
    private Atlas atlas;
    public TmiParser tmiparser;
    public Bitmap[][] bitmapy;
    private RandomAccessFile pliktar;
    private int ilosckafli;
    private Point ostatnicentralnykafel;
    public volatile boolean odswiez;

    public WczytajWatek() {
        zakoncz = false;
        przeladujkonfiguracje = false;
        atlas = null;
        bitmapy = null;
        tmiparser = null;
        pliktar = null;
        ilosckafli = 0;
        odswiez = false;
        ostatnicentralnykafel = new Point(-1000000, -1000000);
    }

    private void zamknijPlik() {
        if(pliktar != null) {
            try {
                pliktar.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pliktar = null;
    }

    private void przeladujKonfiguracje() {
        przeladujkonfiguracje = false;
        atlas = AppService.service.atlas;
        bitmapy = null;
        tmiparser = null;
        ilosckafli = 0;
        ostatnicentralnykafel.set(-1000000, -1000000);
        odswiez = false;
        zamknijPlik();
        if(atlas != null) {
            try {
                tmiparser = AppService.service.tmiparser;
                pliktar = new RandomAccessFile(tmiparser.sciezkatar, "r");
                bitmapy = new Bitmap[tmiparser.ilosckafli.x][tmiparser.ilosckafli.y];
            } catch (Exception e) {
                atlas = null;
                bitmapy = null;
                tmiparser = null;
                pliktar = null;
                odswiez = false;
                e.printStackTrace();
            }
        }
    }

    public boolean czyBitmapaWczytana(Point wspolrzedne) {
        if ((wspolrzedne.x < 0) || (wspolrzedne.x >= tmiparser.ilosckafli.x)) {
            return false;
        }
        if ((wspolrzedne.y < 0) || (wspolrzedne.y >= tmiparser.ilosckafli.y)) {
            return false;
        }
        if (bitmapy[wspolrzedne.x][wspolrzedne.y] == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean czyBitmapaWczytana(int x, int y) {
        if ((x < 0) || (x >= tmiparser.ilosckafli.x)) {
            return false;
        }
        if ((y < 0) || (y >= tmiparser.ilosckafli.y)) {
            return false;
        }
        if (bitmapy[x][y] == null) {
            return false;
        } else {
            return true;
        }
    }

    private void wczytajBitmape(Point wspolrzedne) {
        if ((wspolrzedne.x < 0) || (wspolrzedne.x >= tmiparser.ilosckafli.x)) {
            return;
        }
        if ((wspolrzedne.y < 0) || (wspolrzedne.y >= tmiparser.ilosckafli.y)) {
            return;
        }
        try {
            long startwtar = (long)tmiparser.startkafla[wspolrzedne.x][wspolrzedne.y] * (long)512 + (long)512;
            int dlugoscwtar = tmiparser.dlugosckafla[wspolrzedne.x][wspolrzedne.y] * 512;
            byte[] bajtyplikugraficznego = Rozne.odczytajPlikRAM(pliktar, startwtar, dlugoscwtar);
            bitmapy[wspolrzedne.x][wspolrzedne.y] = BitmapFactory.decodeByteArray(bajtyplikugraficznego, 0, bajtyplikugraficznego.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void wczytajBitmape(int x, int y) {
        if ((x < 0) || (x >= tmiparser.ilosckafli.x)) {
            return;
        }
        if ((y < 0) || (y >= tmiparser.ilosckafli.y)) {
            return;
        }
        try {
            long startwtar = (long)tmiparser.startkafla[x][y] * (long)512 + (long)512;
            int dlugoscwtar = tmiparser.dlugosckafla[x][y] * 512;
            byte[] bajtyplikugraficznego = Rozne.odczytajPlikRAM(pliktar, startwtar, dlugoscwtar);
            bitmapy[x][y] = BitmapFactory.decodeByteArray(bajtyplikugraficznego, 0, bajtyplikugraficznego.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int wyznaczIndexBitmapyX(int x) {
        return x / tmiparser.rozmiarkafla.x;
    }

    public int wyznaczIndexBitmapyY(int y) {
        return y / tmiparser.rozmiarkafla.y;
    }

    private int policzWczytaneBitmapy() {
        int ilosc = 0;
        for(int i = 0; i < tmiparser.ilosckafli.x; i++) {
            for(int j = 0; j < tmiparser.ilosckafli.y; j++) {
                if(bitmapy[i][j] != null) {
                    ilosc = ilosc + 1;
                }
            }
        }
        return ilosc;
    }

    private void puscBitmape(int x, int y) {
        if (czyBitmapaWczytana(x, y)) {
            bitmapy[x][y].recycle();
            bitmapy[x][y] = null;
        }
    }

    private void czyscBitmapy(Point centralnykafel, int promienx, int promieny) {
        int iloscwczytanych = policzWczytaneBitmapy();
        if(przeladujkonfiguracje) {
            return;
        }
        if(iloscwczytanych > 2 * ilosckafli) {
            for(int i = 0; i < centralnykafel.x - promienx; i++) {
                for(int j = 0; j < tmiparser.ilosckafli.y; j++) {
                    puscBitmape(i, j);
                }
            }
            if(przeladujkonfiguracje) {
                return;
            }
            for(int i = centralnykafel.x + promienx + 1; i < tmiparser.ilosckafli.x; i++) {
                for(int j = 0; j < tmiparser.ilosckafli.y; j++) {
                    puscBitmape(i, j);
                }
            }
            if(przeladujkonfiguracje) {
                return;
            }
            for(int i = 0; i < centralnykafel.y - promieny; i++) {
                for(int j = 0; j < tmiparser.ilosckafli.x; j++) {
                    puscBitmape(j, i);
                }
            }
            if(przeladujkonfiguracje) {
                return;
            }
            for(int i = centralnykafel.y + promieny + 1; i < tmiparser.ilosckafli.y; i++) {
                for(int j = 0; j < tmiparser.ilosckafli.x; j++) {
                    puscBitmape(j, i);
                }
            }
        }
    }

    private boolean czyKafleRozne(Point kafel1, Point kafel2) {
        if((kafel1.x == kafel2.x) && (kafel1.y == kafel2.y)) {
            return false;
        } else {
            return true;
        }
    }

    private void sprawdzWczytanieBitmap() {
        Point centralnykafel = new Point(wyznaczIndexBitmapyX(AppService.service.pixelnamapienadsrodkiem.x), wyznaczIndexBitmapyY(AppService.service.pixelnamapienadsrodkiem.y));
        if((czyKafleRozne(centralnykafel, ostatnicentralnykafel)) || (odswiez == true)) {
            odswiez = false;
            Point promien = new Point(Math.round(AppService.service.srodekekranu.x / (float) tmiparser.rozmiarkafla.x * 2), Math.round(AppService.service.srodekekranu.y / (float) tmiparser.rozmiarkafla.y * 2));
            ilosckafli = (promien.x * 2) * (promien.y * 2);
            int wiekszypromien = Math.max(promien.x, promien.y);
            int mniejszypromienbok = 0;
            int mniejszypromiengora = 0;
            for (int obecnypromien = 0; obecnypromien <= wiekszypromien; obecnypromien++) {
                mniejszypromienbok = Math.min(obecnypromien, promien.x);
                mniejszypromiengora = Math.min(obecnypromien, promien.y);
                for (int przesunwbok = -mniejszypromienbok; przesunwbok <= mniejszypromienbok; przesunwbok++) {
                    for (int przesunwgore = -mniejszypromiengora; przesunwgore <= mniejszypromiengora; przesunwgore++) {
                        if(przeladujkonfiguracje) {
                            return;
                        }
                        int x = centralnykafel.x + przesunwbok;
                        int y = centralnykafel.y + przesunwgore;
                        if (czyBitmapaWczytana(x, y) == false) {
                            wczytajBitmape(x, y);
                        }
                    }
                }
            }
            czyscBitmapy(centralnykafel, mniejszypromienbok, mniejszypromiengora);
            ostatnicentralnykafel.set(centralnykafel.x, centralnykafel.y);
        }
    }

    public void run() {
        while(zakoncz == false) {
            if(przeladujkonfiguracje == true) {
                przeladujKonfiguracje();
            }
            if(atlas != null) {
                sprawdzWczytanieBitmap();
            }
            Rozne.czekaj(5);
        }
    }
}
