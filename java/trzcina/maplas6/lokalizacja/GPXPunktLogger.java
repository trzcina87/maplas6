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

import trzcina.maplas6.pomoc.Stale;
import trzcina.maplas6.ustawienia.Ustawienia;

public class GPXPunktLogger {

    private static FileWriter filewriter = null;
    private static PrintWriter printwriter = null;
    public static String nazwapliku = null;
    public static List<PunktNaMapie> lista = new ArrayList<>(1000);

    public static void inicjuj() {
        filewriter = null;
        printwriter = null;
        nazwapliku = null;
        lista = new ArrayList<>(1000);
    }

    private static void otworzPlik() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        String nazwaplikdata = dateFormat.format(System.currentTimeMillis());
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

    public static boolean zapiszPunkt(float wspx, float wspy, String nazwa, String komentarz) {
        if(nazwapliku == null) {
            otworzPlik();
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String data = dateFormat.format(System.currentTimeMillis());
        String liniadata = "    <time>" + data + "</time>";
        try {
            printwriter.println("  <wpt lon='" + wspx + "' lat='" + wspy + "'>");
            printwriter.println(liniadata);
            printwriter.println("    <name>" + nazwa + "</name>");
            printwriter.println("    <cmt>" + komentarz + "</cmt>");
            printwriter.println("  </wpt>");
            printwriter.flush();
            lista.add(new PunktNaMapie(wspx, wspy, nazwa, komentarz));
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
                nazwapliku = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
