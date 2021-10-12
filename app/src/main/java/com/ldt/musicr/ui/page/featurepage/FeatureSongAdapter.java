package com.ldt.musicr.ui.page.featurepage;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ldt.musicr.R;
import com.ldt.musicr.contract.AbsBindAbleHolder;
import com.ldt.musicr.contract.AbsSongAdapter;
import com.ldt.musicr.helper.menu.SongMenuHelper;
import com.ldt.musicr.helper.songpreview.PreviewSong;
import com.ldt.musicr.helper.songpreview.SongPreviewController;
import com.ldt.musicr.helper.songpreview.SongPreviewListener;
import com.ldt.musicr.service.MusicPlayerRemote;
import com.ldt.musicr.ui.AppActivity;
import com.ldt.musicr.ui.bottomsheet.OptionBottomSheet;
import com.ldt.musicr.model.Song;

import com.ldt.musicr.ui.page.searchPage.onClickInterface;
import com.ldt.musicr.ui.widget.CircularPlayPauseProgressBar;
import com.ldt.musicr.util.Util;
import com.ldt.musicr.util.Tool;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.ldt.musicr.contract.AbsMediaAdapter.PALETTE_CHANGED;
import static com.ldt.musicr.contract.AbsMediaAdapter.PLAY_STATE_CHANGED;
import static com.ldt.musicr.contract.AbsMediaAdapter.SONG_PREVIEW_CHANGED;


public class FeatureSongAdapter extends RecyclerView.Adapter<FeatureSongAdapter.ItemHolder> implements SongPreviewListener {
    private static final String TAG = "SongAdapter";
    public ArrayList<Song> mAllSongs = new ArrayList<>();
    public ArrayList<Song>  mData = new ArrayList<>();
    public int currentlyPlayingPosition = 0;

    public Context getContext() {
        return mContext;
    }

    public void init(Context context) {
        mContext = context;
    }

    private Context mContext;
    private long[] songIDs;
    private boolean isPlaylist;
    private boolean animate;
    private int lastPosition = -1;
    private long playlistId;
    onClickInterface onClickInterface;
    private PreviewSong mPreviewSong = null;

    public void feature(onClickInterface onClickInterface) {
        this.onClickInterface = onClickInterface;
    }

    public void setData(List<Song> data) {
        mAllSongs.clear();
        if (data != null) mAllSongs.addAll(data);
        initializeSong();
        if (getContext() instanceof AppActivity) {
            SongPreviewController controller = ((AppActivity) getContext()).getSongPreviewController();
            if (controller != null)
                mPreviewSong = controller.getCurrentPreviewSong();
        }
    }

    protected int mMediaPlayDataItem = -1;
    boolean isMediaPlayItemAvailable() {
        return -1 < mMediaPlayDataItem && mMediaPlayDataItem < mData.size();
    }

    public void notifyOnMediaStateChanged(final String whichChanged) {
        if (whichChanged.equals(PLAY_STATE_CHANGED)) {
            Song currentPlayingSong = MusicPlayerRemote.getCurrentSong();
            if (currentPlayingSong != null && currentPlayingSong.id != -1) {

                if (isMediaPlayItemAvailable()) {

                    notifyItemChanged(currentlyPlayingPosition, PLAY_STATE_CHANGED);

                    boolean isStillOldPos = mData.get(mMediaPlayDataItem).equals(currentPlayingSong);
                    if (!isStillOldPos) {
                        mMediaPlayDataItem = mData.indexOf(currentPlayingSong);
                        if (mMediaPlayDataItem != -1)
                            notifyItemChanged(currentlyPlayingPosition, PLAY_STATE_CHANGED);
                    }
                }
            }
        } else if (whichChanged.equals(PALETTE_CHANGED)) {
            notifyItemRangeChanged(0, getItemCount(), PALETTE_CHANGED);
        }
    }

    public void destroy() {
        mContext = null;
    }

    public void updateSearchResults(ArrayList<Song> searchResults) {
        mAllSongs.clear();
        if (searchResults != null) {
            mAllSongs.addAll(searchResults);
        }
        initializeSong();
    }

    public void initializeSong() {
        mData.clear();

        Collections.shuffle(mAllSongs);

        int size = mAllSongs.size();
        for (int i = 0; i < size; i++)
            mData.add(mAllSongs.get(i));

        this.songIDs = getSongIds();

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public int getAllItemCount() {
        return mAllSongs.size();
    }

    public long[] getSongIds() {
        long[] ret = new long[mAllSongs.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = mAllSongs.get(i).id;
        }

        return ret;
    }

    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song_normal, viewGroup, false);
        return new ItemHolder(v);
    }

