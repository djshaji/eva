package org.acoustixaudio.eva;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class UI {
    public static final String TAG = "UI";

    MainActivity mainActivity ;
    ImageView mainWindow = null, equalizer ;
    ToggleButton toggle_eq ;
    JSONObject skinFormat = null ;

    RecyclerView recyclerView ;
    public PlaylistAdapter playlistAdapter ;
    private List<Song> songsList = new ArrayList<>();
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    ArrayList <SeekBar> eq_slider_list = new ArrayList<>();
    ArrayList <ImageView> pl_title_list = new ArrayList<>();
    ArrayList <ImageView> pl_border_left_list = new ArrayList<>();
    ArrayList <ImageView> pl_border_right_list = new ArrayList<>();
    private ImageView titleBar;
    private Button mainClose;
    private ToggleButton eq_button;
    private ToggleButton pl_button;
    private Button prev;
    private Button play;
    private Button pause;
    private Button stop;
    private Button next;
    private Button eject;
    private ImageView mono;
    private ImageView stereo;
    private ToggleButton shuffle;
    private ToggleButton repeat;
    private SeekBar volume;
    private SeekBar balance;
    private SeekBar posbar;
    private ImageView titleBar_eq;
    private ToggleButton auto_eq;
    private Button presets;
    private ImageView pl_left;
    private ImageView pl_right;
    private ImageView pl_title;
    private ImageView pl_bleft;
    private ImageView pl_bright;
    public Button logoBtn;

    public Button plAdd, plRemove, plSelect, plLoad, plMisc;
    private String defaultPlaylist;

    public void skin () throws JSONException {
        Log.d(TAG, "skin() called");
        paintView(mainWindow, skinFormat.getJSONObject("main_window").getJSONObject("background"), false);
        paintView(titleBar, skinFormat.getJSONObject("main_window").getJSONObject("title_bar"), false);
        paintView(mainClose, skinFormat.getJSONObject("main_window").getJSONObject("close"), false);
        paintView(eq_button, skinFormat.getJSONObject("main_window").getJSONObject("eq_button"), false);
        paintView(pl_button, skinFormat.getJSONObject("main_window").getJSONObject("pl_button"), false);
        paintView(prev, skinFormat.getJSONObject("main_window").getJSONObject("prev"), false);
        paintView(play, skinFormat.getJSONObject("main_window").getJSONObject("play"), false);
        paintView(pause, skinFormat.getJSONObject("main_window").getJSONObject("pause"), false);
        paintView(stop, skinFormat.getJSONObject("main_window").getJSONObject("stop"), false);
        paintView(next, skinFormat.getJSONObject("main_window").getJSONObject("next"), false);
        paintView(eject, skinFormat.getJSONObject("main_window").getJSONObject("eject"), false);
        paintView(mono, skinFormat.getJSONObject("main_window").getJSONObject("mono"), false);
        paintView(stereo, skinFormat.getJSONObject("main_window").getJSONObject("ster"), false);
        paintView(shuffle, skinFormat.getJSONObject("main_window").getJSONObject("shuffle"), false);
        paintView(repeat, skinFormat.getJSONObject("main_window").getJSONObject("repeat"), false);
        paintView(volume, skinFormat.getJSONObject("main_window").getJSONObject("volume"), false);
        paintView(balance, skinFormat.getJSONObject("main_window").getJSONObject("balance"), false);
        paintView(posbar, skinFormat.getJSONObject("main_window").getJSONObject("posbar"), false);
        paintView(equalizer, skinFormat.getJSONObject("equalizer_window").getJSONObject("background"), false);
        paintView(titleBar_eq, skinFormat.getJSONObject("equalizer_window").getJSONObject("title_bar"), false);
        paintView(toggle_eq, skinFormat.getJSONObject("equalizer_window").getJSONObject("on_off_button"), false);
        paintView(auto_eq, skinFormat.getJSONObject("equalizer_window").getJSONObject("auto_button"), false);
        paintView(presets, skinFormat.getJSONObject("equalizer_window").getJSONObject("presets_button"), false);

        for (int i = 0; i < eq_slider_list.size(); i++) {
            paintView(eq_slider_list.get(i), skinFormat.getJSONObject("equalizer_window").getJSONArray("sliders").getJSONObject(i), true);
        }

        int statusBar = (int) ((int) Utils.getStatusBarHeight(mainActivity.getResources()) / mainActivity.skin.scale) + 20; // for some reason bottom of playlist is cut off
        JSONObject bleft = skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("bleft");

        int height = (int) (mainActivity.skin.metrics.heightPixels / mainActivity.skin.scale) - statusBar - 38;
        int right = (int) (mainActivity.skin.metrics.widthPixels / mainActivity.skin.scale) - 25;
        int counter = 0 ;
        for (int i = 232 ; i < height ; i++) {
            JSONObject sl = skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("sleft");
            sl.put("coordinates", new JSONArray(new int[]{0, i, 25, 29}));
            paintView(pl_border_left_list.get(counter), sl, false);
            JSONObject sl2 = skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("sright");
            sl2.put("coordinates", new JSONArray(new int[]{right, i, 25, 29}));
            paintView(pl_border_right_list.get(counter), sl2, false);
            counter ++ ;
        }

        for (int i = 0; i < pl_title_list.size(); i++) {
            paintView(pl_title_list.get(i), skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("repeat"), false);
        }


        paintView(pl_left, skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("left"), false);
        paintView(pl_right, skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("right"), false);
        paintView(pl_title, skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("title"), false);
        paintView(pl_bleft, skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("bleft"), false);
        paintView(pl_bright, skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("bright"), false);

        String plColor = ((JSONObject)(mainActivity.skin.inis.get("pledit.txt"))).getJSONObject ("Text").get ("NormalBG").toString();
        int plColorInt = Color.parseColor(plColor);
        recyclerView.setBackgroundColor(plColorInt);
        Log.i(TAG, "plColor: " + plColor);
    }

    public void create () throws JSONException {
        defaultPlaylist = mainActivity.getFilesDir().getAbsolutePath() + "/playlist.m3u";
        mainWindow = (ImageView) createView(skinFormat.getJSONObject("main_window").getJSONObject("background"));
        mainActivity.root.addView(mainWindow);

        logoBtn = new Button(mainActivity);
        logoBtn.setBackgroundColor(mainActivity.getResources().getColor(android.R.color.transparent));
        int logoSize = (int) (20 * mainActivity.skin.scale);
        ConstraintLayout.LayoutParams lparams = new ConstraintLayout.LayoutParams(logoSize, logoSize);
        lparams.setMargins((int) (245 * mainActivity.skin.scale), (int) (86 * mainActivity.skin.scale),  0, 0);
        lparams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        lparams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        mainActivity.root.addView(logoBtn, lparams);

        titleBar = (ImageView) createView(skinFormat.getJSONObject("main_window").getJSONObject("title_bar"));
        mainActivity.root.addView(titleBar);

        mainClose = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("close"));
//        mainActivity.root.addView(mainClose);
        eq_button = (ToggleButton) createView(skinFormat.getJSONObject("main_window").getJSONObject("eq_button"));
        mainActivity.root.addView(eq_button);
        pl_button = (ToggleButton) createView(skinFormat.getJSONObject("main_window").getJSONObject("pl_button"));
        mainActivity.root.addView(pl_button);

        prev = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("prev"));
        mainActivity.root.addView(prev);
        play = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("play"));
        mainActivity.root.addView(play);
        pause = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("pause"));
        mainActivity.root.addView(pause);
        stop = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("stop"));
        mainActivity.root.addView(stop);
        next = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("next"));
        mainActivity.root.addView(next);
        eject = (Button) createView(skinFormat.getJSONObject("main_window").getJSONObject("eject"));
        mainActivity.root.addView(eject);

        mono = (ImageView) createView(skinFormat.getJSONObject("main_window").getJSONObject("mono"));
        mainActivity.root.addView(mono);

        stereo = (ImageView) createView(skinFormat.getJSONObject("main_window").getJSONObject("ster"));
        mainActivity.root.addView(stereo);

        shuffle = (ToggleButton) createView(skinFormat.getJSONObject("main_window").getJSONObject("shuffle"));
        mainActivity.root.addView(shuffle);
        repeat = (ToggleButton) createView(skinFormat.getJSONObject("main_window").getJSONObject("repeat"));
        mainActivity.root.addView(repeat);

        volume = (SeekBar) createView(skinFormat.getJSONObject("main_window").getJSONObject("volume"));
        mainActivity.root.addView(volume);
        balance = (SeekBar) createView(skinFormat.getJSONObject("main_window").getJSONObject("balance"));
        mainActivity.root.addView(balance);
        posbar = (SeekBar) createView(skinFormat.getJSONObject("main_window").getJSONObject("posbar"));
        mainActivity.root.addView(posbar);

        equalizer = (ImageView) createView(skinFormat.getJSONObject("equalizer_window").getJSONObject("background"));
        mainActivity.root.addView(equalizer);

        titleBar_eq = (ImageView) createView(skinFormat.getJSONObject("equalizer_window").getJSONObject("title_bar"));
        mainActivity.root.addView(titleBar_eq);

        JSONArray eq_sliders = skinFormat.getJSONObject("equalizer_window").getJSONArray("sliders");
        for (int i = 0; i < eq_sliders.length(); i++) {
            if (i == 11)
                break ;
            JSONObject slider = eq_sliders.getJSONObject(i);
//            Log.d(TAG, String.valueOf(i) + " create: " + slider);
            View seekBar = createView(slider);
            eq_slider_list.add((SeekBar) seekBar);
            ((SeekBar) seekBar).setMax(100);
            ((SeekBar) seekBar).setMin(-100);
            mainActivity.root.addView(seekBar);
        }

        toggle_eq = (ToggleButton) createView(skinFormat.getJSONObject("equalizer_window").getJSONObject("on_off_button"));
        mainActivity.root.addView(toggle_eq);
        auto_eq = (ToggleButton) createView(skinFormat.getJSONObject("equalizer_window").getJSONObject("auto_button"));
        mainActivity.root.addView(auto_eq);
        presets = (Button) createView(skinFormat.getJSONObject("equalizer_window").getJSONObject("presets_button"));
        mainActivity.root.addView(presets);

        int width = 0 ;
        int counter = 1 ;
        while (width < 275) {
//            Log.i(TAG, "create: adding playlist title bar " + counter ++);
            JSONObject j = skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("repeat");
            j.put("coordinates", new JSONArray(new int[]{width, 232, 25, 20}));
            ImageView pl_item = (ImageView) createView(j);
            pl_title_list.add(pl_item);
            mainActivity.root.addView(pl_item);
            width += 23 ;
        }

        int statusBar = (int) ((int) Utils.getStatusBarHeight(mainActivity.getResources()) / mainActivity.skin.scale) + 20; // for some reason bottom of playlist is cut off
        JSONObject bleft = skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("bleft");

        recyclerView = new RecyclerView(mainActivity);
        int rw = (int) (250 * mainActivity.skin.scale);
        int rh = (int) (mainActivity.skin.metrics.heightPixels - ((statusBar + 116 + 30 + 140) * mainActivity.skin.scale));
        int marginLeft = 12;
        marginLeft = (int) (marginLeft * mainActivity.skin.scale) ;
        int marginTop = 252;
        marginTop = (int) (marginTop * mainActivity.skin.scale) ;

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(rw, rh);
        params.setMargins(marginLeft, marginTop, 0, 0);
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        recyclerView.setLayoutParams(params);
        mainActivity.root.addView(recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));

        playlistAdapter = new PlaylistAdapter(songsList,
                new PlaylistAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Song song, int position) {
                        // Handle single tap: Play the song
                        playSong(song);
                        // If you want to clear selection on play, do it here:
                        if (actionMode != null) {
                            actionMode.finish(); // This will also call clearSelection in onDestroyActionMode
                        } else {
                            // If not in action mode, but want to clear any stray selection (less common scenario)
                            playlistAdapter.clearSelection();
                        }
                    }
                },
                new PlaylistAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(Song song, int position) {
                        // Long press initiates multi-select mode or adds to selection
//                        if (actionMode == null) {
//                            actionMode = startSupportActionMode(actionModeCallback);
//                        }
                        // The adapter's long click listener already handles toggling selection
                        // You might want to update the ActionMode title here if needed
                        if (actionMode != null) {
                            actionMode.setTitle(playlistAdapter.getSelectedSongs().size() + " selected");
                        }
                    }
                }
        );
        recyclerView.setAdapter(playlistAdapter);

        int height = (int) (mainActivity.skin.metrics.heightPixels / mainActivity.skin.scale) - statusBar - 38;
        int right = (int) (mainActivity.skin.metrics.widthPixels / mainActivity.skin.scale) - 25;
        for (int i = 232 ; i < height ; i++) {
            JSONObject sl = skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("sleft");
            sl.put("coordinates", new JSONArray(new int[]{0, i, 25, 29}));
            ImageView pl_sleft = (ImageView) createView(sl);
            pl_border_left_list.add(pl_sleft);
            mainActivity.root.addView(pl_sleft);
            JSONObject sl2 = skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("sright");
            sl2.put("coordinates", new JSONArray(new int[]{right, i, 25, 29}));
            ImageView pl_sleft2 = (ImageView) createView(sl2);
            mainActivity.root.addView(pl_sleft2);
            pl_border_right_list.add(pl_sleft2);

        }

        pl_left = (ImageView) createView(skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("left"));
        mainActivity.root.addView(pl_left);
        pl_right = (ImageView) createView(skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("right"));
        mainActivity.root.addView(pl_right);

        pl_title = (ImageView) createView(skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("title"));
        mainActivity.root.addView(pl_title);

        int bly = bleft.getJSONArray("coordinates").getInt(3);
        bly = (int)(mainActivity.skin.metrics.heightPixels / mainActivity.skin.scale) - bly - statusBar;
