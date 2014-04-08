package com.josstoh.opencvapp.app;

/**
 * Created by Jocelyn on 02/04/2014.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Preview extends SurfaceView implements SurfaceHolder.Callback{
    SurfaceHolder mHolder;
    Camera mCamera;
    Context context;
    Activity activity;
    ListView listView;

    //Pour le son
    private RawCallback callback;
    private SoundPool soundPool;
    private int soundID;
    boolean loaded = false;



    Preview(Context context) {
        super(context);
        this.context = context;
        this.activity = (Activity) context;
        this.callback = new RawCallback();

        Boolean bool = listView != null;
        Log.d("OPENCV",bool.toString());

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // Son
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                loaded = true;
            }
        });
        soundID = soundPool.load(context, R.raw.mario_coin_sound, 1);

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
                    mCamera.takePicture(callback,
                            null,
                            callback,
                            new Camera.PictureCallback() {

                                @Override
                                public void onPictureTaken(byte[] photo, Camera camera) {
                                    new SavePhotoTask().execute(photo);
                                    Toast.makeText(context, "PHOTO ENREGISTRE", 10).show();
                                    Toast.makeText(context, String.valueOf(photo.length), 10).show();
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
    // Now that the size is known, set up the camera parameters and begin
    // the preview.

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        // Creation alertDialog
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialog = inflater.inflate(R.layout.dialog_dialog_choix_taille,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choisissez la taille");
        builder.setView(findViewById(R.layout.dialog_dialog_choix_taille));
        builder.show();
        this.listView = (ListView) findViewById(R.id.listView);

        // Recuperation des tailles dispos
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        HashMap<String,String> mapSize;
        ArrayList<HashMap<String,String>> listItem = new ArrayList();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

        for (Camera.Size i : previewSizes)
        {
            mapSize = new HashMap();
            mapSize.put("hauteur",String.valueOf(i.height));
            mapSize.put("largeur",String.valueOf(i.width));
            listItem.add(mapSize);
        }

        List<Integer> l = parameters.getSupportedPreviewFormats();
        for(Integer i : l)
        {
            Log.i("Supported Format",i.toString());
        }
        try{
            listView.setAdapter(new SimpleAdapter(context, listItem, R.layout.affichage_size,
                    new String[] {"hauteur","largeur"},new int[] {R.id.hauteur,R.id.largeur} ));
        }
        catch (Exception e) {
            Boolean b = (listView != null);
            Log.w("ERREUR",e.getMessage()+""+b.toString());
        }


        Camera.Size previewSize = previewSizes.get(1);



        parameters.setPreviewSize(previewSize.width, previewSize.height);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    class RawCallback implements Camera.ShutterCallback, Camera.PictureCallback {

        @Override
        public void onShutter() {
            // Getting the user sound settings
            AudioManager audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
            float actualVolume = (float) audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC);
            float maxVolume = (float) audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float volume = actualVolume / maxVolume;
            // Is the sound loaded already?
            if (loaded) {
                soundPool.play(soundID, volume, volume, 1, 0, 1f);
                Log.e("Test", "Played sound");
            }

        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Boolean test = (data!=null);
            Integer s = ((data == null) ? 0 : data.length);
            Toast.makeText(context, "PHOTO RAW", 10).show();
            Toast.makeText(context, test.toString(), 10).show();
            Toast.makeText(context, s.toString(), 10).show();

            Camera.Size size = camera.getParameters().getPictureSize();
            int[]photo = convertYUV420_NV21toRGB8888(data,size.width,size.height);
            //int[] photo = new int[size.height*size.width];
            //applyGrayScale(photo,data,size.width,size.height);

            for(int l=50;l<100;l++){
                for(int c = 50; c<100; c++) {
                    int p = 640*l+c;
                    photo[p]=0xffff00ff; // xxRRVVBB

                }
            }


            Bitmap bmp = Bitmap.createBitmap(photo, size.width, size.height, Bitmap.Config.ARGB_8888);



            File fichierPhoto=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "fichierPhoto.png");

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(fichierPhoto);
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try{
                    out.close();
                } catch(Throwable ignore) {}
            }
        }

        /**
         * Convertit YUV420 NV21 to RGB8888
         *
         * @param data byte array on YUV420 NV21 format.
         * @param width pixels width
         * @param height pixels height
         * @return a RGB8888 pixels int array. Where each int is a pixels ARGB.
         */

        public int[] convertYUV420_NV21toRGB8888(byte [] data, int width, int height) {
            int size = width*height;
            int offset = size;
            int[] pixels = new int[size];
            int u, v, y1, y2, y3, y4;

            // i percorre os Y and the final pixels
            // k percorre os pixles U e V
            for(int i=0, k=0; i < size; i+=2, k+=2) {
                y1 = data[i  ]&0xff;
                y2 = data[i+1]&0xff;
                y3 = data[width+i  ]&0xff;
                y4 = data[width+i+1]&0xff;

                u = data[offset+k  ]&0xff;
                v = data[offset+k+1]&0xff;
                u = u-128;
                v = v-128;

                pixels[i  ] = convertYUVtoRGB(y1, u, v);
                pixels[i+1] = convertYUVtoRGB(y2, u, v);
                pixels[width+i  ] = convertYUVtoRGB(y3, u, v);
                pixels[width+i+1] = convertYUVtoRGB(y4, u, v);

                if (i!=0 && (i+2)%width==0)
                    i+=width;
            }

            return pixels;
        }

        private int convertYUVtoRGB(int y, int u, int v) {
            int r,g,b;

            r = y + (int)1.402f*v;
            g = y - (int)(0.344f*u +0.714f*v);
            b = y + (int)1.772f*u;
            r = r>255? 255 : r<0 ? 0 : r;
            g = g>255? 255 : g<0 ? 0 : g;
            b = b>255? 255 : b<0 ? 0 : b;
            return 0xff000000 | (b<<16) | (g<<8) | r;
        }

        public void applyGrayScale(int [] pixels, byte [] data, int width, int height) {
            int p;
            int size = width*height;
            for(int i = 0; i < size; i++) {
                p = data[i] & 0xFF;
                pixels[i] = 0xff000000 | p<<16 | p<<8 | p;
            }
        }
    }
}
