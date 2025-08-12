package org.acoustixaudio.eva;

import android.content.res.Resources;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    MainActivity mainActivity ;
    public static String TAG = "Utils" ;
    Utils (MainActivity _mainActivity) {
        mainActivity = _mainActivity;
    }

    public JSONObject loadJSONFromAsset (String filename) {
        String json = null;
        JSONObject obj = null;
        try {
            InputStream is = mainActivity.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            obj = new JSONObject(json);
            // Now you can work with the JSONObject
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static JSONObject convertIniToJson(InputStream inputStream) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONObject currentSection = null;
        String currentSectionName = null;

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        Pattern sectionPattern = Pattern.compile("^\\[(.*?)\\]$");
        Pattern keyValuePattern = Pattern.compile("^(.*?)=(.*)$");

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) {
                // Skip empty lines and comments
                continue;
            }

            Matcher sectionMatcher = sectionPattern.matcher(line);
            if (sectionMatcher.matches()) {
                currentSectionName = sectionMatcher.group(1);
                currentSection = new JSONObject();
                jsonObject.put(currentSectionName, currentSection);
                Log.d(TAG, String.format ("%s: %s", currentSectionName, currentSection));
            } else {
                Matcher keyValueMatcher = keyValuePattern.matcher(line);
                if (keyValueMatcher.matches() && currentSection != null) {
                    String key = keyValueMatcher.group(1).trim();
                    String value = keyValueMatcher.group(2).trim();
                    currentSection.put(key, value);
                    Log.d(TAG, String.format ("%s: %s", key, value));
                }
            }
        }
        reader.close();
        return jsonObject;
    }

}
