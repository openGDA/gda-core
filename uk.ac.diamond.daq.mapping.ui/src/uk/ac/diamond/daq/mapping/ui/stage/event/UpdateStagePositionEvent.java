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

package uk.ac.diamond.daq.mapping.ui.stage.event;

import uk.ac.diamond.daq.mapping.ui.controller.StageController;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Position;

/**
 * An event published when a {@link Position} has been updated
 *
 * @see StageController#savePosition(Position)
 *
 * @author Maurizio Nagni
 */
public class UpdateStagePositionEvent extends StageEvent {

	private final Position position;

	public UpdateStagePositionEvent(Object source, Position position) {
		super(source);
		this.position = position;
	}

	/**
	 * Return the updated position of this event
	 * @return the updated position
	 */
	public Position getPosition() {
		return position;
	}
}
