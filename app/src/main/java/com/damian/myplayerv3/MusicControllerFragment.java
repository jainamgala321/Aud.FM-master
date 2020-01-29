package com.damian.myplayerv3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import android.os.Handler;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by damianmandrake on 1/12/17.
 */
public class MusicControllerFragment extends Fragment implements CompoundButton.OnCheckedChangeListener,View.OnClickListener,MusicControllerFragmentConstants,SongRecycler.PlaySong{



    /*  issue #1-> when you quit the app while playing muisc by pressing the back button the state of the fragment buttons also needs to be saved
                    NOTE-> Service instance present throughout the course of execution of the program until the user explicitly kills app is ONE... this means if
                            user selects repeat once and pressess back button  the state has been registered in the MusicService class ... not in your fragment

                            Sol: 1.store val of songPosition on preferences and reload it ONLY WHEN musicService ISNT PLAYING since MainActivity handles this when its playing...
                            ... prolly call saveSong whenever pause is pressed? or whenever activity itself is destroyed so in activity onPause()

        issue #2-> once the app is quit this fragment must point to the song and on click of play must play the song


    */

    private TextView smallSongTitle,artist,songName,progressTime,endTime;
    private ImageView smallAlbumArt,imageAlbumArt;
    private ImageButton prev,next;
    private ToggleButton playPause,smallPlayPause;
    private SeekBar seekBar;
    private Handler handler;
    private Button b,shuffle;
    private boolean isInTouch=false;public boolean hasSavedStateBeenCalled=false;
    private Song song;
    int progress;






    private MusicService musicService;

    public void setCurrentSong(Song s){
        this.song=s;

        smallSongTitle.setText(s.getTitle());
        songName.setText(s.getTitle());
        System.out.println("artist is  " + artist == null);
        artist.setText(s.getArtist());

        int p=seekBar.getMax()/1000;

        endTime.setText((p/60)+":"+(p%60));


        //seekBar.setMax(musicService.getDuration());leads to an illegal state since media player hasnt been intited yet

        if(s.getImgPath()!=null) {
            imageAlbumArt.setImageBitmap(BitmapFactory.decodeFile(s.getLargeImgPath()));
            smallAlbumArt.setImageBitmap(BitmapFactory.decodeFile(s.getImgPath()));
        }else{
            imageAlbumArt.setImageResource(R.drawable.notfound);
            smallAlbumArt.setImageResource(R.drawable.notfound);
        }



    }
    public void setMusicService(MusicService a){
        musicService=a;

        //loadLastSong();
        try{
            loadLastSong();


        }catch (NullPointerException npe){
            System.out.println("IN setMusicService of NPE OF  FRAGMENT");
            npe.printStackTrace();
            hasSavedStateBeenCalled=false;
        }


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public void setSeekBarMax(int max){
        seekBar.setMax(max);

        updateSeekbar();
        //also setting the handler for the seekbar updations




    }
    public void setMaxDuration(int p){
        p/=1000;
        endTime.setText(p / 60 + ":" + p % 60);
    }
    Runnable seekbarUpdater=new Runnable() {
        @Override
        public void run() {

            int p = musicService.getPosition();
            seekBar.setProgress(p);
            int temp=p/1000;
            progressTime.setText((temp / 60) + ":" + (temp % 60));
            handler.postDelayed(this, 1000);
//            song.setCurrDuration(p);
        }
    };
    void updateSeekbar(){
        handler.post(seekbarUpdater);

    }

    public TextView getEndTime(){return endTime;}




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.music_cotroller_frag, container, false);

        System.out.println("is in oncReate");
        handler=new Handler();

        smallSongTitle=(TextView)view.findViewById(R.id.smallSongTitle);
        songName=(TextView)view.findViewById(R.id.songName);
        artist=(TextView)view.findViewById(R.id.artist);
        b=(Button)view.findViewById(R.id.repeater);

        playPause=(ToggleButton)view.findViewById(R.id.playPause);
        smallPlayPause=(ToggleButton)view.findViewById(R.id.smallPlayPlause);
        smallAlbumArt=(ImageView)view.findViewById(R.id.smallAlbumArt);
        imageAlbumArt=(ImageView)view.findViewById(R.id.imageAlbumArt);

        progressTime=(TextView)view.findViewById(R.id.currentTime);
        endTime=(TextView)view.findViewById(R.id.endTime);


        shuffle=(Button)view.findViewById(R.id.shuffle);








        playPause.setOnCheckedChangeListener(this);
        smallPlayPause.setOnCheckedChangeListener(this);
        next=(ImageButton)view.findViewById(R.id.nextSong);

        prev=(ImageButton)view.findViewById(R.id.prev);

        next.setOnClickListener(this);
        prev.setOnClickListener(this);
        b.setOnClickListener(this);
        shuffle.setOnClickListener(this);



