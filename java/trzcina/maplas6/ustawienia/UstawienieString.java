package trzcina.maplas6.ustawienia;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;

import trzcina.maplas6.MainActivity;

@SuppressWarnings("PointlessBooleanExpression")
public class UstawienieString {

    public String wartoscdomyslna;      //Wartosc domyslna konfiguracji
    public String wartosc;              //Wartosc obecna konfiguracji
    public String nazwaustawienia;      //Nazwa pod ktora zapisane bedzie ustawienie
    public View polewopjach;            //Widok odpowiedziany za ustawienie

    //Konstruktor
    public UstawienieString(String wartoscdomyslna, String wartosc, String nazwaustawienia, View polewopjach) {
        this.wartoscdomyslna = wartoscdomyslna;
        this.wartosc = wartosc;
        this.nazwaustawienia = nazwaustawienia;
        this.polewopjach = polewopjach;
    }

    //Sprawdzamy czy ustawienie jest zapisane w telefonie w ogole
    public boolean sprawdzCzyJestZapisane() {
        SharedPreferences sharedPref = MainActivity.activity.getSharedPreferences("UST", Context.MODE_PRIVATE);
        String odczytane = sharedPref.getString(nazwaustawienia, null);
        if(odczytane == null) {
            return false;
        } else {
            return true;
        }
    }

    //Wczytujemy ustawienie z pamieci telefonu, ewentualnie wartosc domyslna
    public void wczytajZUstawien() {
        if(sprawdzCzyJestZapisane() == true) {
            SharedPreferences sharedPref = MainActivity.activity.getSharedPreferences("UST", Context.MODE_PRIVATE);
            wartosc = sharedPref.getString(nazwaustawienia, null);
        } else {
            wartosc = wartoscdomyslna;
        }
    }

    //Uzupelnia wartosc w widoku
    public void uzupelnijPoleWOpcjach() {
        wczytajZUstawien();
        if(polewopjach instanceof EditText) {
            ((EditText) polewopjach).setText(wartosc);
        }
    }

    //Uzupelnia wartosc w widoku
    public void uzupelnijPoleWOpcjachZDomyslnych() {
        if(polewopjach instanceof EditText) {
            ((EditText) polewopjach).setText(wartoscdomyslna);
        }
    }

    //Zapisuje wartosc z widoku do telefonu
    public void zapiszDoUstawien() {
        if(polewopjach instanceof EditText) {
            String obecnawartoscwpolu = ((EditText) polewopjach).getText().toString();
            SharedPreferences sharedPref = MainActivity.activity.getSharedPreferences("UST", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(nazwaustawienia, obecnawartoscwpolu);
            wartosc = obecnawartoscwpolu;
            editor.commit();
        }
    }

    public void zapiszWartoscDoUstawien() {
        SharedPreferences sharedPref = MainActivity.activity.getSharedPreferences("UST", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(nazwaustawienia, wartosc);
        editor.commit();
    }

    //Zapisuje wartosc domyslna jesli nie ma zadenj
    public void zapiszDoUstawienDomyslnaJesliNieMa() {
        if(sprawdzCzyJestZapisane() == false) {
            SharedPreferences sharedPref = MainActivity.activity.getSharedPreferences("UST", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(nazwaustawienia, wartoscdomyslna);
            wartosc = wartoscdomyslna;
            editor.commit();
        }
    }
}
