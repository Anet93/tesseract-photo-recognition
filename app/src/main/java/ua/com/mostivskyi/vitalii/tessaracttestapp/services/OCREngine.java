package ua.com.mostivskyi.vitalii.tessaracttestapp.services;

import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import ua.com.mostivskyi.vitalii.tessaracttestapp.helpers.ImageHelper;
import ua.com.mostivskyi.vitalii.tessaracttestapp.helpers.TextHelper;

/**
 * Created by vitalii.mostivskyi on 13/12/2015.
 */
public class OCREngine {

    private TessBaseAPI baseApi;
    private String lang;


    public OCREngine(String trainDataPath, String recognizeLanguage) {

        baseApi = new TessBaseAPI();
        baseApi.init(trainDataPath, recognizeLanguage);
        lang = recognizeLanguage;
    }

    public String recognize(Bitmap image) {

        image = prepareImageForOCR(image);
        baseApi.setImage(image);

        String recognizedText = baseApi.getUTF8Text();

        return prepareText(recognizedText);
    }

    public void setDebug(Boolean enabled) {
        baseApi.setDebug(enabled);
    }

    public void close() {
        baseApi.end();
    }

    private Bitmap prepareImageForOCR(Bitmap image) {

        image = ImageHelper.ConvertToARGB_8888(image);

        return image;
    }

    private String prepareText(String recognizedText) {

        recognizedText = TextHelper.removeJunkCharacters(recognizedText, lang);
        recognizedText = recognizedText.trim();

        return recognizedText;
    }
}
