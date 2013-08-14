/*
 * Table.java
 *
 * Created 3 oct. 2009.
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

package com.awdisk.android.tableFromHTML;

import java.util.ArrayList;

/**
 * This class manage a table
 *
 * @author <a href="mailto:alexandre@awdisk.com">Alexandre Wetzel</a>
 *
 */
public class Table {
	private ArrayList<ArrayList<String>> line;
	private StringBuilder content;
	private int index;
	private boolean closed;
	private Table parent;
	private String name;
	private ArrayList<String> tableHeader;

	


	/**
	 * This is the main constructor. You need to give an index for the table.
	 * it's plays the role of the name because the <caption> tag is optional.
	 * @param index the number of the table
	 */
	public Table(int index) {
		this.index = index;
		this.closed = false;
		this.parent = null;
		this.line = new ArrayList<ArrayList<String>>();
		this.content = new StringBuilder();
	}

	/**
	 * This constructor is used to specify a hierarchy when the table is in an other table.
	 * @param index the number of the table
	 * @param tmp the table in which our table is
	 */
	public Table(int index, Table tmp) {
		this.index = index;
		this.closed = false;
		this.parent = tmp;
		this.line = new ArrayList<ArrayList<String>>();
		this.content = new StringBuilder();
	}

	/**
	 * This method add a line in the table and return the line to be filled.
	 * @return the new line.
	 */
	public ArrayList<String> addALine() {
		ArrayList<String> columns = new ArrayList<String>();
		line.add(columns);
		return columns;

	}
	
	/**
	 * This returns the last line of the table.
	 * @return the last line of the table
	 */
	public ArrayList<String> getLastLine() {
		return line.get(line.size() - 1);

	}

	/**
	 * This is used to display or get the data of the table 
	 * @param index the number of the line
	 * @return the selected line
	 */
	public ArrayList<String> getALine(int index) {
		return line.get(index);

	}

	/**
	 * This gives you the number of line of the table.
	 * @return the number of line
	 */
	public int size() {
		return line.size();
	}

	/**
	 * This is to test if the table is in an other table.
	 * @return a boolean answer
	 */
	public boolean hasParent() {
		if (parent == null) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * This method converts the table in a String array of arrays.
	 * @param withHTMLCode This is to set if you want to keep or not the html code
	 * @return the new array
	 */
	public String[][] getArray(boolean withHTMLCode){
		int nbrOfLine = line.size();
		String[][] array = new String[nbrOfLine][];
		
		
		for(int i = 0; i<line.size();i++){
			array[i] = new String[line.get(i).size()];
			for(int j = 0; j<array[i].length;j++){
				if(withHTMLCode)array[i][j] = line.get(i).get(j);
				else array[i][j] = removeHTML(line.get(i).get(j));
			}
		}
		
		return array;
	}
	
	/**
	 * This method converts the table in a String array.
	 * @param withHTMLCode This is to set if you want to keep or not the html code
	 * @param withHeader This is to set if you want the horizontal header (BETA)
	 * @return the new array
	 */
	public String[] getSimpleArray(boolean withHTMLCode, boolean withHeader){
		ArrayList<String> tmp = new ArrayList<String>();
		String[] toReturn;
		int index;
		if(withHeader)index=0;
		else index = 1;
		for(; index<line.size();index++){
			
			for(int j = 0; j<line.get(index).size();j++){
				if(withHTMLCode)tmp.add(line.get(index).get(j));
				else tmp.add(removeHTML((line.get(index).get(j))));
			}
		}
		toReturn = new String[tmp.size()];
		for(int i = 0; i<tmp.size();i++){
			toReturn[i] = tmp.get(i);
		}
		return toReturn;
	}
	/**
	 * This class remove all the html tags of a string.
	 * It corrects also some error of encoding.
	 * @param text the string to correct
	 * @return the corrected string
	 */
	private String removeHTML(String text) {
		String regex = "<[^>]*>";
		String toReturn = text.replaceAll(regex,"");
		toReturn  = toReturn.replaceAll("&#176;", "°");
		return toReturn;
		
	}
	
	
	
	//Standard getters and setters
	
	public Table getParent(){
		return parent;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getTableHeader() {
		return tableHeader;
	}

	public void setTableHeader(ArrayList<String> tableHeader) {
		this.tableHeader = tableHeader;
	}

	public int getIndex() {
		return index;
	}

	public boolean isClosed() {
		return closed;
	}

	public StringBuilder getContent() {
		return content;
	}

	public void setClosed() {
		this.closed = true;
	}


}
