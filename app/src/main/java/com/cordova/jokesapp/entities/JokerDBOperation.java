package com.cordova.jokesapp.entities;

import java.util.List;

/**
 * Created by Romi on 18/11/2016.
 */
public interface JokerDBOperation {

    public void addJokes(List<Joke> jokes);

    public void addJoke(Joke joke);

    public List<Joke> getAllJokes();

    public List<Joke> getAllJokesByCategory(String category);

    public Joke getJokeById(String id);

    public void deleteJoke(Joke joke);

    public List<String> getAllCategoriesFromJokes();

    public void addNewJokes(List<Joke> jokes);

    public Joke getNewJokeById(String id);

    public List<Joke> getNewJokes();

    public void addBestJokes(List<Joke> jokes);

    public Joke getBestJokeById(String id);

    public List<Joke> getBestJokes();

    public int updateJokeFromServer(String id, int likes, int dislikes);

    public int updateNewJokeFromServer(String id, int likes, int dislikes);

    public int updateBestJokeFromServer(String id, int likes, int dislikes);

    public int updateJokePlusLike(String id, String table);

    public int updateJokePlusDislike(String id, String table);

    public void deleteNewJoke(Joke joke);

    public void deleteBestJoke(Joke joke);

    public void deleteNewJokes();

    public void deleteBestJokes();

    public void addFeeling(Feeling feeling);

    public Feeling getFeelingById(String id);

    public List<Feeling> getAllFeelings();

    public void deleteAllFeelings();

    public int updateFeelingLike(String id);

    public int updateFeelingDislike(String id);
}