//        Log.i(TAG, "create: bottom left " + bly);
        bleft.put("coordinates", new JSONArray(new int[]{0, bly, 125, 38}));

        JSONObject blr = skinFormat.getJSONObject("playlist").getJSONObject("titlebar").getJSONObject("bright");
        blr.put("coordinates", new JSONArray(
                new int[]{
                        (int)(mainActivity.skin.metrics.widthPixels / mainActivity.skin.scale) - 150,
                        (int)(mainActivity.skin.metrics.heightPixels / mainActivity.skin.scale) - statusBar - 38,
                        150, 38
                }));

        pl_bleft = (ImageView) createView(bleft);
        mainActivity.root.addView(pl_bleft);
        pl_bright = (ImageView) createView(blr);
        mainActivity.root.addView(pl_bright);

        try {
            skin();
        } catch (Exception e) {
            Log.e(TAG, "create: failed to load user skin", e);
            mainActivity.skin.load();
            skin ();
        }

        volume.setMax(100);
        volume.setMin(0);
        balance.setMax(100);
        balance.setMin(-100);
        posbar.setMax(100);
        posbar.setMin(0);

        volume.setProgress(100);
        balance.setProgress(50);
        posbar.setThumbTintBlendMode(BlendMode.CLEAR);
        posbar.setProgress(0);

        plAdd = new Button(mainActivity);
        plAdd.setBackgroundColor(mainActivity.getResources().getColor(android.R.color.transparent));
        add (plAdd, 28, 25, 10, (int) (mainActivity.skin.metrics.heightPixels / mainActivity.skin.scale) - 86);

        plRemove = new Button(mainActivity);
        plRemove.setBackgroundColor(mainActivity.getResources().getColor(android.R.color.transparent));
        add (plRemove, 28, 25, 40, (int) (mainActivity.skin.metrics.heightPixels / mainActivity.skin.scale) - 86);

        plSelect = new Button(mainActivity);
        plSelect.setBackgroundColor(mainActivity.getResources().getColor(android.R.color.transparent));
        add (plSelect, 28, 25, 70, (int) (mainActivity.skin.metrics.heightPixels / mainActivity.skin.scale) - 86);

        plMisc = new Button(mainActivity);
        plMisc.setBackgroundColor(mainActivity.getResources().getColor(android.R.color.transparent));
        add (plMisc, 28, 25, 100, (int) (mainActivity.skin.metrics.heightPixels / mainActivity.skin.scale) - 86);

        plLoad = new Button(mainActivity);
        plLoad.setBackgroundColor(mainActivity.getResources().getColor(android.R.color.transparent));
        add (plLoad, 28, 25, (int) (mainActivity.skin.metrics.widthPixels / mainActivity.skin.scale) - 46, (int) (mainActivity.skin.metrics.heightPixels / mainActivity.skin.scale) - 86);

        playlistMenus();
    }

    void playlistMenus () {
        PopupMenu mAdd = new PopupMenu(mainActivity, plAdd);
        mAdd.getMenuInflater().inflate(R.menu.add, mAdd.getMenu());
        plAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdd.show();
            }
        });

        PopupMenu mRemove = new PopupMenu(mainActivity, plRemove);
        mRemove.getMenuInflater().inflate(R.menu.remove, mRemove.getMenu());

        plRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRemove.show();
            }
        });

        PopupMenu mSelect = new PopupMenu(mainActivity, plSelect);
        mSelect.getMenuInflater().inflate(R.menu.select, mSelect.getMenu());

        plSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelect.show();
            }
        });

        PopupMenu mMisc = new PopupMenu(mainActivity, plMisc);
        mMisc.getMenuInflater().inflate(R.menu.misc, mMisc.getMenu());

        plMisc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMisc.show();
            }
        });

        PopupMenu mLoad = new PopupMenu(mainActivity, plLoad);
        mLoad.getMenuInflater().inflate(R.menu.load_pl, mLoad.getMenu());

        plLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoad.show();
            }
        });

        mAdd.getMenu().findItem(R.id.pl_add_file).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                mainActivity.openFilePicker(MainActivity.AUDIO_FILE_REQUEST_CODE);
                return false;
            }
        });
    }

    void add (View view, int w, int h, int x, int y) {
        float scale = mainActivity.skin.scale;
        view.setLayoutParams(new ConstraintLayout.LayoutParams((int) (w * scale), (int) (h * scale)));
        view.setX(x * scale);
        view.setY(y * scale);
        mainActivity.root.addView(view);
    }

    UI (MainActivity _mainActivity) {
        mainActivity = _mainActivity;
        skinFormat = mainActivity.utils.loadJSONFromAsset("skinformat.json");
    }

    public View createView (JSONObject component) throws JSONException {
        View view = null ;
        ConstraintLayout.LayoutParams params = null ;
//        Log.i(TAG, "createView: " + component);
        boolean rotated = false ;
        switch (component.getString("type")) {
            case "button":
                view = new Button(mainActivity) ;
                break ;
            case "seekbar":
                view = new SeekBar(mainActivity) ;
                ((SeekBar) view).setSplitTrack(false);
                ((SeekBar) view).setProgressDrawable(null);
                if (component.has("vertical") && component.getBoolean("vertical")) {
                    ((SeekBar) view).setRotation(270);
                    rotated = true;
                }

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

            if (rotated) {
                int tmp = width ;
                width = height;
                height = tmp;
            }

            int marginLeft = coordinates.getInt(0);
            marginLeft = (int) (marginLeft * mainActivity.skin.scale) ;
            int marginTop = coordinates.getInt(1);
            marginTop = (int) (marginTop * mainActivity.skin.scale) ;

//            Log.i(TAG, String.format ("[view size]: %d x %d [%d %d]", width, height, marginLeft, marginTop));
            params = new ConstraintLayout.LayoutParams(width, height);
            params.setMargins(marginLeft, marginTop, 0, 0);
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            view.setLayoutParams(params);
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//            Log.d(TAG, String.format("Color: #%06X", (0xFFFFFF & color)));

//            view.setBackgroundColor(color);
            // Example of how to use a predefined color if needed later:
            // view.setBackgroundColor(mainActivity.getResources().getColor(R.color.blanched_almond));

        }

//        if (view != null) {
//            paintView (view, component, rotated);
//        }

        return view ;
    }

    void paintView (View view, JSONObject component, boolean rotated) throws JSONException {
        Bitmap bitmap = mainActivity.skin.skin.get(component.getString("source")).getBitmap();
        if (component.has("source_rect")) {
            JSONArray source_rect = component.getJSONArray("source_rect");
            Bitmap scaledBitmap = getCroppedScaledBitmap(bitmap, source_rect);
            if (scaledBitmap == null) {
                view.setBackgroundColor(mainActivity.getResources().getColor(android.R.color.transparent));
            } else {

                if (rotated) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                }

                view.setBackground(new BitmapDrawable(mainActivity.getResources(), scaledBitmap));
            }

            if (view instanceof ToggleButton) {
                HashMap <Integer, Bitmap> states = new HashMap<>();
                states.put(1, scaledBitmap);
                Bitmap toggled = getCroppedScaledBitmap(bitmap, component.getJSONArray("source_rect_on"));
                states.put(0, toggled);
                mainActivity.skin.states.put(component.getString("name"), states);
                ToggleButton toggleButton = (ToggleButton) view;
                toggleButton.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
                    final String name = component.getString("name") ;
                    @Override
                    public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
                        if (b) {
                            compoundButton.setBackground(new BitmapDrawable(mainActivity.getResources(), mainActivity.skin.states.get(name).get(1)));
                        } else {
                            compoundButton.setBackground(new BitmapDrawable(mainActivity.getResources(), mainActivity.skin.states.get(name).get(0)));
                        }
                    }
                });
            }

            if (view instanceof SeekBar) {
                SeekBar seekBar = (SeekBar) view;
                JSONArray source_rect1 = component.getJSONArray("thumb");
                int x1 = source_rect1.getInt(0);
                int y1 = source_rect1.getInt(1);
                int width1 = source_rect1.getInt(2);// - x;
                int height1 = source_rect1.getInt(3);//- y;

                Log.d(TAG, String.format("[thumb] %d x %d: %d x %d", x1, y1, width1, height1));
//                Log.d(TAG, "paintView: " + component);
                try {
                    Bitmap croppedBitmap1 = Bitmap.createBitmap(bitmap, x1, y1, width1, height1, null, true);
                    width1 = (int) (croppedBitmap1.getWidth() * mainActivity.skin.scale);
                    height1 = (int) (croppedBitmap1.getHeight() * mainActivity.skin.scale);
//                    Log.d(TAG, String.format("[thumb size]: %d x %d", width1, height1));
                    Bitmap scaledBitmap1 = Bitmap.createScaledBitmap(croppedBitmap1, width1, height1, true);
                    Drawable thumbDrawable = new BitmapDrawable(mainActivity.getResources(), scaledBitmap1);

                    if (!rotated)
                        seekBar.setThumb(thumbDrawable);
                    else {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);

                        scaledBitmap1 = Bitmap.createBitmap(scaledBitmap1, 0, 0, scaledBitmap1.getWidth(), scaledBitmap1.getHeight(), matrix, true);
                        thumbDrawable = new BitmapDrawable(mainActivity.getResources(), scaledBitmap1);
                        seekBar.setThumb(thumbDrawable);
                    }
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                    Log.e(TAG, "paintView: the skin doesnt have thumb it seems", e);
                    seekBar.setThumb(null);
                }

                if (component.has("bg") && component.getBoolean("bg")) {
                    int x = source_rect.getInt(0);
                    int y = source_rect.getInt(1);
                    int width = source_rect.getInt(2);// - x;
                    int height = source_rect.getInt(3);//- y;
                    int swidth = (int) (width * mainActivity.skin.scale);
                    int sheight = (int) (height * mainActivity.skin.scale);
                    HashMap<Integer, Bitmap> states = new HashMap<>();
                    for (int i = 0; i < 28; i++) {
                        int y_ = y + (height * i);
                        if (i > 0)
                            y_ = y_ + i;

//                            Log.d(TAG, String.format("[%s]: %d, %d %d %d %d", component.get("source"), i, x, y_, width, height));
                        if (y_ < 0) y_ = 0;
                        if (x + width > bitmap.getWidth())
                            width = bitmap.getWidth() - x;
                        if (y_ + height > bitmap.getHeight())
                            height = bitmap.getHeight() - y_;
                        Bitmap bg = Bitmap.createBitmap(bitmap, x, y_, width, height, null, true);
                        Bitmap bgs = Bitmap.createScaledBitmap(bg, (int) (swidth), (int) (sheight), true);
                        states.put(i, bgs);
                    }

//                        Log.d(TAG, String.format("put %s: %d", component.getString("source"), states.size()));
                    mainActivity.skin.states.put(component.getString("name"), states);
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        final String name = component.getString("name");

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                            Log.d(TAG, "All component names with states:");
//                            for (String componentNameKey : mainActivity.skin.states.keySet()) {
//                                Log.d(TAG, "Component Key: " + componentNameKey);
//                            }

                            int key = (int) ((i / 100.0f) * 27);
                            Bitmap bit = null;
//                                Log.d(TAG, String.format("%s %d: %d", name, i, key));
                            if (!mainActivity.skin.states.containsKey(name)) {
                                Log.e(TAG, String.format("onProgressChanged: no %s in states!", name));
                                return;
                            }

                            HashMap<Integer, Bitmap> state = mainActivity.skin.states.get(name);
                            assert state != null;
                            bit = state.get(key);
                            seekBar.setBackground(new BitmapDrawable(bit));
//                                Log.i(TAG, "onProgressChanged: bitmap changed");
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                }
            }

        }

    }

    Bitmap getCroppedScaledBitmap (Bitmap bitmap, JSONArray source_rect) throws JSONException {
        int x = source_rect.getInt(0);
        int y = source_rect.getInt(1);
        int width = source_rect.getInt(2) ;// - x;
        int height = source_rect.getInt(3) ;//- y;
        if (x + width > bitmap.getWidth())
            width = bitmap.getWidth() - x;
        if (y + height > bitmap.getHeight())
            height = bitmap.getHeight() - y;

        Bitmap croppedBitmap = null ;
        try {
            croppedBitmap = Bitmap.createBitmap(bitmap, x, y, width, height, null, true);
            width = (int) (width * mainActivity.skin.scale);
            height = (int) (height * mainActivity.skin.scale);
        } catch (Exception e) {
            Log.e(TAG, "getCroppedScaledBitmap: ", e);
            return null ;
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, (int) (width), (int)(height), true);
        return scaledBitmap;
    }

    private void playSong(Song song) {
        // ... your logic to start playing the selected song
        Toast.makeText(mainActivity, "Playing: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        mainActivity.player.play(song.getUri());
    }

    public void addToPlaylist (Uri uri) {
        Log.i(TAG, "addToPlaylist() called with: uri = [" + uri + "]");
//        Song song = new Song(uri);
        if (uri == null)
            return;
        Song song = new Song(uri);
        mainActivity.player.setMetadata(song);

        songsList.add(song);
        playlistAdapter.notifyItemInserted(songsList.size() - 1);
        saveCurrentPlaylist();
    }

    public void savePlaylist (String filename) {
        try {
            Utils.writeToFile(songsList, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCurrentPlaylist () {
        savePlaylist(defaultPlaylist);
    }

    public void loadCurrentPlaylist () {
        loadPlaylist(defaultPlaylist);
    }

    public void loadPlaylist (String filename) {
        ArrayList<String> list = new ArrayList<>();
        try {
            list = Utils.readLinesFromFile(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        songsList.clear();
        for (String uri : list) {
            addToPlaylist(Uri.parse(uri));
        }

        playlistAdapter.notifyDataSetChanged();
        saveCurrentPlaylist();
    }

    public void clearPlaylist () {
        songsList.clear();
        playlistAdapter.notifyDataSetChanged();
        saveCurrentPlaylist();
    }
}
