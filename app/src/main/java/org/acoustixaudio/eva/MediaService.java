package org.acoustixaudio.eva;

import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

public class MediaService extends MediaSessionService {
      private MediaSession mediaSession = null;
      public ExoPlayer player;
      int [] eqBands = {60000, 170000, 310000, 600000, 1000000, 3000000, 6000000, 12000000, 14000000, 16000000};
      public static Equalizer equalizer;
      public static LoudnessEnhancer loudnessEnhancer;
      private String TAG = "MediaService";

  // Create your Player and MediaSession in the onCreate lifecycle event
  @OptIn(markerClass = UnstableApi.class) @Override
  public void onCreate() {
    // If you have an ExoPlayer instance which is currently playing media outside of a
    // MediaSessionService, you can pass it to the service when you start it.
    // MainActivity.player is the ExoPlayer instance that you are passing
    super.onCreate();
    // Get the player from the MainActivity
//    player = MainActivity.exoPlayer;
     player = new ExoPlayer.Builder(this).build(); // This is the original code
    mediaSession = new MediaSession.Builder(this, player).build();

    player.addListener(new Player.Listener() {
      @Override
      public void onAudioSessionIdChanged(int audioSessionId) {
        Player.Listener.super.onAudioSessionIdChanged(audioSessionId);
        equalizer = new Equalizer(0, audioSessionId);

        loudnessEnhancer = new LoudnessEnhancer(audioSessionId);
        Log.d(TAG, "Available EQ Bands:");
        for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
          Log.d(TAG, "Band " + i + ": " + equalizer.getCenterFreq(i) / 1000 + " Hz");
        }
        equalizer.setEnabled(false);
        for (short i = 0; i < eqBands.length; i++) {
          short band = equalizer.getBand(eqBands [i]) ;
          Log.d(TAG, String.format ("[%d]: %d", eqBands[i], band));
          equalizer.setBandLevel(band, (short) 0);
        }

      }
    });
  }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    // Remember to release the player and media session in onDestroy
  @Override
  public void onDestroy() {
    mediaSession.getPlayer().release();
    mediaSession.release();
    mediaSession = null;
    super.onDestroy();
  }

}
