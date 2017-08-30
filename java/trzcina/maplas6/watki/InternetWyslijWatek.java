package trzcina.maplas6.watki;

import trzcina.maplas6.AppService;
import trzcina.maplas6.lokalizacja.GPXTrasaLogger;
import trzcina.maplas6.pomoc.HTTP;
import trzcina.maplas6.pomoc.Rozne;
import trzcina.maplas6.ustawienia.Ustawienia;

@SuppressWarnings("PointlessBooleanExpression")
public class InternetWyslijWatek extends Thread {

    public volatile boolean zakoncz;
    public GPXTrasaLogger obecnatrasa;
    public int iloscwyslanych;
    public long ostatniawysylka;
    public String nazwa;

    public InternetWyslijWatek() {
        zakoncz = false;
        obecnatrasa = null;
        iloscwyslanych = 0;
        ostatniawysylka = 0;
        nazwa = null;
    }

    private void wyslijPunkty() {
        int iledowyslania = Math.min(100, obecnatrasa.iloscpunktow - iloscwyslanych);
        String urlstring = Ustawienia.wyslijurl.wartosc + "?nazwa=" + nazwa + "&ilosc=" + iledowyslania + "&";
        for (int i = 0; i < iledowyslania; i++) {
            String czasstart = String.valueOf(Math.round(obecnatrasa.czasstart / 1000F));
            urlstring = urlstring + "s" + i + "=" + czasstart + "&";
            urlstring = urlstring + "c" + i + "=" + Math.round(System.currentTimeMillis() / 1000F) + "&";
            urlstring = urlstring + "x" + i + "=" + obecnatrasa.lista[iloscwyslanych + i].wspx + "&";
            urlstring = urlstring + "y" + i + "=" + obecnatrasa.lista[iloscwyslanych + i].wspy + "&";
            String odp = HTTP.sciagnijPlik(urlstring, Ustawienia.downloaduser.wartosc, Ustawienia.downloadpass.wartosc, null);
            if (odp != null) {
                if (odp.startsWith("MAPLASOK")) {
                    iloscwyslanych = iloscwyslanych + iledowyslania;
                    ostatniawysylka = System.currentTimeMillis();
                    if(iloscwyslanych + 100 < obecnatrasa.iloscpunktow) {
                        ostatniawysylka = ostatniawysylka - 55 * 1000;
                    }
                }
            }
        }
    }

    public void run() {
        while(zakoncz == false) {
            if (AppService.service.obecnatrasa != null) {
                if (AppService.service.obecnatrasa != obecnatrasa) {
                    obecnatrasa = AppService.service.obecnatrasa;
                    iloscwyslanych = 0;
                    ostatniawysylka = 0;
                    nazwa = Ustawienia.nazwaurzadzenia.wartosc;
                }
            }
            if (AppService.service.internetwyslij == true) {
                if (obecnatrasa != null) {
                    if (System.currentTimeMillis() > ostatniawysylka + 60 * 1000) {
                        if (iloscwyslanych < obecnatrasa.iloscpunktow) {
                            wyslijPunkty();
                        } else {
                            ostatniawysylka = System.currentTimeMillis();
                        }
                    }
                }
            }
            AppService.service.odswiezUI();
            Rozne.czekaj(1000);
        }
    }
}
