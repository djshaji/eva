package org.acoustixaudio.eva;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class UI {
    public static final String TAG = "UI";

    MainActivity mainActivity ;
    ImageView mainWindow = null ;
    Button prev, play, pause, next, stop, eject ;
    ToggleButton shuffle, repeat, toggle_playlist, toggle_eq ;
    SeekBar seekBar, volume, balance ;
    JSONObject skinFormat = null ;


    UI (MainActivity _mainActivity) {
        mainActivity = _mainActivity;
        skinFormat = mainActivity.utils.loadJSONFromAsset("skinformat.json");
    }

    public View createView (JSONObject component) throws JSONException {
        View view = null ;
        Log.i(TAG, "createView: " + component);
        switch (component.getString("type")) {
            case "button":
                view = new Button(mainActivity) ;
                break ;
            case "seekbar":
                view = new SeekBar(mainActivity) ;
                break ;
            case "togglebutton":
                view = new ToggleButton(mainActivity) ;
                break ;
            case "image":
                view = new ImageView(mainActivity);
                break;
            default:
                break ;
        }

        JSONArray coordinates = component.getJSONArray("coordinates");
        if (view != null) {
            int width = coordinates.getInt(2) - coordinates.getInt(0);
            int height = coordinates.getInt(3) - coordinates.getInt(1);
            // Assuming the parent layout is AbsoluteLayout for simplicity, adjust as needed

            width = (int) (width * mainActivity.skin.scale) ;
            height = (int) (height * mainActivity.skin.scale) ;


            Log.d(TAG, String.format ("[view size]: %d x %d", width, height));
            view.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
        }

        if (view != null) {
            Bitmap bitmap = mainActivity.skin.skin.get(component.getString("source")).getBitmap();
            if (component.has("source_rect")) {
                JSONArray source_rect = component.getJSONArray("source_rect");
                int x = source_rect.getInt(0);
                int y = source_rect.getInt(1);
                int width = source_rect.getInt(2) - x;
                int height = source_rect.getInt(3) - y;
                Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, width, height, null, true);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, (int) (croppedBitmap.getWidth() * mainActivity.skin.scale), (int)(croppedBitmap.getHeight() * mainActivity.skin.scale), true);
                view.setBackground(new android.graphics.drawable.BitmapDrawable(mainActivity.getResources(), scaledBitmap));
            } else {
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 2, bitmap.getHeight() * 2, false);
                view.setBackground(new android.graphics.drawable.BitmapDrawable(mainActivity.getResources(), scaledBitmap));
            }
        }

        return view ;
    }

    public void create () throws JSONException {
        mainWindow = (ImageView) createView(skinFormat.getJSONObject("main_window").getJSONObject("background"));
        mainActivity.root.addView(mainWindow);

    }
}
