package com.telenor.possumlib.abstractdetectors;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.changeevents.MetaDataChangeEvent;
import com.telenor.possumlib.changeevents.PossumEvent;
import com.telenor.possumlib.interfaces.IPollComplete;
import com.telenor.possumlib.interfaces.IPossumEventListener;
import com.telenor.possumlib.interfaces.ISensorStatusUpdate;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.FileUtil;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Basic detector abstract, storing all needed components needed to detect and run. Even if not
 * every implementation of this will run but utilize a different method of sampling, it is still
 * the base.
 */
public abstract class AbstractDetector implements IPossumEventListener, Comparable<AbstractDetector> {
    protected static final String tag = AbstractDetector.class.getName();
    private final ReentrantLock lock = new ReentrantLock();
    private boolean isListening;
    private Context context;
    private final PossumBus eventBus;
    public static final int MINIMUM_SAMPLES = 500;
    private String uniqueUserId;
    private IPollComplete pollListener;
    private boolean isAuthenticating; // Default not used for authenticating
    int storedValues;

    protected final List<JsonArray> sessionValues; // = new ConcurrentLinkedQueue<>()
    private final List<ISensorStatusUpdate> listeners = new ArrayList<>();

    /**
     * Constructor for the most basic of all detector abstractions
     *
     * @param context  a valid android context
     * @param eventBus an event bus for internal messages
     */
    protected AbstractDetector(Context context, @NonNull PossumBus eventBus) {
        if (context == null) throw new RuntimeException("Missing context on detector:" + this);
        this.context = context;
        this.eventBus = eventBus;
        sessionValues = createInternalList();
    }

    /**
     * Set the unique user id the detector will use
     *
     * @param uniqueUserId a string representation of the user id, must be unique for user
     */
    public void setUniqueUser(String uniqueUserId) {
        this.uniqueUserId = uniqueUserId;
    }

    /**
     * Handy method for getting a present timestamp
     *
     * @return long timestamp in millis
     */
    public long now() {
        return DateTime.now().getMillis();
    }

    /**
     * Whether the detector is enabled on the phone. This is usually a yes or no, depending on model
     * etc. The detector cannot be used if it is not enabled. All subclasses must check for its
     * respective confirmation of whether or not it exist on the phone. Default the detector is
     * enabled. All detectors must confirm this or it will be so.
     *
     * @return true if sensor is present, false if not present
     */
    public boolean isEnabled() {
        return true;
    }

    protected List<JsonArray> createInternalList() {
        return new ArrayList<>();
    }

    /**
     * Whether the detector is currently available (not same as enabled which is whether or not
     * the detector is something the phone has). Should be overridden by the subclassed detectors.
     *
     * @return true if available or false if not
     */
    public boolean isAvailable() {
        return isPermitted();
    }

    /**
     * The required permission to use the detector, if any. Must be overridden if a permission is
     * needed
     *
     * @return a manifest permission or null for none needed
     */
    public String requiredPermission() {
        return null;
    }

