package com.example.final_camera2;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.util.Log;


public class CameraAPI {
    private static final String TAG = "CameraAPI";
    private CameraManager mCameraManager    = null;
    private String mCameraID    = null;
    private CameraDevice mCameraDevice = null;

    public CameraAPI(CameraManager cameraManager, String cameraID) {
        mCameraManager  = cameraManager;
        mCameraID       = cameraID;
    }

    public void openCamera() {
        Log.i(TAG, "open Camera");
        try {
            mCameraManager.openCamera(mCameraID, cameraStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "openCamera X");
    }

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.i(TAG, "onOpened");
            mCameraDevice = camera;
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };


}
