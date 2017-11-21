package com.reputaction.ar

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.vuforia.Device
import com.vuforia.Renderer
import com.vuforia.State
import com.vuforia.Vuforia
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ReputactionAppRenderer(private val session: VuforiaAppSession, mActivity: MainActivity) : GLSurfaceView.Renderer, VuforiaAppRendererControl {

    private var vuforiaAppRenderer: VuforiaAppRenderer

    private var mIsActive: Boolean = false

    init {
        vuforiaAppRenderer = VuforiaAppRenderer(this, mActivity, Device.MODE.MODE_AR, false, 0.010f, 5f)
    }

    override fun onDrawFrame(p0: GL10?) {
        vuforiaAppRenderer.render()
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        session.onSurfaceChanged(width, height)
        vuforiaAppRenderer.onConfigurationChanged(mIsActive)
        initRendering()
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        session.onSurfaceCreated()
        vuforiaAppRenderer.onSurfaceCreated()
    }

    override fun renderFrame(state: State, projectionMatrix: FloatArray) {
        vuforiaAppRenderer.renderVideoBackground()

        Renderer.getInstance().end()
    }

    fun setActive(active: Boolean) {
        mIsActive = active

        if (mIsActive) {
            vuforiaAppRenderer.configureVideoBackground()
        }
    }

    private fun initRendering() {
        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, if (Vuforia.requiresAlpha())
            0.0f
        else
            1.0f)
    }

}