    /**
     * For detectors requiring access to certain privileges - like location or camera,
     *
     * @return whether the user has permitted the use of the sensor. Should be part of the
     * availability.
     */
    public boolean isPermitted() {
        return requiredPermission() == null ||
                ContextCompat.checkSelfPermission(context(), requiredPermission()) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Gives you access to the eventBus used
     *
     * @return the assigned eventBus
     */
    public PossumBus eventBus() {
        return eventBus;
    }

    /**
     * Starts to startListening to the detectors dataSource. If the detector is not enabled, it will not
     * start to startListening. The moment it starts to startListening, a sessionTimestamp is saved recording which
     * interval the detector started to startListening for data.
     *
     * @return true if started to startListening, else false
     */
    public boolean startListening() {
        if (isEnabled() && uniqueUserId != null) { // && isAvailable()
            // Removed isAvailable from listening, it should start to startListening if it detects that it
            // can startListening regardless of whether it is actually available there and then
            isListening = true;
        } else {
            if (!isEnabled()) {
                eventBus.post(new MetaDataChangeEvent(now() + " DETECTOR OFFLINE (" + detectorName() + ") DISABLED"));
            } else if (!isAvailable()) {
                eventBus.post(new MetaDataChangeEvent(now() + " DETECTOR OFFLINE (" + detectorName() + ") UNAVAILABLE"));
            }
        }
        return isListening;
    }

    /**
     * Stops listening to the dataSource. It then checks the validity of the data and if it finds
     * the data sound - it will send it and then delete it. Should the data be invalid, it will
     * simply discard it.
     */
    public void stopListening() {
        if (sessionValues.size() > 0) {
            storeData();
        }
        if (pollListener != null && isAuthenticating) {
            pollListener.pollComplete(this);
        }
        isListening = false;
    }

    protected long uploadFilesSize() {
        long filesSize = 0;
        for (File file : FileUtil.getAllDetectorFiles(context(), detectorName())) {
            filesSize += file.length();
        }
        return filesSize;
    }

    /**
     * Method for setting an eventual model to the detector
     *
     * @param model the model
     */
    public void setModel(Object model) {
        // Empty on purpose, override in relevant detectors
    }

    /**
     * Yields the detectors stored space, either fileSize or streamSize
     *
     * @return number of bytes taken up by file
     */
    public long fileSize() {
        return storedData().length() + uploadFilesSize();
    }

    /**
     * Stores sessionValues to the file determined by detectorType.
     */
    public final void storeData() {
        lock();
        try {
            storeData(storedData());
        } finally {
            unlock();
        }
    }

    /**
     * Stores data and clears memory of stored data
     *
     * @param file file to store data in
     */
    protected void storeData(@NonNull File file) {
        if (isAuthenticating)
            return; // Should the app attempt to authenticate, ignore storing to file
        if (sessionValues.size() > 0) {
            FileUtil.storeLines(file, sessionValues);
            clearData();
        }
    }

    public List<JsonArray> sessionValues() {
        return sessionValues;
    }

    /**
     * Returns a json object with the common things needed to explain detector
     *
     * @return jsonObject with compact form of the detector
     */
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", detectorType());
        object.addProperty("name", detectorName());
        object.addProperty("uniqueUserId", uniqueUserId);
        object.addProperty("isAuthenticating", isAuthenticating);
        object.addProperty("isAvailable", isAvailable());
        object.addProperty("isEnabled", isEnabled());
        object.addProperty("isListening", isListening());
        object.addProperty("stored", storedData().length());
        return object;
    }

    /**
     * Gives the detectors stored data as a restful json object
     *
     * @return a jsonObject for all the data
     */
    public JsonArray jsonData() {
        JsonArray outputArr = new JsonArray();
        for (JsonArray arr : sessionValues) {
            outputArr.add(arr);
        }
        return outputArr;
    }

    /**
     * Handles stopping to startListening and clearing all resources from a detector - or at least it should.
     * Each successive extension of the default method needs to handle its own resources
     */
    public void terminate() {
        stopListening();
    }

