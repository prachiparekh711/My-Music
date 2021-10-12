package com.ldt.musicr.ui.nowplaying;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldt.musicr.R;
import com.ldt.musicr.contract.AbsBindAbleHolder;
import com.ldt.musicr.contract.AbsSongAdapter;
import com.ldt.musicr.glide.ArtistGlideRequest;
import com.ldt.musicr.helper.menu.SongMenuHelper;
import com.ldt.musicr.loader.medialoader.ArtistLoader;
import com.ldt.musicr.loader.medialoader.SongLoader;
import com.ldt.musicr.model.Artist;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.service.MusicPlayerRemote;
import com.ldt.musicr.service.MusicService;
import com.ldt.musicr.service.MusicServiceEventListener;
import com.ldt.musicr.ui.AppActivity;
import com.ldt.musicr.ui.CardLayerController;
import com.ldt.musicr.ui.MusicServiceActivity;
import com.ldt.musicr.ui.bottomsheet.OptionBottomSheet;
import com.ldt.musicr.ui.page.CardLayerFragment;
import com.ldt.musicr.ui.page.favouritePage.SharedPrefrences;
import com.ldt.musicr.ui.widget.avsb.AudioVisualSeekBar;
import com.ldt.musicr.util.MusicUtil;
import com.ldt.musicr.util.SortOrder;
import com.ldt.musicr.util.Tool;
import com.ldt.musicr.util.Util;
import com.ldt.musicr.util.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class NowPlayingLayerFragment extends CardLayerFragment implements MusicServiceEventListener, AudioVisualSeekBar.OnSeekBarChangeListener, PalettePickerAdapter.OnColorChangedListener {
    public static final int WHAT_CARD_LAYER_HEIGHT_CHANGED = 101;
    public static final int WHAT_RECYCLER_VIEW_SMOOTH_SCROLL_TO_CURRENT_POSITION = 102;
    public static final int WHAT_UPDATE_CARD_LAYER_RADIUS = 103;
    public static final int WHAT_ = 104;
    private static final String TAG = "NowPlayingLayerFragment";

    @BindView(R.id.root)
    CardView mRoot;
    @BindView(R.id.dim_view)
    View mDimView;
    @BindView(R.id.minimize_bar)
    View mMinimizeBar;

//    @BindView(R.id.recycler_view)
//    RecyclerView mRecyclerView;

    @BindView(R.id.image)
    ImageView mImage;

    @BindView(R.id.fav)
    ImageView mfav;

    /*@BindView(R.id.view_pager)
    ViewPager2 mViewPager;*/

    @BindView(R.id.back)
    ImageView back;

    @BindView(R.id.visual_seek_bar)
    AudioVisualSeekBar mVisualSeekBar;
    /*@BindView(R.id.time_text_view)
    TextView mTimeTextView;*/
    @BindView(R.id.tvElipsedTime)
    TextView tvElipsedTime;
    @BindView(R.id.tvTotalTime)
    TextView tvTotalTime;
    @BindView(R.id.big_title)
    TextView mBigTitle;
    @BindView(R.id.big_artist)
    TextView mBigArtist;
    @BindView(R.id.safeViewTop)
    View mSpacingInsetTop;
    @BindView(R.id.safeViewBottom)
    View mSpacingInsetBottom;

    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.mAlbum)
    TextView mAlbum;
    @BindView(R.id.mStartTime)
    TextView mStartTime;
    @BindView(R.id.mStopTime)
    TextView mStopTime;
    @BindView(R.id.mSeekbar)
    SeekBar mSeekbar;
    @BindView(R.id.playlist_title)
    TextView mPlaylistTitle;
    @BindView(R.id.button_right)
    ImageView mButtonRight;
    @BindView(R.id.prev_button)
    ImageView mPrevButton;
    @BindView(R.id.next_button)
    ImageView mNextButton;
    @BindView(R.id.play_pause_button)
    ImageView mPlayPauseButton;
    @BindView(R.id.shuffle_button)
    ImageView shuffle_button;
    @BindView(R.id.repeat_button)
    ImageView repeat_button;

    /*
     *
     *  Implement MusicServiceEventListener
     *
     */
    @BindView(R.id.constraint_root)
    MotionLayout mConstraintRoot;
    boolean fragmentPaused = false;
    private float mMaxRadius = 18;
    private boolean isTouchedVisualSeekbar = false;
    private int overflowcounter = 0;
    private boolean mTimeTextIsSync = false;
    private String timeTextViewTemp = "00:00";
    private MyReceiver myReceiver;
    private final Handler mNowPlayingHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            int what = msg.what;
            if (isAdded() && !isDetached() && !isRemoving()) {
                switch (what) {
                    case WHAT_CARD_LAYER_HEIGHT_CHANGED:
                        handleLayerHeightChanged();
                        break;
                    case WHAT_RECYCLER_VIEW_SMOOTH_SCROLL_TO_CURRENT_POSITION:
                        handleRecyclerViewScrollToCurrentPosition();
                        break;
                    case WHAT_UPDATE_CARD_LAYER_RADIUS:
                        handleUpdateCardLayerRadius();
                        break;
                }
            }
        }
    };


    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            CardLayerController.CardLayerAttribute a = getCardLayerController().getMyAttr(NowPlayingLayerFragment.this);
            if (a != null) {
                    a.animateToMin();
            }
        }
    }
    private String timeTextViewTemp1 = "00:00";

    /*
     *
     *   End of Implementing MusicServiceEventListener
     *
     */
    // seekbar
    public Runnable mUpdateProgress = new Runnable() {
        @Override
        public void run() {
            long position = MusicPlayerRemote.getSongProgressMillis();

            if (!isTouchedVisualSeekbar) {

                long pos = position;
                long duration = MusicPlayerRemote.getSongDurationMillis();
                int minute = (int) (pos / 1000 / 60);
                int second = (int) (pos / 1000 - minute * 60);
                int dur_minute = (int) (duration / 1000 / 60);
                int dur_second = (int) (duration / 1000 - dur_minute * 60);

                String text = "";
                String text1 = "";
                if (minute < 10) text += "0";
                text += minute + ":";
                if (second < 10) text += "0";
                text += second;
                if (dur_minute < 10) text1 += "0";
                text1 += dur_minute + ":";
                if (dur_second < 10) text1 += "0";
                text1 += dur_second;

                mStartTime.setText(text);
                mStopTime.setText(text1);
                mSeekbar.setProgress((int) position);
                setTextTime(position, MusicPlayerRemote.getSongDurationMillis());
            }

            if (mVisualSeekBar != null) {
                mVisualSeekBar.setProgress((int) position);
                //TODO: Set elapsedTime
            }
            overflowcounter--;
            if (MusicPlayerRemote.isPlaying()) {
                //TODO: ???
                int delay = (int) (150 - (position) % 100);
                if (overflowcounter < 0 && !fragmentPaused) {
                    overflowcounter++;
                    mVisualSeekBar.postDelayed(mUpdateProgress, delay);
                }
            }
        }
    };

    @OnClick({R.id.shuffle_button})
    void shuffle_btn() {
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
        }
    }

    @OnClick({R.id.repeat_button})
    void repeat_btn() {
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
                Log.d(TAG, "updateRepeatState: None");
                repeat_button.setImageResource(R.drawable.repeat_off);
//                Toasty.info(getContext(),"Repeat Off", Toast.LENGTH_SHORT).show();
                break;
            case MusicService.REPEAT_MODE_THIS:
                Log.d(TAG, "updateRepeatState: Current");
                repeat_button.setImageResource(R.drawable.repeat_one);
//                Toasty.info(getContext(),"Repeat One", Toast.LENGTH_SHORT).show();
                break;
            case MusicService.REPEAT_MODE_ALL:
                repeat_button.setImageResource(R.drawable.repeat_all);
//                Toasty.info(getContext(),"Repeat All", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "updateRepeatState: All");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return inflater.inflate(R.layout.screen_now_playing, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mMaxRadius = getResources().getDimension(R.dimen.max_radius_layer);
        mTitle.setSelected(true);
        myReceiver = new MyReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(myReceiver,
                new IntentFilter("player"));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardLayerController.CardLayerAttribute a = getCardLayerController().getMyAttr(NowPlayingLayerFragment.this);
                if (a != null) {
                    if (a.getState() == CardLayerController.CardLayerAttribute.MINIMIZED)
                        a.animateToMax();
                    else
                        a.animateToMin();
                }
            }
        });

        mfav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean exist=false;
                int pos=0;
                Song song = MusicPlayerRemote.getCurrentSong();
                List<Song> helperList = new ArrayList<>();
                helperList=SharedPrefrences.getFavouriteList(getContext());
                if(helperList!=null && helperList.size()>0){
                    for (int i = 0; i < helperList.size(); i++) {
                        Song song1=helperList.get(i);
                        if (song1.id==song.id) {
                            exist = true;
                            pos = i;
                            break;
                        } else {
                            exist = false;
                        }
                    }
                }
                if (!exist) {
                    helperList.add(song);
                    mfav.setImageDrawable(getResources().getDrawable(R.drawable.color_heart));
                } else {
                    helperList.remove(pos);
                    mfav.setImageDrawable(getResources().getDrawable(R.drawable.black_heart));
                    exist = false;
                }

                SharedPrefrences.setFavouriteList(getContext(),helperList);
            }
        });

        //mRecyclerView.setPageTransformer(false, new SliderTransformer());
