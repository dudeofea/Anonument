package com.denislachance.anonument;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class FindActivity extends AppCompatActivity
        implements  com.google.android.gms.location.LocationListener,
                    GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
    }

    private static final String TAG = FindActivity.class.getSimpleName();
    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks;
    private GoogleApiClient mApiClient;
    private com.google.android.gms.location.LocationListener mContext = this;
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private LocationManager locationManager;
    private Location loc = null;
    private SensorManager sensorManager;
    float bearing = 0, _oldBearing = 0;
    Monument[] nearby;

    //get absolute angular distance between 2 angles
    private float angularDistance(float a, float b){
        //make sure b > a
        if(b < a){
            float tmp = a;
            a = b;
            b = tmp;
        }
        float dist1 = Math.abs(b-a);
        float dist2 = Math.abs(360-b+a);
        return Math.min(dist1, dist2);
    }

    float[] old_acc = {0, 0, 0};
    float[] spd =     {0, 0, 0};
    float[] old_spd = {0, 0, 0};
    float[] pos =     {0, 0, 0};
    long last_timestamp = 0;
    static final float NS2S = 1.0f / 1000000000.0f; //nanoseconds per second
    //use accelerometer to update bearing approximately
    private void updateBearingWithAcc(SensorEvent event){
        //get rid of initial drift
        if(last_timestamp == 0){
            last_timestamp = event.timestamp;
            return;
        }
        float[] acc = event.values;
        float dt = (event.timestamp - last_timestamp) * NS2S;
        //get estimated x position (side to side)
        for (int i = 0; i < spd.length; i++) {
            //high pass filter acceleration
            acc[i] = (0.5f) * acc[i] - (0.5f) * old_acc[i];
            //if(Math.abs(acc[i]) < 0.1){ acc[i] = 0.0f; }
            //integrate for speed
            spd[i] += (acc[i] + old_acc[i])/2 * dt;  //speed is acceleration integral
            //drag speed to settle at 0
            spd[i] *= 0.8;
            if(Math.abs(spd[i]) < 0.005){ spd[i] = 0; }
            //integrate for position
            pos[i] += (spd[i] + old_spd[i])/2 * dt;  //position is speed integral
            old_spd[i] = spd[i];
            old_acc[i] = acc[i];
        }
        last_timestamp = event.timestamp;

        TextView acc1 = (TextView)findViewById(R.id.acc1);
        TextView acc2 = (TextView)findViewById(R.id.acc2);
        TextView acc3 = (TextView)findViewById(R.id.acc3);
        acc1.setText(String.format("%.2f", acc[0]));
        acc2.setText(String.format("%.2f", spd[0]));
        acc3.setText(String.format("%.2f", pos[0]));
    }

    //update bearings on sensor update
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                updateBearingWithAcc(event);

            //TODO: get bearing with lat/lon diff, update with accelerometer

//            //low pass filter
//            float damping = 0.95f;
//            if(jitter_count < 5 && angularDistance(_oldBearing, bearing) > 25){ return; }	//remove jitter
//            jitter_count = 0;
//            //TODO: LPF using similar function to angularDistance
//            bearing = (1-damping) * bearing + (damping) * _oldBearing;
            _oldBearing = bearing;
            NearbyCompassView ncv = (NearbyCompassView)findViewById(R.id.nearbyMonuments);
            ncv.setBearing(bearing);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    @Override
    protected void onResume() {
        super.onResume();

        //--- Setup GPS ---
        //Check for Google Play Services
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            //error
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
            } else {
                Toast.makeText(this, "This device is not supported.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }

        mApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mApiClient.connect();


        //Setup Heading
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorEventListener,
                magField,
                SensorManager.SENSOR_DELAY_GAME);
    }

    //load comments of a nearby monument
    /*public void load_comments(View view) {
        if(nearby == null){
            return;
        }
        final Context me = this;
        if(nearby.length == 1){
            //Open comments for monument
            Intent intent = new Intent(me, CommentActivity.class);
            intent.putExtra("monument_id", nearby[0].id);
            intent.putExtra("monument_title", nearby[0].title);
            intent.putExtra("monument_color", nearby[0].mood_color);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }else if(nearby.length > 1){
            //Open choice dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String[] names = new String[nearby.length];
            for(int i = 0; i < nearby.length; i++){
                names[i] = nearby[i].title + " (" + String.valueOf(Math.round(nearby[i].dist*10.0)/10.0) + "m)";
            }
            builder.setTitle("Which did you mean?")
                    .setItems(names, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int i) {
                            Intent intent = new Intent(me, CommentActivity.class);
                            intent.putExtra("monument_id", nearby[i].id);
                            intent.putExtra("monument_title", nearby[i].title);
                            intent.putExtra("monument_color", nearby[i].mood_color);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    });
            AlertDialog choicePopup = builder.create();
            choicePopup.show();
        }
    }*/

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected");
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        loc = location;
        // Send POST request to server
        HttpPost httppost = new HttpPost(getString(R.string.server_ip_local)+"/hackathon/get_nearby");
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
        //update Nearby Compass View
        NearbyCompassView ncv = (NearbyCompassView)findViewById(R.id.nearbyMonuments);
        ncv.setLocation(location);
        ncv.setBearing(location.getBearing());
        new UpdateMonuments(getApplicationContext(), ncv).execute(httppost);
        //check if some locations can be commented on
        Vector<Monument> can_comment = new Vector<Monument>();
        for(int i = 0; i < ncv.monuments.length; i++){
            if(ncv.monuments[i].dist < 800){
                can_comment.add(ncv.monuments[i]);
            }
        }
        nearby = can_comment.toArray(new Monument[can_comment.size()]);
        Button commentButton = (Button) findViewById(R.id.comment_button);
        TextView bottomlabel = (TextView) findViewById(R.id.message);
        if(nearby.length > 0){
            //show button to comment
            commentButton.setVisibility(View.VISIBLE);
            //hide bottom label
            bottomlabel.setVisibility(View.GONE);
        }else{
            //hide button to comment
            commentButton.setVisibility(View.GONE);
            //show bottom label
            bottomlabel.setVisibility(View.VISIBLE);
        }
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
            if(result != null && result.getStatusLine().getStatusCode()== HttpStatus.SC_OK)
            {
                String responseBody;
                try {
                    responseBody = EntityUtils.toString(result.getEntity());
                    //debug(responseBody);
                    //Create new array of monuments
                    final Monument[] monuments = Monument.fromJSON(responseBody);
                    //update view with new monuments
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ncv.setNearby(monuments);
                            TextView label = (TextView) findViewById(R.id.debug);
                            if(monuments.length > 0){
                                //set middle label
                                label.setText(String.valueOf(monuments.length)+" nearby monuments");
                            }else{
                                //set middle label
                                label.setText(String.valueOf("no nearby monuments :("));
                            }
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
}
