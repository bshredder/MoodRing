package com.moodring.view;

import java.util.ArrayList;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;


public class EmotionGridAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Mood> moodList = new ArrayList<Mood>();

    public EmotionGridAdapter(Context context, ArrayList<Mood> emotionList) {
        this.context = context;
        this.moodList = emotionList;
    }

    public int getCount() {
        return moodList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv;
        if (convertView == null) {
            //tv = (TextView) LayoutInflater.from(context).inflate(R.layout.mood_textview, parent, false);        
        	tv = new TextView(context);
        }
        else {
            tv = (TextView) convertView;
        } 
        
        tv.setText( this.moodList.get(position).getName() );
        //tv.setTextSize(100);// 20sp
        tv.setLayoutParams(new GridView.LayoutParams(85, 85));        
        tv.setBackgroundColor( this.moodList.get(position).getColor() );
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        //tv.setClickable(true);
        //tv.setFocusableInTouchMode(true);
        return tv;
    }


}
