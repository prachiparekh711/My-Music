package com.ldt.musicr.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.ldt.musicr.R;
import com.ldt.musicr.model.Playlist;

public class Utils {
    public static boolean isShuffle=false;
    public static Playlist playlist;

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(R.id.searchBar);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
