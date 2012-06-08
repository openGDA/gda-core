/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeBuilder;
import gda.data.nexus.tree.NexusTreeNodeSelection;
import gda.device.Detector;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.scan.IScanDataPoint;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of DataWriterExtender
 */
public class AsciiWriterExtender extends DataWriterExtenderBase {
	
	protected String headerPrefix;
	protected String filenameTemplate;
	protected List<AsciiWriterExtenderConfig> configs;
	protected Writer out = null;
	protected String sep;
	protected NexusTreeNodeSelection metaDataSelectionTree = null;
	protected boolean metaDataAsXML;
	
	protected Map<String, Double> variableToDoubleMap = new HashMap<String, Double>(31);
	protected Map<String, String> fieldToVariableMap  = new HashMap<String, String>(31);
	/**
	 * @param filename
	 *            - name of file to create
	 * @param configs
	 *            - list of scannables to output, format
	 * @param sep
	 * @param metaDataSelectionTree
	 *            NexusTreeNodeSelection - selection tree for metadata if null all the metaData is put into the file
	 * @param metaDataAsXML
	 *            true if metaData is to be output as XML - otherwise using toTxt method
	 */
	public AsciiWriterExtender(String filename, List<AsciiWriterExtenderConfig> configs, String sep,
			                   NexusTreeNodeSelection metaDataSelectionTree,
			                   boolean metaDataAsXML) {
		
		this.filenameTemplate = filename;
		this.configs = configs;
		this.sep = sep;
		this.metaDataSelectionTree = metaDataSelectionTree;
		this.metaDataAsXML = metaDataAsXML;
	}


	/**
	 * Add a scan variable to the writer which will then be available to write to the file.
	 * @param varName
	 * @param scannableName
	 * @param fieldIndex
	 */
	public void addVariable(String varName, String scannableName, int fieldIndex) {
		
		final Scannable scannable = Finder.getInstance().find(scannableName);
		final String    fieldName;
		if (scannable != null) {
			if (scannable instanceof Detector) {
				 fieldName = scannable.getExtraNames()[fieldIndex];
			} else {
				 fieldName = scannable.getInputNames()[fieldIndex];
			}
	 	} else { // Done to make pauls unit test work, normally scannable name if
	 		     // not findable should not be allowed.
	 		fieldName = scannableName;
	 	}	
		fieldToVariableMap.put(fieldName, varName);
	}
	
	private void storeScannable(String name, Double position) {
		/* if scannable is required get value and add to variables */
		if (fieldToVariableMap.containsKey(name)) {
			variableToDoubleMap.put(fieldToVariableMap.get(name), position);
		}
	}

	protected void wl(String msg) throws IOException {
		if (out!=null) out.write(msg + "\n");
	}

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		
		if (out == null) {
			out = new BufferedWriter(new FileWriter(getFileName(parent)));
			if (parent instanceof NexusDataWriter) {
				String nexusfile = ((NexusDataWriter) parent).getCurrentFileName();
				INexusTree tree = NexusTreeBuilder.getNexusTree(nexusfile,
						metaDataSelectionTree != null ? metaDataSelectionTree : NexusTreeNodeSelection
								.createTreeForAllMetaData());
				if (tree != null) {
					wl(metaDataAsXML ? tree.toXML(true, false) : tree.toText("", "=", "/", "|"));
				}
			}
			StringBuffer line = new StringBuffer();
			for (AsciiWriterExtenderConfig config : configs) {
				line.append(config.label);
				line.append(sep);
			}
			if (line.length() > 0) {
				wl(line.toString());
			}
		}
		
		Double[] positions = dataPoint.getAllValuesAsDoubles();
		String[] headerString = dataPoint.getNames().toArray(new String[0]);
		
		for (int i = 0; i < headerString.length; i++){
			storeScannable(headerString[i], positions[i]);
		}
		
		
		/* evaluate the expressions in the Configs */
		StringBuffer line = new StringBuffer();
		for (AsciiWriterExtenderConfig config : configs) {
			line.append(config.getBufferEntry(variableToDoubleMap));
			line.append(sep);
		}

		if (line.length() > 0) {
			wl(line.toString());
		}
		/*
		 * Handle detectors later for (Detector detector : dataPoint.getDetectors()) { if(detector instanceof
		 * IMultiDetector){ for( ISubDetector subDetector : ((IMultiDetector)detector)){ writeSubDetector(detector,
		 * subDetector); } } else { writeDetector(detector); } }
		 */

		out.flush();

		super.addData(parent, dataPoint);
	}

	/**
	 * Override to format the current file name if required.
	 * By default the file name passed into the constructor
	 * is used directly however if you allow %d for the scan
	 * number as with NexusDataWriter this getFileName() method
	 * may be overridden.
	 * 
	 * @return fileName
	 */
	protected String getFileName(@SuppressWarnings("unused") IDataWriterExtender parent) {
		return filenameTemplate;
	}
	
	@Override
	public void completeCollection() {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				// do nothing
			}
			out = null;
		}
		super.completeCollection();
	}

	protected void writeVariableToDoubleMap(Scannable scannable, Object position) {
		variableToDoubleMap.put(scannable.getName(), (Double)position);
		
	}

	/**
	 * @return Returns the headerPrefix.
	 */
	public String getHeaderPrefix() {
		return headerPrefix;
	}

	/**
	 * @param headerPrefix The headerPrefix to set.
	 */
	public void setHeaderPrefix(String headerPrefix) {
		this.headerPrefix = headerPrefix;
	}
}
