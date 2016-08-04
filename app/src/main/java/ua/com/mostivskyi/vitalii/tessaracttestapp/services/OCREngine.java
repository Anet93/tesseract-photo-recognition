package ua.com.mostivskyi.vitalii.tessaracttestapp.services;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.List;

import ua.com.mostivskyi.vitalii.tessaracttestapp.helpers.ImageHelper;
import ua.com.mostivskyi.vitalii.tessaracttestapp.helpers.TextHelper;

/**
 * Created by vitalii.mostivskyi on 13/12/2015.
 */
public class OCREngine {

    private String SingleCharacterTag = "SingleCharacter";

    private TessBaseAPI baseApi;
    private String lang;


    public OCREngine(String trainDataPath, String recognizeLanguage) {

        baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(trainDataPath, recognizeLanguage);
        baseApi.setVariable("tessedit_char_whitelist","qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM");
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR);
        lang = recognizeLanguage;
    }

    public String recognize(Bitmap image) {

        Log.v(SingleCharacterTag, "In recognize");
        image = prepareImageForOCR(image);
        baseApi.setImage(image);

        String recognizedText = singleCharacterRecognition();
        //String recognizedText = baseApi.getUTF8Text();

        return prepareText(recognizedText);
    }

    private String singleCharacterRecognition() {
        Log.v(SingleCharacterTag, "The begin");
        //todo: read about this variable
        baseApi.setVariable("save_blob_choices", "T");
        //to run Recognize method
        baseApi.getUTF8Text();

        ResultIterator resultIterator = baseApi.getResultIterator();
        int level = TessBaseAPI.PageIteratorLevel.RIL_SYMBOL;

        if (resultIterator == null) {
            Log.v(SingleCharacterTag, "Iterator is null");
            return "";
        }

        String symb = resultIterator.getUTF8Text(level);
        //todo: what is confidence? - точність
        float conf = resultIterator.confidence(level);

        if (symb == null) {
            Log.v(SingleCharacterTag, "Symbol is null");
            return "";
        }

        List<Pair<String, Double>> choices = resultIterator.getChoicesAndConfidence(level);
        Log.v(SingleCharacterTag, "choices count: " + choices.size());
        for (Pair<String, Double> pair : choices) {
            String sym = pair.first;
            Double confidence = pair.second;

            Log.v(SingleCharacterTag, sym + ": " + confidence);
        }

        return symb;
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
