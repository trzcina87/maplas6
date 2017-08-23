package trzcina.maplas6.atlasy;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

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
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.pomoc.Stale;

public class TmiParser {

    public String sciezka;                  //Sciezka pliku TMI
    public String md5sciezka;               //MD5 sciezki pliku
    public int mapstart;                    //Start pliku MAP w pliku TAR
    public int mapdlugosc;                  //Dlugosc pliku MAP w pliku TAR
    public Point rozmiarkafla;              //Rozmiar kafla w poziomie i pionie
    public Point ilosckafli;                //Ilosc kafli w poziomie i pionie
    public Point rozmiarmapy;               //Rozmiar mapy w Pixelach
    public String rozszerzenie;             //Rozszerzenie pliku graficznego
    public String prefix;                   //Prefix plikow graficznych
    public int startkafla[][];              //Starty plikow graficznych w pliku TAR
    public int dlugosckafla[][];            //Dlugosci plikow graficznych w pliku TAR
    public String sciezkabezrozszerzenia;   //Sciezka pliku TMI bez rozszerzenia
    public String sciezkatar;               //Sciezka do pliku TAR
    public PointF gpsstart;                 //Poczatkowy skraj GPS
    public PointF gpskoniec;                //Koncowy skraj GPS
    public Float rozpietoscxgeograficzna;
    public Float rozpietoscygeograficzna;

    public TmiParser(String sciezka) {
        this.sciezka = sciezka;
        md5sciezka = MD5.md5(sciezka);
        ustawSciezkiDodatkowe();
        mapstart = -1;
        mapdlugosc = -1;
        rozszerzenie = null;
        prefix = null;
        startkafla = null;
        dlugosckafla = null;
        gpsstart = new PointF(-1, -1);
        gpskoniec = new PointF(-1, -1);
        rozmiarmapy = new Point(-1, -1);
        rozmiarkafla = new Point(-1, -1);
        ilosckafli = new Point(-1, -1);
    }

    //Na podstawie wpisu z TMI ustawiamy rozszerzenie pliku graficznego
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

    //Znajdujemy prefix na podstawie wpisu z plikiem graficznym
    private void znajdzPrefix(String koniec) {
        String[] podzial = koniec.split("_");
        prefix = podzial[0] + "_";
    }

    //Na podstawie wpisu z plikiem graficznym znajdujemy rozszerzenie i prefix
    private void znajdzRozszerzenieIPrefix(String koniec) {
        znajdzRozszerzenie(koniec);
        znajdzPrefix(koniec);
    }

    //Sprawdzamy czy podany wpis w pliku TMI zawiera wpis graficzny
    private boolean czyLiniaBitmapy(String koniec) {
        if((koniec.endsWith(".jpg")) || (koniec.endsWith(".png")) || (koniec.endsWith(".bmp")) || (koniec.endsWith(".gif"))) {
            return true;
        } else {
            return false;
        }
    }

    //Sprawdzamy czy podany wpis w pliku TMI zawiera wpis o pliku MAP
    private boolean czyLiniaMap(String koniec) {
        if(koniec.endsWith(".map")) {
            return true;
        } else {
            return false;
        }
    }

    //Otwiera plik do odczytu
    private BufferedReader otworzPlik() throws FileNotFoundException {
        InputStream inputstream = new FileInputStream(sciezka);
        InputStreamReader inputreader = new InputStreamReader(inputstream);
        return new BufferedReader(inputreader);
    }

    //Na podstawie nazw plikow (juz fragmentow) w TMI szuka wielkosci kafla w pionie i poziomie
    private void szukajWielkosciKafla(List<Integer> listax, List<Integer> listay) {
        Rozne.sortujListe(listax);
        Rozne.sortujListe(listay);
        int startx = listax.get(0);
        int obecnyx = startx;
        int i = 0;
        while((i < listax.size()) && (obecnyx == startx)) {
            obecnyx = listax.get(i);
            i = i + 1;
        }
        if(obecnyx == startx) {
            rozmiarkafla.x = 1024;
        } else {
            rozmiarkafla.x = obecnyx;
        }
        int starty = listay.get(0);
        int obecnyy = starty;
        i = 0;
        while((i < listay.size()) && (obecnyy == starty)) {
            obecnyy = listay.get(i);
            i = i + 1;
        }
        if(obecnyy == starty) {
            rozmiarkafla.y = 1024;
        } else {
            rozmiarkafla.y = obecnyy;
        }
        ilosckafli.x = listax.get(listax.size() - 1) / rozmiarkafla.x + 1;
        ilosckafli.y = listay.get(listay.size() - 1) / rozmiarkafla.y + 1;
    }

