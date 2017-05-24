package com.telenor.possumlib.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;

public class ImageUtils {
    private static final String tag = ImageUtils.class.getName();

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private static int[] bitmapToIntArray(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] result = new int[width * height];
        image.getPixels(result, 0, width, 0, 0, width, height);
        return result;
    }

    public static int[][][] bitmapToRGBArray(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][][] result = new int[width][height][3];
        int[] pixels = bitmapToIntArray(image);
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                result[i][j][0] = (pixels[i*j] >> 16) & 0xff;
                result[i][j][1] = (pixels[i*j] >> 8) & 0xff;
                result[i][j][2] = pixels[i*j] & 0xff;
            }
        }
        return result;
    }

    public static Bitmap alignFace(Bitmap face, PointF leftEye, PointF rightEye, PointF mouth) {
        return face;
//        Log.i(tag, "Face input: " + face.getWidth() + ", " + face.getHeight());
//        MatOfPoint2f src = new MatOfPoint2f();
//        MatOfPoint2f dest = new MatOfPoint2f();
//
//        // our reference points (source)
//        src.fromArray(new Point(leftEye.x, leftEye.y), new Point(rightEye.x, rightEye.y), new Point(mouth.x, mouth.y));
//
//        double dimX = face.getWidth();
//        double dimY = face.getHeight();
//
//        // http://openface-api.readthedocs.io/en/latest/openface.html
//        // Alex: calculated from python script where inner eyes are interpolated from four eye points (also norm min/max)
//        dest.fromArray(new Point(dimX*0.70726717, dimY*0.1557629), new Point(dimX*0.27657071, dimY*0.16412275), new Point(dimX*0.50020397, dimY*0.75058442));
//
//        Mat matrixTransformation = getAffineTransform(src, dest);
//        Mat orgImage = new Mat();
//        Mat alignedImage = new Mat();
//
//        Utils.bitmapToMat(face, orgImage);
//
//        warpAffine(orgImage, alignedImage, matrixTransformation, orgImage.size());
//
//        Bitmap alignedFace = Bitmap.createBitmap(face.getWidth(), face.getHeight(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(alignedImage, alignedFace);
//
//        return alignedFace;
    }

    public static int[] RGBArrayToIntArray(int[][][] RGBArray, int dimension) {
        int[] intArray = new int[dimension*dimension*3];
        for (int i=0; i<dimension; i++) {
            for (int j=0; j<dimension; j++) {
                intArray[i*dimension+3*j] = RGBArray[i][j][0];
                intArray[i*dimension+3*j+1] = RGBArray[i][j][1];
                intArray[i*dimension+3*j+2] = RGBArray[i][j][2];
            }
        }
        return intArray;
    }
}