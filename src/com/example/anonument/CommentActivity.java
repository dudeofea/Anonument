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

import com.example.anonument.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

/**
 * Activity to create an anonymous monument with a title, mood, location and first comment
 */
public class CommentActivity extends Activity {
	
	private static final String TAG = FindActivity.class.getSimpleName();
	int monument_id = 0;
	int monument_color = 0;
	String monument_title = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		//attach to layout
		setContentView(R.layout.activity_comment);
		
		//get monument info
		if (savedInstanceState == null) {
		    Bundle extras = getIntent().getExtras();
		    if(extras != null) {
		        monument_id = extras.getInt("monument_id");
		        monument_color = extras.getInt("monument_color");
		        monument_title = extras.getString("monument_title");
		    }
		} else {
		    monument_id = (Integer) savedInstanceState.getSerializable("monument_id");
		    monument_color = (Integer) savedInstanceState.getSerializable("monument_color");
		    monument_title = (String) savedInstanceState.getSerializable("monument_title");
		}
		
		//Set the mood
		View root = findViewById(R.id.background);
		root.setBackgroundColor(monument_color);
		
		TextView title = (TextView) findViewById(R.id.mtitle);
		float[] hsv = new float[3];
		Color.colorToHSV(monument_color, hsv);
		hsv[1] = Math.max(hsv[1]-0.04f, 0.0f);
		hsv[2] = 1f;
		int text_color = Color.HSVToColor(hsv);
		title.setTextColor(text_color);
		
		//Set the title
		title.setText(monument_title);
	}
	
	//Left-right animation on back button
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	@Override
   	protected void onResume() {
   	  super.onResume();
   	}
}
