/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.scan.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomLineFileWriter {
	private FileWriter writer;
	private Logger logger = LoggerFactory.getLogger(RandomLineFileWriter.class);
	private int lastWrittenLineNumber =-1;
	private Hashtable<Integer, String> linesBuffer;
	private int totalLinesBuffered;
	public void createRandomLineFile(String string) {
		try {
			writer = new FileWriter(new File(string));
			linesBuffer = new Hashtable<Integer, String>();
		} catch (IOException e) {
			logger.error("unable to create the rgb file " + string);
		}

	}

	public void addHeader( String string) throws IOException {
		writer.write(string + "\n");
		writer.flush();
	}
	public void addToFile(int lineNumber, String string) throws IOException {
		if (writer != null) {
			if(lineNumber == lastWrittenLineNumber + 1)
			{
				writeToFile(lineNumber,string);
			}
			else{
				linesBuffer.put(lineNumber, string);
				totalLinesBuffered = linesBuffer.size();
				writeLinesFromBuffer();
			}
			writer.flush();
		}
	}

	private void writeToFile(int lineNumber, String string) throws IOException
	{
		writer.write(string + "\n");
		writer.flush();
		lastWrittenLineNumber = lineNumber;
	}

	private void writeLinesFromBuffer() throws IOException {

	for(int i =lastWrittenLineNumber + 1 ;; i++)
	{
		String lineToWrite = linesBuffer.get(i);
		if(lineToWrite != null)
		{
			writeToFile(i, lineToWrite);
			linesBuffer.remove(i);
			totalLinesBuffered = linesBuffer.size();
		}
		else
			break;
		if(totalLinesBuffered == 0)
			break;
	}

	}

	public void close() throws IOException
	{
		if(totalLinesBuffered != 0)
			writeLinesFromBuffer();
		if(writer != null)
			writer.close();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
