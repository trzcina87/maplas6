package trzcina.maplas6.atlasy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import trzcina.maplas6.pomoc.Stale;
import trzcina.maplas6.ustawienia.Ustawienia;

public class Atlas {

    public String nazwa;
    public int stan;
    public List<String> plikitmi;
    public List<TmiParser> parserytmi;

    public Atlas(String nazwa) {
        this.nazwa = nazwa;
        stan = Stale.ATLASNOWY;
        plikitmi = new ArrayList<>(20);
        parserytmi = new ArrayList<>(20);
    }

    public void szukajPlikowTMIwFolderze(String folder) {
        File[] pliki = new File(folder).listFiles();
        if(pliki != null) {
            for(int i = 0; i < pliki.length; i++) {
                if((pliki[i].isFile() == true) && (pliki[i].getName().endsWith(".tmi"))) {
                    plikitmi.add(pliki[i].getAbsolutePath());
                }
            }
        }
    }

    public void parsuj() throws IOException {
        stan = Stale.ATLASROBIE;
        szukajPlikowTMIwFolderze(Ustawienia.folderzmapami.wartosc + nazwa);
        File[] pliki = new File(Ustawienia.folderzmapami.wartosc + nazwa).listFiles();
        if(pliki != null) {
            for(int i = 0; i < pliki.length; i++) {
                if(pliki[i].isDirectory()) {
                    szukajPlikowTMIwFolderze(pliki[i].getAbsolutePath());
                }
            }
        }
        if(plikitmi.size() > 0) {
            for(int i = 0; i < plikitmi.size(); i++) {
                TmiParser tmiparser = new TmiParser(plikitmi.get(i));
                tmiparser.parsuj();
                parserytmi.add(tmiparser);
            }
        } else {
            stan = Stale.ATLASBLAD;
        }
    }

}
