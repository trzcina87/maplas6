package trzcina.maplas6.lokalizacja;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import trzcina.maplas6.pomoc.Stale;
import trzcina.maplas6.ustawienia.Ustawienia;

public class GPXTrasaLogger {

    private FileWriter filewriter;
    private PrintWriter printwriter;
    public String nazwapliku;
    public PunktWTrasie[] lista;
    public int dlugosc;
    public float dlugosctrasy;
    public float odlegloscodpoczatku;
    public long czasstart;

    private void utworzNazwe() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        String nazwaplikdata = dateFormat.format(System.currentTimeMillis());
        nazwapliku = Ustawienia.nazwaurzadzenia.wartosc + " " + nazwaplikdata + ".gpx";
    }

    private synchronized void otworzPlik() {
        try {
            filewriter = new FileWriter(new File(Stale.SCIEZKAMAPLAS + nazwapliku));
            printwriter = new PrintWriter(filewriter);
            printwriter.println("<?xml version='1.0' encoding='ISO-8859-1' ?>");
            printwriter.println("<gpx version='1.1' creator='MapLas 1.0' xmlns='http://www.topografix.com/GPX/1/1' xmlns:gpxtpx='http://www.garmin.com/xmlschemas/TrackPointExtension/v1' xmlns:nmea='http://trekbuddy.net/2009/01/gpx/nmea' xmlns:gsm='http://trekbuddy.net/2009/01/gpx/gsm'>");
            printwriter.println("  <trk>");
            printwriter.println("    <trkseg>");
            printwriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GPXTrasaLogger() {
        filewriter = null;
        printwriter = null;
        nazwapliku = null;
        lista = new PunktWTrasie[50000];
        dlugosc = 0;
        dlugosctrasy = 0;
        odlegloscodpoczatku = 0;
        utworzNazwe();
        otworzPlik();
        czasstart = System.currentTimeMillis();
    }

    public synchronized boolean zapiszPunkt(float wspx, float wspy, float dokladnosc) {
        try {
            int dok = Math.round(dokladnosc);
            String linia = "      <trkpt lon='" + wspx + "' lat='" + wspy + "'>";
            Date datanum = new Date(System.currentTimeMillis());
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String data = dateFormat.format(datanum);
            String liniadata = "        <time>" + data + "</time>";
            printwriter.println(linia);
            printwriter.println(liniadata);
            printwriter.println("        <desc>" + "dokladnosc:" + dok + "</desc>");
            printwriter.println("      </trkpt>");
            printwriter.flush();
            lista[dlugosc] = new PunktWTrasie(wspx, wspy);
            dlugosc = dlugosc + 1;
            if(dlugosc > 1) {
                dlugosctrasy = dlugosctrasy + PunktWTrasie.zmierzDystans(lista[dlugosc-1], lista[dlugosc-2]);
            }
            odlegloscodpoczatku = PunktWTrasie.zmierzDystans(lista[dlugosc - 1], lista[0]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void zakonczPlikIOdrzuc() {
        if(printwriter != null) {
            try {
                printwriter.println("    </trkseg>");
                printwriter.println("  </trk>");
                printwriter.println("</gpx>");
                printwriter.flush();
                printwriter.close();
                printwriter = null;
                filewriter = null;
                new File(Stale.SCIEZKAMAPLAS + nazwapliku).renameTo(new File(Stale.SCIEZKAMAPLAS + Stale.FOLDERKOSZ + "/" + nazwapliku));
                nazwapliku = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void zakonczPlik() {
        if(printwriter != null) {
            try {
                printwriter.println("    </trkseg>");
                printwriter.println("  </trk>");
                printwriter.println("</gpx>");
                printwriter.flush();
                printwriter.close();
                PlikiGPX.dodatkowoSparsujIZaznacz(new File(nazwapliku).getName());
                nazwapliku = null;
                printwriter = null;
                filewriter = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
