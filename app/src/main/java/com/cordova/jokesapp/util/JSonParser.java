package com.cordova.jokesapp.util;

import android.content.Context;

import com.cordova.jokesapp.domain.Joke;
import com.cordova.jokesapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Fernando on 10/10/2016.
 */
public class JSonParser {

    public Joke readJson(Context context, String id) throws FileNotFoundException, JSONException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.jokes);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jObject = new JSONObject(byteArrayOutputStream.toString());
            JSONArray jArray = jObject.getJSONArray("jokes");
            String currentId;
            for (int i = 0; i < jArray.length(); i++) {
                currentId = jArray.getJSONObject(i).getString("id");
                if (currentId.equalsIgnoreCase(id)) {
                    JSONObject o = jArray.getJSONObject(i);
                    String jokeCategory = jArray.getJSONObject(i).getString(Util.PARAM_CATEGORY);
                    //  jokeCategory = jokeCategory != null ? jokeCategory.toUpperCase() : "";
                    Joke joke = new Joke(o.getString(Util.PARAM_ID), o.getString(Util.PARAM_TITLE), jokeCategory, o.getString(Util.PARAM_JOKE_TEXT),
                            o.getString(Util.PARAM_USER), o.getInt(Util.PARAM_LIKES), o.getInt(Util.PARAM_DISLIKES), o.getBoolean(Util.PARAM_DIRTY_JOKE),
                            o.getString(Util.PARAM_CREATION_DATE),o.getString(Util.PARAM_TAG),o.getLong(Util.PARAM_CHUNK));
                    return joke;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
