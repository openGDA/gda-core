/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.beamline.synoptics.utils;

import org.apache.commons.vfs.FileChangeEvent;
import org.apache.commons.vfs.FileListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomFileListener implements FileListener {
	private static final Logger logger=LoggerFactory.getLogger(CustomFileListener.class);
	
	@Override
	public void fileChanged(FileChangeEvent arg0) throws Exception {
		logger.info("File {}: changed", arg0.getFile());

	}

	@Override
	public void fileCreated(FileChangeEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		logger.info("File {}: created", arg0.getFile());

	}

	@Override
	public void fileDeleted(FileChangeEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		logger.info("File {}: deleted", arg0.getFile());

	}

}
