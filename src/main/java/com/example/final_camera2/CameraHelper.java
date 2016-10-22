package com.example.final_camera2;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.WindowManager;
import android.graphics.SurfaceTexture;

public class CameraHelper {
    private static final String TAG = "Camera Helper";
    public CameraManager mCameraManager;
    public String mCameraID;
    Context mContext;
    CameraAPI api;
    SurfaceTexture mSurfaceTexture;

    CameraHelper(Context mContext) {
        this.mContext = mContext;

        setManager();
        setCameraID();
        mSurfaceTexture = new SurfaceTexture(10);
        api = new CameraAPI(mCameraManager, mCameraID, mSurfaceTexture);
        api.openCamera();
    }

    void getImage() {
        Log.i(TAG, "In getImage()");
        WindowManager windowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        //todo fix rotation, make it dynamic
        rotation = 2;
        api.takePicture(rotation);
    }

    void setManager()
    {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
    }

    void setCameraID()
    {
        mCameraID = getFrontalCameraId();
    }

    String getFrontalCameraId()
    {
        String cameraId = null;

        try {
            String[] cameraList = mCameraManager.getCameraIdList();
            for (String cameraID : cameraList) {

                if (isFrontalCamera(cameraID)) {
                    Log.i(TAG, "cameraID: "+cameraID + " is frontal");
                    cameraId = cameraID;
                    break;
                } else {
                    Log.i(TAG, "cameraID: " + cameraID + " isn't frontal");
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }

        return cameraId;
    }


    boolean isFrontalCamera(String mCameraID) {
        boolean isFrontal = false;

        try {
            CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(mCameraID);
            int cOrientation = cc.get(CameraCharacteristics.LENS_FACING);
            if(cOrientation == CameraCharacteristics.LENS_FACING_FRONT)
                isFrontal = true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return isFrontal;
    }

}
