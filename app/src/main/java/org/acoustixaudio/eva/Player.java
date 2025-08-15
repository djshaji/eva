package org.acoustixaudio.eva;

import android.net.Uri;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

public class Player {
    public final ExoPlayer player;
    MainActivity mainActivity;

    Player (MainActivity _mainActivity) {
        mainActivity = _mainActivity;
        player = new ExoPlayer.Builder(mainActivity).build();
    }

    public void play (Uri uri) {
        player.setMediaItem(new MediaItem.Builder().setUri(uri).build());
        player.prepare();
        player.play();
    }
}
