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

    //Przypisanie zdarzen o zmianie sufrace, tak by reszta programu znala rozmiarkaflay okna
    private void inicjujSurface() {
        surfaceholder = getHolder();
        startprzesuwania = new Point();
        setOnTouchListener(this);
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
        int x = (int)(event.getX());
        int y = (int)(event.getY());
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startprzesuwania.set(x, y);
                break;
            case MotionEvent.ACTION_UP:
                AppService.service.pixelnamapienadsrodkiem.offset(startprzesuwania.x - x, startprzesuwania.y - y);
                AppService.service.poprawPixelNadSrodkiem();
                AppService.service.odswiezUI();
                AppService.service.rysujwatek.odswiez = true;
                break;
            case MotionEvent.ACTION_MOVE:
                AppService.service.pixelnamapienadsrodkiem.offset(startprzesuwania.x - x, startprzesuwania.y - y);
                AppService.service.poprawPixelNadSrodkiem();
                AppService.service.odswiezUI();
                AppService.service.rysujwatek.odswiez = true;
                startprzesuwania.set(x, y);
                break;
        }
        return true;
    }
}
