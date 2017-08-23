package trzcina.maplas6.ustawienia;

import java.util.ArrayList;
import java.util.List;

import trzcina.maplas6.MainActivity;
import trzcina.maplas6.pomoc.Stale;

public class Ustawienia {

    //Kazde ustawienie to osobny obiekt
    public static UstawienieString nazwaurzadzenia;
    public static UstawienieString folderzmapami;
    public static UstawienieString atlas;
    public static UstawienieString downloadurl;
    public static UstawienieString downloaduser;
    public static UstawienieString downloadpass;
    public static UstawienieString uploadurl;

    public static List<UstawienieString> ustawienia;


    //Tworzymy obiekty z wartosciamy domyslnymi
    private static void utworzUstawienia() {
        ustawienia = new ArrayList<>(20);
        nazwaurzadzenia = new UstawienieString(android.os.Build.MODEL, null, "nazwaurzadzenia", MainActivity.activity.nazwaurzadzenia);
        folderzmapami = new UstawienieString("/", null, "folderzmapami", MainActivity.activity.folderzmapami);
        atlas = new UstawienieString("", null, "atlas", MainActivity.activity.atlasedittext);
        downloadurl = new UstawienieString(Stale.DOWNLOADURL, null, "downloadurl", MainActivity.activity.downloadurl);
        downloadpass = new UstawienieString(Stale.DOWNLOADPASS, null, "downloadpass", MainActivity.activity.downloadpass);
        downloaduser = new UstawienieString(Stale.DOWNLOADUSER, null, "downloaduser", MainActivity.activity.downloaduser);
        uploadurl = new UstawienieString(Stale.DOWNLOADURL, null, "uploadurl", MainActivity.activity.uploadurl);
        ustawienia.add(nazwaurzadzenia);
        ustawienia.add(folderzmapami);
        ustawienia.add(atlas);
        ustawienia.add(downloadpass);
        ustawienia.add(downloadurl);
        ustawienia.add(downloaduser);
        ustawienia.add(uploadurl);
    }

    //Zapisujemy do pamieci wartosci domyslne jesli nie ma w ogole
    private static void wypelnijDomyslneJesliNieMa() {
        for(int i = 0; i < ustawienia.size(); i++) {
            ustawienia.get(i).zapiszDoUstawienDomyslnaJesliNieMa();
        }
    }

    //Wypelniamy wartosci z ustawien z telefonu
    private static void wypelnijWartosc() {
        for(int i = 0; i < ustawienia.size(); i++) {
            ustawienia.get(i).wczytajZUstawien();
        }
    }

    //Inicjujemy ustawienia, wczytujemy z pamieci, ewentlanie ustawiajcac wartosci domyslna
    public static void zainicjujUstawienia() {
        utworzUstawienia();
        wypelnijDomyslneJesliNieMa();
        wypelnijWartosc();
    }

    //Wczytujemy kazde ustawienie do odpowiedniego pola w widoku
    public static void wczytajDoPol() {
        for(int i = 0; i < ustawienia.size(); i++) {
            ustawienia.get(i).uzupelnijPoleWOpcjach();
        }
    }

    //Pobieramy kazde ustawienie z widoku i zapisujemy do telefonu
    public static void zapiszDoUstawien() {
        for(int i = 0; i < ustawienia.size(); i++) {
            ustawienia.get(i).zapiszDoUstawien();
        }
    }
}
