package trzcina.maplas6.lokalizacja;

import android.location.Location;

public class PunktNaMapie {

    public float wspx;
    public float wspy;
    public String nazwa;
    public String opis;

    public PunktNaMapie(float wspx, float wspy, String nazwa, String opis) {
        this.wspx = wspx;
        this.wspy = wspy;
        this.nazwa = nazwa;
        this.opis = opis;
    }

    public float zmierzDystans(double dowspx, double dowspy) {
        Location lok1 = new Location("dummyprovider");
        Location lok2 = new Location("dummyprovider");
        lok1.setLongitude(wspx);
        lok1.setLatitude(wspy);
        lok2.setLongitude(dowspx);
        lok2.setLatitude(dowspy);
        return lok1.distanceTo(lok2);
    }
}
