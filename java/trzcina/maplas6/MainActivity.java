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
    private TextView textinfoprzygotowanie;
    private ProgressBar progressbarprzygotowanie;
    private TextView luxtextview;
    private ImageView menuimageview;
    public MainSurface surface;
    private RelativeLayout przygotowanielayout;
    private RelativeLayout maplayout;
    public LinearLayout opcjemapy;
    public LinearLayout opcjepodstawowelayout;
    public LinearLayout opcjezaawansowanelayout;
    public LinearLayout opcjelayout;
    public LinearLayout contentviewlayout;
    public EditText nazwaurzadzenia;
    public EditText folderzmapami;
    public EditText atlasedittext;
    public LinearLayout spismap;
    public Button opcjeanuluj;
    public Button opcjezapisz;

    private void znajdzLayouty() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        przygotowanielayout = (RelativeLayout) inflater.inflate(R.layout.przygotowanielayout, null);
        przygotowanielayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        maplayout = (RelativeLayout) inflater.inflate(R.layout.maplayout, null);
        maplayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        opcjemapy = (LinearLayout) inflater.inflate(R.layout.opcjemapylayout, null);
        opcjemapy.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        opcjepodstawowelayout = (LinearLayout) inflater.inflate(R.layout.opcjepodstawowelayout, null);
        opcjepodstawowelayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        opcjezaawansowanelayout = (LinearLayout) inflater.inflate(R.layout.opcjezaawansowanelayout, null);
        opcjezaawansowanelayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        opcjelayout = (LinearLayout) inflater.inflate(R.layout.opcjelayout, null);
        opcjelayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        contentviewlayout = (LinearLayout) inflater.inflate(R.layout.contentviewlayout, null);
        contentviewlayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
    }

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

    public void czyscSpisMap() {
        spismap.removeAllViews();
    }

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
        MainActivity.activity.spismap.addView(button);
    }

    private void ustawPager() {
        OpcjePagerAdapter opa = new OpcjePagerAdapter();
        ViewPager mViewPager = (ViewPager) opcjelayout.findViewById(R.id.container);
        mViewPager.setAdapter(opa);
        TabLayout tabLayout = (TabLayout) opcjelayout.findViewById(R.id.taby);
        tabLayout.setupWithViewPager(mViewPager);
    }

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

    private void ustawTextView(final TextView textview, final String komunikat) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textview.setText(komunikat);
            }
        });
    }

    private void ustawProgressBar(final ProgressBar progressbar, final int poziom) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressbar.setProgress(poziom);
            }
        });
    }

    public void ustawInfoPrzygotowanie(String komunikat) {
        ustawTextView(textinfoprzygotowanie, komunikat);
    }

    public void ustawProgressPrzygotowanie(int poziom) {
        ustawProgressBar(progressbarprzygotowanie, poziom);
    }

    public void ustawLux(int lux) {
        ustawTextView(luxtextview, lux + "lx ");
    }

    private void utworzMenu() {
        final PopupMenu popupMenu = new PopupMenu(MainActivity.this, menuimageview);
        popupMenu.setOnMenuItemClickListener(new ObslugaMenu());
        popupMenu.inflate(R.menu.menu_main);
        maplayout.findViewById(R.id.menuimageview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*popupMenu.getMenu().findItem(R.id.gpsitem).setChecked(gpswlaczony);
                popupMenu.getMenu().findItem(R.id.internetitem).setChecked(internetwlaczony);
                popupMenu.getMenu().findItem(R.id.precyzjagps).setChecked(precyzjagps);
                popupMenu.getMenu().findItem(R.id.dzwiekiitem).setChecked(dzwieki);
                popupMenu.getMenu().findItem(R.id.drugaosobaitem).setChecked(false);*/
                popupMenu.show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        znajdzLayouty();
        ustawEkran();
        znajdzWidoki();
        ustawPager();
        przypiszAkcjeDoWidokow();
        utworzMenu();
        Uprawnienia.zainicjujUprawnienia();
        Painty.inicjujPainty();
        Ustawienia.zainicjujUstawienia();
        if(Uprawnienia.czyNadane() == true) {
            wysartujService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int kod, String[] uprawnienia, int[] odpowiedzi) {
        super.onRequestPermissionsResult(kod, uprawnienia, odpowiedzi);
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
        if(Uprawnienia.czyNadane() == true) {
            wysartujService();
        } else {
            pokazToast(Komunikaty.BRAKUPRAWNINEN);
            finish();
        }
    }

    private void wysartujService() {
        startService(new Intent(this, AppService.class));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void wyswietlPotwierdzenieZamkniecia() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(Komunikaty.CZYWYJSCTYTUL).setMessage(Komunikaty.CZYWYJSCTRESC).setPositiveButton("Tak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopService(new Intent(MainActivity.this, AppService.class));
                finish();
            }
        }).setNegativeButton("Nie", null).show();
    }

    private void zamknijOpcjeIWrocDoMapy() {
        contentviewlayout.removeAllViews();
        contentviewlayout.addView(maplayout);
        AppService.widok = Stale.WIDOKMAPA;
    }

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

    public void pokazToast(String komunikat) {
        Toast.makeText(getApplicationContext(), komunikat, Toast.LENGTH_SHORT).show();
    }

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

    public void pokazOpcjeView() {
        AppService.service.widok = Stale.WIDOKOPCJI;
        contentviewlayout.removeAllViews();
        contentviewlayout.addView(opcjelayout, 0);
        Ustawienia.wczytajDoPol();
        Atlasy.wczytajDoPol();
    }

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
}
