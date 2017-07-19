package com.example.android.essentials;

/**
 * Created by takeoff on 018 18 Jul 17.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<Question> questions;


/*    private List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, String> listDataChild;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, String> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
    }*/


    public ExpandableListAdapter(Context context, ArrayList<Question> questions) {
        this.context = context;
        this.questions = questions;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return questions.get(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {


        final String path = ((Question) getChild(groupPosition, childPosition)).getFilePath();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.sub_list_item, null);
        }

        WebView webView = (WebView) convertView.findViewById(R.id.sub_list_web_view);

        webView.loadUrl("file://" + path);


/*
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                "Essentials/CS/const.htm");
        if (file.exists()) {
            webView.loadUrl("file://" + Environment.getExternalStorageDirectory()
                    + "/Essentials/CS/const.htm");
        } else Log.e("WARNING: ", "File not found");
*/


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


        String headerTitle = ((Question) getGroup(groupPosition)).getQuestion();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.sub_list_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.sub_list_header);
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
}