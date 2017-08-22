package trzcina.maplas6.pomoc;

import java.io.File;
import java.io.FilenameFilter;

import trzcina.maplas6.MainActivity;

@SuppressWarnings("PointlessBooleanExpression")
public class Przygotowanie {

    //Tworzy katalog niezbedne dla programu
    public static void utworzKatalogi() {
        boolean katalog1 = Rozne.utworzKatalog(Stale.SCIEZKAMAPLAS);
        boolean katalog2 = Rozne.utworzKatalog(Stale.SCIEZKAMAPLAS + Stale.FOLDERKOSZ);
        if((katalog1 == false) || (katalog2 == false)) {
            MainActivity.activity.zakonczCalaAplikacje();
        }
    }

    //Czysci stare pliki z kosza
    public static void usunKosz() {
        File[] plikigpx = new File(Stale.SCIEZKAMAPLAS + Stale.FOLDERKOSZ + "/").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if(s.endsWith(".gpx")) {
                    return true;
                }
                return false;
            }
        });
        for(int i = 0; i < plikigpx.length; i++) {
            if(plikigpx[i].lastModified() + Stale.CZASRETENCJIPLIKOW < System.currentTimeMillis()) {
                plikigpx[i].delete();
            }
        }
    }

}
