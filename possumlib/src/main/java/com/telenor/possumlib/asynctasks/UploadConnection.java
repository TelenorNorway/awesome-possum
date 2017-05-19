package com.telenor.possumlib.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.common.base.Joiner;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.interfaces.IWrite;
import com.telenor.possumlib.utils.FileUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.telenor.possumlib.managers.S3ModelDownloader.S3_BUCKET;

public class UploadConnection extends AsyncTask<Void, Integer, Exception> {
    private static final String TAG = UploadConnection.class.getName();
    private final Context context;
    private final TransferUtility transferUtility;

    private final IWrite listener;

    private final AtomicInteger filesLeft = new AtomicInteger();
    private final AtomicInteger filesCanceled = new AtomicInteger();
    private final AtomicInteger filesFailed = new AtomicInteger();
    private final AtomicLong bytesTransferred = new AtomicLong();
    private ConcurrentLinkedQueue<AbstractDetector> detectors = new ConcurrentLinkedQueue<>();

    private int totalNumberOfFiles;
    private long totalNumberOfBytes;
    private static final String tag = UploadConnection.class.getName();

    public UploadConnection(@NonNull Context context, @NonNull IWrite listener, @NonNull TransferUtility transferUtility, ConcurrentLinkedQueue<AbstractDetector> detectors) {
        this.context = context;
        this.detectors = detectors;
        this.transferUtility = transferUtility;
        this.listener = listener;
    }

    @Override
    protected Exception doInBackground(Void... params) {
        // TODO: Send message to service?
        for (AbstractDetector sensor : detectors) {
            sensor.prepareUpload();
        }
        startUpload();
//        new Runnable() {
//            @Override
//            public void run() {
//                startUpload();
//            }
//        }.run();
        return null;
    }

    private void startUpload() {
        List<File> sortedFiles = FileUtil.getFilesReadyForUpload(context);
        Log.i(tag, "Files ready for upload:"+sortedFiles.size());
        totalNumberOfFiles = sortedFiles.size();
        if (totalNumberOfFiles == 0) {
            // This should not happen during normal use.
            done();
        }
        filesLeft.set(totalNumberOfFiles);
        filesCanceled.set(0);
        filesFailed.set(0);
        bytesTransferred.set(0);
        totalNumberOfBytes = 0;
        for (File file : sortedFiles ) {
            totalNumberOfBytes += file.length();
            transferUtility
                    .upload(S3_BUCKET, FileUtil.toBucketKey(file), file)
                    .setTransferListener(createTransferListener(file, filesLeft));
        }
    }

    @VisibleForTesting
    private TransferListener createTransferListener(final File file, final AtomicInteger filesLeft) {
        return new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                String ticketTransfer = "ticket " + file.getName();
                switch (state) {
                    case WAITING_FOR_NETWORK:
                        transferUtility.cancel(id);
                        break;
                    case COMPLETED:
                        if (!file.delete()) {
                            Log.e(TAG, "Could not delete: " + file);
                        }
                        oneDone();
                        break;
                    case CANCELED:
                        Log.w(TAG, "Cancelled: " + ticketTransfer);
                        filesCanceled.getAndIncrement();
                        oneDone();
                        break;
                    case FAILED:
                        Log.w(TAG, "Failed: " + ticketTransfer);
                        filesFailed.getAndIncrement();
                        oneDone();
                        break;
                    default:
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                long oldPercentage = computePercentage();
                bytesTransferred.getAndAdd(bytesCurrent);
                long newPercentage = computePercentage();
                if (newPercentage > oldPercentage) {
                    listener.bytesWritten((int) newPercentage);
                }
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.w(TAG, ex);
            }

            private void oneDone() {
                if (filesLeft.decrementAndGet() == 0) {
                    done();
                }
            }
        };
    }

    private long computePercentage() {
        return 100 * bytesTransferred.get() / totalNumberOfBytes;
    }

    private void done() {
        Log.i(tag, "All done uploading");
        Exception exception = null;
        String message = null;
        if (totalNumberOfFiles == 0) {
            message = "No new data needs to be uploaded.";
        } else {
            int canceled = filesCanceled.get();
            int failed = filesFailed.get();
            int unsuccessful = canceled + failed;
            if (unsuccessful > 0) {
                String exMsg = "Upload " + (unsuccessful == totalNumberOfFiles ? "" : "partly ") + "unsuccessful: "
                        + Joiner.on(", ").skipNulls().join(new String[]{
                        canceled > 0 ? canceled + "/" + totalNumberOfFiles + " canceled" : null,
                        failed > 0 ? failed + "/" + totalNumberOfFiles + " failed" : null,
                });
                exception = new Exception(exMsg);
            }
        }
        listener.uploadComplete(exception, message);
    }
}