package ua.com.mostivskyi.vitalii.tessaracttestapp.Helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ua.com.mostivskyi.vitalii.tessaracttestapp.R;

public final class FileHelper {

    public static void UpdateAssets(Context context) {
        String TAG = "Update assets";
        String assetsPath = Environment.getExternalStorageDirectory().toString() + context.getResources().getString(R.string.AssetsFilePath);
        String trainDataPath = context.getResources().getString(R.string.TrainDataPath);
        String capturedImagePath = assetsPath + context.getResources().getString(R.string.CapturedImageName);
        String lang = context.getResources().getString(R.string.Language);

        String[] paths = new String[]{assetsPath, assetsPath + trainDataPath};

        for (String path : paths)
        {
            File dir = new File(path);

            if (!dir.exists())
            {
                if (!dir.mkdirs())
                {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");

                    return;
                }
                else
                {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }
        }

        // lang.traineddata file with the app (in assets folder)
        // This area needs work and optimization
        if (!(new File(assetsPath + "tessdata/" + lang + ".traineddata")).exists()) {
            try {
                AssetManager assetManager = context.getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(assetsPath
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }
    }
}
