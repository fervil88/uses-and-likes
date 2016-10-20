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

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> listDataChild;
    private SharedPreferences sharedpreferences;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<String>> listChildData) {
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

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }

        final String[] values = childText != null ? childText.split("<->") : new String[]{"1","indefinido","0","0","nada","indefinido","2016/10/19","el chistoso"};

        TextView countLikesText = (TextView) convertView.findViewById(R.id.item_like);
        countLikesText.setText(values[2]);

        TextView countDislikesText = (TextView) convertView.findViewById(R.id.item_dislike);
        countDislikesText.setText(values[3]);

        final TextView subCategoryText = (TextView) convertView.findViewById(R.id.item);
        subCategoryText.setText(values[1]);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showJokeIntent = new Intent(context, InfoJokeActivity.class);
                Bundle bId = new Bundle();
                bId.putString("id", String.valueOf(values[0]));
                showJokeIntent.putExtras(bId);
                Bundle bTitle = new Bundle();
                bTitle.putString("title", String.valueOf(values[1]));
                showJokeIntent.putExtras(bTitle);
                Bundle bLikes = new Bundle();
                bLikes.putInt("likes", Integer.parseInt(values[2]));
                showJokeIntent.putExtras(bLikes);
                Bundle bDislikes = new Bundle();
                bDislikes.putInt("dislikes", Integer.parseInt(values[3]));
                showJokeIntent.putExtras(bDislikes);
                Bundle bJokeText = new Bundle();
                bJokeText.putString("joketext", String.valueOf(values[4]));
                showJokeIntent.putExtras(bJokeText);
                Bundle bCategory = new Bundle();
                bCategory.putString("category", String.valueOf(values[5]));
                showJokeIntent.putExtras(bCategory);
                Bundle bIsDirtyJoke = new Bundle();
                bIsDirtyJoke.putBoolean("isdirtyjoke", Boolean.parseBoolean(values[6]));
                showJokeIntent.putExtras(bIsDirtyJoke);
                Bundle bCreationDate = new Bundle();
                bCreationDate.putString("creationdate", String.valueOf(values[7]));
                showJokeIntent.putExtras(bCreationDate);
                Bundle bUser = new Bundle();
                bUser.putString("user", String.valueOf(values[8]));
                showJokeIntent.putExtras(bUser);
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
