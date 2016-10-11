package com.example.joseph.mirar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import org.artoolkit.ar.base.camera.CameraEventListener;

import java.io.IOException;
import java.util.List;

/**
 * Created by Joseph on 2016-08-14.
 */
public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "CameraSurface";
    private Camera camera;

    private CameraEventListener listener;
    public void setCameraEventListener(CameraEventListener listener) {
        this.listener = listener;
    }

    private boolean mustAskPermissionFirst = false;
    public boolean getCameraAccessPermissionFromUser() { return mustAskPermissionFirst; }
    public void resetCameraAccessPermissionFromUserState() {
        mustAskPermissionFirst = false;
    }

    public CameraSurface(Context context, CameraEventListener listener) {
        super(context);

        Log.d(TAG, "CameraSurface() : constructor called");
        Activity activityRef = (Activity)context;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activityRef, Manifest.permission.CAMERA)) {
                    mustAskPermissionFirst = true;

                    if (activityRef.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(activityRef.getApplicationContext(), "App requires access to camera to be granted", Toast.LENGTH_SHORT).show();
                    }

                    Log.i(TAG, "CameraSurface() : must ask user for camera access permission");
                    activityRef.requestPermissions(new String[] { Manifest.permission.CAMERA }, MainActivity.REQUEST_CAMERA_PERMISSION_RESULT);

                    return;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "CameraSurface() : exception caught, " + e.getMessage());
            return;
        }

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        setCameraEventListener(listener);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceCreated() : called, will attempt open camera, set orientation, set preview surface");
        openCamera(surfaceHolder);
    }

    private void openCamera(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "openCamera() : called");

        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            Log.e(TAG, "openCamera() : RuntimeException caught, " + e.getMessage() + ", abnormal exit");
            return;
        } catch (Exception e) {
            Log.e(TAG, "openCamera() : Exception caught, " + e.getMessage() + ", abnormal exit");
            return;
        }

        if (!setOrientationAndPreview(surfaceHolder)) {
            Log.e(TAG, "openCamera() : call to setOrientationAndPreview() failed, openCamera() failed");
        } else {
            Log.i(TAG, "openCamera() : succeeded");
        }
    }

    private boolean setOrientationAndPreview(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "setOrientationAndPreview() called");

        boolean success = true;

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallbackWithBuffer(this);
        } catch (IOException e) {
            Log.e(TAG, "setOrientationAndPreview() : IOException caught, " + e.toString());
            success = false;
        } catch (Exception e) {
            Log.e(TAG, "setOrientationAndPreview() : Exception caught, " + e.toString());
            success = false;
        }

        if (!success) {
            if (null != camera) {
                camera.release();
                camera = null;
            }
            Log.e(TAG, "setOrientationAndPreview() : released camera due to caught exception");
        }

        return success;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (null != camera) {
            Log.i(TAG, "surfaceDestroyed() : closing camera");
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }

        if (null != listener) {
            listener.cameraPreviewStopped();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (null != camera) {
            Camera.Parameters parameters = camera.getParameters();

            Camera.Size size = getBestPreviewSize(width, height, parameters.getSupportedPreviewSizes());

            Log.d(TAG, String.format("Resolution : %sx%s", size.width, size.height));
            parameters.setPreviewSize(size.width, size.height);
            parameters.setPreviewFrameRate(30);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(parameters);

            parameters = camera.getParameters();
            int capWidth = parameters.getPreviewSize().width;
            int capHeight = parameters.getPreviewSize().height;
            int capRate = parameters.getPreviewFrameRate();
            int pixelFormat = parameters.getPreviewFormat();
            PixelFormat pixelInfo = new PixelFormat();
            PixelFormat.getPixelFormatInfo(pixelFormat, pixelInfo);

            int bufSize = capWidth * capHeight * pixelInfo.bitsPerPixel / 8;

            for (int i = 0; i < 5; i++) camera.addCallbackBuffer(new byte[bufSize]);

            camera.startPreview();

            if (null != listener) {
                listener.cameraPreviewStarted(capWidth, capHeight, capRate, 0, false);
            }
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (null != listener) {
            listener.cameraPreviewFrame(bytes);
        }

        camera.addCallbackBuffer(bytes);
    }

    private Camera.Size getBestPreviewSize(int width, int height, List<Camera.Size> supportedPreviewSizes) {
        Camera.Size result = null;

        for (Camera.Size size : supportedPreviewSizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result=size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return result;
    }
}
