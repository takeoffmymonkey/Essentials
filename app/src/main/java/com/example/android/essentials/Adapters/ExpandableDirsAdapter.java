package com.example.android.essentials.Adapters;

/**
 * Created by takeoff on 018 18 Jul 17.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.android.essentials.R;

import java.util.ArrayList;

public class ExpandableDirsAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<String> dirs;


    public ExpandableDirsAdapter(Context context, ArrayList<String> dirs) {
        this.context = context;
        this.dirs = dirs;
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return dirs.get(childPosition);
    }


    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        //For new view
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_sub_dirs, null);

        }

        //Get current question's path
        final String dir = (String) getChild(groupPosition, childPosition);

        TextView textView = (TextView) convertView.findViewById(R.id.sub_dirs_text);
        textView.setText(dir);
        textView.setTextSize(18);

        return convertView;
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        return dirs.size();
    }


    @Override
    public Object getGroup(int groupPosition) {
        return "Directories list:";
    }


    @Override
    public int getGroupCount() {
        return 1;
    }


    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        //If view is empty
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.sub_dirs_list_group, null);
        }


        //Set title of header
        String headerTitle = (String) getGroup(groupPosition);
        TextView headerTextView = (TextView) convertView
                .findViewById(R.id.sub_dirs_list_header);
        headerTextView.setTypeface(null, Typeface.BOLD);
        headerTextView.setText(headerTitle);


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

}