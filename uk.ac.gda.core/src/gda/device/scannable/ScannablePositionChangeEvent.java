/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.scannable;

import java.io.Serializable;
import java.util.Objects;

public class ScannablePositionChangeEvent implements Serializable{

	final public Serializable newPosition;

	public ScannablePositionChangeEvent(Serializable newPosition){
		this.newPosition = newPosition;
	}

	@Override
	public String toString() {
		return String.format("%s(newPosition=%s)", getClass().getSimpleName(), newPosition);
	}

	@Override
	public int hashCode() {
		return Objects.hash(newPosition);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScannablePositionChangeEvent other = (ScannablePositionChangeEvent) obj;
		return Objects.equals(newPosition, other.newPosition);
	}
}
