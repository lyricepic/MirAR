package com.example.joseph.mirar;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joseph on 2016-08-16.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final String TAG = "MyGLSurfaceView";

    private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);                  // OpenGL2.0 버전 사용

        setZOrderMediaOverlay(true);                    // CameraSurfaceView보다 앞쪽에 표시
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);         // 투명화 처리
        getHolder().setFormat(PixelFormat.RGBA_8888);   // 투명화 처리

        mRenderer = new MyGLRenderer(context);

        setRenderer(mRenderer);

//        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);     // requestRender() 메소드 호출시에만 렌더링
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN:               // 첫번째 손가락
            case MotionEvent.ACTION_POINTER_DOWN:       // 두번째 손가락

                break;

            case MotionEvent.ACTION_MOVE:

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:

                break;
        }

        requestRender();

        return true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);

        Log.d("surfaceChanged", "w : " + w + ", h : " + h);
    }
}
