package com.moodring.view;

import com.google.ads.*;
import java.util.ArrayList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/*
 * 
 * View of the emotion grid. This is the startup screen
 * 
 * Exception handling
 * 	If the initialization of the HeatMapSvc generates an exception
 * 		set the initialization flag to false 
 * 		notify user that service is not available and to try later
 * 		do the next action if possible else go back to previous state	
 *
 *	User is notified through a toaster message
 *
 * Performance
 * 
 *  Do not make webservice calls on the ui thread
 *  
 * OOO
 * 
 * 	Do not destroy this (do not call finish() ) when switching to map activity
 *  this will allow for faster switching between screens
 *  also will allow background thread to complete 
 *  
 * 
 */

public class EmotionGridActivity extends Activity implements /*OnClickListener,*/ LocationListener, OnItemClickListener
{	

	private static final String MY_AD_UNIT_ID = "a14e7e7574771ec";
	private ArrayList<Mood> moodList = new ArrayList<Mood>();
	private Location currentLocation = new Location("0,0");
	private GestureDetector gestures = null;
	private AdView adView;
	private boolean isInitialized = false;
	private ProgressDialog MoodMapDialog;
	private WriteMoodThread helpThread = null;
	private Mood selectedMood = null;
	

	
	protected void initialize(){

		try {
			
			// read in data from xml file
			Resources res = getResources();
			String[] moods = res.getStringArray(R.array.mood_types_list);		
			TypedArray colors = res.obtainTypedArray(R.array.emotion_colors);
			
			this.moodList.clear();
			for(int i = 0; i < moods.length && i < colors.length(); i++){
				this.moodList.add(new Mood(moods[i], colors.getColor(i,i) ) );
			}	
			
			// init the heatmapsvc
			HeatMapSvc.Instance().Initialize(this.moodList);
			
			// init the conversion object with the values in xml
			MoodConverter.Instance().Initialize(this.moodList);
			
			// initialize the default range for the map here at the 1st launch of the primary app
	     	GlobalSettings.Instance().setVisibility( 75000 );      
			
		} // end try
		
		catch( HeatMapSvcException exHeat){
			this.HandleHeatMapSvcException( exHeat );
		}
		
		this.isInitialized = true; 
	}	
	
	// switch to UI thread
	protected void HandleHeatMapSvcException(Exception e){
		this.isInitialized = false;
		
		EmotionGridActivity.this.runOnUiThread(new Runnable() {
		    public void run() {
		    	Toast toast = Toast.makeText(getBaseContext(), "We're sorry - web services currently unavailable, please try again later.", Toast.LENGTH_LONG);
		    	toast.show(); 
		    }
		}); 		
	}
		
    protected void WriteAffectiveState(final Mood mood)
    { 
    	// if service was not initialized - try all initialization again
    	if( false == this.isInitialized ){
    		this.initialize();
    	}
    	
		try {
			HeatMapSvc.Instance().WriteAffectiveState( mood, currentLocation );
		} catch (HeatMapSvcException e) {
			this.HandleHeatMapSvcException(e);
		}
    }
    
    protected void MoveToMapView(){
		GlobalSettings.Instance().setLocation(this.currentLocation);
		
    	Intent intent = new Intent();
		intent.setClass(EmotionGridActivity.this, ViewMapActivity.class);
		startActivity(intent);   
    }
    
    protected void PostToasterMsg(){
    	Toast toast = Toast.makeText(getBaseContext(), "Calculating the mood at your location", Toast.LENGTH_LONG);
    	toast.show();    	
    }
	
    protected void SetupAdvertising(){
    	
        // Create the adView
        adView = new AdView(this, AdSize.BANNER, MY_AD_UNIT_ID);

        // Lookup your LinearLayout 
        LinearLayout layout = (LinearLayout)findViewById(R.id.DashboardLayout);

        // Add the adView to it
        layout.addView(adView);

        // Initiate a generic request to load it with an ad
        adView.loadAd(new AdRequest());    	
    }
    
    /*
    *  Message handlers
    * 
    */
    
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // check that initialized and logged into webservices successfully
        if( false == this.isInitialized){
        	this.initialize();
        }
        
        // draw the layout
        setContentView( R.layout.emotion_pallet );          
                   
