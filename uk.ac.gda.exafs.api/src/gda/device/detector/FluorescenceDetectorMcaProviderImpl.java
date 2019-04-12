/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.device.detector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FindableBase;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.jython.JythonStatus;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.devices.detector.FluorescenceDetector;

@ServiceInterface(FluorescenceDetectorMcaProvider.class)
public class FluorescenceDetectorMcaProviderImpl extends FindableBase implements FluorescenceDetectorMcaProvider {
	private static final Logger logger = LoggerFactory.getLogger(FluorescenceDetectorMcaProviderImpl.class);

	@Override
	public double[][] getMCAData(String detectorName, double time) throws DeviceException {
		logger.debug("getMCAData called , time = {} sec,", time);
		if (isScriptOrScanIsRunning())  {
			logger.debug("Returning empty MCA data - scan or script is running");
			return new double[0][0];
		}
		FluorescenceDetector detector = Finder.getInstance().find(detectorName);
		if (detector != null) {
			logger.debug("Returning MCA data from {}", detector.getName());
			return detector.getMCAData(time);
		}
		return new double[0][0];
	}

	@Override
	public boolean canGetMcaData() {
		return !isScriptOrScanIsRunning();
	}

	private boolean isScriptOrScanIsRunning() {
		return JythonServerFacade.getInstance().getScanStatus() != JythonStatus.IDLE ||
			   JythonServerFacade.getInstance().getScriptStatus() != JythonStatus.IDLE;
	}
}
