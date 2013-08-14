/*
 * ParserTable.java
 *
 * Created 30 sept. 2009.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

/**
 * This class extract all the tables of an html code.
 * It takes care of all the following tag:
 * The array is surrounded by the tags <TABLE> and </TABLE>.
 * The title of the array is surrounded by <CAPTION> </CAPTION>.
 * Each line is surrounded by <TR> </TR> (Table Row).
 * The cells of the header are surrounded by <TH> </TH> (Table Header)
 * The value cells are surrounded by <TD> </TD> (Table Data)
 * 
 * @see <a href="http://awdisk.com/program/tableFromHTML">Table from HTML</a>
 * @author <a href="mailto:alexandre@awdisk.com">Alexandre Wetzel</a>
 *
 */
public class ParserTable {
	private String address;
	
	/**
	 * This is to set the url of the html code.
	 * @param address the address (url) of the html code
	 */
	public ParserTable(String address) {
		this.address = address;
	}

	
	public static ArrayList<Table> extractTable(BufferedReader buffer) throws IOException {
		Table table = null;
		StringBuilder content = null;
		String tmp;
		ArrayList<String> columns = null;
		int counter = 0;
		int c;
		c = buffer.read();
        ArrayList<Table>tableList = new ArrayList<Table>();
		while (c != -1) {
			switch (c) {
			case 60:
				// c == '<'
				c = buffer.read();
				if (c == (int) 't' || c == (int) 'T') {
					c = buffer.read();
					if (c == (int) 'a' || c == (int) 'A') {
						c = buffer.read();

						if (c == (int) 'b' || c == (int) 'B') {
							c = buffer.read();

							if (c == (int) 'l' || c == (int) 'L') {
								c = buffer.read();
								if (c == (int) 'e' || c == (int) 'E') {

									c = buffer.read();
									// here we use the while because we
									// don't care of the arguments
									while (c != -1) {
										if (c == (int) '>') {
											// here we are at string =
											// "<table>"
											if (table != null
													&& !table
															.isClosed()) {

												table = new Table(
														counter, table);
												if (content != null) {
													String txt = "<table "
															+ String
																	.valueOf(counter)
															+ ">";

													for (int i = 0; i < txt
															.length(); i++) {
														content
																.append(txt
																		.charAt(i));
													}

												}
											} else {
												table = new Table(
														counter);
											}
											content = table
													.getContent();
											counter++;
											tableList.add(table);
											// stack.add(table);
											break;
										}

										c = buffer.read();
									}
								}else{
									if (content != null) {
										content.append('<');
										content.append('t');
										content.append('a');
										content.append('b');
										content.append('l');
										content.append((char) c);
									}
								}
							}else{
								if (content != null) {
									content.append('<');
									content.append('t');
									content.append('a');
									content.append('b');
									content.append((char) c);
								}
							}
						}else{
							if (content != null) {
								content.append('<');
								content.append('t');
								content.append('a');
								content.append((char) c);
							}
						}
					} else if (c == (int) 'r' || c == (int) 'R') {
						c = buffer.read();
						// here we use the while because we don't care of the arguments
						while (c != -1) {
							if (c == (int) '>') {
								// here we are at <tr>
								columns = table.addALine();
								break;
							}
							c = buffer.read();
							
						}
						
						
						
						//c = buffer.read();
						//if (c == (int) '>') {
							// here we are at <tr>
							//columns = table.addALine();
						//}
					} else if (c == (int) 'd' || c == (int) 'D') {
						c = buffer.read();
						// here we use the while because we don't care
						// of
						// the arguments
						while (c != -1) {
							if (c == (int) '>') {
								// here we are at <td>
								content = table.getContent();
								break;
							}
							c = buffer.read();
						}

					}else if(c == (int) 'h' || c == (int) 'H'){
						c = buffer.read();
						// here we use the while because we don't care
						// of
						// the arguments
						while (c != -1) {
							if (c == (int) '>') {
								// here we are at <th>
								// TODO Improve the header management
								content = table.getContent();
								table.setTableHeader(columns);
								break;
							}
							c = buffer.read();
						}
					}else{
						if (content != null) {
							content.append('<');
							content.append('t');
							content.append((char) c);
						}
					}
				} else if (c == (int) '/') {
					c = buffer.read();
					if (c == (int) 't' || c == (int) 'T') {
						c = buffer.read();
						if (c == (int) 'r' || c == (int) 'R') {
							c = buffer.read();
							if (c == (int) '>') {
								// here we are at </tr>
								// end of the line, not very important
								content.delete(0, content.length());
							}
						} else if (c == (int) 'd' || c == (int) 'D') {
							c = buffer.read();
							if (c == (int) '>') {
								// here we are at </td>
								tmp = content.toString();
								if (tmp.endsWith("<"))
									tmp = tmp.substring(0,
											tmp.length() - 1);
								columns.add(tmp);
								content.delete(0, content.length());
							}
						} else if (c == (int) 'a' || c == (int) 'A') {
							c = buffer.read();

							if (c == (int) 'b' || c == (int) 'B') {
								c = buffer.read();

								if (c == (int) 'l' || c == (int) 'L') {
									c = buffer.read();
									if (c == (int) 'e'
											|| c == (int) 'E') {
										c = buffer.read();
										if (c == (int) '>') {
											// here we are at "</table>"
											table.setClosed();
											if (table.hasParent()) {
												table = table
														.getParent();
												content = table
														.getContent();
												columns = table
														.getLastLine();
											}

										}
									}else{
										if (content != null) {
											content.append('<');
											content.append('/');
											content.append('t');
											content.append('a');
											content.append('b');
											content.append('l');
											content.append((char) c);
										}
									}
								}else{
									if (content != null) {
										content.append('<');
										content.append('/');
										content.append('t');
										content.append('a');
										content.append('b');
										content.append((char) c);
									}
								}
							}else{
								if (content != null) {
									content.append('<');
									content.append('/');
									content.append('t');
									content.append('a');
									content.append((char) c);
								}
							}
						}else if(c == (int) 'h' || c == (int) 'H'){
							c = buffer.read();
							if (c == (int) '>') {
								// here we are at </th>
								tmp = content.toString();
								if (tmp.endsWith("<"))
									tmp = tmp.substring(0,
											tmp.length() - 1);
								columns.add(tmp);
								content.delete(0, content.length());
							}
						}else if(c == (int) 'c' || c == (int) 'C'){
							c = buffer.read();
							if (c == (int) 'a' || c == (int) 'A') {
								c = buffer.read();
								if (c == (int) 'p' || c == (int) 'P') {
									c = buffer.read();
									if (c == (int) 't' || c == (int) 'T') {
										c = buffer.read();
										if (c == (int) 'i' || c == (int) 'I') {
											c = buffer.read();
											if (c == (int) 'o' || c == (int) 'O') {
												c = buffer.read();
												if (c == (int) 'n' || c == (int) 'N') {
													c = buffer.read();
													// here we use the while because we
													// don't care of the arguments
													while (c != -1) {
														if (c == (int) '>') {
															// here we are at string =
															// "</caption>"
															
															content = table
																	.getContent();
																								tableList.add(table);
															
															break;
														}

														c = buffer.read();
													}
												}else{
													if (content != null) {
														content.append('<');
														content.append('/');
														content.append('c');
														content.append('a');
														content.append('p');
														content.append('t');
														content.append('i');
														content.append('o');
														content.append((char) c);
													}
												}
											}else{
												if (content != null) {
													content.append('<');
													content.append('/');
													content.append('c');
													content.append('a');
													content.append('p');
													content.append('t');
													content.append('i');
													content.append((char) c);
												}
											}
										}else{
											if (content != null) {
												content.append('<');
												content.append('/');
												content.append('c');
												content.append('a');
												content.append('p');
												content.append('t');
												content.append((char) c);
											}
										}
									}else{
										if (content != null) {
											content.append('<');
											content.append('/');
											content.append('c');
											content.append('a');
											content.append('p');
											content.append((char) c);
										}
									}
								}else{
									if (content != null) {
										content.append('<');
										content.append('/');
										content.append('c');
										content.append('a');
										content.append((char) c);
									}
								}
							}else{
								if (content != null) {
									content.append('<');
									content.append('/');
									content.append('c');
									content.append((char) c);
								}
							}
						
						}
						else{
							if (content != null) {
								content.append('<');
								content.append('/');
								content.append('t');
								content.append((char) c);
							}
						}

					}else if(c == (int) 'c' || c == (int) 'C'){
						c = buffer.read();
						if (c == (int) 'a' || c == (int) 'A') {
							c = buffer.read();
							if (c == (int) 'p' || c == (int) 'P') {
								c = buffer.read();
								if (c == (int) 't' || c == (int) 'T') {
									c = buffer.read();
									if (c == (int) 'i' || c == (int) 'I') {
										c = buffer.read();
										if (c == (int) 'o' || c == (int) 'O') {
											c = buffer.read();
											if (c == (int) 'n' || c == (int) 'N') {
												c = buffer.read();
													if (c == (int) '>') {
														// here we are at string =
														// "</caption>"
														table.setName(content.toString());
														content.delete(0, content.length());
														
														
													}

													
												
											}
										}
									}
								}
							}
						}
					
					}
					else {
						if (content != null) {
							content.append('<');
							content.append('/');
							content.append((char) c);
						}
					}
				}else if(c == (int) 'c' || c == (int) 'C'){
					c = buffer.read();
					if (c == (int) 'a' || c == (int) 'A') {
						c = buffer.read();
						if (c == (int) 'p' || c == (int) 'P') {
							c = buffer.read();
							if (c == (int) 't' || c == (int) 'T') {
								c = buffer.read();
								if (c == (int) 'i' || c == (int) 'I') {
									c = buffer.read();
									if (c == (int) 'o' || c == (int) 'O') {
										c = buffer.read();
										if (c == (int) 'n' || c == (int) 'N') {
											c = buffer.read();
											// here we use the while because we
											// don't care of the arguments
											while (c != -1) {
												if (c == (int) '>') {
													// here we are at string =
													// "<caption>"
													
													content = table
															.getContent();
																						tableList.add(table);
													
													break;
												}

												c = buffer.read();
											}
										}else {
											if (content != null) {
												content.append('<');
												content.append('c');
												content.append('a');
												content.append('p');
												content.append('t');
												content.append('i');
												content.append('o');
												content.append((char) c);
											}
										}
									}else {
										if (content != null) {
											content.append('<');
											content.append('c');
											content.append('a');
											content.append('p');
											content.append('t');
											content.append('i');
											content.append((char) c);
										}
									}
								}else {
									if (content != null) {
										content.append('<');
										content.append('c');
										content.append('a');
										content.append('p');
										content.append('t');
										content.append((char) c);
									}
								}
							}
							else {
								if (content != null) {
									content.append('<');
									content.append('c');
									content.append('a');
									content.append('p');
									content.append((char) c);
								}
							}
						}else {
							if (content != null) {
								content.append('<');
								content.append('c');
								content.append('a');
								content.append((char) c);
							}
						}
					}else {
						if (content != null) {
							content.append('<');
							content.append('c');
							content.append((char) c);
						}
					}
				
				}else {
					if (content != null) {
						content.append('<');
						content.append((char) c);
					}
				}
			}

			c = buffer.read();
			if (content != null && (char) c != '<' && c != 13
					&& c != 13 && c != 10 && (char) c != '	')
				content.append((char) c);

		}
		return tableList;
	
	}
	
