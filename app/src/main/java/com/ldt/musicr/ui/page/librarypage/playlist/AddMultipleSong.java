package com.ldt.musicr.ui.page.librarypage.playlist;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ldt.musicr.R;
import com.ldt.musicr.loader.medialoader.PlaylistLoader;
import com.ldt.musicr.loader.medialoader.SongLoader;
import com.ldt.musicr.model.Playlist;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.util.PlaylistsUtil;

import java.util.ArrayList;

public class AddMultipleSong extends AppCompatActivity{

    MultipleSongAdapter adapter;
    ArrayList<Song> songArrayList=new ArrayList<>();
    ArrayList<Song> selectedList=new ArrayList<>();
    Playlist mPlaylist;
    String playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_multiple_song);

        playlist=getIntent().getStringExtra("playlist");
        mPlaylist= PlaylistLoader.getPlaylist(AddMultipleSong.this,playlist);
//        songArrayList=getPlaylistWithListId(
//                AddMultipleSong.this, mPlaylist);
        songArrayList=SongLoader.getAllSongs(AddMultipleSong.this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new MultipleSongAdapter(songArrayList,AddMultipleSong.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.done_song, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:

                for (Song model : songArrayList) {
                    if (model.isSelected()) {
                        selectedList.add(model);
                    }
                }
//                Log.e("LLLL name:"+mPlaylist.getName()," Id :" + mPlaylist.getId());
                PlaylistsUtil.addToPlaylist(AddMultipleSong.this,selectedList,mPlaylist.getId(),true);

                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    //    public static ArrayList<Song> getPlaylistWithListId(@NonNull Context context,Playlist list) {
//
//            ArrayList<Song> songlist = new ArrayList<>(PlaylistSongLoader.getPlaylistSongList(context, list.id));
//            return songlist;
//
//    }

}