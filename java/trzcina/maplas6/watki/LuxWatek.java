package trzcina.maplas6.watki;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import trzcina.maplas6.MainActivity;
import trzcina.maplas6.pomoc.Rozne;

public class LuxWatek extends Thread implements SensorEventListener {

    public volatile boolean zakoncz;
    public volatile int lux;

    public LuxWatek() {
        zakoncz = false;
        lux = -1;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int luxtmp = Math.round(event.values[0]);
        if(luxtmp != lux) {
            lux = luxtmp;
            MainActivity.activity.ustawLux(lux);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void run() {
        SensorManager sensory = (SensorManager)MainActivity.activity.getSystemService(MainActivity.SENSOR_SERVICE);
        sensory.registerListener(this, sensory.getDefaultSensor(Sensor.TYPE_LIGHT), 100000);
        while(zakoncz == false) {
            Rozne.czekaj(1000);
        }
        sensory.unregisterListener(this);
    }
}
