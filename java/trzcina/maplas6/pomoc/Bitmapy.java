package trzcina.maplas6.pomoc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import trzcina.maplas6.MainActivity;
import trzcina.maplas6.R;

public class Bitmapy {

    public static Bitmap strzalka;
    public static Bitmap brakmapy;
    public static Bitmap kursorgps;

    //Wczytujemy wszystkie bitmapy uzywane w programie do pamieci
    public static void inicjujBitmapy() {
        strzalka = BitmapFactory.decodeResource(MainActivity.activity.getResources(), R.mipmap.strzalka100);
        brakmapy = BitmapFactory.decodeResource(MainActivity.activity.getResources(), R.mipmap.brakmapy);
        kursorgps = BitmapFactory.decodeResource(MainActivity.activity.getResources(), R.mipmap.gps);
    }
}
