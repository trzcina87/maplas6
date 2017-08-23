package trzcina.maplas6;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import trzcina.maplas6.atlasy.Atlasy;
import trzcina.maplas6.lokalizacja.PlikGPX;
import trzcina.maplas6.lokalizacja.PlikiGPX;
import trzcina.maplas6.pomoc.HTTP;
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
    public LinearLayout pokazplikilayout;

    //Widoki
    public EditText nazwaurzadzenia;
    public EditText folderzmapami;
    public EditText atlasedittext;
    public EditText downloadurl;
    public EditText downloaduser;
    public EditText downloadpass;
    public EditText uploadurl;
    public LinearLayout spismap;
    public LinearLayout spisplikow;
    public Button opcjeanuluj;
    public Button opcjezapisz;
    private TextView textinfoprzygotowanie;
    private ProgressBar progressbarprzygotowanie;
    private TextView luxtextview;
    private TextView gpstextview;
    private ImageView menuimageview;
    private ImageView zmienmageimageview;
    private ImageView pomniejszimageview;
    private ImageView powiekszimageview;
    private ImageView pokazplikiimageview;
    public MainSurface surface;

    //PokazPliki
    private Button pokazplikianuluj;
    private Button wyczyscplikianuluj;
    private Button pokazplikizapisz;
    private Button pokazplikiusun;
    private Button pokazplikidownload;
    private Button pokazplikiupload;

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
        pokazplikilayout = znajdzLinearLayout(R.layout.pokazpliki);
    }

    //Wyszukuje wszystkie widoki uzywane w aplikacji
    private void znajdzWidoki() {
        textinfoprzygotowanie = (TextView) przygotowanielayout.findViewById(R.id.textinfo);
        progressbarprzygotowanie = (ProgressBar) przygotowanielayout.findViewById(R.id.progressbar);
        luxtextview = (TextView) maplayout.findViewById(R.id.luxtextview);
        surface = (MainSurface) maplayout.findViewById(R.id.surface);
        menuimageview = (ImageView)maplayout.findViewById(R.id.menuimageview);
        zmienmageimageview = (ImageView)maplayout.findViewById(R.id.zmienmape);
        pomniejszimageview = (ImageView)maplayout.findViewById(R.id.pomniejszimageview);
        powiekszimageview = (ImageView)maplayout.findViewById(R.id.powiekszimageview);
        opcjeanuluj = (Button)opcjelayout.findViewById(R.id.anulujbutton);
        opcjezapisz = (Button)opcjelayout.findViewById(R.id.zapiszbutton);
        nazwaurzadzenia = (EditText)opcjepodstawowelayout.findViewById(R.id.nazwaurzadzeniaedittext);
        folderzmapami = (EditText)opcjezaawansowanelayout.findViewById(R.id.foldermapyedittext);
        atlasedittext = (EditText)opcjemapy.findViewById(R.id.atlasedittext);
        spismap = (LinearLayout)opcjemapy.findViewById(R.id.spismap);
        spisplikow = (LinearLayout)pokazplikilayout.findViewById(R.id.spisplikow);
        gpstextview = (TextView)maplayout.findViewById(R.id.gpstextview);
        pokazplikianuluj = (Button)pokazplikilayout.findViewById(R.id.pokazplikianuluj);
        wyczyscplikianuluj = (Button)pokazplikilayout.findViewById(R.id.pokazplikiwyczysc);
        pokazplikizapisz = (Button)pokazplikilayout.findViewById(R.id.pokazplikizapisz);
        pokazplikiusun = (Button)pokazplikilayout.findViewById(R.id.pokazplikiusun);
        pokazplikidownload = (Button)pokazplikilayout.findViewById(R.id.pokazplikidownload);
        pokazplikiupload = (Button)pokazplikilayout.findViewById(R.id.pokazplikiupload);
        pokazplikiimageview = (ImageView)maplayout.findViewById(R.id.pokazplikiimageview);
        downloadurl = (EditText)opcjezaawansowanelayout.findViewById(R.id.downloadurl);
        downloaduser = (EditText)opcjezaawansowanelayout.findViewById(R.id.downloaduser);
        downloadpass = (EditText)opcjezaawansowanelayout.findViewById(R.id.downloadpass);
        uploadurl = (EditText)opcjezaawansowanelayout.findViewById(R.id.uploadurl);
    }

    //Czysci wszystkie widoki z spisie map tak by byl pusty
    public void czyscSpisMap() {
        spismap.removeAllViews();
    }

    //Czysci wszystkie widoki z widoku plikow
    public void czyscSpisPlikow() {
        spisplikow.removeAllViews();
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

    private TextView utworzTextViewDlaSpisuPlikow(PlikGPX plik, final CheckBox checkbox) {
        TextView textgpx = new TextView(getApplicationContext());
        textgpx.setTextColor(Color.BLACK);
        textgpx.setText(plik.nazwa);
        textgpx.setSingleLine(false);
        textgpx.setMinHeight((int)(50 * getResources().getDisplayMetrics().density));
        textgpx.setMaxLines(3);
        textgpx.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textgpx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean zaznaczony = ((CheckBox)checkbox).isChecked();
                ((CheckBox)checkbox).setChecked(!zaznaczony);
            }
        });
        return textgpx;
    }

    private CheckBox utworzCheckBoxDlaSpisuPlikow(PlikGPX plik) {
        CheckBox checkbox = new CheckBox(getApplicationContext());
        checkbox.setTag(plik.nazwa);
        checkbox.setChecked(plik.zaznaczony);
        return checkbox;
    }

    public void dodajPozycjeDoSpisuPlikow(PlikGPX plik) {
        LinearLayout wpisgpx = new LinearLayout(getApplicationContext());
        wpisgpx.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        CheckBox checkbox = utworzCheckBoxDlaSpisuPlikow(plik);
        TextView textgpx = utworzTextViewDlaSpisuPlikow(plik, checkbox);
        wpisgpx.addView(checkbox);
        wpisgpx.addView(textgpx);
        spisplikow.addView(wpisgpx);
    }

    public void wyczyscCheckBoxyWSpisiePlikow() {
        for(int i = 0; i < spisplikow.getChildCount(); i++) {
            View view = spisplikow.getChildAt(i);
            if(view instanceof LinearLayout) {
                for(int j = 0; j < ((LinearLayout) view).getChildCount(); j++) {
                    View viewwlayout = ((LinearLayout) view).getChildAt(j);
                    if(viewwlayout instanceof CheckBox) {
                        ((CheckBox) viewwlayout).setChecked(false);
                    }
                }
            }
        }
    }

    private void wyswietlPotwierdzenieUsunicia(final String nazwa, final CheckBox checkbox, final LinearLayout layout) {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(Komunikaty.USUNAC).setMessage(nazwa).setPositiveButton("Tak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkbox.setChecked(false);
                new File(Stale.SCIEZKAMAPLAS + nazwa + ".gpx").renameTo(new File(Stale.SCIEZKAMAPLAS + Stale.FOLDERKOSZ + "/" + nazwa + ".gpx"));
                spisplikow.removeView(layout);
                PlikiGPX.znajdzIUsun(nazwa);
            }
        }).setNegativeButton("Nie", null).show();
    }

    private void usunZaznaczonePlikiDoKosza() {
        for(int i = 0; i < spisplikow.getChildCount(); i++) {
            View view = spisplikow.getChildAt(i);
            if (view instanceof LinearLayout) {
                for (int j = 0; j < ((LinearLayout) view).getChildCount(); j++) {
                    View viewwlayout = ((LinearLayout) view).getChildAt(j);
                    if (viewwlayout instanceof CheckBox) {
                        if(((CheckBox) viewwlayout).isChecked() == true) {
                            wyswietlPotwierdzenieUsunicia((String) viewwlayout.getTag(), (CheckBox)viewwlayout, (LinearLayout)view);
                        }
                    }
                }
            }
        }
    }

    public void zapiszCheckBoxyWPokazPliki() {
        for(int i = 0; i < spisplikow.getChildCount(); i++) {
            View view = spisplikow.getChildAt(i);
            if(view instanceof LinearLayout) {
                for(int j = 0; j < ((LinearLayout) view).getChildCount(); j++) {
                    View viewwlayout = ((LinearLayout) view).getChildAt(j);
                    if(viewwlayout instanceof CheckBox) {
                        PlikiGPX.znajdzIZaznaczPlik((String) viewwlayout.getTag(), ((CheckBox) viewwlayout).isChecked());
                    }
                }
            }
        }
    }

    public void sciagnijPliki() {
        String glownyplik = HTTP.sciagnijPlik(Ustawienia.downloadurl.wartosc, Ustawienia.downloaduser.wartosc, Ustawienia.downloadpass.wartosc, null);
        if(glownyplik != null) {
            String pliki[] = glownyplik.split(Stale.ENTER);
            int poprawnie = 0;
            int blednie = 0;
            for (int i = 0; i < pliki.length; i++) {
                StringBuilder naglowek = new StringBuilder("");
                String plik = HTTP.sciagnijPlik(pliki[i], Ustawienia.downloaduser.wartosc, Ustawienia.downloadpass.wartosc, naglowek);
                String[] zalacznik = naglowek.toString().split("=");
                String zalacznik2 = zalacznik[1].replace("\"", "");
                try {
                    FileWriter filewriter = null;
                    filewriter = new FileWriter(new File(Stale.SCIEZKAMAPLAS + zalacznik2));
                    filewriter.write(plik);
                    filewriter.close();
                    pokazToast("Sciagnieto: " + pliki[i]);
                    poprawnie = poprawnie + 1;
                    String bezgpx = new String(zalacznik2);
                    bezgpx = bezgpx.replace(".gpx", "");
                    PlikiGPX.znajdzIUsun(bezgpx);
                    PlikiGPX.dodatkowoSparsuj(zalacznik2);
                } catch (IOException e) {
                    e.printStackTrace();
                    pokazToast("Blad: " + pliki[i]);
                    blednie = blednie + 1;
                }
            }
            if (poprawnie > 0) {
                czyscSpisPlikow();
                PlikiGPX.wczytajDoPol();
            }
            pokazToast("Poprawnie: " + poprawnie + Stale.ENTER + "Blednie: " + blednie);
        } else {
            pokazToast("Blad podczas sciagnia listy plikow...");
        }
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
                AppService.service.zaczytajOpcje(false, 0, 0);
            }
        });
        zmienmageimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppService.service.wczytajKolejnaMape();
            }
        });
        pomniejszimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppService.service.pomniejszMape();
            }
        });
        powiekszimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppService.service.powiekszMape();
            }
        });
        pokazplikiimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pokazPokazPlikiView();
            }
        });
        pokazplikianuluj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zamknijPokazPlikiIWrocDoMapy();
            }
        });
        wyczyscplikianuluj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wyczyscCheckBoxyWSpisiePlikow();
            }
        });
        pokazplikizapisz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zapiszCheckBoxyWPokazPliki();
                zamknijPokazPlikiIWrocDoMapy();
            }
        });
        pokazplikiusun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usunZaznaczonePlikiDoKosza();
            }
        });
        pokazplikidownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sciagnijPliki();
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

    public void ustawGPSText(String text) {
        ustawTextView(gpstextview, text);
    }

    //Konfigruje Menu glowne programu
    private void utworzMenu() {
        final PopupMenu menuglowne = new PopupMenu(MainActivity.this, menuimageview);
        menuglowne.setOnMenuItemClickListener(new ObslugaMenu());
        menuglowne.inflate(R.menu.menu_main);
        maplayout.findViewById(R.id.menuimageview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuglowne.getMenu().findItem(R.id.przelaczpogps).setChecked(AppService.service.przelaczajpogps);
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

    //Zamyka pokaz pliki i wraca do widoku mapy
    private void zamknijPokazPlikiIWrocDoMapy() {
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

    //Przechodzi do widoku wyboru plikow
    public void pokazPokazPlikiView() {
        AppService.service.widok = Stale.WIDOKPLIKOW;
        contentviewlayout.removeAllViews();
        contentviewlayout.addView(pokazplikilayout, 0);
        PlikiGPX.wczytajDoPol();
    }

    private void ustawPolitykeWatku() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        //Obsluga elementow graficznych
        znajdzLayouty();
        ustawEkran();
        ustawPolitykeWatku();
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
        if(widok == Stale.WIDOKPLIKOW) {
            zamknijPokazPlikiIWrocDoMapy();
        }
    }
}
