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
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
public class IssResult extends ListActivity implements OnItemClickListener {
	public final static int NBR_COLUMNS_ISS = 12;
	private String address;
	private String[] items;
	private IssItem[] listIss;
	private double timeCorr;
	private IssItem tmpIss;
	private String loc, message;
	
	private ArrayList<Table> stack = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.result);
		Bundle extras = getIntent().getExtras();
		address = extras.getString(this.getString(R.string.key));
		timeCorr = extras.getDouble("TIME_CORR");
		loc = extras.getString("SIMPLE_LOC");
		if(stack==null){
		DownloadWebPageTask task = new DownloadWebPageTask();
	    task.execute(new String[] {address });
		}else{
			displayList();
		}
	}
	
	public void displayList(){
		if(stack != null){
			if (stack.size() >= 4) {

				items = stack.get(3).getSimpleArray(false, false);
			} else {
				items = new String[1];
			}
			
			if (items.length >= 9) {
				items = corrItem(items);
			
				try{
					int nbrLine = items.length/NBR_COLUMNS_ISS;
					listIss = new IssItem[nbrLine];
					for(int i = 0; i<nbrLine;i++){
						listIss[i] = new IssItem(timeCorr,items[i*NBR_COLUMNS_ISS],items[i*NBR_COLUMNS_ISS+1],items[i*NBR_COLUMNS_ISS+2],items[i*NBR_COLUMNS_ISS+3],items[i*NBR_COLUMNS_ISS+4],items[i*NBR_COLUMNS_ISS+5],items[i*NBR_COLUMNS_ISS+6],items[i*NBR_COLUMNS_ISS+7],items[i*NBR_COLUMNS_ISS+8],items[i*NBR_COLUMNS_ISS+9],items[i*NBR_COLUMNS_ISS+10], new MonthTable(),new InverseMonthTable());			
					}
				}catch(Exception e){
					Toast.makeText(getApplicationContext(),this.getString(R.string.errIssItem) , Toast.LENGTH_SHORT).show();
				}
			
				// Creating the list
				ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, prepareItem(items));
				setListAdapter(aa);
				getListView().setTextFilterEnabled(true);
				getListView().setOnItemClickListener(this);
			} else {
				new AlertDialog.Builder(IssResult.this)

					.setTitle(R.string.error)

					.setMessage(R.string.errIss)

					.setNeutralButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int whichButton) {
									finish();
								}
							})

					.show();

			}
		}else{
			new AlertDialog.Builder(IssResult.this)

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
	}

	private String[] corrItem(String[] items) {
		int corr = 10;
		String[] toReturn = new String[items.length - corr];
		for (int i = 0; i < toReturn.length; i++) {
			toReturn[i] = items[i + corr];
		}
		return toReturn;
	}

	private String[] prepareItem(String[] items) {
		int nbrLine = items.length / NBR_COLUMNS_ISS;
		String[] toRetun = new String[nbrLine];
		for (int i = 0; i < toRetun.length; i++) {
			toRetun[i] = listIss[i].getSimpleLine(this.getString(R.string.at));
		}
		return toRetun;
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		tmpIss = listIss[position];
		message = tmpIss.getCompleteLine(this.getString(R.string.date),
				this.getString(R.string.intensity),
				this.getString(R.string.start),
				this.getString(R.string.time),
				this.getString(R.string.altitude),
				this.getString(R.string.azimuth),
				this.getString(R.string.maxAlt),
				this.getString(R.string.end));
		
		new AlertDialog.Builder(IssResult.this)

		.setTitle(R.string.title_dialogue)

		.setMessage(message)

		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

			}
		})
		/*
		.setNegativeButton(R.string.add, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				Intent it = new Intent(IssResult.this, ManageCalendar.class);
				it.putExtra("info", tmpIss.getSimpleLine(getString(R.string.at)));
				it.putExtra("start", tmpIss.getStartTime());
				it.putExtra("end", tmpIss.getEndTime());
				it.putExtra("loc", loc);
				it.putExtra("name", tmpIss.getTitle());
				it.putExtra("descr", message);

				startActivity(it);
				
				
				
			}
		})
		*/
		.show();
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
	        	IssResult.this.displayList();
	        }
	    }

}
