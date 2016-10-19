package com.harriague.curso.util;

import android.content.Context;

import com.harriague.curso.domain.Joke;
import com.harriague.curso.myapplication.R;

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

    public static Joke readJson(Context context, String id) throws FileNotFoundException, JSONException {
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
                if (currentId.equalsIgnoreCase(id)){
                    String title = jArray.getJSONObject(i).getString("name");
                    String category = jArray.getJSONObject(i).getString("category");
                    String jokeText = jArray.getJSONObject(i).getString("joke");
                    String user = jArray.getJSONObject(i).getString("user");
                    //String likes = jArray.getJSONObject(i).getString("likes");
                    //String dislikes = jArray.getJSONObject(i).getString("dislikes");
                    Joke joke = new Joke(id, title, category, jokeText, user, Integer.parseInt("30"), Integer.parseInt("30"));
                    return joke;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