        // setup advertising via AdMob - important -> layout must exist before this call
        this.SetupAdvertising();
                         
    	LocationManager lm = null;
    	try{
    		
            // bind adapter to the gridview
            GridView gridview = (GridView) findViewById(R.id.gridView_emotions);
            gridview.setAdapter(new EmotionGridAdapter(this, this.moodList ) ); 
            
            // setup the on ITEM click listener - looking for grid view's items events
            gridview.setOnItemClickListener(this);    		
    		
    		// handle gestures
    		gestures = new GestureDetector(EmotionGridActivity.this, new GestureListener(this));      		
    		
        	// get the location manager and register for location updates
    		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 120000L, 500.0f, this);
               
    	}
    	catch(Exception e){
			this.HandleHeatMapSvcException( e );
    	}      
    }
   
    @Override    
    public void onStart() 
    {    
    	super.onStart();
        
        if( false == this.isInitialized){
        	this.initialize();
        }    	
    }
    

    @Override
    public void onDestroy() {
      adView.destroy();
      super.onDestroy();
    }
    
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        return gestures.onTouchEvent(event);  
    }      
    
    @Override
    protected void onStop()
    {
    	super.onStop();
    	
    	if( null != helpThread ){    
    		Thread moribund = helpThread;
    		helpThread = null;    
    		moribund.interrupt();
    	}
    	
    }
	
	@Override
	public void onLocationChanged(Location location) {
		this.currentLocation = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
		
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mood_view_menu, menu);
        return true;
    }	
	
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	boolean retVal = false;
    	GlobalSettings.Instance().setLocation(this.currentLocation);
        switch (item.getItemId()) 
        {	            
	        case R.id.quit_menuItem:        		        	
	        	Intent home = new Intent(Intent.ACTION_MAIN);
	        	home.addCategory(Intent.CATEGORY_HOME);
	        	startActivity(home);
	        	finish();
	        	retVal = true;
	        	break;
	            
	        case R.id.map_menuItem:
	        	this.MoveToMapView(); 	   
	        	retVal = true;
	        	break;
	        	
	        case R.id.settings_menuItem:
	        	startActivity(new Intent(this, ChangeSettingsActivity.class));		
	    		retVal = true;
	    		break;
        }
        return retVal;
    }	
	
    /*
     *  Inner class
     * 
     */
    private class GestureListener implements GestureDetector.OnGestureListener,  GestureDetector.OnDoubleTapListener {  

    	EmotionGridActivity view;  

    	public GestureListener(EmotionGridActivity emotionGridActivity) {  
    
    		this.view = emotionGridActivity;  
    	}
    	
    	@Override
    	public boolean onDown(MotionEvent e) {
    		return true;
    	}
    	
    	@Override
    	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    		this.view.MoveToMapView();	
    		return true;
    	}

    	@Override
    	public void onLongPress(MotionEvent e) {
    	}

    	@Override
    	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    		// TODO Auto-generated method stub
    		return true;
    	}

    	@Override
    	public void onShowPress(MotionEvent e) {
    		// TODO Auto-generated method stub
    	}

    	@Override
    	public boolean onSingleTapUp(MotionEvent e) {
    		return true;
    	}

    	@Override
    	public boolean onDoubleTap(MotionEvent e) {
    		MoodMapDialog.dismiss();
    		return true;
    	}

    	@Override
    	public boolean onDoubleTapEvent(MotionEvent e) {
    		return true;
    	}

    	@Override
    	public boolean onSingleTapConfirmed(MotionEvent e) {
    		// TODO Auto-generated method stub
    		return true;
    	}  
    }      // end inner class

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {		
		
		// get the selected mood
		this.selectedMood = MoodFactory.Instance().MakeMood( (String) ((TextView)v).getText()  );
		
		// start thread to write mood to webservice
		helpThread = new WriteMoodThread(this);
		helpThread.start();		
		
		this.MoveToMapView();
	}


	 /** Nested class that performs progress calculations (counting) */
    private class WriteMoodThread extends Thread {
    	private EmotionGridActivity parent = null;

    	WriteMoodThread(EmotionGridActivity context){
    		this.parent = context;
    	}
    	
        public void run() {

        	if(null != parent){
        		parent.WriteAffectiveState( parent.selectedMood );
        	}
        }

    }
	

} // end emotiongridactivity class