	/**
	 * This extracts all the tables of the html code
	 * @return An arrayList containing all the table
	 */
	public ArrayList<Table> extractTable() {
		ArrayList<Table> tableList = null;
		if (address != null) {
			URL url;
			URLConnection connection;
			InputStream in;
			int c;

			Table table = null;
			StringBuilder content = null;
			String tmp;
			ArrayList<String> columns = null;
			int counter = 0;

			try {

				url = new URL(this.address);
				
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(new URI(this.address));
				HttpResponse response = client.execute(request);

				String html = "";
				InputStream ins = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
				StringBuilder str = new StringBuilder();
				String line = null;
				while((line = reader.readLine()) != null)
				{
				    str.append(line);
				}
				ins.close();
				html = str.toString();
				
				
				connection = url.openConnection();
				
				in = connection.getInputStream();
				c = in.read();
				tableList = new ArrayList<Table>();
				while (c != -1) {
					switch (c) {
					case 60:
						// c == '<'
						c = in.read();
						if (c == (int) 't' || c == (int) 'T') {
							c = in.read();
							if (c == (int) 'a' || c == (int) 'A') {
								c = in.read();

								if (c == (int) 'b' || c == (int) 'B') {
									c = in.read();

									if (c == (int) 'l' || c == (int) 'L') {
										c = in.read();
										if (c == (int) 'e' || c == (int) 'E') {

											c = in.read();
											// here we use the while because we
											// don't care of the arguments
											while (c != -1) {
												if (c == (int) '>') {
													// here we are at string =
													// "<table>"
													if (table != null
															&& !table
																	.isClosed()) {

														table = new Table(
																counter, table);
														if (content != null) {
															String txt = "<table "
																	+ String
																			.valueOf(counter)
																	+ ">";

															for (int i = 0; i < txt
																	.length(); i++) {
																content
																		.append(txt
																				.charAt(i));
															}

														}
													} else {
														table = new Table(
																counter);
													}
													content = table
															.getContent();
													counter++;
													tableList.add(table);
													// stack.add(table);
													break;
												}

												c = in.read();
											}
										}else{
											if (content != null) {
												content.append('<');
												content.append('t');
												content.append('a');
												content.append('b');
												content.append('l');
												content.append((char) c);
											}
										}
									}else{
										if (content != null) {
											content.append('<');
											content.append('t');
											content.append('a');
											content.append('b');
											content.append((char) c);
										}
									}
								}else{
									if (content != null) {
										content.append('<');
										content.append('t');
										content.append('a');
										content.append((char) c);
									}
								}
							} else if (c == (int) 'r' || c == (int) 'R') {
								c = in.read();
								// here we use the while because we don't care of the arguments
								while (c != -1) {
									if (c == (int) '>') {
										// here we are at <tr>
										columns = table.addALine();
										break;
									}
									c = in.read();
									
								}
								
								
								
								//c = in.read();
								//if (c == (int) '>') {
									// here we are at <tr>
									//columns = table.addALine();
								//}
							} else if (c == (int) 'd' || c == (int) 'D') {
								c = in.read();
								// here we use the while because we don't care
								// of
								// the arguments
								while (c != -1) {
									if (c == (int) '>') {
										// here we are at <td>
										content = table.getContent();
										break;
									}
									c = in.read();
								}

							}else if(c == (int) 'h' || c == (int) 'H'){
								c = in.read();
								// here we use the while because we don't care
								// of
								// the arguments
								while (c != -1) {
									if (c == (int) '>') {
										// here we are at <th>
										// TODO Improve the header management
										content = table.getContent();
										table.setTableHeader(columns);
										break;
									}
									c = in.read();
								}
							}else{
								if (content != null) {
									content.append('<');
									content.append('t');
									content.append((char) c);
								}
							}
						} else if (c == (int) '/') {
							c = in.read();
							if (c == (int) 't' || c == (int) 'T') {
								c = in.read();
								if (c == (int) 'r' || c == (int) 'R') {
									c = in.read();
									if (c == (int) '>') {
										// here we are at </tr>
										// end of the line, not very important
										content.delete(0, content.length());
									}
								} else if (c == (int) 'd' || c == (int) 'D') {
									c = in.read();
									if (c == (int) '>') {
										// here we are at </td>
										tmp = content.toString();
										if (tmp.endsWith("<"))
											tmp = tmp.substring(0,
													tmp.length() - 1);
										columns.add(tmp);
										content.delete(0, content.length());
									}
								} else if (c == (int) 'a' || c == (int) 'A') {
									c = in.read();

									if (c == (int) 'b' || c == (int) 'B') {
										c = in.read();

										if (c == (int) 'l' || c == (int) 'L') {
											c = in.read();
											if (c == (int) 'e'
													|| c == (int) 'E') {
												c = in.read();
												if (c == (int) '>') {
													// here we are at "</table>"
													table.setClosed();
													if (table.hasParent()) {
														table = table
																.getParent();
														content = table
																.getContent();
														columns = table
																.getLastLine();
													}

												}
											}else{
												if (content != null) {
													content.append('<');
													content.append('/');
													content.append('t');
													content.append('a');
													content.append('b');
													content.append('l');
													content.append((char) c);
												}
											}
										}else{
											if (content != null) {
												content.append('<');
												content.append('/');
												content.append('t');
												content.append('a');
												content.append('b');
												content.append((char) c);
											}
										}
									}else{
										if (content != null) {
											content.append('<');
											content.append('/');
											content.append('t');
											content.append('a');
											content.append((char) c);
										}
									}
								}else if(c == (int) 'h' || c == (int) 'H'){
									c = in.read();
									if (c == (int) '>') {
										// here we are at </th>
										tmp = content.toString();
										if (tmp.endsWith("<"))
											tmp = tmp.substring(0,
													tmp.length() - 1);
										columns.add(tmp);
										content.delete(0, content.length());
									}
								}else if(c == (int) 'c' || c == (int) 'C'){
									c = in.read();
									if (c == (int) 'a' || c == (int) 'A') {
										c = in.read();
										if (c == (int) 'p' || c == (int) 'P') {
											c = in.read();
											if (c == (int) 't' || c == (int) 'T') {
												c = in.read();
												if (c == (int) 'i' || c == (int) 'I') {
													c = in.read();
													if (c == (int) 'o' || c == (int) 'O') {
														c = in.read();
														if (c == (int) 'n' || c == (int) 'N') {
															c = in.read();
															// here we use the while because we
															// don't care of the arguments
															while (c != -1) {
																if (c == (int) '>') {
																	// here we are at string =
																	// "</caption>"
																	
																	content = table
																			.getContent();
																										tableList.add(table);
																	
																	break;
																}

																c = in.read();
															}
														}else{
															if (content != null) {
																content.append('<');
																content.append('/');
																content.append('c');
																content.append('a');
																content.append('p');
																content.append('t');
																content.append('i');
																content.append('o');
																content.append((char) c);
															}
														}
													}else{
														if (content != null) {
															content.append('<');
															content.append('/');
															content.append('c');
															content.append('a');
															content.append('p');
															content.append('t');
															content.append('i');
															content.append((char) c);
														}
													}
												}else{
													if (content != null) {
														content.append('<');
														content.append('/');
														content.append('c');
														content.append('a');
														content.append('p');
														content.append('t');
														content.append((char) c);
													}
												}
											}else{
												if (content != null) {
													content.append('<');
													content.append('/');
													content.append('c');
													content.append('a');
													content.append('p');
													content.append((char) c);
												}
											}
										}else{
											if (content != null) {
												content.append('<');
												content.append('/');
												content.append('c');
												content.append('a');
												content.append((char) c);
											}
										}
									}else{
										if (content != null) {
											content.append('<');
											content.append('/');
											content.append('c');
											content.append((char) c);
										}
									}
								
								}
								else{
									if (content != null) {
										content.append('<');
										content.append('/');
										content.append('t');
										content.append((char) c);
									}
								}

							}else if(c == (int) 'c' || c == (int) 'C'){
								c = in.read();
								if (c == (int) 'a' || c == (int) 'A') {
									c = in.read();
									if (c == (int) 'p' || c == (int) 'P') {
										c = in.read();
										if (c == (int) 't' || c == (int) 'T') {
											c = in.read();
											if (c == (int) 'i' || c == (int) 'I') {
												c = in.read();
												if (c == (int) 'o' || c == (int) 'O') {
													c = in.read();
													if (c == (int) 'n' || c == (int) 'N') {
														c = in.read();
															if (c == (int) '>') {
																// here we are at string =
																// "</caption>"
																table.setName(content.toString());
																content.delete(0, content.length());
																
																
															}

															
														
													}
												}
											}
										}
									}
								}
							
							}
							else {
								if (content != null) {
									content.append('<');
									content.append('/');
									content.append((char) c);
								}
							}
						}else if(c == (int) 'c' || c == (int) 'C'){
							c = in.read();
							if (c == (int) 'a' || c == (int) 'A') {
								c = in.read();
								if (c == (int) 'p' || c == (int) 'P') {
									c = in.read();
									if (c == (int) 't' || c == (int) 'T') {
										c = in.read();
										if (c == (int) 'i' || c == (int) 'I') {
											c = in.read();
											if (c == (int) 'o' || c == (int) 'O') {
												c = in.read();
												if (c == (int) 'n' || c == (int) 'N') {
													c = in.read();
													// here we use the while because we
													// don't care of the arguments
													while (c != -1) {
														if (c == (int) '>') {
															// here we are at string =
															// "<caption>"
															
															content = table
																	.getContent();
																								tableList.add(table);
															
															break;
														}

														c = in.read();
													}
												}else {
													if (content != null) {
														content.append('<');
														content.append('c');
														content.append('a');
														content.append('p');
														content.append('t');
														content.append('i');
														content.append('o');
														content.append((char) c);
													}
												}
											}else {
												if (content != null) {
													content.append('<');
													content.append('c');
													content.append('a');
													content.append('p');
													content.append('t');
													content.append('i');
													content.append((char) c);
												}
											}
										}else {
											if (content != null) {
												content.append('<');
												content.append('c');
												content.append('a');
												content.append('p');
												content.append('t');
												content.append((char) c);
											}
										}
									}
									else {
										if (content != null) {
											content.append('<');
											content.append('c');
											content.append('a');
											content.append('p');
											content.append((char) c);
										}
									}
								}else {
									if (content != null) {
										content.append('<');
										content.append('c');
										content.append('a');
										content.append((char) c);
									}
								}
							}else {
								if (content != null) {
									content.append('<');
									content.append('c');
									content.append((char) c);
								}
							}
						
						}else {
							if (content != null) {
								content.append('<');
								content.append((char) c);
							}
						}
					}

					c = in.read();
					if (content != null && (char) c != '<' && c != 13
							&& c != 13 && c != 10 && (char) c != '	')
						content.append((char) c);

				}

			} catch (MalformedURLException e) {
				Log.e("ERR-ParseTable", "Wrong URL");
				return null;
			} catch (IOException e) {
				Log.e("ERR-ParseTable", "Connection error");
				return null;
			} catch (Exception e) {
				Log.e("ERR-ParseTable", e.getMessage());
				return null;
			}
			
		}else{
			Log.e("ERR-ParseTable", "You first need to specify an address for the html code.");
		}
		return tableList;


	}

	
}
