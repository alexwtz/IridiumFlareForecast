/*
 * FlareResult.java
 *
 * Created 11 oct. 2009.
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.awdisk.android.tableFromHTML.*;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.AsyncTask;
//import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This class manage the result of the search and displays the flares.
 * 
 * @author <a href="mailto:alexandre@awdisk.com">Alexandre Wetzel</a>
 */
public class FlareResult extends ListActivity implements OnItemClickListener {
	public final static int NBR_COLUMNS_IR = 8;
	private String address;
	private double timeCorr;
	private String[] items;
	private FlareItem[] listFlare;
	public static FlareItem tmpFlare;
	//private Intent intent;
	//private CalendarItem cal;
	private String location,loc,message;
	
	private ArrayList<Table> stack = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.result);
		Bundle extras = getIntent().getExtras();
		address = extras.getString(this.getString(R.string.key));
		timeCorr = extras.getDouble("TIME_CORR");
		location = extras.getString("LOCATION");
		loc = extras.getString("SIMPLE_LOC");
		message = "";
		if(stack==null){
			DownloadWebPageTask task = new DownloadWebPageTask();
			task.execute(new String[] {address });
		}else{
			displayList();
		}
	}
	
	public void displayList(){
		 
		if (stack != null) {
			if (stack.size() >= 5) {
				Table info = stack.get(3);
				items = info.getSimpleArray(false, false);

				// new test
				try {
					int nbrLine = items.length / NBR_COLUMNS_IR;
					listFlare = new FlareItem[nbrLine];
					for (int i = 0; i < nbrLine; i++) {
						listFlare[i] = new FlareItem(location,timeCorr, items[i
								* NBR_COLUMNS_IR],
								items[i * NBR_COLUMNS_IR + 1], items[i
										* NBR_COLUMNS_IR + 2], items[i
										* NBR_COLUMNS_IR + 3], items[i
										* NBR_COLUMNS_IR + 4], items[i
										* NBR_COLUMNS_IR + 5], items[i
										* NBR_COLUMNS_IR + 6], items[i
										* NBR_COLUMNS_IR + 7],
								new MonthTable(), new InverseMonthTable());
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),
							this.getString(R.string.errFlareItem),
							Toast.LENGTH_SHORT).show();
				}

				// end test

				// Creating the list
				ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, prepareItem(items));
				setListAdapter(aa);
				getListView().setTextFilterEnabled(true);
				getListView().setOnItemClickListener(this);

			} else {
				new AlertDialog.Builder(this)

						.setTitle(R.string.error)

						.setMessage(R.string.errIrr)

						.setNeutralButton(R.string.ok,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int whichButton) {
										finish();
									}
								})

						.show();

			}
		} else {
			new AlertDialog.Builder(FlareResult.this)

					.setTitle(R.string.error)

					.setMessage(R.string.errConnection)

					.setNeutralButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int whichButton) {
									finish();
								}
							})

					.show();

		}

//    	textView.setText(Html.fromHtml(result));
    
	}

	private String[] prepareItem(String[] items) {
		int nbrLine = items.length / NBR_COLUMNS_IR;
		String[] toRetun = new String[nbrLine];
		for (int i = 0; i < toRetun.length; i++) {
			toRetun[i] = listFlare[i]
					.getSimpleLine(this.getString(R.string.at));

			// items[0+i*NBR_COLUMNS_IR]+" "+this.getString(R.string.at)+" "+items[1+i*NBR_COLUMNS_IR]+" | Mag. "+items[2+i*NBR_COLUMNS_IR];
		}
		return toRetun;
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// ListAdapter z = (ListAdapter) parent.getAdapter();
		// String i = (String)z.getItem(position);
		// Log.e("Debug", i);
		tmpFlare = listFlare[position];
		
		Builder alt = new AlertDialog.Builder(FlareResult.this);
		alt.setTitle(R.string.title_dialogue);
		message = tmpFlare.getDescription(this, true);
		alt.setMessage(message);
		alt.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		/*
		alt.setNegativeButton(R.string.add, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				Intent it = new Intent(FlareResult.this, ManageCalendar.class);
				it.putExtra("start", tmpFlare.getTime());
				it.putExtra("loc", loc);
				it.putExtra("info", tmpFlare.getSimpleLine(getString(R.string.at)));
				it.putExtra("name", tmpFlare.getTitle());
				it.putExtra("descr", message);
				startActivity(it);
			}
		});
		*/
		
		alt.show();
	}
	
	 private class DownloadWebPageTask extends AsyncTask<String, Void, ArrayList<Table>> {
	        
		 	@Override
	        protected ArrayList<Table> doInBackground(String... urls) {
	            
	            ArrayList<Table> tableList = null;

	            for (String url : urls) {
	            	DefaultHttpClient client = new DefaultHttpClient();
	                HttpGet httpGet = new HttpGet(url);
	                httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36");
	                try {
	                    HttpResponse execute = client.execute(httpGet);
	                    InputStream cont = execute.getEntity().getContent();

	                    BufferedReader buffer = new BufferedReader(
	                            new InputStreamReader(cont));
	                    tableList = ParserTable.extractTable(buffer);
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	            return tableList;
	        }

	        @Override
	        protected void onPostExecute(ArrayList<Table> tableList) {
	        	stack = tableList;
	        	FlareResult.this.displayList();
	        }
	    }
}
