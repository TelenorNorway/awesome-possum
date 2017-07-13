package com.telenor.possumexample.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.view.View;

/**
 *
 */
public class TrustWheel extends View {
    private RectF circleBounds;
    private RectF circleInnerBounds;
    private Paint circlePaint = new Paint();
    private Paint circleInnerPaint = new Paint();
    private Paint trianglePaint = new Paint();
    private Paint circleInnerStrokePaint = new Paint();
    private float trustScore;
    private float oldTrustScore;
    private float newTrustScore;
    private int layoutWidth;
    private int layoutHeight;
    private int width;
    private int height;
    private float progressRads;
    private float hypotenuse;
    private float centerX, centerY;
    private float timeUsed;
    private int authTime;
    private Path trianglePath = new Path();

    public TrustWheel(Context context, int authTime) {
        super(context);
        this.authTime = authTime;
        init();
    }

    public void setTrustScore(float score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Invalid trustScore, must be within 0 and 100. Was " + score + ".");
        }
        animate().start();
        if (score < 20) {
            circleInnerPaint.setColor(Color.RED);
        } else if (score < 40) {
            circleInnerPaint.setColor(Color.parseColor("#FF6600"));
        } else if (score < 60) {
            circleInnerPaint.setColor(Color.parseColor("#FFCC00"));
        } else if (score < 80) {
            circleInnerPaint.setColor(Color.parseColor("#90EE90"));
        } else {
            circleInnerPaint.setColor(Color.GREEN);
        }
        if (score != trustScore) {
            oldTrustScore = trustScore;
            newTrustScore = score;
            timeUsed = 0;
            getHandler().postDelayed(animation(), animInterval());
        }
    }

    private Runnable animation() {
        return new Runnable() {
            @Override
            public void run() {
                // y = cos((t+1)π)/2+0.5 // Accelerate/Decelerate interpolator
                // http://cogitolearning.co.uk/?p=1078
                timeUsed += animInterval();
                float timeDiff = timeUsed / animTime();
                Rect oldTrust = triangleTrust(trustScore);
                trustScore = oldTrustScore + (float) ((newTrustScore - oldTrustScore) * (Math.cos((timeDiff + 1) * Math.PI) / 2 + 0.5));
                Rect newTrust = triangleTrust(trustScore);
                newTrust.union(oldTrust);
                invalidate(newTrust);
                if (timeUsed < animTime()) {
                    getHandler().postDelayed(animation(), animInterval());
                }
            }
        };
    }

    private int animTime() {
        return 500;
    }

    private int animInterval() {
        return 20;
    }

    private void init() {
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(barWidth());
        circleInnerPaint.setAntiAlias(true);
        circleInnerPaint.setStyle(Paint.Style.FILL);
        circleInnerStrokePaint.setAntiAlias(true);
        circleInnerStrokePaint.setStyle(Paint.Style.STROKE);
        circleInnerStrokePaint.setStrokeWidth(4);
        circleInnerStrokePaint.setColor(Color.BLACK);
        trianglePaint.setAntiAlias(true);
        trianglePaint.setStyle(Paint.Style.FILL);
        trianglePaint.setColor(Color.BLACK);
    }

    /**
     * Milliseconds passed between auth attempts. Sets
     *
     * @param timePassed milliseconds passed
     */
    public void setProgress(int timePassed) {
        progressRads = (float) (360 * timePassed / authTime);
        invalidate(new Rect((int) circleInnerBounds.left, (int) circleInnerBounds.top, (int) circleInnerBounds.right, (int) circleInnerBounds.bottom));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        invalidate();
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
            size = widthWithoutPadding > heightWithoutPadding?heightWithoutPadding:widthWithoutPadding;
        } else {
            size = Math.max(heightWithoutPadding, widthWithoutPadding);
        }
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(),
                size + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
        layoutWidth = newWidth;
        layoutHeight = newHeight;
        defineBounds();
        invalidate();
    }

    /**
     * Width of bar showing colors
     *
     * @return int with size
     */
    private int barWidth() {
        return 50;
    }

    /**
     * Inner padding between inner button and outer bar
     *
     * @return width
     */
    private int innerPadding() {
        return 100;
    }

    /**
     * Length of sides triangle pointing to current value has
     *
     * @return length
     */
    private float triangleSize() {
        return 50f;
    }

    private Rect triangleTrust(float trustScore) { // sin for høyde, cos for bredde
        // 100 TS * x = 330 grader = 33/10 = 3.3 grader pr trustScore
        double rads = (Math.PI / 180) * ((trustScore * 3.3f) + 90);
        double upperRads = rads + Math.PI + Math.PI / 6;
        double lowerRads = rads + Math.PI - Math.PI / 6;
        float pointX = centerX + (float) Math.cos(rads) * hypotenuse;
        float pointY = centerY + (float) Math.sin(rads) * hypotenuse;
        float leftX = pointX + triangleSize() * (float) Math.cos(upperRads);
        float leftY = pointY + triangleSize() * (float) Math.sin(upperRads);
        float rightX = pointX + triangleSize() * (float) Math.cos(lowerRads);
        float rightY = pointY + triangleSize() * (float) Math.sin(lowerRads);
        int minX = (int) Math.min(leftX, rightX);
        int maxX = (int) Math.max(leftX, rightX);
        int minY = (int) Math.min(leftY, rightY);
        int maxY = (int) Math.max(leftY, rightY);
        return new Rect(minX, minY, maxX, maxY);
    }

    private void defineBounds() {
        int minValue = Math.min(layoutWidth, layoutHeight);
        int xOffset = layoutWidth - minValue;
        int yOffset = layoutHeight - minValue;
        int paddingTop = this.getPaddingTop() + (yOffset / 2);
        int paddingBottom = this.getPaddingBottom() + (yOffset / 2);
        int paddingLeft = this.getPaddingLeft() + (xOffset / 2);
        int paddingRight = this.getPaddingRight() + (xOffset / 2);
        width = getWidth();
        height = getHeight();
        circleBounds = new RectF(
                paddingLeft + barWidth(),
                paddingTop + barWidth(),
                width - paddingRight - barWidth(),
                height - paddingBottom - barWidth());
        circleInnerBounds = new RectF(
                paddingLeft + barWidth() + innerPadding(),
                paddingTop + barWidth() + innerPadding(),
                width - paddingRight - barWidth() - innerPadding(),
                height - paddingBottom - barWidth() - innerPadding());
        int[] colors = new int[]{0xffFF0000, 0xffFF6600, 0xffFFCC00, 0xff90EE90, 0xff00FF00};
        SweepGradient sweepGradient = new SweepGradient(width / 2, height / 2, colors, null);
        Matrix gradientMatrix = new Matrix();
        gradientMatrix.preRotate(90f, circleBounds.centerX(), circleBounds.centerY());
        sweepGradient.setLocalMatrix(gradientMatrix);
        circlePaint.setShader(sweepGradient);
        hypotenuse = width / 2 - paddingRight - barWidth() - barWidth() / 2; // Assumes width = height
        centerX = circleBounds.centerX();
        centerY = circleBounds.centerY();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(circleBounds, 90, 330, false, circlePaint);
        canvas.drawArc(circleInnerBounds, 0, 360, false, circleInnerPaint);
        canvas.drawArc(circleInnerBounds, 270, progressRads, false, circleInnerStrokePaint);
        double rads = (Math.PI / 180) * ((trustScore * 3.3f) + 90); // 100 TS * x = 330 grader = 33/10 = 3.3 grader pr trustScore
        float triX = centerX + (float) Math.cos(rads) * hypotenuse;
        float triY = centerY + (float) Math.sin(rads) * hypotenuse;
        double upperRads = rads + Math.PI + (Math.PI / 180) * 30;
        double lowerRads = rads + Math.PI - (Math.PI / 180) * 30; // sin for høyde, cos for bredde
        trianglePath.reset();
        trianglePath.moveTo(triX, triY);
        trianglePath.lineTo(triX + triangleSize() * (float) Math.cos(upperRads), triY + triangleSize() * (float) Math.sin(upperRads));
        trianglePath.lineTo(triX + triangleSize() * (float) Math.cos(lowerRads), triY + triangleSize() * (float) Math.sin(lowerRads));
        trianglePath.close();
        canvas.drawPath(trianglePath, trianglePaint);
    }

    public float trustScore() {
        return trustScore;
    }
}