/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.volimage;

import gda.observable.IObservable;
import gda.observable.IObserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.TransferFunctionView;

/**
 *
 */
@Deprecated
public class CommandClient implements IObservable, IObserver {

	private Socket socket = null;
	private boolean isConnected = false;
	private PrintWriter writer = null;
	private BufferedReader reader = null;
	private LinkedList<IObserver> observerList = null;
	private final static int TOTALNUMTRIES = 15;
	private Logger logger = LoggerFactory.getLogger(ImageStreamReader.class);
	
	/**
	 * @param hostname
	 * @param portNumber
	 */
	public CommandClient(String hostname, int portNumber)
	{
		observerList = new LinkedList<IObserver>();
		int numTries = TOTALNUMTRIES;
		while (numTries > 0) {
			try {
				socket = new Socket(hostname, portNumber);
				if (socket.isConnected()) {
					writer = new PrintWriter(socket.getOutputStream());
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					isConnected = true;	
					numTries = 0;
				}
			} catch (Exception ex) {
				numTries--;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (numTries == 0 && isConnected == false)
			logger.error("Could not connect to commandserver # of tries exceeded");
			
	}
	
	/**
	 * Load a single block raw volume file
	 * @param rawfile filename of the raw file (including absolute path)
	 * @param xsize x dimension size of the raw volume
	 * @param ysize y dimension size of the raw volume
	 * @param zsize z dimension size of the raw volume
	 */
	public void loadRawVolume(String rawfile, 
							  int headerSize,
							  int voxelType,
							  int xsize, 
							  int ysize, 
							  int zsize)
	{
		String command = "LoadRawvolume "+ rawfile + " " + headerSize + " " + voxelType + " " + xsize + " " + ysize + " " + zsize;
		if (writer != null && isConnected())
		{
			writer.print(command);
			writer.flush();
			try {
				String response = reader.readLine();
				if (!response.equals("OK")) {
					System.err.println(response);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}

	/**
	 * Load a single block Diamond Scisoft raw volume file
	 * @param dsrfile filename of the dsr file (including absolute path)
	 */	

	public void loadDSRVolume(String dsrfile) {
		String command = "LoadDSRVolume "+dsrfile;
		if (writer != null && isConnected())
		{
			writer.print(command);
			writer.flush();
			try {
				String response = reader.readLine();
				if (!response.equals("OK")) {
					System.err.println(response);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void notifyObservers(ArrayList<Integer> listOfValues)
	{
		Iterator<IObserver> iter = observerList.iterator();
		while (iter.hasNext())
		{
			IObserver ob = iter.next();
			ob.update(this, listOfValues);
		}
	}
	/**
	 * 
	 */
	public void getHistogram() {
		String command = "GetHistogram";
		if (writer != null && isConnected())
		{
			writer.print(command);
			writer.flush();
			try {
				String response = reader.readLine();
				System.err.println(response);
				ArrayList<Integer> list = new ArrayList<Integer>();
				if (response.startsWith("["))
				{
					String chuckedStr = response.substring(1);
					int index = chuckedStr.indexOf(',');
					String numberStr = "";
					while (index != -1) {
						numberStr = chuckedStr.substring(0,index);
						chuckedStr = chuckedStr.substring(index+1);
						index = chuckedStr.indexOf(',');
						list.add(Integer.parseInt(numberStr));
					}
					if (list.size() > 0)
						notifyObservers(list);
				} else
					System.err.println(response);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	

	/**
	 * @param oldMouseX
	 * @param oldMouseY
	 * @param newMouseX
	 * @param newMouseY
	 */
	public void rotateVolume(float oldMouseX, float oldMouseY, float newMouseX, float newMouseY)
	{
		String command = "Rotate "+oldMouseX+" "+oldMouseY+" "+newMouseX+" "+newMouseY;
		if (writer != null && isConnected())
		{
			writer.print(command);
			writer.flush();
			try {
				String response = reader.readLine();
				if (!response.equals("OK")) {
					System.err.println(response);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * @param zoomValue
	 */
	public void zoomVolume(float zoomValue)
	{
		String command = "Zoom "+zoomValue;
		if (writer != null && isConnected())
		{
			writer.print(command);
			writer.flush();
			try {
				String response = reader.readLine();
				if (!response.equals("OK")) {
					System.err.println(response);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * @param xDelta
	 * @param yDelta
	 */
	public void translate(float xDelta, float yDelta)
	{
		String command = "Translate "+xDelta+" "+yDelta;
		if (writer != null && isConnected())
		{
			writer.print(command);
			writer.flush();
			try {
				String response = reader.readLine();
				if (!response.equals("OK")) {
					System.err.println(response);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * @param newMode
	 */
	public void setDisplayMode(int newMode) {
		String command = "SetDisplayMode " + newMode;
		if (writer != null && isConnected())
		{
			writer.print(command);
			writer.flush();
			try {
				String response = reader.readLine();
				if (!response.equals("OK")) {
					System.err.println(response);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * @param isovalue
	 */
	
	public void setIsoValue(float isovalue) {
		String command = "SetIsovalue " + isovalue;
		if (writer != null && isConnected())
		{
			writer.print(command);
			writer.flush();
			try {
				String response = reader.readLine();
				if (!response.equals("OK")) {
					System.err.println(response);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * @param directory
	 * @param prefix
	 * @param numSlices
	 */
	public void loadSlicedVolume(String directory, String prefix, int numSlices)
	{
		String command = "Loadvolume " + directory + " " + prefix + " " + numSlices;
		if (writer != null && isConnected()) 
		{
			writer.print(command);
			writer.flush();
			try {
				String response = reader.readLine();
				if (!response.equals("OK")) {
					System.err.println(response);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * @param startX
	 * @param startY
	 * @param startZ
	 * @param endX
	 * @param endY
	 * @param endZ
	 */
	public void setROI(String startX, String startY, String startZ, 
					   String endX, String endY, String endZ)
	{
		String command = "SetROI " + startX + " " + startY + " " + startZ +
		                 " "+endX+" "+endY+" "+endZ;
		if (writer != null && isConnected())
		{
			writer.print(command);
			writer.flush();
			try {
				String response = reader.readLine();
				if (!response.equals("OK")) {
					System.err.println(response);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Determine if the Command client is connected to the server or not
	 * @return if yes return true otherwise false
	 */
	
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public void addIObserver(IObserver observer) {
		observerList.add(observer);		
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observerList.remove(observer);
	}

	@Override
	public void deleteIObservers() {
		observerList.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Object source, Object arg) {
		if (source instanceof TransferFunctionView)
		{
			LinkedList<IDataset> transferList = (LinkedList<IDataset>)arg;
			if (transferList.size() == 4)
			{
				IDataset redChannel = transferList.get(0);
				IDataset greenChannel = transferList.get(1);
				IDataset blueChannel = transferList.get(2);
				IDataset alphaChannel = transferList.get(3);
				String command = "StartTransferfunction";
				if (writer != null && isConnected())
				{
					writer.print(command);
					writer.flush();
					try {
						String response = reader.readLine();
						if (!response.equals("OK")) {
							System.err.println(response);
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}					
					command = "[";
					for (int i = 0; i < redChannel.getShape()[0]; i++)
					{
						double red = redChannel.getDouble(i);
						double green = greenChannel.getDouble(i);
						double blue = blueChannel.getDouble(i);
						double alpha = alphaChannel.getDouble(i);
						command += "" + red + "," + green + "," + blue + "," + alpha;
						if (i != redChannel.getShape()[0]-1)
							command += ",";					
					}
					command += "]";
					System.out.println(command.length());
					if (writer != null && isConnected()) 
					{
						writer.print(command);
						writer.flush();
					}
				}				
			}
		}		
	}
	
}
