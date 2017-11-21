package com.reputaction.ar

import com.vuforia.State

interface VuforiaAppRendererControl {

    // method has to be implemented by the Renderer class which handles the content rendering of the app,
    fun renderFrame(state: State, projectionMatrix: FloatArray)
}

