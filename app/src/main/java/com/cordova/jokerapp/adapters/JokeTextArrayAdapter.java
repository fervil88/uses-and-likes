package com.cordova.jokerapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.cordova.jokerapp.R;
import com.cordova.jokerapp.domain.Joke;

import java.util.List;

/**
 * Created by Emi on 30/10/2016.
 */

public class JokeTextArrayAdapter extends ArrayAdapter<Joke> {
    private final Context context;
    private final List<Joke> jokes;
    private final int resource;
    
    public JokeTextArrayAdapter(Context context, int resource, List<Joke> jokes) {
        super(context, resource, jokes);
        this.context = context;
        this.jokes = jokes;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(resource, parent, false);
        TextView textTitle = (TextView) rowView.findViewById(R.id.item);
        TextView textLikes = (TextView) rowView.findViewById(R.id.item_like);
        TextView textDislikes = (TextView) rowView.findViewById(R.id.item_dislike);

        textTitle.setText(jokes.get(position).getTitle());
        textLikes.setText(jokes.get(position).getLikes());
        textDislikes.setText(jokes.get(position).getDislikes());

        return rowView;
    }
}
