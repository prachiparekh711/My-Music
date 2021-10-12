package com.ldt.musicr.ui.page.subpages;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ldt.musicr.R;
import com.ldt.musicr.contract.AbsMediaAdapter;
import com.ldt.musicr.model.Album;
import com.ldt.musicr.ui.page.MusicServiceNavigationFragment;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import es.dmoral.toasty.Toasty;

public class AlbumPagerFragment extends MusicServiceNavigationFragment {
    private static final String TAG = "ArtistPagerFragment";
    private static final String ARTIST = "album";
    private final boolean mBlockPhotoView = true;
    private final SongInArtistPagerAdapter mAdapter = new SongInArtistPagerAdapter("pl");

    @BindView(R.id.root)
    View mRoot;
    @BindView(R.id.title)
    TextView mArtistText;
    @BindView(R.id.icon)
    ImageView icon;
    @BindView(R.id.author)
    TextView mWiki;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    private Album mAlbum;

    private Unbinder mUnbinder;

    public static AlbumPagerFragment newInstance(Album artist) {

        Bundle args = new Bundle();
        if (artist != null)
            args.putParcelable(ARTIST, artist);

        AlbumPagerFragment fragment = new AlbumPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @OnClick(R.id.back)
    void goBack() {
        getMainActivity().onBackPressed();
    }

    @OnClick(R.id.play)
    void shuffle() {
        Toasty.info(getActivity(), "Start playing all.", Toast.LENGTH_SHORT).show();
        mAdapter.playAll(0, true);
    }

    @Override
    public void onDestroyView() {
        mAdapter.destroy();

        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
        super.onDestroyView();
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.album_pager_primary, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setName(TAG);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mAlbum = bundle.getParcelable(ARTIST);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri,
                    mAlbum.getId());

            Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID + "=?",
                    new String[]{String.valueOf(mAlbum.getId())},
                    null);

            if (cursor.moveToFirst()) {

                Glide.with(getContext())
                        .asBitmap()
                        .load(uri)
                        .error(R.drawable.default_image_round)
                        .placeholder(R.drawable.default_image_round)
                        .into(icon);
            }
        }


        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        refreshData();
    }

    private void updateSongs() {
        if (mAlbum == null) return;
        mAdapter.setData(mAlbum.getSongs());
    }

    public void refreshData() {
        if (mAlbum == null) return;
        mArtistText.setText(mAlbum.getTitle());
        String bio = "";
        if (!bio.isEmpty()) bio = ' ' + getResources().getString(R.string.middle_dot) + ' ' + bio;
        mWiki.setText(mAlbum.getSongCount() + " " + getResources().getString(R.string.songs) + bio);
        icon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.sample));
        updateSongs();

    }

    @Override
    public void onServiceConnected() {
        refreshData();
    }


    @Override
    public void onPlayingMetaChanged() {
        if (mAdapter != null)
            mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onPaletteChanged() {
        if (mRecyclerView instanceof FastScrollRecyclerView) {
            FastScrollRecyclerView recyclerView = ((FastScrollRecyclerView) mRecyclerView);
            recyclerView.setPopupBgColor(getResources().getColor(R.color.backward_color));
            recyclerView.setThumbColor(getResources().getColor(R.color.backward_color));
        }
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PALETTE_CHANGED);
        super.onPaletteChanged();
    }

    @Override
    public void onPlayStateChanged() {
        Log.d(TAG, "onPlayStateChanged");
        if (mAdapter != null)
            mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onRepeatModeChanged() {

    }

    @Override
    public void onShuffleModeChanged() {

    }

    @Override
    public void onMediaStoreChanged() {
        refreshData();
    }

    private static class ArtistInfoTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<ResultCallback> mCallback;

        ArtistInfoTask(ResultCallback callback) {
            mCallback = new WeakReference<>(callback);
        }

        void cancel() {
            cancel(true);
            mCallback.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            return null;
        }
    }
}
