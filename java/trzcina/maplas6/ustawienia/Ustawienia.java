package trzcina.maplas6.ustawienia;

import trzcina.maplas6.MainActivity;

public class Ustawienia {

    //Kazde ustawienie to osobny obiekt
    public static UstawienieString nazwaurzadzenia;
    public static UstawienieString folderzmapami;
    public static UstawienieString atlas;


    //Tworzymy obiekty z wartosciamy domyslnymi
    private static void utworzUstawienia() {
        nazwaurzadzenia = new UstawienieString(android.os.Build.MODEL, null, "nazwaurzadzenia", MainActivity.activity.nazwaurzadzenia);
        folderzmapami = new UstawienieString("/", null, "folderzmapami", MainActivity.activity.folderzmapami);
        atlas = new UstawienieString("", null, "atlas", MainActivity.activity.atlasedittext);
    }

    //Zapisujemy do pamieci wartosci domyslne jesli nie ma w ogole
    private static void wypelnijDomyslneJesliNieMa() {
        nazwaurzadzenia.zapiszDoUstawienDomyslnaJesliNieMa();
        folderzmapami.zapiszDoUstawienDomyslnaJesliNieMa();
        atlas.zapiszDoUstawienDomyslnaJesliNieMa();
    }

    //Wypelniamy wartosci z ustawien z telefonu
    private static void wypelnijWartosc() {
        nazwaurzadzenia.wczytajZUstawien();
        folderzmapami.wczytajZUstawien();
        atlas.wczytajZUstawien();
    }

    //Inicjujemy ustawienia, wczytujemy z pamieci, ewentlanie ustawiajcac wartosci domyslna
    public static void zainicjujUstawienia() {
        utworzUstawienia();
        wypelnijDomyslneJesliNieMa();
        wypelnijWartosc();
    }

    //Wczytujemy kazde ustawienie do odpowiedniego pola w widoku
    public static void wczytajDoPol() {
        nazwaurzadzenia.uzupelnijPoleWOpcjach();
        folderzmapami.uzupelnijPoleWOpcjach();
        atlas.uzupelnijPoleWOpcjach();
    }

    //Pobieramy kazde ustawienie z widoku i zapisujemy do telefonu
    public static void zapiszDoUstawien() {
        nazwaurzadzenia.zapiszDoUstawien();
        folderzmapami.zapiszDoUstawien();
        atlas.zapiszDoUstawien();
    }
}
