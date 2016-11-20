package com.example.joseph.mirar.objects;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.example.joseph.mirar.R;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Joseph on 2016-08-18.
 */
public class VideoPlayer {
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;\n" +
            "uniform mat4 uSTMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "  gl_Position = uMVPMatrix * aPosition;\n" +
            "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
            "}\n";

    private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";

    private FloatBuffer vertexBuffer;
    private final FloatBuffer textureBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mTextureHandle;
    private int mMVPMatrixHandle;
    private int mSTMatrixHandle;

    private MediaPlayer mMediaPlayer;

    private SurfaceTexture mSurfaceTexture;
    private boolean updateSurface = false;

    private int mTextureID;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -80f,  40f, 0.0f,   // top left
            -80f, -40f, 0.0f,   // bottom left
            80f, -40f, 0.0f,   // bottom right
            80f,  40f, 0.0f }; // top right
//    static float squareCoords[] = {
//            -120f,  0.0f, 160.0f,   // top left
//            -120f, 0.0f, 0.0f,   // bottom left
//            120f, 0.0f, 0.0f,   // bottom right
//            120f,  0.0f, 160.0f }; // top right

    static final int COORDS_PER_TEXTURE = 2;
    static float textureCoords[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private static final int FLOAT_SIZE_BYTES = 4;
    private final int vertexStride = COORDS_PER_VERTEX * FLOAT_SIZE_BYTES; // 4 bytes per vertex
    private final int textureStride = COORDS_PER_TEXTURE * FLOAT_SIZE_BYTES;

    private static int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    private float[] mSTMatrix;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public VideoPlayer(AssetFileDescriptor mediaAsset) {

        mSTMatrix = new float[16];

         // texture
        ByteBuffer tb = ByteBuffer.allocateDirect(textureCoords.length * FLOAT_SIZE_BYTES);
        tb.order(ByteOrder.nativeOrder());
        textureBuffer = tb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

        mTextureID = getTextureID();
        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                updateSurface = true;
            }
        });

        Surface surface = new Surface(mSurfaceTexture);
        mMediaPlayer = new MediaPlayer();

        try {
            AssetFileDescriptor afd = mediaAsset;
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        } catch (Exception e) {
            Log.e("VideoPlayer", e.getMessage(), e);
        }

        mMediaPlayer.setSurface(surface);
        mMediaPlayer.setScreenOnWhilePlaying(true);
//        mMediaPlayer.setLooping(true);
        surface.release();

        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("VideoPlayer", "Media player prepare failed");
        }

        synchronized (this) {
            updateSurface = false;
        }

//        mMediaPlayer.start();
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {

        synchronized (this) {
            if (updateSurface) {
                mSurfaceTexture.updateTexImage();
                mSurfaceTexture.getTransformMatrix(mSTMatrix);
                updateSurface = false;
            }
        }

        ByteBuffer vb = ByteBuffer.allocateDirect(squareCoords.length * FLOAT_SIZE_BYTES);
        vb.order(ByteOrder.nativeOrder());
        vertexBuffer = vb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);

        // position
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // texture
        mTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");

        GLES20.glEnableVertexAttribArray(mTextureHandle);

        GLES20.glVertexAttribPointer(mTextureHandle, COORDS_PER_TEXTURE, GLES20.GL_FLOAT, false, textureStride, textureBuffer);

        // mvpMatrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
//        MyGLRenderer.checkGlError("glGetUniformLocation");

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
//        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // stMatrix
        mSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
//        MyGLRenderer.checkGlError("glGetUniformLocation");

        GLES20.glUniformMatrix4fv(mSTMatrixHandle, 1, false, mSTMatrix, 0);
//        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public int getTextureID() {

        final int[] textureIDs = new int[1];

        GLES20.glGenTextures(textureIDs.length, textureIDs, 0);

        if (textureIDs[0] != 0) {
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureIDs[0]);

            GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        }

        if (textureIDs[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureIDs[0];
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }
}