//        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        ViewCompat.setOnApplyWindowInsetsListener(mSpacingInsetTop, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), 0);
                v.requestLayout();
                return ViewCompat.onApplyWindowInsets(v, insets);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(mSpacingInsetBottom, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                v.setPadding(insets.getSystemWindowInsetLeft(), 0, insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
                v.requestLayout();
                return ViewCompat.onApplyWindowInsets(v, insets);
            }
        });

        //mViewPager.setAdapter(mAdapter);

//        snapHelper.attachToRecyclerView(mRecyclerView);

        //mViewPager.setOnTouchListener((v, event) -> mLayerController.streamOnTouchEvent(mRoot,event));
//        mRecyclerView.setOnTouchListener((v, event) -> mCardLayerController.dispatchOnTouchEvent(mRoot, event));
        mVisualSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mCardLayerController.dispatchOnTouchEvent(mRoot, event) && event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_UP;
            }
        });

        mVisualSeekBar.setOnSeekBarChangeListener(this);
        Log.d(TAG, "onViewCreated");
        if (getActivity() instanceof MusicServiceActivity) {
            ((AppActivity) getActivity()).addMusicServiceEventListener(this, true);
        }
        new Handler(Looper.getMainLooper()).post(this::setUp);
    }

    @Override
    public void onDestroyView() {

        if (getActivity() instanceof MusicServiceActivity) {
            ((AppActivity) getActivity()).removeMusicServiceEventListener(this);
        }
        super.onDestroyView();
    }

    @Override
    public void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");
        setUp();
    }

    @Override
    public void onServiceDisconnected() {
        Log.d(TAG, "onServiceDisconnected");
    }

    @Override
    public void onQueueChanged() {
        Log.d(TAG, "onQueueChanged");
//        updateQueue();
    }

    @Override
    public void onPlayingMetaChanged() {
        Log.d(TAG, "onPlayingMetaChanged");
        updatePlayingSongInfo();
        sendMessage(WHAT_RECYCLER_VIEW_SMOOTH_SCROLL_TO_CURRENT_POSITION);
    }

    @Override
    public void onPlayStateChanged() {
        Log.d(TAG, "onPlayStateChanged");
        updatePlayPauseState();
        mVisualSeekBar.postDelayed(mUpdateProgress, 10);
    }

    @Override
    public void onRepeatModeChanged() {
        Log.d(TAG, "onRepeatModeChanged");
        /*
        Unused
         */
    }

    @Override
    public void onShuffleModeChanged() {
        Log.d(TAG, "onShuffleModeChanged");
        /*
        Unused
         */
    }

    @Override
    public void onMediaStoreChanged() {
        Log.d(TAG, "onMediaStoreChanged");
//        updateQueue();
    }

    @Override
    public void onPaletteChanged() {

    }

