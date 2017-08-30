package trzcina.maplas6.ustawienia;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import trzcina.maplas6.MainActivity;

/**
 * Created by piotr.trzcinski on 30.08.2017.
 */

public class UstawienieIntSeekBar implements Ustawienie {

    public int wartoscdomyslna;
    public int wartosc;
    public String nazwaustawienia;
    public TextView textviewwopjach;
    public SeekBar progressbarwopcjach;
    public int wartoscmin;
    public int wartoscmax;
    public float mnoznik;
    public boolean zaokraglij;

    public UstawienieIntSeekBar(int wartoscdomyslna, final int wartosc, String nazwaustawienia, TextView textviewwopjach, SeekBar progressbarwopcjach, final int wartoscmin, int wartoscmax, final float mnoznik, final boolean zaokraglij) {
        this.wartoscdomyslna = wartoscdomyslna;
        this.wartosc = wartosc;
        this.nazwaustawienia = nazwaustawienia;
        this.textviewwopjach = textviewwopjach;
        this.progressbarwopcjach = progressbarwopcjach;
        this.wartoscmin = wartoscmin;
        this.wartoscmax = wartoscmax;
        this.mnoznik = mnoznik;
        this.zaokraglij = zaokraglij;
        progressbarwopcjach.setMax(wartoscmax - wartoscmin);
        progressbarwopcjach.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float wartosc = ((wartoscmin + i) * mnoznik);
                if(zaokraglij == false) {
                    UstawienieIntSeekBar.this.textviewwopjach.setText(String.valueOf(wartosc));
                } else {
                    UstawienieIntSeekBar.this.textviewwopjach.setText(String.valueOf(Math.round(wartosc)));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //Sprawdzamy czy ustawienie jest zapisane w telefonie w ogole
    public boolean sprawdzCzyJestZapisane() {
        SharedPreferences sharedPref = MainActivity.activity.getSharedPreferences("UST", Context.MODE_PRIVATE);
        int odczytane = sharedPref.getInt(nazwaustawienia, wartoscmin - 1);
        if(odczytane == wartoscmin - 1) {
            return false;
        } else {
            return true;
        }
    }

    //Wczytujemy ustawienie z pamieci telefonu, ewentualnie wartosc domyslna
    public void wczytajZUstawien() {
        if(sprawdzCzyJestZapisane() == true) {
            SharedPreferences sharedPref = MainActivity.activity.getSharedPreferences("UST", Context.MODE_PRIVATE);
            wartosc = sharedPref.getInt(nazwaustawienia, wartoscmin - 1);
        } else {
            wartosc = wartoscdomyslna;
        }
    }

    //Uzupelnia wartosc w widoku
    public void uzupelnijPoleWOpcjach() {
        wczytajZUstawien();
        float war = (wartoscmin + wartosc) * mnoznik;
        if(zaokraglij == false) {
            UstawienieIntSeekBar.this.textviewwopjach.setText(String.valueOf(war));
        } else {
            UstawienieIntSeekBar.this.textviewwopjach.setText(String.valueOf(Math.round(war)));
        }
        progressbarwopcjach.setProgress(wartosc);
    }

    //Uzupelnia wartosc w widoku
    public void uzupelnijPoleWOpcjachZDomyslnych() {
        wczytajZUstawien();
        float war = ((wartoscmin + wartoscdomyslna) * mnoznik);
        if(zaokraglij == false) {
            UstawienieIntSeekBar.this.textviewwopjach.setText(String.valueOf(war));
        } else {
            UstawienieIntSeekBar.this.textviewwopjach.setText(String.valueOf(Math.round(war)));
        }
        progressbarwopcjach.setProgress(wartoscdomyslna);
    }

    public void zapiszDoUstawien() {
        int obecnawartoscwpolu = ((ProgressBar)progressbarwopcjach).getProgress();
        SharedPreferences sharedPref = MainActivity.activity.getSharedPreferences("UST", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(nazwaustawienia, obecnawartoscwpolu);
        wartosc = obecnawartoscwpolu;
        editor.commit();
    }

    public void zapiszWartoscDoUstawien() {
        SharedPreferences sharedPref = MainActivity.activity.getSharedPreferences("UST", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(nazwaustawienia, wartosc);
        editor.commit();
    }

    //Zapisuje wartosc domyslna jesli nie ma zadenj
    public void zapiszDoUstawienDomyslnaJesliNieMa() {
        if(sprawdzCzyJestZapisane() == false) {
            SharedPreferences sharedPref = MainActivity.activity.getSharedPreferences("UST", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(nazwaustawienia, wartoscdomyslna);
            wartosc = wartoscdomyslna;
            editor.commit();
        }
    }

}
