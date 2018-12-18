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

package gda.rcp.ncd;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.detectorsystem.NcdDetector;
import uk.ac.gda.server.ncd.subdetector.INcdSubDetector;

/**
 * Helper class for GUI to communicate with server
 */
public class NcdController {
	private static final Logger logger = LoggerFactory.getLogger(NcdController.class);
	private static NcdController instance = new NcdController();
	private NcdDetector ncdDetectorSystem;
	private Finder finder;
	public static final String NODETECTOR = "None";

	/**
	 * Returns the singleton instance of this class.
	 * 
	 * @return NcdController
	 */
	public static NcdController getInstance() {
		return instance;
	}

	/**
	 * private constructor to ensure we are a singleton
	 */
	private NcdController() {
		finder = Finder.getInstance();
		configure();
	}

	private void configure() {
		try {
			List<Findable> allDetectors;
			allDetectors = finder.listAllObjects("NcdDetector");
			for (Findable detector : allDetectors) {
				String detectorType = ((Detector) detector).getDetectorType();
				if ("SYS".equals(detectorType)) {
					ncdDetectorSystem = (NcdDetector) detector;
					break;
				}
			}
		} catch (Exception e) {
			logger.error("could not find NcdDetectorSystem", e);
		}
	}

	/**
	 * @return Returns the tfg.
	 */
	public Timer getTfg() {
		Timer tfg = null;
		if (ncdDetectorSystem != null) {
			try {
				String tfgName = ncdDetectorSystem.getTfgName();
				Finder finder = Finder.getInstance();
				tfg = (gda.device.Timer) finder.find(tfgName);
			} catch (DeviceException e) {
				logger.error("DeviceException in getTfg", e);
			}
		}
		return tfg;
	}

	/**
	 * @return ncdDetectorSystem
	 */
	public NcdDetector getNcdDetectorSystem() {
		return ncdDetectorSystem;
	}

	/**
	 * @param type
	 *            WAXS SAXS CALIB or SYS
	 * @return a collection of names of matching detectors available on the server
	 */
	public Collection<String> getDetectorNames(String type) {
		List<String> result = new Vector<String>();

		List<Findable> allNcdDetectors = finder.listAllObjects("INcdSubDetector");
		for (Findable detector : allNcdDetectors) {
			try {
				String detectorType = ((INcdSubDetector) detector).getDetectorType();
				if (type.equals(detectorType)) {
					result.add(detector.getName());
				}
			} catch (DeviceException e) {
				logger.error("could not get Detector type ", e);
			}
		}
		return result;
	}

	/**
	 * @param type
	 *            WAXS SAXS CALIB or SYS
	 * @return a name of a configured detector of that type on the NcdDetectorSystem
	 */
	public String getDetectorName(String type) {
		String detectorName = null;

		String script = "def getncddetoftype(detsys, type):\n\tfor ncdcontrollerloop in detsys.getDetectors():\n\t\tif ncdcontrollerloop.getDetectorType() == type:\n\t\t\treturn ncdcontrollerloop.getName()\n\n\n\n";
		JythonServerFacade.getInstance().runsource(script);
		detectorName = JythonServerFacade.getInstance().evaluateCommand(
				String.format("getncddetoftype(%s,\"%s\")", ncdDetectorSystem.getName(), type));

		return detectorName;
	}

	/**
	 * @param name
	 *            of detector to be added to the ncddetectorsystem
	 */
	public void addDetector(String name) {
		if (name != null && !"None".equalsIgnoreCase(name)) {
			JythonServerFacade.getInstance().runsource(
					String.format("%s.addDetector(finder.find(\"%s\"))", ncdDetectorSystem.getName(), name));
		}
	}

	/**
	 * @param name
	 *            detectorName to be removed from the ncddetectorsystem
	 */
	public void removeDetector(String name) {
		JythonServerFacade.getInstance().runsource(
				String.format("%s.removeDetector(finder.find(\"%s\"))", ncdDetectorSystem.getName(), name));
	}

	/**
	 * set the named detector as the only one of that type on the ncddetectorsystem in principle the ncddetectorsystem
	 * can have any number of waxs detectors (for example) this limits that.
	 * 
	 * @param type
	 *            detector type
	 * @param name
	 *            detector name
	 */
	public void setDetector(String type, String name) {
		String script = "for ncdcontrollerloop in %s.getDetectors()[:]:\n\tif ncdcontrollerloop.getDetectorType() == \"%s\":\n\t\t%s.removeDetector(ncdcontrollerloop)\n\n\n";
		script = String.format(script, ncdDetectorSystem.getName(), type, ncdDetectorSystem.getName());
		JythonServerFacade.getInstance().runsource(script);
		addDetector(name);
	}
	
	public boolean isDetectorConfigured(String name) {
		if (name != null && !"None".equalsIgnoreCase(name)) {
			String result = JythonServerFacade.getInstance().evaluateCommand(
					String.format("finder.find(\"%s\").isConfigured()", name));
			return Boolean.valueOf(result);
		}
		return false;
	}
	
	public INcdSubDetector getDetectorByName(String name) {
		if (name != null && !NODETECTOR.equals(name)) {
			return finder.find(name);
		}		
		return null;
	}
}