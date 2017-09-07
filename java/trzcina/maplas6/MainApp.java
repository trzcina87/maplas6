package trzcina.maplas6;

import android.app.Application;

import java.io.File;
import java.io.PrintStream;

import trzcina.maplas6.lokalizacja.GPXPunktLogger;
import trzcina.maplas6.lokalizacja.PlikiGPX;
import trzcina.maplas6.pomoc.Stale;

public class MainApp extends Application {

    private static void zapiszDoPliku(Throwable wyjatek) {
        try {
            File file = new File(Stale.SCIEZKAMAPLAS + System.currentTimeMillis() + ".crash");
            PrintStream ps = new PrintStream(file);
            wyjatek.printStackTrace(ps);
            ps.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final Thread.UncaughtExceptionHandler obsluga = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                zapiszDoPliku(e);
                if(AppService.service != null) {
                    AppService.service.zakonczUsluge();
                }
                obsluga.uncaughtException(t, e);
            }
        });
    }
}
