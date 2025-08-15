package org.acoustixaudio.eva;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.SongViewHolder> {

    private List<Song> songs;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    private boolean multiSelectMode = false;
    private Set<Integer> selectedItems = new HashSet<>(); // Store indices of selected items

    // Interfaces for click listeners
    public interface OnItemClickListener {
        void onItemClick(Song song, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Song song, int position);
    }

    public PlaylistAdapter(List<Song> songs, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
        this.songs = songs;
        this.onItemClickListener = onItemClickListener;
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item_layout, parent, false); // Create your list item layout
        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song currentSong = songs.get(position);
        holder.bind(currentSong, position);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    private void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
            songs.get(position).setSelected(false);
        } else {
            selectedItems.add(position);
            songs.get(position).setSelected(true);
        }
        notifyItemChanged(position);

        if (selectedItems.isEmpty()) {
            multiSelectMode = false;
            // Potentially notify your Activity/Fragment to hide contextual action mode
        }
    }

    public List<Song> getSelectedSongs() {
        List<Song> selectedSongsList = new ArrayList<>();
        for (Integer position : selectedItems) {
            if (position >= 0 && position < songs.size()) { // Boundary check
                selectedSongsList.add(songs.get(position));
            }
        }
        return selectedSongsList;
    }

    public void clearSelection() {
        Set<Integer> previouslySelectedItems = new HashSet<>(selectedItems);
        selectedItems.clear();
        multiSelectMode = false;
        for (Integer position : previouslySelectedItems) {
            if (position >= 0 && position < songs.size()) { // Boundary check
                songs.get(position).setSelected(false);
                notifyItemChanged(position);
            }
        }
    }

    public void setMultiSelectMode(boolean enabled) {
        multiSelectMode = enabled;
        if (!enabled) {
            clearSelection();
        }
        // You might want to notify the adapter about changes if the visual state needs an update
        // for all items when mode changes, though individual item updates are handled in toggleSelection.
    }


    class SongViewHolder extends RecyclerView.ViewHolder {
        // Get references to your list item views (e.g., TextView for title, ImageView for album art)
        TextView titleTextView;
        TextView artistTextView;
        // ... other views

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.song_title);
            artistTextView = itemView.findViewById(R.id.song_artist);
            // ... initialize other views
        }

        void bind(final Song song, final int position) {
            titleTextView.setText(song.getTitle());
            artistTextView.setText(song.getArtist());
            // Load album art if you have it (using a library like Glide or Picasso)

            // Highlight selected items
            itemView.setActivated(selectedItems.contains(position));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (multiSelectMode) {
                        toggleSelection(getAdapterPosition());
                    } else {
                        if (onItemClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                            onItemClickListener.onItemClick(songs.get(getAdapterPosition()), getAdapterPosition());
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!multiSelectMode) {
                        multiSelectMode = true;
                        // Potentially notify your Activity/Fragment to show contextual action mode
                    }
                    toggleSelection(getAdapterPosition());
                    if (onItemLongClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        onItemLongClickListener.onItemLongClick(songs.get(getAdapterPosition()), getAdapterPosition());
                    }
                    return true; // Consume the long click
                }
            });
        }
    }
}