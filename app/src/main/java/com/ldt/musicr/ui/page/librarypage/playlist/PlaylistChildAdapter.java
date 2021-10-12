package com.ldt.musicr.ui.page.librarypage.playlist;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.AsyncTask;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;


import com.ldt.musicr.helper.EventListener;
import com.ldt.musicr.helper.Reliable;
import com.ldt.musicr.helper.ReliableEvent;
import com.ldt.musicr.helper.menu.MenuHelper;
import com.ldt.musicr.loader.medialoader.TopAndRecentlyPlayedTracksLoader;
import com.ldt.musicr.ui.bottomsheet.OptionBottomSheet;
import com.ldt.musicr.ui.page.featurepage.FeaturePlaylistAdapter;
import com.ldt.musicr.ui.page.subpages.singleplaylist.SinglePlaylistViewModel;
import com.ldt.musicr.util.AutoGeneratedPlaylistBitmap;
import com.ldt.musicr.ui.widget.BounceInterpolator;
import com.ldt.musicr.util.Tool;
import com.ldt.musicr.R;
import com.ldt.musicr.loader.medialoader.LastAddedLoader;
import com.ldt.musicr.loader.medialoader.PlaylistSongLoader;

import com.ldt.musicr.model.Playlist;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.util.Util;
import com.makeramen.roundedimageview.RoundedDrawable;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaylistChildAdapter extends RecyclerView.Adapter<PlaylistChildAdapter.ItemHolder> {
    private static final String TAG = "PlaylistAdapter";
    public ArrayList<Playlist> mPlaylistData = new ArrayList<>();
    public FeaturePlaylistAdapter.PlaylistClickListener mListener;

    public void init(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    private Context mContext;

    public void setShowAuto(boolean showAuto) {
        this.showAuto = showAuto;
    }

    private boolean showAuto;
    private int songCountInt;
    private long firstAlbumID=-1;


    public void setOnItemClickListener(FeaturePlaylistAdapter.PlaylistClickListener listener) {
        mListener = listener;
    }

    public void unBindAdapter() {
        mListener = null;
        mContext = null;
    }
    public void setData(List<Playlist> data) {
        Log.d(TAG, "setData: count = " + data.size());
        mPlaylistData.clear();
        if(data!=null) {
            mPlaylistData.addAll(data);
            notifyDataSetChanged();

        }
    }

    public void addData(ArrayList<Playlist> data) {
        if(data!=null) {
            int posBefore = mPlaylistData.size();
            mPlaylistData.addAll(data);
            notifyItemRangeInserted(posBefore,data.size());
        }
    }

    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_playlist_child, viewGroup,false);
        return new ItemHolder(v);
    }
    @Override
    public void onBindViewHolder(@NotNull final ItemHolder itemHolder, int i) {
        final Playlist playlist = mPlaylistData.get(i);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            itemHolder.mImage.setClipToOutline(true);
        }

        if(playlist.id==-1){
            itemHolder.mImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic__added));
        }else if(playlist.id==-2){

            itemHolder.mImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic__played));
        }else if(playlist.id==-3){
            itemHolder.mImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_image_round));
        }else{
            itemHolder.mImage.setPadding(0,0,0,0);
            new PlaylistBitmapLoader(this,playlist,itemHolder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        itemHolder.mImage.setTag(firstAlbumID);
        itemHolder.mTitle.setText(playlist.name);
        if(Util.isLollipop()) itemHolder.mImage.setTransitionName("transition_album_art"+i);

        itemHolder.menu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Playlist playlist = mPlaylistData != null ? mPlaylistData.get(i) : null;
                if (playlist != null) {
                    int[] optionIds = playlist.id < 0 ? MenuHelper.AUTO_GEN_PLAYLIST_OPTION : MenuHelper.USER_PLAYLIST_OPTION;
                    OptionBottomSheet.newInstance(optionIds, playlist).show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "playlist_option_menu");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return  mPlaylistData.size();
    }

    public List<Song> getPlaylistWithListId(int position, int id) {
        if(mContext!=null) {
            firstAlbumID = -1;
            if(showAuto) {
                switch (position) {
                    case 0: return  LastAddedLoader.getLastAddedSongs(mContext);
                    case 1:
                        return TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(mContext);
                    case 2:
                        return TopAndRecentlyPlayedTracksLoader.getTopTracks(mContext);
                    default:
                        return new ArrayList<>(PlaylistSongLoader.getPlaylistSongList(mContext, id));
                }
            } else PlaylistSongLoader.getPlaylistSongList(mContext, id);
        }
        return null;
    }


    private EventListener<SinglePlaylistViewModel.State> mEventListener;

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnTouchListener{
        @BindView(R.id.title) TextView mTitle;
        @BindView(R.id.image) ImageView mImage;
        @BindView(R.id.over) View view_over;
        @BindView(R.id.menu_button) ImageView menu_button;
        int currentColor=0;
        ItemHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
            mImage.setOnClickListener(this);
            view_over.setOnTouchListener(this);


        }
        @Override
        public void onClick(View v) {
            //Todo: Navigate to playlist detail
            final android.view.animation.Animation myAnim = AnimationUtils.loadAnimation(mContext, R.anim.bounce_slow);
            BounceInterpolator interpolator = new BounceInterpolator(0.1, 30);
            myAnim.setInterpolator(interpolator);
            myAnim.setDuration(350);
            itemView.startAnimation(myAnim);

            if(mListener!=null) {
                Bitmap bitmap = null;
                Drawable d = mImage.getDrawable();
                if(d instanceof BitmapDrawable) bitmap = ((BitmapDrawable)d).getBitmap();
                else if(d instanceof RoundedDrawable) bitmap = ((RoundedDrawable)d).getSourceBitmap();
                mListener.onClickPlaylist(mPlaylistData.get(getAdapterPosition()), bitmap);
            }
               //itemView.startAnimation(myAnim);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(currentColor!=Tool.getBaseColor()) {
                currentColor = Tool.getBaseColor();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((RippleDrawable) view_over.getBackground()).setColor(ColorStateList.valueOf(Tool.getBaseColor()));
                }
            }
            return false;
        }
    }


    private static class PlaylistBitmapLoader extends AsyncTask<Void,Void,Bitmap> {
        private final WeakReference<PlaylistChildAdapter>mWeakAdapter;
        private final WeakReference<ItemHolder> mWeakItemHolder;
        private Playlist mPlaylist;

        PlaylistBitmapLoader(PlaylistChildAdapter adapter, Playlist playlist, ItemHolder item) {
            mWeakAdapter = new WeakReference<>(adapter);
            mWeakItemHolder = new WeakReference<>(item);
            mPlaylist = playlist;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ItemHolder itemHolder = mWeakItemHolder.get();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemHolder.mImage.setClipToOutline(true);
            }
            if(itemHolder!=null)
            itemHolder.mImage.setImageBitmap(bitmap);
        }

        @Override
        protected Bitmap doInBackground(Void... v) {
            PlaylistChildAdapter adapter = mWeakAdapter.get();
            ItemHolder itemHolder = mWeakItemHolder.get();
            if (adapter != null && itemHolder != null) {
                List<Song> l = adapter.getPlaylistWithListId(itemHolder.getAdapterPosition(), mPlaylist.id);
                return AutoGeneratedPlaylistBitmap.getBitmap(adapter.mContext, l, false, false);
            } else return null;
        }
    }

}