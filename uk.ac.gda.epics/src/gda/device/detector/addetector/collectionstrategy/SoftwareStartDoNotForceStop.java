/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.collectionstrategy;

import gda.jython.JythonServerFacade;

/**
 * TBD
 */
public class SoftwareStartDoNotForceStop extends SoftwareStartStop {

	private int timeoutSeconds = 300;

	@Override
	protected void rawCompleteCollection() throws Exception {
		final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
		jythonServerFacade.print(String.format("Cannot abort detector mid acquisition, instead waiting %d seconds for acquisition to finish", timeoutSeconds));
		try {
			getAdBase().waitForDetectorStateIDLE(timeoutSeconds);
		} catch (Exception e) {
			jythonServerFacade.print(String.format("Detector did not stop after %d seconds. Aborting mid collection, this will likely kill the IOC", timeoutSeconds));
			getAdBase().stopAcquiring();
		}
	}

	// Class properties

	public int getTimeoutSeconds() {
		return timeoutSeconds;
	}

	public void setTimeoutSeconds(int timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}

}