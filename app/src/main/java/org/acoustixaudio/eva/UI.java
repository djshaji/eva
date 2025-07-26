package org.acoustixaudio.eva;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import java.util.Random;

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
            case "toggle":
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
            int width = coordinates.getInt(2);// - coordinates.getInt(0);
            int height = coordinates.getInt(3);// - coordinates.getInt(1);
            // Assuming the parent layout is AbsoluteLayout for simplicity, adjust as needed

            width = (int) (width * mainActivity.skin.scale) ;
            height = (int) (height * mainActivity.skin.scale) ;

            int marginLeft = coordinates.getInt(0);
            marginLeft = (int) (marginLeft * mainActivity.skin.scale) ;
            int marginTop = coordinates.getInt(1);
            marginTop = (int) (marginTop * mainActivity.skin.scale) ;

            Log.i(TAG, String.format ("[view size]: %d x %d [%d %d]", width, height, marginLeft, marginTop));
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(width, height);
            params.setMargins(marginLeft, marginTop, 0, 0);
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            view.setLayoutParams(params);
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            Log.d(TAG, String.format("Color: #%06X", (0xFFFFFF & color)));

//            view.setBackgroundColor(color);
            // Example of how to use a predefined color if needed later:
            // view.setBackgroundColor(mainActivity.getResources().getColor(R.color.blanched_almond));

        }

        if (view != null) {
            Bitmap bitmap = mainActivity.skin.skin.get(component.getString("source")).getBitmap();
            if (component.has("source_rect")) {
                JSONArray source_rect = component.getJSONArray("source_rect");
                int x = source_rect.getInt(0);
                int y = source_rect.getInt(1);
                int width = source_rect.getInt(2) ;// - x;
                int height = source_rect.getInt(3) ;//- y;
                Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, width, height, null, true);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, (int) (croppedBitmap.getWidth() * mainActivity.skin.scale), (int)(croppedBitmap.getHeight() * mainActivity.skin.scale), true);
                Log.d(TAG, String.format ("[bitmap size]: %d x %d", scaledBitmap.getWidth(), scaledBitmap.getHeight()));
                view.setBackground(new android.graphics.drawable.BitmapDrawable(mainActivity.getResources(), scaledBitmap));
            }
        }

        return view ;
    }

    public void create () throws JSONException {
        mainWindow = (ImageView) createView(skinFormat.getJSONObject("main_window").getJSONObject("background"));
        mainActivity.root.addView(mainWindow);
        ImageView titleBar = (ImageView) createView(skinFormat.getJSONObject("main_window").getJSONObject("title_bar"));
        mainActivity.root.addView(titleBar);

        Button mainClose = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("close"));
//        mainActivity.root.addView(mainClose);
        ToggleButton eq_button = (ToggleButton) createView(skinFormat.getJSONObject("main_window").getJSONObject("eq_button"));
        mainActivity.root.addView(eq_button);
        ToggleButton pl_button = (ToggleButton) createView(skinFormat.getJSONObject("main_window").getJSONObject("pl_button"));
        mainActivity.root.addView(pl_button);

        Button prev = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("prev"));
        mainActivity.root.addView(prev);
        Button play = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("play"));
        mainActivity.root.addView(play);
        Button pause = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("pause"));
        mainActivity.root.addView(pause);
        Button stop = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("stop"));
        mainActivity.root.addView(stop);
        Button next = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("next"));
        mainActivity.root.addView(next);
        Button eject = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("eject"));
        mainActivity.root.addView(eject);
    }
}
