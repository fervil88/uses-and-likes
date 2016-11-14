package com.cordova.jokesapp.entities;

import android.provider.BaseColumns;
/**
 * Created by Emi on 12/11/2016.
 */

/*Table used to store pending likes and/or dislike to send to the server*/
public class Feeling implements Entity{
    private String id;
    private int likes;
    private int dislikes;

    public Feeling() { }

    public Feeling(String id, int likes, int dislikes) {
        this.id = id;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    /* Inner class that defines the table contents */
    public static abstract class FeelingTable implements BaseColumns {
        public static final String TABLE_NAME = "FEELING";
        public static final String COLUMN_LIKE = "like";
        public static final String COLUMN_DISLIKE = "dislike";
    }

}
