package com.moodring.view;

public class MoodStatistic  {
	
	private Mood _mood;
	private int  _frequency = 0;
	
	public MoodStatistic(Mood mood){
		this._mood = mood;
	}
	
	public void Reset(){
		this._frequency = 0;
	}

	public Mood getMood(){return _mood;}
	
	public void setMood(Mood mood){ this._mood = mood;}
	
	public int getFreqency(){return _frequency;}
	
	public void incrementFrequency(){this._frequency++;}
}
