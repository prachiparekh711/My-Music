package com.ldt.musicr.ui.page.librarypage.artist;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ldt.musicr.App;
import com.ldt.musicr.R;
import com.ldt.musicr.contract.AbsMediaAdapter;
import com.ldt.musicr.loader.medialoader.AlbumLoader;
import com.ldt.musicr.model.Album;
import com.ldt.musicr.model.Genre;
import com.ldt.musicr.ui.page.MusicServiceFragment;
import com.ldt.musicr.ui.page.subpages.AlbumPagerFragment;
import com.ldt.musicr.ui.widget.fragmentnavigationcontroller.NavigationFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AlbumChildTab extends MusicServiceFragment implements AlbumAdapter.AlbumClickListener {
    public static final String TAG = "AlbumChildTab";
    private final AlbumAdapter mAdapter = new AlbumAdapter();
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @Nullable
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    Unbinder mUnBinder;
    private LoadArtistAsyncTask mLoadArtist;
    @BindView(R.id.number)
    TextView number;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.album_child_tab, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setName(TAG);
        mAdapter.setAlbumClickListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnBinder = ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        mRecyclerView.setAdapter(mAdapter);
        ViewCompat.setOnApplyWindowInsetsListener(mRecyclerView, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                v.setPadding(insets.getSystemWindowInsetLeft(),
                        0,
                        insets.getSystemWindowInsetRight(),
                        (int) (insets.getSystemWindowInsetBottom() + v.getResources().getDimension(R.dimen.bottom_back_stack_spacing)));
                return ViewCompat.onApplyWindowInsets(v, insets);
            }
        });

        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setOnRefreshListener(this::refresh);
        }
        refresh();

    }

    @Override
    public void onDestroyView() {

        if (mLoadArtist != null) mLoadArtist.cancel(true);
        mAdapter.destroy();
        if (mUnBinder != null)
            mUnBinder.unbind();

        super.onDestroyView();
    }

    private void refresh() {

        if (mLoadArtist != null) mLoadArtist.cancel(true);
        mLoadArtist = new LoadArtistAsyncTask(this);
        mLoadArtist.execute();

    }

    @Override
    public void onAlbumItemClick(Album artist) {
        NavigationFragment sf = AlbumPagerFragment.newInstance(artist);
        /*      SupportFragment sf = ArtistTrialPager.newInstance(artist);*/
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof NavigationFragment)
            ((NavigationFragment) parentFragment).getNavigationController().presentFragment(sf);
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
        refresh();
    }

    private static class AsyncResult {
        private ArrayList<Genre>[] mGenres;
        private List<Album> mArtist;
    }

    private static class LoadArtistAsyncTask extends AsyncTask<Void, Void, AsyncResult> {
        private final WeakReference<AlbumChildTab> mFragment;
        private boolean mCancelled = false;

        public LoadArtistAsyncTask(AlbumChildTab fragment) {
            super();
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        protected AsyncResult doInBackground(Void... voids) {
            AsyncResult result = new AsyncResult();
            Context context = null;

            if (App.getInstance() != null)
                context = App.getInstance().getApplicationContext();

            if (context != null)
                result.mArtist = AlbumLoader.getAllAlbums(App.getInstance());
            else return null;


            return result;
        }

        public void cancel() {
            mCancelled = true;
            cancel(true);
            mFragment.clear();
        }

        @Override
        protected void onPostExecute(AsyncResult asyncResult) {
            if (mCancelled) return;
            AlbumChildTab fragment = mFragment.get();
            if (fragment != null && !fragment.isDetached()) {
                if (fragment.mSwipeRefreshLayout != null)
                    fragment.mSwipeRefreshLayout.setRefreshing(false);
                if (!asyncResult.mArtist.isEmpty()) {
                    fragment.mAdapter.setData(asyncResult.mArtist);
                    fragment.number.setText(String.valueOf(asyncResult.mArtist.size()));
                }
                fragment.mLoadArtist = null;
            }
        }


    }
}
