package com.josstoh.opencvapp.app;

/**
 * Created by Jocelyn on 02/04/2014.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class Preview extends SurfaceView implements SurfaceHolder.Callback{
    SurfaceHolder mHolder;
    Camera mCamera;
    Context context;
    Activity activity;

    Preview(Context context) {
        super(context);
        this.context = context;
        this.activity = (Activity) context;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        mCamera = Camera.open();
        try {
            mCamera.setPreviewDisplay(holder);

            setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean b, Camera camera) {
                            Toast.makeText(context, "FOCUS", 10).show();
                        }
                    });
                    return true;
                }
            });

            setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View view){
                    mCamera.takePicture(null,null,null, new Camera.PictureCallback() {

                        @Override
                        public void onPictureTaken(byte[] photo, Camera camera) {
                            new SavePhotoTask().execute(photo);
                            camera.startPreview();
                        }
                    });
                }
            });
        } catch (Exception e) {
            mCamera.release();
            mCamera = null;

        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(findViewById(R.layout.dialog_dialog_choix_taille)).show();

        Camera.Size previewSize = previewSizes.get(1);

        parameters.setPreviewSize(previewSize.width, previewSize.height);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }


}