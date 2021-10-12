package com.ldt.musicr.ui.page.librarypage.song;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ldt.musicr.App;
import com.ldt.musicr.R;
import com.ldt.musicr.contract.AbsMediaAdapter;
import com.ldt.musicr.loader.medialoader.SongLoader;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.service.MusicPlayerRemote;
import com.ldt.musicr.service.MusicService;
import com.ldt.musicr.ui.page.MusicServiceFragment;
import com.ldt.musicr.ui.bottomsheet.SortOrderBottomSheet;
import com.ldt.musicr.util.InterpolatorUtil;
import com.ldt.musicr.util.Tool;
import com.ldt.musicr.util.Util;
import com.ldt.musicr.util.Utils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class SongChildTab extends MusicServiceFragment implements SortOrderBottomSheet.SortOrderChangedListener, PreviewRandomPlayAdapter.FirstItemCallBack {
    public static final String TAG = "SongChildTab";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

//    @BindView(R.id.preview_shuffle_list)
//    RecyclerView mPreviewRecyclerView;

    @BindView(R.id.refresh)
    ImageView mRefresh;

//    @BindView(R.id.image)
//    ImageView mImage;
//
//    @BindView(R.id.title)
//    TextView mTitle;
//
//    @BindView(R.id.description)
//    TextView mArtist;

    @BindView(R.id.random_group)
    Group mRandomGroup;
    @BindView(R.id.shuffle_button)
    ImageView shuffle_button;
    @BindView(R.id.repeat_button)
    ImageView repeat_button;
    private int mCurrentSortOrder = 0;

    private void initSortOrder() {
        mCurrentSortOrder = App.getInstance().getPreferencesUtility().getSongChildSortOrder();

    }
//    @BindView(R.id.top_background) View mTopBackground;
//    @BindView(R.id.bottom_background) View mBottomBackground;
//    @BindView(R.id.random_header) View mRandomHeader;
//    @BindView(R.id.shuffle_button) View ShuffleButton;


    @OnClick({R.id.shuffle_button})
    void shuffle() {
        if(Utils.isShuffle){
            Utils.isShuffle=false;
            shuffle_button.setImageDrawable(getResources().getDrawable(R.drawable.suffle_off));
//            Toasty.info(getContext(),"Shuffle Off", Toast.LENGTH_SHORT).show();
            MusicPlayerRemote.setShuffleMode(MusicService.SHUFFLE_MODE_NONE);
        }else {
            Utils.isShuffle=true;
            shuffle_button.setImageDrawable(getResources().getDrawable(R.drawable.suffle_on));
//            Toasty.info(getContext(),"Shuffle On", Toast.LENGTH_SHORT).show();
            repeat_button.setImageResource(R.drawable.repeat_off);
            MusicPlayerRemote.setShuffleMode(MusicService.REPEAT_MODE_NONE);
            MusicPlayerRemote.setShuffleMode(MusicService.SHUFFLE_MODE_SHUFFLE);
            mAdapter.shuffle();
        }
    }

    @OnClick({R.id.repeat_button})
    void repeat() {
        MusicPlayerRemote.setShuffleMode(MusicService.SHUFFLE_MODE_NONE);
        Utils.isShuffle=false;
        MusicPlayerRemote.cycleRepeatMode();
        updateShuffleState();
        updateRepeatState();
    }

    private void updateShuffleState() {
        int mode = MusicPlayerRemote.getShuffleMode();
        if (mode == MusicService.SHUFFLE_MODE_NONE)
            Log.d(TAG, "updateShuffleState: None");
        else if (mode == MusicService.SHUFFLE_MODE_SHUFFLE)
            Log.d(TAG, "updateShuffleState: Normal");
        else Log.d(TAG, "updateShuffleState: Auto");

        if (mode == MusicService.SHUFFLE_MODE_NONE) {
//            Toasty.info(getContext(),"Shuffle Off", Toast.LENGTH_SHORT).show();
            shuffle_button.setImageDrawable(getResources().getDrawable(R.drawable.suffle_off));
        }
        else{
//            Toasty.info(getContext(),"Shuffle On", Toast.LENGTH_SHORT).show();
            shuffle_button.setImageDrawable(getResources().getDrawable(R.drawable.suffle_on));
        }
    }

    private void updateRepeatState() {
        int mode = MusicPlayerRemote.getRepeatMode();

        switch (mode) {
            case MusicService.REPEAT_MODE_NONE:
//                Log.d(TAG, "updateRepeatState: None");
                repeat_button.setImageResource(R.drawable.repeat_off);
//                Toasty.info(getContext(),"Repeat Off", Toast.LENGTH_SHORT).show();
                break;
            case MusicService.REPEAT_MODE_THIS:
//                Log.d(TAG, "updateRepeatState: Current");
                repeat_button.setImageResource(R.drawable.repeat_one);
//                Toasty.info(getContext(),"Repeat One", Toast.LENGTH_SHORT).show();
                break;
            case MusicService.REPEAT_MODE_ALL:
                repeat_button.setImageResource(R.drawable.repeat_all);
//                Toasty.info(getContext(),"Repeat All", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "updateRepeatState: All");
        }
    }

    private final SongChildAdapter mAdapter = new SongChildAdapter();
//    PreviewRandomPlayAdapter mPreviewAdapter;

    @OnClick(R.id.refresh)
    void refresh() {
        mRefresh.animate().rotationBy(360).setInterpolator(InterpolatorUtil.getInterpolator(6)).setDuration(650);
        mRefresh.postDelayed(mAdapter::randomize, 300);
        Toasty.info(getContext(),"Playing list refreshed!!!", Toast.LENGTH_SHORT).show();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.screen_songs_tab, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setName(TAG);
        mAdapter.setCallBack(this);
        mAdapter.setSortOrderChangedListener(this);
        MusicPlayerRemote.setShuffleMode(MusicService.SHUFFLE_MODE_NONE);
        MusicPlayerRemote.setShuffleMode(MusicService.REPEAT_MODE_NONE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initSortOrder();
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);

        refreshData();
    }

    @Override
    public void onDestroyView() {
        mAdapter.destroy();
        super.onDestroyView();
    }

    private void refreshData() {
    /*    if(getContext() != null)
        SongLoader.doSomething(getContext());
*/
        ArrayList<Song> songs = SongLoader.getAllSongs(getActivity(), SortOrderBottomSheet.mSortOrderCodes[mCurrentSortOrder]);
        mAdapter.setData(songs);
        showOrHidePreview(!songs.isEmpty());

    }

    private void showOrHidePreview(boolean show) {
        int v = show ? View.VISIBLE : View.GONE;

        mRandomGroup.setVisibility(v);
    }

    @Override
    public void onFirstItemCreated(Song song) {
//        mTitle.setText(song.title);
//        mArtist.setText(song.artistName);
//
//        Glide.with(this)
//                .load(Util.getAlbumArtUri(song.albumId))
//                .placeholder(R.drawable.default_image_round)
//                .error(R.drawable.default_image_round)
//                .into(mImage);

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
            recyclerView.setPopupBgColor(Tool.getHeavyColor());
            recyclerView.setThumbColor(Tool.getHeavyColor());
        }
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PALETTE_CHANGED);
        super.onPaletteChanged();
    }

    @Override
    public void onPlayStateChanged() {
        if (mAdapter != null)
            mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onMediaStoreChanged() {
        ArrayList<Song> songs = SongLoader.getAllSongs(getActivity(), SortOrderBottomSheet.mSortOrderCodes[mCurrentSortOrder]);
        mAdapter.setData(songs);
        showOrHidePreview(!songs.isEmpty());
    }

    @Override
    public int getSavedOrder() {
        return mCurrentSortOrder;
    }

    @Override
    public void onOrderChanged(int newType, String name) {
        if (mCurrentSortOrder != newType) {
            mCurrentSortOrder = newType;
            App.getInstance().getPreferencesUtility().setSongChildSortOrder(mCurrentSortOrder);
            refreshData();
        }
    }
}
