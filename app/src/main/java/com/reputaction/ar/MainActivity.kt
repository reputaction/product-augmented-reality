package com.reputaction.ar

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.vuforia.ObjectTracker
import com.vuforia.State
import com.vuforia.TrackerManager

class MainActivity : AppCompatActivity(), VuforiaSessionControl {

    private lateinit var vuforiaAppSession: VuforiaAppSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vuforiaAppSession = VuforiaAppSession(this)

        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)


        setContentView(R.layout.activity_main)
    }

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doStartTrackers(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    override fun onInitARDone(e: VuforiaAppSession) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
}
