package com.awdisk.android.iridium;

import java.util.Calendar;

public class IssItem {
	private String date, intensity, stT, stAlt,stAz, mxT, mxAlt, mxAz, enT, enAlt, enAz;
	private int corrHour,corrMinute;
	private Calendar timeStart, timeMax, timeEnd;
	private MonthTable mt;
	private InverseMonthTable imt;
	
	public IssItem(double gmtValue,String s1,String s2,String s3,String s4,String s5,String s6,String s7,String s8,String s9,String s10,String s11, MonthTable mt, InverseMonthTable imt)throws Exception {
		this.date = s1;
		this.intensity = s2;
		this.stT = s3;
		this.stAlt = s4;
		this.stAz = s5;
		this.mxT = s6;
		this.mxAlt = s7;
		this.mxAz = s8;
		this.enT = s9;
		this.enAlt = s10;
		this.enAz = s11;
		this.mt = mt;
		this.imt = imt;
		
		corrHour = (int)Math.floor(gmtValue);
		corrMinute = (int)((gmtValue-corrHour) * 60);
		
		timeStart = Calendar.getInstance();
		timeMax = Calendar.getInstance();
		timeEnd = Calendar.getInstance();
		this.date = this.date.replaceAll("\\s+", " ");
		this.date = this.date.replaceAll("^\\s", "");
		this.date = this.date.replaceAll(",", "");
		String[] dataDay = this.date.split("\\s");
		if(dataDay.length == 2){
			timeStart.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dataDay[0]));
			timeStart.set(Calendar.MONTH, this.mt.get(dataDay[1]));
			timeMax.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dataDay[0]));
			timeMax.set(Calendar.MONTH, this.mt.get(dataDay[1]));
			timeEnd.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dataDay[0]));
			timeEnd.set(Calendar.MONTH, this.mt.get(dataDay[1]));
			
		}else{
			throw new Exception("dateTime");
		}
		
		//start
		String[] dataTime1 = this.stT.split(":");
		if (dataTime1.length==3) {
			timeStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dataTime1[0]));
			timeStart.set(Calendar.MINUTE, Integer.parseInt(dataTime1[1]));
			timeStart.set(Calendar.SECOND, Integer.parseInt(dataTime1[2]));
			
			timeStart.add(Calendar.HOUR_OF_DAY,corrHour );
			timeStart.add(Calendar.MINUTE, corrMinute);
			
		}else{
			throw new Exception("startTime");
		}
		
		//Max
		String[] dataTime2 = this.mxT.split(":");
		if (dataTime2.length==3) {
			timeMax.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dataTime2[0]));
			timeMax.set(Calendar.MINUTE, Integer.parseInt(dataTime2[1]));
			timeMax.set(Calendar.SECOND, Integer.parseInt(dataTime2[2]));
			
			timeMax.add(Calendar.HOUR_OF_DAY,corrHour );
			timeMax.add(Calendar.MINUTE, corrMinute);
			
		}else{
			throw new Exception("maxTime");
		}
		
		//end
		String[] dataTime3 = this.enT.split(":");
		if (dataTime3.length==3) {
			timeEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dataTime3[0]));
			timeEnd.set(Calendar.MINUTE, Integer.parseInt(dataTime3[1]));
			timeEnd.set(Calendar.SECOND, Integer.parseInt(dataTime3[2]));
			
			timeEnd.add(Calendar.HOUR_OF_DAY,corrHour );
			timeEnd.add(Calendar.MINUTE, corrMinute);
			
		}else{
			throw new Exception("endTime");
		}
		
	}
	
	private String getStartHour(){
		String toReturn = "";
		if(timeStart.get(Calendar.HOUR_OF_DAY)<10){
			toReturn+="0";
		}
		toReturn += timeStart.get(Calendar.HOUR_OF_DAY)+":";
		if(timeStart.get(Calendar.MINUTE)<10){
			toReturn+="0";
		}
		toReturn += timeStart.get(Calendar.MINUTE)+":";
		if(timeStart.get(Calendar.SECOND)<10){
			toReturn+="0";
		}
		toReturn += timeStart.get(Calendar.SECOND);
		
		return toReturn;
	}
	
	private String getMaxHour(){
		String toReturn = "";
		if(timeMax.get(Calendar.HOUR_OF_DAY)<10){
			toReturn+="0";
		}
		toReturn += timeMax.get(Calendar.HOUR_OF_DAY)+":";
		if(timeMax.get(Calendar.MINUTE)<10){
			toReturn+="0";
		}
		toReturn += timeMax.get(Calendar.MINUTE)+":";
		if(timeMax.get(Calendar.SECOND)<10){
			toReturn+="0";
		}
		toReturn += timeMax.get(Calendar.SECOND);
		
		return toReturn;
	}
	
	private String getEndHour(){
		String toReturn = "";
		if(timeEnd.get(Calendar.HOUR_OF_DAY)<10){
			toReturn+="0";
		}
		toReturn += timeEnd.get(Calendar.HOUR_OF_DAY)+":";
		if(timeEnd.get(Calendar.MINUTE)<10){
			toReturn+="0";
		}
		toReturn += timeEnd.get(Calendar.MINUTE)+":";
		if(timeEnd.get(Calendar.SECOND)<10){
			toReturn+="0";
		}
		toReturn += timeEnd.get(Calendar.SECOND);
		
		return toReturn;
	}
	
	public String getSimpleLine(String at){
		return timeStart.get(Calendar.DAY_OF_MONTH)+" "+
				imt.get(timeStart.get(Calendar.MONTH))+" "+at+" "+
				getStartHour()+" | Mag. "+
				this.intensity;
	}

	public String getCompleteLine(String date, String intensity,
			String start, String time, String altitude, String azimuth,
			String maxAlt, String end) {
		
		String toDisplay = "";
		toDisplay += date + " "
				+ timeStart.get(Calendar.DAY_OF_MONTH)+" "+imt.get(timeStart.get(Calendar.MONTH))+"\n";;
		toDisplay += intensity + " "
				+ this.intensity + "\n";
		toDisplay += start;
		toDisplay += time + " "
				+ getStartHour() + "\n";
		toDisplay += altitude + " "
				+ this.stAlt + "\n";
		toDisplay += azimuth + " "
				+ this.stAz + "\n";
		toDisplay += maxAlt;
		toDisplay += time + " "
				+ getMaxHour() + "\n";
		toDisplay += altitude + " "
				+ this.mxAlt + "\n";
		toDisplay += azimuth + " "
				+ this.mxAz + "\n";
		toDisplay += end;
		toDisplay += time + " "
				+ getEndHour() + "\n";
		toDisplay += altitude + " "
				+ this.enAlt + "\n";
		toDisplay += azimuth + " "
				+ this.enAz + "\n";
		
		return toDisplay;
		
	}
	
	public long getStartTime() {
		return timeStart.getTimeInMillis();
	}
	
	public long getEndTime() {
		return timeEnd.getTimeInMillis();
	}

	public String getTitle() {
		return "ISS Mag: "+this.intensity;
	}
	
	
}
