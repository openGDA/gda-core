/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.beamline.health;

import gda.device.detector.DAServerStatusChecker;
import gda.factory.FactoryException;

public class DAServerCondition extends RateLimitedServerCondition {

	private DAServerStatusChecker statusChecker;
	private String host;
	private Integer port;

	@Override
	public void configure() throws FactoryException {
		if (statusChecker == null) {
			throw new FactoryException("Status checker is not set");
		}
		if (host == null) {
			throw new FactoryException("Host is not set");
		}
		if (port == null) {
			throw new FactoryException("Port is not set");
		}
		super.configure();
	}

	@Override
	protected boolean isServiceRunning() {
		final boolean running = statusChecker.checkStatus(host, port);
		if (!running) {
			setErrorMessage(statusChecker.getStatusMessage());
		}
		return running;
	}

	public void setStatusChecker(DAServerStatusChecker statusChecker) {
		this.statusChecker = statusChecker;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
