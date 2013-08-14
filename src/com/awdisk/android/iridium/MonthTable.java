package com.awdisk.android.iridium;

import java.util.HashMap;

@SuppressWarnings("serial")
class MonthTable extends HashMap<String, Integer> {

	public MonthTable() {
		this.put("Jan",0);
		this.put("Feb",1);
		this.put("Mar",2);
		this.put("Apr",3);
		this.put("May",4);
		this.put("Jun",5);
		this.put("Jul",6);
		this.put("Aug",7);
		this.put("Sep",8);
		this.put("Oct",9);
		this.put("Nov",10);
		this.put("Dec",11);
	}
}
