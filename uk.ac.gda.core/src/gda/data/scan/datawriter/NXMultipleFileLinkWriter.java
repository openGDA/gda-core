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

package gda.data.scan.datawriter;

import gda.scan.IScanDataPoint;

import java.io.File;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NXMultipleFileLinkWriter extends DataWriterExtenderBase{
	private static final Logger logger = LoggerFactory.getLogger(NXMultipleFileLinkWriter.class);

	private Vector<String> externalfilenames;
	private String filename;
	private NXLinkCreator linkCreator;

	private Vector<String> detectorHeader;
	
	public NXMultipleFileLinkWriter(NXLinkCreator linkCreator) {
		super();
		this.linkCreator = linkCreator;
	}

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		filename=new File(dataPoint.getCurrentFilename()).getAbsolutePath();
		Vector<Object> data = dataPoint.getDetectorData();
		detectorHeader = dataPoint.getDetectorHeader();
		for (Object name : data)
			if (name instanceof String)
				externalfilenames.add(name.toString());
	}

	@Override
	public void completeCollection(IDataWriterExtender parent) {
		/**
		 * add sub-entry section with links to other
		 */
		try {
			for (int i=0; i<externalfilenames.size(); i++) {
				String filename=externalfilenames.get(i);
				String header=detectorHeader.get(i);
				linkCreator.addLink(header, filename);
			}
			linkCreator.makelinks(filename);
		} catch (Throwable e) {
			logger.error("Error making links in " + filename, e);
		}
	}

}