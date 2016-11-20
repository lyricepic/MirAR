package com.example.joseph.mirar.objects;


import com.example.joseph.mirar.data.VertexArray;
import com.example.joseph.mirar.programs.TextureShaderProgram;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static com.example.joseph.mirar.Constants.BYTES_PER_FLOAT;

/**
 * Created by Joseph on 2016-09-27.
 */

public class GaugeForHiro implements IPainter{

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private static final int POINTS = 18;
    private static final int SIZE = 60;
    private static final int POSITION_X = 0;
    private static final int POSITION_Y = 110;

    private static final float[] VERTEX_DATA = {
            0f, 0f, 0.5f, 0.5f,
            -40f, -40f, 0f, 0.9f,
            40f, -40f, 1f, 0.9f,
            40f, 40f, 1f, 0.1f,
            -40f, 40f, 0f, 0.1f,
            -40f, -40f, 0f, 0.9f
    };

    private final VertexArray vertexArray;

    public GaugeForHiro() {
//        vertexArray = new VertexArray(VERTEX_DATA);
        vertexArray = new VertexArray(makeCircle(POINTS));
    }

    @Override
    public void bindData(TextureShaderProgram textureProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                textureProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE
        );

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE
        );
    }

    @Override
    public void draw() {
        glDrawArrays(GL_TRIANGLE_FAN, 0, POINTS + 2);
    }

    private float[] makeCircle(int points) {
        float[] vertexData = new float[(points + 2) * 4];

        int arrIdx = 0;
        vertexData[arrIdx++] = 0 + POSITION_X;
        vertexData[arrIdx++] = 0 - POSITION_Y;
        vertexData[arrIdx++] = 0.5f;
        vertexData[arrIdx++] = 0.5f;

        for (int i = 0; i < points; i++) {
            float radius = (float) (2 * Math.PI * i / points);
            float xCos = (float) Math.cos(radius);
            float ySin = (float) Math.sin(radius);

            vertexData[arrIdx++] = xCos * SIZE + POSITION_X;
            vertexData[arrIdx++] = ySin * SIZE - POSITION_Y;
            vertexData[arrIdx++] = xCos * 0.5f + 0.5f;
            vertexData[arrIdx++] = 1.0f - (ySin * 0.5f + 0.5f);
        }

        // 시작 포인트를 마지막에 넣어줘야 제대로 된 도형이 표시됨
        vertexData[arrIdx++] = vertexData[4];
        vertexData[arrIdx++] = vertexData[5];
        vertexData[arrIdx++] = vertexData[6];
        vertexData[arrIdx++] = vertexData[7];

        return vertexData;
    }
}
