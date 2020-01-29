package com.damian.myplayerv3;

import android.Manifest;

import android.app.ProgressDialog;
import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.os.Environment;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;

public class MainActivity extends AppCompatActivity implements MainActivityConstants {


    private Button b;
    boolean resumeApp=false;

    private AllSongsFragment allSongsFragment;
    public MusicControllerFragment musicControllerFragment;//since this has to be accessed by all other classes in the program


    public ProgressDialog progressDialog;


    private MusicService musicService;
    private Intent musicPlayerIntent;
    private boolean isPlayerBound=false;
    private FrameLayout frameLayout,musicListFrame;
    private android.support.v4.app.FragmentManager fragmentManager;
    private android.support.v4.app.FragmentTransaction fragmentTransaction;

    private Class mHolder=MusicService.class;
    private float previousY,trans,diff;


    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            System.out.println("inside onServiceConnected of musicConnection which inits musicService");
            MusicService.MusicBinder musicBinder = (MusicService.MusicBinder) service;
            System.out.println(musicBinder.toString()+" music BINDER");
            musicService = musicBinder.getServiceInstance();
            System.out.println("MUSIC SERVICE HAS VAL " + musicService.toString());

            if(resumeApp && musicService.isPlaying()) {
                System.out.println("Inside service if");
                Song s=musicService.getCurrentlyPlayingSong();//happens only if musicService is running ie most times... have to check it out
                if(s!=null) {
                    System.out.println("setting song to curr");
                    musicControllerFragment.setCurrentSong(s);
                    int maxDur=musicService.getDuration();
                    musicControllerFragment.setSeekBarMax(maxDur);
                    musicControllerFragment.setMaxDuration(maxDur);
                    musicControllerFragment.setMusicService(musicService);



                }
            }
            musicService.setRef(musicControllerFragment);


            isPlayerBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("inside onServiceDisconnected");
            isPlayerBound = false;

        }
    };





    @Override
    protected void onStart(){
        super.onStart();


        if(musicPlayerIntent==null){

            musicPlayerIntent=new Intent(this,mHolder);
            System.out.println("about to start service");
            getApplicationContext().bindService(musicPlayerIntent, musicConnection, Context.BIND_AUTO_CREATE);//, musicConnection, Context.BIND_AUTO_CREATE);


        }

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check if data is present in savedInstance

        System.out.println("setting layout");
        setContentView(R.layout.activity_main);


        try{
            loadPreferences();


        }catch (NullPointerException npe){
            System.out.println("App started without call to onDestroy previously");
        }



                allSongsFragment = new AllSongsFragment();
                musicControllerFragment = new MusicControllerFragment();


        frameLayout=(FrameLayout)findViewById(R.id.musicControllerFragPlaceholder);
        fragmentManager=getSupportFragmentManager();

        musicListFrame=(FrameLayout)findViewById(R.id.musicListFrag);


        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setInverseBackgroundForced(false);






        b=(Button)findViewById(R.id.permButton);




        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {

            System.out.println("INSIDE perm if");



            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                System.out.println(" permissions had been granted before about to start async");
                b.setVisibility(View.INVISIBLE);

                allSongsFragment.setBackTaskAndExecute(this);
                setFramesVisible();
                initBotFrag();
                initOtherFrag(allSongsFragment);



            }else
            {
                System.out.println("inside else about to turn button visibility on");//remove asap
                //show button only when perms are denied
                b.setVisibility(View.VISIBLE);
                setFramesInvisible();


            }
        }
        //this takes 2 onclick listeners



    }

    void savePreferences(){
        SharedPreferences sharedPreferences=getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(IS_IN_ON_DESTROY,true);


        editor.commit();
    }


    void loadPreferences(){
        SharedPreferences sharedPreferences=getPreferences(MODE_PRIVATE);
        resumeApp=(Boolean)sharedPreferences.getAll().get(IS_IN_ON_DESTROY);
    }


    public MusicService getMusicService(){
        return musicService;
    }
    void setFramesInvisible(){
        musicListFrame.setVisibility(View.INVISIBLE);
        frameLayout.setVisibility(View.INVISIBLE);
    }
    void setFramesVisible(){
        frameLayout.setVisibility(View.VISIBLE);
        musicListFrame.setVisibility(View.VISIBLE);
    }

    private void initBotFrag(){
        System.out.println("inside initBotFrag");

        fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.musicControllerFragPlaceholder,musicControllerFragment);
        fragmentTransaction.commit();

        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        previousY = motionEvent.getY();

                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float currentY = motionEvent.getY();
                        diff = previousY - currentY;
                        trans = view.getTranslationY();
                        trans -= diff;

                        trans = trans < 0 ? 0 : (trans > MAX_TRANSLATION ? MAX_TRANSLATION : trans);

                        view.setTranslationY(trans);

                        return true;

                    case MotionEvent.ACTION_UP:

                        if (diff < 0)
                            view.setTranslationY(MAX_TRANSLATION);
                        else if (diff > 0)
                            view.setTranslationY(0);


                        return true;


                }

                return view.onTouchEvent(motionEvent);

            }
        });

    }
    private void initOtherFrag(Fragment f){
        fragmentTransaction =fragmentManager.beginTransaction();
        fragmentTransaction.replace(musicListFrame.getId(),f);
        fragmentTransaction.commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            if(musicPlayerIntent!=null&&isPlayerBound){
                unbindService(musicConnection);
            }
            deleteCompressedImages();onDestroy();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }






    public void askPerms(View v){
        //has to be public so that the xml code can access it
        System.out.println("about to ask for perm");
        if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            toast(REQUIRE_PERMS);
        System.out.println("about to get into activity compat");
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_READ_EXTERNAL);

    }
    private void toast(String a){

        Toast.makeText(this, a, Toast.LENGTH_SHORT).show();
    }






    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_READ_EXTERNAL:
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED) {
                    toast(REQUIRE_PERMS);
                    setFramesInvisible();
                    b.setVisibility(View.VISIBLE);
                }
                else {
                    System.out.println("ABOUT TO CALL AsyncThread");
                    b.setVisibility(View.INVISIBLE);

                    allSongsFragment.setBackTaskAndExecute(this);
                    initBotFrag();
                    initOtherFrag(allSongsFragment);
                    setFramesVisible();
                    /*backTask=new SongListCompressBackTask(this,recyclerView);   //make backtask obj in fragment and excute...
                    */
                }
                return;

            default:
                super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        savePreferences();
        //musicControllerFragment.saveLastSong();wont work as fragments are removed before activity

    }

    private void deleteCompressedImages() {

        File[] f = externalParentDir.listFiles();
        if (f != null)
            for (File x : f) {
                System.out.println("deleting " + x.getName());
                System.out.println(x.delete());
            }
        System.out.println("in on destroy prolly deleted files/folders");

    }





}

interface MainActivityConstants{
    static final int MY_READ_EXTERNAL=4;
    static final String REQUIRE_PERMS="require permissions for proper functioning";
    static final String externalStoragePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).getAbsolutePath();
    static final File externalParentDir=new File(externalStoragePath);
    static final int MAX_TRANSLATION=1300;

    static final String IS_IN_ON_DESTROY="isInOnDestroy";

}