package com.ldt.musicr.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ldt.musicr.App;
import com.ldt.musicr.model.Album;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public class CustomAlbumImageUtil {
    private static final String CUSTOM_ARTIST_IMAGE_PREFS = "custom_artist_image";
    private static final String FOLDER_NAME = "/custom_artist_images/";

    private static CustomAlbumImageUtil sInstance;

    private final SharedPreferences mPreferences;

    private CustomAlbumImageUtil(@NonNull final Context context) {
        mPreferences = context.getApplicationContext().getSharedPreferences(CUSTOM_ARTIST_IMAGE_PREFS, Context.MODE_PRIVATE);
    }

    public static CustomAlbumImageUtil getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new CustomAlbumImageUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    public static File getFile(Album artist) {
        File dir = new File(App.getInstance().getFilesDir(), FOLDER_NAME);
        return new File(dir, getFileName(artist));
    }

    private static String getFileName(Album artist) {
        String artistName = artist.getTitle();
        if (artistName == null)
            artistName = "";
        // replace everything that is not a letter or a number with _
        artistName = artistName.replaceAll("[^a-zA-Z0-9]", "_");
        return String.format(Locale.US, "#%d#%s.jpeg", artist.getId(), artistName);
    }

    public void setCustomArtistImage(final Album artist, Uri uri) {
        Glide.with(App.getInstance())
                .asBitmap()
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        Toast.makeText(App.getInstance(), "Sorry, something went wrong :((", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResourceReady(final Bitmap resource, Transition<? super Bitmap> glideAnimation) {
                        new AsyncTask<Void, Void, Void>() {
                            @SuppressLint("ApplySharedPref")
                            @Override
                            protected Void doInBackground(Void... params) {
                                File dir = new File(App.getInstance().getFilesDir(), FOLDER_NAME);
                                if (!dir.exists()) {
                                    if (!dir.mkdirs()) { // create the folder
                                        return null;
                                    }
                                }
                                File file = new File(dir, getFileName(artist));

                                boolean succesful = false;
                                try {
                                    OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                                    succesful = ImageUtil.resizeBitmap(resource, 2048).compress(Bitmap.CompressFormat.JPEG, 100, os);
                                    os.close();
                                } catch (IOException e) {
                                    Toast.makeText(App.getInstance(), e.toString(), Toast.LENGTH_LONG).show();
                                }

                                if (succesful) {
                                    mPreferences.edit().putBoolean(getFileName(artist), true).commit();
                                    ArtistSignatureUtil.getInstance(App.getInstance()).updateArtistSignature(artist.getTitle());
                                    App.getInstance().getContentResolver().notifyChange(Uri.parse("content://media"), null); // trigger media store changed to force artist image reload
                                }
                                return null;
                            }
                        }.execute();
                    }
                });
    }

    public void resetCustomArtistImage(final Album artist) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("ApplySharedPref")
            @Override
            protected Void doInBackground(Void... params) {
                mPreferences.edit().putBoolean(getFileName(artist), false).commit();
                ArtistSignatureUtil.getInstance(App.getInstance()).updateArtistSignature(artist.getTitle());
                App.getInstance().getContentResolver().notifyChange(Uri.parse("content://media"), null); // trigger media store changed to force artist image reload

                File file = getFile(artist);
                if (!file.exists()) {
                    return null;
                } else {
                    file.delete();
                }
                return null;
            }
        }.execute();
    }

    public boolean hasCustomAlbumImage(Album artist) {
        return mPreferences.getBoolean(getFileName(artist), false);
    }
}