    /**
     * Adds a listener for updates to the sensors availability
     *
     * @param listener a given listener for the event
     */
    public void addSensorUpdateListener(ISensorStatusUpdate listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener for updates to the sensors availability
     *
     * @param listener a given listener for the event
     */
    public void removeSensorUpdateListener(ISensorStatusUpdate listener) {
        listeners.remove(listener);
    }

//    /**
//     * Function to handle what happens when data is uploaded
//     *
//     * @param failedException Exception if failed, null if successful
//     */
//    public void uploadedData(Exception failedException) {
//        if (isAuthenticating) return;
//        if (failedException == null) {
//            FileUtil.deleteFile(storedData());
//            clearData();
//            storedValues = 0;
//            Log.d(tag, "Completed upload of:" + detectorName() + ", deleted it");
//        }
//    }

    /**
     * Sends the sensorUpdate to all listeners for sensor availability updates
     */
    public void sensorStatusChanged() {
        for (ISensorStatusUpdate listener : listeners) {
            listener.sensorStatusChanged(detectorType());
        }
    }

    /**
     * Each sensor needs access to a context, the different implementation must take this into account
     *
     * @return the context the sensor supplies
     */
    public Context context() {
        return context;
    }

    /**
     * A handle to the actual stored data
     *
     * @return the File storing the data
     */
    public File storedData() {
        return FileUtil.getFile(context(), detectorName());
    }

    /**
     * Confirms whether detector is actually listening
     *
     * @return true if listening, false if not
     */
    public boolean isListening() {
        return isListening;
    }

    /**
     * Sorts detectors by name alphabetically
     *
     * @param detector detector you want to sort
     * @return default compareTo return
     */
    public int compareTo(@NonNull AbstractDetector detector) {
        return detectorName().compareTo(detector.detectorName());
    }

    /**
     * Yields the detector type from the DetectorType class. Should correspond to match with
     * Sensor. Type and whichever own type of detector
     *
     * @return integer representing the type (Check DetectorType class)
     */
    public abstract int detectorType();

    /**
     * Official "name" of detector, a simple string using the english language. Should not be used
     * for official UI use - if so, use detectorType and map it to a resource name so it can be
     * localized
     *
     * @return string with "name" or "designation" of detector
     */
    public abstract String detectorName();

    /**
     * Define whether the detector should be used for authenticating next, or for listening
     *
     * @param isAuthenticating true if it should be used for authenticating
     */
    public void setAuthenticating(boolean isAuthenticating) {
        this.isAuthenticating = isAuthenticating;
    }

    /**
     * Whether the detector is presently being used for authentication or not
     *
     * @return true if used for authentication, false if not
     */
    public boolean isAuthenticating() {
        return isAuthenticating;
    }

    /**
     * Zip data and move to upload directory.
     */
    public void prepareUpload() {
        if (isAuthenticating) {
            // Should not be a file to upload, ignoring rest of method
            return;
        }
        lock();
        try {
            File file = storedData();
            if (file == null || file.length() == 0) {
                return;
            }
            File zipFile = FileUtil.zipFile(file, new File(file.getAbsolutePath() + ".zip"));
            if (zipFile != null && stageForUpload(zipFile) && !file.delete()) {
                Log.e(tag, "Unable to delete: " + file.getName());
            }
        } finally {
            unlock();
        }
    }

    protected String bucketKey() {
        return "possumlibdata/" + AwesomePossum.versionName(context()) + "/" + detectorName() + "/" + uniqueUserId + "/" + now() + ".zip";
    }

    protected boolean stageForUpload(File file) {
        if (isAuthenticating) {
            // Should not be a file to stage, ignoring method
            return false;
        }
        if (file == null) {
            Log.e(tag, "Stage for upload failed - no file (" + detectorName() + ")");
            return false;
        }
        if (file.length() == 0) {
            return false;
        }
        File dest = FileUtil.toUploadFile(
                context(),
                bucketKey());
        if (!file.renameTo(dest)) {
            Log.e(tag, "Unable to stage: " + file.getName());
        } else {
            Log.i(tag, "Staged for upload:" + dest.getAbsolutePath());
        }
        return true;
    }

    protected void lock() {
        try {
            if (lock.tryLock(20, TimeUnit.SECONDS)) { // TODO: increase timeout
                return;
            }
        } catch (InterruptedException ignore) {
        }
        throw new RuntimeException("Unable to acquire lock");
    }

    protected void unlock() {
        if (lock.isLocked()) {
            lock.unlock();
        }
    }

    /**
     * Default implementation of IPossumEventListener
     *
     * @param object the event received
     */
    public void eventReceived(PossumEvent object) {
    }

    public void clearData() {
        sessionValues.clear();
    }
}