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
import com.telenor.possumlib.interfaces.IWrite;
import com.telenor.possumlib.utils.FileUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AsyncUpload extends AsyncTask<Void, Integer, Exception> {
    protected Context context;
    private IWrite listener;
    private String uploadArea;
    private TransferUtility transferUtility;
    List<File> filesToUpload;

    private final AtomicInteger filesLeft = new AtomicInteger();
    private final AtomicInteger filesCanceled = new AtomicInteger();
    private final AtomicInteger filesFailed = new AtomicInteger();
    private final AtomicLong bytesTransferred = new AtomicLong();

    private int totalNumberOfFiles;
    private long totalNumberOfBytes;

    private static final String tag = AsyncUpload.class.getName();

    public AsyncUpload(@NonNull Context context, @NonNull IWrite listener, @NonNull TransferUtility transferUtility, @NonNull String uploadArea, List<File> filesToUpload) {
        this.context = context;
        this.listener = listener;
        this.transferUtility = transferUtility;
        this.filesToUpload = filesToUpload;
        this.uploadArea = uploadArea;
    }

    @Override
    protected Exception doInBackground(Void... params) {
        return upload();
    }

    /**
     * The upload called in the background thread
     * @return null if successful, or an exception if it failed
     */
    private Exception upload() {
        Log.i(tag, "Files ready for upload:"+filesToUpload.size());
        totalNumberOfFiles = filesToUpload.size();
        if (totalNumberOfFiles == 0) {
            // This should not happen during normal use.
            done();
        }
        filesLeft.set(totalNumberOfFiles);
        filesCanceled.set(0);
        filesFailed.set(0);
        bytesTransferred.set(0);
        totalNumberOfBytes = 0;
        for (File file : filesToUpload ) {
            totalNumberOfBytes += file.length();
            transferUtility // Switch out upload below with a path?
                    .upload(uploadArea, FileUtil.toBucketKey(file), file)
                    .setTransferListener(createTransferListener(file, filesLeft));
        }

        return null;
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
                            Log.e(tag, "Could not delete: " + file);
                        }
                        oneDone();
                        break;
                    case CANCELED:
                        Log.w(tag, "Cancelled: " + ticketTransfer);
                        filesCanceled.getAndIncrement();
                        oneDone();
                        break;
                    case FAILED:
                        Log.w(tag, "Failed: " + ticketTransfer);
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
                Log.w(tag, ex);
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