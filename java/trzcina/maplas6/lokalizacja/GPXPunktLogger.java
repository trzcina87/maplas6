package trzcina.maplas6.lokalizacja;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import trzcina.maplas6.AppService;
import trzcina.maplas6.pomoc.Stale;
import trzcina.maplas6.pomoc.Wear;
import trzcina.maplas6.ustawienia.Ustawienia;

public class GPXPunktLogger {

    private static FileWriter filewriter = null;
    private static PrintWriter printwriter = null;
    public static String nazwapliku = null;
    public static String nazwaplikdata = null;
    public static List<PunktNaMapie> lista = new ArrayList<>(1000);

    private static String pobierzSkroconaNazweAtlasu(String nazwa) {
        String[] tablica = nazwa.split(" ");
        String nazwaskrocona = "";
        for(int i = 0; i < tablica.length; i++) {
            if(tablica[i].length() > 0) {
                if (Character.isUpperCase(tablica[i].charAt(0))) {
                    nazwaskrocona = nazwaskrocona + tablica[i] + " ";
                } else {
                    break;
                }
            }
        }
        return nazwaskrocona.trim();
    }

    public static void inicjuj() {
        filewriter = null;
        printwriter = null;
        nazwapliku = null;
        lista = new ArrayList<>(1000);
    }

    private static void otworzPlik() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        nazwaplikdata = dateFormat.format(System.currentTimeMillis());
        nazwapliku = Ustawienia.nazwaurzadzenia.wartosc + " " + nazwaplikdata + " wpt.gpx";
        try {
            filewriter = new FileWriter(new File(Stale.SCIEZKAMAPLAS + nazwapliku));
            printwriter = new PrintWriter(filewriter);
            printwriter.println("<?xml version='1.0' encoding='ISO-8859-1' ?>");
            printwriter.println("<gpx version='1.1' creator='MapLas 1.0' xmlns='http://www.topografix.com/GPX/1/1' xmlns:gpxtpx='http://www.garmin.com/xmlschemas/TrackPointExtension/v1' xmlns:nmea='http://trekbuddy.net/2009/01/gpx/nmea' xmlns:gsm='http://trekbuddy.net/2009/01/gpx/gsm'>");
            printwriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean zapiszPunkt(float wspx, float wspy, String nazwa, String komentarz, float dokladnosc) {
        if(nazwapliku == null) {
            otworzPlik();
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String data = dateFormat.format(System.currentTimeMillis());
        String liniadata = "    <time>" + data + "</time>";
        try {
            int dok = Math.round(dokladnosc);
            printwriter.println("  <wpt lon='" + wspx + "' lat='" + wspy + "'>");
            printwriter.println(liniadata);
            printwriter.println("    <name>" + nazwa + "</name>");
            printwriter.println("    <cmt>" + komentarz + "</cmt>");
            printwriter.println("    <desc>" + "dokladnosc:" + dok + "</desc>");
            printwriter.println("  </wpt>");
            printwriter.flush();
            lista.add(new PunktNaMapie(wspx, wspy, nazwa, komentarz));
            Wear.wyslijPunktDoZegarka(new PunktNaMapie(wspx, wspy, nazwa, komentarz));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void zakonczPlik() {
        if(printwriter != null) {
            try {
                printwriter.write("</gpx>");
                printwriter.flush();
                printwriter.close();
                if(AppService.service.atlas != null) {
                    String skroconanazwa = pobierzSkroconaNazweAtlasu(AppService.service.atlas.nazwa);
                    new File(Stale.SCIEZKAMAPLAS + nazwapliku).renameTo(new File(Stale.SCIEZKAMAPLAS + Ustawienia.nazwaurzadzenia.wartosc + " " + skroconanazwa + " " + nazwaplikdata + " wpt.gpx"));
                }
                nazwapliku = null;
                nazwaplikdata = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
