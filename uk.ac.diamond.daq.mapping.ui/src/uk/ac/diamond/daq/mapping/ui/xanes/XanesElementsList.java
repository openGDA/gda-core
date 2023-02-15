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

package uk.ac.diamond.daq.mapping.ui.xanes;

import java.util.List;
import java.util.Objects;

import gda.factory.Findable;
import gda.factory.FindableBase;

/**
 * Wraps a list of {@link XanesElement} in a {@link Findable} object
 */
public class XanesElementsList extends FindableBase {

	private List<XanesElement> xanesElements;

	public List<XanesElement> getXanesElements() {
		return xanesElements;
	}

	public void setXanesElements(List<XanesElement> xanesElements) {
		this.xanesElements = xanesElements;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(xanesElements);
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
		XanesElementsList other = (XanesElementsList) obj;
		return Objects.equals(xanesElements, other.xanesElements);
	}

	@Override
	public String toString() {
		return "XanesElementsList [xanesElements=" + xanesElements + "]";
	}

}
