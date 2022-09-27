/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.device.scannable;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.InterfaceProvider;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(Scannable.class)
public class JythonScannableWrapper extends ScannableMotionBase {
	private static Logger logger =  LoggerFactory.getLogger(JythonScannableWrapper.class);
	private String scannableName;
	private Scannable scannable = null; // this cannot be set in configure() at bean creation as the Jython scannable is not
									// yet created!

	public JythonScannableWrapper() {
		// no-op, exist for Spring bean
	}

	public JythonScannableWrapper(String scannableName) {
		this.scannableName = scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	/**
	 * Returns an {@link Optional} of the {@link Scannable} as it may fail to find scannable in Jython namespace.
	 *
	 */
	Optional<Scannable> getScannable() {
		if (Objects.isNull(scannable)) {
			connectScannable();
		}
		return Optional.ofNullable(scannable);
	}

	/**
	 * connect to the Jython scannable for the scannable name set in this wrapper and add IOBserver to it.
	 * This method is required to re-connect this wrapper to Jython scannable on 'reset_namespace'.
	 * This method must be called in localStation.py to initialize this wrapper properly after the wrapped scannable is available in Jython namespace.
	 */
	public void connectScannable() {
		Object jythonObj = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
		if (jythonObj instanceof Scannable) {
			scannable = (Scannable) jythonObj;
			//initialize wrapper scannable attributes - this required to support Live Control GUI for this wrapper scannable!
			this.setInputNames(scannable.getInputNames());
			this.setExtraNames(scannable.getExtraNames());
			this.setOutputFormat(scannable.getOutputFormat());
			logger.debug("Add observer to scannable {}", scannableName);
			scannable.addIObserver(this::notifyIObservers);
		}
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		getScannable().orElseThrow().asynchronousMoveTo(externalPosition);
	}

	@Override
	public Object getPosition() throws DeviceException {
		return getScannable().orElseThrow().getPosition();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return getScannable().orElseThrow().isBusy();
	}

	@Override
	public void stop() throws DeviceException {
		getScannable().orElseThrow().stop();
	}
}
