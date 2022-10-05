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

package org.eclipse.scanning.api;

/**
 * Default implementations of functions in {@link INameable} and {@link ITimeoutable}
 * <p>
 * These implementations should be adequate for most classes.
 *
 * @since GDA 9.20
 */
public abstract class AbstractNameableTimeoutable extends AbstractNameable implements ITimeoutable {

	private long timeout = -1;

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (timeout ^ (timeout >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractNameableTimeoutable other = (AbstractNameableTimeoutable) obj;
		if (timeout != other.timeout)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractNameableTimeoutable [timeout=" + timeout + "]";
	}
}
