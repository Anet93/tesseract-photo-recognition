package ua.com.mostivskyi.vitalii.tessaracttestapp.helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
//        String assetsPath = Environment.getExternalStorageDirectory().toString() + context.getResources().getString(R.string.AssetsFilePath);

        String assetsPath = context.getExternalFilesDir(null).toString() + context.getResources().getString(R.string.AssetsFilePath);

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
                AssetManager mngr = context.getAssets();
//                InputStream in = mngr.open(trainDataPath + trainDataFileName);
                String path = trainDataPath + trainDataFileName;
                InputStream in = mngr.open(path);
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

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
