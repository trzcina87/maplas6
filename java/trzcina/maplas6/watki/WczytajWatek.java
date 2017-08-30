package trzcina.maplas6.watki;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import trzcina.maplas6.AppService;
import trzcina.maplas6.MainActivity;
import trzcina.maplas6.R;
import trzcina.maplas6.atlasy.Atlas;
import trzcina.maplas6.atlasy.TmiParser;
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.ustawienia.Ustawienia;

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
    public int nasycenie;
    public int kontrast;
    public int[] kontrasttablica;

    public WczytajWatek() {
        zakoncz = false;
        przeladujkonfiguracje = false;
        atlas = null;
        bitmapy = null;
        tmiparser = null;
        pliktar = null;
        ilosckafli = 0;
        nasycenie = 0;
        kontrast = 0;
        odswiez = false;
        ostatnicentralnykafel = new Point(-1000000, -1000000);
        kontrasttablica = new int[256];
        for(int i = 0; i < 256; i++) {
            kontrasttablica[i] = 1;
        }
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
        nasycenie = Ustawienia.nasycenie.wartosc;
        kontrast = Ustawienia.kontrast.wartosc;
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
        if(kontrast != 0) {
            int kontrasttmp[] = new int[256];
            double contrastVal = kontrast * 2;
            double contrastVal2 = Math.pow((100 + contrastVal) / 100f, 2);
            for(int i = 0; i < 256; i++) {
                kontrasttmp[i] = (int)(((((i / 255.0) - 0.5) * contrastVal2) + 0.5) * 255.0);
                kontrasttmp[i] = truncate(kontrasttmp[i]);
            }
            kontrasttablica = kontrasttmp;

        }
    }

    private int truncate(int value) {
        if (value < 0) {
            return 0;
        } else if (value > 255) {
            return 255;
        }
        return value;
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

    private Bitmap dodajKontrast(Bitmap bitmapa) {
        /*Bitmap bit0 = BitmapFactory.decodeResource(MainActivity.activity.getResources(), R.mipmap.b140);
        Log.e("BUF", String.valueOf(bit0.getRowBytes() * bit0.getHeight()));
        ByteBuffer bufor0 = ByteBuffer.allocate(bit0.getRowBytes() * bit0.getHeight());
        bit0.copyPixelsToBuffer(bufor0);
        byte[] buf0 = bufor0.array();
        for(int i = 0; i < buf0.length; i++) {
            Log.e("BUF", String.valueOf(buf0[i]));
        }*/
        ByteBuffer bufor = ByteBuffer.allocateDirect(bitmapa.getRowBytes() * bitmapa.getHeight());
        bitmapa.copyPixelsToBuffer(bufor);
        byte[] buf = bufor.array();
        for(int i = 0; i < buf.length; i++) {
            if(buf[i] >= 0) {
                int nowawartosc = (kontrasttablica[(int) buf[i]]);
                if(nowawartosc >= 128) {
                    buf[i] = (byte) (-128 + (nowawartosc - 128));
                } else {
                    buf[i] = (byte)nowawartosc;
                }
            } else {
                int nowypixel = 255 + buf[i];
                int nowawartosc = (kontrasttablica[nowypixel]);
                if(nowawartosc >= 128) {
                    buf[i] = (byte) (-128 + (nowawartosc - 128));
                } else {
                    buf[i] = (byte)nowawartosc;
                }
            }
        }
        Bitmap nowa = Bitmap.createBitmap(bitmapa.getWidth(), bitmapa.getHeight(), bitmapa.getConfig());
        nowa.copyPixelsFromBuffer(ByteBuffer.wrap(buf));
        nowa.setHasAlpha(false);
        return nowa;
    }

    private Bitmap dodajNasycenie(Bitmap bitmapa) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation((1.0F/11.0F) * nasycenie + 1);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        Paint paint = new Paint();
        paint.setColorFilter(filter);
        Bitmap bitmapatmp = Bitmap.createBitmap(bitmapa.getWidth(), bitmapa.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(bitmapatmp).drawBitmap(bitmapa, 0, 0, paint);
        return bitmapatmp;
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
            if(kontrast != 0) {
                bitmapy[wspolrzedne.x][wspolrzedne.y] = dodajKontrast(bitmapy[wspolrzedne.x][wspolrzedne.y]);
            }
            if(nasycenie != 0) {
                bitmapy[wspolrzedne.x][wspolrzedne.y] = dodajNasycenie(bitmapy[wspolrzedne.x][wspolrzedne.y]);
            }
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
            if(kontrast != 0) {
                bitmapy[x][y] = dodajKontrast(bitmapy[x][y]);
            }
            if(nasycenie != 0) {
                bitmapy[x][y] = dodajNasycenie(bitmapy[x][y]);
            }
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
            if(MainActivity.activity.activitywidoczne == true) {
                if (przeladujkonfiguracje == true) {
                    przeladujKonfiguracje();
                }
                if (atlas != null) {
                    sprawdzWczytanieBitmap();
                }
                Rozne.czekaj(5);
            } else {
                Rozne.czekaj(20);
            }
        }
    }
}
