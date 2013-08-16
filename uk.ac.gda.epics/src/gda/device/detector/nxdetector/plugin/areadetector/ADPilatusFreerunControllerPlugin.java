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

package gda.device.detector.nxdetector.plugin.areadetector;

import gda.device.detector.NXDetector;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADDriverPilatus.PilatusTriggerMode;
import gda.device.detector.nxdetector.NXPlugin;
import gda.device.detector.nxdetector.plugin.NullNXPlugin;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

/**
 * An {@link NXPlugin} implementation that has no effect during a scan but exposes a new {@link #start(double)} method
 * to start a free run collection. This will result in an exception if the plugin is within an {@link NXDetector} that
 * is being scanned. The {@link #stop()} method will stop the freerun collection.
 * <p>
 * IMPORTANT: This class does not yet disable file writers!!! *
 * <p>
 * If used within a an NXDetector that uses any filewriter other than the once that impliticelty and controls camserser,
 * this class should not be used as is.
 */
public class ADPilatusFreerunControllerPlugin extends NullNXPlugin {

	private boolean operatingInScan = false;

	private final ADBase adBase;

	public ADPilatusFreerunControllerPlugin(ADBase adBase) {
		this.adBase = adBase;
	}

	@Override
	public String getName() {
		return "freerun";
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		operatingInScan = true;
	}

	@Override
	public void completeCollection() throws Exception {
		operatingInScan = false;
	}

	@Override
	public void stop() throws Exception {
		operatingInScan = false;
		getAdBase().stopAcquiring();
	}

	public void start(double collectionTime) throws Exception {
		if (operatingInScan) {
			String msg = "Failed to start a freerun collection as this detector is operating in a scan";
			InterfaceProvider.getTerminalPrinter().print("** " + msg + "**");
			throw new IllegalStateException(msg);
		}
		getAdBase().stopAcquiring();
		getAdBase().setAcquireTime(collectionTime);
		getAdBase().setTriggerMode(PilatusTriggerMode.ALIGNMENT.ordinal());
		getAdBase().startAcquiring();
	}

	public ADBase getAdBase() {
		return adBase;
	}
	
	@Override
	public String toString() {
		String str = "";
		if (operatingInScan) {
			str += "*disabled as scan running*";
		} else {
			str += "ready";
		}
		return str;
	}

}
