package com.telenor.possumexample.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.telenor.possumexample.R;
import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.interfaces.IPossumMessage;
import com.telenor.possumlib.utils.Do;

public class PreviewImage extends AppCompatImageView implements IPossumMessage {
    private static final String tag = PreviewImage.class.getName();
    private boolean showsFace = false;
    public PreviewImage(Context context) {
        super(context);
        init(context);
    }

    public PreviewImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PreviewImage);
        showsFace = a.getBoolean(0, false);
        a.recycle();
        init(context);
    }

    public PreviewImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PreviewImage);
        showsFace = a.getBoolean(0, false);
        a.recycle();
        init(context);
    }

    private void init(Context context) {
        AwesomePossum.addMessageListener(context, this);
    }

    public void destroy() {
        AwesomePossum.removeMessageListener(this);
    }

    @Override
    public void possumMessageReceived(String msgType, String message) {

    }

    @Override
    public void possumFaceFound(final byte[] dataReceived) {
        if (showsFace) {
            final Bitmap image = BitmapFactory.decodeByteArray(dataReceived, 0, dataReceived.length);
            Do.onMain(new Runnable() {
                @Override
                public void run() {
                    setImageBitmap(image);
                }
            });
        }
    }

    @Override
    public void possumImageSnapped(byte[] dataReceived) {
        if (!showsFace) {
            final Bitmap image = BitmapFactory.decodeByteArray(dataReceived, 0, dataReceived.length);
            Do.onMain(new Runnable() {
                @Override
                public void run() {
                    setImageBitmap(image);
                }
            });
        }
    }

    @Override
    public void possumFaceCoordsReceived(int[] xCoords, int[] yCoords) {

    }
}