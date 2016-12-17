package com.cordova.jokesapp.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cordova.jokesapp.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emi on 12/11/2016.
 */

public class DataBaseHandler extends SQLiteOpenHelper implements JokerDBOperation {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Jokes.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String BLOB_TYPE = " BLOB";
    private static final String NUMERIC_TYPE = " NUMERIC";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String REAL_TYPE = "REAL";
    private static final String COMMA_SEP = ",";
    private static DataBaseHandler dbInstance = null;

    private static final String SQL_CREATE_FEELINGS =
            "CREATE TABLE IF NOT EXISTS " + Feeling.FeelingTable.TABLE_NAME + " (" +
                    Feeling.FeelingTable._ID + " TEXT PRIMARY KEY," +
                    Feeling.FeelingTable.COLUMN_LIKE + INTEGER_TYPE + COMMA_SEP +
                    Feeling.FeelingTable.COLUMN_DISLIKE + INTEGER_TYPE + " )";

    private static final String SQL_CREATE_JOKES =
            "CREATE TABLE IF NOT EXISTS " + Joke.JokeTable.TABLE_NAME + " (" +
                    Joke.JokeTable._ID + " TEXT PRIMARY KEY," +
                    Joke.JokeTable.COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_CATEGORY + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_TEXT + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_USER + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_LIKE + INTEGER_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_DISLIKE + INTEGER_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_IS_DIRTY + INTEGER_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_CREATION_DATE + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_TAG + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_CHUNK + INTEGER_TYPE + " )";

    private static final String SQL_CREATE_NEW_JOKES =
            "CREATE TABLE IF NOT EXISTS " + Joke.NewJokeTable.TABLE_NAME + " (" +
                    Joke.JokeTable._ID + " TEXT PRIMARY KEY," +
                    Joke.JokeTable.COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_CATEGORY + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_TEXT + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_USER + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_LIKE + INTEGER_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_DISLIKE + INTEGER_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_IS_DIRTY + INTEGER_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_CREATION_DATE + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_TAG + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_CHUNK + INTEGER_TYPE + " )";

    private static final String SQL_CREATE_BEST_JOKES =
            "CREATE TABLE IF NOT EXISTS " + Joke.BestJokeTable.TABLE_NAME + " (" +
                    Joke.JokeTable._ID + " TEXT PRIMARY KEY," +
                    Joke.JokeTable.COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_CATEGORY + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_TEXT + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_USER + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_LIKE + INTEGER_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_DISLIKE + INTEGER_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_IS_DIRTY + INTEGER_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_CREATION_DATE + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_TAG + TEXT_TYPE + COMMA_SEP +
                    Joke.JokeTable.COLUMN_CHUNK + INTEGER_TYPE + " )";

    private static final String SQL_DELETE_FEELINGS =
            "DROP TABLE IF EXISTS " + Feeling.FeelingTable.TABLE_NAME;

    private static final String SQL_DELETE_JOKES =
            "DROP TABLE IF EXISTS " + Joke.JokeTable.TABLE_NAME;

    private static final String SQL_DELETE_NEW_JOKES =
            "DROP TABLE IF EXISTS " + Joke.NewJokeTable.TABLE_NAME;

    private static final String SQL_DELETE_BEST_JOKES =
            "DROP TABLE IF EXISTS " + Joke.BestJokeTable.TABLE_NAME;

    private final String[] allJokesField = {
            Joke.JokeTable._ID,
            Joke.JokeTable.COLUMN_TITLE,
            Joke.JokeTable.COLUMN_CATEGORY,
            Joke.JokeTable.COLUMN_TEXT,
            Joke.JokeTable.COLUMN_USER,
            Joke.JokeTable.COLUMN_LIKE,
            Joke.JokeTable.COLUMN_DISLIKE,
            Joke.JokeTable.COLUMN_IS_DIRTY,
            Joke.JokeTable.COLUMN_CREATION_DATE,
            Joke.JokeTable.COLUMN_TAG,
            Joke.JokeTable.COLUMN_CHUNK
    };

    private final String[] allFeelingField = {
            Joke.JokeTable._ID,
            Joke.JokeTable.COLUMN_LIKE,
            Joke.JokeTable.COLUMN_DISLIKE
    };


