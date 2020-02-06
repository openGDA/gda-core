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

package gda.rcp.views;

/**
 * Implements {@link ReservableControl}
 *
 * @author Maurizio Nagni
 */
public final class ReservedControl implements ReservableControl {
	private Object owner;

	@Override
	public boolean reserve(Object owner) {
		if (isReserved() && !isOwner(owner)) {
			return false;
		}
		this.owner = owner;
		return true;
	}

	@Override
	public boolean release(Object owner) {
		if (isReserved() && !isOwner(owner)) {
			return false;
		}
		this.owner = null;
		return true;
	}

	@Override
	public boolean isReserved() {
		return owner != null;
	}

	@Override
	public boolean isOwner(Object owner) {
		if (this.owner == null) {
			return false;
		}
		return this.owner.equals(owner);
	}
}