//

    @Override
    public void onBindViewHolder(@NotNull ItemHolder itemHolder, int i) {
        itemHolder.bind(mData.get(i));
        itemHolder.bindPreviewButton(mData.get(i));
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder itemHolder, int i, @NonNull List<Object> payloads) {

        if ( !payloads.isEmpty())
            for (Object object : payloads) {
                Log.d(TAG, "onBindViewHolder: object " + object);
                if (object instanceof String) {
                    switch ((String) object) {
                        case PLAY_STATE_CHANGED:
                            Log.d(TAG, " onBindViewHolder: " + PLAY_STATE_CHANGED);
                            itemHolder.bind(mData.get(i));
                            break;
                        case SONG_PREVIEW_CHANGED:
                            Log.d(TAG,  " onBindViewHolder: " + SONG_PREVIEW_CHANGED);
                            itemHolder.bindPreviewButton(mData.get(i));
                            break;
                        case PALETTE_CHANGED:
                            Log.d(TAG, " onBindViewHolder: " + PALETTE_CHANGED);
                            itemHolder.bind(mData.get(i));
                            break;
                        default:
                            super.onBindViewHolder(itemHolder, i, payloads);
                    }
                } else super.onBindViewHolder(itemHolder, i, payloads);
            }
        else {
            super.onBindViewHolder(itemHolder, i, payloads);
        }
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {
        itemHolder.mMenuButton.setOnClickListener(v -> {
            OptionBottomSheet
                    .newInstance(SongMenuHelper.SONG_OPTION, mData.get(position))
                    .show(((AppCompatActivity) mContext).getSupportFragmentManager(), "song_popup_menu");
        });
    }

    @Override
    public void onSongPreviewStart(PreviewSong song) {
        int position = mData.indexOf(song.getSong());

        Log.d(TAG, "onSongPreviewStart: position = " + position);
        if (position != -1) {
            mPreviewSong = song;
            notifyItemChanged(position);
        }
    }

    @Override
    public void onSongPreviewFinish(PreviewSong song) {
        mPreviewSong = null;
        int position = mData.indexOf(song.getSong());
        Log.d(TAG, "onSongPreviewFinish: position = " + position);

        if (position != -1)
            notifyItemChanged(position);
    }


    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.title)
        TextView mTitle;
        @BindView(R.id.description)
        TextView mDescription;
        @BindView(R.id.image)
        ImageView mImage;
        @BindView(R.id.menu_button)
        View mMenuButton;
        @BindView(R.id.quick_play_pause)
        ImageView mQuickPlayPause;
        @BindView(R.id.imgRing)
        ImageView mImageRing;
        @BindView(R.id.duration)
        TextView mDuration;
        @BindView(R.id.mImageProgress)
        ProgressBar mImageProgress;
        @BindView(R.id.preview_button)
        CircularPlayPauseProgressBar mPreviewButton;

        private void resetProgress() {
            mPreviewButton.resetProgress();
        }

        @OnClick(R.id.quick_play_pause)
        void clickQuickPlayPause() {
            if (MusicPlayerRemote.getCurrentSong().id == mData.get(getAdapterPosition()).id) {
                MusicPlayerRemote.playOrPause();
                checkQuickPlayPause();
            } else onClick(itemView);
        }

        @OnClick(R.id.preview_button)
        void clickPresent() {

            if (mPreviewSong != null && mPreviewSong.getSong().equals(mData.get(getAdapterPosition()))) {
                resetProgress();
                forceStopPreview();
            } else {
                previewThisSong(getAdapterPosition());
            }
        }

        protected void previewThisSong(int positionData) {

            if (getContext() instanceof AppActivity) {
                SongPreviewController preview = ((AppActivity) getContext()).getSongPreviewController();
                if (preview != null) {
                    if (preview.isPlayingPreview() && preview.isPreviewingSong(mData.get(positionData)))
                        preview.cancelPreview();
                    else {
                        preview.previewSongs(mData.get(positionData));
                    }
                }
            }
        }

        private void forceStopPreview() {
            mPreviewSong = null;
            if (getContext() instanceof AppActivity) {
                SongPreviewController controller = ((AppActivity) getContext()).getSongPreviewController();
                if (controller != null) controller.cancelPreview();
            }
        }

        public ItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                MusicPlayerRemote.openQueue(mAllSongs, getAdapterPosition(), true);
                Handler handler1 = new Handler();
                handler1.postDelayed(() -> {
                    notifyItemChanged(currentlyPlayingPosition, PLAY_STATE_CHANGED);
                    notifyItemChanged(getAdapterPosition(), PLAY_STATE_CHANGED);
//                    notifyItemChanged(currentlyPlayingPosition);
//                    notifyItemChanged(getAdapterPosition());
                    currentlyPlayingPosition = getAdapterPosition();
                }, 50);
            }, 100);
        }

        public void bind(Song song) {

            mTitle.setText(song.title);
            mDescription.setText(song.artistName);
            long duration = song.duration;
            int dur_minute = (int) (duration / 1000 / 60);
            int dur_second = (int) (duration / 1000 - dur_minute * 60);

            String text1 = "";
            if (dur_minute < 10) text1 += "0";
            text1 += dur_minute + ":";
            if (dur_second < 10) text1 += "0";
            text1 += dur_second;
            mDuration.setText(text1);
            mImageProgress.setMax((int) song.duration);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mImage.setClipToOutline(true);
                ((RippleDrawable) mMenuButton.getBackground()).setColor(ColorStateList.valueOf(mContext.getResources().getColor(R.color.rama)));
                ((RippleDrawable) mPreviewButton.getBackground()).setColor(ColorStateList.valueOf(mContext.getResources().getColor(R.color.rama)));
            }

            Glide.with(mContext).load(Util.getAlbumArtUri(song.albumId))
                    .placeholder(R.drawable.default_image_round)
                    .error(R.drawable.default_image_round)
                    .into(mImage);
            setOnPopupMenuListener(this, getAdapterPosition());
            checkQuickPlayPause();
