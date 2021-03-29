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

package uk.ac.diamond.daq.mapping.ui.stage.enumeration;

import uk.ac.gda.ui.tool.ClientMessages;

/**
 * A collection of standard position definitions
 *
 * @author Maurizio Nagni
 */
public enum Position {

	DEFAULT(ClientMessages.EMPTY_MESSAGE, ClientMessages.EMPTY_MESSAGE),
	OUT_OF_BEAM(ClientMessages.OUT_OF_BEAM_POSITION, ClientMessages.OUT_OF_BEAM_POSITION_TP),
	START(ClientMessages.IN_BEAM, ClientMessages.START_POSITION_TP),
	END(ClientMessages.EMPTY_MESSAGE, ClientMessages.EMPTY_MESSAGE),
	Open(ClientMessages.EMPTY_MESSAGE, ClientMessages.EMPTY_MESSAGE),
	Close(ClientMessages.EMPTY_MESSAGE, ClientMessages.EMPTY_MESSAGE);

	private final ClientMessages name;
	private final ClientMessages tooltip;

	private Position(ClientMessages name, ClientMessages tooltip) {
		this.name = name;
		this.tooltip = tooltip;
	}

	public ClientMessages getName() {
		return name;
	}

	public ClientMessages getTooltip() {
		return tooltip;
	}
}
