/*
 * MyLocationManager.java
 *
 * Created 2 jan. 2010.
 *
 * awdisk: Android_IridiumFlare.
 * Copyright (c) 2009 Alexandre Wetzel (alexandre@awdisk.com) http://awdisk.com.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.awdisk.android.iridium;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;

/**
 * This class manage the GPS location
 * 
 * This class is mainly inspired by the code of "jagtap.jj1" from <a href=
 * "http://www.anddev.org/app_with_gps-enable_but_the_device_is_indoor-t8997.html"
 * >anddev forom's post</a>
 * 
 * @author jagtap.jj1 and Alexandre Wetzel
 * 
 */
public class MyLocationManager {

	private static final String TAG = "MyLocationManager ";

	private Context mContext = null;
	private LocationManager mLocationManager = null;

	public MyLocationManager(Context context) {

		this.mContext = context;

		if (this.mContext != null) {
			this.mLocationManager = (LocationManager) this.mContext
					.getSystemService(Context.LOCATION_SERVICE);

		}
	}

	LocationListener[] mLocationListeners = new LocationListener[] {
			new LocationListener(LocationManager.GPS_PROVIDER),
			new LocationListener(LocationManager.NETWORK_PROVIDER) };

	private class LocationListener implements android.location.LocationListener {
		Location mLastLocation;
		boolean mValid = false;
		String mProvider;

		public LocationListener(String provider) {
			mProvider = provider;
			mLastLocation = new Location(mProvider);
		}

		public void onLocationChanged(Location newLocation) {
			if (newLocation.getLatitude() == 0.0
					&& newLocation.getLongitude() == 0.0) {
				// Hack to filter out 0.0,0.0 locations
				return;
			}
			if (newLocation != null) {
				// /if(newLocation.getTime() == 0)
				// newLocation.setTime(System.currentTimeMillis());
				newLocation.setTime(System.currentTimeMillis());

				if (Config.DEBUG) {
					Log.i(TAG, "onLocationChanged in loc mgnr");
				}
			}
			mLastLocation.set(newLocation);
			mValid = true;
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
			mValid = false;
			//Log.println(0, "GSP_Status", "Pas de signal gps");
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.OUT_OF_SERVICE) {
				mValid = false;
			}
		}

		public Location current() {
			return mValid ? mLastLocation : null;
		}
	};

	public void startLocationReceiving() {
		if (this.mLocationManager != null) {
			try {
				this.mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 1000, 0F,
						this.mLocationListeners[1]);
			} catch (java.lang.SecurityException ex) {
				if (Config.DEBUG) {
					Log.e(TAG, "SecurityException " + ex.getMessage());
				}
			} catch (IllegalArgumentException ex) {
				// Log.e(TAG, "provider does not exist " + ex.getMessage());
			}
			
			try {
				this.mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 1000, 0F,
						this.mLocationListeners[0]);
			} catch (java.lang.SecurityException ex) {
				if (Config.DEBUG) {
					Log.e(TAG, "SecurityException " + ex.getMessage());
				}
			} catch (IllegalArgumentException ex) {
				// Log.e(TAG, "provider does not exist " + ex.getMessage());
			}
			
		}
	}

	public void stopLocationReceiving() {
		if (this.mLocationManager != null) {
			for (int i = 0; i < this.mLocationListeners.length; i++) {
				try {
					this.mLocationManager.removeUpdates(mLocationListeners[i]);
				} catch (Exception ex) {
					// ok
				}
			}
		}
	}

	public Location getCurrentLocation() {
		Location l = null;

		// go in best to worst order
		for (int i = 0; i < this.mLocationListeners.length; i++) {
			l = this.mLocationListeners[i].current();
			if (l != null)
				break;
		}

		return l;
	}

	/**
	 * Test is the GPS is enabled.
	 * 
	 * @return the status of the GPS
	 */
	public boolean isGPSEnabled() {
		return mLocationManager.isProviderEnabled("gps");
	}
}