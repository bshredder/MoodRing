package com.moodring.view;

import java.util.ArrayList;
import java.util.Hashtable;

import android.location.Location;
import service.pin.PinService;
import service.types.BasicFilter;
import service.types.BasicPager;
import service.types.Credentials;
import service.types.GetPinsResponse;
import service.types.IPin;
import service.types.Pin;



/*
 * 
 * HeatMapSvc is a proxy to the webservices the application layer uses
 * 
 * Exception handling strategy
 * 	All public interface methods throw exceptions
 * 	All input values tested and throw invalid arg exception
 * 		user should resend a valid value
 *  All web service exceptions thrown as HeatMapSvcException exceptions 
 * 		user should display error and have a response if service is not available
 * 
 */
 
interface IHeatMapSvc{
	
	public void Login( Credentials c ) throws HeatMapSvcException;
	
	public void Initialize( ArrayList<Mood> moods ) throws HeatMapSvcException;

	// writes mood and location
	public void WriteAffectiveState( Mood m, Location l) throws HeatMapSvcException;

	// returns the users importance
	public double GetRank(Location loc) throws HeatMapSvcException;	
	
	// returns a table of the location and color of the pins within range
	public Hashtable<Location, Integer> GetPointsToMap(Location loc, long range ) throws HeatMapSvcException;	
	
	// returns an internally used mood string from a pin title
	public Mood ParseTitleForMood( String title ) throws HeatMapSvcException;
}


public class HeatMapSvc  implements IHeatMapSvc {

	private static String 		DEFAULT_LOGIN		= "thedroid";
	private static String 		DEFAULT_PSWD 		= "thedroid";	
	private static HeatMapSvc 	instance 			= null;
	private Credentials 		cred 				= null;
	private ArrayList<MoodStatistic> _moodStatistics 	= null;


	//
	// private methods
	//
	
	private HeatMapSvc(){
		this.LoginToWebservice();
	}
					
	private void LoginToWebservice(){
		
		if( null == cred ){
			this.LoginToWebserviceAsGuest();
		}
	}
		
