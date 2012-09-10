package com.moodring.view;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ChangeSettingsActivity extends Activity implements OnClickListener
{	
	SeekBar _seekbarRange;	
	TextView _textviewRange;	
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // draw the layout
        setContentView( R.layout.change_settings );
           
        // set up handlers
        Button updateSettingsButton = (Button)findViewById(R.id.UpdateSettingsButton);
        updateSettingsButton.setOnClickListener(this);

        Button cancelSettingsButton = (Button)findViewById(R.id.CancelSettingsButton);
        cancelSettingsButton.setOnClickListener(this);    	
             
         _textviewRange = (TextView)findViewById(R.id.textViewActualRange);   
         _seekbarRange = (SeekBar)findViewById(R.id.seekBarRange);  

         // set range
         long visibility = GlobalSettings.Instance().getVisibility();          
         _seekbarRange.setProgress((int) visibility);
 		_textviewRange.setText(visibility + " KM");            	
     
        _seekbarRange.setOnSeekBarChangeListener( new OnSeekBarChangeListener()
        {
        	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        	{                        
        		_textviewRange.setText(progress + " KM");            
        	}
            
        	public void onStartTrackingTouch(SeekBar seekBar){                        
        	}
            
        	public void onStopTrackingTouch(SeekBar seekBar){                        
        	}
        });        
    }

    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    }      
    
    @Override
    protected void onStart()
    {
    	super.onStart();
    }    
    
    @Override
    protected void onStop()
    {
    	super.onStop();
    }            
    
	@Override
	public void onResume(){
		super.onResume();	
		_seekbarRange.setProgress( GetCachedVisibilitySettings() );
	}
	
	@Override
	public void onPause(){				
		super.onPause();	
		
		// UpdateCachedVisibilitySettings() // only do this if the user updates
	}	
    
    // click handler
    public void onClick(View v) 
    {
    	int buttonId = v.getId();
    	Intent intent = null;
    	switch(buttonId)
    	{
	    	case R.id.UpdateSettingsButton:
	    		intent = new Intent();
	    		intent.setClass(ChangeSettingsActivity.this, ViewMapActivity.class);
	    		intent.putExtra("ShouldRefresh", true);
	    		this.UpdateCachedVisibilitySettings();	    		
	    		startActivity(intent);
	    		break;
    		
	    	case R.id.CancelSettingsButton:    		
	    		startActivity(new Intent(this, EmotionGridActivity.class));
	    		break;    	
    	}
    }    
    
    protected void UpdateCachedVisibilitySettings(){    	
    	GlobalSettings.Instance().setVisibility( _seekbarRange.getProgress() );        
    }
    
    protected int GetCachedVisibilitySettings(){    	
    	return (int) GlobalSettings.Instance().getVisibility();
    }    
}