package org.acoustixaudio.eva;

import androidx.annotation.Nullable;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

public class MediaService extends MediaSessionService {
      private MediaSession mediaSession = null;
      public ExoPlayer player;
  // Create your Player and MediaSession in the onCreate lifecycle event
  @Override
  public void onCreate() {
    // If you have an ExoPlayer instance which is currently playing media outside of a
    // MediaSessionService, you can pass it to the service when you start it.
    // MainActivity.player is the ExoPlayer instance that you are passing
    super.onCreate();
    // Get the player from the MainActivity
//    player = MainActivity.exoPlayer;
     player = new ExoPlayer.Builder(this).build(); // This is the original code
    mediaSession = new MediaSession.Builder(this, player).build();
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
