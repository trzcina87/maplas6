package trzcina.maplas6.watki;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import trzcina.maplas6.AppService;
import trzcina.maplas6.MainActivity;
import trzcina.maplas6.pomoc.Rozne;

public class KompasWatek extends Thread implements SensorEventListener {

    public volatile boolean zakoncz;    //Info czy zakonczyc
    public volatile int kat;            //Odczyt katu
    public volatile int dokladnosc;     //Dokladnosc kompasu

    public KompasWatek() {
        kat = 0;
        dokladnosc = -1;
        zakoncz = false;
    }

    //Odczyt wartosci kata, jesli nowy to odswiezamy rysunek
    @Override
    public void onSensorChanged(SensorEvent event) {
        int kattmp = Math.round(event.values[0]);
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