//    private void updateQueue() {
//        mAdapter.setData(MusicPlayerRemote.getPlayingQueue());
//    }

    public void setUp() {
        if (getView() != null && isAdded() && !isRemoving() && !isDetached()) {
            updatePlayingSongInfo();
            updatePlayPauseState();
//            updateQueue();
            sendMessage(WHAT_RECYCLER_VIEW_SMOOTH_SCROLL_TO_CURRENT_POSITION);
        }
    }

    private void setCardRadius(float value) {
        if (mRoot != null) {
            float valueTemp;
            if (value > 1) valueTemp = 1;
            else if (value <= 0.1f) valueTemp = 0;
            else valueTemp = value;
            mRoot.setRadius(mMaxRadius * valueTemp);
        }
    }

    @Override
    public void onLayerUpdate(ArrayList<CardLayerController.CardLayerAttribute> attrs, ArrayList<Integer> actives, int me) {
        if (isAdded() && !isRemoving() && !isDetached()) {
            float translationPercent = attrs.get(actives.get(0)).getRuntimePercent();
            if (me == 1) {
                mDimView.setAlpha(0.3f * translationPercent);
                //  mRoot.setRoundNumber( attrs.get(actives.get(0)).getRuntimePercent(),true);
                setCardRadius(translationPercent);
            } else {
                //  other, active_i >1
                // min = 0.3
                // max = 0.45
                float min = 0.3f, max = 0.65f;
                float hieu = max - min;
                float heSo_sau = (me - 1.0f) / (me - 0.75f); // 1/2, 2/3,3/4, 4/5, 5/6 ...
                float heSo_truoc = (me - 2.0f) / (me - 0.75f); // 0/1, 1/2, 2/3, ...
                float darken = min + hieu * heSo_truoc + hieu * (heSo_sau - heSo_truoc) * translationPercent;
                // Log.d(TAG, "darken = " + darken);
                mDimView.setAlpha(darken);
                setCardRadius(1);
            }
        }
        //  checkStatusStyle();
    }

    @Override
    public void onLayerHeightChanged(CardLayerController.CardLayerAttribute attr) {
        //sendMessage(WHAT_CARD_LAYER_POSITION_CHANGED);
        handleLayerHeightChanged();
    }

    private void handleUpdateCardLayerRadius() {
        if (isFullscreenLayer()) {
            setCardRadius(0);
        } else {
            setCardRadius(mConstraintRoot.getProgress());
        }
    }

    private void handleLayerHeightChanged() {
        if (isAdded() && !isRemoving() && !isDetached()) {
            CardLayerController.CardLayerAttribute attribute = NowPlayingLayerFragment.this.getCardLayerController().getMyAttr(NowPlayingLayerFragment.this);
            if (attribute != null && isAdded() && !isRemoving() && !isDetached()) {
                float progress = attribute.getRuntimePercent();

                mConstraintRoot.setProgress(progress);
                // sync time text view
                if (progress != 0 && !mTimeTextIsSync) {
                    tvElipsedTime.setText(timeTextViewTemp);
                    mStartTime.setText(timeTextViewTemp);
                    tvTotalTime.setText(timeTextViewTemp1);
                    mStopTime.setText(timeTextViewTemp1);
                }

                sendMessage(WHAT_UPDATE_CARD_LAYER_RADIUS);
                sendMessage(WHAT_RECYCLER_VIEW_SMOOTH_SCROLL_TO_CURRENT_POSITION);
            }
        }
    }

    private void handleRecyclerViewScrollToCurrentPosition() {
        if (mConstraintRoot.getProgress() == 0 || mConstraintRoot.getProgress() == 1) {
            try {
                int position = MusicPlayerRemote.getPosition();
//                if (position >= 0) {
//                    mRecyclerView.scrollToPosition(position);
//                }
                //mViewPager.setCurrentItem(MusicPlayerRemote.getPosition());
            } catch (Exception ignore) {
            }
        }
    }

    private void postRunnable(Runnable runnable) {
        mNowPlayingHandler.removeCallbacks(runnable);
        mNowPlayingHandler.post(runnable);
    }

    private void sendMessage(int what) {
        mNowPlayingHandler.removeMessages(what);
        mNowPlayingHandler.sendEmptyMessage(what);
    }

    private void sendMessage(Message m) {
        mNowPlayingHandler.removeMessages(m.what);
        mNowPlayingHandler.sendMessage(m);
    }

    public void checkStatusStyle() {
        if (mConstraintRoot.getProgress() >= 0.9 && mDimView.getAlpha() <= 0.1
        ) {
            if (getActivity() instanceof AppActivity)
                ((AppActivity) getActivity()).setTheme(true);
        } else {
            if (getActivity() instanceof AppActivity)
                ((AppActivity) getActivity()).setTheme(false);
        }
    }

    @Override
    public int getLayerMinHeight(Context context, int h) {
        int systemInsetsBottom = 0;
        CardLayerController controller = getCardLayerController();
        Activity activity = controller != null ? controller.getActivity() : getActivity();
        if (activity instanceof AppActivity) {
            systemInsetsBottom = ((AppActivity) activity).getCurrentSystemInsets()[3];
        }
        Log.d(TAG, "activity availibility = " + (activity != null));
        Log.d(TAG, "systemInsetsBottom = " + systemInsetsBottom);
        return systemInsetsBottom + (int) (context.getResources().getDimension(R.dimen.bottom_navigation_height) + context.getResources().getDimension(R.dimen.now_laying_height_in_minimize_mode));
    }

    @Override
    public String getCardLayerTag() {
        return TAG;
    }

    @OnClick({R.id.play_pause_button, R.id.button_right})
    void playOrPause() {
        MusicPlayerRemote.playOrPause();
/*        Handler handler = new Handler();
        handler.postDelayed(MusicPlayerRemote::playOrPause,50);*/
    }

    @OnClick({R.id.prev_button, R.id.prev_button1})
    void goToPrevSong() {
        MusicPlayerRemote.back();
   /*     Handler handler = new Handler();
        handler.postDelayed(MusicPlayerRemote::back,50);*/
    }

    @OnClick({R.id.next_button, R.id.next_button1})
    void goToNextSong() {
        MusicPlayerRemote.playNextSong();
/*        Handler handler = new Handler();
        handler.postDelayed(MusicPlayer::next,100);*/
    }

    void updatePlayPauseState() {
        if (MusicPlayerRemote.isPlaying()) {
            mButtonRight.setImageResource(R.drawable.ic_paus_now);
            mPlayPauseButton.setImageResource(R.drawable.ic_paus_now);
        } else {
            mButtonRight.setImageResource(R.drawable.ic_play_now);
            mPlayPauseButton.setImageResource(R.drawable.ic_play_now);
        }
    }

