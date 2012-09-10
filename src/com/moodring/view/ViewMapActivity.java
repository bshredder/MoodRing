package com.moodring.view;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import service.types.Pin;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/********************************************************************************************************************
 * Workflow description
 * 
 * Initialization
 *
 * 		A) When entering ViewMap display a GOOGLEMAP with a MapKeyOverlay centered on the users location. This should
 * 		come up fast to delight the user with a snappy screen switch. 
 * 
 * 		B) Inform the user through a progress dialog that APP is fetching "moods" from server for this location while
 * 			HeatMapSvc is fetching 1st batch of pins
 * 
 * 		C) Draw the batch of pins when you receive them 
 * 
 * 		D) Display on the ViewMap the number of total moods and the number of drawn moods 
 * 
 * 		E) Inform the user the APP will be getting more pins in the background
 * 
 * 		F) Repeat C - E until all pins are recieved
 * 
 * (before calling webservice) On start of activity - get first batch of pins and tell the user what you are doing
 * (webservice returns) Dismiss dialog
 * (notify update thread) Display results 
 * (before calling webservice?)If there are more batches - tell the user your going to get them and get them
 * (webservice returns) Dismiss dialog 
 * (notify update thread) Display results
 * 
 * Hashtable is thread safe
 * Semaphore used for predictability
 * 
 ********************************************************************************************************************/

public class ViewMapActivity extends MapActivity 
{
	private MapView mapView;
	private static List<Overlay> mapOverlays;
	private MapController mapController;
	private Hashtable< Location, Integer > moodPointList = new Hashtable<Location, Integer>();	
	private GetMoodsFromServiceTask _asyncTask = null;
	
	// Milwaukee's location
	private static final int HOME_LATITUDE_MICRO_DEGREES = 43038888;
	private static final int HOME_LONGITUDE_MICRO_DEGREES = -87906388;
	GeoPoint homeLocation = new GeoPoint(HOME_LATITUDE_MICRO_DEGREES, HOME_LONGITUDE_MICRO_DEGREES);

	// update statistics
	private float _totalNumMoods = 1;
	private int _totalNumMoodsFetched = 0;	
	TextView _statisticsTray;	
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {	
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mapview);
  
