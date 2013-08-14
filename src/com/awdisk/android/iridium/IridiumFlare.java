package com.awdisk.android.iridium;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
//Date is used to knwo the timezone
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class get the GPS coordinate to send the correct request.
 * 
 * @author <a href="mailto:alexandre@awdisk.com">Alexandre Wetzel</a>
 */
public class IridiumFlare extends Activity implements View.OnClickListener {

	private static String PAYPAL_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=X7BQM79PTEWF8&lc=CH&item_name=Iridium%20Flare%20Forecast%20%2d%20Alexandre%20Wetzel&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted";
	
	private Button localizeMeGPS;
	private Button localizeMeCell;
	private Button ask;
	private Button askIss;

	private TextView latitude;
	private TextView longitude;
	private TextView gmt;
	private EditText latEdit;
	private EditText lonEdit;
	private String address;

	private Double gmtValue;

	private int help = Menu.FIRST;
	private int about = help + 1;
	private int ad = about + 1;
	private int info = ad + 1;
	private int donate = info + 1;
	private MenuItem helpItm;
	private MenuItem aboutItm;
	private MenuItem adItm;
	private MenuItem infoItm;
	private boolean isAdDisabled = false;
	private MenuItem donateItm;

	private String HELP_MESSAGE;
	private String ABOUT_MESSAGE;

	private MyLocationManager m;

	private GsmCellLocation location;
	private ProgressDialog myProgressDialog;
	private int cellID, lac;
	private double lat, lng;
	private Handler uiCallback;
	private Location loc;
	
	public final String option = "OPTION";
	
	private TextView alt;
	private EditText altEdit;
	
	private Intent intent;

	private SharedPreferences mySharedPreferences;
	private SharedPreferences.Editor editor;
	
	private final String AD_ENABLE = "isAdEnable";
	private final String LATITUDE_OLD = "lastLatitude";
	private final String LONGITUDE_OLD = "lastLongitude";
	private final String ALTITUDE_OLD = "lastAltitude";
	
	private String versionCode;
	
	private AdView adView;
	private LinearLayout adLayout;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		// Create the adView
		//adView = new AdView(this, AdSize.BANNER, "a14bcae286a490b");

		// Lookup your LinearLayout assuming it’s been given
		// the attribute android:id="@+id/mainLayout"
		//adLayout = (LinearLayout) findViewById(R.id.ad);

		/*
		if (adLayout != null && adView != null) {
			// Add the adView to it
			adLayout.addView(adView);

			// Initiate a generic request to load it with an ad
			adView.loadAd(new AdRequest());
		}
		*/
		mySharedPreferences = getPreferences(Context.MODE_PRIVATE);
		if(mySharedPreferences.getBoolean(AD_ENABLE, false)){
		
			this.findViewById(R.id.ad).setEnabled(false);
			isAdDisabled = true;
			
		}
		
		HELP_MESSAGE = this.getString(R.string.help_msg_1)
				+ this.getString(R.string.localizeGps)
				+ this.getString(R.string.help_msg_3)
				+ this.getString(R.string.localizeCell)
				+ this.getString(R.string.help_msg_2);
		PackageManager pm = getPackageManager(); 
		versionCode = "0.0";
		try {
	            //---get the package info---
	            PackageInfo pi =  
	                pm.getPackageInfo("com.awdisk.android.iridium", 0);
	            //---display the versioncode---        
	            versionCode = pi.versionName;
	        } catch (NameNotFoundException e) {
	            // Impossible
	        }

		ABOUT_MESSAGE = this.getString(R.string.about_msg_1) + " " + versionCode
				+ this.getString(R.string.about_msg_2);

		localizeMeGPS = (Button) findViewById(R.id.Button01);
		localizeMeGPS.setText(R.string.localizeGps);
		localizeMeGPS.setOnClickListener(this);

		localizeMeCell = (Button) findViewById(R.id.Button03);
		localizeMeCell.setText(R.string.localizeCell);
		localizeMeCell.setOnClickListener(this);
	
