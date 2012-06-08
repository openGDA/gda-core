/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data.scan.datawriter;

import gda.jython.ICommandRunner;
import gda.jython.ICurrentScanController;
import gda.jython.ICurrentScanInformationHolder;
import gda.jython.IDefaultScannableProvider;
import gda.jython.IJythonNamespace;
import gda.jython.IJythonServerNotifer;
import gda.jython.IScanStatusHolder;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for datahandler objects
 */
public abstract class DataWriterBase implements DataWriter, IDataWriterExtender {

	private static final Logger logger = LoggerFactory.getLogger(DataWriterBase.class);

	/**
	 * when arrays of data written to file, this will be placed between each element
	 */
	public static String delimiter = "\t";

	protected ArrayList<String> header = new ArrayList<String>();

	final protected ICurrentScanInformationHolder currentScanHolder;
	final protected IJythonServerNotifer jythonServerNotifer;
	final protected IDefaultScannableProvider defaultScannableProvider;
	final protected ICommandRunner commandRunner;
	final protected ICurrentScanController currentScanController;
	final protected ITerminalPrinter terminalPrinter;
	final protected IScanStatusHolder scanStatusHolder;
	final protected IJythonNamespace jythonNamespace;

	private DataWriterExtenderBase extender; 
	
	/**
	 * 
	 */
	public DataWriterBase(){
		currentScanHolder = InterfaceProvider.getCurrentScanInformationHolder();
		jythonServerNotifer = InterfaceProvider.getJythonServerNotifer();
		defaultScannableProvider = InterfaceProvider.getDefaultScannableProvider();
		commandRunner = InterfaceProvider.getCommandRunner();
		currentScanController = InterfaceProvider.getCurrentScanController();
		terminalPrinter = InterfaceProvider.getTerminalPrinter();
		scanStatusHolder = InterfaceProvider.getScanStatusHolder();
		jythonNamespace = InterfaceProvider.getJythonNamespace();
		extender = new DataWriterExtenderBase();
	}

	/**
	 * To help write data to a data file. If its an array, then writes out each element of the array with a delimiter
	 * mark between each.
	 * 
	 * @param dataPoint
	 *            An element of a vector passed from a scan object
	 * @param isFirst
	 *            if a leading delimiter should be omitted.
	 * @return String
	 */
	public static String getDetectorData(Object dataPoint, boolean isFirst) {
		StringBuilder output = new StringBuilder();
		Class<? extends Object> dataClass = dataPoint.getClass();
		
		//TODO if possible (i.e. this information is not the only written to file) enforce reasonable limit on array length
		// if this was overwritten in the actual datawriter and not used from the base the first decision would be easy to make
		if (dataClass.isArray()) {
			// first check to see if its any an AbstractCollection
			if (dataPoint instanceof AbstractCollection<?>) {
				for (Object temp2 : (AbstractCollection<?>) dataPoint) {
					if (!isFirst) {
						output.append(delimiter);
					}
					output.append(temp2.toString());
					isFirst = false;
				}
			}
			// next it could be an array of objects
			else if (dataPoint instanceof Object[]) {
				List<Object> list = Arrays.asList((Object[]) dataPoint);
				for (Object element : list) {
					output.append(getDetectorData(element, isFirst));
					isFirst = false;
				}
			}
			// else its an array of native types. Use reflection here.
			else {
				int length = Array.getLength(dataPoint);
				for (int i = 0; i < length; i++) {
					if (!isFirst) {
						output.append(delimiter);
					}
					output.append(Array.get(dataPoint, i).toString());
					isFirst = false;
				}
			}
		} else if (dataPoint instanceof Vector<?>) {
			for (Object o : (Vector<?>) dataPoint) {
				output.append(getDetectorData(o, isFirst));
				isFirst = false;
			}
		}
		// else print straight to string
		else {
			if (!isFirst) {
				output.append(delimiter);
			}
			output.append(dataPoint.toString());
			isFirst = false;
		}
		return output.toString();
	}

	/**
	 * Returns the parts of this element of data as a string array.
	 * 
	 * @param dataPoint
	 *            The element of a vector from a scan
	 * @return String[]
	 */
	public static String[] getDetectorData(Object dataPoint) {
		String[] output = null;
		// first check to see if its any an AbstractCollection
		if (dataPoint instanceof AbstractCollection<?>) {
			output = new String[((AbstractCollection<?>) dataPoint).size()];

			int j = 0;
			for (Object obj : (AbstractCollection<?>) dataPoint) {
				output[j] = obj.toString();
				j++;
			}
		}
		// next it could be an array of objects
		else if (dataPoint instanceof Object[]) {
			List<Object> list = Arrays.asList((Object[]) dataPoint);
			output = new String[list.size()];
			int j = 0;
			for (Object obj : list) {
				output[j] = obj.toString();
				j++;
			}
		}
		// else its an array of native types. Use reflection here.
		else {
			int length = Array.getLength(dataPoint);
			output = new String[length];
			for (int i = 0; i < length; i++) {
				output[i] = Array.get(dataPoint, i).toString();
			}
		}
		return output;
	}

	@Override
	public void setHeader(ArrayList<String> header) {
		this.header = header;
	}
	
	@Override
	public ArrayList<String> getHeader() {
		return header;
	}

	@Override
	public void addData(IScanDataPoint newData) throws Exception {
			try {
				extender.addData(this, newData);
			} catch (Exception e) {
				// we ignore that error, we cannot pass it on. Logging is good however
				logger.error("Exception in extender.addData: ", e);
			}
	}

	@Override
	public void addDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		extender.addDataWriterExtender(dataWriterExtender);
	}

	@Override
	public void completeCollection() throws Exception {
		extender.completeCollection(this);
	}

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		extender.addData(parent, dataPoint);
	}

	@Override
	public void completeCollection(IDataWriterExtender parent) {
		extender.completeCollection(parent);
	}

	@Override
	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		extender.removeDataWriterExtender(dataWriterExtender);	
	}
	
	/**
	 * If filenumber not set then read from scandatapoint or from num tracker
	 * @throws InstantiationException 
	 */
	@Override
	public void configureScanNumber(Long scanNumber) throws Exception {
		// do nothing - provide base implementation
		
	}	
}