//    @OnClick(R.id.playlist_title)
//    void popUpPlayingList() {
//        Activity activity = getActivity();
//        if (activity instanceof AppActivity) {
//            ((AppActivity) getActivity()).popUpPlaylistTab();
//        }
//    }

    private void updatePlayingSongInfo() {
        boolean exist=false;
        Song song = MusicPlayerRemote.getCurrentSong();
        if (song == null || song.id == -1) {
            ArrayList<Song> list = SongLoader.getAllSongs(mPlayPauseButton.getContext(), SortOrder.SongSortOrder.SONG_DATE);
            if (list.isEmpty()) return;
            MusicPlayerRemote.openQueue(list, 0, false);
            return;
        }

        List<Song> helperList = new ArrayList<>();
        helperList=SharedPrefrences.getFavouriteList(getContext());
        if(helperList!=null && helperList.size()>0){
            for (int i = 0; i < helperList.size(); i++) {
                Song song1=helperList.get(i);
                if (song1.id==song.id) {
                    exist = true;
                    break;
                } else {
                    exist = false;
                }
            }
        }
        if (exist) {
            mfav.setImageDrawable(getResources().getDrawable(R.drawable.color_heart));
        } else {
            mfav.setImageDrawable(getResources().getDrawable(R.drawable.black_heart));
        }

        mTitle.setText(song.title);
        mAlbum.setText(song.artistName);
        mBigTitle.setText(song.title);
        mBigArtist.setText(song.artistName);

        Artist artist = ArtistLoader.getArtist(getContext(),song.artistId);
        int[] screen = Tool.getScreenSize(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mImage.setClipToOutline(true);
        }

        Glide.with(getContext())
                .load(Util.getAlbumArtUri(song.albumId))
                .override(screen[1])
//                .placeholder(R.drawable.default_image_round)
                .error(
                        ArtistGlideRequest.Builder.from(Glide.with(getContext()), artist).tryToLoadOriginal(true).whichImage(1).generateBuilder(getContext()).buildRequestDrawable()
                                .error(ArtistGlideRequest.Builder.from(Glide.with(getContext()),artist).tryToLoadOriginal(false).whichImage(1).generateBuilder(getContext()).buildRequestDrawable().error(R.drawable.sample)
                                ))
                .into(mImage);

        String path = song.data;
        long duration = song.duration;
        if (duration > 0 && path != null && !path.isEmpty() && mVisualSeekBar.getCurrentSongID() != song.id) {
            Log.d(TAG, "start visualize " + path + "dur = " + duration + ", pos = " + MusicPlayerRemote.getSongProgressMillis());
            mVisualSeekBar.visualize(song, duration, MusicPlayerRemote.getSongProgressMillis());
        } else {
            Log.d(TAG, "ignore visualize " + path);
        }
        mSeekbar.setMax((int) duration);

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MusicPlayerRemote.seekTo(seekBar.getProgress());
            }
        });

        mVisualSeekBar.postDelayed(mUpdateProgress, 10);
        if (getActivity() instanceof MusicServiceActivity)
            ((MusicServiceActivity) getActivity()).refreshPalette();
    }

    private void onColorPaletteReady(int color1, int color2, float alpha1, float alpha2) {
        Log.d(TAG, "onColorPaletteReady :" + color1 + ", " + color2 + ", " + alpha1 + ", " + alpha2);
       /* mPrevButton.setColorFilter(color2);
        mNextButton.setColorFilter(color2);
*/
        //  mTimeTextView.setTextColor(color1);
        //   (mTimeTextView.getBackground()).setColorFilter(color1, PorterDuff.Mode.SRC_IN);

//        mBigTitle.setTextColor(Tool.lighter(color1, 0.5f));
        // mBigArtist.setAlpha(alpha2);
//        mBigArtist.setTextColor(color2);
        mVisualSeekBar.updateDrawProperties();

    }

    ////////////////////////////////
    /// VISUAL SEEK BAR IMPLEMENTED
    ////////////////////////////////

    private void addAnimationOperations() {
        final boolean[] set = {false};
        ConstraintSet constraint1 = new ConstraintSet();
        constraint1.clone(mConstraintRoot);

        ConstraintSet constraint2 = new ConstraintSet();
        constraint2.clone(getContext(), R.layout.screen_now_playing_expanded);

        mButtonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(mConstraintRoot);
                ConstraintSet constraintSet = (set[0]) ? constraint1 : constraint2;
                constraintSet.applyTo(mConstraintRoot);
                set[0] = !set[0];
            }
        });

    }

    @Override
    public boolean onGestureDetected(int gesture) {
        if (gesture == CardLayerController.SINGLE_TAP_UP) {
            CardLayerController.CardLayerAttribute a = getCardLayerController().getMyAttr(this);
            if (a != null) {
                if (a.getState() == CardLayerController.CardLayerAttribute.MINIMIZED)
                    a.animateToMax();
                else
                    a.animateToMin();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFullscreenLayer() {
        return false;
    }

    @Override
    public void onColorChanged(int position, int newColor) {
//        mBigTitle.setTextColor(newColor);
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        fragmentPaused = false;
        if (mVisualSeekBar != null) {
            mVisualSeekBar.postDelayed(mUpdateProgress, 10);
        }
    }

    @Override
    public void onSeekBarSeekTo(AudioVisualSeekBar seekBar, int position) {
        MusicPlayerRemote.seekTo(position);
    }

    @Override
    public void onSeekBarTouchDown(AudioVisualSeekBar seekBar) {
        isTouchedVisualSeekbar = true;
    }

    @Override
    public void onSeekBarTouchUp(AudioVisualSeekBar seekBar) {
        isTouchedVisualSeekbar = false;
    }

    @Override
    public void onSeekBarSeeking(int seekingValue) {
        /*
        int distance = Math.abs(MusicPlayerRemote.getSongProgressMillis() - seekingValue);
        if (distance > 2000) {
            MusicPlayerRemote.seekTo(seekingValue);
        }
        */
        setTextTime(seekingValue, MusicPlayerRemote.getSongDurationMillis());
    }

    private void setTextTime(long pos, long duration) {
        int minute = (int) (pos / 1000 / 60);
        int second = (int) (pos / 1000 - minute * 60);
        int dur_minute = (int) (duration / 1000 / 60);
        int dur_second = (int) (duration / 1000 - dur_minute * 60);

        String text = "";
        String text1 = "";
        if (minute < 10) text += "0";
        text += minute + ":";
        if (second < 10) text += "0";
        text += second;
        if (dur_minute < 10) text1 += "0";
        text1 += dur_minute + ":";
        if (dur_second < 10) text1 += "0";
        text1 += dur_second;

        if (mConstraintRoot.getProgress() != 0) {
            tvElipsedTime.setText(text);
            mStartTime.setText(text);
            tvTotalTime.setText(text1);
            mStopTime.setText(text1);
            //Log.d(TAG, "setTextTime: "+text);
            mTimeTextIsSync = true;
        } else {
            mTimeTextIsSync = false;
            timeTextViewTemp = text;
            timeTextViewTemp1 = text1;
        }
    }

    private static class CustomDecoration extends RecyclerView.ItemDecoration {
        public CustomDecoration() {
            super();
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
        }
    }
}
