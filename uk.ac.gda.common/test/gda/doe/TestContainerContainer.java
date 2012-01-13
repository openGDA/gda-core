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

package gda.doe;

import java.io.Serializable;

public class TestContainerContainer implements Serializable {

	private TestContainer testContainer;

	/**
	 * @return Returns the testContainer.
	 */
	public TestContainer getTestContainer() {
		return testContainer;
	}

	/**
	 * @param testContainer The testContainer to set.
	 */
	public void setTestContainer(TestContainer testContainer) {
		this.testContainer = testContainer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((testContainer == null) ? 0 : testContainer.hashCode());
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
		TestContainerContainer other = (TestContainerContainer) obj;
		if (testContainer == null) {
			if (other.testContainer != null)
				return false;
		} else if (!testContainer.equals(other.testContainer))
			return false;
		return true;
	}

}
