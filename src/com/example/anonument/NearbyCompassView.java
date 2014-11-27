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
		int c = Color.parseColor("#33b5e5");
		border_p.setColor(c);
		border_p.setStyle(Paint.Style.STROKE);
		border_p.setStrokeWidth(5.0f);
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
		int cy = canvas.getHeight()/2;
		int cx = canvas.getWidth()/2;
		int rad = cx-20;
		canvas.drawCircle(cx, cy, rad, border_p);
		for(int i = 0; i < monuments.length; i++){
			this.drawMonumentIcon(canvas, monuments[i], i*20, cx, cy, rad);
		}
	}
	private void drawMonumentIcon(Canvas canvas, Monument m, double bearing, int cx, int cy, int rad){
		float pos_x = rad*((float)Math.sin(Math.PI*bearing/180)) + cx;
		float pos_y = -rad*((float)Math.cos(Math.PI*bearing/180)) + cy;
		fill_p.setColor(m.mood_color);
		canvas.drawCircle(pos_x, pos_y, 13f, border_p);
		canvas.drawCircle(pos_x, pos_y, 11f, fill_p);
	}
}
