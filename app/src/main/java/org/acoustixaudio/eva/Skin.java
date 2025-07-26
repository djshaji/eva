package org.acoustixaudio.eva;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

import java.util.HashMap;

public class Skin {
    private static final String TAG = "EVA";
    MainActivity mainActivity ;
    AssetManager assets ;
    float scale = 1.0f;
    DisplayMetrics metrics ;
    HashMap <String, BitmapDrawable> skin = new HashMap<>();
    HashMap <String, HashMap<Integer, Bitmap>> states ;
    String [] filenames = {
            "main_window.jpg",
        "balance.bmp",
        "eqmain.bmp",
        "main.bmp",
        "numbers.bmp",
        "posbar.bmp",
        "titlebar.bmp",
        "cbuttons.bmp",
        "gen.bmp",
        "mb.bmp",
        "playpaus.bmp",
        "shufrep.bmp",
        "video.bmp",
        "eq_ex.bmp",
        "genex.bmp",
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
        skin.clear();
        for (String filename : filenames) {
            try {
                InputStream stream = assets.open("classic/" + filename);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                BitmapDrawable drawable = new BitmapDrawable(bitmap);
                skin.put(filename, drawable);
            } catch (Exception e) {
                // TODO: use resources instead
                Log.e("EVA", "Cannot load " + filename + " from assets");
            }
        }
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

}
