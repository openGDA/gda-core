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

import uk.ac.gda.tomography.service.message.TomographyRunMessage;

/**
 * Notifies registered listeners that a tomography acquisition configuration has started.
 *
 * @author Maurizio Nagni
 */
public class TomographyRunAcquisitionEvent extends ApplicationEvent {
	private final TomographyRunMessage tomographyRunMessage;

    public TomographyRunAcquisitionEvent(Object source, TomographyRunMessage tomographyRunMessage) {
		super(source);
		this.tomographyRunMessage = tomographyRunMessage;
	}

	public TomographyRunMessage getRunTomographyMessage() {
		return tomographyRunMessage;
	}
}
