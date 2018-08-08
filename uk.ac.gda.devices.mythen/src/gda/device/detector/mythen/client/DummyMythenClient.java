/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.mythen.client;

import gda.device.DeviceException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * Dummy implementation of {@link MythenClient}.
 */
public class DummyMythenClient implements MythenClient {

	private int numberOfModules;
	
	/**
	 * Creates a dummy client.
	 * 
	 * @param numberOfModules number of modules
	 */
	public DummyMythenClient(int numberOfModules) {
		this.numberOfModules = numberOfModules;
	}
	
	@Override
	public void acquire(AcquisitionParameters params) throws DeviceException {
		Random rand = new Random();
		try {
			PrintWriter pw = new PrintWriter(params.getFilename());
			for (int channel=0; channel<numberOfModules*1280; channel++) {
				int count = rand.nextInt(1001);
				pw.printf("%d %d\n", channel, count);
			}
			pw.close();
		} catch (IOException ioe) {
			throw new DeviceException("Unable to save data", ioe);
		}
	}

}