		ask = (Button) findViewById(R.id.Button02);
		ask.setText(R.string.ask);
		ask.setOnClickListener(this);
		
		askIss = (Button) findViewById(R.id.Button04);
		askIss.setText(R.string.iss);
		askIss.setOnClickListener(this);
		
		latitude = (TextView) findViewById(R.id.TextView01);
		latitude.setText(R.string.latitude);
		longitude = (TextView) findViewById(R.id.TextView02);
		longitude.setText(R.string.longitude);
		latEdit = (EditText) findViewById(R.id.EditText01);
		//latEdit.setText(R.string.zero_cste);
		latEdit.setText(mySharedPreferences.getString(LATITUDE_OLD, this.getString(R.string.zero_cste)));
		lonEdit = (EditText) findViewById(R.id.EditText02);
		//lonEdit.setText(R.string.zero_cste);
		lonEdit.setText(mySharedPreferences.getString(LONGITUDE_OLD,this.getString(R.string.zero_cste)));
			
		gmt = (TextView) findViewById(R.id.TextView03);
		gmt.setText(this.getString(R.string.gmt) + getTimeZone());
		
		alt = (TextView) findViewById(R.id.TextView04);
		alt.setText(R.string.alt);
		altEdit = (EditText)findViewById(R.id.EditText03);
		//altEdit.setText(R.string.zero_cste);
		altEdit.setText(mySharedPreferences.getString(ALTITUDE_OLD, this.getString(R.string.zero_cste)));
		
		m = new MyLocationManager(this);
		m.startLocationReceiving();
		
