package com.cordova.jokerapp.activities;

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
import android.widget.Toast;

import com.cordova.jokerapp.R;
import com.cordova.jokerapp.domain.Joke;
import com.cordova.jokerapp.util.RequestBuilder;
import com.cordova.jokerapp.util.Util;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InfoJokeActivity extends AppCompatActivity {

    private SharedPreferences sharedpreferences;
    private String currentId;
    InterstitialAd mInterstitialAd;
    private long mLastClickTime = 0;
    private Map<String, List<Joke>> hashCategory;
    private Map<String, List<Joke>> mapJokeToDelete = new HashMap<String, List<Joke>>();
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_joke);
        mapJokeToDelete.clear();
        Intent i = getIntent();

        final Random random = new Random();
        final Joke[] joke = {(Joke) i.getSerializableExtra("joke")};
        hashCategory = (Map<String, List<Joke>>) i.getSerializableExtra("listCategory");

        final List<Joke> listCategory = hashCategory.get(joke[0].getCategory());
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
                i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Joke by Chistoso");
                i.putExtra(android.content.Intent.EXTRA_TEXT, "By JokerApp:" + jokeText.getText().toString() );
                startActivity(Intent.createChooser(i,"Share via"));
                mLastClickTime = SystemClock.elapsedRealtime();
            }
        });
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
        String idDisliked = sharedpreferences.getString(joke.getId()+"disliked", "");
        if (idDisliked.equals("disliked")){ //Dislike button was pressed
          //  dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
            RequestBuilder.requestUpdateLike(getBaseContext(), RequestBuilder.URL_JOKE_DISLIKE + joke.getId());
            dislikeButton.setImageResource(R.mipmap.red_trash);
            dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0F7FA")));
        } else {
            dislikeButton.setImageResource(R.mipmap.dislike_white);
            dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
        }

        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idDisliked = sharedpreferences.getString(joke.getId()+"disliked", "");
                if (idDisliked.equals("disliked")) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    // set dialog message
                    alertDialogBuilder.setMessage(R.string.message_delete_joke)
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    listCategory.remove(joke);
                                    List<Joke> jokes;
                                    if (!mapJokeToDelete.containsKey(joke.getCategory())) {
                                        jokes = new ArrayList<>();
                                    } else {
                                        jokes = mapJokeToDelete.get(joke.getCategory());
                                    }
                                    jokes.add(joke);
                                    mapJokeToDelete.put(joke.getCategory(), jokes);
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                   dialog.cancel();
                                }
                            });
                    alertDialogBuilder.create().show();
                } else {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(joke.getId()+"disliked", "disliked");
                    editor.commit();
                    RequestBuilder.requestUpdateLike(getBaseContext(), RequestBuilder.URL_JOKE_DISLIKE + joke.getId());
                    dislikeButton.setImageResource(R.mipmap.red_trash);
                    dislikeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0F7FA")));
                    //TODO Update dislikes on existing list
                }
            }
        });
    }

    private void updateLike(final Joke joke) {
        final FloatingActionButton likeButton = (FloatingActionButton) findViewById(R.id.like);
        assert likeButton != null;

        String idLiked = sharedpreferences.getString(joke.getId()+"liked", "");
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
                editor.putString(joke.getId()+"liked", "liked");
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
        setResult(MainActivity.RESULT_OK,returnIntent);
    }
}
