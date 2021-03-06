package trzcina.maplas6.pomoc;

import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.MenuItem;

import trzcina.maplas6.AppService;
import trzcina.maplas6.MainActivity;
import trzcina.maplas6.MainSurface;
import trzcina.maplas6.R;

public class ObslugaMenu implements PopupMenu.OnMenuItemClickListener {

    private String pobierzPamiec() {
        Runtime runtime = Runtime.getRuntime();
        long uzyte = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        long heapm = runtime.maxMemory() / 1048576L;
        int procent = (int) ((uzyte / (double)heapm) * 100);
        return new String("Pamięc: " + uzyte + "MB/" + heapm + "MB" + " (" + procent + "%)");
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {

            //Pozyacja w menu: Reset AGPS
            case R.id.resetagpsitem:
                Bundle extras = new Bundle();
                extras.putBoolean("all", true);
                Bundle bundle = new Bundle();
                AppService.service.locationmanager.sendExtraCommand(LocationManager.GPS_PROVIDER, "delete_aiding_data", extras);
                AppService.service.locationmanager.sendExtraCommand("gps", "force_xtra_injection", bundle);
                AppService.service.locationmanager.sendExtraCommand("gps", "force_time_injection", bundle);
                MainActivity.activity.pokazToast(Komunikaty.RESETAGPS);
                return true;

            case R.id.trybsamochodowyitem:
                AppService.service.trybsamochodowy = ! AppService.service.trybsamochodowy;
                if(AppService.service.wlaczgps == true) {
                    MainActivity.activity.pokazToast(Komunikaty.TRYBSAMOCHODOWY);
                }
                return true;

            //Pozycja w menu: Ustawienia
            case R.id.ustawieniaitem:
                MainActivity.activity.pokazOpcjeView();
                return true;

            case R.id.internetitem:
                AppService.service.internetwyslij = ! AppService.service.internetwyslij;
                return true;

            case R.id.precyzjagps:
                AppService.service.precyzyjnygps = ! AppService.service.precyzyjnygps;
                return true;

            case R.id.dzwiekiitem:
                AppService.service.grajdzwieki = ! AppService.service.grajdzwieki;
                return true;

            case R.id.gpsitem:
                AppService.service.wlaczgps = ! AppService.service.wlaczgps;
                AppService.service.zmianaTrybuGPS(false);
                return true;

            //Pozycja w menu: Informacja
            case R.id.infoitem:
                String komunikat = "Kompilacja: " + Rozne.pobierzDateBudowania() + Stale.ENTER;
                komunikat = komunikat + pobierzPamiec() + Stale.ENTER + Stale.ENTER + Stale.INSTRUKCJA;
                new AlertDialog.Builder(MainActivity.activity).setIcon(android.R.drawable.ic_dialog_info).setTitle(Komunikaty.INFORMACJA).setMessage(komunikat).setPositiveButton("OK", null).show();
                return true;

            case R.id.przelaczpogps:
                AppService.service.przelaczajpogps = ! AppService.service.przelaczajpogps;
                return true;

            case R.id.trybtelewizyjnyitem:
                AppService.service.trybtelewizyjny = ! AppService.service.trybtelewizyjny;
                MainActivity.activity.ustawTrybTelewizyjny();
                return true;
            case R.id.wyslijsmsitem:
                AppService.service.wyslijSMSZLokcalizacja();
                return true;
        }
        return true;
    }
}
