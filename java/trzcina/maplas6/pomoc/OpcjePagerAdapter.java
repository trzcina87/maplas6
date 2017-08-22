package trzcina.maplas6.pomoc;


import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import trzcina.maplas6.MainActivity;

//Obsluga zakladek
public class OpcjePagerAdapter extends PagerAdapter {

    public OpcjePagerAdapter() {
        super();
    }

    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View)object);
    }

    public Object instantiateItem(View collection, int position) {

        //W zaleznosci od zakladki zwracamy odpowiedni widok
        View view = null;
        switch (position) {
            case 0:
                view = MainActivity.activity.opcjemapy;
                break;
            case 1:
                view = MainActivity.activity.opcjepodstawowelayout;
                break;
            case 2:
                view = MainActivity.activity.opcjezaawansowanelayout;
                break;
        }
        ((ViewPager) collection).addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(View arg0, int arg1, Object arg2) {
        ((ViewPager) arg0).removeView((View) arg2);
    }

    //W zaleznosci od zakladki zwracamy odpowiedni tytul
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "MAPY";
            case 1:
                return "PODSTAWOWE";
            case 2:
                return "EXPERT";
        }
        return null;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}