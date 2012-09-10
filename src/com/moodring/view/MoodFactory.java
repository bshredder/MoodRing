package com.moodring.view;


public class MoodFactory{
	
	private static MoodFactory instance = null;
	
	private MoodFactory(){	
	}
	
	private Integer CalculateColorFromMood(String s){
		return MoodConverter.Instance().CalculateColorFromMood(s);
	}

	private String CalculateMoodFromColor(Integer i){
		return MoodConverter.Instance().CalculateMoodFromColor(i);
	}	
	
	public static MoodFactory Instance(){
		if( null == instance){
			instance = new MoodFactory();
		}
		return MoodFactory.instance;
	}
	
	public Mood MakeMood(String mood){
		return new Mood(mood, this.CalculateColorFromMood(mood) );
	}
	
	public Mood MakeMood(Integer color){
		return new Mood( this.CalculateMoodFromColor(color), color);
	}
	
	public Mood MakeMood(String mood, Integer color){
		return new Mood(mood, color);
	}
}
