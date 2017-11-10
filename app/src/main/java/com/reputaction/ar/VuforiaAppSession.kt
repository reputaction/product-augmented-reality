package com.reputaction.ar

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.view.OrientationEventListener
import android.view.WindowManager
import com.vuforia.INIT_FLAGS
import com.vuforia.State
import com.vuforia.Vuforia

/**
 * Vuforia app session
 */

public class VuforiaAppSession constructor(mSessionControl: VuforiaSessionControl) : Vuforia.UpdateCallbackInterface {

    private lateinit var mActivity: Activity

    // Vuforia initialization flags
    private var mVuforiaFlags = 0

    // The async tasks to initialize the Vuforia SDK:
    private var mInitVuforiaTask: InitVuforiaTask? = null
    //private var mInitTrackerTask: InitTrackerTask? = null
    //private var mLoadTrackerTask: LoadTrackerTask? = null
    //private var mStartVuforiaTask: StartVuforiaTask? = null
    //private var mResumeVuforiaTask: ResumeVuforiaTask? = null

    // An object used for synchronizing Vuforia initialization, dataset loading
    // and the Android onDestroy() life cycle event. If the application is
    // destroyed while a data set is still being loaded, then we wait for the
    // loading operation to finish before shutting down Vuforia:
    private val mLifecycleLock = Any()

    // Initializes Vuforia and sets up preferences.
    fun initAR(activity: Activity, screeOrientation: Int) {
        mActivity = activity

        // Use an OrientationChangeListener here to capture all orientation changes.  Android
        // will not send an Activity.onConfigurationChanged() callback on a 180 degree rotation,
        // Vuforia needs to react to this change and the VuforiaAppSession needs to update the Projection Matrix.
        val orientationEventListener = object : OrientationEventListener(mActivity) {
            internal var mLastRotation = -1
            override fun onOrientationChanged(p0: Int) {
                val activityRotation = mActivity.windowManager.defaultDisplay.rotation
                if (activityRotation != mLastRotation) {
                    mLastRotation = activityRotation
                }
            }
        }
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }

        // set orientation
        mActivity.requestedOrientation = screeOrientation

        // keep screen on and bright
        mActivity.window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mVuforiaFlags = INIT_FLAGS.GL_20

        // Initialize Vuforia SDK asynchronously
        // dont init twice
        if (mInitVuforiaTask != null) {
            Log.e("errortag", "dont init vuforia twice")
        }
        try {
            mInitVuforiaTask = InitVuforiaTask()
            mInitVuforiaTask?.execute()
        } catch (e: Exception) {
            Log.e("errortag", "vuforia init error")
        }

    }

    override fun Vuforia_onUpdate(p0: State?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private inner class InitVuforiaTask : AsyncTask<Void, Int, Boolean>() {
        private var mProgressValue = -1

        override fun doInBackground(vararg p0: Void?): Boolean {

            Vuforia.setInitParameters(mActivity, mVuforiaFlags, Constants.vuforiaLicenseKey)
            do {
                // Vuforia.init() blocks until an initialization step is
                // complete, then it proceeds to the next step
                mProgressValue = Vuforia.init()
                Log.d("debugtag", "" + mProgressValue)
                publishProgress(mProgressValue)
            } while (!isCancelled && mProgressValue >= 0 && mProgressValue < 100)

            return (mProgressValue > 0)

        }

        protected fun onProgressUpdate(vararg values: Int) {
            // Do something with the progress value "values[0]", e.g. update
            // splash screen, progress bar, etc.
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                Log.d("debugtag", "Vuforia init ok")
            } else {
                Log.d("debugtag", "Vuforia init error")
            }

        }

    }

}