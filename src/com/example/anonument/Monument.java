package com.example.anonument;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;

/*
 * Monuments are things which you comment on
 */
public class Monument {
	public Location loc = null;
	public int mood_color;
	public String title;
	public float bearing;
	public float dist;
	public int id = 0;
	public Monument(Location loc, int mood_color, String title, int id){
		this.loc = loc;
		this.mood_color = mood_color;
		this.title = title;
		this.id = id;
	}
	static public Monument[] fromJSON(String json){
		JSONObject ms = null;
		JSONArray msa = null;
		Vector<Monument> mv = new Vector<Monument>();
		try {
			ms = new JSONObject(json);			//monuments string
			msa = ms.getJSONArray("monuments");	//monuments string array
			int len = msa.length();
			for(int i = 0; i < len; i++){
				JSONObject jm = msa.getJSONObject(i);	//JSON monument
				//get location from lat/lon
				Location l = new Location("Generated Location");
				l.setLatitude(Double.parseDouble(jm.getString("lat")));
				l.setLongitude(Double.parseDouble(jm.getString("lon")));
				//get color from hex
				int c = Color.parseColor("#"+jm.getString("mood"));
				mv.add(new Monument(l, c, jm.getString("title"), jm.getInt("id")));
			}
			return mv.toArray(new Monument[mv.size()]);
		} catch (JSONException e) {
			Log.d("JSON Error", e.getMessage());
			return new Monument[0];
		}
	}
}
