package trzcina.maplas6.pomoc;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;

import trzcina.maplas6.MainActivity;
import trzcina.maplas6.R;

public class ObslugaMenu implements PopupMenu.OnMenuItemClickListener {

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {

            //Pozyacja w menu: Reset AGPS
            case R.id.resetagpsitem:
                Bundle extras = new Bundle();
                extras.putBoolean("all", true);
                /*MainActivity.activity.locationmanager.sendExtraCommand(LocationManager.GPS_PROVIDER, "delete_aiding_data", extras);
                Bundle bundle = new Bundle();
                activity.locationmanager.sendExtraCommand("gps", "force_xtra_injection", bundle);
                activity.locationmanager.sendExtraCommand("gps", "force_time_injection", bundle);*/
                MainActivity.activity.pokazToast(Komunikaty.RESETAGPS);
                return true;

            //Pozycja w menu: Ustawienia
            case R.id.ustawieniaitem:
                MainActivity.activity.pokazOpcjeView();
                return true;

            //Pozycja w menu: Informacja
            case R.id.infoitem:
                new AlertDialog.Builder(MainActivity.activity).setIcon(android.R.drawable.ic_dialog_info).setTitle(Komunikaty.INFORMACJA).setMessage(Rozne.pobierzDateBudowania()).setPositiveButton("OK", null).show();
                return true;
        }
        return true;
    }
}
