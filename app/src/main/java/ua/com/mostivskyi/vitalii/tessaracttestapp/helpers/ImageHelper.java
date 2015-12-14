package ua.com.mostivskyi.vitalii.tessaracttestapp.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.IOException;

public final class ImageHelper {

    private static final String RotateImageTag = "Rotate image";

    public static Bitmap fixCameraRotation(Bitmap image, String imagePath) {
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            Log.e(RotateImageTag, "Couldn't correct orientation - " + e.getMessage());
        }

        int exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);

        Log.v(RotateImageTag, "Orient: " + exifOrientation);

        int rotate = 0;

        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
        }

        Log.v(RotateImageTag, "Rotation: " + rotate);

        if (rotate != 0) {
            int w = image.getWidth();
            int h = image.getHeight();

            Matrix mtx = new Matrix();
            mtx.preRotate(rotate);

            return Bitmap.createBitmap(image, 0, 0, w, h, mtx, false);
        }

        return image;
    }

    public static Bitmap ConvertToARGB_8888(Bitmap image) {
        return image.copy(Bitmap.Config.ARGB_8888, true);
    }

    public static Bitmap getImage(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getCapturedImage(String path) {
        Bitmap image = getImage(path);

        return fixCameraRotation(image, path);
    }
}
