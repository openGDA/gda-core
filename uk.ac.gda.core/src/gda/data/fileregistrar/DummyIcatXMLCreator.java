/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.data.fileregistrar;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyIcatXMLCreator implements ArchiveFileCreator {

	private static final Logger logger = LoggerFactory.getLogger(DummyIcatXMLCreator.class);

	@Override
	public void registerFiles(String datasetId, String[] files) {
		logger.info("Registering datasetId: {}, files: {}", datasetId, Arrays.asList(files));
	}

	@Override
	public void setDirectory(String directory) {
		// Not required to do anything
	}

}
