package zju.homework.augmentedstudio.Interfaces;

import com.vuforia.State;

import zju.homework.augmentedstudio.Utils.ARApplicationException;

/**
 * Created by stardust on 2016/12/12.
 */

public interface ARApplicationControl {

    // To be called to initialize the trackers
    boolean doInitTrackers();


    // To be called to load the trackers' data
    boolean doLoadTrackersData();


    // To be called to start tracking with the initialized trackers and their
    // loaded data
    boolean doStartTrackers();


    // To be called to stop the trackers
    boolean doStopTrackers();


    // To be called to destroy the trackers' data
    boolean doUnloadTrackersData();


    // To be called to deinitialize the trackers
    boolean doDeinitTrackers();


    // This callback is called after the Vuforia initialization is complete,
    // the trackers are initialized, their data loaded and
    // tracking is ready to start
    void onInitARDone(ARApplicationException e);

    // This callback is called every cycle
    void onVuforiaUpdate(State state);

}