    //Tworzy odpowiednio duze tablice na przechowywanie info o starcie i dlugosci plikow graficznych
    private void utworzTablice() {
        startkafla = new int[ilosckafli.x][ilosckafli.y];
        dlugosckafla = new int[ilosckafli.x][ilosckafli.y];
    }

    private void pierwszyPrzebieg(List<Integer> listax, List<Integer> listay) throws IOException {

        //Otwieramy plik i czytamy kazda linie
        BufferedReader bufferedreader = otworzPlik();
        String przetwarzanalinia = bufferedreader.readLine();
        while(przetwarzanalinia != null) {

            //Dzieli wedlug dwukropka
            String[] podzialdwukropek = przetwarzanalinia.split(":");
            if (podzialdwukropek.length == 2) {
                String p1trim = podzialdwukropek[1].trim();

                //Szukamy rozszerzenie graficznego jesli nie ma
                if(rozszerzenie == null) {
                    if(czyLiniaBitmapy(p1trim) == true) {
                        znajdzRozszerzenieIPrefix(p1trim);
                    }
                }

                //Pobieramy indeks pixela z kazdego pliku graficznego do listy
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

    //Wyznaczymy start i dlugosc kazdego pliku graficznego i pliku MAP
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
                        dlugosckafla[poprzednix / rozmiarkafla.x][poprzedniy / rozmiarkafla.y] = Integer.parseInt(p0trim) - startkafla[poprzednix / rozmiarkafla.x][poprzedniy / rozmiarkafla.y];
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
                    startkafla[x / rozmiarkafla.x][y / rozmiarkafla.y] = Integer.parseInt(p0trim);
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
        if((poprzednix != -1) && (poprzedniy != -1)) {
            if ((poprzednix == -2) && (poprzedniy == -2)) {
                mapdlugosc = mapstart + 1000;
            } else {
                dlugosckafla[poprzednix / rozmiarkafla.x][poprzedniy / rozmiarkafla.y] = startkafla[poprzednix / rozmiarkafla.x][poprzedniy / rozmiarkafla.y] + 1000;
            }
        }
    }

    //Sprawdzamy czy istnieja pliki Cache
    private boolean sprawdzCache() {
        if((Arrays.asList(MainActivity.activity.fileList()).contains(md5sciezka + Stale.SUFFIXCACHEDANE)) && (Arrays.asList(MainActivity.activity.fileList()).contains(md5sciezka + Stale.SUFFIXCACHETAB))) {
            return true;
        } else {
            return false;
        }
    }

    //Wczytuje zmienne z pierwszego pliku cache
    private void wczytajPierwszyPlik() throws IOException {
        FileInputStream plikcachedane = MainActivity.activity.openFileInput(md5sciezka + Stale.SUFFIXCACHEDANE);
        int dlugoscrozszerzenia = wczytajIntZCache(plikcachedane);
        rozszerzenie = wczytajStringZCache(plikcachedane, dlugoscrozszerzenia);
        int dlugoscprefixu = wczytajIntZCache(plikcachedane);
        prefix = wczytajStringZCache(plikcachedane, dlugoscprefixu);
        ilosckafli.x = wczytajIntZCache(plikcachedane);
        ilosckafli.y = wczytajIntZCache(plikcachedane);
        rozmiarkafla.x = wczytajIntZCache(plikcachedane);
        rozmiarkafla.y = wczytajIntZCache(plikcachedane);
        mapstart = wczytajIntZCache(plikcachedane);
        mapdlugosc = wczytajIntZCache(plikcachedane);
        int rozmiarmapyx = wczytajIntZCache(plikcachedane);
        int rozmiarmapyy = wczytajIntZCache(plikcachedane);
        int gpsstartx = wczytajIntZCache(plikcachedane);
        int gpsstarty = wczytajIntZCache(plikcachedane);
        int gpskoniecx = wczytajIntZCache(plikcachedane);
        int gpskoniecy = wczytajIntZCache(plikcachedane);
        rozmiarmapy.set(rozmiarmapyx, rozmiarmapyy);
        gpsstart.set(gpsstartx / 100000.0F, gpsstarty / 100000.0F);
        gpskoniec.set(gpskoniecx / 100000.0F, gpskoniecy / 100000.0F);
        plikcachedane.close();
        utworzTablice();
    }

    //Wczytuje tablice z drugiego pliku cache
    private void wczytajDrugiPlik() throws IOException {
        FileInputStream plikcachetab = MainActivity.activity.openFileInput(md5sciezka + Stale.SUFFIXCACHETAB);
        ByteBuffer bajtytablicy = ByteBuffer.allocate(2 * 4 * ilosckafli.x * ilosckafli.y);
        Rozne.odczytajZPliku(plikcachetab, 2 * 4 * ilosckafli.x * ilosckafli.y, bajtytablicy.array());
        plikcachetab.close();
        IntBuffer intbajtytablicy = bajtytablicy.asIntBuffer();
        for(int i = 0; i < ilosckafli.x; i++) {
            intbajtytablicy.get(startkafla[i], 0, ilosckafli.y);
        }
        for(int i = 0; i < ilosckafli.x; i++) {
            intbajtytablicy.get(dlugosckafla[i], 0, ilosckafli.y);
        }
    }

    //Wczytuje i zwraca pojedynczy int z cache
    private int wczytajIntZCache(FileInputStream plik) throws IOException {
        byte[] bajty = Rozne.odczytajZPliku(plik, 4);
        return Rozne.bajtyNaInt(bajty);
    }

    //Wczytuje i zwraca String wczytany z cache o zadanej dlugosci
    private String wczytajStringZCache(FileInputStream plik, int dlugosc) throws IOException {
        byte[] bajty = Rozne.odczytajZPliku(plik, dlugosc);
        return new String(bajty);
    }

    //Wczytuje parser z plikow cache
    private void wczytajCache() throws IOException {
        wczytajPierwszyPlik();
        wczytajDrugiPlik();
    }

    //Tworzy dodatkowe sciezki na podstawie istnirjacej
    private void ustawSciezkiDodatkowe() {
        sciezkabezrozszerzenia = sciezka.replace(".tmi", "");
        sciezkatar = sciezkabezrozszerzenia + ".tar";
    }

    //Parsuje plik MAP
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

    //Zapis pojedynczych zmiennych
    private void zapiszPierwszyPlik() throws IOException {
        FileOutputStream plikcachedane = MainActivity.activity.openFileOutput(md5sciezka + Stale.SUFFIXCACHEDANE, Context.MODE_PRIVATE);
        plikcachedane.write(Rozne.intNaBajty(rozszerzenie.getBytes().length));
        plikcachedane.write(rozszerzenie.getBytes());
        plikcachedane.write(Rozne.intNaBajty(prefix.getBytes().length));
        plikcachedane.write(prefix.getBytes());
        plikcachedane.write(Rozne.intNaBajty(ilosckafli.x));
        plikcachedane.write(Rozne.intNaBajty(ilosckafli.y));
        plikcachedane.write(Rozne.intNaBajty(rozmiarkafla.x));
        plikcachedane.write(Rozne.intNaBajty(rozmiarkafla.y));
        plikcachedane.write(Rozne.intNaBajty(mapstart));
        plikcachedane.write(Rozne.intNaBajty(mapdlugosc));
        plikcachedane.write(Rozne.intNaBajty(rozmiarmapy.x));
        plikcachedane.write(Rozne.intNaBajty(rozmiarmapy.y));
        plikcachedane.write(Rozne.intNaBajty((int) (gpsstart.x * 100000)));
        plikcachedane.write(Rozne.intNaBajty((int) (gpsstart.y * 100000)));
        plikcachedane.write(Rozne.intNaBajty((int) (gpskoniec.x * 100000)));
        plikcachedane.write(Rozne.intNaBajty((int) (gpskoniec.y * 100000)));
        plikcachedane.close();
    }

    //Zapis tablic
    private void zapiszDrugiPlik() throws IOException {
        ByteBuffer bajtytablicy = ByteBuffer.allocate(2 * 4 * ilosckafli.x * ilosckafli.y);
        IntBuffer intbajtytablicy = bajtytablicy.asIntBuffer();
        for(int i = 0; i < ilosckafli.x; i++) {
            intbajtytablicy.put(startkafla[i]);
        }
        for(int i = 0; i < ilosckafli.x; i++) {
            intbajtytablicy.put(dlugosckafla[i]);
        }
        FileOutputStream plikcachetab = MainActivity.activity.openFileOutput(md5sciezka + Stale.SUFFIXCACHETAB, Context.MODE_PRIVATE);
        plikcachetab.write(bajtytablicy.array(), 0, 2 * 4 * ilosckafli.x * ilosckafli.y);
        plikcachetab.close();
    }

    //Zapisuje dane i tablice do plikow cache
    private void zapiszCache() throws IOException {
        zapiszPierwszyPlik();
        zapiszDrugiPlik();
    }

    private void uzupelnijPolaPomocnicze() {
        rozpietoscxgeograficzna = gpskoniec.x - gpsstart.x;
        rozpietoscygeograficzna = gpskoniec.y - gpsstart.y;
    }

    //Parsujemy plik TMI
    public void parsuj() throws IOException {

        //Jesli jest w cache to tylko wczytujemy
        if(sprawdzCache()) {
            wczytajCache();
        } else {

            //Jesli nie ma w cache to parsujemy i zapisujemy do cache
            List<Integer> listax = new ArrayList<>(10000);
            List<Integer> listay = new ArrayList<>(10000);
            pierwszyPrzebieg(listax, listay);
            szukajWielkosciKafla(listax, listay);
            utworzTablice();
            drugiPrzebieg();
            parsujMap();
            zapiszCache();
        }
        uzupelnijPolaPomocnicze();
    }

    public boolean weryfikuj() {
        if((mapstart == -1) || (mapdlugosc == -1)){
            return false;
        }
        if((rozszerzenie == null) || (prefix == null)) {
            return false;
        }
        if((startkafla == null) || (dlugosckafla == null)) {
            return false;
        }
        if((gpsstart.x < 0) || (gpsstart.y < 0) || (gpskoniec.x < 0) || (gpskoniec.y < 0)) {
            return false;
        }
        if((rozmiarmapy.x == -1) || (rozmiarmapy.y == -1)) {
            return false;
        }
        if((rozmiarkafla.x == -1) || (rozmiarkafla.y == -1)) {
            return false;
        }
        if((ilosckafli.x == -1) || (ilosckafli.y == -1)) {
            return false;
        }
        return true;
    }

    public float obliczWspolrzednaXDlaPixela(int pixel) {
        float procentmapy = pixel / (float)rozmiarmapy.x;
        return gpsstart.x + procentmapy * rozpietoscxgeograficzna;
    }

    public float obliczWspolrzednaYDlaPixela(int pixel) {
        float procentmapy = pixel / (float)rozmiarmapy.y;
        return gpskoniec.y - procentmapy * rozpietoscygeograficzna;
    }

    public int obliczPixelXDlaWspolrzednej(float wspolrzedna) {
        float procentwspolrzednej = (wspolrzedna - gpsstart.x) / rozpietoscxgeograficzna;
        return Math.round(procentwspolrzednej * rozmiarmapy.x);
    }

    public int obliczPixelYDlaWspolrzednej(float wspolrzedna) {
        float procentwspolrzednej = (wspolrzedna - gpsstart.y) / rozpietoscygeograficzna;
        return Math.round(rozmiarmapy.y - procentwspolrzednej * rozmiarmapy.y);
    }

    public boolean czyWspolrzedneWewnatrz(float gpsx, float gpsy) {
        if((gpsx >= gpsstart.x ) && (gpsx <= gpskoniec.x) && (gpsy >= gpsstart.y) && (gpsy <= gpskoniec.y)) {
            return true;
        } else {
            return false;
        }
    }

    private void debugTMI() {
        Log.e("WWW", sciezka);
        Log.e("WWW", rozmiarkafla.x + " " + rozmiarkafla.y);
        Log.e("WWW", ilosckafli.x + " " + ilosckafli.y);
        Log.e("WWW", rozszerzenie);
        Log.e("WWW", prefix);
        for(int i = 0; i < ilosckafli.x; i++) {
            for(int j = 0; j < ilosckafli.y; j++) {
                Log.e("WWW", i + " " + j + ": " + startkafla[i][j] + " " + dlugosckafla[i][j]);
            }
        }
        Log.e("WWW", String.valueOf(mapstart + " " + mapdlugosc));
        Log.e("WWW", rozmiarmapy.x + " " + rozmiarmapy.y);
        Log.e("WWW", gpsstart.x + " " + gpsstart.y);
        Log.e("WWW", gpskoniec.x + " " + gpskoniec.y);
    }

}
