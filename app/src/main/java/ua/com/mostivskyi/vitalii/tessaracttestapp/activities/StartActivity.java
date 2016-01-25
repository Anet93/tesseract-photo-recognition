package ua.com.mostivskyi.vitalii.tessaracttestapp.activities;

import java.io.File;

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
import android.widget.Toolbar;

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
        startCameraActivity();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkPermission(){
        String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA","android.permission.READ_EXTERNAL_STORAGE" };
        int permsRequestCode = 200;
        requestPermissions(perms, permsRequestCode);

    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case 200:
                boolean WRITE_EXTERNAL_STORAGE = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                boolean cameraAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                boolean READ_EXTERNAL_STORAGE = grantResults[2]==PackageManager.PERMISSION_GRANTED;
                break;
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

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            checkPermission();
        }

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
                        String url = FileHelper.getRealPathFromURI(this, imageUri);
                        startCropActivity(url);
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
}
