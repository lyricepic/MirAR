package com.example.joseph.mirar;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.joseph.mirar.objects.Mallet;
import com.example.joseph.mirar.objects.Table;
import com.example.joseph.mirar.programs.ColorShaderProgram;
import com.example.joseph.mirar.programs.TextureShaderProgram;
import com.example.joseph.mirar.util.TextureHelper;

import org.artoolkit.ar.base.ARToolKit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by Joseph on 2016-08-18.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";

    private int markerID = -1;
    private final Context mContext;
    private Table table;
    private Mallet mallet;
    private float[] modelMatrix;
    private float[] projectionMatrix;

    private TextureShaderProgram textureProgram;
    private ColorShaderProgram colorProgram;

    private int texture;

    public MyGLRenderer(Context context) {
        mContext = context;
        markerID = ARToolKit.getInstance().addMarker("single;Data/hiro.patt;80");
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        table = new Table();
        mallet = new Mallet();

        textureProgram = new TextureShaderProgram(mContext);
        colorProgram = new ColorShaderProgram(mContext);

        texture = TextureHelper.loadTexture(mContext, R.drawable.air_hockey_surface);
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

        if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {
            projectionMatrix = ARToolKit.getInstance().getProjectionMatrix();
            modelMatrix = ARToolKit.getInstance().queryMarkerTransformation(markerID);

            final float[] temp = new float[16];
            multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
            System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);

            textureProgram.useProgram();
            textureProgram.setUniforms(projectionMatrix, texture);
            table.bindData(textureProgram);
            table.draw();

            colorProgram.useProgram();
            colorProgram.setUniforms(projectionMatrix);
            mallet.bindData(colorProgram);
            mallet.draw();
        }
    }
}
