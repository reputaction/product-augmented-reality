package com.reputaction.ar

import com.vuforia.State

/**
 * Interface to be implemented by the activity which uses VuforiaAppSession
 */

interface VuforiaSessionControl {

    // To be called to initialize the trackers
    fun doInitTrackers(): Boolean


    // To be called to load the trackers' data
    fun doLoadTrackersData(): Boolean


    // To be called to start tracking with the initialized trackers and their
    // loaded data
    fun doStartTrackers(): Boolean


    // To be called to stop the trackers
    fun doStopTrackers(): Boolean


    // To be called to destroy the trackers' data
    fun doUnloadTrackersData(): Boolean


    // To be called to deinitialize the trackers
    fun doDeinitTrackers(): Boolean


    // This callback is called after the Vuforia initialization is complete,
    // the trackers are initialized, their data loaded and
    // tracking is ready to start
    fun onInitARDone()


    // This callback is called every cycle
    fun onVuforiaUpdate(state: State)


    // This callback is called on Vuforia resume
    fun onVuforiaResumed()


    // This callback is called once Vuforia has been started
    fun onVuforiaStarted()

}
