package com.harriague.curso.myapplication;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.harriague.curso.Util.JSonParser;
import com.harriague.curso.domain.Joke;

import org.json.JSONException;

import java.io.FileNotFoundException;

public class InfoJokeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_joke);
        Bundle b = getIntent().getExtras();
        String idStr = b.getString("id");

        try {
            Joke joke = JSonParser.readJson(this, idStr);
            if (joke != null){
                TextView title = (TextView) findViewById(R.id.title_joke);
                title.setText(joke.getTitle());
                TextView category = (TextView) findViewById(R.id.category_joke);
                category.setText(joke.getCategory());
                TextView jokeText = (TextView) findViewById(R.id.joke_text);
                jokeText.setText(joke.getJokeText());
                TextView user = (TextView) findViewById(R.id.user_joke);
                user.setText(joke.getUser());
                //TextView like = (TextView) findViewById(R.id.title_joke);
                //TextView dislike = (TextView) findViewById(R.id.title_joke);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
