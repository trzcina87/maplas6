package trzcina.maplas6.pomoc;

import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;

import trzcina.maplas6.MainActivity;
import trzcina.maplas6.R;

public class ObslugaMenu implements PopupMenu.OnMenuItemClickListener {

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.resetagpsitem:
                Bundle extras = new Bundle();
                extras.putBoolean("all", true);
                /*MainActivity.activity.locationmanager.sendExtraCommand(LocationManager.GPS_PROVIDER, "delete_aiding_data", extras);
                Bundle bundle = new Bundle();
                activity.locationmanager.sendExtraCommand("gps", "force_xtra_injection", bundle);
                activity.locationmanager.sendExtraCommand("gps", "force_time_injection", bundle);*/
                MainActivity.activity.pokazToast(Komunikaty.RESETAGPS);
                return true;
            case R.id.ustawieniaitem:
                MainActivity.activity.pokazOpcjeView();
                return true;
        }
        return true;
    }
}
