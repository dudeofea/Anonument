package com.denislachance.anonument;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

import java.util.Vector;

/*
 * A View for showing all nearby monuments and navigating to them
 */
public class NearbyCompassView extends View {
    public Monument monuments[];
    private Paint border_p = new Paint();
    private Paint text_p = new Paint();
    private Paint fill_p = new Paint();
    private Location loc = null;
    private float device_bearing = 0;
    /*
     * Sets the array of nearby Monuments to a new array
     */
    public void setNearby(Monument m[]){
        monuments = m;
        //recalc bearings & distances
        for(int i = 0; i < monuments.length; i++){
            monuments[i].bearing = loc.bearingTo(monuments[i].loc);
            monuments[i].dist = loc.distanceTo(monuments[i].loc);
        }
        this.invalidate();
    }
    /*
     * updates the compass' location
     */
    public void setLocation(Location l){
        loc = l;
        //recalc bearings & distances
        for(int i = 0; i < monuments.length; i++){
            monuments[i].bearing = loc.bearingTo(monuments[i].loc);
            monuments[i].dist = loc.distanceTo(monuments[i].loc);
        }
        this.invalidate();
    }
    /*
     * update the bearing of the device
     */
    public void setBearing(float b){
        device_bearing = b;
        this.invalidate();
    }
    private void init(){
        monuments = new Monument[0];
        int c = Color.parseColor("#33b5e5");
        border_p.setColor(c);
        border_p.setStyle(Paint.Style.STROKE);
        border_p.setStrokeWidth(5.0f);
        text_p.setColor(Color.BLACK);
        text_p.setStyle(Paint.Style.STROKE);
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
        int rad = cx-40;
        canvas.drawCircle(cx, cy, rad, border_p);
        if(loc == null){ return; };
        //find monuments that have similar bearings
        //Vector<Monument> named = monuments_v;
        //Vector<Monument> new_named = new Vector<Monument>();
        //Vector<Integer> indexes = new Vector<Integer>();
        Monument[] named = monuments;
        Vector<Monument> new_named = new Vector<Monument>();
        Vector<Monument> no_named = new Vector<Monument>();
        int size = named.length;
        while(size > 0){
            int count = 0;
            //find near bearings
            for(int j = 0; j < named.length; j++){
                if(Math.abs(named[0].bearing - named[j].bearing) < 20){
                    no_named.add(named[j]);
                    count++;
                }else if(j > 0){	//don't add the element we're watching
                    new_named.add(named[j]);
                }
            }
            if(count > 1){
                //draw average
                double avg_bearing = 0;
                float avg_color = 0;
                float avg_dist = 0;
                int no_named_size = no_named.size();
                for(int j = 0; j < no_named_size; j++){
                    Monument m = no_named.get(j);
                    avg_bearing += m.bearing;
                    avg_color += m.mood_color;
                    avg_dist +=loc.distanceTo(m.loc);
                }
                avg_bearing /= no_named_size;
                avg_color /= no_named_size;
                avg_dist /= no_named_size;
                this.drawIcon(canvas, (int)avg_color, String.valueOf(count)+" Monuments", this.formatDist(avg_dist), avg_bearing + device_bearing, cx, cy, rad);
            }else if(count == 1){
                //draw the icon
                this.drawMonumentIcon(canvas, named[0], cx, cy, rad);
            }
            //reset arrays
            size = new_named.size();
            named = new_named.toArray(new Monument[size]);
            new_named.removeAllElements();
            no_named.removeAllElements();
        }
    }
    private void drawMonumentIcon(Canvas canvas, Monument m, int cx, int cy, int rad){
        this.drawIcon(canvas,
                m.mood_color,
                m.title,
                this.formatDist(loc.distanceTo(m.loc)),
                m.bearing + device_bearing,
                cx,
                cy,
                rad);
    }
    private void drawIcon(Canvas canvas, int mood_color, String text1, String text2, double bearing, int cx, int cy, int rad){
        float pos_x = rad*((float)Math.sin(Math.PI*bearing/180)) + cx;
        float pos_y = -rad*((float)Math.cos(Math.PI*bearing/180)) + cy;
        fill_p.setColor(mood_color);
        canvas.drawCircle(pos_x, pos_y, 13f, border_p);
        canvas.drawCircle(pos_x, pos_y, 11f, fill_p);
        float center = text_p.measureText(text1) / 2.0f;
        canvas.drawText(text1, pos_x - center, pos_y - 45, text_p);
        center = text_p.measureText(text2) / 2.0f;
        canvas.drawText(text2, pos_x - center, pos_y - 25, text_p);
    }
    private String formatDist(float dist){
        if(dist < 1000){
            return String.valueOf(Math.round(dist))+"m";
        }
        return String.valueOf(Math.round(dist / 100.0) / 10.0)+"km";
    }
}
