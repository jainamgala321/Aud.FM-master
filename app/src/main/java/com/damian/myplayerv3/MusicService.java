package com.damian.myplayerv3;


        import android.app.Service;
        import android.content.ContentUris;
        import android.content.Intent;
        import android.media.AudioManager;
        import android.media.MediaPlayer;
        import android.net.Uri;
        import android.os.Binder;
        import android.os.IBinder;
        import android.os.Parcel;
        import android.os.Parcelable;
        import android.os.PowerManager;
        import android.provider.MediaStore;
        import android.support.annotation.Nullable;

        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Damian on 12/30/2016.
 */
public class MusicService extends Service implements MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener,MusicServiceConstants {

    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songsList;
    private int songPosition;// song position will be used to restore state once app is restarted
    private final MusicBinder musicBinder=new MusicBinder();
    private MusicControllerFragment ref;
    static int repeatState=0;//0 is normal,1 is repeat once,2 is repeat infinitely
    private boolean playCount;
    static boolean isMediaPlayerPrepared=false;//true in onPrepared and false in onCompleted or in all other cases....
    static boolean isShuffleOn=false;

    //might have to implement parcelable to write the current state in a bundle
    //Parcelable

    //since services dont require ctor... use lifecycle methods
    @Override
    public void onCreate(){
        //whenever you call baseclass lifecycle func... android binds and marks the current class as a service
        super.onCreate();

        songPosition=0;
        mediaPlayer=new MediaPlayer();
        initMusicPlayer();

    }
    public void initMusicPlayer(){

        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);// this lets the service run even when the device is locked
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //setting the listeners for the MediaPlayer interfaces

        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
    }
    public void setSongsList(ArrayList<Song> s) {
        songsList=s;
    }

    public boolean isListEmpty(){
        return songsList.isEmpty();
    }
    public MediaPlayer getMediaPlayer(){return mediaPlayer;}


    public class MusicBinder extends Binder{
        MusicService getServiceInstance(){
            return MusicService.this;//return an instance of MusicService... this is being calledin the MainActivity as a part of SrviceConnection callback so that you can actually obtain the instance of the service from the OS
        }
    }
    public void playSong(){
        mediaPlayer.reset();
        Song toBePlayed=songsList.get(songPosition);
        System.out.println("playing "+toBePlayed.getTitle());

        ref.setCurrentSong(toBePlayed);
        playCount =!playCount ;

        //done ... dont have to do this anywhere else;
        long currSong=toBePlayed.getId();
        Uri trackUri= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,currSong);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);

        }catch (IOException io){
            System.out.println("MusicPlayer Excpetion ");
        }

        mediaPlayer.prepareAsync();


    }

    public void setSongPosition(int p){
        songPosition=p;
    }
    public int getSongPosition(){return songPosition;}
    public int getPosition(){
        return mediaPlayer.getCurrentPosition();
    }



    public int getDuration() {
        return mediaPlayer.getDuration();

    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
    public void pause(){
        mediaPlayer.pause();
    }
    public void startPlaying(){
        mediaPlayer.start();
    }
    public void seekTo(int p){
        mediaPlayer.seekTo(p);
    }

    public void playNext(){

         if(repeatState==PLAY_NORMALLY )
        songPosition= isShuffleOn?ThreadLocalRandom.current().nextInt(0,songsList.size()):(songPosition+1)%songsList.size();
        else if(repeatState==REPEAT_ONCE && !playCount){
            playCount=!playCount;

            repeatState=PLAY_NORMALLY;

            playNext();
            repeatState=REPEAT_ONCE;
        }

        playSong();

    }
    public Song getCurrentlyPlayingSong(){

            return songsList.get(songPosition);

    }


    public void playPrevious(){

        if(repeatState==PLAY_NORMALLY)
        songPosition = songPosition ==0 ? songsList.size()-1:songPosition-1;

        playSong();
    }

    public void onTouchNext(){
        playCount=false;
        int repeatButtonStatus=repeatState;
        repeatState=PLAY_NORMALLY;
        playNext();
        repeatState=repeatButtonStatus;



    }
    public void onTouchPrev(){
        playCount=false;
        int repeatButtonStatus=repeatState;
        repeatState=PLAY_NORMALLY;
        playPrevious();
        repeatState=repeatButtonStatus;

    }

    public void setRef(MusicControllerFragment m){
        ref=m;
    }






    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("About to return musicBinder");
        return musicBinder;
        //return null;//since im using start service... dont require a musicBinder instance
    }
    @Override
    public boolean onUnbind(Intent intent){
        mediaPlayer.stop();
        mediaPlayer.release();

        return false;
    }



    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();isMediaPlayerPrepared=false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }


    //this callback is called every time muisc is changed
    @Override
    public void onPrepared(MediaPlayer mp) {
        System.out.println("in onPrepared");
        isMediaPlayerPrepared=true;
        mediaPlayer.start();
        int p=mediaPlayer.getDuration();
        ref.setSeekBarMax(p);p/=1000;
        ref.getEndTime().setText((p/60)+":"+p%60);
        System.out.println("************************************** IN ONCOMPLETED OF MEDIAPLAYER");
        if(ref.hasSavedStateBeenCalled) {//to play song from the point it was paused
            ref.hasSavedStateBeenCalled=false;
            mediaPlayer.seekTo(ref.progress);
        }


    }







}
interface MusicServiceConstants{
     static final byte PLAY_NORMALLY=0,REPEAT_ONCE=1,REPEAT_INFINITELY=2;

}