    private DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DataBaseHandler getInstance(Context context) {
        if (dbInstance == null)
            dbInstance = new DataBaseHandler(context);
        return dbInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_JOKES);
        db.execSQL(SQL_CREATE_FEELINGS);
        db.execSQL(SQL_CREATE_NEW_JOKES);
        db.execSQL(SQL_CREATE_BEST_JOKES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DATABASE_VERSION && newVersion == DATABASE_VERSION){
            db.execSQL(SQL_DELETE_JOKES);
            db.execSQL(SQL_DELETE_FEELINGS);
            db.execSQL(SQL_DELETE_NEW_JOKES);
            db.execSQL(SQL_DELETE_BEST_JOKES);
            onCreate(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void insert(SQLiteDatabase db, String tableName, ContentValues values) {
        // Inserting Row
        db.insert(tableName, null, values);
        db.close(); // Closing database connection
    }

    // Adding new Feeling
    public void addFeeling(Feeling feeling) {
        ContentValues values = new ContentValues();
        values.put(Feeling.FeelingTable._ID, feeling.getId());
        values.put(Feeling.FeelingTable.COLUMN_LIKE, feeling.getLikes());
        values.put(Feeling.FeelingTable.COLUMN_DISLIKE, feeling.getDislikes());
        insert(this.getWritableDatabase(), Feeling.FeelingTable.TABLE_NAME, values);
    }

    // Getting All Feeling
    public List<Feeling> getAllFeelings() {
        List<Feeling> feellingList = new ArrayList<Feeling>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + Feeling.FeelingTable.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Feeling feeling = new Feeling(cursor.getString(0),cursor.getInt(1),cursor.getInt(2));
                feellingList.add(feeling);
            } while (cursor.moveToNext());
        }
        db.close();
        return feellingList;
    }

