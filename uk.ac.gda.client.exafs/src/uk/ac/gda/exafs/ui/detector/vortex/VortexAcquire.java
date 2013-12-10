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

package uk.ac.gda.exafs.ui.detector.vortex;

import gda.device.Timer;
import gda.device.XmapDetector;
import uk.ac.gda.exafs.ui.detector.Acquire;

public class VortexAcquire extends Acquire {
	private int[][][] data3d;
	
	protected void acquire(double collectionTime, XmapDetector xmapDetector, Timer tfg) throws Exception {
		xmapDetector.clearAndStart();
		tfg.countAsync(collectionTime);
		xmapDetector.stop();
		xmapDetector.waitWhileBusy();
		int[][] data = xmapDetector.getData();
		data3d = convert2DTo3DArray(data);
	}
	
	public int[][][] getData3d() {
		return data3d;
	}

	protected int[][][] convert2DTo3DArray(int[][] data) {
		int[][][] ret = new int[data.length][1][];
		for (int i = 0; i < data.length; i++)
			ret[i][0] = data[i];
		return ret;
	}

}