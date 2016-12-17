package com.cordova.jokesapp.util;

import android.content.SharedPreferences;
import android.util.Log;

import com.cordova.jokesapp.entities.Joke;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
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
    public static final String FILENAME = "local_jokes.json";
    public static final String NEW_FILENAME = "new_jokes.json";
    public static final int SHOW_JOKES = 1;
    public static final String PARAM_ID = "id";
    public static final String PARAM_USER = "user";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_JOKE_TEXT = "jokeText";
    public static final String PARAM_LIKES = "likes";
    public static final String PARAM_DISLIKES = "dislikes";
    public static final String PARAM_CATEGORY = "category";
    public static final String PARAM_DIRTY_JOKE= "dirtyJoke";
    public static final String PARAM_CREATION_DATE = "creationDate";
    public static final String PARAM_TAG = "tag";
    public static final String PARAM_CHUNK = "chunk";

    public List<Joke> getAllBestJokes(int top, Map<String, List<Joke>> mapJokes){
        return getTheBestJokes(top, mapJokes, null, true);
    }


    public List<Joke> getTheBestJokes(int top, Map<String, List<Joke>> mapJokes, SharedPreferences sharedpreferences){
        return getTheBestJokes(top, mapJokes, sharedpreferences, false);
    }

    private List<Joke> getTheBestJokes(int top, Map<String, List<Joke>> mapJokes, SharedPreferences sharedpreferences, boolean includeAll){
        List<Joke> jokes = new LinkedList<>();
        float minAdded = 0;
        boolean includeDirtyJokes = includeAll ? includeAll : sharedpreferences.getBoolean(Util.MY_ENABLED_HEAVY_JOKE, false);

        for(Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
            if(Util.NEW_JOKES.equalsIgnoreCase(entry.getKey()) || Util.BEST_JOKES.equalsIgnoreCase(entry.getKey()))
                continue;

            for (Joke joke: entry.getValue()){
                if (!includeDirtyJokes && joke.isDirtyJoke()){
                    continue;
                }
                if (jokes.size() < top) {
                    jokes.add(joke); // jokes.add(newJoke);
                    Collections.sort(jokes);
                    minAdded = (jokes.get(jokes.size() - 1).getLikes() / (jokes.get(jokes.size() - 1).getDislikes() != 0 ? jokes.get(jokes.size() - 1).getDislikes() : 1));
                } else if (minAdded <= (joke.getLikes() / (joke.getDislikes() != 0 ? joke.getDislikes() : 1))) {
                    jokes.remove(jokes.size() - 1);
                    jokes.add(joke); //jokes.add(newJoke);
                    Collections.sort(jokes);
                    minAdded = (jokes.get(jokes.size() - 1).getLikes() / (jokes.get(jokes.size() - 1).getDislikes() != 0 ? jokes.get(jokes.size() - 1).getDislikes() : 1));
                } else {
                    break;
                }
            }
        }
        Collections.sort(jokes);
        return jokes;
    }

    public boolean isFromCurrentMonth (Joke joke) {
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd"); //actual date

        try {
            Date jokeDate = simpleDateFormat.parse(joke.getCreationDate());
            Calendar cal = Calendar.getInstance();
            int currentMonth = cal.get(Calendar.MONTH);
            int currentYear = cal.get(Calendar.YEAR);
            cal.setTime(jokeDate);
            int jokeMonth = cal.get(Calendar.MONTH);
            int jokeYear = cal.get(Calendar.YEAR);
            if (currentYear == jokeYear && currentMonth == jokeMonth) {
                return true;
            }
        } catch (ParseException e) {
            Log.e(Util.TAG, "Error parsing date: " + e.getMessage());
        }
        return false;
    }

}
