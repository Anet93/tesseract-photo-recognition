package ua.com.mostivskyi.vitalii.tessaracttestapp.activities;

import java.io.File;
import java.io.FileOutputStream;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ua.com.mostivskyi.vitalii.tessaracttestapp.helpers.FileHelper;
import ua.com.mostivskyi.vitalii.tessaracttestapp.R;
import ua.com.mostivskyi.vitalii.tessaracttestapp.helpers.ImageHelper;
import ua.com.mostivskyi.vitalii.tessaracttestapp.helpers.IntentUtils;
import ua.com.mostivskyi.vitalii.tessaracttestapp.services.OCREngine;

public class StartActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final int CAMERA_REQUEST_CODE = 999;
    private static final int GALLERY_REQUEST_CODE = 1337;
    private static final int CAMERA_PERMISSION_CODE = 285;
    private static final int CROP_REQUEST_CODE = 2935;

    private static final String TAG = "TesseractTestApp";
    private static final String PhotoTakenInstanceStateName = "photo_taken";
    public static final String CROP_INTENT = "crop";
    public static final String CROPED_IMAGE_PATH = "cropped_path";

    private OCREngine OCREngine;

    private String assetsPath;
    private String capturedImagePath;
    private String capturedCropImagePath;
    private String lang;
    private boolean isPhotoTaken;
    Bitmap bitmap;

    @Bind(R.id.recognizedTextField)
    EditText recognizedTextField;

    @Bind(R.id.croppedImageView)
    ImageView croppedImageView;

    @Bind(R.id.fabCamera)
    FloatingActionButton fabCamera;

    @Bind(R.id.fabGallary)
    FloatingActionButton fabGallary;

    @OnClick(R.id.fabCamera)
    public void takePhotoButtonClick(View view) {
        Log.v(TAG, "Starting Camera app");

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            checkPermission();
        }
        startCameraActivity();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkPermission(){
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraActivity();            }
            else {
                Toast.makeText(this, getResources().getString(R.string.canNotOpenCameraMessage),Toast.LENGTH_LONG);
            }
        }
    }

    @OnClick(R.id.fabGallary)
    public void recognizeFromFileButtonClick(View view) {

        Log.v(TAG, "Starting galary intent");
        startActivityForResult(IntentUtils.getGalleryIntent(), GALLERY_REQUEST_CODE);
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
        assetsPath = getExternalFilesDir(null).getPath() + getResources().getString(R.string.AssetsFilePath);
        capturedImagePath = assetsPath + getResources().getString(R.string.CapturedImageName);
        capturedCropImagePath = assetsPath + getResources().getString(R.string.CapturedCropImageName);

        lang = getResources().getString(R.string.Language);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "Received an  resultCode: " + requestCode + " " + resultCode + " " + data);

        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    isPhotoTaken = true;
                    startCropActivity(capturedImagePath);
                } else {
                    Log.v(TAG, "User cancelled");
                }
                break;

            case GALLERY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Uri imageUri = data.getData();
                        Log.v(TAG, imageUri.getPath());
                        startCropActivity(String.valueOf(imageUri));
                    }
                }
                break;

            case CROP_REQUEST_CODE:
                if(resultCode == RESULT_OK) {
                        onPhotoRecognized();
                }
        }
    }

    public void startCropActivity(String uri){
        Intent crop = new Intent(this, CropActivity.class);

        crop.putExtra(CROPED_IMAGE_PATH, capturedCropImagePath);
        crop.putExtra(CROP_INTENT, uri);
        startActivityForResult(crop, CROP_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(StartActivity.PhotoTakenInstanceStateName, isPhotoTaken);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState()");

        if (savedInstanceState.getBoolean(StartActivity.PhotoTakenInstanceStateName)) {

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

        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    protected void onPhotoRecognized() {

        Bitmap bitmap = ImageHelper.getCapturedImage(capturedCropImagePath);

        croppedImageView.setImageBitmap(bitmap);

        Log.v(TAG, "Before OCREngine");
        String recognizedText = OCREngine.recognize(bitmap);
        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        if (recognizedText.length() > 0) {
            recognizedTextField.setText(recognizedText);
            recognizedTextField.setSelection(recognizedTextField.getText().toString().length());
        }
    }

    private void onPhotoSelected(Uri imageUri) {
        Bitmap image = ImageHelper.getImage(this, imageUri);

        if (image == null) {
            Toast.makeText(this, getString(R.string.canNotOpenImageMessage), Toast.LENGTH_SHORT).show();
        } else {
            recognizedTextField.setText(OCREngine.recognize(image));
        }
    }
}
