package trzcina.maplas6;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class MainSurface extends SurfaceView implements View.OnTouchListener {

    public SurfaceHolder surfaceholder;
    private Point startprzesuwania;
    public long ostatnieklikniecie;

    //Przypisanie zdarzen o zmianie sufrace, tak by reszta programu znala rozmiarkaflay okna
    private void inicjujSurface() {
        surfaceholder = getHolder();
        startprzesuwania = new Point();
        setOnTouchListener(this);
        ostatnieklikniecie = 0;
        surfaceholder.addCallback(new SurfaceHolder.Callback(){

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                AppService.service.zmianaSurface(MainSurface.this.getWidth(), MainSurface.this.getHeight());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                AppService.service.zmianaSurface(MainSurface.this.getWidth(), MainSurface.this.getHeight());
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    public MainSurface(Context context) {
        super(context);
        inicjujSurface();
    }

    public MainSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        inicjujSurface();
    }

    public MainSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inicjujSurface();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MainSurface(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inicjujSurface();
    }

    //Dotkniecie palcem na surface
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float zoom = AppService.service.zoom / (float)10;
        int x = (int)(event.getX());
        int y = (int)(event.getY());
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startprzesuwania.set(x, y);
                break;
            case MotionEvent.ACTION_UP:
                AppService.service.pixelnamapienadsrodkiem.offset(Math.round((startprzesuwania.x - x) / zoom), Math.round((startprzesuwania.y - y) / zoom));
                AppService.service.poprawPixelNadSrodkiem();
                AppService.service.odswiezUI();
                AppService.service.rysujwatek.odswiez = true;
                AppService.service.przesuwajmapezgps = false;
                int procent = (int) (0.25F * Math.min(AppService.service.srodekekranu.x, AppService.service.srodekekranu.y));
                if ((x >= AppService.service.srodekekranu.x - procent) && (x <= AppService.service.srodekekranu.x + procent) && (y >= AppService.service.srodekekranu.y - procent) && (y <= AppService.service.srodekekranu.y + procent)) {
                    if (System.currentTimeMillis() <= ostatnieklikniecie + 500) {
                        AppService.service.wysrodkujMapeDoGPS();
                    }
                    ostatnieklikniecie = System.currentTimeMillis();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                AppService.service.pixelnamapienadsrodkiem.offset(Math.round((startprzesuwania.x - x) / zoom), Math.round((startprzesuwania.y - y) / zoom));
                AppService.service.poprawPixelNadSrodkiem();
                AppService.service.odswiezUI();
                AppService.service.rysujwatek.odswiez = true;
                startprzesuwania.set(x, y);
                AppService.service.przesuwajmapezgps = false;
                break;
        }
        return true;
    }
}
