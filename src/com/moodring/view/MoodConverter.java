package com.moodring.view;

import java.util.ArrayList;
import java.util.Hashtable;
import android.graphics.Color;



public class MoodConverter{
	
	private Integer defaultColor = Color.parseColor("#0D0D0D");
	private String defaultMood = "Default Mood";
	private static MoodConverter instance = null;
	private Hashtable< Integer, String > moodTable = null; // get mood from color
	private Hashtable< String, Integer > colorTable = null; // get color from mood
	
	
	private MoodConverter(){	
	}
	
	// color that is returned if no matches are found
	public void SetDefaultColor(Integer i){
		this.defaultColor = i;
	}
	
	// mood that is returned if no matches are found
	public void SetDefaultMood(String i){
		this.defaultMood = i;
	}
	
	
	public Integer CalculateColorFromMood(String s){
		if( colorTable.contains(s)){
			return colorTable.get(s);
		}
		return this.defaultColor;
	}

	public String CalculateMoodFromColor(Integer i){
		if( moodTable.contains(i)){
			return moodTable.get(i);
		}
		return this.defaultMood;
	}	
	
	public boolean isInitialized(){
		if( null == moodTable || null == colorTable){
			return false;
		}
		return true;
	}
	
	public void Initialize(ArrayList<Mood> moodlist){
		this.moodTable = new Hashtable<Integer, String>();
		this.colorTable = new Hashtable<String, Integer>(); 
		for(int i = 0; i < moodlist.size(); i++){
			moodTable.put( moodlist.get(i).getColor(), moodlist.get(i).getName());
			colorTable.put(moodlist.get(i).getName(),  moodlist.get(i).getColor());
		}
	}
	
	public static MoodConverter Instance(){
		if( null == instance){
			instance = new MoodConverter();
		}
		return MoodConverter.instance;
	}
}