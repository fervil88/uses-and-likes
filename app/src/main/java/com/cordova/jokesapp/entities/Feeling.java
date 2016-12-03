package com.cordova.jokesapp.entities;

import android.provider.BaseColumns;

import java.io.Serializable;

/**
 * Created by Emi on 12/11/2016.
 */

/*Table used to store pending likes and/or dislike to send to the server*/
public class Feeling implements Entity, Serializable{
    private String id;
    private int likes;
    private int dislikes;
    private String category;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Feeling)) {
            return false;
        }
        return ((Feeling) o).getId().equals(id);
    }

    //Idea from effective Java : Item 9
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id.hashCode();
        return result;
    }


    /* Inner class that defines the table contents */
    public static abstract class FeelingTable implements BaseColumns {
        public static final String TABLE_NAME = "FEELING";
        public static final String COLUMN_LIKE = "like";
        public static final String COLUMN_DISLIKE = "dislike";
    }

}
