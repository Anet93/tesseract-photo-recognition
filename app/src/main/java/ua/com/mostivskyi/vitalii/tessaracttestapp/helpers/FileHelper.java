package ua.com.mostivskyi.vitalii.tessaracttestapp.helpers;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ua.com.mostivskyi.vitalii.tessaracttestapp.R;

public final class FileHelper {
    private static final String UpdateAssetsTag = "Update assets";
    private static final String CreateDirectoriesTag = "Create directories";

    private static final String TrainDataExtension = ".traineddata";

    public static void UpdateAssets(Context context) {
        String assetsPath = Environment.getExternalStorageDirectory().toString() + context.getResources().getString(R.string.AssetsFilePath);
        String trainDataPath = context.getResources().getString(R.string.TrainDataPath);
        String lang = context.getResources().getString(R.string.Language);

        //create directories for train data (if not exists)
        String[] paths = new String[]{assetsPath, assetsPath + trainDataPath};
        createDirectories(paths);

        String trainDataFileName = lang + TrainDataExtension;
        String trainDataFilePath = assetsPath + trainDataPath + trainDataFileName;

        //copy drain data file from assets to sd card ((if not exists))
        if (!(new File(trainDataFilePath)).exists()) {
            try {
                InputStream in = context.getAssets().open(trainDataPath + trainDataFileName);
                OutputStream out = new FileOutputStream(trainDataFilePath);
                copyFile(in, out);

                Log.v(UpdateAssetsTag, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(UpdateAssetsTag, "Was unable to copy " + lang + " traineddata - " + e.toString());
            }
        }
    }

    private static void createDirectories(String[] paths) {
        for (String path : paths) {
            File dir = new File(path);

            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(CreateDirectoriesTag, "ERROR: Creation of directory " + path + " on sdcard failed");

                    return;
                } else {
                    Log.v(CreateDirectoriesTag, "Created directory " + path + " on sdcard");
                }
            }
        }
    }

    private static void copyFile(InputStream inps, OutputStream outps) throws IOException {
        byte[] buffer = new byte[1024];
        int length;

        while ((length = inps.read(buffer)) > 0) {
            outps.write(buffer, 0, length);
        }

        inps.close();
        outps.close();
    }
}
