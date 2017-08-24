package trzcina.maplas6.pomoc;

import android.util.Base64;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import trzcina.maplas6.MainActivity;
import trzcina.maplas6.lokalizacja.PlikGPX;
import trzcina.maplas6.lokalizacja.PunktNaMapie;

public class HTTP {

    public static String sciagnijPlik(String download, String user, String haslo, StringBuilder naglowek) {
        try {
            URL url = new URL(download);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            String userPass = user + ":" + haslo;
            String basicAuth = "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.DEFAULT);
            urlConnection.setRequestProperty("Authorization", basicAuth);
            InputStream strumien = new BufferedInputStream(urlConnection.getInputStream());
            if (urlConnection.getResponseCode() == 200) {
                byte bodybajty[] = new byte[1000000];
                int przeczytano = Rozne.odczytajZeStrumienia(strumien, 1000000, bodybajty);
                if(naglowek != null) {
                    naglowek.append(urlConnection.getHeaderField("Content-Disposition"));
                }
                strumien.close();
                return new String(bodybajty, 0, przeczytano);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void wyslijPlik(String upload, String user, String haslo, PlikGPX plik) {
        try {
            String parametry = "";
            if(plik.punkty.size() > 0) {
                for(int i = 0; i < plik.punkty.size(); i++) {
                    PunktNaMapie punkt = plik.punkty.get(i);
                    parametry = parametry + "x" + i + "=" + punkt.wspx + "&";
                    parametry = parametry + "y" + i + "=" + punkt.wspy + "&";
                    parametry = parametry + "n" + i + "=" + punkt.nazwa + "&";
                    parametry = parametry + "o" + i + "=" + punkt.opis + "&";
                }
                byte[] danepost = parametry.getBytes(StandardCharsets.UTF_8);
                int dlugoscdanych = danepost.length;
                String zapytanie = upload + "?ilosc=" + plik.punkty.size();
                URL url = new URL(zapytanie);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                String userPass = user + ":" + haslo;
                String basicAuth = "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.DEFAULT);
                conn.setRequestProperty("Authorization", basicAuth);
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(dlugoscdanych));
                conn.setUseCaches(false);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.write(danepost);
                InputStream in = new BufferedInputStream(conn.getInputStream());
                if (conn.getResponseCode() == 200) {
                    byte bodybajty[] = new byte[100];
                    in.read(bodybajty, 0, 100);
                    if (new String(bodybajty).startsWith("PUSZCZAOK")) {
                        MainActivity.activity.pokazToast(plik.nazwa + ": " + plik.punkty.size() + " punkty wysłane!");
                    } else {
                        MainActivity.activity.pokazToast(plik.nazwa + ": " + "Błędna odpowiedź z serwera!");
                    }
                } else {
                    MainActivity.activity.pokazToast(plik.nazwa + ": " + "Błędna odpowiedź z serwera!");
                }
            } else {
                MainActivity.activity.pokazToast(plik.nazwa + ": " + "Brak punktów!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            MainActivity.activity.pokazToast(plik.nazwa + ": " + "Bląd wysyłania pliku!");
        }
    }

}
