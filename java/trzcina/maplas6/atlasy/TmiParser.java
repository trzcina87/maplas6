package trzcina.maplas6.atlasy;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import trzcina.maplas6.MainActivity;
import trzcina.maplas6.pomoc.MD5;
import trzcina.maplas6.pomoc.Stale;

public class TmiParser {

    public String sciezka;
    public int mapstart;
    public int mapdlugosc;
    public int rozmiarx;
    public int rozmiary;
    public int ilosckaflix;
    public int ilosckafliy;
    public Point rozmiarmapy;
    public String rozszerzenie;
    public String prefix;
    public int startkafla[][];
    public int dlugosckafla[][];
    public String sciezkabezrozszerzenia;
    public String sciezkatar;
    public PointF gpsstart;
    public PointF gpskoniec;

    public TmiParser(String sciezka) {
        this.sciezka = sciezka;
        ustawSciezkiDodatkowe();
        mapstart = -1;
        mapdlugosc = -1;
        rozszerzenie = null;
        prefix = null;
        rozmiarx = -1;
        rozmiary = -1;
        ilosckaflix = -1;
        ilosckafliy = -1;
        startkafla = null;
        dlugosckafla = null;
        gpsstart = new PointF();
        gpskoniec = new PointF();
        rozmiarmapy = new Point();
    }

    private void znajdzRozszerzenie(String koniec) {
        if (koniec.endsWith(".jpg")) {
            rozszerzenie = ".jpg";
        }
        if (koniec.endsWith(".png")) {
            rozszerzenie = ".png";
        }
        if (koniec.endsWith(".bmp")) {
            rozszerzenie = ".bmp";
        }
        if (koniec.endsWith(".gif")) {
            rozszerzenie = ".gif";
        }
    }

    private void znajdzPrefix(String koniec) {
        String[] podzial = koniec.split("_");
        prefix = podzial[0] + "_";
    }

    private void znajdzRozszerzenieIPrefix(String koniec) {
        znajdzRozszerzenie(koniec);
        znajdzPrefix(koniec);
    }

    private boolean czyLiniaBitmapy(String koniec) {
        if((koniec.endsWith(".jpg")) || (koniec.endsWith(".png")) || (koniec.endsWith(".bmp")) || (koniec.endsWith(".gif"))) {
            return true;
        } else {
            return false;
        }
    }

    private boolean czyLiniaMap(String koniec) {
        if(koniec.endsWith(".map")) {
            return true;
        } else {
            return false;
        }
    }

    private BufferedReader otworzPlik() throws FileNotFoundException {
        InputStream inputstream = new FileInputStream(sciezka);
        InputStreamReader inputreader = new InputStreamReader(inputstream);
        return new BufferedReader(inputreader);
    }

