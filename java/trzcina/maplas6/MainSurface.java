package trzcina.maplas6;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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

    private void inicjujSurface() {
        surfaceholder = getHolder();
        setOnTouchListener(this);
        surfaceholder.addCallback(new SurfaceHolder.Callback(){

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                AppService.service.srodekekranu.set(MainSurface.this.getWidth() / 2, MainSurface.this.getHeight() / 2);
                try {
                    AppService.service.rysujwatek.odswiez = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                AppService.service.srodekekranu.set(MainSurface.this.getWidth() / 2, MainSurface.this.getHeight() / 2);
                try {
                    AppService.service.rysujwatek.odswiez = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
}
