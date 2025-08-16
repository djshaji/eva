package org.acoustixaudio.eva;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

public class Player {
    private static final String TAG = "Player";
    public final ExoPlayer player;
    MainActivity mainActivity;
    MediaMetadataRetriever retriever = null ;

    Player (MainActivity _mainActivity) {
        mainActivity = _mainActivity;
        player = new ExoPlayer.Builder(mainActivity).build();
        retriever = new MediaMetadataRetriever();
    }

    public void play (Uri uri) {
        player.setMediaItem(new MediaItem.Builder().setUri(uri).build());
        player.prepare();
        player.play();
    }

    public void setMetadata (Song song) {
        Uri uri = song.getUri() ;
        if (uri == null || uri.toString().isEmpty() || uri.toString().equals("null")) {
            return ;
        }

        Log.d(TAG, "setMetadata: " + uri);
        try {
            retriever.setDataSource(mainActivity, uri);
        } catch (RuntimeException e) {
            Log.e(TAG, "setMetadata: " + e.getMessage());
            return ;
        }

        song.title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        song.artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        song.album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

        if (song.title == null) {
            song.title = mainActivity.getFileNameFromUri(uri);
        }

        Log.d(TAG, "setMetadata: " + song.title + " " + song.artist + " " + song.album);
    }
}
