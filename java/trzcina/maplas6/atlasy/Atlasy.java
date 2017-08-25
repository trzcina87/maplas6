package trzcina.maplas6.atlasy;

import android.location.Location;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import trzcina.maplas6.MainActivity;
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.pomoc.Stale;
import trzcina.maplas6.ustawienia.Ustawienia;

public class Atlasy {

    //Globalna lista atlasow
    public static List<Atlas> atlasy;

    //Szukamy wszystkich atlasow w katalogu z mapami
    public static void szukajAtlasow() {
        atlasy = new ArrayList<>(50);
        File[] katalogi = new File(Ustawienia.folderzmapami.wartosc).listFiles();
        if(katalogi != null) {

            //Sortujemy atlasy alfabetycznie
            Arrays.sort(katalogi, new Comparator<File>() {
                @Override
                public int compare(File file, File t1) {
                    return file.getName().toLowerCase().compareTo(t1.getName().toLowerCase());
                }
            });

            //Dla kazdego katalogu w katalogu z mapami tworzymy atlas
            for(int i = 0; i < katalogi.length; i++) {
                if(katalogi[i].isDirectory()) {

                    //Parsowanie atlasu i dodawnie do listy jesli sparsowal sie dobrze
                    Atlas atlas = new Atlas(katalogi[i].getName());
                    MainActivity.activity.ustawInfoPrzygotowanie("Parsuje: " + katalogi[i].getName());
                    try {
                        atlas.parsuj();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(atlas.stan == Stale.ATLASGOTOWY) {
                        atlasy.add(atlas);
                    }
                }
            }
        }
    }

    //Dodajemy wpisy do spisu atlasow w opjcach
    public static void wczytajDoPol() {
        MainActivity.activity.czyscSpisMap();
        for(int i = 0; i < atlasy.size(); i++) {
            MainActivity.activity.dodajPozycjeDoSpisuMap(atlasy.get(i).nazwa);
        }
    }

    public static Atlas znajdzAtlasPoNazwie(String nazwa) {
        for(int i = 0; i < atlasy.size(); i++) {
            if(atlasy.get(i).nazwa.equals(nazwa)) {
                return atlasy.get(i);
            }
        }
        return null;
    }

    public static Atlas szukajNajlepszejMapy(Location location) {
        Atlas propozcyja = null;
        double dokladnosc = 1000000;
        for(int i = 0; i < atlasy.size(); i++) {
            if(atlasy.get(i).czyWspolrzedneWewnatrz((float)location.getLongitude(), (float)location.getLatitude())) {
                if(atlasy.get(i).pobierzDokladnosc() < dokladnosc) {
                    propozcyja = atlasy.get(i);
                    dokladnosc = atlasy.get(i).pobierzDokladnosc();
                }
            }
        }
        return propozcyja;
    }
}
