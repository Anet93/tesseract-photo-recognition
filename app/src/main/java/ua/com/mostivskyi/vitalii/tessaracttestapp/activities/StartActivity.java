package ua.com.mostivskyi.vitalii.tessaracttestapp.activities;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
    private String lang;
    private boolean isPhotoTaken;

    @Bind(R.id.recognizedTextField) EditText recognizedTextField;
    @Bind(R.id.takePhotoButton) Button takePhotoButton;

    @OnClick(R.id.takePhotoButton)
    public void takePhotoButtonClick(View view) {
        Log.v(TAG, "Starting Camera app");
        startCameraActivity();
    }

    @OnClick(R.id.recognizeFromFileButton)
    public void recognizeFromFileButtonClick(View view) {

        String imagePath = assetsPath + "myName.jpg";
        Bitmap image = ImageHelper.getImage(imagePath);

        recognizedTextField.setText(OCREngine.recognize(image));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        Initialize();
    }

    private void Initialize() {

        SetUpGlobals();
        FileHelper.UpdateAssets(this);
        InitializeOCR();
    }

    private void InitializeOCR() {

        OCREngine = new OCREngine(assetsPath, lang);
        OCREngine.setDebug(true);
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

        Bitmap bitmap = ImageHelper.getCapturedImage(capturedImagePath);

        Log.v(TAG, "Before OCREngine");
        String recognizedText = OCREngine.recognize(bitmap);
        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        if (recognizedText.length() > 0) {
            recognizedTextField.setText(recognizedText);
            recognizedTextField.setSelection(recognizedTextField.getText().toString().length());
        }
    }
}
