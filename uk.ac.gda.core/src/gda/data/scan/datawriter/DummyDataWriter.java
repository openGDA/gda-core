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

import gda.scan.IScanDataPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyDataWriter extends DataWriterBase {
	private static final Logger logger = LoggerFactory.getLogger(DummyDataWriter.class);
	int scanId;

	@Override
	public String getCurrentFileName() {
		return "filename";
	}

	@Override
	public int getCurrentScanIdentifier() {
		return scanId;
	}

	@Override
	public void setHeader(String header) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addData(IScanDataPoint newData) throws Exception {
		String s = "ScanDataPoint " + newData +" = " + newData.toFormattedString();
		logger.info(s);
		System.out.println(s);
		super.addData(newData);
	}

	@Override
	public void configureScanNumber(int scanNumber) {
		scanId = scanNumber;
	}

}