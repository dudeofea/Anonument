package com.denislachance.anonument;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class CreateAnonumentActivity extends AppCompatActivity implements LocationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_create_anonument);

        //hue slider
        final SeekBar hsk = (SeekBar) findViewById(R.id.hueSeekBar);
        hsk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                setHue(progress);
            }
        });
        this.setHue(hsk.getProgress());
        //sat slider
        final SeekBar ssk = (SeekBar) findViewById(R.id.satSeekBar);
        ssk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                setSat(progress);
            }
        });
        this.setSat(ssk.getProgress());
        //Setup GPS
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                500,   // 0.5 sec
                10, this);
        //Setup Heading
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    }

    private int mood_color;
    private Location loc = null;
    private float hue, sat;

    private SensorManager sensorManager;
    float[] aValues = new float[3];
    float[] mValues = new float[3];
    private float bearing = 0;
    private float _oldBearing = 0;
    //use accelerometer / compass to get heading
    private float[] calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        float[] outR = new float[9];

        SensorManager.getRotationMatrix(R, null, aValues, mValues);
        SensorManager.remapCoordinateSystem(R,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                outR);

        SensorManager.getOrientation(outR, values);

        // Convert from Radians to Degrees.
        values[0] = (float) Math.toDegrees(values[0]);
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);

        return values;
    }
    //update bearing on sensor update
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                aValues = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mValues = event.values;

            bearing = calculateOrientation()[0];
            //low pass filter
            float damping = 0.2f;
            bearing = (1-damping) * bearing + (damping) * _oldBearing;
            _oldBearing = bearing;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
    //set the hue of the background
    protected void setHue(int h){
        hue = h;
        mood_color = Color.HSVToColor(new float[]{hue / 2, sat / 100, 0.77f});
        View root = findViewById(R.id.background);
        root.setBackgroundColor(mood_color);

        int text_color = Color.HSVToColor(new float[]{hue/2, Math.max(sat-0.4f, 0.0f)/100, 1f});
        TextView textview = (TextView) findViewById(R.id.mtitle);
        textview.setTextColor(text_color);
    }
    //set the saturation of the background
    protected void setSat(int s){
        sat = s;
        mood_color = Color.HSVToColor(new float[]{hue/2, sat/100, 0.77f});
        View root = findViewById(R.id.background);
        root.setBackgroundColor(mood_color);

        int text_color = Color.HSVToColor(new float[]{hue/2, Math.max(sat-0.4f, 0.0f)/100, 1f});
        TextView textview = (TextView) findViewById(R.id.mtitle);
        textview.setTextColor(text_color);
    }

    //re-init the sensors when starting up again
    @Override
    protected void onResume() {
        super.onResume();

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorEventListener,
                magField,
                SensorManager.SENSOR_DELAY_GAME);
    }

    //create the anonument
    public void send_post_request(View view) {
        if(loc == null){
            return;
        }
        String mood_string = String.format("%06X", (0xFFFFFF & mood_color));
        EditText title = (EditText) findViewById(R.id.title);
        EditText comment = (EditText) findViewById(R.id.comment);
        // Send POST request to server
        HttpPost httppost = new HttpPost(getString(R.string.server_ip_local)+"/hackathon/create_anonument");
        // Add the data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
        nameValuePairs.add(new BasicNameValuePair("mood", mood_string));
        nameValuePairs.add(new BasicNameValuePair("title", title.getText().toString()));
        nameValuePairs.add(new BasicNameValuePair("comment", comment.getText().toString()));
        nameValuePairs.add(new BasicNameValuePair("heading", String.valueOf(bearing)));
        nameValuePairs.add(new BasicNameValuePair("lat", String.valueOf(loc.getLatitude())));
        nameValuePairs.add(new BasicNameValuePair("lon", String.valueOf(loc.getLongitude())));
        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        new HttpPostTask(getApplicationContext()).execute(httppost);
    }

    //Async Class to Send a POST request
    class HttpPostTask extends AsyncTask<HttpPost, Void, HttpResponse> {

        private Context mContext;
        public HttpPostTask (Context context){
            mContext = context;
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

        //function for debugging responses
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
            if(result != null){
                if(result.getStatusLine().getStatusCode()== HttpStatus.SC_OK)
                {
                    String responseBody;
                    try {
                        responseBody = EntityUtils.toString(result.getEntity());
                        debug(responseBody);
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

    //Location Listener Methods
    @Override
    public void onLocationChanged(Location location) {
        if(loc == null){
            TextView label = (TextView) findViewById(R.id.gpsLabel);
            label.setText("Location Found");
            Button postButton = (Button) findViewById(R.id.postButton);
            postButton.setEnabled(true);
        }
        loc = location;
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
