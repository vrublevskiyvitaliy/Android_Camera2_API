package com.example.final_camera2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private CameraHelper cameraHelper;
    private Button takePictureButton;
    private Button takePictureWithFocusButton;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    public static TextureView textureView;
    private TextView mTextValue;
    private SeekBar mSeekbar;
    // Focus routine
    final float MIN_FOCUS_DISTANCE = 1f;
    final float MAX_FOCUS_DISTANCE = 10.f;
    final float FOCUS_STEP = (MAX_FOCUS_DISTANCE - MIN_FOCUS_DISTANCE) / 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = (TextureView) findViewById(R.id.texture_for_preview);
        assert textureView != null;
        takePictureButton = (Button) findViewById(R.id.btn_get_photo);
        assert takePictureButton != null;
        takePictureWithFocusButton = (Button) findViewById(R.id.btn_get_photo_focus);
        assert takePictureWithFocusButton != null;

        textureView.setSurfaceTextureListener(textureListener);

        cameraHelper = new CameraHelper(this);
        cameraHelper.setTextureView(textureView);

        // Add permission for camera and let user grant the permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float startFocus = 1f;
                float endFocus = 10f;
                float stepFocus = 1f;
                cameraHelper.getImage(startFocus, endFocus, stepFocus);
            }
        });

        takePictureWithFocusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if focus was set manually
                String text = mTextValue.getText().toString();
                if (text.equals("AUTO")) {
                    Toast.makeText(MainActivity.this, "Sorry! Set manual focus first", Toast.LENGTH_LONG).show();
                    return;
                }
                cameraHelper.getImageWithFocus(getCurrentProgress(mSeekbar));
            }
        });
        mTextValue = (TextView)findViewById(R.id.textView2);
        mTextValue.setText("AUTO");

        mSeekbar = (SeekBar)findViewById(R.id.seekBar_focus_distance);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                processChange(seekBar);
            }

            private void processChange(SeekBar seekBar) {
                float focus = getCurrentProgress(seekBar);
                mTextValue.setText(String.format("%.2f", focus));
                cameraHelper.changePreviewFocus(focus);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                processChange(seekBar);
            }
        });
    }

    private float getCurrentProgress(SeekBar seekBar)
    {
        int progress = seekBar.getProgress();

        float correctProgress = MIN_FOCUS_DISTANCE + FOCUS_STEP * progress;
        return correctProgress;
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            cameraHelper.openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(MainActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        cameraHelper.setBackgroundHandler(mBackgroundHandler);
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
            cameraHelper.setBackgroundHandler(null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            cameraHelper.openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        stopBackgroundThread();
        cameraHelper.closeCamera();
        super.onPause();
    }
}
