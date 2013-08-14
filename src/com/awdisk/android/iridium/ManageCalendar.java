package com.awdisk.android.iridium;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class ManageCalendar extends Activity {

	private TextView text;
	private Spinner spinner;
	private CheckBox check;
	private Button saveButton,returnButton;

	private String path;
	private long timeStart,timeEnd;
	private String title, location,info;
	private HashMap<String, String> calendarList;
	private String descr;

	private Uri calendars;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
		     path = "content://calendar/";
		}else{
			path = "content://com.android.calendar/";
		}
		
		calendarList = new HashMap<String, String>();

		Bundle extras = getIntent().getExtras();
		timeStart = extras.getLong("start");
		timeEnd = extras.getLong("end",timeStart+60000);
		title = extras.getString("name");
		info = extras.getString("info");
		location = extras.getString("loc");
		descr = extras.getString("descr");
		saveButton = (Button) findViewById(R.id.Button01);
		saveButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				ContentValues event = new ContentValues();
				event.put("calendar_id",
						calendarList.get(spinner.getSelectedItem()));
				event.put("title", title);
				event.put("description", descr);
				event.put("eventLocation", location);
				event.put("dtstart", timeStart);
				event.put("dtend", timeEnd);
				if (check.isChecked())
					event.put("hasAlarm", 1); // 0 for false, 1 for true
				else
					event.put("hasAlarm", 0);
				Uri eventsUri = Uri.parse(path+"events");
				getContentResolver().insert(eventsUri, event);
				finish();
			}
		});

		returnButton = (Button) findViewById(R.id.Button02);
		returnButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
				
			}
		});
		text = (TextView) findViewById(R.id.TextView01);
		
		text.setText(info +"\nLocation: "+location+"\n\nChoose your calendar");
		
		spinner = (Spinner) findViewById(R.id.Spinner01);
		String[] projection = new String[] { "_id", "name" };
		calendars = Uri.parse(path+"calendars");

		Cursor managedCursor = managedQuery(calendars, projection,
				"selected=1", null, null);
		ArrayList<String> calAdapter = new ArrayList<String>();

		if (managedCursor.moveToFirst()) {
			String calName;
			String calId;
			int nameColumn = managedCursor.getColumnIndex("name");
			int idColumn = managedCursor.getColumnIndex("_id");
			do {
				calName = managedCursor.getString(nameColumn);
				calId = managedCursor.getString(idColumn);
				if (calName != null && calName.length() > 0) {
					calAdapter.add(calName);
					calendarList.put(calName, calId);
				}
			} while (managedCursor.moveToNext());
		}

		if (calAdapter.size() > 0) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, calAdapter);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			
			check = (CheckBox) findViewById(R.id.CheckBox01);
			check.setText("Alarm on");
		} else {
			new AlertDialog.Builder(ManageCalendar.this)

					.setTitle(R.string.error)

					.setMessage(R.string.no_calendar)

					.setNeutralButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int whichButton) {
									finish();
								}
							})

					.show();
		}
	}
	
}
