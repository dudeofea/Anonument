package com.example.anonument;

import com.example.anonument.R;
import com.example.anonument.util.SystemUiHider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/*
 * Main screen, shows title and button choices
 */
public class FullscreenActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		//attach to layout
		setContentView(R.layout.activity_fullscreen);
		
		//Remove notification bar
	    //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	//Creating a new monument
	public void create_anonument(View view) {
		Intent intent = new Intent(this, CreateAnonumentActivity.class);
		startActivity(intent);
	}
	
	//Finding nearby monuments
		public void find_nearby(View view) {
			Intent intent = new Intent(this, FindActivity.class);
			startActivity(intent);
		}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}
}
