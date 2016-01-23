package ua.com.mostivskyi.vitalii.tessaracttestapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ua.com.mostivskyi.vitalii.tessaracttestapp.R;

/**
 * Created by ${Anet} on 2016-01-23.
 */
public class CropActivity extends Activity {

    private String croppedImagePath;
    private String TAG = "CropActivity";

    @Bind(R.id.cropButton)
    Button cropButton;

    @Bind(R.id.cropImageView)
    CropImageView cropImageView;

    @OnClick(R.id.cropButton)
    public void cropButtonClick(View view) {
        Log.v(TAG, "Cropping");
        createBitmap();
        closeIntent();
    }

    private void closeIntent() {

        setResult(StartActivity.RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        ButterKnife.bind(this);

        croppedImagePath = getIntent().getStringExtra(StartActivity.CROPED_IMAGE_PATH);

        String path = getIntent().getStringExtra(StartActivity.CROP_INTENT);
        performCrop(path);

    }

    protected void performCrop(String path) {
        cropImageView.setCropMode(CropImageView.CropMode.RATIO_FREE);
        cropImageView.setImageBitmap(BitmapFactory.decodeFile(path));
        cropImageView.setFrameStrokeWeightInDp(1);
        cropImageView.setGuideStrokeWeightInDp(1);
        cropImageView.setHandleSizeInDp(6);
        cropImageView.setTouchPaddingInDp(8);
        cropImageView.setMinFrameSizeInDp(50);
        cropImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void createBitmap() {

        Bitmap bitmap = cropImageView.getCroppedBitmap();

        File file = new File(croppedImagePath);
        try {
            file.createNewFile();
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
