package org.acoustixaudio.eva;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;

import java.util.HashMap;

public class Skin {
    private static final String TAG = "EVA";
    MainActivity mainActivity ;
    AssetManager assets ;
    String defaultSkinDir = "classic/";
    float scale = 1.0f;
    DisplayMetrics metrics ;
    HashMap <String, JSONObject> inis = new HashMap<>();
    HashMap <String, BitmapDrawable> skin = new HashMap<>();
    HashMap <String, HashMap<Integer, Bitmap>> states ;
    String [] ini_filenames = {
//            "region.txt",
//            "viscolor.txt",
            "pledit.txt"
    };

    String [] filenames = {
        "balance.bmp",
        "eqmain.bmp",
        "main.bmp",
        "numbers.bmp",
        "posbar.bmp",
        "titlebar.bmp",
        "cbuttons.bmp",
//        "gen.bmp",
//        "mb.bmp",
        "playpaus.bmp",
        "shufrep.bmp",
//        "video.bmp",
//        "eq_ex.bmp",
//        "genex.bmp",
        "monoster.bmp",
        "pledit.bmp",
        "text.bmp",
        "volume.bmp"
    };

    Skin (MainActivity _mainActivity) {
        mainActivity = _mainActivity;
        assets = mainActivity.getAssets();
        metrics = mainActivity.getResources().getDisplayMetrics();
        scale = metrics.widthPixels / 275.0f;
        Log.d("EVA", "Scale: " + scale + ' ' + metrics);
    }

    public void reset () {
        Log.d(TAG, "reset() called");
        skin.clear();
        for (String _filename : ini_filenames) {
            try {
                InputStream stream = assets.open(defaultSkinDir + _filename);
                inis.put(_filename, Utils.convertIniToJson(stream));
                stream.close();
            } catch (Exception e) {
                Log.e(TAG, "reset: ", e);
            }
        }

        for (String filename : filenames) {
            try {
                InputStream stream = assets.open(defaultSkinDir + filename);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                BitmapDrawable drawable = new BitmapDrawable(bitmap);
                skin.put(filename, drawable);
            } catch (Exception e) {
                // TODO: use resources instead
                Log.e("EVA", "Cannot load " + filename + " from assets");
            }
        }
    }

    public void load (String dir) {
        Log.d(TAG, "load() called with: dir = [" + dir + "]");
        skin.clear();
        for (String _filename : ini_filenames) {
            Log.d(TAG, String.format ("ini: %s", dir + "/" + _filename));
            try {
                InputStream stream = new FileInputStream(dir + "/" + _filename);
                inis.put(_filename, Utils.convertIniToJson(stream));
                stream.close();
            } catch (Exception e) {
                Log.e(TAG, "load: ", e);
                load();
                return;
            }
        }

        for (String filename : filenames) {
            Log.d(TAG, String.format ("bitmap: %s", dir + "/" + filename));
            try {
                InputStream stream = new FileInputStream(dir + "/" + filename);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                BitmapDrawable drawable = new BitmapDrawable(bitmap);
                skin.put(filename, drawable);
                stream.close();
            } catch (Exception e) {
                // TODO: use resources instead
                Log.e("EVA", "Cannot load " + filename + " from assets");
                load();
                return;
            }
        }

        states = new HashMap<>();
    }

    BitmapDrawable extract (Bitmap source, int width, int height, int x, int y, float scale) {
        Bitmap bitmap = Bitmap.createBitmap(source, x, y, width, height);
        if (scale != 1.0f) {
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (width * scale), (int) (height * scale), false);
        }
        return new BitmapDrawable(bitmap);
    }

    void main () {
        // load skin here
        reset();
    }

    public void load () {
        Log.d(TAG, "load() called");
        states = new HashMap<>();
        main();
    }

    public String getFromIni (String filename, String heading, String key) {
        String value = null;
        if (inis.containsKey(filename)) {
            JSONObject jsonObject = inis.get(filename);
            try {
                assert jsonObject != null;
                value = jsonObject.getJSONObject(heading).getString(key);
            } catch (Exception e) {
                Log.e(TAG, "getFromIni: ", e);
            }
        }
        return value;
    }
}
