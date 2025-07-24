package org.acoustixaudio.eva;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
    MainActivity mainActivity ;
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
}
