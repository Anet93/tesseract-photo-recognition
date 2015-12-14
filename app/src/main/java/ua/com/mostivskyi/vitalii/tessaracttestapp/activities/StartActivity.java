package ua.com.mostivskyi.vitalii.tessaracttestapp.activities;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.isseiaoki.simplecropview.CropImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ua.com.mostivskyi.vitalii.tessaracttestapp.helpers.FileHelper;
import ua.com.mostivskyi.vitalii.tessaracttestapp.R;
import ua.com.mostivskyi.vitalii.tessaracttestapp.helpers.ImageHelper;
import ua.com.mostivskyi.vitalii.tessaracttestapp.services.OCREngine;

public class StartActivity extends Activity {

    private static final String TAG = "TesseractTestApp";
    private static final String PhotoTakenInstanceStateName = "photo_taken";

    private OCREngine OCREngine;

    private String assetsPath;
    private String capturedImagePath;
    private String capturedCropImagePath;
    private String lang;
    private boolean isPhotoTaken;
    Bitmap bitmap;

    @Bind(R.id.recognizedTextField)
    EditText recognizedTextField;
    @Bind(R.id.takePhotoButton)
    Button takePhotoButton;
    @Bind(R.id.cropButton)
    Button cropButton;
    @Bind(R.id.cropImageView)
    CropImageView cropImageView;
    @Bind(R.id.croppedImageView)
    ImageView croppedImageView;

    @OnClick(R.id.takePhotoButton)
    public void takePhotoButtonClick(View view) {
        Log.v(TAG, "Starting Camera app");
        startCameraActivity();
    }

    @OnClick(R.id.cropButton)
    public void cropButtonClick(View view) {
        Log.v(TAG, "Cropping");
        croppedImageView.setImageBitmap(cropImageView.getCroppedBitmap());
        createCroppedBitmap();
        onPhotoTaken();
    }

    private void createCroppedBitmap() {

        croppedImageView.setDrawingCacheEnabled(true);
        bitmap = cropImageView.getCroppedBitmap();

        File file = new File(capturedCropImagePath);
        try {
            file.createNewFile();
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        initialize();
    }

    private void initialize() {

        setUpGlobals();
        FileHelper.UpdateAssets(this);
        initializeOCR();
    }

    private void initializeOCR() {

        OCREngine = new OCREngine(assetsPath, lang);
        OCREngine.setDebug(true);
    }

    private void setUpGlobals() {
        assetsPath = Environment.getExternalStorageDirectory().toString() + getResources().getString(R.string.AssetsFilePath);
        capturedImagePath = assetsPath + getResources().getString(R.string.CapturedImageName);
        capturedCropImagePath = assetsPath + getResources().getString(R.string.CapturedCropImageName);

        lang = getResources().getString(R.string.Language);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "onActivity resultCode: " + resultCode);

        if (resultCode == -1) {
            performCrop();
        } else {
            Log.v(TAG, "User cancelled");
        }
    }

    protected void performCrop() {
        cropImageView.setCropMode(CropImageView.CropMode.RATIO_FREE);
        cropImageView.setImageBitmap(BitmapFactory.decodeFile(capturedImagePath));
        cropImageView.setFrameStrokeWeightInDp(1);
        cropImageView.setGuideStrokeWeightInDp(1);
        cropImageView.setHandleSizeInDp(6);
        cropImageView.setTouchPaddingInDp(8);
        cropImageView.setMinFrameSizeInDp(50);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OCREngine.close();
    }

    protected void startCameraActivity() {
        File file = new File(capturedImagePath);
        Uri outputFileUri = Uri.fromFile(file);

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(intent, 0);
    }

    protected void onPhotoTaken() {
        isPhotoTaken = true;

        Bitmap bitmap = ImageHelper.getCapturedImage(capturedCropImagePath);

        Log.v(TAG, "Before OCREngine");
        String recognizedText = OCREngine.recognize(bitmap);
        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        if (recognizedText.length() > 0) {
            recognizedTextField.setText(recognizedText);
            recognizedTextField.setSelection(recognizedTextField.getText().toString().length());
        }
    }
}
