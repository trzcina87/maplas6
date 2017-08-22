package trzcina.maplas6.pomoc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import trzcina.maplas6.MainActivity;
import trzcina.maplas6.R;

public class Bitmapy {

    public static Bitmap strzalka;

    public static void inicjujBitmapy() {
        strzalka = BitmapFactory.decodeResource(MainActivity.activity.getResources(), R.mipmap.strzalka100);
    }
}
