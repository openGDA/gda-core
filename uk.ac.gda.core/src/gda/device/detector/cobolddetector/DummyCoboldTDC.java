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

package gda.device.detector.cobolddetector;

import gda.util.GDALogger;

import java.io.File;
import java.io.IOException;

/**
 * DummyCoboldTDC Class
 */
public class DummyCoboldTDC extends CoboldTDC {
	private String dummyDataFile = null;

	/**
	 * @return data file name
	 */
	public String getDummyDataFile() {
		return dummyDataFile;
	}

	/**
	 * @param fn
	 *            sets the filename
	 */
	public void setDummyDataFile(String fn) {
		dummyDataFile = fn;
	}

	@Override
	public void startCoboldScan(String newCommand) {
		if (!new File(lmfName).exists())
			try {
				new File(lmfName).createNewFile();
			} catch (IOException e) {
				GDALogger.warn("DummyCoboldTDEC: error creating dummy LMF file ");
			}
		super.startCoboldScan("new \"" + dummyDataFile + "\"");
	}
}
