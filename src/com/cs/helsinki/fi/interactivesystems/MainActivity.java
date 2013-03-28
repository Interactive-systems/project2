package com.cs.helsinki.fi.interactivesystems;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements LocationListener, OnInitListener {
	GoogleMap googleMap;
	MarkerOptions markerOptions;
	ProgressDialog mProgressDialog;
	XMLParser xmlParser;
	CredentialsReader credenatialsReader;
	private LocationManager mLocManager;

	private PopupWindow mMarkerInfoWindow;
	private boolean mMarkerInfoWindowVisible = false;
	private Marker mActiveMarker;
	private Map<Marker, Entry> mMarkerMap = new HashMap<Marker, Entry>();
	
    private static final int MENU_ITEM_SHOW_BOOKMARKS = 1;
    private static final int BOOKMARK_LIST_ACTIVITY = 1;
    private static final int VOICE_SEARCH_ACTIVITY = 2;
	 
	private static final String FILE_NAME = "database.xml";
	public static final String COORDS_PREFS = "com.cs.helsinki.fi.interactivesystems.saved_coords";
	public static final String BOOKMARK_PREFS = "com.cs.helsinki.fi.interactivesystems.bookmark";
	
	private int mMapTop;

	private TextToSpeech mTalker;
	private boolean mTalkerReady = false;
	
	private LinearLayout mSearchBar;
	private EditText mSearchText;
	private ImageButton mSearchButton;
	private ImageButton mSpeakButton;
	
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
                    showMarkerInfoWindow(marker, true, false);
                    return true;
                }
			});
			
			mSearchText = (EditText) findViewById(R.id.search_text);
			
			mSearchButton = (ImageButton) findViewById(R.id.search_button);
		    mSearchButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    // parse search params from searchBar EditText text
                    String[] params = parseSearchParams(mSearchText.getText().toString());
                    filterMarkers(params); // search
                }
		        
		    });
		    
		    // measure map top position on the layout (it's right bellow search bar ==> top == search bar height)
		    mSearchBar = (LinearLayout) findViewById(R.id.search_bar);
		    mSearchBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		    mMapTop = mSearchBar.getMeasuredHeight();

		    // init voice search
		    mSpeakButton = (ImageButton) findViewById(R.id.speak_button);
		    PackageManager pm = getPackageManager();
	        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
	        if (activities.size() == 0) { // disable speak button if speech recognizer is not found
	            mSpeakButton.setEnabled(false);
	        } else { // add OnClickListener to speak button
	            mSpeakButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        startActivityForResult(intent, VOICE_SEARCH_ACTIVITY); // start speech recognition widget
                    }
                });
	        }
		    
		    
			mLocManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			String provider = mLocManager.getBestProvider(criteria, true);
			Location location = mLocManager.getLastKnownLocation(provider);

			mTalker = new TextToSpeech(this, this); // init text to speech widget

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
		if(mTalker != null) {
		    mTalker.stop();
		    mTalker.shutdown();
		}
		
		super.onDestroy();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // add menu items
        super.onCreateOptionsMenu(menu);
        menu.addSubMenu(Menu.NONE, MENU_ITEM_SHOW_BOOKMARKS, Menu.NONE, "Show bookmarks");

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case MENU_ITEM_SHOW_BOOKMARKS:
                Intent intent = new Intent(this, BookmarkListActivity.class);
                
                // add bookmarked entries into an arraylist
                ArrayList<Entry> entryList = new ArrayList<Entry>();
                for(Entry e : mMarkerMap.values()) {
                    if(isBookmarked(e.getId())) {
                        entryList.add(e);
                    }
                }
                // start BookmarkListActivity and send the bookmarked entries to it
                intent.putParcelableArrayListExtra("bookmarkEntries", entryList);
                startActivityForResult(intent, BOOKMARK_LIST_ACTIVITY);
                
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
        case BOOKMARK_LIST_ACTIVITY: // receive request from BookmarkListActivity
            if(data != null) {
                Bundle extras = data.getExtras();
                
                // get list of to be deleted bookmarks
                ArrayList<String> deletedBookmarks = extras.getStringArrayList("deletedBookmarks");
                
                // iterate through the list and
                // 1) remove item from bookmarks
                // 2) change marker icons to back to default color
                for(String deleteId : deletedBookmarks) {
                    removeBookmark(deleteId);
                    
                    // iterate through the marker/entry map to find the correct marker so it can be changed back to default
                    for(Map.Entry<Marker, Entry> item : mMarkerMap.entrySet()) {
                        Entry entry = item.getValue();
                        if(entry.getId().equals(deleteId)) { // found the correct marker/entry
                            // replace old marker with changed status
                            Marker marker = item.getKey();
                            replaceMarker(entry, marker);
                            break;
                        }
                    }
                }
                
                // activate marker with given entry id and set map position accordingly
                String id = extras.getString("activeMarker");
                if(id != null) {
                    // must iterate through the marker/entry map to find the correct marker
                    for(Map.Entry<Marker, Entry> item : mMarkerMap.entrySet()) {
                        if(item.getValue().getId().equals(id)) { // found the correct marker/entry
                            showMarkerInfoWindow(item.getKey(), true, true);
                            break;
                        }
                    }                     
                }
            }
            break;
        case VOICE_SEARCH_ACTIVITY: // receive request from speech recognition widget
            if(resultCode == RESULT_OK) {
                // populate wordsList with the String values the speech recognition engine thought it heard
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                
                // if matches were found, filter markers based on the first suggestion from the matches list
                if(matches != null && matches.size() > 0) {
                    String searchStr = matches.get(0); // get first suggestion
                    mSearchText.setText(searchStr); // set search bar EditText text to match the suggested text
                    String[] params = parseSearchParams(searchStr); // parse search params
                    filterMarkers(params); // search
                }
            }
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }

    
    private void filterMarkers(String... filterParams) {
        if(filterParams == null || filterParams.length == 0) {
            for(Map.Entry<Marker, Entry> item : mMarkerMap.entrySet()) {
                Marker marker = item.getKey();
                marker.setVisible(true);
            }
        } else {
            int resultCount = mMarkerMap.size();
            for(Map.Entry<Marker, Entry> item : mMarkerMap.entrySet()) {
                Entry entry = item.getValue();
                String searchStr = entry.getSearchString().toLowerCase(Locale.getDefault());
                
                Marker marker = item.getKey();
                marker.setVisible(true);
                
                // cumulative filtering of markers: each param decreases the amount of visible markers                
                for(String param : filterParams) {
                    if(marker.isVisible() == true) {
                        param.toLowerCase(Locale.getDefault());
                        
                        // param is not found from search string: hide marker from map
                        if(searchStr.indexOf(param) == -1) {
                            marker.setVisible(false);
                            resultCount--;
                            // incase marker has an active info window, remove it as well
                            if(mMarkerInfoWindowVisible && mActiveMarker.getId().equals(marker.getId())) {
                                deleteMarkerInfoWindow();
                            }
                        }
                    }
                }
            }
            String resultStr = resultCount + " search results";
            int ringerMode = ((AudioManager)getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
            
            switch(ringerMode) {
            case AudioManager.RINGER_MODE_NORMAL:
                speak(resultStr); // tell user how many search results were found (with TextToSpeach)
            case AudioManager.RINGER_MODE_VIBRATE: // if ringer mode is on normal or on vibrate
                if(resultCount == 0) { // if no results, do 1 long vibrate 
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(1000);
                } else { // results were found, vibrate 2 times
                    long[] pattern = {0, 300, 100, 300};
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(pattern, -1);
                }
            default: // always show toast with the result count
                Toast searchToast = Toast.makeText(this, resultStr, Toast.LENGTH_SHORT);
                searchToast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 100);
                searchToast.show();              
            }
        }
    }
    
    /**
     * Save bookmark (entry id) to the internal storage
     * 
     * @param id
     */
	private void addBookmark(String id) {
        SharedPreferences settings = getSharedPreferences(BOOKMARK_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(id, true);
        editor.commit();
	}
	
    /**
     * Remove bookmark (entry id) from the internal storage
     * 
     * @param id
     */
	private void removeBookmark(String id) {
        SharedPreferences settings = getSharedPreferences(BOOKMARK_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(id);
        editor.commit();
	}
	
    /**
     * Check from internal storage if entry is bookmarked
     * 
     * @param id
     */
	private boolean isBookmarked(String id) {
        SharedPreferences settings = getSharedPreferences(BOOKMARK_PREFS, MODE_PRIVATE);
        return settings.getBoolean(id, false);
    }
	   
	private String getSavedCoordinates(String entryId) {
		SharedPreferences settings = getSharedPreferences(COORDS_PREFS, 0);
		String coordinates = settings.getString(entryId, "");
		return coordinates;
	}

	private boolean checkIfCoordinatesHaveBeenSaved(String entryId) {
		SharedPreferences settings = getSharedPreferences(COORDS_PREFS, 0);
		String coordinates = settings.getString(entryId, "");

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
	
	private void saveCoordinates(String entryId, Address address) {
		SharedPreferences settings = getSharedPreferences(COORDS_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();

		//save the coordinates as a string using comma as the separator
		String coordinateString = "" + address.getLatitude() + "," + address.getLongitude();
		//save the address info
		//String addressString = address.getAddressLine(0) + ", " + address.getSubAdminArea() + ", " + address.getThoroughfare();

		editor.putString(entryId, coordinateString);

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
	private Marker createMarker(Entry entry, String coordinates) {
	    LatLng latLng = new LatLng(parseLatitude(coordinates), parseLongitude(coordinates));
	    return createMarker(entry, latLng);
	}
	
	/**
     * Create marker on map and "bind" with to an Entry object
     * 
     * @param entry, latLng
     */
	private Marker createMarker(Entry entry, LatLng latLng) {
		markerOptions = new MarkerOptions();
		markerOptions.position(latLng);
		
		// set marker color to orange if it is bookmarked
		if(isBookmarked(entry.getId())) {
		    markerOptions.icon(getBookmarkedMarkerIcon());
		} else { // else set marker color to default
		    markerOptions.icon(getDefaultMarkerIcon());
		}

		Marker marker = googleMap.addMarker(markerOptions);
		mMarkerMap.put(marker, entry); // "bind" entry with the created marker
		
		return marker;
	}
	
	/**
     * Replace marker with changed status
     * 
     * @param entry, marker
     */
	private Marker replaceMarker(Entry entry, Marker marker) {
        mMarkerMap.remove(marker);
        Marker newMarker = createMarker(entry, marker.getPosition());
        
        // if the marker is selected, update its infoWindow
        if(mMarkerInfoWindowVisible && mActiveMarker.getId().equals(marker.getId()) ) {
            updateMarkerInfo(newMarker);
            newMarker.setVisible(true); //marker with active info window is always visible
        } else {
            newMarker.setVisible(marker.isVisible()); // inherit visibility from old marker
        }
        
        marker.remove(); // remove the old marker from map
        
        return newMarker;
	}
	
	private BitmapDescriptor getDefaultMarkerIcon() {
	    return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
	}
	
    private BitmapDescriptor getBookmarkedMarkerIcon() {
        return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
    }

    /**
     * Show marker info window on the map on top of the given marker
     * 
     */
    private void showMarkerInfoWindow(Marker marker, boolean updateMapCameraPosition, final boolean zoom) {
        marker.setVisible(true);
        if(mMarkerInfoWindowVisible) {
            updateMarkerInfo(marker); // active marker has changed, update info
        } else {
            createMarkerInfoWindow(marker); // no info window on screen: create ite
        }

        if(updateMapCameraPosition) {
            // center map to marker's position
            LatLng mapPosition = marker.getPosition();
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(mapPosition), new CancelableCallback() {
                @Override
                public void onCancel() {
                    // do nothing
                }

                @Override
                public void onFinish() {
                    if(zoom) {
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    }
                }
            });            
        }
    }
    
    /**
     * Creates a custom info window and shows it on the screen
     * 
     * @param marker
     */
    private void createMarkerInfoWindow(Marker marker) {
        mMarkerInfoWindowVisible = true;
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
        int y = mMapTop + screenPosition.y - mMarkerInfoWindow.getHeight();
        
        // show info window on screen
        mMarkerInfoWindow.showAtLocation(mapView, Gravity.NO_GRAVITY, x, y);
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
    private void setInfoWindowData(View infoWindow, final Marker marker) {
        String titleText = "";
        String addressText = "";
        final Entry entry = (Entry) mMarkerMap.get(marker);
        
        if(entry != null) {
            titleText = entry.getJob();
            addressText = entry.getAddress();
            
            ImageView bookmark = (ImageView) infoWindow.findViewById(R.id.bookmark);
            
            // use selected state to change bookmark image if the entry is bookmarked
            bookmark.setSelected(isBookmarked(entry.getId()));
            
            // set OnClickListener for bookmark icon
            bookmark.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    if(isBookmarked(entry.getId()) == false) {
                        addBookmark(entry.getId());                      
                    } else {
                        removeBookmark(entry.getId());
                    }
                    
                    // replace old marker with changed status
                    replaceMarker(entry, marker);
                }
            });
        }
        
        // update info window title
        TextView title = (TextView) infoWindow.findViewById(R.id.title);
        title.setText(titleText);
        
        // update info window address
        TextView address = (TextView) infoWindow.findViewById(R.id.address);
        address.setText(addressText);
        
        LinearLayout info = (LinearLayout) infoWindow.findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open browser with the specified url and entry id
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, UrlLibrary.getJobUrl(entry.getId()));
                startActivity(browserIntent); 
            }
        });
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
            int y = mMapTop + screenPosition.y - mMarkerInfoWindow.getHeight();
            
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
						if (checkIfCoordinatesHaveBeenSaved(e.getId())) {
							createMarker(e, getSavedCoordinates(e.getId()));
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
				saveCoordinates(mEntry.getId(), address);
				
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

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) { // successfully inited TextToSpeech engine
            mTalkerReady = true;
        } else {
            Toast.makeText(this, "TTS Initilization Failed", Toast.LENGTH_LONG).show();
        }
    }
    
    // convert text to speech
    public void speak(String str) {
        if(mTalkerReady) {
            mTalker.speak(str, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    
    // parse search params to string array from the given string
    public String[] parseSearchParams(String str) {
        str.trim().replace(" ", ",");
        return str.split("\\s*,\\s*");
    }
}