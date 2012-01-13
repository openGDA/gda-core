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
import java.util.List;

public class TestContainerList implements Serializable {

	private List<TestContainer> testContainers;

	/**
	 * @return Returns the testContainers.
	 */
	public List<TestContainer> getTestContainers() {
		return testContainers;
	}

	/**
	 * @param testContainers The testContainers to set.
	 */
	public void setTestContainers(List<TestContainer> testContainers) {
		this.testContainers = testContainers;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((testContainers == null) ? 0 : testContainers.hashCode());
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
		TestContainerList other = (TestContainerList) obj;
		if (testContainers == null) {
			if (other.testContainers != null)
				return false;
		} else if (!testContainers.equals(other.testContainers))
			return false;
		return true;
	}

	
}
