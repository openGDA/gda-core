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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NXSubEntryWriter extends DataWriterExtenderBase{
	private static final Logger logger = LoggerFactory.getLogger(NXSubEntryWriter.class);

	String filename;

	private NXLinkCreator linkCreator;
	
	
	public NXSubEntryWriter(NXLinkCreator linkCreator) {
		super();
		this.linkCreator = linkCreator;
	}

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		filename = new File(dataPoint.getCurrentFilename()).getAbsolutePath();
	}

	@Override
	public void completeCollection(IDataWriterExtender parent) {
		/**
		 * add sub-entry section with links to other
		 */
		try {
			linkCreator.makelinks(filename);
		} catch (Throwable e) {
			logger.error("Error making links in " + filename, e);
		}

	}

};

