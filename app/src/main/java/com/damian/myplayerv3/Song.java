package com.damian.myplayerv3;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Damian on 12/28/2016.
 */
public class Song  implements Serializable{
    private long id;
    private String title,artist;
    private String imgPath;
    private String largeImgPath;



    public Song(long x,String t,String p){
        id=x;
        title=t;
        artist=p;
        imgPath=null;

    }
    public Song(long x, String t, String p, String a,String l){
        this(x,t,p);
        imgPath=a;
        largeImgPath=l;
    }



    public String getTitle(){return this.title;}
    public String getArtist(){return this.artist;}
    public long getId(){return this.id;}
    public String getImgPath(){return this.imgPath;}
    public String getLargeImgPath(){return this.largeImgPath;}


    @Override
    public String toString(){
        return "Song {title="+getTitle()+",artist="+getArtist()+",imgPath="+getImgPath()+",largeImgPath="+getLargeImgPath()+
               ",id="+getId()+"}";
    }



}
