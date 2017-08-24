package trzcina.maplas6;

import android.app.Application;

import trzcina.maplas6.lokalizacja.GPXPunktLogger;
import trzcina.maplas6.lokalizacja.PlikiGPX;

public class MainApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        final Thread.UncaughtExceptionHandler obsluga = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if(AppService.service != null) {
                    AppService.service.zakonczUsluge();
                }
                obsluga.uncaughtException(t, e);
            }
        });
    }
}
