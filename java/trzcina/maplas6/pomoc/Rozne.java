package trzcina.maplas6.pomoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import trzcina.maplas6.BuildConfig;

public class Rozne {

    //Czeka podana ilosc czasu
    public static void czekaj(int milisekundy) {
        try {
            Thread.sleep(milisekundy);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Tworzy katalog
    public static boolean utworzKatalog(String nazwa) {
        File katalog = new File(nazwa);
        if(katalog.isDirectory()) {
            return true;
        } else {
            try {
                katalog.mkdir();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    //Sortuje liste Integer
    public static void sortujListe(List<Integer> lista) {
        Collections.sort(lista, new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return integer.compareTo(t1);
            }
        });
    }

    public static byte[] intNaBajty(int i) {
        ByteBuffer bajty = ByteBuffer.allocate(4);
        bajty.order(ByteOrder.LITTLE_ENDIAN);
        bajty.putInt(i);
        return bajty.array();
    }

    public static int bajtyNaInt(byte[] b) {
        final ByteBuffer bajty = ByteBuffer.wrap(b);
        bajty.order(ByteOrder.LITTLE_ENDIAN);
        return bajty.getInt();
    }

    public static byte[] odczytajZPliku(FileInputStream plik, int ilosc) throws IOException {
        byte[] bajty = new byte[ilosc];
        int przeczytano = 0;
        while(przeczytano < ilosc) {
            int przeczytanowprzebiegu = plik.read(bajty, przeczytano, ilosc - przeczytano);
            if(przeczytanowprzebiegu == -1) {
                break;
            }
            przeczytano = przeczytano + przeczytanowprzebiegu;
        }
        return bajty;
    }

    public static void odczytajZPliku(FileInputStream plik, int ilosc, byte[] bajty) throws IOException {
        int przeczytano = 0;
        while(przeczytano < ilosc) {
            int przeczytanowprzebiegu = plik.read(bajty, przeczytano, ilosc - przeczytano);
            if(przeczytanowprzebiegu == -1) {
                break;
            }
            przeczytano = przeczytano + przeczytanowprzebiegu;
        }
    }

    public static String pobierzDateBudowania() {
        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String data = dateFormat.format(buildDate);
        return data;
    }
}
