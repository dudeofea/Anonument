package com.example.anonument;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.example.anonument.CreateAnonumentActivity.HttpPostTask;
import com.example.anonument.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

/**
 * An activity to find nearby monuments and open the comment threads
 */
public class FindActivity extends Activity implements LocationListener {
	
	private LocationManager locationManager;
	private Location loc = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_find);
	}
	
	@Override
   	protected void onResume() {
   	  super.onResume();

	   	//Setup GPS
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
	            3000,   // 3 sec
	            10, this);
   	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public void onLocationChanged(Location location) {
		loc = location;
		// Send POST request to server
	    HttpPost httppost = new HttpPost("http://192.168.1.10:81/hackathon/get_nearby");
        // Add the data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("lat", String.valueOf(loc.getLatitude())));
        nameValuePairs.add(new BasicNameValuePair("lon", String.valueOf(loc.getLongitude())));
        try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        NearbyCompassView ncv = (NearbyCompassView)findViewById(R.id.nearbyMonuments);
        new UpdateMonuments(getApplicationContext(), ncv).execute(httppost);
	}
	
	//Async Class to Send a POST request
	class UpdateMonuments extends AsyncTask<HttpPost, Void, HttpResponse> {
		
		private Context mContext;
		final private NearbyCompassView ncv;
	    public UpdateMonuments (Context context, NearbyCompassView ncv){
	         mContext = context;
	         this.ncv = ncv;
	    }

		@Override
		protected HttpResponse doInBackground(HttpPost... params) {
			HttpPost httppost = params[0];
			HttpClient httpclient = new DefaultHttpClient();
			try{
				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				return response;
			} catch (ClientProtocolException e) {
				debug("ClientProtocolException: "+e.getMessage());
		    } catch (IOException e) {
		    	debug("IOException: "+e.getMessage());
		    }
			return null;
		}
		
		private void debug(final String message){
			final Context cont = mContext;
			runOnUiThread(new Runnable() {
			    public void run() {
			    	Toast debug = Toast.makeText(cont, message, Toast.LENGTH_LONG);
			        debug.show();
			    }
			});
		}
		
		@Override
		protected void onPostExecute(HttpResponse result){
			if(result != null && result.getStatusLine().getStatusCode()==HttpStatus.SC_OK)
	        {
				String responseBody;
				try {
					responseBody = EntityUtils.toString(result.getEntity());
					//debug(responseBody);
					//Create new array of monuments
			        final Monument[] monuments = Monument.fromJSON(responseBody);
			        for(int i = 0; i < monuments.length; i++){
			        	debug(monuments[i].title);
			        }
			        //update view with new monuments
					runOnUiThread(new Runnable() {
					    public void run() {
					    	ncv.setNearby(monuments);
					    }
					});
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
}
