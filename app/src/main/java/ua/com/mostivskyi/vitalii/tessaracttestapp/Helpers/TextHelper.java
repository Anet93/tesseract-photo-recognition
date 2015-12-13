package ua.com.mostivskyi.vitalii.tessaracttestapp.helpers;

/**
 * Created by vitalii.mostivskyi on 13/12/2015.
 */
public final class TextHelper {

    public static String removeJunkCharacters(String input, String language) {

        switch (language)
        {
            case "eng":
                input = input.replaceAll("[^a-zA-Z0-9]+", " ");
                break;
        }

        return input;
    }
}
