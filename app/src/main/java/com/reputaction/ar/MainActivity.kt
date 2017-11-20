package com.reputaction.ar

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.reputaction.ar.util.SimpleGLView
import com.reputaction.ar.util.Texture
import com.vuforia.*
import java.util.*

class MainActivity : AppCompatActivity(), VuforiaSessionControl {

    private lateinit var vuforiaAppSession: VuforiaAppSession

    var mFinderStarted: Boolean = false

    // gl view
    lateinit var mGlView: SimpleGLView

    private lateinit var mRenderer: ReputactionAppRenderer

    // textures for rendering
    private lateinit var mTextures: Vector<Texture>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vuforiaAppSession = VuforiaAppSession(this)

        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        //mTextures = Vector()


        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        vuforiaAppSession.onResume()
    }

    /* VuforiaSessionControl overrides */
    override fun doInitTrackers(): Boolean {
        val tManager = TrackerManager.getInstance()
        var result = true   // indicate if init correctly

        val tracker = tManager.initTracker(ObjectTracker.getClassType())
        if (tracker == null) {
            Log.e(
                    "logtag",
                    "Tracker not initialized. Tracker already initialized or the camera is already started")
            result = false
        } else {
            Log.i("logtag", "tracker init correct")
        }
        return result
    }

    override fun doLoadTrackersData(): Boolean {
        // get object tracker
        val tManager = TrackerManager.getInstance()
        val objectTracker = tManager.getTracker(ObjectTracker.getClassType()) as ObjectTracker

        // init target finder
        val targetFinder = objectTracker.targetFinder

        // start init
        if (targetFinder.startInit(Constants.kAccessKey, Constants.kSecretKey)) {
            targetFinder.waitUntilInitFinished()
        }

        val resultCode = targetFinder.initState
        if (resultCode != TargetFinder.INIT_SUCCESS) {
            Log.e("logtag", "error init target")
        }

        return true
    }

    override fun doStartTrackers(): Boolean {
        val tManager = TrackerManager.getInstance()
        val objectTracker = tManager.getTracker(ObjectTracker.getClassType()) as ObjectTracker
        objectTracker.start()

        // start cloud recognition if we are in scanning mode
        val targetFinder = objectTracker.targetFinder
        targetFinder.startRecognition()

        mFinderStarted = true

        return true
    }

    override fun doStopTrackers(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doUnloadTrackersData(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doDeinitTrackers(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onInitARDone() {
        initApplicationAR()
    }

    override fun onVuforiaUpdate(state: State) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onVuforiaResumed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onVuforiaStarted() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun initApplicationAR() {
        // create opengl es view
        val depthSize = 16
        val stencilSize = 0
        val translucent = Vuforia.requiresAlpha()

        // init gl view
        mGlView = SimpleGLView(this)
        mGlView.init(translucent, depthSize, stencilSize)

        // setup renderer for gl view
        mRenderer = ReputactionAppRenderer(vuforiaAppSession, this)
        mGlView.setRenderer(mRenderer)
    }
}
