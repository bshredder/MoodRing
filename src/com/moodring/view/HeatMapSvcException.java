package com.moodring.view;


/*
*
*	Custom exception handler for heatmapsvc - translates all exceptions into actionable application exceptions
*		also allows for some flag enabled debugging logging during development
*
*/

public class HeatMapSvcException extends Exception {

	static final long serialVersionUID = 0;

	public HeatMapSvcException(Exception ex) {
		super( ex );
	}
	
	public HeatMapSvcException(){
	}
	
	private void DebugException(Exception ex){
		if( isDebugMode()){
			WriteExceptionToConsole(ex);
			WriteExceptionToLog(ex);
		}
	}
	
	private void WriteExceptionToConsole(Exception ex){
	
	}

	private void WriteExceptionToLog(Exception ex){
	
	}
	
	private boolean isDebugMode(){
		return false;
	}

}