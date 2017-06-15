package com.telenor.possumlib.models;

import android.support.annotation.NonNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Short hand method of implementing guavas CountingOutputStream
 */
public class CountingOutputStream extends FilterOutputStream {
    private long count;
    /**
     * Wraps another output stream, counting the number of bytes written.
     *
     * @param out the output stream to be wrapped
     */
    public CountingOutputStream(@NonNull OutputStream out) {
        super(out);
    }

    /** Returns the number of bytes written. */
    public long getCount() {
        return count;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        count += len;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        count++;
    }

    // Overriding close() because FilterOutputStream's close() method pre-JDK8 has bad behavior:
    // it silently ignores any exception thrown by flush(). Instead, just close the delegate stream.
    // It should flush itself if necessary.
    @Override
    public void close() throws IOException {
        out.close();
    }
}