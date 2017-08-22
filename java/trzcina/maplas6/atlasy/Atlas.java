package trzcina.maplas6.atlasy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import trzcina.maplas6.pomoc.Stale;
import trzcina.maplas6.ustawienia.Ustawienia;

public class Atlas {

    public String nazwa;                    //Nazwa atlasu (katalogu)
    public int stan;                        //Stan parsowania
    public List<String> plikitmi;           //Lista plikow TMI
    public List<TmiParser> parserytmi;      //Lista parserow TMI

    public Atlas(String nazwa) {
        this.nazwa = nazwa;
        stan = Stale.ATLASNOWY;
        plikitmi = new ArrayList<>(20);
        parserytmi = new ArrayList<>(20);
    }

    //Szuka plikow TMI w podanym folderze i dodaje do listy
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

    //Parsuje atlas
    public void parsuj() throws IOException {
        stan = Stale.ATLASROBIE;

        //Szuakmy plikow TMI bezposrednio w folderze
        szukajPlikowTMIwFolderze(Ustawienia.folderzmapami.wartosc + nazwa);

        //Szuakmy plikow TMI rowniez w podfolderach w folderze atlasu, ale nie blebiej
        File[] pliki = new File(Ustawienia.folderzmapami.wartosc + nazwa).listFiles();
        if(pliki != null) {
            for(int i = 0; i < pliki.length; i++) {
                if(pliki[i].isDirectory()) {
                    szukajPlikowTMIwFolderze(pliki[i].getAbsolutePath());
                }
            }
        }

        //Jesli sa pliki TMI to tworzymy parsery i parsujemy kazdy plik
        if(plikitmi.size() > 0) {
            for(int i = 0; i < plikitmi.size(); i++) {
                TmiParser tmiparser = new TmiParser(plikitmi.get(i));
                tmiparser.parsuj();
                if(tmiparser.weryfikuj()) {
                    parserytmi.add(tmiparser);
                }
            }

            //Jesli dodalismy jakis parser znaczy ze atlas jest sprawny
            if(parserytmi.size() > 0) {
                stan = Stale.ATLASGOTOWY;
            } else {
                stan = Stale.ATLASBLAD;
            }
        } else {

            //Jesli nie ma to atlas jest bledy
            stan = Stale.ATLASBLAD;
        }
    }

}
