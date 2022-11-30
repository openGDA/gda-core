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

package uk.ac.diamond.daq.mapping.api.document.event;

import org.springframework.context.ApplicationEvent;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;

/**
 *   Event published by components manipulating a {@link ScanningAcquisition}
 *
 * @author Maurizio Nagni
 */
public class ScanningAcquisitionChangeEvent extends ApplicationEvent {

	public enum UpdatedProperty {
		PATH,

		DETECTOR_EXPOSURE
	}

	private final UpdatedProperty property;

	public ScanningAcquisitionChangeEvent(Object source, UpdatedProperty property) {
		super(source);
		this.property = property;
	}

	public UpdatedProperty getProperty() {
		return property;
	}

}
