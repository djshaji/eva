package org.acoustixaudio.eva;

import android.net.Uri;

public class Song {
    private long id;
    private Uri uri;
    private String title;
    private String artist;
    private String albumArtUri; // Optional: Path to album art
    private boolean isSelected; // To track selection state

    public Song(long id, Uri uri, String title, String artist, String albumArtUri) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.albumArtUri = albumArtUri;
        this.isSelected = false;
        this.uri = uri ;
    }

    Song (Uri uri) {
        this.uri = uri;
        this.title = uri.getPath();
        this.artist = "Unknown";
        this.isSelected = false;
        this.id = 0;
    }
    // Getters
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public boolean isSelected() {
        return isSelected;
    }

    // Setter for isSelected
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Uri getUri() {
        return uri;
    }

    // You might want to override equals() and hashCode() if you're using Sets of Songs directly
}