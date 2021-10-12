package com.ldt.musicr.ui.page.librarypage.playlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ldt.musicr.R;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.util.Util;

import java.util.ArrayList;

public class MultipleSongAdapter extends RecyclerView.Adapter<MultipleSongAdapter.ViewHolder> {

    ArrayList<Song> songArrayList=new ArrayList<>();
    Context context;

    public MultipleSongAdapter(ArrayList<Song> songArrayList, Context context){
        this.songArrayList = songArrayList;
        this.context=context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle,mDescription;
        ImageView mImage;
        LinearLayout panel;

        public ViewHolder(View view) {
            super(view);

            mTitle = (TextView) view.findViewById(R.id.title);
            mDescription = (TextView) view.findViewById(R.id.description);
            mImage =  view.findViewById(R.id.image);
            panel=view.findViewById(R.id.panel);
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multiple_song, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position){

        holder.mTitle.setText(songArrayList.get(position).title);
        holder.mDescription.setText(songArrayList.get(position).artistName);

        Glide.with(context).load(Util.getAlbumArtUri(songArrayList.get(position).albumId))
                .placeholder(R.drawable.default_image_round)
                .error(R.drawable.default_image_round)
                .into(holder.mImage);
        final Song song = songArrayList.get(position);
        if(song.isSelected()){
            holder.panel.setBackgroundColor(context.getResources().getColor(R.color.s1) );
        }else{
            holder.panel.setBackgroundColor( context.getResources().getColor(R.color.backward_color));
        }
        holder.panel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                song.setSelected(!song.isSelected());
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount(){
        return songArrayList.size();
    }

}
