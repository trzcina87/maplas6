package trzcina.maplas6.lokalizacja;

import android.graphics.Rect;
import android.location.Location;

import trzcina.maplas6.AppService;
import trzcina.maplas6.pomoc.Painty;

public class PunktNaMapie {

    public float wspx;
    public float wspy;
    public String nazwa;
    public String opis;
    public Rect rectnazwa;
    public Rect rectopis;
    public Location lokalizacja;
    public Location lokalizacjamierzona;

    public PunktNaMapie(float wspx, float wspy, String nazwa, String opis) {
        this.wspx = wspx;
        this.wspy = wspy;
        this.nazwa = nazwa;
        this.opis = opis;
        rectnazwa = new Rect();
        rectopis = new Rect();
        Painty.paintbialytekst.getTextBounds(nazwa, 0, nazwa.length(), rectnazwa);
        Painty.paintbialytekst.getTextBounds(opis, 0, opis.length(), rectopis);
        lokalizacja = new Location("dummyprovider");
        lokalizacja.setLongitude(wspx);
        lokalizacja.setLatitude(wspy);
        lokalizacjamierzona = new Location("dummyprovider");
    }

    public float zmierzDystans(double dowspx, double dowspy) {
        lokalizacjamierzona.setLongitude(dowspx);
        lokalizacjamierzona.setLatitude(dowspy);
        return lokalizacja.distanceTo(lokalizacjamierzona);
    }
}
