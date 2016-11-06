package com.cordova.jokerapp.domain;

import java.io.Serializable;

/**
 * Created by Fernando on 10/10/2016.
 */
public class Joke implements Comparable<Joke>, Cloneable, Serializable{
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

    public Joke(){}

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

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getJokeText() {
        return jokeText;
    }

    public String getUser() {
        return user;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDirtyJoke() {
        return isDirtyJoke;
    }

    public void setDirtyJoke(boolean dirtyJoke) {
        isDirtyJoke = dirtyJoke;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setJokeText(String jokeText) {
        this.jokeText = jokeText;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
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

    @Override
    public int compareTo(Joke anotherJoke) {

        float compareQuantityOfLikes = ((Joke) anotherJoke).getLikes() / (((Joke) anotherJoke).getDislikes() != 0 ? ((Joke) anotherJoke).getDislikes() : 1);

        //ascending order
        //return ((this.getLikes() - (this.getDislikes() != 0 ? this.getDislikes() : 1)) - compareQuantityOfLikes);

        //descending order
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

    //Idea from effective Java : Item 9
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + title.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + creationDate.hashCode();
        return result;
    }
}
