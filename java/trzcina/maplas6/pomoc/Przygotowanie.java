package trzcina.maplas6.pomoc;

import java.io.File;
import java.io.FilenameFilter;

public class Przygotowanie {

    public static void utworzKatalogi() {
        File glownykatalog = new File(Stale.SCIEZKAMAPLAS);
        File koszkatalog = new File(Stale.SCIEZKAMAPLAS + Stale.FOLDERKOSZ);
        if(glownykatalog.exists() == false) {
            glownykatalog.mkdir();
        }
        if(koszkatalog.exists() == false) {
            koszkatalog.mkdir();
        }
    }

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
