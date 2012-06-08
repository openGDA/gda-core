/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.data.metadata;

import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.jython.Jython;
import gda.jython.JythonServerStatus;
import gda.observable.IObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The content of this metadata item is reset after each scan.
 */
public class StoredScanMetadataEntry extends StoredMetadataEntry implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(StoredScanMetadataEntry.class);

	private String resetValue = "";
	private boolean scanRunning = false;
	
	@Override
	public void configure() throws FactoryException {
		super.configure();
		InterfaceProvider.getJSFObserver().addIObserver(this);
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof JythonServerStatus) {
			JythonServerStatus jss = (JythonServerStatus) arg;
			logger.debug("received {}", jss);
			boolean nowIdle = (jss.scanStatus == Jython.IDLE);
			if (scanRunning && nowIdle) {
				setValue(resetValue);
			}
			scanRunning = !nowIdle;
		}
	}

	public void setResetValue(String resetValue) {
		this.resetValue = resetValue;
	}

	public String getResetValue() {
		return resetValue;
	}
}
