package ua.com.mostivskyi.vitalii.tessaracttestapp.activities;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.googlecode.tesseract.android.TessBaseAPI;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ua.com.mostivskyi.vitalii.tessaracttestapp.helpers.FileHelper;
import ua.com.mostivskyi.vitalii.tessaracttestapp.R;

public class StartActivity extends Activity {

    private static final String TAG = "TesseractTestApp";
    private static final String PhotoTakenInstanceStateName = "photo_taken";

    private String assetsPath;
    private String capturedImagePath;
    private String lang;
    private boolean isPhotoTaken;

    @Bind(R.id.recognizedTextField) EditText recognizedTextField;
    @Bind(R.id.takePhotoButton) Button takePhotoButton;

    @OnClick(R.id.takePhotoButton)
    public void takePhotoButtonClick(View view) {
        Log.v(TAG, "Starting Camera app");
        startCameraActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Initialize();
        FileHelper.UpdateAssets(this);
    }

    private void Initialize() {

        ButterKnife.bind(this);
        SetUpGlobals();
    }

    private void SetUpGlobals() {
        assetsPath = Environment.getExternalStorageDirectory().toString() + getResources().getString(R.string.AssetsFilePath);
        capturedImagePath = assetsPath + getResources().getString(R.string.CapturedImageName);;
        lang = getResources().getString(R.string.Language);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "onActivity resultCode: " + resultCode);

        if (resultCode == -1)
        {
            onPhotoTaken();
        }
        else
        {
            Log.v(TAG, "User cancelled");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(StartActivity.PhotoTakenInstanceStateName, isPhotoTaken);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState()");

        if (savedInstanceState.getBoolean(StartActivity.PhotoTakenInstanceStateName)) {
            onPhotoTaken();
        }
    }

    // Simple android photo capture:
    // http://labs.makemachine.net/2010/03/simple-android-photo-capture/
    protected void startCameraActivity() {
        File file = new File(capturedImagePath);
        Uri outputFileUri = Uri.fromFile(file);

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(intent, 0);
    }

    protected void onPhotoTaken() {
        isPhotoTaken = true;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile(capturedImagePath, options);

        try
        {
            ExifInterface exif = new ExifInterface(capturedImagePath);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.v(TAG, "Orient: " + exifOrientation);

            int rotate = 0;

            switch (exifOrientation)
            {
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

            Log.v(TAG, "Rotation: " + rotate);

            if (rotate != 0) {

                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }

            // Convert to ARGB_8888, required by tess
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        } catch (IOException e) {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }

        // _image.setImageBitmap( bitmap );

        Log.v(TAG, "Before baseApi");

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(assetsPath, lang);
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text();

        baseApi.end();

        // You now have the text in recognizedText var, you can do anything with it.
        // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
        // so that garbage doesn't make it to the display.

        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        if (lang.equalsIgnoreCase("eng")) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        recognizedText = recognizedText.trim();

        if (recognizedText.length() != 0) {
            recognizedTextField.setText(recognizedTextField.getText().toString().length() == 0 ? recognizedText : recognizedTextField.getText() + " " + recognizedText);
            recognizedTextField.setSelection(recognizedTextField.getText().toString().length());
        }
    }
}
