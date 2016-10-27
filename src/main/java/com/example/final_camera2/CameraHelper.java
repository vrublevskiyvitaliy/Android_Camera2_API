package com.example.final_camera2;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.WindowManager;

public class CameraHelper {
    private static final String TAG = "Camera Helper";

    public CameraManager mCameraManager;
    public String mCameraID;
    Context mContext;
    CameraAPI api;

    CameraHelper(Context mContext) {
        this.mContext = mContext;
        setManager();
        setCameraID();
        api = new CameraAPI(mCameraManager, mCameraID);
    }

    public int getRotation()
    {
        WindowManager windowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        return rotation;
    }

    void getImage() {
        Log.i(TAG, "In getImage()");
        float startFocus = 0.7f;
        float endFocus = 1f;
        float stepFocus = 0.1f;

        api.takePicture(getRotation(), startFocus, endFocus, stepFocus);
    }

    void getImageWithFocus(float focus) {
        Log.i(TAG, "In getImageWithFocus()");
        Log.i(TAG, focus + "");
        api.takePictureWithFocus(getRotation(), focus);
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

    public void openCamera() {
        api.openCamera();
    }

    public void closeCamera() {
        api.closeCamera();
    }

    public void setBackgroundHandler(Handler handler)
    {
        api.setBackgroundHandler(handler);
    }

    public void setTextureView(TextureView textureView)
    {
        api.setTextureView(textureView);
    }

    public void changePreviewFocus(float focus) {
        api.updatePreviewWithManualFocus(focus);
    }
}
