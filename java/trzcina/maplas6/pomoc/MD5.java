package trzcina.maplas6.pomoc;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5 {

    //Liczymy MD5 stringa
    public static String md5(String tekst) {
        try {
            MessageDigest md5skrot = MessageDigest.getInstance("MD5");
            md5skrot.update(tekst.getBytes(), 0, tekst.getBytes().length);
            String md5 = new BigInteger(1, md5skrot.digest()).toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String md5pliku(String sciezka){
        try {
            MessageDigest md5skrot = MessageDigest.getInstance("MD5");
            int dlugoscpliku = (int)(new File(sciezka).length());
            FileInputStream plik = new FileInputStream(sciezka);
            byte[] bajty = new byte[dlugoscpliku];
            int wczytane = plik.read(bajty, 0, dlugoscpliku);
            if (wczytane < dlugoscpliku) {
                int pozostale = dlugoscpliku - wczytane;
                while (pozostale > 0) {
                    byte[] bajtytymczasowe = new byte[pozostale];
                    wczytane = plik.read(bajtytymczasowe, 0, pozostale);
                    System.arraycopy(bajtytymczasowe, 0, bajty, dlugoscpliku - pozostale, wczytane);
                    pozostale = pozostale - wczytane;
                }
            }
            md5skrot.update(bajty, 0, dlugoscpliku);
            String md5 = new BigInteger(1, md5skrot.digest()).toString(16);
            while ( md5.length() < 32 ) {
                md5 = "0"+md5;
            }
            return md5;
        }
        catch (Exception e) {
        }
        return null;
    }
}