        seekBar=(SeekBar)view.findViewById(R.id.musicSeekbar);// add its listener

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(seekbarUpdater);
                isInTouch = true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("final progress is " + progress);
                if (isInTouch) {
                    musicService.seekTo(progress);
                    isInTouch = false;
                }
                updateSeekbar();
            }
        });








        return view;
    }
    void toast(String a){

        Toast.makeText(getContext(),a,Toast.LENGTH_SHORT).show();


    }

    private void handleButtons(boolean b,boolean shouldItDoAnything){
        System.out.println("************************** value of handle buttons is " + b);
        if(b) {
            playPause.setBackgroundResource(R.mipmap.play);
            smallPlayPause.setBackgroundResource(R.mipmap.play);
            System.out.println("inside true of handle buttons");
            if(musicService.isPlaying()) {//dont need to check whether or not player is prep'd since player is unprep'd when its not playing
                System.out.println("about to pause the song");
                musicService.pause();
            }
            saveLastSong();
        }else{
            playPause.setBackgroundResource(R.mipmap.pause);
            smallPlayPause.setBackgroundResource(R.mipmap.pause);
            System.out.println("has savedStateBeenCalled is "+hasSavedStateBeenCalled);
                if(!MusicService.isMediaPlayerPrepared)//dont let the next statements to be processesed since player isnt prepared yet
                    return;
                if(shouldItDoAnything)
                if(!musicService.isPlaying())//what if no songs set... or musicService is null... or songs running while my button shows play
                musicService.startPlaying();
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        System.out.println("onCheckedChanged called");
        handleButtons(b,true);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.prev:
                musicService.onTouchPrev();

                return;

            case R.id.nextSong:
                musicService.onTouchNext();
                return;

            case R.id.repeater:
                MusicService.repeatState=(MusicService.repeatState+1)%3;
                setRepeatButton();
                return;

            case R.id.shuffle:
                MusicService.isShuffleOn=!MusicService.isShuffleOn;
                String x="Shuffle is Off";
                if(MusicService.isShuffleOn)
                    x="Shuffle is on";
                Toast.makeText(getContext(),x,Toast.LENGTH_LONG).show();

                //return;//not need since its the last one




        }
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("in onpause");
        saveLastSong();
    }

    public void saveLastSong(){

            SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            //System.out.println("SONG BEING SAVED IS " + song.toString());
            editor.putInt(CURR_SONG_POS_REF,musicService.getSongPosition());//since ive put songPos i can call setSongPos and call play
            editor.putInt(REPEAT_BUTTON_STATUS, MusicService.repeatState);
            editor.putBoolean(HAS_SAVE_BEEN_CALLED, true);
            editor.putInt(SEEKBAR_POS,progress);


            editor.putInt("seekbarMax",seekBar.getMax());
            editor.commit();




    }

    private void loadLastSong(){
        if(!musicService.isPlaying()) {
            SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            Map t=sharedPreferences.getAll();
            hasSavedStateBeenCalled=(Boolean)t.get(HAS_SAVE_BEEN_CALLED);
            musicService.setSongPosition((Integer) t.get(CURR_SONG_POS_REF));
            musicService.playSong();
            progress=(Integer)t.get(SEEKBAR_POS);

            MusicService.repeatState=(Integer)t.get(REPEAT_BUTTON_STATUS);
            int p=(Integer)t.get("seekbarMax");
            setRepeatButton();
            //this will pause the song bydefault
            seekBar.setMax(p);
            handleButtons(false,false);
            //handleButtons(true);//not pausing the song ... since till the time the true part is executed the musicService hasnt really started playing the song...



        }
        System.out.println("Muisc is playing");
    }

    //to be called while restoring state... also called whenever its clicked
    private void setRepeatButton(){
        switch (MusicService.repeatState){

            case 1:
                b.setBackgroundResource(R.mipmap.repeat_infinite);
                toast(REPEAT_ONCE);
                break;

            case 2:
                b.setBackgroundResource(R.mipmap.play_once);
                toast(REPEAT_INFINITE);
                break;

            default://0
                toast(PLAY_NORM);
                b.setBackgroundResource(R.mipmap.repeat_once);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("IN FRAG ON DESTROY");
        //saveLastSong();
    }


    //Overriding the callback i wrote in the SongRecycler class... everytime onClick is generated in SongRecycler this callback will be called
    @Override
    public void play(int p){
        musicService.setSongPosition(p);
        musicService.playSong();
        handleButtons(false,false);

    }



}
interface MusicControllerFragmentConstants{
    final static String PLAY_NORM="Song will be played once",REPEAT_ONCE="Song will be repeated once",REPEAT_INFINITE="Song will be repeated infinitely";
    final static String CURR_SONG_POS_REF="SONG_POSITION";
    final static String HAS_SAVE_BEEN_CALLED="in onSavePreferences";
    final static String REPEAT_BUTTON_STATUS="repeat button status";
    final static String SEEKBAR_POS="seekbar position";
}

