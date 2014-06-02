/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.hrpd.cvscan;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import gda.scan.Scan.ScanStatus;

public class ScanStatusProvider implements IScanStatusProvider {

	private ScanStatus status;
	private static final Logger logger=LoggerFactory.getLogger(ScanStatusProvider.class);

	@Override
	public ScanStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(ScanStatus newStatus) {
		if (this.status.possibleFollowUps().contains(newStatus)) {
			this.status = newStatus;
			// notify Command (Jython) Server that the status has changed
			InterfaceProvider.getJythonServerNotifer().notifyServer(this, this.getStatus().asJython());
		} else {
			String msg = MessageFormat.format("Scan status change from {0} to {1} is not expected", this.status.name(),
					newStatus.name());
			logger.error(msg);
		}
	}
}
