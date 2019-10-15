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

package uk.ac.gda.tomography.event;

import org.springframework.context.ApplicationEvent;

/**
 *  Notifies registered listeners that a tomography acquisition configuration has been saved.
 *
 * @author Maurizio Nagni
 */
public class TomographySaveEvent extends ApplicationEvent {
	private final String name;
	private final String acquisitionConfiguration;
	private final String scriptPath;

    public TomographySaveEvent(Object source, String name, String acquisitionConfiguration, String scriptPath) {
		super(source);
		this.name = name;
		this.acquisitionConfiguration = acquisitionConfiguration;
		this.scriptPath = scriptPath;
	}

	public String getName() {
		return name;
	}

	public String getAcquisitionConfiguration() {
		return acquisitionConfiguration;
	}

	public String getScriptPath() {
		return scriptPath;
	}
}
