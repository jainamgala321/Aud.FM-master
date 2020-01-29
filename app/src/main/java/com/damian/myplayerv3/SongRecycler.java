package com.damian.myplayerv3;


        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.drawable.Drawable;
        import android.os.AsyncTask;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.FileInputStream;
        import java.io.IOException;
        import java.util.ArrayList;

/**
 * Created by Damian on 12/30/2016.
 */
public class SongRecycler extends RecyclerView.Adapter<SongRecycler.SongViewHolder> {

    private Context ctx;
    private ArrayList<Song> songList;
    private static MusicService musicService;

    static private PlaySong playSong;//reference obj... is static since i have to reference this from the inner class

    public SongRecycler(Context c, ArrayList a){
        ctx=c;
        songList=a;

        System.out.println("INSIDE CTOR OF SOngRecycyler");


    }



    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(ctx).inflate(R.layout.song_recycler_item,parent,false);
        System.out.println("inside oncreateViewHolder");

        return new SongViewHolder(view,songList,ctx);
    }

    @Override
    public void onBindViewHolder(final SongViewHolder holder, int position) {
        System.out.println("Inside onBindViewHolder");

        final Song currSong=songList.get(position);
        System.out.println("binding "+currSong.getTitle());
        if(currSong.getImgPath()!=null) {
            holder.setCurrSong(position);
            System.out.println("imgPath of " + currSong.getTitle() + " IS " + currSong.getImgPath());


            //adding async thread here

            new AsyncTask<RecyclerView.ViewHolder,Void,Bitmap>(){

                private RecyclerView.ViewHolder v;

                @Override
                protected Bitmap doInBackground(RecyclerView.ViewHolder... viewHolders) {
                    v=viewHolders[0];
                    return BitmapFactory.decodeFile(((SongViewHolder)v).getCurrSongImgPath());



                }


                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    ((SongViewHolder)v).albumArt.setImageBitmap(bitmap);
                    System.out.println("image to albumArt just got set");

                }
            }.execute(holder);


            //



        }
        else
            holder.albumArt.setImageResource(R.drawable.notfound);


        holder.songName.setText(currSong.getTitle());
        holder.artistName.setText(currSong.getArtist());

        System.out.println("Leaving onBindViewHolder");

    }



    @Override
    public int getItemCount() {
        return songList.size();
    }

    public void setPlaySongReference(PlaySong ref){
        playSong=ref;
    }





    public static class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView songName,artistName;
        ImageView albumArt;
        ArrayList<Song> tempSong;
        Context ctx;
        int p;


        SongViewHolder(View view,ArrayList<Song> arrayList,Context c){
            super(view);
            view.setOnClickListener(this);
            tempSong=arrayList;
            ctx=c;
            songName=(TextView)view.findViewById(R.id.songTitle);
            artistName=(TextView)view.findViewById(R.id.artistName);
            albumArt=(ImageView) view.findViewById(R.id.songImg);

        }


        @Override
        public void onClick(View view){
            int position=getAdapterPosition();
            //Song clickedSong=tempSong.get(position);
            //Toast.makeText(ctx,"You just clicked on "+clickedSong.getTitle(),Toast.LENGTH_SHORT).show();
            SongRecycler.playSong.play(position);



        }

        //added the following methods to make it kinda lightweight
        void setCurrSong(int p){
            this.p=p;
        }
        String getCurrSongImgPath(){
            return tempSong.get(p).getImgPath();
        }





    }


     interface PlaySong{
        public void play(int position);
    }









}
