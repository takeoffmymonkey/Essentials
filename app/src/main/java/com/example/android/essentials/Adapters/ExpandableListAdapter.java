package com.example.android.essentials.Adapters;

/**
 * Created by takeoff on 018 18 Jul 17.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.example.android.essentials.Question;
import com.example.android.essentials.R;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<Question> questions;


    public ExpandableListAdapter(Context context, ArrayList<Question> questions) {
        this.context = context;
        this.questions = questions;
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return questions.get(groupPosition);
    }


    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        //Get current question's path
        final String path = ((Question) getChild(groupPosition, childPosition)).getFilePath();
        Log.e (TAG, "going to open: " + "file://" + path);

        //For new view
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_sub_list, null);
        }

        //Make webview and load url
        WebView webView = (WebView) convertView.findViewById(R.id.sub_list_web_view);
        webView.loadUrl("file://" + path);

        //Text size and zoomable
        WebSettings webSettings = webView.getSettings();
        webSettings.setTextZoom(140);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        return convertView;
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }


    @Override
    public Object getGroup(int groupPosition) {
        return questions.get(groupPosition);
    }


    @Override
    public int getGroupCount() {
        return questions.size();
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
            convertView = inflater.inflate(R.layout.sub_list_group, null);
        }


        //Set title of header
        String headerTitle = ((Question) getGroup(groupPosition)).getQuestion();
        TextView headerTextView = (TextView) convertView
                .findViewById(R.id.sub_list_header);
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