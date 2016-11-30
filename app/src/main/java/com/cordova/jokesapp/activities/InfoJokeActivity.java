package com.cordova.jokesapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.cordova.jokesapp.R;
import com.cordova.jokesapp.entities.DataBaseHandler;
import com.cordova.jokesapp.entities.Feeling;
import com.cordova.jokesapp.entities.Joke;
import com.cordova.jokesapp.entities.JokerDBOperation;
import com.cordova.jokesapp.util.Util;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InfoJokeActivity extends AppCompatActivity {

    private SharedPreferences sharedpreferences;
    private String currentId;
    InterstitialAd mInterstitialAd;
    private long mLastClickTime = 0;
    private Map<String, List<Joke>> hashCategory;
    private Map<String, List<Joke>> mapJokeToDelete = new TreeMap<String, List<Joke>>();
    private List<Feeling> listFeeling = new ArrayList<Feeling>();;
    final Context context = this;
    private final Random random = new Random();
    private JokerDBOperation jdbc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_joke);
        Intent i = getIntent();

        mapJokeToDelete.clear();
        listFeeling.clear();

        jdbc = DataBaseHandler.getInstance(getApplicationContext());
        final Joke[] joke = {(Joke) i.getSerializableExtra("joke")};
        final String category = i.getStringExtra("category");

        hashCategory = (Map<String, List<Joke>>) i.getSerializableExtra("listCategory");
        final List<Joke> listCategory = hashCategory.get(category);
        this.sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);

        AdView mAdView = (AdView) findViewById(R.id.adView_info);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_info_interstitial));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();

        updateLike(joke[0]);
        updateDislike(joke[0], listCategory);
        updateJoke(joke[0]);

        final FloatingActionButton nextButton = (FloatingActionButton) findViewById(R.id.next_joke);
        assert nextButton != null;
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickOnNext(joke, listCategory, random);
            }
        });

        final FloatingActionButton shareButton = (FloatingActionButton) findViewById(R.id.share);
        assert shareButton != null;
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareButton.setEnabled(false);
                new Handler().postDelayed(new Runnable(){
                    public void run(){
                        shareButton.setEnabled(true);
                    };
                }, 5000);

                TextView jokeText = (TextView) findViewById(R.id.joke_text);
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_SUBJECT,"By JokerApp:");
                i.putExtra(android.content.Intent.EXTRA_TEXT, "By JokerApp:" + jokeText.getText().toString() );
                startActivity(Intent.createChooser(i,"Share via"));
                mLastClickTime = SystemClock.elapsedRealtime();
            }
        });
    }

    private void clickOnNext(Joke[] joke, List<Joke> listCategory, Random random) {
        currentId = joke[0].getId();
        while(listCategory.size() > 1 && joke[0].getId() == currentId){
            int index = random.nextInt(listCategory.size());
            joke[0] = listCategory.get(index);
        }
        currentId = joke[0].getId();

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }

        updateLike(joke[0]);
        updateDislike(joke[0], listCategory);
        updateJoke(joke[0]);
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    private void updateDislike(final Joke joke, final List<Joke> listCategory) {
        final FloatingActionButton dislikeButton = (FloatingActionButton) findViewById(R.id.dislike);
        assert dislikeButton != null;
        Set<String> set = sharedpreferences.getStringSet(joke.getId(), new HashSet<String>());
        if (set.contains("disliked")){ //Dislike button was pressed
            dislikeButton.setImageResource(R.mipmap.red_trash);
            dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0F7FA")));
        } else {
            dislikeButton.setImageResource(R.mipmap.dislike_white);
            dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
        }

        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<String> set = sharedpreferences.getStringSet(joke.getId(), new HashSet<String>());
                if (set.contains("disliked")){ //Dislike button was pressed
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setMessage(R.string.message_delete_joke)
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes_deletion, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    List<Joke> jokes;
                                    if (!mapJokeToDelete.containsKey(joke.getCategory())) {
                                        jokes = new ArrayList<>();
                                    } else {
                                        jokes = mapJokeToDelete.get(joke.getCategory());
                                    }
                                    jokes.add(joke);
                                    mapJokeToDelete.put(joke.getCategory(), jokes);
                                    jdbc.deleteJoke(joke);
                                    jdbc.deleteNewJoke(joke);
                                    jdbc.deleteBestJoke(joke);
                                    Feeling f = new Feeling(joke.getId(), (joke.getLikes()), joke.getDislikes());
                                    listFeeling.remove(f);
                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    editor.remove(joke.getId());
                                    editor.commit();
                                    onBackPressed();
                                }
                            })
                            .setNegativeButton(R.string.no_deletion, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    alertDialogBuilder.create().show();
                } else {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    set.add("disliked");
                    editor.putStringSet(joke.getId(), set);
                    editor.commit();
                    dislikeButton.setImageResource(R.mipmap.red_trash);
                    dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0F7FA")));
                    jdbc.updateJokePlusDislike(joke.getId(), joke.getCategory());
                    jdbc.updateFeelingDislike(joke.getId());
                    Feeling f = new Feeling();
                    f.setId(joke.getId());
                    int feelingIndex = listFeeling.indexOf(joke);
                    if(feelingIndex > -1){
                        f = listFeeling.get(feelingIndex);
                    } else {
                        listFeeling.add(f);
                    }
                    f.setDislikes(1);
                }
            }
        });
    }

    private void updateLike(final Joke joke) {
        final FloatingActionButton likeButton = (FloatingActionButton) findViewById(R.id.like);
        assert likeButton != null;

        Set<String> set = sharedpreferences.getStringSet(joke.getId(), new HashSet<String>());
        if (set.contains("liked")){
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
                Set<String> set = sharedpreferences.getStringSet(joke.getId(), new HashSet<String>());
                set.add("liked");
                editor.putStringSet(joke.getId(), set);
                editor.commit();
                likeButton.setEnabled(false);
                likeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00897B")));
                jdbc.updateJokePlusLike(joke.getId(), joke.getCategory());
                jdbc.updateFeelingLike(joke.getId());
                Feeling f = new Feeling();
                f.setId(joke.getId());
                int feelingIndex = listFeeling.indexOf(joke);
                if(feelingIndex > -1){
                    f = listFeeling.get(feelingIndex);
                } else {
                    listFeeling.add(f);
                }
                f.setLikes(1);
            }
        });
    }

    private void updateJoke(Joke joke) {
        //TODO Take JSon from server instead of file
        // Joke joke = new Joke(idStr, b.getString("title"), b.getString("category"), b.getString("joketext"), b.getString("user"), b.getInt("likes"), b.getInt("dislikes"), b.getBoolean("isdirtyjoke"), b.getString("creationdate"));
        if (joke != null) {
            this.setTitle(joke.getTitle());
            TextView category = (TextView) findViewById(R.id.category_joke);
            category.setText(joke.getCategory());
            TextView jokeText = (TextView) findViewById(R.id.joke_text);
            jokeText.setText(joke.getJokeText());
            TextView user = (TextView) findViewById(R.id.user_joke);
            user.setText(joke.getUser());
        }
    }

    @Override
    public void onBackPressed() {
        returnJokesToDelete();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        returnJokesToDelete();
        finish();
        super.onDestroy();
    }

    private void returnJokesToDelete(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("jokesToDelete", (Serializable) mapJokeToDelete);
        returnIntent.putExtra("listFeeling",(Serializable) listFeeling);
        setResult(MainActivity.RESULT_OK,returnIntent);
    }
}
