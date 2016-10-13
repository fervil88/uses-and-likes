package com.harriague.curso.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.harriague.curso.Util.JSonParser;
import com.harriague.curso.domain.Joke;

import org.json.JSONException;

import java.io.FileNotFoundException;

public class InfoJokeActivity extends AppCompatActivity {

    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_joke);
        Bundle b = getIntent().getExtras();
        final String idStr = b.getString("id");

        this.sharedpreferences = getSharedPreferences(MainActivity.MY_PREFERENCES, Context.MODE_PRIVATE);

        final FloatingActionButton likeButton = (FloatingActionButton) findViewById(R.id.like);
        assert likeButton != null;

        final FloatingActionButton dislikeButton = (FloatingActionButton) findViewById(R.id.dislike);
        assert dislikeButton != null;

        String idLiked = sharedpreferences.getString(idStr, "");
        if (idLiked.equals("liked")){
            likeButton.setEnabled(false);
            likeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
        }
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(idStr, "liked");
                editor.commit();
                likeButton.setEnabled(false);
                likeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
                dislikeButton.setEnabled(true);
                dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
                //TODO Send like to the server
                //TODO Update likes on existing list
            }
        });



        String idDisliked = sharedpreferences.getString(idStr, "");
        if (idDisliked.equals("disliked")){
            dislikeButton.setEnabled(false);
            dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
        }
        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(idStr, "disliked");
                editor.commit();
                dislikeButton.setEnabled(false);
                dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
                likeButton.setEnabled(true);
                likeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00897B")));
                //TODO Send dislike to the server
                //TODO Update dislikes on existing list
            }
        });

        try {
            //TODO Take JSon from server instead of file
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
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        final FloatingActionButton shareButton = (FloatingActionButton) findViewById(R.id.share);
        assert shareButton != null;
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView jokeText = (TextView) findViewById(R.id.joke_text);
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Joke by Chistoso");
                i.putExtra(android.content.Intent.EXTRA_TEXT, jokeText.getText().toString() );
                startActivity(Intent.createChooser(i,"Share via"));
            }
        });


        /*
        * Intent i=new Intent(android.content.Intent.ACTION_SEND);
i.setType("text/plain");
i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Subject test");
i.putExtra(android.content.Intent.EXTRA_TEXT, "extra text that you want to put");
startActivity(Intent.createChooser(i,"Share via"));*/


    }

}
