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

package uk.ac.diamond.daq.mapping.api;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.springframework.context.ApplicationEvent;

/**
 * Notifies listeners that a {@link ScanRequest} has been saved
 * and is ready for submission.
 */
public class ScanRequestSavedEvent extends ApplicationEvent {

	private final String scanName;
	private final ScanRequest scanRequest;

	public ScanRequestSavedEvent(Object source, String scanName, ScanRequest scanRequest) {
		super(source);
		this.scanName = scanName;
		this.scanRequest = scanRequest;
	}

	public String getScanName() {
		return scanName;
	}

	public ScanRequest getScanRequest() {
		return scanRequest;
	}

}
