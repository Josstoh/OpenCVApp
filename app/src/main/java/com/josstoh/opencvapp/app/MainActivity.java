package com.josstoh.opencvapp.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;


public class MainActivity extends ActionBarActivity{

    private Preview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new Preview(this);
        setContentView(mPreview);
    }
}
