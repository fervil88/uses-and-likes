package com.harriague.curso.myapplication;

/**
 * Created by Fernando on 9/26/2016.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.harriague.curso.domain.Joke;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<Joke>> listDataChild;
    private SharedPreferences sharedpreferences;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<Joke>> listChildData) {

        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Joke selectedJoke = (Joke) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        TextView countLikesText = (TextView) convertView.findViewById(R.id.item_like);
        countLikesText.setText(selectedJoke.getLikes() + "");

        TextView countDislikesText = (TextView) convertView.findViewById(R.id.item_dislike);
        countDislikesText.setText(selectedJoke.getDislikes() + "");

        final TextView subCategoryText = (TextView) convertView.findViewById(R.id.item);
        subCategoryText.setText(selectedJoke.getTitle());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showJokeIntent = new Intent(context, InfoJokeActivity.class);
                showJokeIntent.putExtra("joke", selectedJoke);
                showJokeIntent.putExtra("listCategory", listDataChild );
                context.startActivity(showJokeIntent);
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.group_header);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setSharedPreference(SharedPreferences sharedpreferences) {
        this.sharedpreferences = sharedpreferences;
    }

}
