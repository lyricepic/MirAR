package com.example.joseph.mirar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.camera.CameraEventListener;

public class MainActivity extends AppCompatActivity implements CameraEventListener {

    private static final String TAG = "MainActivity";
    public static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;

    private int PATT_SIZE = 16;
    private int PATT_COUNT_MAX = 25;

    private CameraSurface cameraSurface;
    private GLSurfaceView glSurfaceView;
    private FrameLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                handleUncaughtException(thread, throwable);
            }
        });

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mainLayout = (FrameLayout)findViewById(R.id.mainLayout);

        if (!ARToolKit.getInstance().initialiseNativeWithOptions(this.getCacheDir().getAbsolutePath(), PATT_SIZE, PATT_COUNT_MAX)) {
            new AlertDialog.Builder(this)
                    .setMessage("The native library is not loaded. The application cannot continue.")
                    .setTitle("Error")
                    .setCancelable(true)
                    .setNeutralButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish();
                                }
                            })
                    .show();

            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraSurface = new CameraSurface(this, this);
        Log.i(TAG, "onResume() : CameraSurface constructed");

        if (cameraSurface.getCameraAccessPermissionFromUser()) {
            return;
        }

        glSurfaceView = new MyGLSurfaceView(this);

        mainLayout.addView(cameraSurface);
        mainLayout.addView(glSurfaceView);

        if (null != glSurfaceView) glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (null != glSurfaceView) {
            glSurfaceView.onPause();
        }

        mainLayout.removeView(cameraSurface);
        mainLayout.removeView(glSurfaceView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult() : called");

        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Application will not run with camera access denied", Toast.LENGTH_SHORT).show();
            } else if (1 <= permissions.length) {
                Toast.makeText(getApplicationContext(), String.format("Camera access permission \"%s\" allowed", permissions[0]), Toast.LENGTH_SHORT).show();
            }

            Log.i(TAG, "onRequestPermissionsResult() : reset ask for cam access perm");
            cameraSurface.resetCameraAccessPermissionFromUserState();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void handleUncaughtException(Thread thread, Throwable e)
    {
        Log.e(TAG, "handleUncaughtException(): exception type, " + e.toString());
        Log.e(TAG, "handleUncaughtException(): thread, \"" + thread.getName() + "\" exception, \"" + e.getMessage() + "\"");
        e.printStackTrace();
    }

    @Override
    public void cameraPreviewStarted(int width, int height, int rate, int cameraIndex, boolean cameraIsFrontFacing) {
        if (ARToolKit.getInstance().initialiseAR(width, height, null, cameraIndex, cameraIsFrontFacing)) {
            Log.i(TAG, "cameraPreviewStarted(): Camera initialised");
        } else {
            Log.e(TAG, "cameraPreviewStarted(): Error initialising camera. Cannot continue.");
            finish();
        }
    }

    @Override
    public void cameraPreviewFrame(byte[] frame) {
        if (ARToolKit.getInstance().convertAndDetect(frame)) {
            if (null != glSurfaceView) {
                glSurfaceView.requestRender();
            }
        }
    }

    @Override
    public void cameraPreviewStopped() {
        ARToolKit.getInstance().cleanup();
    }
}
