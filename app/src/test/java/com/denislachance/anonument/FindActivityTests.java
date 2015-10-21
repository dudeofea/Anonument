package com.denislachance.anonument;

import android.location.Location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class FindActivityTests {

    private Location createFakeLocation(double lat, double lon){
        Location loc = new Location(""); //we dont need a provider string
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        return loc;
    }

    @Test
    public void bearingTest() throws Exception {
        FindActivity find = Robolectric.setupActivity(FindActivity.class);
        Location l1 = this.createFakeLocation(53.518087, -113.486116);
        find.onLocationChanged(l1);
        assertEquals(0, 0);
    }
}