package trzcina.maplas6.pomoc;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import trzcina.maplas6.MainActivity;

@SuppressWarnings({"PointlessBooleanExpression", "RedundantIfStatement"})
public class Uprawnienia {

    public static volatile boolean odczyt;
    public static volatile boolean zapis;
    public static volatile boolean lokalizacja;

    public static boolean czyNadane() {
        if((odczyt == true) && (zapis == true) && (lokalizacja == true)) {
            return true;
        } else {
            return false;
        }
    }

    public static void zainicjujUprawnienia() {
        odczyt = false;
        zapis = false;
        lokalizacja = false;
        List<String> lista = new ArrayList<>(3);
        if (Build.VERSION.SDK_INT >= 23) {
            if (sprawdzUprawnienie(Manifest.permission.READ_EXTERNAL_STORAGE) == false) {
                lista.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                odczyt = true;
            }
            if (sprawdzUprawnienie(Manifest.permission.WRITE_EXTERNAL_STORAGE) == false) {
                lista.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                zapis = true;
            }
            if (sprawdzUprawnienie(Manifest.permission.ACCESS_FINE_LOCATION) == false) {
                lista.add(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                lokalizacja = true;
            }
            if(lista.size() > 0) {
                String[] listanowa = new String[lista.size()];
                for(int i = 0; i < lista.size(); i++) {
                    listanowa[i] = lista.get(i);
                }
                poprosUprawnienia(listanowa);
            }
        } else {
            odczyt = true;
            zapis = true;
            lokalizacja = true;
        }
    }

    private static boolean sprawdzUprawnienie(String uprawnienie) {
        int rezultat = ContextCompat.checkSelfPermission(MainActivity.activity, uprawnienie);
        if (rezultat == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private static void poprosUprawnienia(String[] uprawnienia) {
        ActivityCompat.requestPermissions(MainActivity.activity, uprawnienia, 1);
    }

}
