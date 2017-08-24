package trzcina.maplas6.watki;

import trzcina.maplas6.AppService;
import trzcina.maplas6.MainActivity;
import trzcina.maplas6.MainSurface;
import trzcina.maplas6.pomoc.Rozne;

/**
 * Created by piotr.trzcinski on 24.08.2017.
 */

public class DzwiekiWatek extends Thread {

    public volatile boolean zakoncz;
    public volatile long czasostatniejlokalizacji;
    public volatile boolean zagralemblad;
    public volatile long ostatatniezagraniebledu;

    public DzwiekiWatek() {
        zakoncz = false;
        czasostatniejlokalizacji = System.currentTimeMillis();
        ostatatniezagraniebledu = System.currentTimeMillis();
        zagralemblad = false;
    }

    public void run() {
        while(zakoncz == false) {
            if(AppService.service.wlaczgps) {
                if(AppService.service.grajdzwieki == true) {
                    if (System.currentTimeMillis() - czasostatniejlokalizacji >= 30000) {
                        if(zagralemblad == false) {
                            MainActivity.activity.grajDzwiek(MainActivity.activity.soundfixerror);
                            ostatatniezagraniebledu = System.currentTimeMillis();
                            zagralemblad = true;
                        } else {
                            if(System.currentTimeMillis() - ostatatniezagraniebledu >= 15000) {
                                MainActivity.activity.grajDzwiek(MainActivity.activity.soundfixerror);
                                ostatatniezagraniebledu = System.currentTimeMillis();
                                zagralemblad = true;
                            }
                        }
                    } else {
                        if(zagralemblad == true) {
                            MainActivity.activity.grajDzwiek(MainActivity.activity.soundfixok);
                            zagralemblad = false;
                        }
                    }
                } else {
                    czasostatniejlokalizacji = System.currentTimeMillis();
                    ostatatniezagraniebledu = System.currentTimeMillis();
                    zagralemblad = false;
                }
            } else {
                czasostatniejlokalizacji = System.currentTimeMillis();
                ostatatniezagraniebledu = System.currentTimeMillis();
                zagralemblad = false;
            }
            Rozne.czekaj(500);
        }
    }
}