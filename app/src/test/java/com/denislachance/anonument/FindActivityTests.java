package com.denislachance.anonument;

import android.location.Location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class FindActivityTests {

    private Location createFakeLocation(double lat, double lon, float acc){
        Location loc = new Location(""); //we dont need a provider string
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        loc.setAccuracy(acc);
        return loc;
    }

    @Test
    public void bearingTest1() throws Exception {
        FindActivity find = Robolectric.setupActivity(FindActivity.class);
        Location l1 = this.createFakeLocation(53.518133, -113.497657, 1);
        Location l2 = this.createFakeLocation(53.518133, -113.497505, 1);
        find.loc = l1;
        find.updatePosition(l2);
        float new_b = find.bearing;
        System.out.println(String.valueOf(new_b));
        assert(new_b < -89.0);
        assert(new_b >= -90.0);
    }

    @Test
    public void bearingTest2() throws Exception {
        FindActivity find = Robolectric.setupActivity(FindActivity.class);
        Location l1 = this.createFakeLocation(53.518133, -113.497657, 1);
        Location l2 = this.createFakeLocation(53.518012, -113.497657, 1);
        find.loc = l1;
        find.updatePosition(l2);
        float new_b = find.bearing;
        System.out.println(String.valueOf(new_b));
        assert(new_b == 0.0);
    }

    @Test
    public void postionUpdate1() throws Exception {
        FindActivity find = Robolectric.setupActivity(FindActivity.class);
        //load GPS positions from file and test
        String file_path = "../logs/test_data_oct21.csv";
        BufferedReader br = null;
        String line = "";
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        try {
            br = new BufferedReader(new FileReader(file_path));
            while((line = br.readLine()) != null){
                String[] l_string = line.split(",");
                float lat, lon, acc;
                lat = Float.parseFloat(l_string[0]);
                lon = Float.parseFloat(l_string[1]);
                acc = Float.parseFloat(l_string[2]);
                Location loc = createFakeLocation(lat, lon, acc);
                find.updatePosition(loc);
                System.out.println(String.format("Lat: %f, Lon: %f, Acc: %s Bearing: %f", find.loc.getLatitude(), find.loc.getLongitude(), l_string[2], find.bearing));
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

}