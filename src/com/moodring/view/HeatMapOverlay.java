package com.moodring.view;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Geocoder;

public class HeatMapOverlay extends Overlay {

    Geocoder geoCoder = null;
    
    // Rough approximation - one degree = 50 nautical miles    
    // private static final double MAX_TAP_DISTANCE_KM = 3;
    // private static final double MAX_TAP_DISTANCE_DEGREES = MAX_TAP_DISTANCE_KM * 0.5399568 * 50;
    private final GeoPoint geoPoint;
    private final Integer color;

   public HeatMapOverlay(Context context, GeoPoint geoPoint, int drawable, Integer color) {
    	super();
        this.geoPoint = geoPoint;
        this.color = color;
    }

   public boolean onTap(GeoPoint geoPoint, MapView mapView){
        return super.onTap(geoPoint,mapView);
    }

   @Override 	  
   public void draw(Canvas canvas, MapView mapView, boolean shadow) {
    	
	   super.draw(canvas, mapView, shadow);
    	        
	   // Convert geo coordinates to screen pixels
	   Point screenPoint = new Point();
	   mapView.getProjection().toPixels(geoPoint, screenPoint);
    	    
	   // create a rounded rectangle to use to overlay the map position
	   Paint paint = new Paint();
	   paint.setStyle(Paint.Style.FILL);
	   Rect rect = new Rect();
	   rect.left = screenPoint.x;    
	   rect.right = screenPoint.x + 10;    
	   rect.top = screenPoint.y;    
	   rect.bottom = screenPoint.y + 10;
    	        	    
	   // determine the color based on the type of emotion
	   paint.setColor(this.color);
    	    
	   // determine the transparency based on the magnitude of the emotion
	   paint.setAlpha(50);    	    
    	        
	   // draw the overlay 
	   canvas.drawRect(rect, paint); 
    }
}
