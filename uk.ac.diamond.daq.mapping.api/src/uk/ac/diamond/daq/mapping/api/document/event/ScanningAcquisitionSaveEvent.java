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

/**
 * Notifies registered listeners that a diffraction acquisition configuration has been saved.
 *
 * @author Maurizio Nagni
 */
public class ScanningAcquisitionSaveEvent extends ScanningAcquisitionEvent {
	private final String name;
	private final String acquisitionConfiguration;

	public ScanningAcquisitionSaveEvent(Object source, String name, String acquisitionConfiguration) {
		super(source);
		this.name = name;
		this.acquisitionConfiguration = acquisitionConfiguration;
	}

	public String getName() {
		return name;
	}

	public String getAcquisitionConfiguration() {
		return acquisitionConfiguration;
	}
}
