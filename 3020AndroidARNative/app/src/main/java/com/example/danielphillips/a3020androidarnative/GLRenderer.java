package com.example.danielphillips.a3020androidarnative;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;

import com.wikitude.common.rendering.RenderExtension;

import java.nio.IntBuffer;
import java.util.TreeMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {

    private RenderExtension mWikitudeRenderExtension = null;
    private TreeMap<String, Renderable> mOccluders = new TreeMap<>();
    private TreeMap<String, Renderable> mRenderables = new TreeMap<>();


    public GLRenderer(RenderExtension wikitudeRenderExtension) {
        mWikitudeRenderExtension = wikitudeRenderExtension;
        /*
         * Until Wikitude SDK version 2.1 onDrawFrame triggered also a logic update inside the SDK core.
         * This behaviour is deprecated and onUpdate should be used from now on to update logic inside the SDK core. <br>
         *
         * The default behaviour is that onDrawFrame also updates logic. <br>
         *
         * To use the new separated drawing and logic update methods, RenderExtension.useSeparatedRenderAndLogicUpdates should be called.
         * Otherwise the logic will still be updated in onDrawFrame.
         */
        mWikitudeRenderExtension.useSeparatedRenderAndLogicUpdates();
    }


    @Override
    public synchronized void onDrawFrame(final GL10 unused) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //Log.d("GLRender","OnDrawFrame");
        if (mWikitudeRenderExtension != null) {
            // Will trigger a logic update in the SDK
            mWikitudeRenderExtension.onUpdate();
            // will trigger drawing of the camera frame
            mWikitudeRenderExtension.onDrawFrame(unused);

        }
        if(FrameCaptureEnabled) {

            Current = takeScreenshot(1680, 1080, unused);
        }
        for (TreeMap.Entry<String, Renderable> pairOccluder : mOccluders.entrySet()) {
            Renderable renderable = pairOccluder.getValue();

            renderable.onDrawFrame();
        }

        for (TreeMap.Entry<String, Renderable> pairRenderables : mRenderables.entrySet()) {
            Renderable renderable = pairRenderables.getValue();
            renderable.onDrawFrame();
        }
    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        if (mWikitudeRenderExtension != null) {
            mWikitudeRenderExtension.onSurfaceCreated(unused, config);
        }

        for (TreeMap.Entry<String, Renderable> pairOccluder : mOccluders.entrySet()) {
            Renderable renderable = pairOccluder.getValue();
            renderable.onSurfaceCreated();
        }

        for (TreeMap.Entry<String, Renderable> pairRenderables : mRenderables.entrySet()) {
            Renderable renderable = pairRenderables.getValue();
            renderable.onSurfaceCreated();
        }
    }
    public static boolean FrameCaptureEnabled = false;



    public static Bitmap SavePixels(int x, int y, int w, int h, GL10 gl)
    {
        int b[]=new int[w*(y+h)];
        int bt[]=new int[w*h];
        IntBuffer ib=IntBuffer.wrap(b);
        ib.position(0);
        gl.glReadPixels(x, 0, w, y+h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);

        for(int i=0, k=0; i<h; i++, k++)
        {//remember, that OpenGL bitmap is incompatible with Android bitmap
            //and so, some correction need.
            for(int j=0; j<w; j++)
            {
                int pix=b[i*w+j];
                int pb=(pix>>16)&0xff;
                int pr=(pix<<16)&0x00ff0000;
                int pix1=(pix&0xff00ff00) | pr | pb;
                bt[(h-k-1)*w+j]=pix1;
            }
        }


        Bitmap sb=Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
        return sb;
    }
    public Bitmap takeScreenshot(int h, int w, GL10 mGL) {
        final int mWidth = w;
        final int mHeight = h;
        IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
        IntBuffer ibt = IntBuffer.allocate(mWidth * mHeight);
        mGL.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);

        // Convert upside down mirror-reversed image to right-side up normal image.
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                ibt.put((mHeight - i - 1) * mWidth + j, ib.get(i * mWidth + j));
            }
        }

        Bitmap mBitmap = Bitmap.createBitmap(mWidth, mHeight,Bitmap.Config.ARGB_8888);
        mBitmap.copyPixelsFromBuffer(ibt);
        return mBitmap;
    }
    static Bitmap Current;
    @Override
    public void onSurfaceChanged(final GL10 unused, final int width, final int height) {
        if (mWikitudeRenderExtension != null) {
            mWikitudeRenderExtension.onSurfaceChanged(unused, width, height);
            Current = takeScreenshot(width,height,unused);
            Log.d("GLRENDERER","screenshot");

        }
    }

    public void onResume() {
        if (mWikitudeRenderExtension != null) {
            mWikitudeRenderExtension.onResume();
        }
    }

    public void onPause() {
        if (mWikitudeRenderExtension != null) {
            mWikitudeRenderExtension.onPause();
        }
    }

    public synchronized void setRenderablesForKey(final String key, final Renderable renderbale, final Renderable occluder) {
        if (occluder != null) {
            mOccluders.put(key, occluder);
        }

        mRenderables.put(key, renderbale);
    }

    public synchronized void removeRenderablesForKey(final String key) {
        mRenderables.remove(key);
        mOccluders.remove(key);
    }

    public synchronized void removeAllRenderables() {
        mRenderables.clear();
        mOccluders.clear();
    }

    public synchronized Renderable getRenderableForKey(final String key) {
        return mRenderables.get(key);
    }

    public synchronized Renderable getOccluderForKey(final String key) {
        return mOccluders.get(key);
    }




}