		latEdit.setSelected(false);
	}

	public void onClick(View v) {
		if (v == ask) {
			m.stopLocationReceiving();
			String latitude = latEdit.getText().toString();
			String longitude = lonEdit.getText().toString();
			String altitude = altEdit.getText().toString();
			
			saveValues(latitude,longitude,altitude);
			
			try {
				Float.valueOf(latitude);
				Float.valueOf(longitude);
				Float.valueOf(altitude);
				String location = this.getString(R.string.home_cste);
				address = this.getString(R.string.url_adress_part1) + latitude + this.getString(R.string.url_adress_part2)
						+ longitude + this.getString(R.string.url_adress_part3) + location + this.getString(R.string.url_adress_part4)+
						altitude + this.getString(R.string.url_adress_part5) + "uct";//corr.get(gmtValue);
				intent = new Intent(IridiumFlare.this, FlareResult.class);
				intent.putExtra(this.getString(R.string.key), address);
				intent.putExtra("TIME_CORR", gmtValue);
				intent.putExtra("LOCATION","lat:"+latitude+"/long:"+longitude);
				intent.putExtra("SIMPLE_LOC", ""+latitude+" "+longitude);
				startActivity(intent);
			} catch (NumberFormatException e) {

				new AlertDialog.Builder(IridiumFlare.this)

				.setTitle(R.string.error)

				.setMessage(R.string.wrong_gps_format)

				.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {

					}
				})

				.show();
			}

		} else if (v == localizeMeGPS) {
			m.startLocationReceiving();
			if (!m.isGPSEnabled()) {

				new AlertDialog.Builder(IridiumFlare.this)

				.setTitle(R.string.error)

				.setMessage(R.string.enable_gps)

				.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {

						startActivity(new Intent(
								"android.settings.LOCATION_SOURCE_SETTINGS"));

					}
				})

				.show();

			} else {
				
				getLocation(1);
			}
		} else if (v == localizeMeCell) {
			getLocation(0);
		} else if (v == askIss){
			m.stopLocationReceiving();
			
			String latitude = latEdit.getText().toString();
			String longitude = lonEdit.getText().toString();
			String altitude = altEdit.getText().toString();

			saveValues(latitude,longitude,altitude);
			
			try {
				Float.valueOf(latitude);
				Float.valueOf(longitude);
				Float.valueOf(altitude);
				String location = this.getString(R.string.home_cste);
				address = this.getString(R.string.url_iss_adress_part1) + latitude + this.getString(R.string.url_adress_part2)
						+ longitude + this.getString(R.string.url_adress_part3) + location + this.getString(R.string.url_adress_part4)+
						altitude + this.getString(R.string.url_adress_part5) + "uct";//corr.get(gmtValue);
				intent = new Intent(IridiumFlare.this, IssResult.class);
				intent.putExtra(this.getString(R.string.key), address);
				intent.putExtra("TIME_CORR", gmtValue);
				intent.putExtra("SIMPLE_LOC", ""+latitude+" "+longitude);
				startActivity(intent);
			} catch (NumberFormatException e) {

				new AlertDialog.Builder(IridiumFlare.this)

				.setTitle(R.string.error)

				.setMessage(R.string.wrong_gps_format)

				.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {

					}
				})

				.show();
			}
		}
		
	}

	
	
	private void saveValues(String latitude2, String longitude2, String altitude) {
		editor = mySharedPreferences.edit();
		editor.putString(LATITUDE_OLD, latitude2);
		editor.putString(LONGITUDE_OLD, longitude2);
		editor.putString(ALTITUDE_OLD, altitude);
		editor.commit();
		
	}

	private String getTimeZone() {

		TimeZone tz = TimeZone.getDefault();
		gmtValue = tz.getRawOffset() / 3600000.0;
		Date d;
		if(tz.useDaylightTime()){
			d = new Date();
			if(tz.inDaylightTime(d)){
				//we add one hour
				gmtValue += 1.0;
				//Toast.makeText(getApplicationContext(), "in "+gmtValue, Toast.LENGTH_LONG).show();
			}else{
				//Toast.makeText(getApplicationContext(), "not in "+gmtValue, Toast.LENGTH_LONG).show();
			}
		}else{
			//Toast.makeText(getApplicationContext(), "no daylight "+gmtValue, Toast.LENGTH_LONG).show();
		}
		//Toast.makeText(getApplicationContext(), ""+gmtValue, Toast.LENGTH_SHORT).show();
		if (gmtValue >= 0) {
			return "+" + Double.toString(gmtValue);
		} else {
			return Double.toString(gmtValue);
		}
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		helpItm = menu.add(Menu.NONE, help, help, R.string.help);
		// helpItm.setIcon(R.drawable.search);
		helpItm.setOnMenuItemClickListener(helpMenu);
		aboutItm = menu.add(Menu.NONE, about, about, R.string.about);
		// aboutItm.setIcon(R.drawable.schedule);
		aboutItm.setOnMenuItemClickListener(aboutMenu);
		if(isAdDisabled){
			adItm = menu.add(Menu.NONE, ad,ad , R.string.adEnable);
		}else{
			adItm = menu.add(Menu.NONE, ad,ad , R.string.adDisable);
		}
		adItm.setOnMenuItemClickListener(adMenu);
		infoItm = menu.add(Menu.NONE,info,info,R.string.infoVers);
		infoItm.setOnMenuItemClickListener(infoMenu);
		donateItm = menu.add(Menu.NONE, donate,donate ,R.string.donate);
		donateItm.setOnMenuItemClickListener(donateMenu);
		return super.onCreateOptionsMenu(menu);
	}
	
	private MenuItem.OnMenuItemClickListener donateMenu = new MenuItem.OnMenuItemClickListener() {
		
		public boolean onMenuItemClick(MenuItem item) {
			Uri uri = Uri.parse(PAYPAL_URL);
			Intent webDonate = new Intent(Intent.ACTION_VIEW,
					   uri);
					startActivity(webDonate);
			return false;
		}
	};

	private MenuItem.OnMenuItemClickListener infoMenu = new MenuItem.OnMenuItemClickListener() {

		public boolean onMenuItemClick(MenuItem item) {
				new AlertDialog.Builder(IridiumFlare.this)

				.setTitle(R.string.update)

				.setMessage("Iridium Flare Forecast V"+versionCode+"\n"+getString(R.string.update_info))

				.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {

					}
				})

				.show();
				return false;
			}

		
		

		};
	
	private MenuItem.OnMenuItemClickListener helpMenu = new MenuItem.OnMenuItemClickListener() {

		public boolean onMenuItemClick(MenuItem item) {
			
				new AlertDialog.Builder(IridiumFlare.this)

				.setTitle(R.string.help)

				.setMessage(HELP_MESSAGE)

				.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {

					}
				})

				.show();
				return false;
			}

		
		

		};

	private MenuItem.OnMenuItemClickListener aboutMenu = new MenuItem.OnMenuItemClickListener() {

		
		public boolean onMenuItemClick(MenuItem item) {
			new AlertDialog.Builder(IridiumFlare.this)

			.setTitle(R.string.about)

			.setMessage(ABOUT_MESSAGE)

			.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int whichButton) {

				}
			})

			.show();
			return false;
		}

	};
	
	
	private MenuItem.OnMenuItemClickListener adMenu = new MenuItem.OnMenuItemClickListener() {
		
		public boolean onMenuItemClick(MenuItem item) {
			editor = mySharedPreferences.edit();
			if(isAdDisabled){
				item.setTitle(getString(R.string.adDisable));
				findViewById(R.id.ad).setEnabled(true);
				editor.putBoolean(AD_ENABLE, false);
				editor.commit();
			}else{
				item.setTitle(getString(R.string.adEnable));
				findViewById(R.id.ad).setEnabled(false);
				editor.putBoolean(AD_ENABLE, true);
				editor.commit();
			}
			isAdDisabled = !isAdDisabled;
			return false;
			}
	};
		
	/*
	private MenuItem.OnMenuItemClickListener donateMenu = new MenuItem.OnMenuItemClickListener() {
		
		public boolean onMenuItemClick(MenuItem item) {
			if(isAdDisabled){
				item.setTitle(getString(R.string.adDisable));
			}else{
				item.setTitle(getString(R.string.adEnable));
			}
			isAdDisabled = !isAdDisabled;
			return false;
			}
	};
	*/
	private void getLocation(final int choice) {
		// Display a progress bar while getting the GPS info
		

		// This method will generate a Thread
		switch (choice) {
		case 0:
			// cell
			myProgressDialog = ProgressDialog.show(IridiumFlare.this,
					this.getString(R.string.processing), this.getString(R.string.find_location), true, false);
			findLocationWithCell();
			break;
		case 1:
			// gps
			findLocationWithGPS();
			break;
		}
		// Works when the thread is over
		uiCallback = new Handler() {
			public void handleMessage(Message msg) {
				myProgressDialog.dismiss();	
				if(msg.what == 0){
						latEdit.setText("" + lat);
						lonEdit.setText("" + lng);
						getAltitude(lng, lat);
					}else{
						new AlertDialog.Builder(IridiumFlare.this)

						.setTitle(R.string.error)

						.setMessage(R.string.errCell)

						.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int whichButton) {
								
							}
						})

						.show();
					}
			}
		};
		
		

	}

	private void findLocationWithGPS() {
		loc = m.getCurrentLocation();
		if (loc == null) {
			new AlertDialog.Builder(IridiumFlare.this)

			.setTitle(R.string.error)

			.setMessage(R.string.no_gps)

			.setNeutralButton(R.string.ok,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog,
								int whichButton) {

						}
					})

			.show();

		} else {
			lat = loc.getLatitude();
			lng = loc.getLongitude();
			latEdit.setText("" + lat);
			lonEdit.setText("" + lng);
			getAltitude(lng, lat);
			

		}
		
	}

	/**
	 * This method generate a progress dialog box and "do" the calculation
	 */
	private void findLocationWithCell() {
		new Thread() {
			public void run() {
				try {
					getCellIDAndLac();
					getLongLat();
					// send message to handler to process results
					uiCallback.sendEmptyMessage(0);
				} catch (Exception e) {
					uiCallback.sendEmptyMessage(1);
					
				}
			}
		}.start();
	}

	/**
	 * This method connects to the "secret Google's API" to get GPS coordinates
	 * from CellID and Lac
	 */
	private void getLongLat() {
		try {

			// Getting the coordinates

			String urlString = "http://www.google.com/glm/mmap";

			// ---open a connection to Google Maps API---
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			httpConn.connect();

			// ---write some custom data to Google Maps API---
			OutputStream outputStream = httpConn.getOutputStream();
			WriteData(outputStream, cellID, lac);

			// ---get the response---
			InputStream inputStream = httpConn.getInputStream();
			DataInputStream dataInputStream = new DataInputStream(inputStream);

			// ---interpret the response obtained---
			dataInputStream.readShort();
			dataInputStream.readByte();
			int code = dataInputStream.readInt();
			if (code == 0) {
				lat = (double) dataInputStream.readInt() / 1000000D;
				lng = (double) dataInputStream.readInt() / 1000000D;
				dataInputStream.readInt();
				dataInputStream.readInt();
				dataInputStream.readUTF();

			} else {
				new AlertDialog.Builder(IridiumFlare.this)

				.setTitle(R.string.error)

				.setMessage(R.string.no_cell)

				.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {

					}
				})

				.show();

			}
		
		} catch (Exception e) {
			//Should never happens
			new AlertDialog.Builder(IridiumFlare.this)

			.setTitle(R.string.error)

			.setMessage(R.string.no_cell)

			.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int whichButton) {

				}
			})

			.show();
		}

	}

	/**
	 * This method is used to send the data to the "secret Google's API"
	 * 
	 * @param out
	 * @param cellID
	 * @param lac
	 * @throws IOException
	 */
	private void WriteData(OutputStream out, int cellID, int lac)
			throws IOException {
		DataOutputStream dataOutputStream = new DataOutputStream(out);
		dataOutputStream.writeShort(21);
		dataOutputStream.writeLong(0);
		dataOutputStream.writeUTF("en");
		dataOutputStream.writeUTF("Android");
		dataOutputStream.writeUTF("1.0");
		dataOutputStream.writeUTF("Web");
		dataOutputStream.writeByte(27);
		dataOutputStream.writeInt(0);
		dataOutputStream.writeInt(0);
		dataOutputStream.writeInt(3);
		dataOutputStream.writeUTF("");

		dataOutputStream.writeInt(cellID);
		dataOutputStream.writeInt(lac);

		dataOutputStream.writeInt(0);
		dataOutputStream.writeInt(0);
		dataOutputStream.writeInt(0);
		dataOutputStream.writeInt(0);
		dataOutputStream.flush();
	}

	/**
	 * This method gets the CellID and the Lac of your Android device.
	 */
	private void getCellIDAndLac() throws Exception{
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		CellLocation tmp = tm.getCellLocation();
		if(tmp != null){	
			location = (GsmCellLocation) tm.getCellLocation();

			lac = location.getLac();
			cellID = location.getCid();
		}else {
			throw new Exception();
		}
	}
	
	private void getAltitude(Double longitude, Double latitude) {
		
		
		double result = Double.NaN;
	    HttpClient httpClient = new DefaultHttpClient();
	    HttpContext localContext = new BasicHttpContext();
	    String url = "http://ws.geonames.org/srtm3?lat="+
	    latitude+"&lng="+longitude+"&style=full";
	    	
	    HttpGet httpGet = new HttpGet(url);
	    try {
	        HttpResponse response = httpClient.execute(httpGet, localContext);
	        HttpEntity entity = response.getEntity();
	        if (entity != null) {
	            InputStream instream = entity.getContent();
	            int r = -1;
	            StringBuffer respStr = new StringBuffer();
	            while ((r = instream.read()) != -1)
	                respStr.append((char) r);
	                result = Double.parseDouble(respStr.toString());
	            instream.close();
	        }
	    } catch (Exception e) {
	    	result = 0.0;
	    	Toast.makeText(getApplicationContext(), this.getString(R.string.errAlt), Toast.LENGTH_SHORT).show();
	    } 
	    altEdit.setText(""+result);
	}
	
	/**
	 * Used when we quit the program to disable the GPS
	 */
	public void onBackPressed() {
		m.stopLocationReceiving();
		finish();
	}
	


	
}
