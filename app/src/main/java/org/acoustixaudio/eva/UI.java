package org.acoustixaudio.eva;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class UI {
    MainActivity mainActivity ;
    Button prev, play, pause, next, stop, eject ;
    ToggleButton shuffle, repeat, toggle_playlist, toggle_eq ;
    SeekBar seekBar, volume, balance ;

    GridLayout mainWindow ;

    UI (MainActivity _mainActivity) {
        mainActivity = _mainActivity;
        mainWindow = mainActivity.findViewById(R.id.main_window);
    }

    public void create () {
        prev = new Button(mainActivity);
        Button b = mainActivity.findViewById(R.id.bmin);
    }
}
