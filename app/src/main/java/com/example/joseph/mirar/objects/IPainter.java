package com.example.joseph.mirar.objects;

import com.example.joseph.mirar.programs.TextureShaderProgram;

/**
 * Created by yeon on 2016-11-09.
 */

public interface IPainter {
    public void bindData(TextureShaderProgram textureProgram);
    public void draw();
}
