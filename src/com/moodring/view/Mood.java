

package com.moodring.view;


public class Mood{
	
	private String moodName = "default mood";
	private Integer color = 0;
	
	public Mood(String mood, Integer color){
		this.moodName = mood;
		this.color = color;
	}
	
	public String getName(){
		return this.moodName;
	}
	
	public Integer getColor(){
		return this.color;
	}
}