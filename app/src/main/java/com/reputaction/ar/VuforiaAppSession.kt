package com.reputaction.ar

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.view.OrientationEventListener
import android.view.WindowManager
import com.vuforia.CameraDevice
import com.vuforia.INIT_FLAGS
import com.vuforia.State
import com.vuforia.Vuforia

/**
 * Vuforia app session
 */

class VuforiaAppSession constructor(val mSessionControl: VuforiaSessionControl) : Vuforia.UpdateCallbackInterface {

    private lateinit var mActivity: Activity

    // Vuforia initialization flags
    private var mVuforiaFlags = 0

    // flag if vuforia is started
    private var mStarted = false
    private var mCameraRunning = false

    // camera config
    private var mCamera = CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT

    // The async tasks to initialize the Vuforia SDK:
    private var mInitVuforiaTask: InitVuforiaTask? = null
    private var mInitTrackerTask: InitTrackerTask? = null
    private var mLoadTrackerTask: LoadTrackerTask? = null
    private var mStartVuforiaTask: StartVuforiaTask? = null
    private var mResumeVuforiaTask: ResumeVuforiaTask? = null

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

    fun startAR(camera: Int) {
        try {
            mStartVuforiaTask = StartVuforiaTask()
            mStartVuforiaTask?.execute()
        } catch (e: Exception) {
            Log.e("errortag", "start ar error")
        }
    }

    fun resumeAR() {
        try {
            mResumeVuforiaTask = ResumeVuforiaTask()
            mResumeVuforiaTask?.execute()
        } catch (e: Exception) {
            Log.e("errortag", "res ar error")
        }
    }

    // start camera
    fun startCameraAndTrackers() {
        if (mCameraRunning) {
            Log.e("errortag", "error: camera already running")
            return
        }

        if (!CameraDevice.getInstance().init(mCamera)) {
            Log.e("errortag", "error: open camera")
            return
        }

        if (!CameraDevice.getInstance().selectVideoMode(CameraDevice.MODE.MODE_DEFAULT)) {
            Log.e("errortag", "error: video mode")
            return
        }

        if (!CameraDevice.getInstance().start()) {
            Log.e("errortag", "error: start camera")
            return
        }

        mSessionControl.doStartTrackers()

        mCameraRunning = true
    }

    override fun Vuforia_onUpdate(p0: State?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Methods to be called to handle lifecycle
    fun onResume() {
        if (mResumeVuforiaTask == null) {
            resumeAR()
        }
    }

    private inner class InitVuforiaTask : AsyncTask<Void, Int, Boolean>() {
        private var mProgressValue = -1

        override fun doInBackground(vararg p0: Void?): Boolean {

            // Prevent the onDestroy() method to overlap with initialization
            synchronized(mLifecycleLock) {

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
        }

        protected fun onProgressUpdate(vararg values: Int) {
            // Do something with the progress value "values[0]", e.g. update
            // splash screen, progress bar, etc.
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                Log.d("debugtag", "Vuforia init ok")
                mInitTrackerTask = InitTrackerTask()
                mInitTrackerTask?.execute()
            } else {
                Log.d("debugtag", "Vuforia init error")
            }

        }
    }

    private inner class InitTrackerTask : AsyncTask<Void, Int, Boolean>() {

        override fun doInBackground(vararg p0: Void?): Boolean {
            synchronized(mLifecycleLock)
            {
                // load dataset
                return mSessionControl.doInitTrackers()
            }
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                Log.d("debugtag", "tracker init ok")
                mLoadTrackerTask = LoadTrackerTask()
                mLoadTrackerTask?.execute()
            } else {
                Log.d("debugtag", "tracker init error")
            }
        }
    }

    private inner class LoadTrackerTask : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg p0: Void?): Boolean {
            synchronized(mLifecycleLock)
            {
                synchronized(mLifecycleLock)
                {
                    return mSessionControl.doLoadTrackersData()
                }
            }
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                Log.d("debugtag", "load target init ok")

                // Hint to the virtual machine that it would be a good time to
                // run the garbage collector
                System.gc()

                Vuforia.registerCallback(this@VuforiaAppSession)

                mStarted = true
            } else {
                Log.d("debugtag", "load target init error")
            }

            // Done loading the tracker, update application status, send the
            // exception to check errors
            mSessionControl.onInitARDone()
        }
    }

    private inner class ResumeVuforiaTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg p0: Void?): Void? {
            synchronized(mLifecycleLock) {
                Vuforia.onResume()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {

            Log.d("debugtag", "" + mStarted)
            Log.d("debugtag", "" + mCameraRunning)


            //start the camera only if the Vuforia SDK has already been initialized
            if (mStarted && !mCameraRunning) {
                startAR(mCamera)
            }
        }
    }

    private inner class StartVuforiaTask : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg p0: Void?): Boolean {
            synchronized(mLifecycleLock)
            {
                startCameraAndTrackers()
            }

            return true
        }

        override fun onPostExecute(result: Boolean?) {
            mSessionControl.onVuforiaStarted()
        }

    }

}