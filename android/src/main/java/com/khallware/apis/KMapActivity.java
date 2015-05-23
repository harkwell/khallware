// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import java.util.ArrayList;
import java.util.List;

public class KMapActivity extends MapActivity
{
	public static final String ARG_LATITUDE = "latitude";
	public static final String ARG_LONGITUDE = "longitude";
	public static final int DEF_ZOOM = 6;

	public static class KOverlay extends Overlay
	{
		private List<GeoPoint> geoPoints;
		Bitmap icon = null;

		public KOverlay(List<GeoPoint> geoPoints, Bitmap icon)
		{
			this.geoPoints = geoPoints;
			this.icon = icon;
		}

		@Override
		public boolean draw(Canvas canvas, MapView mapView, 
				boolean shadow, long when) 
		{
			Point point = new Point();
			super.draw(canvas, mapView, shadow);

			for (GeoPoint gp : geoPoints) {
				mapView.getProjection().toPixels(gp, point);
				canvas.drawBitmap(icon,point.x,point.y-50,null);
			}
			return(true);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView view) 
		{  
			boolean retval = false;

			switch (event.getAction()) {
			case 1: // user lifts their finger
				geoPoints.clear();
				geoPoints.add(view.getProjection().fromPixels(
					(int)event.getX(), (int)event.getY()));
				retval = true;
			}
			return(retval);
		}
	}

	protected List<GeoPoint> geoPoints = new ArrayList<>();
	protected MapController mapController = null;
	protected MapView mapView = null;

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.map);
		try {
			Bitmap icon = BitmapFactory.decodeResource(
				getResources(),
				android.R.drawable.star_on);
			mapView = (MapView)findViewById(R.id.mapview);
			mapView.setBuiltInZoomControls(true);
			// mapView.getOverlays().clear();
			mapView.getOverlays().add(new KOverlay(geoPoints,icon));
			mapView.setStreetView(true);
			mapController = mapView.getController();
			mapController.setZoom(DEF_ZOOM);
			mapView.invalidate();
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return(false);
	}

	public void returnResult(View view)
	{
		Intent intent = new Intent();
		GeoPoint geoPoint = (geoPoints.size() > 0)
			? geoPoints.get(0)
			: new GeoPoint(0,0);
		double latitude = (geoPoint.getLatitudeE6() / 1e6);
		double longitude = (geoPoint.getLongitudeE6() / 1e6);
		intent.putExtra(ARG_LATITUDE, ""+latitude);
		intent.putExtra(ARG_LONGITUDE, ""+longitude);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}
}
