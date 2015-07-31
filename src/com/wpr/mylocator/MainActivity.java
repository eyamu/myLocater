package com.wpr.mylocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

//public class MainActivity extends Activity implements OnMapReadyCallback {
public class MainActivity extends Activity implements LocationListener {

	private Button save, journeyupdate, stop;
	private static MySQLiteHelper jnyHelper;
	private static String dist, dura;
	public static String jouneyMessage;
	private static float speed;

	double lat, lon;

	private GoogleMap googleMap;
	ArrayList<LatLng> markerPoints;

	public static double currentLong, currentLat, longtudes, latitudes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// show error dialog if GoolglePlayServices not available
		if (!isGooglePlayServicesAvailable()) {
			trigerAlarm();
			Toast.makeText(getBaseContext(),
					"You dont have google play service available",
					Toast.LENGTH_LONG).show();
			finish();
		}
		// Initializing
		markerPoints = new ArrayList<LatLng>();
		jnyHelper = new MySQLiteHelper(getBaseContext());

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String bestProvider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(bestProvider);
		longtudes = location.getLongitude();
		latitudes = location.getLatitude();	
		speed = location.getSpeed();
				
		try {
			if (googleMap == null) {
				googleMap = ((MapFragment) getFragmentManager()
						.findFragmentById(R.id.map)).getMap();
			}
			// googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			// googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			// googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			googleMap.getUiSettings().setZoomGesturesEnabled(true);
			googleMap.setMyLocationEnabled(true);
			googleMap.setOnMapClickListener(new OnMapClick());
			googleMap.addMarker(new MarkerOptions().position(markerPoints.get(0)));			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		if (location != null) {
			onLocationChanged(location);
		}
		//locationManager.requestLocationUpdates(bestProvider, 1000 * 60 * 1, 0, this);
		locationManager.requestLocationUpdates(bestProvider, 2000, 0, this);
		
		save = (Button) findViewById(R.id.record);
		journeyupdate = (Button) findViewById(R.id.myjouney);
		stop = (Button) findViewById(R.id.stopTracking);

		stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getBaseContext(), "Journey tracking stoped",
						Toast.LENGTH_SHORT).show();
				finish();
			}

		});

		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String destnCityName = getCityName(currentLat, currentLong);
				String cityName = getCityName(latitudes, longtudes);

				Journey journey = new Journey();
				journey.setLongtudes(currentLat + "");
				journey.setLatitudes(currentLong + "");
				journey.setFrom(cityName);
				journey.setTo(destnCityName);

				jnyHelper.addJourney(journey);

				Toast.makeText(getBaseContext(), "Journey details saved",
						Toast.LENGTH_SHORT).show();
			}
		});

		journeyupdate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Journey journey = jnyHelper.getJourney();
				String mymessage = "From: " + journey.getFrom() + "\nTo: "
						+ journey.getTo() + "\nCurrent Location: "
								+ getCityName(latitudes, longtudes)+ "\n"
						+ getDistance()
						+ "\nTravel Speed: "+speed+" m/s";
				
				jouneyMessage = mymessage;
				Intent intent = new Intent(getBaseContext(),
						JourneyActivity.class);
				startActivity(intent);
			}
		});

	}

	@Override
	public void onLocationChanged(Location location) {
		// TextView locationTv = (TextView) findViewById(R.id.latlongLocation);
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		longtudes = longitude;
		latitudes = latitude;
		speed = location.getSpeed();
		
		LatLng latLng = new LatLng(latitude, longitude);
		if(markerPoints.size()<1){
		googleMap.addMarker(new MarkerOptions().position(latLng));
		markerPoints.add(latLng);		
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
		}
		Journey j = jnyHelper.getJourney();
		 if ((j.getTo().equals(getCityName(latitudes, longtudes)))&&(!j.getTo().equals(""))) {
			 trigerAlarm();
			 String mymessage = "YOUR JOURNEY FROM: " + j.getFrom() + "TO: "
						+ j.getTo() + " IS REACHED";						
				jouneyMessage = mymessage;
//				Intent intent = new Intent(getBaseContext(),
//						JourneyActivity.class);
//				startActivity(intent);			 
		 }
		 
		Toast.makeText(getBaseContext(),
				"Current Location: " + getCityName(latitude, longitude),
				Toast.LENGTH_LONG).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

	private boolean isGooglePlayServicesAvailable() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == status) {
			return true;
		} else {
			GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
			return false;
		}
	}

	private String getCityName(double lat, double lon) {
		String c = "";
		Geocoder gcd = new Geocoder(getBaseContext());
		List<Address> addresses = null;
		try {
			addresses = gcd.getFromLocation(lat, lon, 1);
		} catch (IOException e) {
		}
		if (addresses != null && addresses.size() > 0) {
			Address address = addresses.get(0);
			c = String.format(
					"%s, %s, %s",
					address.getMaxAddressLineIndex() > 0 ? address
							.getAddressLine(0) : "", address.getLocality(),
					address.getCountryName());
		}

		return c;
	}

	private String getDistance() {
		return "Distance: " + dist + " Duration: " + dura;
	}

	private void setDistance(String dis, String dur) {
		dist = dis;
		dura = dur;
	}

	private void trigerAlarm() {
		Intent intent = new Intent(this, MyBroadcastReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				this.getApplicationContext(), 234324243, intent, 0);
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + (0), pendingIntent);

	}

	private void handleGeoPoint(LatLng p) {
		currentLat = p.latitude;
		currentLong = p.longitude;
		String s = "Lat: " + currentLat + " Lon: " + currentLong + " City: "
				+ getCityName(currentLat, currentLong) + " Dist: "
				+ getDistance();
		Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
	}

	class OnMapClick implements OnMapClickListener {
		@Override
		public void onMapClick(LatLng latLng) {
			// Already two locations
			if (markerPoints.size() > 2) {
				markerPoints.remove(1);
				googleMap.clear();
				googleMap.addMarker(new MarkerOptions().position(markerPoints.get(0)));
			}
			// Adding new item to the ArrayList
			markerPoints.add(latLng);
			handleGeoPoint(latLng);
			// Creating a marker
			MarkerOptions markerOptions = new MarkerOptions();
			// Setting the position for the marker
			markerOptions.position(latLng);
			// Setting the title for the marker.
			// This will be displayed on taping the marker
			markerOptions.title(getCityName(latLng.latitude, latLng.longitude));
			/**
			 * For the start location, the color of marker is GREEN and for the
			 * end location, the color of marker is RED.
			 */
			if (markerPoints.size() == 1) {
				markerOptions.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
			} else if (markerPoints.size() == 2) {
				markerOptions.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			}

			// Clears the previously touched position
			//googleMap.clear();
			// Animating to the touched position
			googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
			// Placing a marker on the touched position
			googleMap.addMarker(markerOptions);

			// Checks, whether start and end locations are captured
			if (markerPoints.size() == 2) {
				LatLng origin = markerPoints.get(0);
				LatLng dest = markerPoints.get(1);
				// Getting URL to the Google Directions API
				String url = getDirectionsUrl(origin, dest);
				DownloadTask downloadTask = new DownloadTask();
				// Start downloading json data from Google Directions API
				downloadTask.execute(url);
			}

		}
	}

	private String getDirectionsUrl(LatLng origin, LatLng dest) {
		// Origin of route
		String str_origin = "origin=" + origin.latitude + ","
				+ origin.longitude;
		// Destination of route
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
		// Sensor enabled
		String sensor = "sensor=false";
		// Building the parameters to the web service
		String parameters = str_origin + "&" + str_dest + "&" + sensor;
		// Output format
		String output = "json";
		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + parameters;
		return url;
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);
			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();
			// Connecting to url
			urlConnection.connect();
			// Reading data from url
			iStream = urlConnection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			data = sb.toString();
			br.close();
		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	// Fetches data from url passed
	private class DownloadTask extends AsyncTask<String, Void, String> {
		// Downloading data in non-ui thread
		@Override
		protected String doInBackground(String... url) {
			// For storing data from web service
			String data = "";
			try {
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executes in UI thread, after the execution of
		// doInBackground()
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			ParserTask parserTask = new ParserTask();
			// Invokes the thread for parsing the JSON data
			parserTask.execute(result);
		}

		/** A class to parse the Google Places in JSON format */
		private class ParserTask extends
				AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

			// Parsing the data in non-ui thread
			@Override
			protected List<List<HashMap<String, String>>> doInBackground(
					String... jsonData) {

				JSONObject jObject;
				List<List<HashMap<String, String>>> routes = null;

				try {
					jObject = new JSONObject(jsonData[0]);
					DirectionsJSONParser parser = new DirectionsJSONParser();

					// Starts parsing data
					routes = parser.parse(jObject);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return routes;
			}

			// Executes in UI thread, after the parsing process
			@Override
			protected void onPostExecute(
					List<List<HashMap<String, String>>> result) {
				ArrayList<LatLng> points = null;
				PolylineOptions lineOptions = null;
				// MarkerOptions markerOptions = new MarkerOptions();
				String distance = "";
				String duration = "";

				if (result.size() < 1) {
					Toast.makeText(getBaseContext(), "No Points",
							Toast.LENGTH_SHORT).show();
					return;
				}

				// Traversing through all the routes
				for (int i = 0; i < result.size(); i++) {
					points = new ArrayList<LatLng>();
					lineOptions = new PolylineOptions();

					// Fetching i-th route
					List<HashMap<String, String>> path = result.get(i);

					// Fetching all the points in i-th route
					for (int j = 0; j < path.size(); j++) {
						HashMap<String, String> point = path.get(j);

						if (j == 0) { // Get distance from the list
							distance = (String) point.get("distance");
							continue;
						} else if (j == 1) { // Get duration from the list
							duration = (String) point.get("duration");
							continue;
						}

						double lat = Double.parseDouble(point.get("lat"));
						double lng = Double.parseDouble(point.get("lng"));
						LatLng position = new LatLng(lat, lng);

						points.add(position);
					}

					// Adding all the points in the route to LineOptions
					lineOptions.addAll(points);
					lineOptions.width(2);
					lineOptions.color(Color.RED);
				}
				setDistance(distance, duration);
				// Drawing polyline in the Google Map for the i-th route
				googleMap.addPolyline(lineOptions);
			}
		}

	}

}
