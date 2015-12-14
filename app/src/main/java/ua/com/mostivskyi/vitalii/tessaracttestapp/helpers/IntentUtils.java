package ua.com.mostivskyi.vitalii.tessaracttestapp.helpers;

import android.content.Intent;

/**
 * Created by vitalii.mostivskyi on 13/12/2015.
 */
public final class IntentUtils {

    public static Intent getGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        return galleryIntent;
    }
}
