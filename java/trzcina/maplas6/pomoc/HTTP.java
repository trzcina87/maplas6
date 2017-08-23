package trzcina.maplas6.pomoc;

import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

}
