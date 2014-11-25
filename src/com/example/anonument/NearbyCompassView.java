package com.example.anonument;

import java.lang.reflect.Array;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.View;

/*
 * A View for showing all nearby monuments and navigating to them
 */
public class NearbyCompassView extends View {
	private Monument monuments[];
	Paint border_p = new Paint();
	Paint fill_p = new Paint();
	/*
	 * Sets the array of nearby Monuments to a new array
	 */
	public void setNearby(Monument m[]){
		monuments = m;
		this.invalidate();
	}
	private void init(){
		monuments = new Monument[0];
		border_p.setColor(Color.BLACK);
	}
	public NearbyCompassView(Context context) {
		super(context);
		init();
	}
	public NearbyCompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public NearbyCompassView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	protected void onDraw(Canvas canvas) {
		for(int i = 0; i < monuments.length; i++){
			float pos_x = 50f;
			float pos_y = i*50f;
			fill_p.setColor(monuments[i].mood_color);
			canvas.drawCircle(pos_x, pos_y, 13f, border_p);
			canvas.drawCircle(pos_x, pos_y, 9f, fill_p);
		}
	}
}
