/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.cirrus;

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableStatus;
import gda.factory.FactoryException;

/**
 * The Jython-level interface to the Cirrus gardware for users.
 * <p>
 * Implements the Scannable interface so it may also run in scans, but this has otehr functionality for users outside of
 * scans.
 */
public class CirrusScannable extends ScannableBase implements Cirrus {

	private CirrusController controller;
	private String cirrusHost;
	private Integer[] masses = new Integer[] {};

	public CirrusScannable() {
		this.inputNames = new String[] {};
		this.extraNames = new String[] {};
		this.outputFormat = new String[] {};
	}

	@Override
	public void configure() throws FactoryException {
		if (cirrusHost == null || cirrusHost.isEmpty()) {
			throw new FactoryException("Cirrus host IP address not defined - cannot connect.");
		}

		if (controller == null) {
			throw new FactoryException("CirrusController object not supplied - cannot connect.");
		}

		controller.connect(cirrusHost);
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {

		// create a measurement and set the masses
		if (masses.length == 0) {
			throw new DeviceException("No masses defined - cannot take a measurement.");
		}

		// add the measurement to the scan and start it
		controller.createAndRunScan(masses);
	}

	@Override
	public Object getPosition() throws DeviceException {
		// TODO Auto-generated method stub
		controller.getCurrentState().getLastMeasurement();
		return super.getPosition();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return controller.getCurrentState().getStatus() == ScannableStatus.BUSY;
	}

	@Override
	public void stop() throws DeviceException {
		controller.stop();
	}

	public void setController(CirrusController controller) {
		this.controller = controller;
	}

	public CirrusController getController() {
		return controller;
	}

	public void setCirrusHost(String cirrusHost) {
		this.cirrusHost = cirrusHost;
	}

	public String getCirrusHost() {
		return cirrusHost;
	}

	@Override
	public void setMasses(Integer[] masses) {
		this.masses = masses;
	}

	@Override
	public Integer[] getMasses() {
		return masses;
	}

}
