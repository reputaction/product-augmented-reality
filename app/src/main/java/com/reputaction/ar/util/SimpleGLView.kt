package com.reputaction.ar.util

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

class SimpleGLView// Constructor.
(context: Context) : GLSurfaceView(context) {


    // Initialization.
    fun init(translucent: Boolean, depth: Int, stencil: Int) {
        // By default GLSurfaceView tries to find a surface that is as close
        // as possible to a 16-bit RGB frame buffer with a 16-bit depth buffer.
        // This function can override the default values and set custom values.

        // By default, GLSurfaceView() creates a RGB_565 opaque surface.
        // If we want a translucent one, we should change the surface's
        // format here, using PixelFormat.TRANSLUCENT for GL Surfaces
        // is interpreted as any 32-bit surface with alpha by SurfaceFlinger.

        Log.i(LOGTAG, "Using OpenGL ES 2.0")
        Log.i(LOGTAG, "Using " + (if (translucent) "translucent" else "opaque")
                + " GLView, depth buffer size: " + depth + ", stencil size: "
                + stencil)

        // If required set translucent format to allow camera image to
        // show through in the background
        if (translucent) {
            this.holder.setFormat(PixelFormat.TRANSLUCENT)
        }

        // Setup the context factory for 2.0 rendering
        setEGLContextFactory(ContextFactory())

        // We need to choose an EGLConfig that matches the format of
        // our surface exactly. This is going to be done in our
        // custom config chooser. See ConfigChooser class definition
        // below.
        setEGLConfigChooser(if (translucent)
            ConfigChooser(8, 8, 8, 8, depth,
                    stencil)
        else
            ConfigChooser(5, 6, 5, 0, depth, stencil))
    }

    // Creates OpenGL contexts.
    private class ContextFactory : GLSurfaceView.EGLContextFactory {


        override fun createContext(egl: EGL10, display: EGLDisplay,
                                   eglConfig: EGLConfig): EGLContext {
            val context: EGLContext

            Log.i(LOGTAG, "Creating OpenGL ES 2.0 context")
            checkEglError("Before eglCreateContext", egl)
            val attrib_list_gl20 = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
            context = egl.eglCreateContext(display, eglConfig,
                    EGL10.EGL_NO_CONTEXT, attrib_list_gl20)

            checkEglError("After eglCreateContext", egl)
            return context
        }


        override fun destroyContext(egl: EGL10, display: EGLDisplay,
                                    context: EGLContext) {
            egl.eglDestroyContext(display, context)
        }

        companion object {
            private val EGL_CONTEXT_CLIENT_VERSION = 0x3098
        }
    }

    // The config chooser.
    private class ConfigChooser(// Subclasses can adjust these values:
            protected var mRedSize: Int, protected var mGreenSize: Int, protected var mBlueSize: Int, protected var mAlphaSize: Int, protected var mDepthSize: Int, protected var mStencilSize: Int) : GLSurfaceView.EGLConfigChooser {
        private val mValue = IntArray(1)


        private fun getMatchingConfig(egl: EGL10, display: EGLDisplay,
                                      configAttribs: IntArray): EGLConfig? {
            // Get the number of minimally matching EGL configurations
            val num_config = IntArray(1)
            egl.eglChooseConfig(display, configAttribs, null, 0, num_config)

            val numConfigs = num_config[0]
            if (numConfigs <= 0)
                throw IllegalArgumentException("No matching EGL configs")

            // Allocate then read the array of minimally matching EGL configs
            val configs = arrayOfNulls<EGLConfig>(numConfigs)
            egl.eglChooseConfig(display, configAttribs, configs, numConfigs,
                    num_config)


            // Now return the "best" one
            return chooseConfig(egl, display, configs)
        }


        override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig? {
            // This EGL config specification is used to specify 2.0
            // rendering. We use a minimum size of 4 bits for
            // red/green/blue, but will perform actual matching in
            // chooseConfig() below.
            val EGL_OPENGL_ES2_BIT = 0x0004
            val s_configAttribs_gl20 = intArrayOf(EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4, EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE)

            return getMatchingConfig(egl, display, s_configAttribs_gl20)
        }


        fun chooseConfig(egl: EGL10, display: EGLDisplay,
                         configs: Array<EGLConfig?>): EGLConfig? {
            for (config in configs) {
                val d = findConfigAttrib(egl, display, config!!,
                        EGL10.EGL_DEPTH_SIZE, 0)
                val s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0)

                // We need at least mDepthSize and mStencilSize bits
                if (d < mDepthSize || s < mStencilSize)
                    continue

                // We want an *exact* match for red/green/blue/alpha
                val r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0)
                val g = findConfigAttrib(egl, display, config,
                        EGL10.EGL_GREEN_SIZE, 0)
                val b = findConfigAttrib(egl, display, config,
                        EGL10.EGL_BLUE_SIZE, 0)
                val a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0)

                if (r == mRedSize && g == mGreenSize && b == mBlueSize
                        && a == mAlphaSize)
                    return config
            }

            return null
        }


        private fun findConfigAttrib(egl: EGL10, display: EGLDisplay,
                                     config: EGLConfig, attribute: Int, defaultValue: Int): Int {

            return if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) mValue[0] else defaultValue

        }
    }

    companion object {
        private val LOGTAG = "Vuforia_SampleGLView"


        // Checks the OpenGL error.
        private fun checkEglError(prompt: String, egl: EGL10) {
            var error: Int = egl.eglGetError()
            while ((error) != EGL10.EGL_SUCCESS) {
                error = egl.eglGetError()
                Log.e(LOGTAG, String.format("%s: EGL error: 0x%x", prompt, error))
            }
        }
    }
}


