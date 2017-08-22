package trzcina.maplas6.atlasy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import trzcina.maplas6.MainActivity;
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.ustawienia.Ustawienia;

public class Atlasy {

    public static List<Atlas> atlasy;

    public static void szukajAtlasow() throws IOException {
        atlasy = new ArrayList<>(50);
        File[] katalogi = new File(Ustawienia.folderzmapami.wartosc).listFiles();
        if(katalogi != null) {
            Arrays.sort(katalogi, new Comparator<File>() {
                @Override
                public int compare(File file, File t1) {
                    return file.getName().toLowerCase().compareTo(t1.getName().toLowerCase());
                }
            });
            for(int i = 0; i < katalogi.length; i++) {
                if(katalogi[i].isDirectory()) {
                    Atlas atlas = new Atlas(katalogi[i].getName());
                    MainActivity.activity.ustawInfoPrzygotowanie("Parsuje: " + katalogi[i].getName());
                    atlas.parsuj();
                    atlasy.add(atlas);
                }
            }
        }
    }

    public static void wczytajDoPol() {
        MainActivity.activity.czyscSpisMap();
        for(int i = 0; i < atlasy.size(); i++) {
            MainActivity.activity.dodajPozycjeDoSpisuMap(atlasy.get(i).nazwa);
        }
    }
}