    public void deleteAllFeelings() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Feeling.FeelingTable.TABLE_NAME, null, null);
        db.close();
    }

    // Adding new Joke
    private void addJokes(List<Joke> jokes, String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Joke joke: jokes){
            ContentValues values = new ContentValues();
            values.put(Joke.JokeTable._ID, joke.getId());
            values.put(Joke.JokeTable.COLUMN_TITLE, joke.getTitle());
            values.put(Joke.JokeTable.COLUMN_CATEGORY, joke.getCategory());
            values.put(Joke.JokeTable.COLUMN_TEXT, joke.getJokeText());
            values.put(Joke.JokeTable.COLUMN_USER, joke.getUser());
            values.put(Joke.JokeTable.COLUMN_LIKE, joke.getLikes());
            values.put(Joke.JokeTable.COLUMN_DISLIKE, joke.getDislikes());
            values.put(Joke.JokeTable.COLUMN_IS_DIRTY, convertBooleanToInt(joke.isDirtyJoke()));
            values.put(Joke.JokeTable.COLUMN_CREATION_DATE, joke.getCreationDate());
            values.put(Joke.JokeTable.COLUMN_TAG, joke.getTag());
            values.put(Joke.JokeTable.COLUMN_CHUNK, joke.getChunk());
            db.insert(table, null, values);
        }
        db.close();
    }

    // Adding new Joke
    public void addJokes(List<Joke> jokes) {
        addJokes(jokes, Joke.JokeTable.TABLE_NAME);
    }

    // Adding new Joke
    public void addJoke(Joke joke) {
        ContentValues values = new ContentValues();
        values.put(Joke.JokeTable._ID, joke.getId());
        values.put(Joke.JokeTable.COLUMN_TITLE, joke.getTitle());
        values.put(Joke.JokeTable.COLUMN_CATEGORY, joke.getCategory());
        values.put(Joke.JokeTable.COLUMN_TEXT, joke.getJokeText());
        values.put(Joke.JokeTable.COLUMN_USER, joke.getUser());
        values.put(Joke.JokeTable.COLUMN_LIKE, joke.getLikes());
        values.put(Joke.JokeTable.COLUMN_DISLIKE, joke.getDislikes());
        values.put(Joke.JokeTable.COLUMN_IS_DIRTY, convertBooleanToInt(joke.isDirtyJoke()));
        values.put(Joke.JokeTable.COLUMN_CREATION_DATE, joke.getCreationDate());
        values.put(Joke.JokeTable.COLUMN_TAG, joke.getTag());
        values.put(Joke.JokeTable.COLUMN_CHUNK, joke.getChunk());
        insert(this.getWritableDatabase(), Joke.JokeTable.TABLE_NAME, values);
    }

    // Getting All Jokes
    public List<Joke> getAllJokes() {
        return getJokes(Joke.JokeTable.TABLE_NAME);
    }


    // Getting All Feeling
    public List<Joke> getAllJokesByCategory(String category) {
        List<Joke> jokeList = new ArrayList<Joke>();
        // Select All Query
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(Joke.JokeTable.TABLE_NAME, allJokesField, Joke.JokeTable.COLUMN_CATEGORY + " = ?",
                new String[]{category}, null, null, null, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Joke joke = new Joke();
                joke.setId(cursor.getString(0));
                joke.setTitle(cursor.getString(1));
                joke.setCategory(cursor.getString(2));
                joke.setJokeText(cursor.getString(3));
                joke.setUser(cursor.getString(4));
                joke.setLikes(cursor.getInt(5));
                joke.setDislikes(cursor.getInt(6));
                joke.setDirtyJoke(convertIntToBoolean(cursor.getInt(7)));
                joke.setCreationDate(cursor.getString(8));
                joke.setTag(cursor.getString(9));
                joke.setChunk(cursor.getLong(10));
                jokeList.add(joke);
            } while (cursor.moveToNext());
        }
        db.close();
        return jokeList;
    }

    private Feeling getFeeling(Cursor cursor) {
        if (cursor.getCount() == 0)
            return null;

        cursor.moveToFirst();
        Feeling feeling = new Feeling();
        feeling.setId(cursor.getString(0));
        feeling.setLikes(cursor.getInt(1));
        feeling.setDislikes(cursor.getInt(2));
        return feeling;
    }

    private Joke getJoke(Cursor cursor) {
        if (cursor.getCount() == 0)
            return null;

        cursor.moveToFirst();
        Joke joke = new Joke();
        joke.setId(cursor.getString(0));
        joke.setCategory(cursor.getString(1));
        joke.setJokeText(cursor.getString(2));
        joke.setUser(cursor.getString(3));
        joke.setLikes(cursor.getInt(4));
        joke.setDislikes(cursor.getInt(5));
        joke.setDirtyJoke(convertIntToBoolean(cursor.getInt(6)));
        joke.setCreationDate(cursor.getString(7));
        joke.setTag(cursor.getString(8));
        joke.setChunk(cursor.getLong(9));
        return joke;
    }


    // Getting Joke
    public Joke getJokeById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Joke.JokeTable.TABLE_NAME, allJokesField, Joke.JokeTable._ID + " = ?",
                new String[]{id}, null, null, null, null);
        Joke joke = getJoke(cursor);
        db.close();
        return joke;
    }

    // Getting Feeling
    public Feeling getFeelingById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Feeling.FeelingTable.TABLE_NAME, allFeelingField, Joke.JokeTable._ID + " = ?",
                new String[]{id}, null, null, null, null);
        Feeling feeling = getFeeling(cursor);
        db.close();
        return feeling;
    }

    public void deleteJoke(Joke joke) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Joke.JokeTable.TABLE_NAME, Joke.JokeTable._ID + " = ?",
                new String[] { joke.getId() });
        db.close();
    }

    // Getting All Categories
    public List<String> getAllCategoriesFromJokes() {
        List<String> categoryList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT DISTINCT "+ Joke.JokeTable.COLUMN_CATEGORY +" FROM " + Joke.JokeTable.TABLE_NAME +
                " ORDER BY "+Joke.JokeTable.COLUMN_CATEGORY + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                categoryList.add(new String(cursor.getString(0)));
            } while (cursor.moveToNext());
        }
        db.close();
        return categoryList;
    }

    public int updateFeelingLike(String id) {
        Feeling feeling = getFeelingById(id);
        SQLiteDatabase db = this.getWritableDatabase();
        if (feeling != null) {
            ContentValues values = new ContentValues();
            values.put(Feeling.FeelingTable.COLUMN_LIKE, 1);

            int count = db.update(Feeling.FeelingTable.TABLE_NAME,values, Feeling.FeelingTable._ID + " = ?", new String[] { id });
            db.close();
            return count;
        }
        addFeeling(new Feeling(id, 1, 0));
        return 1;
    }

    public int updateFeelingDislike(String id) {
        Feeling feeling = getFeelingById(id);
        SQLiteDatabase db = this.getWritableDatabase();
        if (feeling != null) {
            ContentValues values = new ContentValues();
            values.put(Feeling.FeelingTable.COLUMN_DISLIKE, 1);

            int count = db.update(Feeling.FeelingTable.TABLE_NAME,values, Feeling.FeelingTable._ID + " = ?", new String[] { id });
            db.close();
            return count;
        }
        addFeeling(new Feeling(id, 0, 1));
        return 1;
    }

    public int updateJokePlusLike(String id, String category) {
        Joke joke = getJokeById(id);
        SQLiteDatabase db = this.getWritableDatabase();
        String table = Joke.JokeTable.TABLE_NAME;
        if( category.equals(Util.BEST_JOKES)) {
            table = Joke.BestJokeTable.TABLE_NAME;
        }else if( category.equals(Util.NEW_JOKES)) {
            table = Joke.NewJokeTable.TABLE_NAME;
        }
        ContentValues values = new  ContentValues();
        values.put(Joke.JokeTable.COLUMN_LIKE, joke.getLikes() + 1);

        int count = db.update(table,values, Joke.JokeTable._ID + " = ?", new String[] { id });
        db.close();
        return count;
    }

    public int updateJokePlusDislike(String id, String category) {
        Joke joke = getJokeById(id);
        SQLiteDatabase db = this.getWritableDatabase();
        String table = Joke.JokeTable.TABLE_NAME;
        if( category.equals(Util.BEST_JOKES)) {
            table = Joke.BestJokeTable.TABLE_NAME;
        }else if( category.equals(Util.NEW_JOKES)) {
            table = Joke.NewJokeTable.TABLE_NAME;
        }
        ContentValues values = new  ContentValues();
        values.put(Joke.JokeTable.COLUMN_DISLIKE, joke.getDislikes() + 1);

        int count = db.update(table,values, Joke.JokeTable._ID + " = ?", new String[] { id });
        db.close();
        return count;
    }

    private int updateJokeFeel(String id, int likes, int dislikes, String table) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new  ContentValues();
        values.put(Joke.JokeTable.COLUMN_LIKE, likes);
        values.put(Joke.JokeTable.COLUMN_DISLIKE, dislikes);

        int count = db.update(table,values, Joke.JokeTable._ID + " = ?", new String[] { id });
        db.close();
        return count;
    }

    public int updateJokeFromServer(String id, int likes, int dislikes) {
        return updateJokeFeel(id, likes, dislikes, Feeling.FeelingTable.TABLE_NAME);
    }

    public int updateNewJokeFromServer(String id, int likes, int dislikes) {
        return updateJokeFeel(id, likes, dislikes, Joke.NewJokeTable.TABLE_NAME);
    }

    public int updateBestJokeFromServer(String id, int likes, int dislikes) {
        return updateJokeFeel(id, likes, dislikes, Joke.BestJokeTable.TABLE_NAME);
    }

    // Adding new Joke
    public void addNewJokes(List<Joke> jokes) {
        addJokes(jokes, Joke.NewJokeTable.TABLE_NAME);
    }

    // Getting New Joke
    public Joke getNewJokeById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Joke.NewJokeTable.TABLE_NAME, allJokesField, Joke.NewJokeTable._ID + " = ?",
                new String[]{id}, null, null, null, null);
        Joke joke = getJoke(cursor);
        db.close();
        return joke;
    }

    // Getting All New Jokes
    public List<Joke> getNewJokes() {
        return getJokes(Joke.NewJokeTable.TABLE_NAME);
    }

    // Adding new Joke
    public void addBestJokes(List<Joke> jokes) {
        addJokes(jokes, Joke.BestJokeTable.TABLE_NAME);
    }

    // Getting Best Joke
    public Joke getBestJokeById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Joke.BestJokeTable.TABLE_NAME, allJokesField, Joke.BestJokeTable._ID + " = ?",
                new String[]{id}, null, null, null, null);
        Joke joke = getJoke(cursor);
        db.close();
        return joke;
    }

    // Getting All Best Jokes
    public List<Joke> getBestJokes() {
        return getJokes(Joke.BestJokeTable.TABLE_NAME);
    }

    private List<Joke> getJokes(String table) {
        List<Joke> jokeList = new ArrayList<Joke>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + table;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Joke joke = new Joke();
                joke.setId(cursor.getString(0));
                joke.setCategory(cursor.getString(1));
                joke.setJokeText(cursor.getString(2));
                joke.setUser(cursor.getString(3));
                joke.setLikes(cursor.getInt(4));
                joke.setDislikes(cursor.getInt(5));
                joke.setDirtyJoke(convertIntToBoolean(cursor.getInt(6)));
                joke.setCreationDate(cursor.getString(7));
                joke.setTag(cursor.getString(8));
                joke.setChunk(cursor.getLong(9));
                jokeList.add(joke);
            } while (cursor.moveToNext());
        }
        db.close();
        return jokeList;
    }

    public void deleteNewJoke(Joke joke) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Joke.NewJokeTable.TABLE_NAME, Joke.JokeTable._ID + " = ?",
                new String[] { joke.getId() });
        db.close();
    }

    public void deleteNewJokes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Joke.NewJokeTable.TABLE_NAME, null, null);
        db.close();
    }

    public void deleteBestJoke(Joke joke) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Joke.BestJokeTable.TABLE_NAME, Joke.JokeTable._ID + " = ?",
                new String[] { joke.getId() });
        db.close();
    }

    public void deleteBestJokes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Joke.BestJokeTable.TABLE_NAME, null, null);
        db.close();
    }



    private boolean convertIntToBoolean(int i){
        if (i != 0)
            return true;
        return false;
    }

    private int convertBooleanToInt(boolean b){
        if (b)
            return 1;
        return 0;
    }


}
