package com.moodring.view;

import android.location.Location;

public class GlobalSettings 
{
	// defaults
	public static final long defaultVisibility = 1000;
	
	private long m_Visiblity = defaultVisibility;
	private Location location;
	
	protected static GlobalSettings m_Instance = null;
		
	public static GlobalSettings Instance()
	{
		if( null == m_Instance )
		{
			m_Instance = new GlobalSettings();
		}
		return m_Instance;
	}
	
	protected GlobalSettings() {}

	public void setVisibility(long m_Visiblity)
	{
		this.m_Visiblity = m_Visiblity;
	}

	public long getVisibility()
	{
		return m_Visiblity;
	}
	
	public void setLocation(Location l)
	{
		this.location = l;
	}

	public Location getLocation()
	{
		return this.location;
	}
}
