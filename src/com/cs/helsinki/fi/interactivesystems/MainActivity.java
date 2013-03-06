package com.cs.helsinki.fi.interactivesystems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements LocationListener {

	GoogleMap googleMap;
	MarkerOptions markerOptions;
	ProgressDialog mProgressDialog;
	XMLParser xmlParser;
	CredentialsReader credenatialsReader;
	private LocationManager mLocManager;

	private PopupWindow mMarkerInfoWindow;
	private boolean mMarkerInfoWindowVisible = false;
	private Marker mActiveMarker;
	private Map<String, Entry> mMarkerMap = new HashMap<String, Entry>();
	
	private static final String FILE_NAME = "database.xml";
	public static final String PREFS_NAME = "MyPrefsFile";

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

			
			// update marker info window position when map camera location changes
			googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition position) {
                    // move marker info window after every camera position change
                    moveMarkerInfoWindow();
                }
			    
			});
			
			// hide marker info window when user clicks on the map
			googleMap.setOnMapClickListener(new OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if(mMarkerInfoWindowVisible) {
                        deleteMarkerInfoWindow(); // hide marker info window
                    }
                }
			});
			
			// show marker info window when user clicks on a marker
			googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if(mMarkerInfoWindowVisible) {
                        updateMarkerInfo(marker); // active marker has changed, update info
                    } else {
                        createMarkerInfoWindow(marker);
                    }
                    
                    // center map to marker's position
                    LatLng mapPosition = marker.getPosition();
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(mapPosition));
                    return true;
                }
			    
			});

			mLocManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			String provider = mLocManager.getBestProvider(criteria, true);
			Location location = mLocManager.getLastKnownLocation(provider);

			if (location != null) {
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				LatLng latLng = new LatLng(latitude, longitude);
				googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
				googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

				onLocationChanged(location);
			}
			mLocManager.requestLocationUpdates(provider, 20000, 0, this); //receive location updates

			
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

	private String getSavedCoordinates(String address) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String coordinates = settings.getString(address, "");
		return coordinates;
	}

	private boolean checkIfCoordinatesHaveBeenSaved(String address) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String coordinates = settings.getString(address, "");

		if (coordinates == null || coordinates.equals("")) {
			return false;
		}
		else {
			return true;
		}
	}
	

	/**
	 * Save the coordinates to the internal storage
	 * 
	 * @param address
	 */
	
	private void saveCoordinates(Address address) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		//save the coordinates as a string using comma as the separator
		String coordinateString = "" + address.getLatitude() + "," + address.getLongitude();
		//save the address info
		String addressString = address.getAddressLine(0) + ", " + address.getSubAdminArea() + ", " + address.getThoroughfare();

		editor.putString(addressString, coordinateString);

		editor.commit();
	}

	/**
	 * Parse the latitude info from the string  
	 * @param coordinates
	 * @return
	 */
	private double parseLatitude(String coordinates) {
		String latString = coordinates.substring(0, coordinates.indexOf(",") - 2);

		try {
			return Double.valueOf(latString);
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Parse the longitude info from the string
	 * @param coordinates
	 * @return
	 */
	private double parseLongitude(String coordinates) {
		String lonString = coordinates.substring(coordinates.indexOf(",") + 1);

		try {
			return Double.valueOf(lonString);
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

   /**
     * Create marker on map and "bind" it to an Entry object
     * 
     * @param entry, coordinates
     */
	private void createMarker(Entry entry, String coordinates) {
	    LatLng latLng = new LatLng(parseLatitude(coordinates), parseLongitude(coordinates));
	    createMarker(entry, latLng);
	}
	
   /**
     * Create marker on map and "bind" with to an Entry object
     * 
     * @param entry, latLng
     */
	private void createMarker(Entry entry, LatLng latLng) {
		markerOptions = new MarkerOptions();
		markerOptions.position(latLng);

		Marker marker = googleMap.addMarker(markerOptions);
		mMarkerMap.put(marker.getId(), entry); // "bind" entry with the created marker
	}

    /**
     * Creates a custom info window and shows it on the screen
     * 
     * @param marker
     */
    private void createMarkerInfoWindow(Marker marker) {
        mActiveMarker = marker;
        
        // create info window for the marker
        View infoWindow = getLayoutInflater().inflate(R.layout.marker_info_window, null);
        
        // set info window data
        setInfoWindowData(infoWindow, marker);
       
        // measure width for the marker info window
        infoWindow.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mMarkerInfoWindow = new PopupWindow(infoWindow, infoWindow.getMeasuredWidth(), infoWindow.getMeasuredHeight());
        mMarkerInfoWindow.setClippingEnabled(false);
        
        View mapView = MainActivity.this.findViewById(R.id.map);
        
        // calculate drawing coords
        LatLng mapPosition = marker.getPosition();
        Projection projection = googleMap.getProjection();
        Point screenPosition = projection.toScreenLocation(mapPosition);
        int x = screenPosition.x - mMarkerInfoWindow.getWidth() / 2;
        int y = screenPosition.y - mMarkerInfoWindow.getHeight() / 2;
        
        // show info window on screen
        mMarkerInfoWindow.showAtLocation(mapView, Gravity.NO_GRAVITY, x, y);
        
        mMarkerInfoWindowVisible = true;
    }
    
    /**
     * Deletes the custom info window
     * 
     */
    private void deleteMarkerInfoWindow() {
        mMarkerInfoWindowVisible = false;
        mActiveMarker = null;
        mMarkerInfoWindow.dismiss();
    }
    
    /**
     * Updates the custom info window
     * 
     * @param marker
     */
    private void updateMarkerInfo(Marker marker) {
        // update data only if different marker has been clicked
        if(mMarkerInfoWindowVisible && !mActiveMarker.getId().equals(marker.getId())) {
            mActiveMarker = marker;
            View infoWindow = mMarkerInfoWindow.getContentView();
            setInfoWindowData(infoWindow, marker);
            
            // measure new width for the marker info window
            infoWindow.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mMarkerInfoWindow.update(infoWindow.getMeasuredWidth(), infoWindow.getMeasuredHeight());            
        }

        moveMarkerInfoWindow();
    }
    
    /**
     * Sets custom info window title and OnClickListeners for the layout icons
     * 
     * @param infoWindow, marker
     */
    private void setInfoWindowData(View infoWindow, Marker marker) {
        String titleText = "";
       
        final Entry entry = (Entry) mMarkerMap.get(marker.getId());
        if(entry != null) {
            titleText = entry.getJob();
            
            // set OnClickListener for info icon
            ImageView info = (ImageView) infoWindow.findViewById(R.id.info);
            info.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    // open browser with the specified url and entry id
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://iseteenindus.tootukassa.ee/toopakkumised/" + entry.getId()));
                    startActivity(browserIntent); 
                }
            });
    
            // set OnClickListener for bookmark icon
            ImageView bookmark = (ImageView) infoWindow.findViewById(R.id.bookmark);
            bookmark.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    // bookmark this entry/marker
                }
            });
        }
        
        // update info window title
        TextView title = (TextView) infoWindow.findViewById(R.id.title);
        title.setText(titleText);       
    }
    
    /**
     * Updates the custom info window location on the screen based on the map
     * position of the currently active marker
     * 
     */
    private void moveMarkerInfoWindow() {
        if(mMarkerInfoWindowVisible && mActiveMarker != null) {
            // calculate drawing coords
            LatLng mapPosition = mActiveMarker.getPosition();
            Projection projection = googleMap.getProjection();
            Point screenPosition = projection.toScreenLocation(mapPosition);
            int x = screenPosition.x - mMarkerInfoWindow.getWidth() / 2;
            int y = screenPosition.y - mMarkerInfoWindow.getHeight() / 2;
            
            // set info window position
            mMarkerInfoWindow.update(x, y, -1, -1);
        }
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

			//get the geocodes for the addresses
			if (list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
				    
					Entry e = list.get(i);
					if (e != null && e.getAddress() != null) {
						//if the coordinates have been saved to internal storage already, fetch them from there
						if (checkIfCoordinatesHaveBeenSaved(e.getAddress())) {
							createMarker(e, getSavedCoordinates(e.getAddress()));
						}
						else { //no coordinates found, so we need to use geocoding
							new getGeocodeTask().execute(e);
						}
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
	private class getGeocodeTask extends AsyncTask<Entry, Void, List<Address>> {
	    private Entry mEntry;
	    
		@Override
		protected List<Address> doInBackground(Entry... entry) {
		    mEntry = entry[0];
		    
			Geocoder geocoder = new Geocoder(getBaseContext());
			List<Address> addresses = null;

			try {
				addresses = geocoder.getFromLocationName(entry[0].getAddress(), 1);
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
				//add the marker

				Address address = (Address) addresses.get(i);

				LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

				//save the address info to internal storage
				saveCoordinates(address);
				
				/*String text = String.format("%s, %s",
						address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
								address.getCountryName());*/
				
				createMarker(mEntry, latLng);
				
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
				connection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP));
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