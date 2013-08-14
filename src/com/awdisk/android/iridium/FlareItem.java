package com.awdisk.android.iridium;

import java.util.Calendar;

import android.app.Activity;

public class FlareItem {

	private String date, hours, azimuth, distToFlare, satellite, intensity,
			alt, magCenter,sunAlt;
	private Calendar time;
	private int corrHour, corrMinute;
	private MonthTable mt;
	private InverseMonthTable imt;
	private String location;

	public FlareItem(String location,double gmtValue, String data1, String data2, String data3,
			String data4, String data5, String data6, String data7,
			String data8, MonthTable mt, InverseMonthTable imt)
			throws Exception {
		this.location = location;
		this.date = data1;
		this.intensity = data2;
		this.alt = data3;
		this.azimuth = data4;
		this.satellite = data5;
		this.distToFlare = data6;
		this.magCenter = data7;
		this.sunAlt = data8;
		this.mt = mt;
		this.imt = imt;

		corrHour = (int) Math.floor(gmtValue);
		corrMinute = (int) ((gmtValue - corrHour) * 60);

		time = Calendar.getInstance();
		this.date = this.date.replaceAll("\\s+", " ");
		this.date = this.date.replaceAll("^\\s", "");
		this.date = this.date.replaceAll(",", "");
		String[] dataDay = this.date.split("\\s");
		this.hours = dataDay[2];
		String[] dataTime = this.hours.split(":");
		if (dataDay.length == 3 && dataTime.length == 3) {
			
			time.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dataDay[1]));
			time.set(Calendar.MONTH, this.mt.get(dataDay[0]));
			time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dataTime[0]));
			time.set(Calendar.MINUTE, Integer.parseInt(dataTime[1]));
			time.set(Calendar.SECOND, Integer.parseInt(dataTime[2]));

			time.add(Calendar.HOUR_OF_DAY, corrHour);
			time.add(Calendar.MINUTE, corrMinute);

		} else {
			throw new Exception();
		}

	}

	private String getHour() {
		String toReturn = "";
		if (time.get(Calendar.HOUR_OF_DAY) < 10) {
			toReturn += "0";
		}
		toReturn += time.get(Calendar.HOUR_OF_DAY) + ":";
		if (time.get(Calendar.MINUTE) < 10) {
			toReturn += "0";
		}
		toReturn += time.get(Calendar.MINUTE) + ":";
		if (time.get(Calendar.SECOND) < 10) {
			toReturn += "0";
		}
		toReturn += time.get(Calendar.SECOND);

		return toReturn;
	}

	public String getSimpleLine(String at) {
		return time.get(Calendar.DAY_OF_MONTH) + " "
				+ imt.get(time.get(Calendar.MONTH)) + " " + at + " "
				+ getHour() + " | Mag. " + this.intensity;
	}

	private String getCompleteLine(String sDate, String sLocalTime, String sIntensity, String sAlt,
			String sAzimuth, String sDist, String sFlareCenter, String sSatellite, String sSunAlt) {
		String toDisplay = "";
		
		if(sDate != null)toDisplay += sDate + " " + time.get(Calendar.DAY_OF_MONTH) + " "
		+ imt.get(time.get(Calendar.MONTH)) + "\n";
		
		if(sLocalTime != null)toDisplay += sLocalTime + " " + getHour() + "\n";
		
		toDisplay += sIntensity + " " + this.intensity + "\n";
		toDisplay += sAlt + " " + this.alt + "\n";
		toDisplay += sAzimuth + " " + this.azimuth + "\n";
		toDisplay += sDist + " " + this.distToFlare + "\n";
		toDisplay += sFlareCenter + " " + this.magCenter + "\n";
		toDisplay += sSatellite + " " + this.satellite + "\n";
		toDisplay += sSunAlt + " " + this.sunAlt + "\n";
		return toDisplay;
	}

	public String getTitle() {
		return satellite + " Mag: " + intensity;
	}

	public String getDescription(Activity act, boolean withDate) {
		if(withDate)return getCompleteLine(
				act.getString(R.string.date),
				act.getString(R.string.local_time),
				act.getString(R.string.intensity),
				act.getString(R.string.altitude),
				act.getString(R.string.azimuth),
				act.getString(R.string.distance),
				act.getString(R.string.intensity_fl),
				act.getString(R.string.satellite),
				act.getString(R.string.sunaltitude));
		else return getCompleteLine(
				null,
				null,
				act.getString(R.string.intensity),
				act.getString(R.string.altitude),
				act.getString(R.string.azimuth),
				act.getString(R.string.distance),
				act.getString(R.string.intensity_fl),
				act.getString(R.string.satellite),
				act.getString(R.string.sunaltitude));
			
		
	}

	public String getLocation() {
		return location;
	}

	public long getTime() {
		return time.getTimeInMillis();
	}

}
