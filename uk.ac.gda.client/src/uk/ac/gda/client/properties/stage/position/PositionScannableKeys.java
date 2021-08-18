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

package uk.ac.gda.client.properties.stage.position;

import java.util.List;

/**
 * Defines the ScannableGroup/Properties which have to be associated with the specific {@link Position}
 *
 * @author Maurizio Nagni
 */
public class PositionScannableKeys {

	private Position position;
	private List<ScannableKeys> keys;
	public Position getPosition() {
		return position;
	}
	public void setPosition(Position position) {
		this.position = position;
	}
	public List<ScannableKeys> getKeys() {
		return keys;
	}
	public void setKeys(List<ScannableKeys> keys) {
		this.keys = keys;
	}

}
