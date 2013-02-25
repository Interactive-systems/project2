package com.cs.helsinki.fi.interactivesystems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

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
	ProgressDialog mProgressDialog;
	XMLParser xmlParser;
	CredentialsReader credenatialsReader;
	private LocationManager mLocManager;

	private static final String FILE_NAME = "database.xml";

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

			//TODO add parsing functionality
			xmlParser = new XMLParser(this);

			//Read credentials from file
			credenatialsReader = new CredentialsReader(this);
			credenatialsReader.readCredentials();

			//if the file does not exists, we need to download it
			if (!checkIfDatabaseExists()) {

				//Show a progress dialog while downloading the database file
				mProgressDialog = new ProgressDialog(MainActivity.this);
				mProgressDialog.setMessage("Please wait");
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setMax(100);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

				DownloadFile downloadFile = new DownloadFile();
				downloadFile.execute("");
			}
			//else we can start parsing the data
			else {
				startXmlParsing();
			}
		}	 
	}

	@Override
	public void onLocationChanged(Location location) {
		TextView tvLocation = (TextView) findViewById(R.id.tv_location);
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		LatLng latLng = new LatLng(latitude, longitude);

		//googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		//googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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

	private boolean checkIfDatabaseExists() {
		FileInputStream input = null;
		try {
			input = openFileInput(FILE_NAME);
			input.close();
		}

		catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	private void startXmlParsing() {
		FileInputStream input = null;
		try {
			input = openFileInput(FILE_NAME);
			List<Entry> list = xmlParser.parse(input);
			
			if (input != null) {
				input.close();
			}
			
			xmlParser.printContents(list);
						
			//get the geocodes for the addresses
			if (list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					Entry e = list.get(i);
					
					if (e != null && e.getAddress() != null) {
						new getGeocodeTask().execute(e.getAddress());
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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
				Log.d("test", "no results on onPostExecute");
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

	/**
	 * This class is used for downloading the database file from the server.
	 */
	private class DownloadFile extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... sUrl) {
			try {
				//set up the connection
				final URL url = new URL(credenatialsReader.getAddress());
				final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				final String auth = new String(credenatialsReader.getUsername() + ":" + credenatialsReader.getPassword());
				connection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString(auth.getBytes(), Base64.URL_SAFE));
				connection.setUseCaches(false);
				connection.setConnectTimeout(5000);
				connection.setDoOutput(true);
				connection.connect();

				//get the file size
				int fileLength = connection.getContentLength();

				//specify where to save it
				FileOutputStream f = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);

				InputStream in = connection.getInputStream();

				byte[] buffer = new byte[1024];
				int len1 = 0;
				long total = 0;

				//write the file to disk
				while ((len1 = in.read(buffer)) > 0) {
					total += len1; //total = total + len1
					publishProgress((int)((total*100)/fileLength));
					f.write(buffer, 0, len1);
				}
				f.close();
				in.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			mProgressDialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);

			mProgressDialog.dismiss();

			startXmlParsing();
		}
	} 
}