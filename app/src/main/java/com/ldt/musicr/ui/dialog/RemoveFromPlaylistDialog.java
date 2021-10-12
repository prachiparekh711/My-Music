package com.ldt.musicr.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ldt.musicr.R;
import com.ldt.musicr.loader.medialoader.PlaylistLoader;
import com.ldt.musicr.model.Playlist;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.util.PlaylistsUtil;
import com.ldt.musicr.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class RemoveFromPlaylistDialog extends DialogFragment {

    @NonNull
    public static RemoveFromPlaylistDialog create(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static RemoveFromPlaylistDialog create(ArrayList<Song> songs) {
        RemoveFromPlaylistDialog dialog = new RemoveFromPlaylistDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("songs", songs);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.e("Playlist",Utils.playlist.getName());
        final ArrayList<Song> songs = getArguments().getParcelableArrayList("songs");
        Dialog  dialog1= new MaterialDialog.Builder(getActivity())
                .title("Delete from playlist")
                .positiveText(R.string.delete_action)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, which) -> {
                    if (getActivity() == null)
                        return;
                    boolean isRename=PlaylistsUtil.removeFromPlaylist(getActivity(), songs.get(0), Utils.playlist.id);
                    if(isRename){
                        getActivity().onBackPressed();
                    }
                })
                .build();
        dialog1.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
        return dialog1;

    }
}
