package com.cs.helsinki.fi.interactivesystems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements LocationListener {

	GoogleMap googleMap;
	MarkerOptions markerOptions;
	LatLng latLng;
	private LocationManager mLocManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		//Getting Google Play Services availability status
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

		//Showing the status
		if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
			dialog.show();
		} 
		else { //Google Play Services are available

			//set up the map component
			SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
			googleMap = fm.getMap();
			googleMap.setMyLocationEnabled(true); //the user's location will be tracked

			mLocManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			String provider = mLocManager.getBestProvider(criteria, true);
			Location location = mLocManager.getLastKnownLocation(provider);

			if (location != null) {
				onLocationChanged(location);
			}
			mLocManager.requestLocationUpdates(provider, 20000, 0, this); //receive location updates

			//parse the xml file for real addresses and add the to the arrayList
			ArrayList<String> addresses = new ArrayList<String>();
			addresses.add("Jõe 1, Põltsamaa linn, Jõgevamaa"); //for testing
			addresses.add("Vahtra  17-3, Kohtla-Järve linn, Ida-Virumaa, 30323"); //for testing
			addresses.add("Kõidama küla, Suure-Jaani vald, Viljandimaa, 71504"); //for testing
			
			//get the geocodes for the addresses
			if (addresses != null && addresses.size() > 0) {
				for (int i = 0; i < addresses.size(); i++) {
					new getGeocodeTask().execute(addresses.get(i));
				}
			}
		}	 
	}

	@Override
	public void onLocationChanged(Location location) {
		TextView tvLocation = (TextView) findViewById(R.id.tv_location);
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		LatLng latLng = new LatLng(latitude, longitude);

		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
		tvLocation.setText("Latitude:" +  latitude  + ", Longitude:"+ longitude );
	}

	@Override
	public void onProviderDisabled(String provider) {	
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onDestroy() {
		mLocManager.removeUpdates(this);
		super.onDestroy();
	}

	/**
	 * This class is used for fetching the geocode data from human readable address and for drawing markers
	 * on the specified locations.
	 */
	private class getGeocodeTask extends AsyncTask<String, Void, List<Address>> {

		@Override
		protected List<Address> doInBackground(String... locationName) {
			Geocoder geocoder = new Geocoder(getBaseContext());
			List<Address> addresses = null;

			try {
				addresses = geocoder.getFromLocationName(locationName[0], 1);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return addresses;
		}

		@Override
		protected void onPostExecute(List<Address> addresses) {
			//if no results were found
			if (addresses == null || addresses.size() == 0) {
				return;
			}

			for (int i = 0; i < addresses.size(); i++) {
				//add the marker and text for each marker
				
				Address address = (Address) addresses.get(i);

				latLng = new LatLng(address.getLatitude(), address.getLongitude());
				
				String text = String.format("%s, %s",
		                address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
		                address.getCountryName());

				markerOptions = new MarkerOptions();
				markerOptions.position(latLng);
				markerOptions.title(text);

				googleMap.addMarker(markerOptions);
			}
		}
	}
} 