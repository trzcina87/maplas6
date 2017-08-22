package trzcina.maplas6.pomoc;

import android.graphics.Color;
import android.graphics.Paint;

import trzcina.maplas6.MainActivity;

public class Painty {

    public static float density;
    public static Paint paintczerwonysrodek;

    //Inicjuje painty uzywane przez watek rysuj
    public static void inicjujPainty() {
        density = MainActivity.activity.getResources().getDisplayMetrics().density;
        paintczerwonysrodek = new Paint();
        paintczerwonysrodek.setStyle(Paint.Style.STROKE);
        paintczerwonysrodek.setColor(Color.RED);
        paintczerwonysrodek.setStrokeWidth(density);
    }

}
