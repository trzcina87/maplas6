package trzcina.maplas6.pomoc;

public class Stale {

    //Rozne ustawienia programu
    public static String SCIEZKAMAPLAS = "/sdcard/MapLas6/";
    public static String FOLDERKOSZ = "Kosz";
    public static long CZASRETENCJIPLIKOW = 7L*24L*60L*60L*1000L;
    public static String ENTER = "\n";

    //To w jakim widoku aktualnie jestesmy w aplikacji
    public static int WIDOKBRAK = 0;
    public static int WIDOKPRZYGOTOWANIE = 1;
    public static int WIDOKMAPA = 2;
    public static int WIDOKOPCJI = 3;
    public static int WIDOKPLIKOW = 4;

    //Stale majace wplyw na rysowanie
    public static int SZEROKOSCSRODKOWEGOKOLA = 3;

    //Stan atlasu
    public static int ATLASNOWY = 1;
    public static int ATLASROBIE = 2;
    public static int ATLASGOTOWY = 3;
    public static int ATLASBLAD = 4;

    //Stan pliku
    public static int PLIKNOWY = 1;
    public static int PLIKROBIE = 2;
    public static int PLIKGOTOWY = 3;
    public static int PLIKBLAD = 4;

    //Suffixy plikow Cache
    public static String SUFFIXCACHEDANE = "dane.v1.cache";
    public static String SUFFIXCACHETAB = "tab.v1.cache";

    //Obsluga HTTP
    public static String DOWNLOADURL = "http://trzcina.d2.pl/puszcza/gpx2016/download.php";
    public static String UPLOADURL = "http://trzcina.d2.pl/puszcza/gpx2016/upload.php";
    public static String DOWNLOADUSER = "readonly";
    public static String DOWNLOADPASS = "ReadOnly2017gpx";

}
