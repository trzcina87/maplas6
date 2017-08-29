package trzcina.maplas6.watki;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;
import android.view.WindowManager;

import trzcina.maplas6.AppService;
import trzcina.maplas6.MainActivity;
import trzcina.maplas6.pomoc.Rozne;

public class KompasWatek extends Thread implements SensorEventListener {

    public volatile boolean zakoncz;    //Info czy zakonczyc
    public volatile int kat;            //Odczyt katu
    public volatile int dokladnosc;     //Dokladnosc kompasu
    private WindowManager windowmanager;


    private int zwrocKatUrzadzenia() {
        int rotation = windowmanager.getDefaultDisplay().getRotation();
        if (Surface.ROTATION_0 == rotation) {
            return 0;
        }
        if(Surface.ROTATION_180 == rotation) {
            return 180;
        }
        if(Surface.ROTATION_90 == rotation) {
            return 90;
        }
        if(Surface.ROTATION_270 == rotation) {
            return 270;
        }
        return 0;
    }


    public KompasWatek() {
        kat = 0;
        dokladnosc = -1;
        zakoncz = false;
        windowmanager = (WindowManager)MainActivity.activity.getSystemService(Context.WINDOW_SERVICE);
    }

    //Odczyt wartosci kata, jesli nowy to odswiezamy rysunek
    @Override
    public void onSensorChanged(SensorEvent event) {
        int kattmp = Math.round(event.values[0]) + zwrocKatUrzadzenia();
        if(kattmp != kat) {
            kat = kattmp;
            AppService.service.rysujwatek.odswiez = true;
        }
    }

    //Zmiana wartosci dokladnosci
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        dokladnosc = i;
        AppService.service.rysujwatek.odswiez = true;
    }

    //Rejestracja kompasu i wyrejestrowanie po zakonczeniu
    public void run() {
        SensorManager sensory = (SensorManager)MainActivity.activity.getSystemService(MainActivity.SENSOR_SERVICE);
        sensory.registerListener(this, sensory.getDefaultSensor(Sensor.TYPE_ORIENTATION), 100000);
        while(zakoncz == false) {
            Rozne.czekaj(1000);
        }
        sensory.unregisterListener(this);
    }
}