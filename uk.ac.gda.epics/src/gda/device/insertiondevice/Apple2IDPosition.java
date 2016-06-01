/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.insertiondevice;

// Definition of the overall position of the ID
public class Apple2IDPosition {
	public final double gap;
	public final double topOuterPos;
	public final double topInnerPos;
	public final double bottomOuterPos;
	public final double bottomInnerPos;

	public Apple2IDPosition(double gap, double topOuterPos, double topInnerPos, double bottomOuterPos, double bottomInnerPos) {
		this.gap = gap;
		this.topOuterPos = topOuterPos;
		this.topInnerPos = topInnerPos;
		this.bottomOuterPos = bottomOuterPos;
		this.bottomInnerPos = bottomInnerPos;
	}

	@Override
	public String toString() {
		return String.format("gap: %.3f, topOuterPos: %.3f, topInnerPos: %.3f, bottomOuterPos: %.3f, bottomInnerPos: %.3f", gap, topOuterPos, topInnerPos,
				bottomOuterPos, bottomInnerPos);
	}

	@Override
	public Apple2IDPosition clone() {
		return new Apple2IDPosition(gap, topOuterPos, topInnerPos, bottomOuterPos, bottomInnerPos);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(bottomInnerPos);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(bottomOuterPos);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(gap);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(topInnerPos);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(topOuterPos);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		Apple2IDPosition other = (Apple2IDPosition) obj;
		if (Double.doubleToLongBits(bottomInnerPos) != Double.doubleToLongBits(other.bottomInnerPos))
			return false;
		if (Double.doubleToLongBits(bottomOuterPos) != Double.doubleToLongBits(other.bottomOuterPos))
			return false;
		if (Double.doubleToLongBits(gap) != Double.doubleToLongBits(other.gap))
			return false;
		if (Double.doubleToLongBits(topInnerPos) != Double.doubleToLongBits(other.topInnerPos))
			return false;
		if (Double.doubleToLongBits(topOuterPos) != Double.doubleToLongBits(other.topOuterPos))
			return false;
		return true;
	}
}