package com.harriague.curso.domain;

/**
 * Created by Fernando on 10/10/2016.
 */
public class Joke implements Comparable<Joke> {
    private String id;
    private String title;
    private String category;
    private String jokeText;
    private String user;
    private int likes;
    private int dislikes;
    private boolean isDirtyJoke;

    public Joke(){}

    public Joke(String id, String title, String category, String jokeText, String user, int likes, int dislikes, boolean isDirtyJoke) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.jokeText = jokeText;
        this.user = user;
        this.likes = likes;
        this.dislikes = dislikes;
        this.isDirtyJoke = isDirtyJoke;
    }

    public Joke(String id, String title, String category, String jokeText, String user, int likes, int dislikes) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.jokeText = jokeText;
        this.user = user;
        this.likes = likes;
        this.dislikes = dislikes;
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

    @Override
    public int compareTo(Joke anotherJoke) {

        int compareQuantityOfLikes = ((Joke) anotherJoke).getLikes() - ((Joke) anotherJoke).getDislikes();

        //ascending order
        //return ((this.getLikes() - this.getDislikes()) - compareQuantityOfLikes);

        //descending order
        return (compareQuantityOfLikes - (this.getLikes() - this.getDislikes()));
    }
}
