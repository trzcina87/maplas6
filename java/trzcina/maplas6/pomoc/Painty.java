package trzcina.maplas6.pomoc;

import android.graphics.Color;
import android.graphics.Paint;

import trzcina.maplas6.MainActivity;

public class Painty {

    public static float density;
    public static Paint paintczerwonysrodek;
    public static Paint paintzielonyokrag;
    public static Paint paintzielonyokragtrasa;

    //Inicjuje painty uzywane przez watek rysuj
    public static void inicjujPainty() {
        density = MainActivity.activity.getResources().getDisplayMetrics().density;
        paintczerwonysrodek = new Paint();
        paintczerwonysrodek.setStyle(Paint.Style.STROKE);
        paintczerwonysrodek.setColor(Color.RED);
        paintczerwonysrodek.setStrokeWidth(density);
        paintzielonyokrag = new Paint();
        paintzielonyokrag.setStyle(Paint.Style.STROKE);
        paintzielonyokrag.setColor(Color.rgb(0, 255, 0));
        paintzielonyokrag.setStrokeWidth(3 * density);
        paintzielonyokragtrasa = new Paint();
        paintzielonyokragtrasa.setStyle(Paint.Style.STROKE);
        paintzielonyokragtrasa.setColor(Color.rgb(0, 255, 0));
        paintzielonyokragtrasa.setStrokeWidth(2 * density);
    }

}
