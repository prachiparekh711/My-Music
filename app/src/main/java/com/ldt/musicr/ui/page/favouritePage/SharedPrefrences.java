package com.ldt.musicr.ui.page.favouritePage;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldt.musicr.model.Song;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPrefrences {

    public static final String MyPREFERENCES = "FAVOURITES";
    public static String MODEL = "MODEL";

    public static <T> void setFavouriteList(Context c1, List<T> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);

        set(c1,MODEL, json);
    }

    public static void set(Context context,String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static List<Song> getFavouriteList(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        List<Song> arrayItems=new ArrayList<>();
        String serializedObject = sharedPreferences.getString(MODEL, null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Song>>(){}.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }
        return arrayItems;
    }
}
