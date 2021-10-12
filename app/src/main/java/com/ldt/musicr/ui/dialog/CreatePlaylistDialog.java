package com.ldt.musicr.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ldt.musicr.R;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.ui.page.librarypage.playlist.AddMultipleSong;
import com.ldt.musicr.util.PlaylistsUtil;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class CreatePlaylistDialog extends DialogFragment {

    int i = 0;
    private static final String SONGS = "songs";
    private static String from = "";
    Activity activity;

    @NonNull
    public static CreatePlaylistDialog create() {
        return create((Song) null);
    }

    @NonNull
    public static CreatePlaylistDialog create(@Nullable Song song) {
        ArrayList<Song> list = new ArrayList<>();
        if (song != null)
            list.add(song);
        return create(list, "without_song");
    }

    @NonNull
    public static CreatePlaylistDialog create(ArrayList<Song> songs, String s) {
        from = s;
        CreatePlaylistDialog dialog = new CreatePlaylistDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList(SONGS, songs);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (from == "with_song") {
            Dialog dialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.new_playlist_title)
                    .positiveText(R.string.create_action)
                    .negativeText(android.R.string.cancel)
                    .inputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                            InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                    .input(R.string.playlist_name_empty, 0, false, (materialDialog, charSequence) -> {
                        if (getActivity() == null)
                            return;
                        final String name = charSequence.toString().trim();
                        if (!name.isEmpty()) {
                            if (!PlaylistsUtil.doesPlaylistExist(getActivity(), name)) {
                                final int playlistId = PlaylistsUtil.createPlaylist(getActivity(), name);
                                if (getActivity() != null) {
                                    //noinspection unchecked
                                    ArrayList<Song> songs = getArguments().getParcelableArrayList(SONGS);
                                    if (songs != null && !songs.isEmpty()) {
                                        PlaylistsUtil.addToPlaylist(getActivity(), songs, playlistId, true);
                                    }
                                }
                            } else {

                                Toasty.warning(getActivity(), getActivity().getResources().getString(R.string.playlist_exists, name), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .build();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
            return dialog;
        } else {

            Dialog dialog2 = new MaterialDialog.Builder(getActivity())
                    .title(R.string.new_playlist_title)
                    .positiveText(R.string.create_action)
                    .negativeText(android.R.string.cancel)
                    .inputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                            InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                    .input(R.string.playlist_name_empty, 0, false, (materialDialog, charSequence) -> {
                        if (getActivity() == null)
                            return;
                        final String name = charSequence.toString().trim();
                        if (!name.isEmpty()) {
                            if (!PlaylistsUtil.doesPlaylistExist(getActivity(), name)) {
                                final int playlistId = PlaylistsUtil.createPlaylist(getActivity(), name);
                                if (getActivity() != null) {
                                    ArrayList<Song> songs = getArguments().getParcelableArrayList(SONGS);

                                    activity = getActivity();
                                    PlaylistsUtil.addToPlaylist(getActivity(), songs, playlistId, false);
                                    Dialog dialog1 = new MaterialDialog.Builder(getActivity())
                                            .title("Add songs to " + name )
                                            .positiveText("Add")
                                            .onPositive((dialog, which) -> {
                                                Intent intent = new Intent(activity, AddMultipleSong.class);
                                                intent.putExtra("playlist", name);
                                                activity.startActivity(intent);
                                            })
                                            .negativeText(android.R.string.cancel)
                                            .onNegative((dialog, which) -> {
                                                dialog.dismiss();
                                            }).build();
                                    dialog1.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
                                    dialog1.show();
                                }
                            }
                        }
                    })
                    .build();
            dialog2.getWindow().setBackgroundDrawableResource(R.drawable.dialog);
            return dialog2;
        }
    }

}

