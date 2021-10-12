package com.ldt.musicr.ui.page.subpages;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.ldt.musicr.R;
import com.ldt.musicr.contract.AbsMediaAdapter;
import com.ldt.musicr.glide.ArtistGlideRequest;

import com.ldt.musicr.model.Artist;
import com.ldt.musicr.ui.page.MusicServiceNavigationFragment;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;

public class ArtistPagerFragment extends MusicServiceNavigationFragment {
    private static final String TAG = "ArtistPagerFragment";
    private static final String ARTIST = "artist";
    public static ArtistPagerFragment newInstance(Artist artist) {

        Bundle args = new Bundle();
        if(artist!=null)
        args.putParcelable(ARTIST,artist);

        ArtistPagerFragment fragment = new ArtistPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Artist mArtist;

    @BindView(R.id.title)
    TextView mArtistText;


    @BindView(R.id.icon)
    ImageView mBigImage;

    @BindView(R.id.description) TextView mWiki;

    private boolean mBlockPhotoView = true;


    @OnClick(R.id.back)
    void goBack() {
        getMainActivity().onBackPressed();
    }

    @OnClick(R.id.play)
    void shuffle() {
        mAdapter.shuffle();
    }

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private final SongInArtistPagerAdapter mAdapter = new SongInArtistPagerAdapter("pl");

    @Override
    public void onDestroyView() {
        mAdapter.destroy();

        if(mUnbinder!=null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
        super.onDestroyView();
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.screen_single_artist_primary,container,false);
    }

    private Unbinder mUnbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setName(TAG);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        mUnbinder = ButterKnife.bind(this,view);

        Bundle bundle = getArguments();
        if(bundle!=null) {
            mArtist = bundle.getParcelable(ARTIST);
        }

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));

        refreshData();
    }
    private void updateSongs() {
        if(mArtist==null) return;
        mAdapter.setData(mArtist.getSongs());
    }

    public void refreshData() {
        if(mArtist==null) return;
        mArtistText.setText(mArtist.getName());
        String bio ="";
        if(!bio.isEmpty()) bio = ' '+getResources().getString(R.string.middle_dot)+' '+bio;
        mWiki.setText(mArtist.getSongCount() +" "+getResources().getString(R.string.songs)+bio);

        if(getContext() !=null) {
            ArtistGlideRequest.Builder.from(Glide.with(getContext()), mArtist)
                    .tryToLoadOriginal(true)
                    .generateBuilder(getContext())
                    .build()
                    .thumbnail(
                            ArtistGlideRequest
                                    .Builder
                                    .from(Glide.with(getContext()), mArtist)
                                    .generateBuilder(getContext())
                                    .build())
                    .into(mBigImage);
        }
            updateSongs();

    }

    @Override
    public void onServiceConnected() {
        refreshData();
    }


    @Override
    public void onPlayingMetaChanged() {
        Log.d(TAG, "onPlayingMetaChanged");
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onPaletteChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PALETTE_CHANGED);
        super.onPaletteChanged();
    }

    @Override
    public void onPlayStateChanged() {
        Log.d(TAG, "onPlayStateChanged");
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

    private static class ArtistInfoTask extends AsyncTask<Void,Void,Void> {
        private WeakReference<ResultCallback> mCallback;
        ArtistInfoTask(ResultCallback callback) {
            mCallback = new WeakReference<>(callback);
        }

        void cancel() {
            cancel(true);
            mCallback.clear();
        }

        @Override
        protected  Void doInBackground(Void... voids) {

            return null;
        }
    }
}
