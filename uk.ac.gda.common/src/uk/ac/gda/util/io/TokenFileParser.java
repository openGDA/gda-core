/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Read a tabbed delimited file into columns. Each row must have
 * the same number of entries currently.
 * 
 * Assumes that files are small. For larger files use StreamTokenizer(Reader r) which is more efficient.
 */
public class TokenFileParser {

	private String token       = "\\t";
	private String commentChar = "#";
	private List<List<String>> data;
	private BufferedReader reader;
	/**
	 * @param url
	 * @throws IOException
	 */
	public TokenFileParser(final URL url) throws IOException {
		this(url.openStream());
	}
	
	/**
	 * @param file
	 * @throws IOException 
	 */
	public TokenFileParser(final File file) throws IOException {
		this(new FileInputStream(file));
	}
	
	/**
	 * NOTE Does not use StreamTokenizer so files need to be small.
	 * 
	 * @param unbuffered
	 * @throws IOException 
	 */
	public TokenFileParser(final InputStream unbuffered) throws IOException {
		
		this.data = new ArrayList<List<String>>(7);
		
		this.reader = new BufferedReader(new InputStreamReader(unbuffered, "US-ASCII"));
	}
	
	
	/**
	 * Must be called to parse the file.
	 * @throws IOException 
	 */
	public void parse() throws IOException {
		
		boolean firstLine = true;
		String l = null;
		while((l=reader.readLine())!=null) {
			
			if (getCommentChar()!=null && l.startsWith(getCommentChar())) continue;
			
			final String[] line = l.trim().split(token);
			
			int index = 0;
			for (int i = 0; i < line.length; i++) {
				final String value = line[i].trim();
				if ("".equals(value)) continue;
				if (firstLine) data.add(new ArrayList<String>(31));
				final List<String> d = data.get(index);
				d.add(value);
				++index;
			}
			firstLine = false;
		}
	}
	
	/**
	 * @param icol
	 * @return the exact column, modifying it will change the data
	 */
	public List<String> getColumn(final int icol) {
		return data.get(icol);
	}
	
	@Override
	public String toString() {
		
		final StringBuilder buf = new StringBuilder();
		final int size = data.get(0).size();
		for (int i = 0; i < size; i++) {
			for (List<String> d : data) {
				buf.append("\t");
				buf.append(d.get(i));
			}
			buf.append("\n");
		}
		
		return buf.toString();
	}
	
	/**
	 * Testing only
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String [] args) throws Exception {
		final File file = new File("/home/fcp94556/workspace/uk.ac.gda.core/src/gda/gui/exafs/Element-CoreHole.txt");
		final TokenFileParser p = new TokenFileParser(file);
		System.out.println(p);
	}

	/**
	 * Exceptions can be thrown if the column cannot parse as double
	 * @param i
	 * @return Double[]
	 */
	public Double[] getColumnAsDoubleArray(int i, int... ignoreRows) {
		final List<Double> ret = getColumnAsDoubleList(i, ignoreRows);
		
		return ret.toArray(new Double[ret.size()]);
	}

	/**
	 * @param i
	 * @param ignoreRows
	 * @return column as list of Doubles
	 */
	public List<Double> getColumnAsDoubleList(int i, int... ignoreRows) {
		
		final List<Integer> ig    = new ArrayList<Integer>(ignoreRows.length);
		for (int j = 0; j < ignoreRows.length; j++) ig.add(ignoreRows[j]);
		
		final List<String> values = data.get(i);
		final List<Double> ret    = new ArrayList<Double>(values.size());
		for (int row = 0; row < values.size(); row++) {
			if (ig.contains(row)) continue;
			ret.add(Double.parseDouble(values.get(row)));
		}
		return ret;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getCommentChar() {
		return commentChar;
	}

	public void setCommentChar(String commentChar) {
		this.commentChar = commentChar;
	}


}
