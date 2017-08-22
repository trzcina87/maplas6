package trzcina.maplas6;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import trzcina.maplas6.atlasy.Atlasy;
import trzcina.maplas6.pomoc.Komunikaty;
import trzcina.maplas6.pomoc.ObslugaMenu;
import trzcina.maplas6.pomoc.OpcjePagerAdapter;
import trzcina.maplas6.pomoc.Painty;
import trzcina.maplas6.pomoc.Stale;
import trzcina.maplas6.pomoc.Uprawnienia;
import trzcina.maplas6.ustawienia.Ustawienia;

@SuppressWarnings({"PointlessBooleanExpression", "NullableProblems", "StatementWithEmptyBody"})
public class MainActivity extends AppCompatActivity {

    public static volatile MainActivity activity;

    //Layouty
    private RelativeLayout przygotowanielayout;
    private RelativeLayout maplayout;
    public LinearLayout opcjemapy;
    public LinearLayout opcjepodstawowelayout;
    public LinearLayout opcjezaawansowanelayout;
    public LinearLayout opcjelayout;
    public LinearLayout contentviewlayout;

    //Widoki
    public EditText nazwaurzadzenia;
    public EditText folderzmapami;
    public EditText atlasedittext;
    public LinearLayout spismap;
    public Button opcjeanuluj;
    public Button opcjezapisz;
    private TextView textinfoprzygotowanie;
    private ProgressBar progressbarprzygotowanie;
    private TextView luxtextview;
    private ImageView menuimageview;
    public MainSurface surface;

    //Pomocnicze
    private LayoutInflater inflater;

    //Dla danego id zasobu (w res/layout) zwraca widok
    private LinearLayout znajdzLinearLayout(int zasob) {
        LinearLayout layouttmp = (LinearLayout) inflater.inflate(zasob, null);
        layouttmp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        return layouttmp;
    }

    //Dla danego id zasobu (w res/layout) zwraca widok
    private RelativeLayout znajdzRelativeLayout(int zasob) {
        RelativeLayout layouttmp = (RelativeLayout) inflater.inflate(zasob, null);
        layouttmp.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        return layouttmp;
    }

    //Wyszukuje wszystkie layouty uzywane w aplikacji
    private void znajdzLayouty() {
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        przygotowanielayout = znajdzRelativeLayout(R.layout.przygotowanielayout);
        maplayout = znajdzRelativeLayout(R.layout.maplayout);
        opcjemapy = znajdzLinearLayout(R.layout.opcjemapylayout);
        opcjepodstawowelayout = znajdzLinearLayout(R.layout.opcjepodstawowelayout);
        opcjezaawansowanelayout = znajdzLinearLayout(R.layout.opcjezaawansowanelayout);
        opcjelayout = znajdzLinearLayout(R.layout.opcjelayout);
        contentviewlayout = znajdzLinearLayout(R.layout.contentviewlayout);
    }

    //Wyszukuje wszystkie widoki uzywane w aplikacji
    private void znajdzWidoki() {
        textinfoprzygotowanie = (TextView) przygotowanielayout.findViewById(R.id.textinfo);
        progressbarprzygotowanie = (ProgressBar) przygotowanielayout.findViewById(R.id.progressbar);
        luxtextview = (TextView) maplayout.findViewById(R.id.luxtextview);
        surface = (MainSurface) maplayout.findViewById(R.id.surface);
        menuimageview = (ImageView)maplayout.findViewById(R.id.menuimageview);
        opcjeanuluj = (Button)opcjelayout.findViewById(R.id.anulujbutton);
        opcjezapisz = (Button)opcjelayout.findViewById(R.id.zapiszbutton);
        nazwaurzadzenia = (EditText)opcjepodstawowelayout.findViewById(R.id.nazwaurzadzeniaedittext);
        folderzmapami = (EditText)opcjepodstawowelayout.findViewById(R.id.foldermapyedittext);
        atlasedittext = (EditText)opcjemapy.findViewById(R.id.atlasedittext);
        spismap = (LinearLayout)opcjemapy.findViewById(R.id.spismap);
    }