//            bindPreviewButton(song);
        }

        private void bindPreviewButton(Song song) {
            if ((mPreviewSong == null || !mPreviewSong.getSong().equals(song))
                    && mPreviewButton.getMode() == CircularPlayPauseProgressBar.PLAYING)
                mPreviewButton.resetProgress();
            else if (mPreviewSong != null && mPreviewSong.getSong().equals(song)) {
                long timePlayed = mPreviewSong.getTimePlayed();
                //    Log.d(TAG, "bindPreviewButton: timePlayed = " + timePlayed);
                if (timePlayed == -1) mPreviewButton.resetProgress();
                else {
                    if (timePlayed < 0) timePlayed = 0;
                    if (timePlayed <= mPreviewSong.getTotalPreviewDuration())
                        mPreviewButton.syncProgress(mPreviewSong.getTotalPreviewDuration(), (int) timePlayed);
                }
            }
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                while (true) {
                    try {
                        long position = MusicPlayerRemote.getSongProgressMillis();
                        mImageProgress.setProgress((int) position);
                        Thread.sleep(1000);
                    } catch (Exception exception) {
                    }
                }
            }
        };

        public void checkQuickPlayPause() {
            if (MusicPlayerRemote.getCurrentSong().id == mData.get(getAdapterPosition()).id) {
                currentlyPlayingPosition = getAdapterPosition();
                mImageProgress.setVisibility(View.VISIBLE);
                mImageRing.setVisibility(View.GONE);
                mTitle.setTextColor(getContext().getResources().getColor(R.color.s3));
                mDescription.setTextColor(getContext().getResources().getColor(R.color.backward_color1));

                if (MusicPlayerRemote.isPlaying()) {
                    if (thread != null) {
                        if (!thread.isAlive()) {
                            thread.start();
                        }
                    }
                    mQuickPlayPause.setImageResource(R.drawable.quick_pause);
                } else {
                    if (thread != null) {
                        if (thread.isAlive())
                            thread.interrupt();
                    }
                    mQuickPlayPause.setImageResource(R.drawable.quick_play);
                }
            } else {
                mImageProgress.setVisibility(View.GONE);
                mImageRing.setVisibility(View.VISIBLE);
                mQuickPlayPause.setImageDrawable(null);
                mTitle.setTextColor(mQuickPlayPause.getResources().getColor(R.color.FlatWhite));
                mDescription.setTextColor(getContext().getResources().getColor(R.color.backward_color1));
            }
        }
    }


    public Song getSongAt(int i) {
        return mData.get(i);
    }

    public void addSongTo(int i, Song song) {
        mData.add(i, song);
    }

    public void removeSongAt(int i) {
        mData.remove(i);
    }

}
