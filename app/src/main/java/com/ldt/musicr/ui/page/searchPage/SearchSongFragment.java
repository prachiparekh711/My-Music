package com.ldt.musicr.ui.page.searchPage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.ldt.musicr.App;
import com.ldt.musicr.R;
import com.ldt.musicr.contract.AbsMediaAdapter;
import com.ldt.musicr.loader.medialoader.SongLoader;
import com.ldt.musicr.model.Playlist;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.ui.bottomsheet.SortOrderBottomSheet;
import com.ldt.musicr.ui.page.MusicServiceNavigationFragment;
import com.ldt.musicr.ui.page.featurepage.FeatureSongAdapter;
import com.ldt.musicr.ui.page.librarypage.artist.ArtistAdapter;
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

public class SearchSongFragment extends MusicServiceNavigationFragment implements SortOrderBottomSheet.SortOrderChangedListener {

    private final FeatureSongAdapter mAdapter = new FeatureSongAdapter();
    private final ArrayList<Song> searchResults = new ArrayList<>();
    private final Executor mSearchExecutor = Executors.newSingleThreadExecutor();
    @BindView(R.id.search_recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.playlist_pager_collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    SongMiniAdapter mSongMiniAdapter;
    Playlist mPlaylist;
    ViewGroup springBackLayout;
    //search
    androidx.appcompat.widget.SearchView mSerachbar;
    Bundle bundle;
    //

    Bitmap mPreviewBitmap;
    private String queryString;
    @Nullable
    private AsyncTask mSearchTask = null;
    private Unbinder mUnbinder;
    private int mCurrentSortOrder = 0;

    public static SearchSongFragment newInstance(Context context) {
        SearchSongFragment fragment = new SearchSongFragment();

        return fragment;
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

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        View view= inflater.inflate(R.layout.fragment_search_song, container, false);
        Utils.hideKeyboard(requireActivity());
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());

    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideKeyboard(requireActivity());
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
        setSuggestedSongs(SongLoader.getAllSongs(getContext()));

        mSerachbar = view.findViewById(R.id.searchBar);

        mSerachbar.setQueryHint("Search Your Music");
        mSerachbar.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onQueryTextChange(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals(queryString)) {
                    return true;
                }
                if (mSearchTask != null) {
                    mSearchTask.cancel(false);
                    mSearchTask = null;
                }
                queryString = newText;
                if (queryString.trim().equals("")) {
                    searchResults.clear();
                    mAdapter.updateSearchResults(SongLoader.getAllSongs(getContext()));
                    mAdapter.notifyDataSetChanged();
                } else {
                    mSearchTask = new SearchTask().executeOnExecutor(mSearchExecutor, queryString);
                    Log.d("AAAABBBBBB", "TaskCanelled? " + (mSearchTask.isCancelled()));
                }

                return true;
            }
        });

        mSerachbar.setIconifiedByDefault(false);
        mSerachbar.setIconified(false);
        if (savedInstanceState != null && savedInstanceState.containsKey("QUERY_STRING")) {
            bundle = savedInstanceState;
        }
        if (bundle != null && bundle.containsKey("QUERY_STRING")) {
            mSerachbar.setQuery(bundle.getString("QUERY_STRING"), true);
        }
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

    @Override
    public int getSavedOrder() {
        return mCurrentSortOrder;
    }

    @Override
    public void onOrderChanged(int newType, String name) {
        if (mCurrentSortOrder != newType) {
            mCurrentSortOrder = newType;
            App.getInstance().getPreferencesUtility().getSharePreferences().edit().putInt("sort_order_playlist_" + mPlaylist.name + "_" + mPlaylist.id, mCurrentSortOrder).commit();

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

            mAdapter.setData(playlists);

        }

        public void notifyDataSetChanged() {
            mAdapter.notifyDataSetChanged();
        }

        public int getItemCount() {
            return mAdapter.getItemCount();
        }
    }

    private class SearchTask extends AsyncTask<String, Void, ArrayList<Song>> {

        @Override
        protected ArrayList<Song> doInBackground(String... params) {
            ArrayList<Song> results = new ArrayList<>(27);
            List<Song> songList = SongLoader.searchSongs(getContext(), params[0], 10);
            if (!songList.isEmpty()) {

                results.addAll(songList);
            }
            boolean canceled = isCancelled();
            if (canceled) {
                return null;
            }
//
            if (results.size() == 0) {
//                Toast.makeText(getContext(),"Nothing Found",Toast.LENGTH_LONG).show();
            }
            return results;
        }

        @Override
        protected void onPostExecute(ArrayList<Song> objects) {
            super.onPostExecute(objects);
            mSearchTask = null;
            if (objects != null) {
                mAdapter.updateSearchResults(objects);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

}
