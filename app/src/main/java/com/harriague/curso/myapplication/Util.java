package com.harriague.curso.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.harriague.curso.domain.Joke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Fernando on 10/12/2016.
 */
public class Util {
    public static final String TAG = "Jokes";
    public static final String NEW_JOKES = "Nuevos";
    public static final String BEST_JOKES = "Destacados";
    public static final String CATEGORIES = "categories";
    public static final String MY_PREFERENCES = "MyPreference";
    public static final String MY_ENABLED_HEAVY_JOKE = "ENABLED_HEAVY_JOKE";


    public void includeTheBestJokes(int top, Map<String, List<Joke>> mapJokes, List<String> listDataHeader, Map<String, List<String>> listDataChild, SharedPreferences sharedpreferences){
        List<Joke> jokes = getTheBestJokes(top, mapJokes, sharedpreferences);
        List<String> subCategoriesJokes = new ArrayList<String>();
        for (Joke joke: jokes){
            subCategoriesJokes.add(joke.getId()+"<->"+joke.getTitle()+"<->"+joke.getLikes()+"<->"+joke.getDislikes()+"<->"+joke.getJokeText()+"<->"+joke.getCategory()+"<->"+joke.isDirtyJoke()+"<->"+joke.getCreationDate()+"<->"+joke.getUser());
        }
        listDataHeader.add(Util.BEST_JOKES);
        listDataChild.put(Util.BEST_JOKES, subCategoriesJokes);
    }

    private List<Joke> getTheBestJokes(int top, Map<String, List<Joke>> mapJokes, SharedPreferences sharedpreferences){
        List<Joke> jokes = new ArrayList<Joke>();
        float minAdded = 0;
        boolean includeDirtyJokes = sharedpreferences.getBoolean(Util.MY_ENABLED_HEAVY_JOKE, false);

        for(Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
            if(Util.NEW_JOKES.equalsIgnoreCase(entry.getKey()) || Util.BEST_JOKES.equalsIgnoreCase(entry.getKey()))
                continue;

            for (Joke joke: entry.getValue()){
                if (!includeDirtyJokes && joke.isDirtyJoke()){
                    continue;
                }
                try {
                    Joke newJoke = (Joke) joke.clone();
                    newJoke.setCategory(Util.BEST_JOKES);
                    if (jokes.size() < top) {
                        jokes.add(newJoke);
                        Collections.sort(jokes);
                        minAdded = (jokes.get(jokes.size() - 1).getLikes() / (jokes.get(jokes.size() - 1).getDislikes() != 0 ? jokes.get(jokes.size() - 1).getDislikes() : 1));
                    } else if (minAdded <= (joke.getLikes() / (joke.getDislikes() != 0 ? joke.getDislikes() : 1))) {
                        jokes.remove(jokes.size() - 1);
                        jokes.add(newJoke);
                        Collections.sort(jokes);
                        minAdded = (jokes.get(jokes.size() - 1).getLikes() / (jokes.get(jokes.size() - 1).getDislikes() != 0 ? jokes.get(jokes.size() - 1).getDislikes() : 1));
                    } else {
                        break;
                    }
                } catch (CloneNotSupportedException e) {
                    Log.e(Util.TAG, "Error clone joke: " + e.getMessage());
                }
            }
        }
        Collections.sort(jokes);
        return jokes;
    }

}
