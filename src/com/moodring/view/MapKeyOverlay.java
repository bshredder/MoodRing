package com.moodring.view;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;

public class MapKeyOverlay extends Overlay {

    Geocoder geoCoder = null;
    private Context context;

   public MapKeyOverlay(Context context, GeoPoint geoPoint, int drawable, Integer color) {
    	super();
        this.context = context;
    }

   public boolean onTap(GeoPoint geoPoint, MapView mapView){
        return super.onTap(geoPoint,mapView);
    }

   // TODO check that the method I am overriding is the correct ~return boolean
   @Override 	  
   public void draw(Canvas canvas, MapView mapView, boolean shadow) {
    	
	   super.draw(canvas, mapView, shadow);
	   this.DrawMoodKey(canvas, mapView);
	   
    }
   
   protected void DrawMoodKey(Canvas canvas, MapView mapView){
	   
	   final int MARGIN_SPACE = 5;
	   final int COLOR_BOX_WIDTH = 150; // original == 20;	   
	      
	   // calculate the position of the bounding rectangle for the key 
	   int left = mapView.getLeft();
	   int top  = mapView.getTop();   
	   int center = ( mapView.getBottom() - top ) / 2;  
	   int totalLinearSpace = center - MARGIN_SPACE;			   
		      
	   // setup the bounding rectangle for the key   
	   Rect rect = new Rect();
	   rect.left = left + MARGIN_SPACE; 
	   rect.right = left + COLOR_BOX_WIDTH; 
	   rect.top = top + MARGIN_SPACE; 
	   rect.bottom = rect.top + totalLinearSpace;	   
	   
	   // draw the key on the canvas within the bounding rect
	   Drawable d = this.context.getResources().getDrawable( R.drawable.emotionlist );
	   d.setBounds(rect);
	   d.draw(canvas);    
   }
   
}
