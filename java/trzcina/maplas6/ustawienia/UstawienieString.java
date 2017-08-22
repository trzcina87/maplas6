package trzcina.maplas6.ustawienia;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;

import trzcina.maplas6.MainActivity;

@SuppressWarnings("PointlessBooleanExpression")
public class UstawienieString {

    public String wartoscdomyslna;
    public String wartosc;
    public String nazwaustawienia;
    public View polewopjach;

    public UstawienieString(String wartoscdomyslna, String wartosc, String nazwaustawienia, View polewopjach) {
        this.wartoscdomyslna = wartoscdomyslna;
        this.wartosc = wartosc;
        this.nazwaustawienia = nazwaustawienia;
        this.polewopjach = polewopjach;
    }

    public boolean sprawdzCzyJestZapisane() {
        SharedPreferences sharedPref = MainActivity.activity.getSharedPreferences("UST", Context.MODE_PRIVATE);
        String odczytane = sharedPref.getString(nazwaustawienia, null);
        if(odczytane == null) {
            return false;
        } else {
            return true;
        }
    }

    public void wczytajZUstawien() {
        if(sprawdzCzyJestZapisane() == true) {
            SharedPreferences sharedPref = MainActivity.activity.getSharedPreferences("UST", Context.MODE_PRIVATE);
            wartosc = sharedPref.getString(nazwaustawienia, null);
        } else {
            wartosc = wartoscdomyslna;
        }
    }

    public void uzupelnijPoleWOpcjach() {
        wczytajZUstawien();
        if(polewopjach instanceof EditText) {
            ((EditText) polewopjach).setText(wartosc);
        }
    }

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
