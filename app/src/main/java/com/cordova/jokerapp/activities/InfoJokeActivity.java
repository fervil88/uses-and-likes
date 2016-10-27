package com.cordova.jokerapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.cordova.jokerapp.R;
import com.cordova.jokerapp.domain.Joke;
import com.cordova.jokerapp.util.RequestBuilder;
import com.cordova.jokerapp.util.Util;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class InfoJokeActivity extends AppCompatActivity {

    private SharedPreferences sharedpreferences;
    private String currentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_joke);
        Intent i = getIntent();

        final Random random = new Random();
        final Joke[] joke = {(Joke) i.getSerializableExtra("joke")};
        final Map<String, List<Joke>> hashCategory = (Map<String, List<Joke>>) i.getSerializableExtra("listCategory");

        final List<Joke> listCategory = hashCategory.get(joke[0].getCategory());
        this.sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);

        updateLike(joke[0]);
        updateDislike(joke[0]);
        updateJoke(joke[0]);

        final FloatingActionButton nextButton = (FloatingActionButton) findViewById(R.id.next_joke);
        assert nextButton != null;
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentId = joke[0].getId();
                while(joke[0].getId() == currentId){
                    int index = random.nextInt(listCategory.size());
                    joke[0] = listCategory.get(index);
                }
                currentId = joke[0].getId();

                updateLike(joke[0]);
                updateDislike(joke[0]);
                updateJoke(joke[0]);
            }
        });

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
    }

    private void updateDislike(final Joke joke) {
        final FloatingActionButton dislikeButton = (FloatingActionButton) findViewById(R.id.dislike);
        assert dislikeButton != null;
        String idDisliked = sharedpreferences.getString(joke.getId(), "");
        if (idDisliked.equals("disliked")){
            dislikeButton.setEnabled(false);
            dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
        } else {
            dislikeButton.setEnabled(true);
            dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
        }

        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(joke.getId(), "disliked");
                editor.commit();
                dislikeButton.setEnabled(false);
                dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));

                RequestBuilder.requestUpdateLike(getBaseContext(), RequestBuilder.URL_JOKE_DISLIKE + joke.getId());
                //TODO Update dislikes on existing list
            }
        });
    }

    private void updateLike(final Joke joke) {
        final FloatingActionButton likeButton = (FloatingActionButton) findViewById(R.id.like);
        assert likeButton != null;

        String idLiked = sharedpreferences.getString(joke.getId(), "");
        if (idLiked.equals("liked")){
            likeButton.setEnabled(false);
            likeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00897B")));
        } else {
            likeButton.setEnabled(true);
            likeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
        }

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(joke.getId(), "liked");
                editor.commit();
                likeButton.setEnabled(false);
                likeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00897B")));
                RequestBuilder.requestUpdateLike(getBaseContext(), RequestBuilder.URL_JOKE_LIKE + joke.getId());
                //TODO Update likes on existing list
            }
        });
    }

    private void updateJoke(Joke joke) {
        //TODO Take JSon from server instead of file
        // Joke joke = new Joke(idStr, b.getString("title"), b.getString("category"), b.getString("joketext"), b.getString("user"), b.getInt("likes"), b.getInt("dislikes"), b.getBoolean("isdirtyjoke"), b.getString("creationdate"));
        if (joke != null) {
            TextView title = (TextView) findViewById(R.id.title_joke);
            title.setText(joke.getTitle());
            TextView category = (TextView) findViewById(R.id.category_joke);
            category.setText(joke.getCategory());
            TextView jokeText = (TextView) findViewById(R.id.joke_text);
            jokeText.setText(joke.getJokeText());
            TextView user = (TextView) findViewById(R.id.user_joke);
            user.setText(joke.getUser());
        }
    }
}
