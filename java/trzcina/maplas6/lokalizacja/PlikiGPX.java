package trzcina.maplas6.lokalizacja;

import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import trzcina.maplas6.MainActivity;
import trzcina.maplas6.pomoc.Stale;
import trzcina.maplas6.ustawienia.Ustawienia;

public class PlikiGPX {

    //Globalna lista plikow
    public static List<PlikGPX> pliki;

    //Sortujemy pliki, pierwszenstwo maja mlodsze i te pochodzadze z urzadzenia
    private static void sortujPliki(File[] tablica) {
        final String nazwaurzadzenia = Ustawienia.nazwaurzadzenia.wartosc;
        Arrays.sort(tablica, new Comparator<File>() {
            @Override
            public int compare(File t1, File t2) {
                if((t1.getName().startsWith(nazwaurzadzenia)) && (t2.getName().startsWith(nazwaurzadzenia))) {
                    if(t1.lastModified() < t2.lastModified()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                if((! t1.getName().startsWith(nazwaurzadzenia)) && (! t2.getName().startsWith(nazwaurzadzenia))) {
                    if(t1.lastModified() < t2.lastModified()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                if((t1.getName().startsWith(nazwaurzadzenia)) && (! t2.getName().startsWith(nazwaurzadzenia))) {
                    return 1;
                }
                if((! t1.getName().startsWith(nazwaurzadzenia)) && (t2.getName().startsWith(nazwaurzadzenia))) {
                    return -1;
                }
                return 0;
            }
        });
    }

    //Akceptujemy tylko pliki z rozszerzeniem
    private static File[] szukajPlikowGPSWKatalogu() {
        return new File(Stale.SCIEZKAMAPLAS).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if(s.endsWith(".gpx")) {
                    return true;
                }
                return false;
            }
        });
    }

    public static void wczytajDoPol() {
        MainActivity.activity.czyscSpisPlikow();
        for(int i = 0; i < pliki.size(); i++) {
            MainActivity.activity.dodajPozycjeDoSpisuPlikow(pliki.get(i));
        }
    }

    public static void znajdzIZaznaczPlik(String nazwa, boolean zaznaczony) {
        for(int i = 0; i < pliki.size(); i++) {
            if(pliki.get(i).nazwa.equals(nazwa)) {
                pliki.get(i).zaznaczony = zaznaczony;
            }
        }
    }

    public static void znajdzIUsun(String nazwa) {
        PlikGPX plik = null;
        for(int i = 0; i < pliki.size(); i++) {
            if(pliki.get(i).nazwa.equals(nazwa)) {
                plik = pliki.get(i);
            }
        }
        if(plik != null) {
            pliki.remove(plik);
        }
    }

    //Szukamy wszystkich plikow w katalogu aplikacji
    public static void szukajPlikow() {
        pliki = new ArrayList<>(100);
        File[] plikiwkatalogu = szukajPlikowGPSWKatalogu();
        if(plikiwkatalogu != null) {

            sortujPliki(plikiwkatalogu);

            //Dla kazdego katalogu w katalogu z mapami tworzymy atlas
            for(int i = 0; i < plikiwkatalogu.length; i++) {
                PlikGPX plikgpx = new PlikGPX(plikiwkatalogu[i].getAbsolutePath());
                plikgpx.parsuj();
                if(plikgpx.stan == Stale.PLIKGOTOWY) {
                    pliki.add(plikgpx);
                }
            }
        }
    }

    public static void dodatkowoSparsuj(String nazwa) {
        if(nazwa.endsWith(".gpx")) {
            PlikGPX plikgpx = new PlikGPX(Stale.SCIEZKAMAPLAS + nazwa);
            plikgpx.parsuj();
            if(plikgpx.stan == Stale.PLIKGOTOWY) {
                pliki.add(plikgpx);
            }
        }
    }
}
