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
        printForAllCamerasProperties();
        api = new CameraAPI(mCameraManager, mCameraID);
    }

    public int getRotation()
    {
        WindowManager windowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        return rotation;
    }

    void getImage(float startFocus, float endFocus, float stepFocus) {
        Log.i(TAG, "In getImage()");
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

    public void changePreviewFocus(float focus)
    {
        api.updatePreviewWithManualFocus(focus);
    }

    public void printForAllCamerasProperties()
    {
        try {
            String[] cameraList = mCameraManager.getCameraIdList();
            for (String cameraID : cameraList) {

                printProperties(cameraID);
            }
        } catch (CameraAccessException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }
    }

    public void printProperties(String cameraID)
    {
        String localTAG = "Properties: ";
        try {
            CameraCharacteristics characteristics
                    = mCameraManager.getCameraCharacteristics(cameraID);
            Log.d(localTAG, " ====================================== ");
            Log.d(localTAG, " CAMERA_ID = " + cameraID);

            switch (characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)) {
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                    Log.d(localTAG, "INFO_SUPPORTED_HARDWARE_LEVEL = " + "INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                    Log.d(localTAG, "INFO_SUPPORTED_HARDWARE_LEVEL = " + "INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                    Log.d(localTAG, "INFO_SUPPORTED_HARDWARE_LEVEL = " + "INFO_SUPPORTED_HARDWARE_LEVEL_FULL");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                    Log.d(localTAG, "INFO_SUPPORTED_HARDWARE_LEVEL = " + "INFO_SUPPORTED_HARDWARE_LEVEL_3");
                    break;
            }

            switch (characteristics.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION)) {
                case CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_UNCALIBRATED:
                    Log.d(localTAG, "LENS_INFO_FOCUS_DISTANCE_CALIBRATION = " + "LENS_INFO_FOCUS_DISTANCE_CALIBRATION_UNCALIBRATED");
                    break;
                case CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE:
                    Log.d(localTAG, "LENS_INFO_FOCUS_DISTANCE_CALIBRATION = " + "LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE");
                    break;
                case CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED:
                    Log.d(localTAG, "LENS_INFO_FOCUS_DISTANCE_CALIBRATION = " + "LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED");
                    break;
            }

            Log.d(localTAG, "LENS_INFO_MINIMUM_FOCUS_DISTANCE = " + characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE));

            Log.d(localTAG, " ====================================== ");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float getMinFocusForCamera(){
        float focus = 0f;
        try {
            CameraCharacteristics characteristics
                    = mCameraManager.getCameraCharacteristics(mCameraID);

            focus = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return focus;
    }
}
