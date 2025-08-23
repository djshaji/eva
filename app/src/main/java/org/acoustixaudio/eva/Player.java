package org.acoustixaudio.eva;

import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaController;

public class Player {
    private static final String TAG = "Player";
    public MediaController player;
    MainActivity mainActivity;
    MediaMetadataRetriever retriever = null ;

    Equalizer equalizer;
    LoudnessEnhancer loudnessEnhancer;

    int [] eqBands = {60000, 170000, 310000, 600000, 1000000, 3000000, 6000000, 12000000, 14000000, 16000000};

    @OptIn(markerClass = UnstableApi.class) Player (MainActivity _mainActivity, MediaController p) {
        mainActivity = _mainActivity;
//        player = new ExoPlayer.Builder(mainActivity).build();
        player = p;
        equalizer = MediaService.equalizer;
        loudnessEnhancer = MediaService.loudnessEnhancer;
        retriever = new MediaMetadataRetriever();
        AudioEffect.Descriptor[] effects = AudioEffect.queryEffects();
        Log.d(TAG, "Supported effects:");
        for (AudioEffect.Descriptor effect : effects) {
            Log.d(TAG, "  " + effect.name + " (" + effect.implementor + ")");
        }

    }

    public void init () {

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
