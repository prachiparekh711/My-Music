package com.ldt.musicr.ui.page.librarypage.song;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.ldt.musicr.R;
import com.ldt.musicr.contract.AbsBindAbleHolder;
import com.ldt.musicr.contract.AbsSongAdapter;

import com.ldt.musicr.helper.menu.MenuHelper;
import com.ldt.musicr.helper.menu.SongMenuHelper;
import com.ldt.musicr.model.Playlist;
import com.ldt.musicr.service.MusicPlayerRemote;
import com.ldt.musicr.service.MusicService;
import com.ldt.musicr.ui.bottomsheet.OptionBottomSheet;
import com.ldt.musicr.ui.bottomsheet.SortOrderBottomSheet;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SongChildAdapter extends AbsSongAdapter
        implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter,
        SortOrderBottomSheet.SortOrderChangedListener {

    private static final String TAG = "SongChildAdapter";
    String mFrom = "";
    Playlist playlist;

    public SongChildAdapter() {
        super();
    }

    public SongChildAdapter(String mFrom) {
        this.mFrom = mFrom;
    }

    public int mRandomItem = 0;
    private Random mRandom = new Random();

    @Override
    protected void onDataSet() {
        super.onDataSet();
        randomize();
    }

    public void destroy() {
        removeCallBack();
        removeOrderListener();
        super.destroy();
    }

    public void setSongOptionHelperRes(final int[] res) {
        mOptionRes = res;
    }

    private int[] mOptionRes = SongMenuHelper.SONG_OPTION;
    private int[] mOptionRes1 = SongMenuHelper.PL_SONG_OPTION;

    @Override
    protected void onMenuItemClick(int positionInData) {
        if (mFrom.equals("pl")) {
            OptionBottomSheet
                    .newInstance(mOptionRes1, getData().get(positionInData))
                    .show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "song_popup_menu");
        }else {
            OptionBottomSheet
                    .newInstance(mOptionRes, getData().get(positionInData))
                    .show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "song_popup_menu");
        }
    }

    public int MEDIA_LAYOUT_RESOURCE = R.layout.item_song_normal;

    @Override
    public int getItemViewType(int position) {

        if (position == 0) return R.layout.item_sort_song_child;
        return MEDIA_LAYOUT_RESOURCE;

    }

    @Override
    protected int getMediaHolderPosition(int dataPosition) {
        return dataPosition + 1;
    }

    @Override
    protected int getDataPosition(int itemHolderPosition) {
        return itemHolderPosition - 1;
    }

    @Override
    public int getItemCount() {
        return getData().size() + 1;
    }

    @Override
    public int getSavedOrder() {
        if (mSortOrderListener != null)
            return mSortOrderListener.getSavedOrder();
        return 0;
    }

    @Override
    public void onOrderChanged(int newType, String name) {
        if (mSortOrderListener != null) {
            mSortOrderListener.onOrderChanged(newType, name);
        }
    }

    private SortOrderBottomSheet.SortOrderChangedListener mSortOrderListener;

    public void setSortOrderChangedListener(SortOrderBottomSheet.SortOrderChangedListener listener) {
        mSortOrderListener = listener;
    }

    public void removeOrderListener() {
        mSortOrderListener = null;
    }

    private void sortHolderClicked() {
        if (getContext() instanceof AppCompatActivity) {
            SortOrderBottomSheet bs = SortOrderBottomSheet.newInstance(this);
            bs.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), TAG);
        }
    }


    @NotNull
    @Override
    public AbsBindAbleHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(viewType, viewGroup, false);


        if (viewType == R.layout.item_sort_song_child)
            return new SortHolder(v);


        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NotNull AbsBindAbleHolder itemHolder, int position) {
        if (itemHolder instanceof ItemHolder)
            ((ItemHolder) itemHolder).bind(getData().get(getDataPosition(position)));
        else {
            ((SortHolder) itemHolder).bind(null);
        }
    }


    public void randomize() {
        if (getData().isEmpty()) return;
        mRandomItem = mRandom.nextInt(getData().size());
        if (mCallBack != null) mCallBack.onFirstItemCreated(getData().get(mRandomItem));
    }

    public SongChildAdapter setCallBack(PreviewRandomPlayAdapter.FirstItemCallBack callBack) {
        mCallBack = callBack;
        return this;
    }

    public void removeCallBack() {
        mCallBack = null;
    }

    private PreviewRandomPlayAdapter.FirstItemCallBack mCallBack;

    public void shuffle() {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            MusicPlayerRemote.setShuffleMode(MusicService.SHUFFLE_MODE_SHUFFLE);
            MusicPlayerRemote.openQueue(getData(), mRandomItem, true);

//            MusicPlayer.playAll(mContext, mSongIDs, mRandomItem, -1, Util.IdType.NA, false);
            Handler handler1 = new Handler();
            handler1.postDelayed(() -> {
                notifyItemChanged(getMediaHolderPosition(mMediaPlayDataItem));
                notifyItemChanged(getMediaHolderPosition(mRandomItem));
                mMediaPlayDataItem = mRandomItem;
                randomize();
            }, 50);
        }, 100);
    }


    @NonNull
    @Override
    public String getSectionName(int position) {
        if (position == 0) return "A";
        if (getData().get(position - 1).title.isEmpty())
            return "A";
        return getData().get(position - 1).title.substring(0, 1);
    }

    public int getViewTypeHeight(RecyclerView recyclerView, @Nullable RecyclerView.ViewHolder viewHolder, int viewType) {

        if (viewType == R.layout.item_sort_song_child) {

            return recyclerView.getResources().getDimensionPixelSize(R.dimen.item_sort_song_child_height);

        } else if (viewType == R.layout.item_song_normal) {
            return recyclerView.getResources().getDimensionPixelSize(R.dimen.item_song_child_height);
        }

        return 0;
    }

    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, int i) {
        return recyclerView.getResources().getDimensionPixelSize(R.dimen.item_song_child_height);
    }

    public class SortHolder extends AbsBindAbleHolder {
        @BindView(R.id.sort_text)
        TextView mSortText;
        @BindView(R.id.sort_parent)
        ConstraintLayout mSort_parent;

        @OnClick(R.id.sort_parent)
        void sortClicked() {

            sortHolderClicked();
        }

        public SortHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            if (mFrom.equals("pl")) {
                mSort_parent.setVisibility(View.GONE);
            }
        }

        @Override
        public void bind(Object object) {
            if (mSortOrderListener != null) {
                String str = getContext().getResources().getString(
                        SortOrderBottomSheet.mSortStringRes[mSortOrderListener.getSavedOrder()]);
                mSortText.setText(str);
            }
        }
    }

    public class ItemHolder extends AbsSongAdapter.SongHolder {
        public ItemHolder(View view) {
            super(view);
        }
    }
}
