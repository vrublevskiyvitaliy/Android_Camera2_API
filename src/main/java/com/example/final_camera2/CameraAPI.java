package com.example.final_camera2;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.util.Log;


public class CameraAPI {
    private static final String TAG = "CameraAPI";
    private CameraManager mCameraManager    = null;
    private String mCameraID    = null;

    public CameraAPI(CameraManager cameraManager, String cameraID) {
        mCameraManager  = cameraManager;
        mCameraID       = cameraID;
    }
}
