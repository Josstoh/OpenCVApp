package com.josstoh.opencvapp.app;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends ActionBarActivity{

    private Preview mPreview;
    private View dialog = null;

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
        LayoutInflater inflater = LayoutInflater.from(mPreview.context);
        this.dialog = inflater.inflate(R.layout.dialog_dialog_choix_taille,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(mPreview.context);
        builder.setTitle("Choisissez la taille").setView(dialog).show();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

}
