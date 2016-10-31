package com.cordova.jokerapp.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.cordova.jokerapp.R;
import com.cordova.jokerapp.adapters.JokeTextArrayAdapter;
import com.cordova.jokerapp.domain.Joke;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListJokesActivity extends ListActivity  {

    private List<Joke> jokes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        //TODO Pass a parameter called listJokes from the new view
        jokes = (List<Joke>) intent.getSerializableExtra("listJokes");
        setListAdapter(new JokeTextArrayAdapter(this, R.layout.list_item, (jokes == null ? new ArrayList<Joke>() : jokes)));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Joke joke = (Joke) getListAdapter().getItem(position);
        Map<String, List<Joke>> hashJoke = new HashMap<String, List<Joke>>();
        hashJoke.put(joke.getCategory(),(jokes == null ? new ArrayList<Joke>() : jokes));
        Intent showJokeIntent = new Intent(this, InfoJokeActivity.class);
        showJokeIntent.putExtra("joke", joke);
        showJokeIntent.putExtra("listCategory", (Serializable) hashJoke);
        this.startActivity(showJokeIntent);
    }
}
