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

import gda.data.PathConstructor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.Scannable;
import gda.device.detector.xspress.Xspress2System;
import gda.scan.IScanDataPoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import uk.ac.gda.beans.xspress.DetectorElement;

/**
 *
 */
public class XasDataWriterExtender extends AsciiWriterExtender {

	private List<String> header;
	/**
	 * @param filename
	 * @param configs
	 * @param sep
	 * @param header 
	 * @param metaDataAsXML
	 */
	public XasDataWriterExtender(String filename, List<AsciiWriterExtenderConfig> configs, String sep,
			                     List<String> header, boolean metaDataAsXML) {
		super(filename, configs, sep, null, metaDataAsXML);
		this.header = header;
	}

	/**
	 * Write out specific element of the xspress2 system scalers
	 *
	 * @see gda.data.scan.datawriter.AsciiWriterExtender#writeVariableToDoubleMap(gda.device.Scannable, java.lang.Object)
	 */
	@Override
	public void writeVariableToDoubleMap(Scannable scannable, Object data) {
			for (DetectorElement detectorElement : ((Xspress2System) scannable).getDetectorList()) {
				int j = 0;
				for (String label : detectorElement.getLabels()) {
					String inputName = detectorElement.getName() + "_" + label;
					if (fieldToVariableMap.containsKey(inputName)) {
						int index = (detectorElement.getNumber()-1) * 4 + j;
						@SuppressWarnings("unchecked")
						HashMap<String, NexusGroupData > positions = (HashMap<String, NexusGroupData >) data;
						NexusGroupData ngd = positions.get("scaler");
						int[] idata = (int[]) ngd.getBuffer();
						long lvalue = idata[index];
						if (lvalue < 0) lvalue = (lvalue << 32) >>> 32;
						variableToDoubleMap.put(inputName, new Double(lvalue));
					}
					j++;
				}
			}
	}

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		if (out == null) {
			final String dataDir = PathConstructor.createFromDefaultProperty();
			final File   file    = new File(dataDir+"/"+getFileName(parent));
			file.getParentFile().mkdirs();
			if (!file.exists()) {
				file.createNewFile();
			}
			out = new BufferedWriter(new FileWriter(file));

			if (header!=null){ 
				for (String line : header) {
					if (getHeaderPrefix()!=null) {
						line = getHeaderPrefix()+" "+line;
					}
					wl(line);
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
		super.addData(parent, dataPoint);
	}
	
	@Override
	protected String getFileName(final IDataWriterExtender parent) {
		
		try {
			if (parent instanceof DataWriter) {
				final String runNum   = ((DataWriter)parent).getCurrentScanIdentifier();
				final String fileName = String.format(filenameTemplate,  Integer.parseInt(runNum));
				return fileName;
			}
		} catch (Exception ignored) {
			// if getCurrentScanIdentifier is not a number we do not do the 
			// format at all.
		}
		
		return filenameTemplate;
	}
	
	@Override
	public void completeCollection() {
      // Do nothing
	}
	
	/**
	 * Called from ExafsScan
	 */
	public void close() {
		super.completeCollection();
	}
	
	/**
	 * Can be used to manually write lines to the ascii file.
	 * Has no effect on the nexus file.
	 * @param line
	 * @throws IOException 
	 */
	public void writeLine(final String line) throws IOException {
		wl(line);
	}

}
