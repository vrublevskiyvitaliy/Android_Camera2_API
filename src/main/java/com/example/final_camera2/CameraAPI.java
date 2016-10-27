package com.example.final_camera2;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CameraAPI {
    private static final String TAG = "CameraAPI";
    private CameraManager mCameraManager    = null;
    private String mCameraID    = null;
    private CameraDevice mCameraDevice = null;
    private int number = 1;
    private Size imageDimension;
    protected CaptureRequest.Builder captureRequestBuilder;
    protected CameraCaptureSession cameraCaptureSessions;
    private Handler mBackgroundHandler = null;
    private TextureView mTextureView;
    private float manualFocus;
    private boolean useManualFocus;

    public void setBackgroundHandler(Handler backgroundHandler)
    {
        mBackgroundHandler = backgroundHandler;
    }

    public void setTextureView(TextureView textureView)
    {
        mTextureView = textureView;
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 270);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 90);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public CameraAPI(CameraManager cameraManager, String cameraID) {
        mCameraManager  = cameraManager;
        mCameraID       = cameraID;
        useManualFocus = false;

        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

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
            createCameraPreview();
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

    protected Size getMaxSize()
    {
        Size maxSize = new Size(640, 480);
        try {

            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).
                        getOutputSizes(ImageFormat.JPEG);
            }
            if (jpegSizes != null && 0 < jpegSizes.length) {
                maxSize = new Size(jpegSizes[0].getWidth(), jpegSizes[0].getHeight());
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return maxSize;
    }

    public void prepareRequest(CaptureRequest.Builder captureBuilder, int rotation) {
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        captureBuilder.set(CaptureRequest.CONTROL_AF_MODE , CameraMetadata.CONTROL_AF_MODE_OFF);
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
        captureBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON);
        captureBuilder.set(CaptureRequest.JPEG_QUALITY, (byte)100);
    }

    public void takePicture(int rotation, final float startFocus, final float endFocus, final float stepFocus) {
        if(null == mCameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        try {
            final int numberOfPhotos = (int) ((endFocus - startFocus) / stepFocus) + 1;

            Size size = getMaxSize();
            ImageReader reader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.JPEG, numberOfPhotos);

            List<Surface> outputSurfaces = new ArrayList<Surface>(1);
            final ArrayList<Image> images = new ArrayList<>(numberOfPhotos);

            outputSurfaces.add(reader.getSurface());
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());

            prepareRequest(captureBuilder, rotation);

            number = 0;
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    images.add(image);
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.i(TAG, "Saved:");
                }
            };
            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        float focus = 0;
                        for (int i = 0; i < numberOfPhotos - 1; i++)
                        {
                            focus = startFocus + i * stepFocus;
                            captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focus);
                            session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                        }

                        focus = endFocus;
                        captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focus);
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                        session.close();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }

                public void saveImages()
                {
                    try {

                        for (int i = 0; i < numberOfPhotos; i++)
                        {
                            saveImage(images.get(i), "series" + number);
                            number += 1;
                            images.get(i).close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onClosed(@NonNull CameraCaptureSession session) {
                    Log.i(TAG, "Session is closed!");
                    saveImages();
                    createCameraPreview();
                }


            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void saveImage(Image image, String name) throws IOException {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        OutputStream output = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory()+"/" + name  +".jpg");
            output = new FileOutputStream(file);
            output.write(bytes);
        } finally {
            if (null != output) {
                output.close();
            }
        }
    }

    public void takePictureWithFocus(int rotation, final float focus) {
        if(null == mCameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        try {
            Size size = getMaxSize();
            ImageReader reader = ImageReader.newInstance(size.getWidth(), size.getHeight(), ImageFormat.JPEG, 1);

            List<Surface> outputSurfaces = new ArrayList<Surface>(1);
            final ArrayList<Image> images = new ArrayList<>(1);

            outputSurfaces.add(reader.getSurface());
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());

            prepareRequest(captureBuilder, rotation);

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    try {
                        Image image = reader.acquireLatestImage();
                        String name = "focusDist" + String.format("%.2f", focus);
                        saveImage(image, name);
                        image.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.i(TAG, "Saved:");
                }
            };
            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focus);
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                        session.close();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }

                @Override
                public void onClosed(@NonNull CameraCaptureSession session) {
                    Log.i(TAG, "Session is closed!");
                    createCameraPreview();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == mCameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.e(TAG, "Configuration failed");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public void updatePreviewWithManualFocus(float focus) {
        if(null == mCameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        manualFocus = focus;
        useManualFocus = true;
        try {
            cameraCaptureSessions.stopRepeating();
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE , CameraMetadata.CONTROL_AF_MODE_OFF);
            captureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON);
            captureRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focus);

            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if (useManualFocus) {
            updatePreviewWithManualFocus(manualFocus);
            return;
        }
        if(null == mCameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeCamera() {
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }
}
