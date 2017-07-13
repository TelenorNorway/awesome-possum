package com.telenor.possumexample.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.telenor.possumexample.R;
import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.IPossumTrust;

import java.util.HashMap;
import java.util.Map;

public class IconWheel extends View implements IPossumTrust {
    private Paint offlinePaint = new Paint(), trainingPaint = new Paint(), onlinePaint = new Paint();
    private static int width;
    private static int height;
    private float centerX, centerY;
    private static float iconWidth;
    private static float iconHeight;
    private static float hypotenuse;

    private Map<Integer, SensorContainer> sensors = new HashMap<>();

    @Override
    public void changeInCombinedTrust(float combinedTrustScore, String status) {

    }

    @Override
    public void changeInDetectorTrust(int detectorType, float newTrustScore, String status) {
        SensorContainer sensor = sensors.get(detectorType);
        if (sensor != null) {
            sensor.setStatus("TRAINING".equals(status)?SensorStatus.Training:SensorStatus.Online);
            invalidate();
        }
    }

    @Override
    public void failedToAscertainTrust(Exception exception) {

    }

    private enum SensorStatus {
        Offline,
        Training,
        Online
    }

    private class SensorContainer {
        private Bitmap bitmap;
        private SensorStatus sensorStatus = SensorStatus.Offline;
        private double angleRad;
        private Rect rect;

        public SensorContainer(Bitmap bitmap, float angle) {
            this.bitmap = bitmap;
            angleRad = (Math.PI / 180) * angle;
        }

        public Bitmap bitmap() {
            return bitmap;
        }
        public void setStatus(SensorStatus sensorStatus) {
            this.sensorStatus = sensorStatus;
        }

        public void setRect(Rect rect) {
            this.rect = rect;
        }

        public Rect rect() {
            return rect;
        }

        public SensorStatus sensorStatus() {
            return sensorStatus;
        }

        public void updateRect() {
            rect = new Rect((int) (centerX - iconWidth / 2), (int) (centerY - iconHeight / 2), (int) (centerX + iconWidth / 2), (int) (centerY + iconHeight / 2));
            rect.offset((int) (hypotenuse * Math.cos(angleRad)), (int) (hypotenuse * Math.sin(angleRad)));
        }
    }

    public IconWheel(Context context) {
        super(context);
        init();
    }

    public IconWheel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IconWheel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        sensors.put(DetectorType.Accelerometer, new SensorContainer(
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_screen_rotation_black_48dp),
                320));
        sensors.put(DetectorType.Bluetooth, new SensorContainer(
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_bluetooth_black_48dp),
                0
        ));
        sensors.put(DetectorType.Audio, new SensorContainer(
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_volume_up_black_48dp),
                40
        ));
        sensors.put(DetectorType.Image, new SensorContainer(
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_camera_front_black_48dp),
                140
        ));
        sensors.put(DetectorType.Position, new SensorContainer(
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_location_on_black_48dp),
                180
        ));
        sensors.put(DetectorType.Wifi, new SensorContainer(
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_wifi_black_48dp),
                220
        ));

        offlinePaint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN));
        trainingPaint.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#FF6600"), PorterDuff.Mode.SRC_IN));
        onlinePaint.setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN));

        iconWidth = pixelValue(30);
        iconHeight = pixelValue(30);
        hypotenuse = pixelValue(140);

        AwesomePossum.addTrustListener(getContext(), this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size;
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (heightMode != MeasureSpec.UNSPECIFIED && widthMode != MeasureSpec.UNSPECIFIED) {
            size = widthWithoutPadding > heightWithoutPadding ? heightWithoutPadding : widthWithoutPadding;
        } else {
            size = Math.max(heightWithoutPadding, widthWithoutPadding);
        }
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(),
                size + getPaddingTop() + getPaddingBottom());
    }

    private float pixelValue(int dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
        int minValue = Math.min(newWidth, newHeight);
        int xOffset = newWidth - minValue;
        int yOffset = newHeight - minValue;
        int paddingTop = this.getPaddingTop() + (yOffset / 2);
        int paddingBottom = this.getPaddingBottom() + (yOffset / 2);
        int paddingLeft = this.getPaddingLeft() + (xOffset / 2);
        int paddingRight = this.getPaddingRight() + (xOffset / 2);
        width = getWidth();
        height = getHeight();
        RectF area = new RectF(
                paddingLeft,
                paddingTop,
                width - paddingRight,
                height - paddingBottom);
        centerX = area.centerX();
        centerY = area.centerY();
        for (SensorContainer sensor: sensors.values()) {
            sensor.updateRect();
        }
        invalidate();
    }

    public void terminate() {
        AwesomePossum.removeTrustListener(this);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (SensorContainer sensor: sensors.values()) {
            Paint paint;
            switch (sensor.sensorStatus()) {
                case Training:
                    paint = trainingPaint;
                    break;
                case Offline:
                    paint = offlinePaint;
                    break;
                default:
                    paint = onlinePaint;
                    break;
            }
            canvas.drawBitmap(sensor.bitmap(), null, sensor.rect(), paint);
        }
    }
}