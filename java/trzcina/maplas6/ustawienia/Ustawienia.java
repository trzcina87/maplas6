package trzcina.maplas6.ustawienia;

import trzcina.maplas6.MainActivity;

/**
 * Created by piotr.trzcinski on 22.08.2017.
 */

public class Ustawienia {

    public static UstawienieString nazwaurzadzenia;
    public static UstawienieString folderzmapami;
    public static UstawienieString atlas;

    private static void utworzUstawienia() {
        nazwaurzadzenia = new UstawienieString(android.os.Build.MODEL, null, "nazwaurzadzenia", MainActivity.activity.nazwaurzadzenia);
        folderzmapami = new UstawienieString("/", null, "folderzmapami", MainActivity.activity.folderzmapami);
        atlas = new UstawienieString("", null, "atlas", MainActivity.activity.atlasedittext);
    }

    private static void wypelnijDomyslneJesliNieMa() {
        nazwaurzadzenia.zapiszDoUstawienDomyslnaJesliNieMa();
        folderzmapami.zapiszDoUstawienDomyslnaJesliNieMa();
        atlas.zapiszDoUstawienDomyslnaJesliNieMa();
    }

    private static void wypelnijWartosc() {
        nazwaurzadzenia.wczytajZUstawien();
        folderzmapami.wczytajZUstawien();
        atlas.wczytajZUstawien();
    }

    public static void zainicjujUstawienia() {
        utworzUstawienia();
        wypelnijDomyslneJesliNieMa();
        wypelnijWartosc();
    }

    public static void wczytajDoPol() {
        nazwaurzadzenia.uzupelnijPoleWOpcjach();
        folderzmapami.uzupelnijPoleWOpcjach();
        atlas.uzupelnijPoleWOpcjach();
    }

    public static void zapiszDoUstawien() {
        nazwaurzadzenia.zapiszDoUstawien();
        folderzmapami.zapiszDoUstawien();
        atlas.zapiszDoUstawien();
    }
}
