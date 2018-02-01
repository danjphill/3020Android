package com.example.danielphillips.a3020androidarnative;

public abstract class Renderable {
    public float[] projectionMatrix = null;
    public float[] viewMatrix = null;

    public abstract void onSurfaceCreated();
    public abstract void onDrawFrame();
}
