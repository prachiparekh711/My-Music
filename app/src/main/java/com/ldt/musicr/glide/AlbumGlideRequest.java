package com.ldt.musicr.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.ldt.musicr.App;
import com.ldt.musicr.glide.artistimage.AlbumImage;
import com.ldt.musicr.model.Album;
import com.ldt.musicr.util.ArtistSignatureUtil;
import com.ldt.musicr.util.CustomAlbumImageUtil;

import static com.ldt.musicr.glide.artistimage.ArtistImage.FIRST;

public class AlbumGlideRequest {
    public static final int DEFAULT_ANIMATION = android.R.anim.fade_in;
    private static final DiskCacheStrategy DEFAULT_DISK_CACHE_STRATEGY = DiskCacheStrategy.AUTOMATIC;

    public static RequestBuilder<Drawable> createBaseRequestForDrawable(RequestManager requestManager, Album album, boolean noCustomImage, boolean forceDownload, boolean loadOriginal, int imageNumber) {
        RequestBuilder<Drawable> builder;
        boolean hasCustomImage = CustomAlbumImageUtil.getInstance(App.getInstance()).hasCustomAlbumImage(album);
        if (noCustomImage || !hasCustomImage) {
            builder = requestManager.load(new AlbumImage(album.getTitle(), forceDownload, loadOriginal, imageNumber));
        } else {
            builder = requestManager.load(CustomAlbumImageUtil.getFile(album));
        }
        return builder;
    }

    public static RequestBuilder<Bitmap> createBaseRequest(RequestManager requestManager, Album album, boolean noCustomImage, boolean forceDownload, boolean loadOriginal, int imageNumber) {
        RequestBuilder<Bitmap> builder;
        boolean hasCustomImage = CustomAlbumImageUtil.getInstance(App.getInstance()).hasCustomAlbumImage(album);
        if (noCustomImage || !hasCustomImage) {
            builder = requestManager.asBitmap().load(new AlbumImage(album.getTitle(), forceDownload, loadOriginal, imageNumber));
        } else {
            builder = requestManager.asBitmap().load(CustomAlbumImageUtil.getFile(album));
        }
        return builder;
    }

    public static Key createSignature(Album album, boolean isLoadOriginal, int whichImage) {
        return ArtistSignatureUtil.getInstance(App.getInstance()).getArtistSignature(album.getTitle(), isLoadOriginal, whichImage);
    }

    public static class Builder {
        final RequestManager requestManager;
        final Album album;
        boolean noCustomImage = false;
        boolean forceDownload;
        boolean mLoadOriginalImage = false;
        int mImageNumber = FIRST;

        private Builder(@NonNull RequestManager requestManager, Album album) {
            this.requestManager = requestManager;
            this.album = album;
        }

        public static Builder from(@NonNull RequestManager requestManager, Album album) {
            return new Builder(requestManager, album);
        }

        public PaletteBuilder generateBuilder(Context context) {
            return new PaletteBuilder(this, context);
        }

        public BitmapBuilder asBitmap() {
            return new BitmapBuilder(this);
        }

        public Builder noCustomImage(boolean noCustomImage) {
            this.noCustomImage = noCustomImage;
            return this;
        }

        public Builder forceDownload(boolean forceDownload) {
            this.forceDownload = forceDownload;
            return this;
        }

        public Builder tryToLoadOriginal(boolean b) {
            this.mLoadOriginalImage = b;
            return this;
        }

        public Builder whichImage(final int whichImage) {
            this.mImageNumber = whichImage;
            return this;
        }

        public RequestBuilder<Bitmap> build() {
            return createBaseRequest(requestManager, album, noCustomImage, forceDownload, mLoadOriginalImage, mImageNumber)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)

                    .transition(GenericTransitionOptions.with(DEFAULT_ANIMATION))
                    .priority(Priority.LOW)
                    //.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .signature(createSignature(album, mLoadOriginalImage, mImageNumber));
        }
    }

    public static class BitmapBuilder {
        private final Builder builder;

        public BitmapBuilder(Builder builder) {
            this.builder = builder;
        }

        public RequestBuilder<Bitmap> build() {
            //noinspection unchecked
            return createBaseRequest(builder.requestManager, builder.album, builder.noCustomImage, builder.forceDownload, builder.mLoadOriginalImage, builder.mImageNumber)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .priority(Priority.LOW)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .signature(createSignature(builder.album, builder.mLoadOriginalImage, builder.mImageNumber));
        }

        public RequestBuilder<Drawable> buildRequestDrawable() {
            //noinspection unchecked
            return createBaseRequestForDrawable(builder.requestManager, builder.album, builder.noCustomImage, builder.forceDownload, builder.mLoadOriginalImage, builder.mImageNumber)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .priority(Priority.LOW)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .signature(createSignature(builder.album, builder.mLoadOriginalImage, builder.mImageNumber));
        }

    }

    public static class PaletteBuilder {
        final Context context;
        private final Builder builder;

        public PaletteBuilder(Builder builder, Context context) {
            this.builder = builder;
            this.context = context;
        }

        public RequestBuilder<Bitmap> build() {
            //noinspection unchecked
            return createBaseRequest(builder.requestManager, builder.album, builder.noCustomImage, builder.forceDownload, builder.mLoadOriginalImage, builder.mImageNumber)
                    //.transcode(new BitmapPaletteTranscoder(context), BitmapPaletteWrapper.class)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)

                    .transition(GenericTransitionOptions.with(DEFAULT_ANIMATION))
                    .priority(Priority.LOW)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .signature(createSignature(builder.album, builder.mLoadOriginalImage, builder.mImageNumber));
        }

        public RequestBuilder<Drawable> buildRequestDrawable() {
            //noinspection unchecked
            return createBaseRequestForDrawable(builder.requestManager, builder.album, builder.noCustomImage, builder.forceDownload, builder.mLoadOriginalImage, builder.mImageNumber)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .priority(Priority.LOW)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .signature(createSignature(builder.album, builder.mLoadOriginalImage, builder.mImageNumber));
        }

    }
}