	private void LoginToWebserviceAsGuest(){
				
		String _name = DEFAULT_LOGIN;
		String _pswd = DEFAULT_PSWD;		
		
		try{		
			PinService.Instance().Login( _name, _pswd);
		} 
		catch (Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	//
	// Public methods
	//
	
	public Integer convertPinToMoodColor(Pin pin) throws HeatMapSvcException {
		try {
		return this.ParseTitleForMood(pin.getTitle()).getColor();
		} catch (Exception e) {
			e.printStackTrace();
			throw new HeatMapSvcException( e );
		}		
	}	
	
	public MoodStatistic GetHighestFrequencyStatistic(){
	
		int highFrequency = 0;
		int currentFrequency = 0;
		int highFrequencyIndex = 0;   
		
		int arraySize = _moodStatistics.size();
		for( int i = 0; i < arraySize; i++ ){
			currentFrequency = _moodStatistics.get(i).getFreqency();
			
			if( currentFrequency > highFrequency){
				highFrequencyIndex = i;
				highFrequency = currentFrequency;
			}
		}
		return _moodStatistics.get(highFrequencyIndex);
	}	
	
	public int GetTotalValidMoodSamples(){
		int total = 0;
		
		int arraySize = _moodStatistics.size();
		for( int i = 0; i < arraySize; i++ ){
			total = total + _moodStatistics.get(i).getFreqency();
		}		
		return total;
	}
	
	@Override	
	public void Initialize(ArrayList<Mood> moods) throws HeatMapSvcException {
		try{
			int sizeOfArray = moods.size();		
			_moodStatistics = new ArrayList<MoodStatistic>();	
			
			for(int i = 0; i < sizeOfArray; i++){
				_moodStatistics.add(new MoodStatistic( new Mood(moods.get(i).getName(), moods.get(i).getColor()) ) );			
			}
		}	

		catch ( Exception e ){
			e.printStackTrace();			
			throw new HeatMapSvcException(e);
		}
	}
	
	// TODO remove stateful info from service - client should pass a session object	
	public void Reset(){
		int sizeOfArray = _moodStatistics.size();			
	
		for(int i = 0; i < sizeOfArray; i++){
			_moodStatistics.get(i).Reset();
		}						
	}
	
	public static HeatMapSvc Instance(){
		if(null == instance ){
			instance = new HeatMapSvc();
		}
		return instance;
	}	
	
	public boolean isInitialized(){
		if(null == _moodStatistics){
			return false;
		}
		return true;
	}
	
	@Override
	public void WriteAffectiveState(Mood mood, Location l) throws HeatMapSvcException {
			
		IPin pin = new Pin();
		try {
			pin.setTitle( "This place is " +  mood.getName() );
			pin.setLatitude(l.getLatitude());
			pin.setLongitude(l.getLongitude());
			
			PinService.Instance().PutPins(pin);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}	
		

	@Override
	public double GetRank(Location loc) throws HeatMapSvcException {
		try {
			// TODO - test value only - add call to service once feature is turned on
			return 0.88;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HeatMapSvcException( e );
		}		
	}		

	
	
	

	


	// get a page full of moods starting from currentId
	public ArrayList<Pin> GetMoods( 
			Location inloc, 
			long range,
			long currentId
			
	) throws HeatMapSvcException 
	{		
        try {
        	BasicFilter pinFilter = new BasicFilter();
        	pinFilter.setRange(range);
        	pinFilter.setLatitude(inloc.getLatitude());
        	pinFilter.setLongitude(inloc.getLongitude());
        	
        	PinService.Instance().setFilter(pinFilter);

        	BasicPager pager = new BasicPager();
        	pager.setStartPosition(currentId);
        	
        	PinService.Instance().setPager(pager);
        	
        	return((GetPinsResponse) PinService.Instance().GetPins()).getPins();	 		
		} 
			
        catch (Exception e) {
        	e.printStackTrace();
        	throw new HeatMapSvcException( e );
        }
	}	
	
	


	
	
	@Override
	public Hashtable< Location, Integer > GetPointsToMap( Location inloc, long range) throws HeatMapSvcException{
        
		Hashtable<Location, Integer> table  = new Hashtable< Location, Integer >();
		GetPinsResponse pinResponse = null;
		
        try {
        	// filter the moods by range
        	BasicFilter pinFilter = new BasicFilter();
        	pinFilter.setRange(range);
        	pinFilter.setLatitude(inloc.getLatitude());
        	pinFilter.setLongitude(inloc.getLongitude());
        	PinService.Instance().setFilter(pinFilter);
        	
        	pinResponse = (GetPinsResponse) PinService.Instance().GetPins();	 
			ArrayList<Pin> pins = pinResponse.getPins();
			 			
			for( int i = 0; i < pins.size(); i++ ){

				 Location loc = new Location("");
				 loc.setLatitude(pins.get(i).getLatitude() * 1000000 );
				 loc.setLongitude(pins.get(i).getLongitude()  * 1000000 );
				 	 
				 table.put(loc, convertPinToMoodColor(pins.get(i) ) );
			 }
		} 
			
        catch (Exception e) {
        	e.printStackTrace();
        	throw new HeatMapSvcException( e );
        }
		
		return table;            
	}

	@Override
	public void Login(Credentials c) {
		this.cred = c;
	}
        
    
  
	
	@Override
	public Mood ParseTitleForMood(String title) throws HeatMapSvcException {

		Mood type = MoodFactory.Instance().MakeMood("peaceful");		
		boolean isMatch = false;
		int numMoods = this._moodStatistics.size();
		
		try {
			if( this.isInitialized()){
				
				for(int i = 0; i < numMoods; i++){
					
					if( title.contains( this._moodStatistics.get(i).getMood().getName() ) ){
						type = this._moodStatistics.get(i).getMood();
						this._moodStatistics.get(i).incrementFrequency();
						isMatch = true;
						break;
					}
				}
				
				// TODO get rid of this hack - add to resource file legacy moods
				if( !isMatch ){
				
					if( title.contains( "afraid" ) ) {
					type = this._moodStatistics.get(1).getMood();
					this._moodStatistics.get(1).incrementFrequency();
					}
					else if( title.contains( "balanced" ) ) {
						type = this._moodStatistics.get(3).getMood();
						this._moodStatistics.get(3).incrementFrequency();
					}
					else if( title.contains( "none" ) ) {
						type = this._moodStatistics.get(7).getMood();
						this._moodStatistics.get(7).incrementFrequency();
					}						
					else if( title.contains( "depressed" ) ) {
						type = this._moodStatistics.get(8).getMood();
						this._moodStatistics.get(8).incrementFrequency();		
					}
				}
			}
 
		} // end try
		
		catch (Exception e) {
			e.printStackTrace();
			throw new HeatMapSvcException( e );
		}	
		
		return type;
	}

}