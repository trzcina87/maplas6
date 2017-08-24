package trzcina.maplas6;

import android.app.Application;

import trzcina.maplas6.lokalizacja.GPXPunktLogger;

public class MainApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        final Thread.UncaughtExceptionHandler obsluga = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                GPXPunktLogger.zakonczPlik();
                obsluga.uncaughtException(t, e);
            }
        });
    }
}