    //Czysci wszystkie widoki z spisie map tak by byl pusty
    public void czyscSpisMap() {
        spismap.removeAllViews();
    }

    //Dodaje pozycje do spisu map
    public void dodajPozycjeDoSpisuMap(String nazwa) {
        Button button = new Button(MainActivity.activity);
        button.setTag(nazwa);
        button.setText(nazwa);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atlasedittext.setText((String)view.getTag());
            }
        });
        spismap.addView(button);
    }

    //Ustawia obsluge zakladek z opcjach
    private void ustawPager() {
        OpcjePagerAdapter opcjepageradapter = new OpcjePagerAdapter();
        ViewPager viewpager = (ViewPager) opcjelayout.findViewById(R.id.kontener);
        viewpager.setAdapter(opcjepageradapter);
        TabLayout tabLayout = (TabLayout) opcjelayout.findViewById(R.id.taby);
        tabLayout.setupWithViewPager(viewpager);
    }

    //Przypisuje akcje do przyciskow
    private void przypiszAkcjeDoWidokow() {
        opcjeanuluj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zamknijOpcjeIWrocDoMapy();
            }
        });
        opcjezapisz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zamknijOpcjeIWrocDoMapy();
                Ustawienia.zapiszDoUstawien();
            }
        });
    }

    //Ustawia text dla podanego ImageView w UI watku
    private void ustawTextView(final TextView textview, final String komunikat) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textview.setText(komunikat);
            }
        });
    }

    //Ustawia progress dla podanego Bara w UI watku
    private void ustawProgressBar(final ProgressBar progressbar, final int poziom) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressbar.setProgress(poziom);
            }
        });
    }

    //Ustawia info o postepie uruchomienia aplikacji
    public void ustawInfoPrzygotowanie(String komunikat) {
        ustawTextView(textinfoprzygotowanie, komunikat);
    }

    //Ustawia postep bara podczas uruchamiania aplikacji
    public void ustawProgressPrzygotowanie(int poziom) {
        ustawProgressBar(progressbarprzygotowanie, poziom);
    }

    //Ustawia wartosc w widoku odpowiedzialnym za swiatlomierz
    public void ustawLux(int lux) {
        ustawTextView(luxtextview, lux + "lx ");
    }

    //Konfigruje Menu glowne programu
    private void utworzMenu() {
        final PopupMenu menuglowne = new PopupMenu(MainActivity.this, menuimageview);
        menuglowne.setOnMenuItemClickListener(new ObslugaMenu());
        menuglowne.inflate(R.menu.menu_main);
        maplayout.findViewById(R.id.menuimageview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*menuglowne.getMenu().findItem(R.id.gpsitem).setChecked(gpswlaczony);
                menuglowne.getMenu().findItem(R.id.internetitem).setChecked(internetwlaczony);
                menuglowne.getMenu().findItem(R.id.precyzjagps).setChecked(precyzjagps);
                menuglowne.getMenu().findItem(R.id.dzwiekiitem).setChecked(dzwieki);
                menuglowne.getMenu().findItem(R.id.drugaosobaitem).setChecked(false);*/
                menuglowne.show();
            }
        });
    }

    //Pokazuje konunikat w watku UI
    public void pokazToast(final String komunikat) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), komunikat, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Uruchamia glowny service aplikacji
    private void wysartujService() {
        startService(new Intent(this, AppService.class));
    }

    //Konczy dzialanie programu
    public void zakonczCalaAplikacje() {
        stopService(new Intent(MainActivity.this, AppService.class));
        finish();
    }

    //Wyswietla potwierdzenie wyjscia z programu
    private void wyswietlPotwierdzenieZamkniecia() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(Komunikaty.CZYWYJSCTYTUL).setMessage(Komunikaty.CZYWYJSCTRESC).setPositiveButton("Tak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
             zakonczCalaAplikacje();
            }
        }).setNegativeButton("Nie", null).show();
    }

    //Zamyka opcje i wraca do widoku mapy
    private void zamknijOpcjeIWrocDoMapy() {
        contentviewlayout.removeAllViews();
        contentviewlayout.addView(maplayout);
        AppService.widok = Stale.WIDOKMAPA;
    }

    //Zamyka widok startowy i uruchamia widok mapy
    public void zakonczPrzygotowanie() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contentviewlayout.removeAllViews();
                contentviewlayout.addView(maplayout, 0);
                AppService.widok = Stale.WIDOKMAPA;
            }
        });
    }

    //Ustawia parametry ekranu i odpowiedni widok w zaleznosci z jakiego poziomu Activity startuje
    private void ustawEkran() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().hide();
        int widok = AppService.service.widok;
        setContentView(contentviewlayout);
        if(widok == Stale.WIDOKBRAK) {
            AppService.service.widok = Stale.WIDOKPRZYGOTOWANIE;
            contentviewlayout.removeAllViews();
            contentviewlayout.addView(przygotowanielayout);
        }
        if(widok == Stale.WIDOKPRZYGOTOWANIE) {
            contentviewlayout.removeAllViews();
            contentviewlayout.addView(przygotowanielayout);
        }
        if(widok == Stale.WIDOKMAPA) {
            contentviewlayout.removeAllViews();
            contentviewlayout.addView(maplayout);
        }
        if(widok == Stale.WIDOKOPCJI) {
            pokazOpcjeView();
        }
    }

    //Przechodzi do widoku opcji i wypelnia go
    public void pokazOpcjeView() {
        AppService.service.widok = Stale.WIDOKOPCJI;
        contentviewlayout.removeAllViews();
        contentviewlayout.addView(opcjelayout, 0);
        Ustawienia.wczytajDoPol();
        Atlasy.wczytajDoPol();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        //Obsluga elementow graficznych
        znajdzLayouty();
        ustawEkran();
        znajdzWidoki();
        ustawPager();
        przypiszAkcjeDoWidokow();
        utworzMenu();
        Painty.inicjujPainty();

        //Obsluga uprawnien i ustawien
        Uprawnienia.zainicjujUprawnienia();
        Ustawienia.zainicjujUstawienia();

        //Jesli sa uprawniania bez pytania usera to uruchamiamy aplikacje dalej
        if(Uprawnienia.czyNadane() == true) {
            wysartujService();
        }
    }

    //Obsluga uprawnien, aplikcji nie ma sensu uruchamiac jesli uzytkownik nie dal uprawnien
    @Override
    public void onRequestPermissionsResult(int kod, String[] uprawnienia, int[] odpowiedzi) {
        super.onRequestPermissionsResult(kod, uprawnienia, odpowiedzi);

        //Sprawdzamy kazde uprawnienie czy zostalo nadane
        for(int i = 0; i < uprawnienia.length; i++) {
            if(uprawnienia[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                if(odpowiedzi[i] == PackageManager.PERMISSION_GRANTED) {
                    Uprawnienia.odczyt = true;
                }
            }
            if(uprawnienia[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if(odpowiedzi[i] == PackageManager.PERMISSION_GRANTED) {
                    Uprawnienia.zapis = true;
                }
            }
            if(uprawnienia[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if(odpowiedzi[i] == PackageManager.PERMISSION_GRANTED) {
                    Uprawnienia.lokalizacja = true;
                }
            }
        }

        //Jesli sa uprawniania do aplikacja dziala dalej, jesli nie to koniec
        if(Uprawnienia.czyNadane() == true) {
            wysartujService();
        } else {
            pokazToast(Komunikaty.BRAKUPRAWNINEN);
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //Reakcja na przycisk wstecz, albo zamkniecie aplikacji albo wyjscie do widoku mapy
    @Override
    public void onBackPressed() {
        int widok = AppService.widok;
        if(widok == Stale.WIDOKMAPA) {
            wyswietlPotwierdzenieZamkniecia();
        }
        if(widok == Stale.WIDOKOPCJI) {
            zamknijOpcjeIWrocDoMapy();
        }
    }
}
