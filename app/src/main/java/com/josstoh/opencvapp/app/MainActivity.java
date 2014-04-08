package com.josstoh.opencvapp.app;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends ActionBarActivity{

    private Preview mPreview;
    private View dialog = null;
    private PowerManager.WakeLock wakeLock;
    private String TAG = "opencvapp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 16 || true) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Create our Preview view and set it as the content of our activity.
        mPreview = new Preview(this);
        setContentView(mPreview);
        // Gestion mise en veille de l'ecran
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        wakeLock.acquire();
        //LayoutInflater inflater = LayoutInflater.from(mPreview.context);
        //this.dialog = inflater.inflate(R.layout.dialog_dialog_choix_taille,null);
        //AlertDialog.Builder builder = new AlertDialog.Builder(mPreview.context);
        //builder.setTitle("Choisissez la taille").setView(dialog).show();
    }

    @Override
    protected void onPause() {

        super.onPause();
        if (wakeLock != null) {
            Log.v(TAG, "Releasing wakelock");
            try {
                wakeLock.release();
            } catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released
            }
        } else {
            // should never happen during normal workflow
            Log.e(TAG, "Wakelock reference is null");
        }

    }

}
