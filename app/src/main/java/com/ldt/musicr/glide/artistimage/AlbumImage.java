package com.ldt.musicr.glide.artistimage;

public class AlbumImage {
    public static final int FIRST = 0;
    public static final int RANDOM = -1;
    public static final int LOCAL = -2;

    public final String mAlbumName;
    public final boolean mSkipOkHttpCache;
    public final boolean mLoadOriginal;
    public final int mImageNumber;

    public AlbumImage(String albumName, boolean skipOkHttpCache, boolean loadOriginal, int imageNumber) {
        this.mAlbumName = albumName;
        this.mSkipOkHttpCache = skipOkHttpCache;
        this.mLoadOriginal = loadOriginal;
        this.mImageNumber = imageNumber;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public boolean isSkipOkHttpCache() {
        return mSkipOkHttpCache;
    }
}