        // get zoom controls
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);        
        
        // center the map at Milwaukee
        // TODO change this to center on your current location
        //homeLocation = new GeoPoint(0,0);
        //if( 0 == homeLocation.getLatitudeE6() || 0 == homeLocation.getLongitudeE6() ){
        //	homeLocation = new GeoPoint(HOME_LATITUDE_MICRO_DEGREES, HOME_LONGITUDE_MICRO_DEGREES);
        //}
        
		_statisticsTray = (TextView) findViewById(R.id.textMoodStats);	        
    }
	
	@Override
	public void onStart(){
		super.onStart();	
		mapController = mapView.getController();
        mapController.setCenter(homeLocation);
		mapOverlays = mapView.getOverlays();        			
		mapOverlays.add(new MapKeyOverlay(this, null, 0, null));          		
		mapView.invalidate();
	}
	
	@Override
	public void onStop(){				

		super.onStop();
		
		mapOverlays.clear();
	}
	
	
	@Override
	public void onResume(){
		
		super.onResume();
		
		HeatMapSvc.Instance().Reset(); // TODO - get rid of state info in service ugh
		
		_totalNumMoods = 1;
		_totalNumMoodsFetched = 0;	
		moodPointList.clear();

		// spin up a background thread to get the moods
		// this could take some time so inform the user what's up periodically
		_asyncTask = new GetMoodsFromServiceTask();		
		_asyncTask.execute(1,2,null);		
		Toast toast = Toast.makeText(this, "Downloading moods ...", Toast.LENGTH_LONG); toast.show();		
	}
	
	@Override
	public void onPause(){				

		super.onPause();
		
		mapOverlays.clear();
		
		// shut down background thread 
		_asyncTask.cancel(true);		
	}	

	
	
    @Override
    protected boolean isRouteDisplayed() 
    {
        return false;
    }
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }	
	
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	boolean bRetval = false;
        switch (item.getItemId()) 
        {	            
	        case R.id.settings_menuItem:        		    		
	        	startActivity(new Intent(this, ChangeSettingsActivity.class));			
	        	bRetval = true;
	        	break;
	            
	        case R.id.home_menuItem:	            
	        	startActivity(new Intent(this, EmotionGridActivity.class));			        
	        	bRetval = true;
	        	break;
        }
        return bRetval;
    }

	 /***************************************************************************************************
	  * Custom AsyncTask to execute webservice calls in background thread
	  * 
	  * 
	  * 
	  ****************************************************************************************************/      
    class GetMoodsFromServiceTask extends AsyncTask<Integer, Integer, Void>{

    	private long _currentMoodNumber = 0;   
    	
		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected void onPostExecute(Void result) {
			this.UpdateUI();
		}
		
		@Override
		protected void onCancelled() {
		}

		@Override
		protected Void doInBackground(Integer... params) {

				try {

					while(isMoreMoods() && !isCancelled() ){	
						ReadPinsFromWebservice();  	
						publishProgress();									
					}	
				}

				catch (Exception e) {
					Log.e("Error", e.toString());
				}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			int percent = 0;
			int justFetched =  moodPointList.size();
			
			_totalNumMoodsFetched = ( _totalNumMoodsFetched + justFetched );			
			
			// still fetching moods
			if (this.isMoreMoods() ) { // can i replace the below if statement with this line ???? so that when no service the "getting 0 of 1" msg doesn't show ???
			//if( (_totalNumMoodsFetched < _totalNumMoods) ){
					_statisticsTray.setText("Downloading " + Integer.toString(_totalNumMoodsFetched) + " of " + Integer.toString( (int)_totalNumMoods) + " emotion samples" );					
			}
			// done fetching moods - display some stats
			else {
				float frequency = HeatMapSvc.Instance().GetHighestFrequencyStatistic().getFreqency();
				float totalSampleSize = (float)HeatMapSvc.Instance().GetTotalValidMoodSamples();
				
				if( _totalNumMoods > 0){
					percent =  Math.round( ( (frequency / totalSampleSize /*_totalNumMoods*/) * 100) );
					_statisticsTray.setText("Strongest emotion in your location is: " + HeatMapSvc.Instance().GetHighestFrequencyStatistic().getMood().getName()  + " " + Integer.toString(percent) + "%" ) ;					
				}
				else{	
					frequency = 0;
					_statisticsTray.setText("There are 0 samples for your location - try increasing the range");			
				}
			}
			
			UpdateUI();
		}
		
        private boolean isMoreMoods(){
        	if(_currentMoodNumber < _totalNumMoods ){
        		return true;
        	}
			 return false;
       }      
        
        private void ReadPinsFromWebservice(){
        	try {    	   	 	    		            		            	

        		ArrayList<Pin> pins =  HeatMapSvc.Instance().GetMoods( 
            			GlobalSettings.Instance().getLocation(),
            			GlobalSettings.Instance().getVisibility(),
            			_currentMoodNumber );
        			 				
            	for( int i = 0; i < pins.size(); i++ ){

        				 Location loc = new Location("");
        				 loc.setLatitude(pins.get(i).getLatitude() * 1000000 );
        				 loc.setLongitude(pins.get(i).getLongitude()  * 1000000 );
	
        				 moodPointList.put( loc, HeatMapSvc.Instance().convertPinToMoodColor(pins.get(i) ));   
    	   					
        				 _totalNumMoods = pins.get(0).getTotalPinCount();		
        				 _currentMoodNumber = pins.get(i).getPinNumber();	 
            	}        		
        	}                	
               	
       		catch (Exception e) {
       				e.printStackTrace();
       		} // end catch          	
        }        
   
        private void UpdateUI(){
			
			 try { 		
				 
				 // get an iterator to the shared Hashtable with service thread (Hashtable is thread safe)
				 Iterator<Hashtable.Entry<Location, Integer>> it = moodPointList.entrySet().iterator();
								
				// if the shared Hashtable has entries - process them
				while (it.hasNext()) {
					
					Entry<Location, Integer> entry = it.next();
         
					// TODO - this is a hack to convert 0,0 locations to "home" location
					GeoPoint point = null;
					if( 0 == (entry.getKey().getLatitude()) ||  0 == (entry.getKey().getLongitude()) ){
						point = new GeoPoint(HOME_LATITUDE_MICRO_DEGREES, HOME_LONGITUDE_MICRO_DEGREES);
					}
					
					else{
						point = new GeoPoint( (int)(entry.getKey().getLatitude()), (int)(entry.getKey().getLongitude()) );            	        		
					}
  	
					// create and add an overlay with mood information 
					HeatMapOverlay hm = new HeatMapOverlay(getBaseContext(), point, 0, entry.getValue());
    	  			mapOverlays.add(hm);      	  
				}  	
				
				// clear the shared Hashtable
				moodPointList.clear();
				
				mapView.postInvalidate();
				
			 } // end try
			 
			 catch (Exception e) {
				e.printStackTrace();					
			} // end catch        	
        }
    
    };    
    
    
}; // end ViewMap Class