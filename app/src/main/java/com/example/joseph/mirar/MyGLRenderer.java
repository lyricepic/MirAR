package com.example.joseph.mirar;

import android.content.Context;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.joseph.mirar.objects.GaugeForHiro;
import com.example.joseph.mirar.objects.GaugeForMiro;
import com.example.joseph.mirar.objects.IPainter;
import com.example.joseph.mirar.objects.VideoPlayer;
import com.example.joseph.mirar.programs.ColorShaderProgram;
import com.example.joseph.mirar.programs.TextureShaderProgram;
import com.example.joseph.mirar.util.TextureHelper;

import org.artoolkit.ar.base.ARToolKit;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.Matrix.multiplyMM;

/**
 * Created by Joseph on 2016-08-18.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";

    private int markerHiroID = -1;
    private int markerMiroID = -1;

    private final Context mContext;
    private IPainter gaugeForHiro;
    private IPainter gaugeForMiro;
    private VideoPlayer videoPlayerForHiro;
    private VideoPlayer videoPlayerForMiro;

    private TextureShaderProgram textureProgram;
    private ColorShaderProgram colorProgram;

    private int texture;
    private int naviItemIdx = 0;

    public MyGLRenderer(Context context) {
        mContext = context;
        markerHiroID = ARToolKit.getInstance().addMarker("single;Data/hiro.patt;80");
        markerMiroID = ARToolKit.getInstance().addMarker("single;Data/miro.patt;80");
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        gaugeForHiro = new GaugeForHiro();
        gaugeForMiro = new GaugeForMiro();
        videoPlayerForHiro = new VideoPlayer(mContext.getResources().openRawResourceFd(R.raw.inst1));
        videoPlayerForMiro = new VideoPlayer(mContext.getResources().openRawResourceFd(R.raw.inst2));

        textureProgram = new TextureShaderProgram(mContext);
        colorProgram = new ColorShaderProgram(mContext);

//        texture = TextureHelper.loadTexture(mContext, R.drawable.air_hockey_surface);
        texture = TextureHelper.loadTexture(mContext, R.drawable.gauge);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (!ARToolKit.getInstance().isRunning()) {
            return;
        }


        if(naviItemIdx == 1){
            stopMediaPlayer();

            if (ARToolKit.getInstance().queryMarkerVisible(markerHiroID)) {
                float[] projectionMatrix = ARToolKit.getInstance().getProjectionMatrix();
                float[] modelMatrix = ARToolKit.getInstance().queryMarkerTransformation(markerHiroID);
                final float[] temp = new float[16];
                multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
                System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);

                textureProgram.useProgram();
                textureProgram.setUniforms(projectionMatrix, texture);
                gaugeForHiro.bindData(textureProgram);
                gaugeForHiro.draw();
            }
//            if (ARToolKit.getInstance().queryMarkerVisible(markerMiroID)) {
//                float[] projectionMatrix = ARToolKit.getInstance().getProjectionMatrix();
//                float[] modelMatrix = ARToolKit.getInstance().queryMarkerTransformation(markerMiroID);
//                final float[] temp = new float[16];
//                multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
//                System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
//
//                textureProgram.useProgram();
//                textureProgram.setUniforms(projectionMatrix, texture);
//                gaugeForMiro.bindData(textureProgram);
//                gaugeForMiro.draw();
//            }
        }
        else if(naviItemIdx == 2){
            if (ARToolKit.getInstance().queryMarkerVisible(markerHiroID)) {
                float[] projectionMatrix = ARToolKit.getInstance().getProjectionMatrix();
                float[] modelMatrix = ARToolKit.getInstance().queryMarkerTransformation(markerHiroID);
                final float[] temp = new float[16];
                multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
                System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);

                if (!videoPlayerForHiro.getMediaPlayer().isPlaying()) {
                    videoPlayerForHiro.getMediaPlayer().start();
                }
                videoPlayerForHiro.draw(projectionMatrix);
            }else{
                if (videoPlayerForHiro.getMediaPlayer().isPlaying()) {
                    videoPlayerForHiro.getMediaPlayer().pause();
                }
            }
//            if (ARToolKit.getInstance().queryMarkerVisible(markerMiroID)) {
//                float[] projectionMatrix = ARToolKit.getInstance().getProjectionMatrix();
//                float[] modelMatrix = ARToolKit.getInstance().queryMarkerTransformation(markerHiroID);
//                final float[] temp = new float[16];
//                multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
//                System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
//
//                if (!videoPlayerForMiro.getMediaPlayer().isPlaying()) {
//                    videoPlayerForMiro.getMediaPlayer().start();
//                }
//                videoPlayerForMiro.draw(projectionMatrix);
//            }else{
//                if (videoPlayerForMiro.getMediaPlayer().isPlaying()) {
//                    videoPlayerForMiro.getMediaPlayer().pause();
//                }
//            }
        }
    }


    public void stopMediaPlayer() {
        if (videoPlayerForHiro != null) {
            videoPlayerForHiro.getMediaPlayer().stop();
        }

        if (videoPlayerForMiro != null) {
            videoPlayerForMiro.getMediaPlayer().stop();
        }
    }

    public void setNaviItemIdx(int naviItemIdx){
        this.naviItemIdx = naviItemIdx;
    }
}
