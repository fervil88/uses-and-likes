package com.cordova.jokesapp.entities;

import android.provider.BaseColumns;

import java.io.Serializable;

/**
 * Created by Emi on 12/11/2016.
 */

public class Joke implements Entity, Comparable<Joke>, Cloneable, Serializable {
    private String id;
    private String title;
    private String category;
    private String jokeText;
    private String user;
    private int likes;
    private int dislikes;
    private boolean isDirtyJoke;
    private String creationDate;
    private String tag;
    private long chunk;

    @Override
    public String toString() {
        return "Joke{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", jokeText='" + jokeText + '\'' +
                ", user='" + user + '\'' +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                ", isDirtyJoke=" + isDirtyJoke +
                ", creationDate='" + creationDate + '\'' +
                ", tag='" + tag + '\'' +
                ", chunk=" + chunk +
                '}';
    }

    public Joke() {}

    public Joke(String id, String title, String category, String jokeText, String user, int likes, int dislikes, boolean isDirtyJoke, String creationDate, String tag, long chunk) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.jokeText = jokeText;
        this.user = user;
        this.likes = likes;
        this.dislikes = dislikes;
        this.isDirtyJoke = isDirtyJoke;
        this.creationDate = creationDate;
        this.tag = tag;
        this.chunk = chunk;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getJokeText() {
        return jokeText;
    }

    public void setJokeText(String jokeText) {
        this.jokeText = jokeText;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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

    public boolean isDirtyJoke() {
        return isDirtyJoke;
    }

    public void setDirtyJoke(boolean dirtyJoke) {
        isDirtyJoke = dirtyJoke;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getChunk() {
        return chunk;
    }

    public void setChunk(long chunk) {
        this.chunk = chunk;
    }

    @Override
    public int compareTo(Joke anotherJoke) {

        float compareQuantityOfLikes = ((Joke) anotherJoke).getLikes() / (((Joke) anotherJoke).getDislikes() != 0 ? ((Joke) anotherJoke).getDislikes() : 1);
        return (Math.round(compareQuantityOfLikes) - (this.getLikes() / (this.getDislikes() != 0 ? this.getDislikes() : 1) ));
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Joke)) {
            return false;
        }

        Joke joke = (Joke) o;

        return joke.getId().equals(id) &&
                joke.getTitle().equals(title)
                && joke.getCreationDate().equals(creationDate);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    //Idea from effective Java : Item 9
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + title.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + creationDate.hashCode();
        return result;
    }

    /* Inner class that defines the table contents for Jokes */
    public static abstract class JokeTable implements BaseColumns {
        public static final String TABLE_NAME = "Joke";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_USER = "user";
        public static final String COLUMN_LIKE = "like";
        public static final String COLUMN_DISLIKE = "dislike";
        public static final String COLUMN_IS_DIRTY = "is_dirty";
        public static final String COLUMN_CREATION_DATE = "creation_date";
        public static final String COLUMN_TAG = "tag";
        public static final String COLUMN_CHUNK = "chunk";
    }

    /* Inner class that defines the table contents for New Jokes */
    public static abstract class NewJokeTable extends JokeTable implements BaseColumns {
        public static final String TABLE_NAME = "New_Joke";
    }

    /* Inner class that defines the table contents for Best Jokes */
    public static abstract class BestJokeTable extends JokeTable implements BaseColumns {
        public static final String TABLE_NAME = "Best_Joke";
    }
}
