package com.ldt.musicr.ui.page.favouritePage;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.ldt.musicr.App;
import com.ldt.musicr.R;
import com.ldt.musicr.contract.AbsMediaAdapter;
import com.ldt.musicr.loader.medialoader.SongLoader;
import com.ldt.musicr.model.Playlist;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.ui.page.MusicServiceNavigationFragment;
import com.ldt.musicr.ui.page.featurepage.FeatureSongAdapter;
import com.ldt.musicr.ui.page.librarypage.artist.ArtistAdapter;
import com.ldt.musicr.ui.page.searchPage.SearchSongFragment;
import com.ldt.musicr.ui.widget.fragmentnavigationcontroller.PresentStyle;
import com.ldt.musicr.util.Tool;
import com.ldt.musicr.util.Utils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FavouriteFragment extends MusicServiceNavigationFragment {

    private final FeatureSongAdapter mAdapter = new FeatureSongAdapter();

    @BindView(R.id.fav_recycler)
    RecyclerView mRecyclerView;

    @BindView(R.id.noData)
    RelativeLayout noData;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    SongMiniAdapter mSongMiniAdapter;
    Playlist mPlaylist;
    ViewGroup springBackLayout;

    Bitmap mPreviewBitmap;

    private Unbinder mUnbinder;
    private int mCurrentSortOrder = 0;

    public static FavouriteFragment newInstance(String param1, String param2) {
        FavouriteFragment fragment = new FavouriteFragment();

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_favourite, container, false);
    }

    @Override
    public int getPresentTransition() {
        return PresentStyle.ACCORDION_LEFT;
    }

    public void setTheme() {
        int buttonColor = ArtistAdapter.lighter(Tool.getBaseColor(), 0.25f);
        int heavyColor = Tool.getHeavyColor();


        if (mRecyclerView instanceof FastScrollRecyclerView) {
            ((FastScrollRecyclerView) mRecyclerView).setPopupBgColor(heavyColor);
            ((FastScrollRecyclerView) mRecyclerView).setThumbColor(heavyColor);
        }
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onPlayingMetaChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onPaletteChanged() {
        setTheme();
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PALETTE_CHANGED);
        super.onPaletteChanged();
    }

    @Override
    public void onPlayStateChanged() {
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        initSortOrder();


        setupToolbar();
        setTheme();

        if (mPreviewBitmap != null) {

            mPreviewBitmap = null;
        }

        springBackLayout = view.findViewById(R.id.springBackLayout);
        mSongMiniAdapter = new SongMiniAdapter(springBackLayout);

    }


    @Override
    public void onResume() {
        super.onResume();
        setSuggestedSongs(SharedPrefrences.getFavouriteList(getContext()));
        Utils.hideKeyboard(requireActivity());
    }

    public void setSuggestedSongs(List<Song> song) {
        mSongMiniAdapter.bind(song);
    }

    @Override
    public void onSetStatusBarMargin(int value) {
        ((ViewGroup.MarginLayoutParams) toolbar.getLayoutParams()).topMargin = value;
        toolbar.requestLayout();
        int padding_top_back_constraint = (int) (56 * getResources().getDimension(R.dimen.oneDP) + 2 * value);

    }

    private void setupToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setDisplayShowHomeEnabled(true);
                ab.setDisplayShowTitleEnabled(false);
            }
        }
    }

    private void initSortOrder() {
        if (mPlaylist != null && !mPlaylist.name.isEmpty()) {
            int defaultOrder = 0;
            if (mPlaylist.name.equals(getResources().getString(R.string.playlist_last_added)))
                defaultOrder = 2;
            mCurrentSortOrder = App.getInstance().getPreferencesUtility().getSharePreferences().getInt("sort_order_playlist_" + mPlaylist.name + "_" + mPlaylist.id, defaultOrder);

        }
    }


    public class SongMiniAdapter {
        private final View mItemView;

        SongMiniAdapter(View v) {
            this.mItemView = v;
            ButterKnife.bind(this, v);
            mAdapter.init(getContext());
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            mRecyclerView.setAdapter(mAdapter);

        }

        @SuppressLint("DefaultLocale")
        public void bind(List<Song> playlists) {
            if (playlists != null && playlists.size() > 0) {
                mRecyclerView.setVisibility(View.VISIBLE);
                noData.setVisibility(View.GONE);
                mAdapter.setData(playlists);
            } else {
                mRecyclerView.setVisibility(View.GONE);
                noData.setVisibility(View.VISIBLE);
            }
        }

        public void notifyDataSetChanged() {
            mAdapter.notifyDataSetChanged();
        }

        public int getItemCount() {
            return mAdapter.getItemCount();
        }
    }

}