    private void sortujListe(List<Integer> lista) {
        Collections.sort(lista, new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return integer.compareTo(t1);
            }
        });
    }

    private void szukajWielkosciKafla(List<Integer> listax, List<Integer> listay) {
        sortujListe(listax);
        sortujListe(listay);
        int startx = listax.get(0);
        int obecnyx = startx;
        int i = 0;
        while((i < listax.size()) && (obecnyx == startx)) {
            obecnyx = listax.get(i);
            i = i + 1;
        }
        if(obecnyx == startx) {
            rozmiarx = 1024;
        } else {
            rozmiarx = obecnyx;
        }
        int starty = listay.get(0);
        int obecnyy = starty;
        i = 0;
        while((i < listay.size()) && (obecnyy == starty)) {
            obecnyy = listay.get(i);
            i = i + 1;
        }
        if(obecnyy == starty) {
            rozmiarx = 1024;
        } else {
            rozmiary = obecnyy;
        }
        ilosckaflix = listax.get(listax.size() - 1) / rozmiarx + 1;
        ilosckafliy = listay.get(listay.size() - 1) / rozmiary + 1;
    }

    private void utworzTablice() {
        startkafla = new int[ilosckaflix][ilosckafliy];
        dlugosckafla = new int[ilosckaflix][ilosckafliy];
    }

    private void pierwszyPrzebieg(List<Integer> listax, List<Integer> listay) throws IOException {
        BufferedReader bufferedreader = otworzPlik();
        String przetwarzanalinia = bufferedreader.readLine();
        while(przetwarzanalinia != null) {
            String[] podzialdwukropek = przetwarzanalinia.split(":");
            if (podzialdwukropek.length == 2) {
                String p1trim = podzialdwukropek[1].trim();
                if(rozszerzenie == null) {
                    if(czyLiniaBitmapy(p1trim) == true) {
                        znajdzRozszerzenieIPrefix(p1trim);
                    }
                }
                if(czyLiniaBitmapy(p1trim)) {
                    String usunieterozszerzenie = p1trim.replace(rozszerzenie, "");
                    String[] podzial = usunieterozszerzenie.split("_");
                    listax.add(Integer.parseInt(podzial[podzial.length - 2]));
                    listay.add(Integer.parseInt(podzial[podzial.length - 1]));
                }
            }
            przetwarzanalinia = bufferedreader.readLine();
        }
    }

    private void drugiPrzebieg() throws IOException {
        BufferedReader bufferedreader = otworzPlik();
        String przetwarzanalinia = bufferedreader.readLine();
        int poprzednix = -1;
        int poprzedniy = -1;
        while(przetwarzanalinia != null) {
            String[] podzialdwukropek = przetwarzanalinia.split(":");
            if (podzialdwukropek.length == 2) {
                String p0trim = podzialdwukropek[0].toLowerCase().replace("block", "").trim();
                if((poprzednix != -1) && (poprzedniy != -1)) {
                    if((poprzednix == -2) && (poprzedniy == -2)) {
                        mapdlugosc = Integer.parseInt(p0trim) - mapstart;
                    } else {
                        dlugosckafla[poprzednix / rozmiarx][poprzedniy / rozmiary] = Integer.parseInt(p0trim) - startkafla[poprzednix / rozmiarx][poprzedniy / rozmiary];
                    }
                    poprzednix = -1;
                    poprzedniy = -1;
                }
                String p1trim = podzialdwukropek[1].trim();
                if(czyLiniaBitmapy(p1trim)) {
                    String usunieterozszerzenie = p1trim.replace(rozszerzenie, "");
                    String[] podzial = usunieterozszerzenie.split("_");
                    int x = Integer.parseInt(podzial[podzial.length - 2]);
                    int y = Integer.parseInt(podzial[podzial.length - 1]);
                    startkafla[x / rozmiarx][y / rozmiary] = Integer.parseInt(p0trim);
                    poprzednix = x;
                    poprzedniy = y;
                }
                if(czyLiniaMap(p1trim)) {
                    mapstart = Integer.parseInt(p0trim);
                    poprzednix = -2;
                    poprzedniy = -2;
                }
            }
            przetwarzanalinia = bufferedreader.readLine();
        }
    }

    private boolean sprawdzCache() {
        if((Arrays.asList(MainActivity.activity.fileList()).contains(MD5.md5(sciezka) + Stale.SUFFIXCACHEDANE)) && (Arrays.asList(MainActivity.activity.fileList()).contains(MD5.md5(sciezka) + Stale.SUFFIXCACHETAB))) {
            return true;
        } else {
            return false;
        }
    }

    private void wczytajCache() throws IOException {
        FileInputStream plikcachedane = MainActivity.activity.openFileInput(MD5.md5(sciezka) + Stale.SUFFIXCACHEDANE);
        byte[] bajty = new byte[1000];
        plikcachedane.read(bajty, 0, 4);
        int dlugoscrozszerzenia = bajtyNaInt(bajty);
        plikcachedane.read(bajty, 0, dlugoscrozszerzenia);
        rozszerzenie = new String(bajty, 0, dlugoscrozszerzenia);
        plikcachedane.read(bajty, 0, 4);
        int dlugoscprefixu = bajtyNaInt(bajty);
        plikcachedane.read(bajty, 0, dlugoscprefixu);
        prefix = new String(bajty, 0, dlugoscprefixu);
        plikcachedane.read(bajty, 0, 4);
        ilosckaflix = bajtyNaInt(bajty);
        plikcachedane.read(bajty, 0, 4);
        ilosckafliy = bajtyNaInt(bajty);
        plikcachedane.read(bajty, 0, 4);
        rozmiarx = bajtyNaInt(bajty);
        plikcachedane.read(bajty, 0, 4);
        rozmiary = bajtyNaInt(bajty);
        plikcachedane.read(bajty, 0, 4);
        mapstart = bajtyNaInt(bajty);
        plikcachedane.read(bajty, 0, 4);
        mapdlugosc = bajtyNaInt(bajty);
        plikcachedane.close();
        startkafla = new int[ilosckaflix][ilosckafliy];
        dlugosckafla = new int[ilosckaflix][ilosckafliy];
        FileInputStream plikcachetab = MainActivity.activity.openFileInput(MD5.md5(sciezka) + Stale.SUFFIXCACHETAB);
        ByteBuffer bajtytablicy = ByteBuffer.allocate(2 * 4 * ilosckaflix * ilosckafliy);
        plikcachetab.read(bajtytablicy.array(), 0, 2 * 4 * ilosckaflix * ilosckafliy);
        plikcachetab.close();
        IntBuffer intbajtytablicy = bajtytablicy.asIntBuffer();
        for(int i = 0; i < ilosckaflix; i++) {
            intbajtytablicy.get(startkafla[i], 0, ilosckafliy);
        }
        for(int i = 0; i < ilosckaflix; i++) {
            intbajtytablicy.get(dlugosckafla[i], 0, ilosckafliy);
        }
    }

    private byte[] intNaBajty(int i) {
        ByteBuffer bajty = ByteBuffer.allocate(4);
        bajty.order(ByteOrder.LITTLE_ENDIAN);
        bajty.putInt(i);
        return bajty.array();
    }

    public static int bajtyNaInt(byte[] b) {
        final ByteBuffer bajty = ByteBuffer.wrap(b);
        bajty.order(ByteOrder.LITTLE_ENDIAN);
        return bajty.getInt();
    }

    private void ustawSciezkiDodatkowe() {
        sciezkabezrozszerzenia = sciezka.replace(".tmi", "");
        sciezkatar = sciezkabezrozszerzenia + ".tar";
    }

    private void parsujMap() throws IOException {
        RandomAccessFile pliktar = new RandomAccessFile(sciezkatar, "r");
        pliktar.seek((long)mapstart * 512L + 512);
        byte[] bajty = new byte[512 * mapdlugosc];
        pliktar.read(bajty, 0, 512 * mapdlugosc);
        pliktar.close();
        String wczytanyplik = new String(bajty);
        String[] maparray = wczytanyplik.split("\n");
        float gpsstartx = 1000.0F;
        float gpsstarty = 1000.0F;
        float gpskoniecx = -1000.0F;
        float gpskoniecy = -1000.0F;
        int rozmiarmapyx = -1;
        int rozmiarmapyy = -1;
        for(int i = 0; i < maparray.length; i++) {
            if(maparray[i].trim().matches("MMPLL.*")) {
                String[] manarray2 = maparray[i].trim().split(",");
                if(Float.parseFloat(manarray2[2].trim()) < gpsstartx) {
                    gpsstartx = Float.parseFloat(manarray2[2].trim());
                }
                if(Float.parseFloat(manarray2[2].trim()) > gpskoniecx) {
                    gpskoniecx = Float.parseFloat(manarray2[2].trim());
                }
                if(Float.parseFloat(manarray2[3].trim()) < gpsstarty) {
                    gpsstarty = Float.parseFloat(manarray2[3].trim());
                }
                if(Float.parseFloat(manarray2[3].trim()) > gpskoniecy) {
                    gpskoniecy = Float.parseFloat(manarray2[3].trim());
                }
            }
            if(maparray[i].trim().matches("MMPXY *, *3 *,.*")) {
                String[] manarray4 = maparray[i].split(",", -1);
                rozmiarmapyx = Integer.parseInt(manarray4[2].trim()) + 1;
                rozmiarmapyy = Integer.parseInt(manarray4[3].trim()) + 1;
            }
        }
        gpsstart.set(gpsstartx, gpsstarty);
        gpskoniec.set(gpskoniecx, gpskoniecy);
        rozmiarmapy.set(rozmiarmapyx, rozmiarmapyy);
    }

    private void zapiszCache() throws IOException {
        FileOutputStream plikcachedane = MainActivity.activity.openFileOutput(MD5.md5(sciezka) + Stale.SUFFIXCACHEDANE, Context.MODE_PRIVATE);
        plikcachedane.write(intNaBajty(rozszerzenie.getBytes().length));
        plikcachedane.write(rozszerzenie.getBytes());
        plikcachedane.write(intNaBajty(prefix.getBytes().length));
        plikcachedane.write(prefix.getBytes());
        plikcachedane.write(intNaBajty(ilosckaflix));
        plikcachedane.write(intNaBajty(ilosckafliy));
        plikcachedane.write(intNaBajty(rozmiarx));
        plikcachedane.write(intNaBajty(rozmiary));
        plikcachedane.write(intNaBajty(mapstart));
        plikcachedane.write(intNaBajty(mapdlugosc));
        plikcachedane.close();
        ByteBuffer bajtytablicy = ByteBuffer.allocate(2 * 4 * ilosckaflix * ilosckafliy);
        IntBuffer intbajtytablicy = bajtytablicy.asIntBuffer();
        for(int i = 0; i < ilosckaflix; i++) {
            intbajtytablicy.put(startkafla[i]);
        }
        for(int i = 0; i < ilosckaflix; i++) {
            intbajtytablicy.put(dlugosckafla[i]);
        }
        FileOutputStream plikcachetab = MainActivity.activity.openFileOutput(MD5.md5(sciezka) + Stale.SUFFIXCACHETAB, Context.MODE_PRIVATE);
        plikcachetab.write(bajtytablicy.array(), 0, 2 * 4 * ilosckaflix * ilosckafliy);
        plikcachetab.close();
    }

    public void parsuj() throws IOException {
        if(sprawdzCache()) {
            wczytajCache();
        } else {
            List<Integer> listax = new ArrayList<>(10000);
            List<Integer> listay = new ArrayList<>(10000);
            pierwszyPrzebieg(listax, listay);
            szukajWielkosciKafla(listax, listay);
            utworzTablice();
            drugiPrzebieg();
            zapiszCache();
        }
        parsujMap();
        /*Log.e("WWW", sciezka);
        Log.e("WWW", String.valueOf(rozmiarx));
        Log.e("WWW", String.valueOf(rozmiary));
        Log.e("WWW", String.valueOf(ilosckaflix));
        Log.e("WWW", String.valueOf(ilosckafliy));
        Log.e("WWW", rozszerzenie);
        Log.e("WWW", prefix);
        for(int i = 0; i < ilosckaflix; i++) {
            for(int j = 0; j < ilosckafliy; j++) {
                Log.e("WWW", i + " " + j + ": " + startkafla[i][j] + " " + dlugosckafla[i][j]);
            }
        }
        Log.e("WWW", String.valueOf(mapstart));
        Log.e("WWW", String.valueOf(mapdlugosc));*/
    }

}
